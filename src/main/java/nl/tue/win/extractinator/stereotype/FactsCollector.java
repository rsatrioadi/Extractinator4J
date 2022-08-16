package nl.tue.win.extractinator.stereotype;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import nl.tue.win.extractinator.graph.Resolver;

import java.util.Map;
import java.util.Set;

public class FactsCollector extends VoidVisitorAdapter<Map<String, Facts>> {

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

    @Override
    public void visit(ClassOrInterfaceDeclaration decl, Map<String, Facts> facts) {

        currentClass = null;

        new Resolver<>(decl).getResolution().ifPresent(cls -> {

            currentClass = cls.getQualifiedName();
            if (!facts.containsKey(currentClass)) facts.put(currentClass, new Facts());
            Facts f = facts.get(currentClass);

            WordsExtractor we = new WordsExtractor(decl.toString(config));
            f.setWords(we.getOutput(stopWords));

            f.setNumExpressions(decl.stream().filter(n -> n instanceof Expression).distinct().count());
            f.setNumStatements(decl.stream().filter(n -> n instanceof Statement).distinct().count());
            f.setNumLoops(decl.stream().filter(n -> n instanceof ForStmt
                    || n instanceof WhileStmt
                    || n instanceof ForEachStmt
                    || n instanceof DoStmt).distinct().count());
            f.setNumConditionals(decl.stream().filter(n -> n instanceof IfStmt
                    || n instanceof SwitchStmt).distinct().count());

        });

        super.visit(decl, facts);
    }

    @Override
    public void visit(EnumDeclaration decl, Map<String, Facts> facts) {

        currentClass = null;

        new Resolver<>(decl).getResolution().ifPresent(enm -> {

            currentClass = enm.getQualifiedName();
            if (!facts.containsKey(currentClass)) facts.put(currentClass, new Facts());
            Facts f = facts.get(currentClass);

            WordsExtractor we = new WordsExtractor(decl.toString(config));
            f.setWords(we.getOutput(stopWords));

            f.setNumExpressions(decl.stream().filter(n -> n instanceof Expression).distinct().count());
            f.setNumStatements(decl.stream().filter(n -> n instanceof Statement).distinct().count());
            f.setNumLoops(decl.stream().filter(n -> n instanceof ForStmt
                    || n instanceof WhileStmt
                    || n instanceof ForEachStmt
                    || n instanceof DoStmt).distinct().count());
            f.setNumConditionals(decl.stream().filter(n -> n instanceof IfStmt
                    || n instanceof SwitchStmt).distinct().count());

        });

        super.visit(decl, facts);
    }

    @Override
    public void visit(MethodDeclaration decl, Map<String, Facts> facts) {

        new Resolver<>(decl).getResolution().ifPresent(method -> {
            try {
                ResolvedReferenceTypeDeclaration type = method.declaringType();
                if (type.getQualifiedName().equals(currentClass)) {
                    String key = String.format("%s#%s", currentClass, decl.getSignature());
                    if (!facts.containsKey(key)) facts.put(key, new Facts());
                    Facts f = facts.get(key);

                    WordsExtractor we = new WordsExtractor(decl.toString(config));
                    f.setWords(we.getOutput(stopWords));

                    f.setNumExpressions(decl.stream().filter(n -> n instanceof Expression).distinct().count());
                    f.setNumStatements(decl.stream().filter(n -> n instanceof Statement).distinct().count());
                    f.setNumLoops(decl.stream().filter(n -> n instanceof ForStmt
                            || n instanceof WhileStmt
                            || n instanceof ForEachStmt
                            || n instanceof DoStmt).distinct().count());
                    f.setNumConditionals(decl.stream().filter(n -> n instanceof IfStmt
                            || n instanceof SwitchStmt).distinct().count());
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        });

        super.visit(decl, facts);
    }

    @Override
    public void visit(ConstructorDeclaration decl, Map<String, Facts> facts) {

        new Resolver<>(decl).getResolution().ifPresent(ctor -> {
            try {
                ResolvedReferenceTypeDeclaration type = ctor.declaringType();
                if (type.getQualifiedName().equals(currentClass)) {
                    String key = String.format("%s#%s", currentClass, decl.getSignature());
                    if (!facts.containsKey(key)) facts.put(key, new Facts());
                    Facts f = facts.get(key);

                    WordsExtractor we = new WordsExtractor(decl.toString(config));
                    f.setWords(we.getOutput(stopWords));

                    f.setNumExpressions(decl.stream().filter(n -> n instanceof Expression).distinct().count());
                    f.setNumStatements(decl.stream().filter(n -> n instanceof Statement).distinct().count());
                    f.setNumLoops(decl.stream().filter(n -> n instanceof ForStmt
                            || n instanceof WhileStmt
                            || n instanceof ForEachStmt
                            || n instanceof DoStmt).distinct().count());
                    f.setNumConditionals(decl.stream().filter(n -> n instanceof IfStmt
                            || n instanceof SwitchStmt).distinct().count());
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        });

        super.visit(decl, facts);
    }
}
