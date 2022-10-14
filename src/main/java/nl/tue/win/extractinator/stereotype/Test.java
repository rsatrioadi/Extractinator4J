package nl.tue.win.extractinator.stereotype;

import nl.tue.win.collections.Counter;

public class Test {
    public static void main(String[] args) {
        new Counter(5, 5, 4, 5, 4, 3, 5, 4, 3, 2, 5, 4, 3, 2, 1)
                .entrySet()
                .forEach(System.out::println);

        System.out.println(WordsExtractor.JAVA_KEYWORDS);
    }
}
