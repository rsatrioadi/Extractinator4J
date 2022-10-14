package nl.tue.win.extractinator.stereotype;

import java.util.*;
import java.util.stream.Collectors;

public class WordsExtractor {
    public static final Set<String> JAVA_KEYWORDS = Set.of("_", "abstract", "continue", "for", "new", "switch",
            "assert", "default", "goto", "package", "synchronized", "boolean", "do", "if", "private", "this", "break",
            "double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum",
            "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", "final",
            "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float",
            "native", "super", "while", "true", "false", "null");
    private static final String keywordsAsRegexPattern = String.format("(%s)", String.join("|", JAVA_KEYWORDS));

    private final String input;
    private final Map<String, List<String>> output = new HashMap<>();

    public WordsExtractor(String input) {
        this.input = input;
    }

    public String getInput() {
        return input;
    }

    public List<String> getOutput(Set<String> stopWords) {
        String key = stopWords.toString();
        if (!output.containsKey(key)) {
            output.put(key, Arrays.stream(input.replaceAll(String.format("\\b%s\\b", keywordsAsRegexPattern), " ")
                            .split("\\s+"))
                    .filter(s -> s.matches("\\w+"))
                    .flatMap(s -> Arrays.stream(s.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"))
                            .map(String::toLowerCase))
                    .filter(s -> !s.isBlank() && !stopWords.contains(s))
                    .collect(Collectors.toList()));
        }
        return output.get(key);
    }
}
