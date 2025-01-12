package nl.tue.win.extractinator.stereotype;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import nl.tue.win.collections.StringList;
import nl.tue.win.extractinator.graph.Resolver;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassFactsCollector extends VoidVisitorAdapter<Map<String, ClassFacts>> {

    private static final List<BinaryExpr.Operator> mathOperations = List.of(BinaryExpr.Operator.PLUS, BinaryExpr.Operator.MINUS, BinaryExpr.Operator.MULTIPLY, BinaryExpr.Operator.DIVIDE, BinaryExpr.Operator.REMAINDER);
    private static final List<BinaryExpr.Operator> boolOperations = List.of(BinaryExpr.Operator.AND, BinaryExpr.Operator.OR, BinaryExpr.Operator.XOR);
    private static final List<BinaryExpr.Operator> comparisons = List.of(BinaryExpr.Operator.EQUALS, BinaryExpr.Operator.NOT_EQUALS, BinaryExpr.Operator.GREATER, BinaryExpr.Operator.GREATER_EQUALS, BinaryExpr.Operator.LESS, BinaryExpr.Operator.LESS_EQUALS);
    public static Function<ResolvedReferenceTypeDeclaration, List<ResolvedReferenceType>> resolvedTraverser = (rrtd) -> {
        List<ResolvedReferenceType> ancestors = new ArrayList<>();
        // We want to avoid infinite recursion in case of Object having Object as ancestor
        if (!rrtd.isJavaLangObject()) {
            for (ResolvedReferenceType ancestor : rrtd.getAncestors(true)) {
                List<ResolvedReferenceType> moreAncestors = new ArrayList<>();
                moreAncestors.add(ancestor);
                while (!moreAncestors.isEmpty()) {
                    ancestors.addAll(moreAncestors);
                    moreAncestors = moreAncestors.stream()
                            .filter(a -> !a.isJavaLangObject())
                            .flatMap(a -> a.getTypeDeclaration().stream()
                                    .flatMap(t -> t.getAncestors(true).stream()))
                            .distinct()
                            .collect(Collectors.toList());
                }
            }
        }
        return ancestors;
    };
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
    private String currentClass;

    {
        config = new DefaultPrinterConfiguration();
        config.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_JAVADOC));
        config.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.PRINT_COMMENTS));
        config.addOption(new DefaultConfigurationOption(DefaultPrinterConfiguration.ConfigOption.SPACE_AROUND_OPERATORS));
    }

    private void visitStructure(TypeDeclaration<?> decl, Map<String, ClassFacts> facts) {

        new Resolver<>((Resolvable<ResolvedReferenceTypeDeclaration>) decl).getResolution().ifPresent(cls -> {

            currentClass = cls.getQualifiedName();
            if (!facts.containsKey(currentClass)) {
                facts.put(currentClass, new ClassFacts());
            }
            ClassFacts f = facts.get(currentClass);

            WordsExtractor we = new WordsExtractor(decl.toString(config));
            StringList words = new StringList(we.getOutput(stopWords));
            f.put(ClassFacts.Type.words,
                    words);
            f.put(ClassFacts.Type.numUniqueWords,
                    (long) new HashSet<>(words).size());

            f.put(ClassFacts.Type.numExpressions,
                    decl.stream()
                            .filter(n -> n instanceof Expression)
                            .distinct()
                            .count());
            f.put(ClassFacts.Type.numStatements,
                    decl.stream()
                            .filter(n -> n instanceof Statement)
                            .distinct()
                            .count());
            f.put(ClassFacts.Type.numLoops,
                    decl.stream()
                            .filter(n -> n instanceof ForStmt
                                    || n instanceof WhileStmt
                                    || n instanceof ForEachStmt
                                    || n instanceof DoStmt)
                            .distinct()
                            .count());
            f.put(ClassFacts.Type.numConditionals,
                    decl.stream()
                            .filter(n -> n instanceof IfStmt
                                    || n instanceof SwitchStmt)
                            .distinct()
                            .count());
            f.put(ClassFacts.Type.numAssignemnts,
                    decl.stream()
                            .filter(n -> n instanceof AssignExpr)
                            .distinct()
                            .count());

            f.put(ClassFacts.Type.isClass,
                    cls.isClass());
            f.put(ClassFacts.Type.isInterface,
                    cls.isInterface());
            f.put(ClassFacts.Type.isEnum,
                    cls.isEnum());
            f.put(ClassFacts.Type.isAnonymous,
                    cls.isAnonymousClass());

            f.put(ClassFacts.Type.isAbstract,
                    decl.hasModifier(Modifier.Keyword.ABSTRACT));
            f.put(ClassFacts.Type.isFinal,
                    decl.hasModifier(Modifier.Keyword.FINAL));
            f.put(ClassFacts.Type.isStatic,
                    decl.hasModifier(Modifier.Keyword.STATIC));

            f.put(ClassFacts.Type.isCollection,
                    cls.getAllAncestors(resolvedTraverser).stream()
                            .filter(ResolvedReferenceType::isReferenceType)
                            .map(ResolvedReferenceType::getQualifiedName)
                            .anyMatch(n -> n.equals("java.util.Collection")));
            f.put(ClassFacts.Type.isMap,
                    cls.getAllAncestors(resolvedTraverser).stream()
                            .filter(ResolvedReferenceType::isReferenceType)
                            .map(ResolvedReferenceType::getQualifiedName)
                            .anyMatch(n -> n.equals("java.util.Map")));

            f.put(ClassFacts.Type.specializesListener,
                    cls.getAllAncestors(resolvedTraverser).stream()
                            .filter(ResolvedReferenceType::isReferenceType)
                            .map(ResolvedReferenceType::getQualifiedName)
                            .anyMatch(n -> n.endsWith("Listener") || n.endsWith("Observer")));
            f.put(ClassFacts.Type.specializesAdapter,
                    cls.getAllAncestors(resolvedTraverser).stream()
                            .filter(ResolvedReferenceType::isReferenceType)
                            .map(ResolvedReferenceType::getQualifiedName)
                            .anyMatch(n -> n.endsWith("Adapter")));

            f.put(ClassFacts.Type.isNamedManager,
                    cls.getClassName().endsWith("Manager"));
            f.put(ClassFacts.Type.isNamedController,
                    cls.getClassName().endsWith("Controller"));

            List<VariableDeclarator> fieldDeclarations = decl.getFields().stream()
                    .flatMap(fd -> fd.getVariables().stream()).toList();
            List<ResolvedType> fields = fieldDeclarations.stream()
                    .map(fd -> new Resolver<>(fd.getType()).getResolution())
                    .filter(Optional::isPresent)
                    .map(Optional::get).toList();

            f.put(ClassFacts.Type.numFields,
                    fieldDeclarations.size());
            f.put(ClassFacts.Type.numBooleanFields,
                    fields.stream()
                            .filter(ResolvedType::isPrimitive)
                            .filter(fld -> fld.asPrimitive().getBoxTypeQName().equals("java.lang.Boolean"))
                            .count());
            f.put(ClassFacts.Type.numPrimitiveFields,
                    fields.stream()
                            .filter(ResolvedType::isPrimitive)
                            .count());
            f.put(ClassFacts.Type.numStringFields,
                    fields.stream()
                            .filter(ResolvedType::isReferenceType)
                            .filter(fld -> fld.asReferenceType().getQualifiedName().equals("java.lang.String"))
                            .count());
            f.put(ClassFacts.Type.numCollectionFields,
                    fields.stream()
                            .filter(ResolvedType::isReferenceType)
                            .map(typ -> typ.asReferenceType().getTypeDeclaration())
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .filter(typ -> typ.getAllAncestors(resolvedTraverser).stream()
                                    .map(ResolvedReferenceType::getQualifiedName)
                                    .anyMatch(n -> n.equals("java.util.Collection")))
                            .count());
            f.put(ClassFacts.Type.numMapFields,
                    fields.stream()
                            .filter(ResolvedType::isReferenceType)
                            .map(typ -> typ.asReferenceType().getTypeDeclaration())
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .filter(typ -> typ.getAllAncestors(resolvedTraverser).stream()
                                    .map(ResolvedReferenceType::getQualifiedName)
                                    .anyMatch(n -> n.equals("java.util.Map")))
                            .count());
            f.put(ClassFacts.Type.numArrayFields,
                    fields.stream()
                            .filter(ResolvedType::isArray)
                            .count());

            f.put(ClassFacts.Type.numMethods,
                    (long) decl.getMethods().size());
            f.put(ClassFacts.Type.numHiddenMethods,
                    decl.getMethods().stream()
                            .filter(m -> m.hasModifier(Modifier.Keyword.PRIVATE) || m.hasModifier(Modifier.Keyword.PROTECTED))
                            .count());
            f.put(ClassFacts.Type.numVisibleMethods,
                    decl.getMethods().stream()
                            .filter(m -> m.hasModifier(Modifier.Keyword.PUBLIC))
                            .count());
            f.put(ClassFacts.Type.numStaticMethods,
                    decl.getMethods().stream()
                            .filter(m -> m.hasModifier(Modifier.Keyword.STATIC))
                            .count());

            f.put(ClassFacts.Type.numAncestors,
                    (long) cls.getAllAncestors(resolvedTraverser).size());

            f.put(ClassFacts.Type.numNamedGetters,
                    decl.getMethods().stream()
                            .filter(m -> m.getNameAsString().matches("^(get|is)[A-Z].*"))
                            .count());
            f.put(ClassFacts.Type.numNamedSetters,
                    decl.getMethods().stream().filter(m -> m.getNameAsString().matches("^set[A-Z].*"))
                            .count());


            f.put(ClassFacts.Type.numHiddenFields,
                    decl.getFields().stream()
                            .filter(m -> m.hasModifier(Modifier.Keyword.PRIVATE) || m.hasModifier(Modifier.Keyword.PROTECTED))
                            .mapToLong(m -> m.getVariables().size())
                            .sum());
            f.put(ClassFacts.Type.numVisibleFields,
                    decl.getFields().stream()
                            .filter(m -> m.hasModifier(Modifier.Keyword.PUBLIC))
                            .mapToLong(m -> m.getVariables().size())
                            .sum());
            f.put(ClassFacts.Type.numStaticFields,
                    decl.getFields().stream()
                            .filter(m -> m.hasModifier(Modifier.Keyword.STATIC))
                            .mapToLong(m -> m.getVariables().size())
                            .sum());

            // numMathOperation, numBoolOperation, numComparison
            f.put(ClassFacts.Type.numMathOperations, (long) 0);
            f.put(ClassFacts.Type.numBoolOperations, (long) 0);
            f.put(ClassFacts.Type.numComparisons, (long) 0);

            f.put(ClassFacts.Type.invokesIO, false);
            f.put(ClassFacts.Type.numOutboundCalls, (long) 0);
            f.put(ClassFacts.Type.numCtorCalls, (long) 0);
        });
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration decl, Map<String, ClassFacts> facts) {
        String prevClass = currentClass;
        currentClass = null;
        visitStructure(decl, facts);
        super.visit(decl, facts);
        currentClass = prevClass;
    }

    @Override
    public void visit(EnumDeclaration decl, Map<String, ClassFacts> facts) {
        String prevClass = currentClass;
        currentClass = null;
        visitStructure(decl, facts);
        super.visit(decl, facts);
        currentClass = prevClass;
    }

    @Override
    public void visit(MethodCallExpr expr, Map<String, ClassFacts> facts) {
        new Resolver<>(expr).getResolution()
                .ifPresent(methodCall -> {
                    try {
                        ClassFacts f = facts.get(currentClass);
                        String ownerName = methodCall.declaringType().asReferenceType().getQualifiedName();

                        if (ownerName.startsWith("java.io")) {
                            f.put(ClassFacts.Type.invokesIO, true);
                        }

                        if (!ownerName.equals(currentClass)) {
                            f.put(ClassFacts.Type.numOutboundCalls, ((long) f.getOrDefault(ClassFacts.Type.numOutboundCalls, (long) 0)) + 1);
                        }

                        if (expr.getName().asString().equals("equals")) {
                            f.put(ClassFacts.Type.numComparisons, ((long) f.getOrDefault(ClassFacts.Type.numComparisons, (long) 0)) + 1);
                        }
                    } catch (Exception e) {
                        System.err.println("unable to resolve method call " + expr.getNameAsString());
                    }
                });
        super.visit(expr, facts);
    }

    @Override
    public void visit(ObjectCreationExpr expr, Map<String, ClassFacts> facts) {
        new Resolver<>(expr).getResolution()
                .ifPresent(methodCall -> {
                    try {
                        ClassFacts f = facts.get(currentClass);
                        String ownerName = methodCall.declaringType().asReferenceType().getQualifiedName();

                        if (ownerName.startsWith("java.io")) {
                            f.put(ClassFacts.Type.invokesIO, true);
                        }

                        if (!ownerName.equals(currentClass)) {
                            f.put(ClassFacts.Type.numCtorCalls, ((long) f.getOrDefault(ClassFacts.Type.numCtorCalls, (long) 0)) + 1);
                        }
                    } catch (Exception e) {
                        System.err.println("unable to resolve ctor call " + expr.getTypeAsString());
                    }
                });
        super.visit(expr, facts);
    }

    @Override
    public void visit(BinaryExpr expr, Map<String, ClassFacts> facts) {
        ClassFacts f = facts.get(currentClass);
        if (mathOperations.contains(expr.getOperator())) {
            f.put(ClassFacts.Type.numMathOperations, ((long) f.getOrDefault(ClassFacts.Type.numMathOperations, (long) 0)) + 1);
        }
        if (boolOperations.contains(expr.getOperator())) {
            f.put(ClassFacts.Type.numBoolOperations, ((long) f.getOrDefault(ClassFacts.Type.numBoolOperations, (long) 0)) + 1);
        }
        if (comparisons.contains(expr.getOperator())) {
            f.put(ClassFacts.Type.numComparisons, ((long) f.getOrDefault(ClassFacts.Type.numComparisons, (long) 0)) + 1);
        }
        super.visit(expr, facts);
    }
}
