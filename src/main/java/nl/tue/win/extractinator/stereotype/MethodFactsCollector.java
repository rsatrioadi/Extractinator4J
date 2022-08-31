package nl.tue.win.extractinator.stereotype;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.declarations.ResolvedMethodLikeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import nl.tue.win.extractinator.graph.Resolver;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MethodFactsCollector extends VoidVisitorAdapter<Map<String, MethodFacts>> {

    private final Set<String> stopWords = Set.of("a", "about", "above", "after", "again", "against", "all", "am", "an",
            "and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between",
            "both", "but", "by", "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't",
            "doing", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has",
            "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers",
            "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into",
            "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself",
            "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves",
            "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so",
            "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there",
            "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to",
            "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were",
            "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's",
            "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've",
            "your", "yours", "yourself", "yourselves");
    private final DefaultPrinterConfiguration config;

    {
        config = new DefaultPrinterConfiguration();
        config.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_JAVADOC));
        config.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS));
        config.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.SPACE_AROUND_OPERATORS));
    }

    private void visitOperation(CallableDeclaration<?> decl, Map<String, MethodFacts> facts) {
        new Resolver<>((Resolvable<ResolvedMethodLikeDeclaration>) decl).getResolution().ifPresent(method -> {
            try {
                ResolvedReferenceTypeDeclaration type = method.declaringType();
                String key = String.format("%s#%s", type.getQualifiedName(), decl.getSignature());
                if (!facts.containsKey(key)) facts.put(key, new MethodFacts());
                MethodFacts f = facts.get(key);

                WordsExtractor we = new WordsExtractor(decl.toString(config));
                f.put(MethodFacts.Type.words, String.join(" ", we.getOutput(stopWords)));

                f.put(MethodFacts.Type.isAbstract,
                        decl.hasModifier(Modifier.Keyword.ABSTRACT));
                f.put(MethodFacts.Type.isFinal,
                        decl.hasModifier(Modifier.Keyword.FINAL));
                f.put(MethodFacts.Type.isStatic,
                        decl.hasModifier(Modifier.Keyword.STATIC));

                f.put(MethodFacts.Type.numExpressions,
                        decl.stream()
                                .filter(n -> n instanceof Expression)
                                .distinct()
                                .count());
                f.put(MethodFacts.Type.numStatements,
                        decl.stream()
                                .filter(n -> n instanceof Statement)
                                .distinct()
                                .count());
                f.put(MethodFacts.Type.numLoops,
                        decl.stream()
                                .filter(n -> n instanceof ForStmt
                                        || n instanceof WhileStmt
                                        || n instanceof ForEachStmt
                                        || n instanceof DoStmt)
                                .distinct()
                                .count());
                f.put(MethodFacts.Type.numConditionals,
                        decl.stream()
                                .filter(n -> n instanceof IfStmt
                                        || n instanceof SwitchStmt)
                                .distinct()
                                .count());

                List<Type> varTypes = decl.stream()
                        .filter(fd -> fd instanceof VariableDeclarationExpr)
                        .map(fd -> (VariableDeclarationExpr) fd)
                        .flatMap(fd -> fd.getVariables().stream().map(VariableDeclarator::getType))
                        .collect(Collectors.toUnmodifiableList());

                f.put(MethodFacts.Type.numVars,
                        varTypes.size());
                f.put(MethodFacts.Type.numBooleanVars,
                        varTypes.stream()
                                .filter(Type::isPrimitiveType)
                                .filter(typ -> typ.asPrimitiveType().resolve().getBoxTypeQName().equals("java.lang.Boolean"))
                                .count());
                f.put(MethodFacts.Type.numPrimitiveVars,
                        varTypes.stream()
                                .filter(Type::isPrimitiveType)
                                .count());
                f.put(MethodFacts.Type.numStringVars,
                        varTypes.stream()
                                .map(typ -> new Resolver<>(typ).getResolution())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(typ -> typ.isReferenceType() && typ.asReferenceType().getQualifiedName().equals("java.lang.String"))
                                .count());
                f.put(MethodFacts.Type.numCollectionVars,
                        varTypes.stream()
                                .map(typ -> new Resolver<>(typ).getResolution())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(ResolvedType::isReferenceType)
                                .map(typ -> typ.asReferenceType().getTypeDeclaration())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(typ -> typ.getAllAncestors(ClassFactsCollector.resolvedTraverser).stream()
                                        .map(ResolvedReferenceType::getQualifiedName)
                                        .anyMatch(n -> n.equals("java.util.Collection")))
                                .count());
                f.put(MethodFacts.Type.numMapVars,
                        varTypes.stream()
                                .map(typ -> new Resolver<>(typ).getResolution())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(ResolvedType::isReferenceType)
                                .map(typ -> typ.asReferenceType().getTypeDeclaration())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(typ -> typ.getAllAncestors(ClassFactsCollector.resolvedTraverser).stream()
                                        .map(ResolvedReferenceType::getQualifiedName)
                                        .anyMatch(n -> n.equals("java.util.Map")))
                                .count());
                f.put(MethodFacts.Type.numArrayVars,
                        varTypes.stream()
                                .filter(Type::isArrayType)
                                .count());

                List<Type> paramTypes = decl.getParameters().stream()
                        .map(Parameter::getType)
                        .collect(Collectors.toUnmodifiableList());

                f.put(MethodFacts.Type.numParams,
                        paramTypes.size());
                f.put(MethodFacts.Type.numBooleanParams,
                        paramTypes.stream()
                                .filter(Type::isPrimitiveType)
                                .filter(typ -> typ.asPrimitiveType().resolve().getBoxTypeQName().equals("java.lang.Boolean"))
                                .count());
                f.put(MethodFacts.Type.numPrimitiveParams,
                        paramTypes.stream()
                                .filter(Type::isPrimitiveType)
                                .count());
                f.put(MethodFacts.Type.numStringParams,
                        paramTypes.stream()
                                .map(typ -> new Resolver<>(typ).getResolution())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(typ -> typ.isReferenceType() && typ.asReferenceType().getQualifiedName().equals("java.lang.String"))
                                .count());
                f.put(MethodFacts.Type.numCollectionParams,
                        paramTypes.stream()
                                .map(typ -> new Resolver<>(typ).getResolution())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(ResolvedType::isReferenceType)
                                .map(typ -> typ.asReferenceType().getTypeDeclaration())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(typ -> typ.getAllAncestors(ClassFactsCollector.resolvedTraverser).stream()
                                        .map(ResolvedReferenceType::getQualifiedName)
                                        .anyMatch(n -> n.equals("java.util.Collection")))
                                .count());
                f.put(MethodFacts.Type.numMapParams,
                        paramTypes.stream()
                                .map(typ -> new Resolver<>(typ).getResolution())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(ResolvedType::isReferenceType)
                                .map(typ -> typ.asReferenceType().getTypeDeclaration())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(typ -> typ.getAllAncestors(ClassFactsCollector.resolvedTraverser).stream()
                                        .map(ResolvedReferenceType::getQualifiedName)
                                        .anyMatch(n -> n.equals("java.util.Map")))
                                .count());
                f.put(MethodFacts.Type.numArrayParams,
                        paramTypes.stream()
                                .filter(Type::isArrayType)
                                .count());

            } catch (Throwable e) {
                e.printStackTrace(System.err);
            }
        });

    }

    @Override
    public void visit(MethodDeclaration decl, Map<String, MethodFacts> facts) {
        visitOperation(decl, facts);
        super.visit(decl, facts);
    }

    @Override
    public void visit(ConstructorDeclaration decl, Map<String, MethodFacts> facts) {
        visitOperation(decl, facts);
        super.visit(decl, facts);
    }

    @Override
    public void visit(InitializerDeclaration decl, Map<String, MethodFacts> facts) {
        // TODO
        super.visit(decl, facts);
    }
}
