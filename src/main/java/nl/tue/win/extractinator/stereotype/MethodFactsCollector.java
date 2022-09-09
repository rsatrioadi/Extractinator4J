package nl.tue.win.extractinator.stereotype;

import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.declarations.ResolvedMethodLikeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import nl.tue.win.collections.StringList;
import nl.tue.win.extractinator.graph.Resolver;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
                StringList words = new StringList(we.getOutput(stopWords));
                f.put(MethodFacts.Type.words,
                        words);
                f.put(MethodFacts.Type.numUniqueWords,
                        (long) new HashSet<>(words).size());

                f.put(MethodFacts.Type.isAbstract,
                        decl.isAbstract());
                f.put(MethodFacts.Type.isFinal,
                        decl.isFinal());
                f.put(MethodFacts.Type.isStatic,
                        decl.isStatic());

                f.put(MethodFacts.Type.isNamedGetter,
                        decl.getNameAsString().matches("^(get|is)[A-Z].*"));
                f.put(MethodFacts.Type.isNamedSetter,
                        decl.getNameAsString().matches("^set[A-Z].*"));


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
                        .flatMap(fd -> fd.getVariables().stream().map(VariableDeclarator::getType)).toList();

                f.put(MethodFacts.Type.numVars,
                        (long) varTypes.size());
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
                        .map(Parameter::getType).toList();

                f.put(MethodFacts.Type.numParams,
                        (long) paramTypes.size());
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


                //isGetter, isPredicate, isProperty, isSetter, isCommand,
                AtomicBoolean isGetter = new AtomicBoolean(false);
                if (decl.isPublic()) {
                    decl.findAll(ReturnStmt.class)
                            .forEach(s -> s.getExpression().ifPresent(expr -> {
                                try {
                                    if (expr.isFieldAccessExpr()) {
                                        isGetter.set(true);
                                    }
                                    else if (expr instanceof NameExpr ne) {
                                        if (ne.resolve().isField()) {
                                            isGetter.set(true);
                                        }
                                    }
                                } catch (Exception e) {
                                    System.err.println(e.getMessage());
                                }
                            }));
                }

                f.put(MethodFacts.Type.isGetter,
                        isGetter.get());

                //isFactory, numCtorCalls,
                //numAssignmentsToField, numCallsToField, numFieldsInvolved, numInternalCalls, numExternalCalls,
                //numCallsToParam, numCallsToVars,
                //numMathOperations, numBoolOperations, numComparisons, numAssignments

            } catch (Throwable e) {
                System.err.println("unable to resolve " + decl.getSignature());
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
