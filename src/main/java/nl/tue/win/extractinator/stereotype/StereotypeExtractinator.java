package nl.tue.win.extractinator.stereotype;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.symbolsolver.resolution.typesolvers.MemoryTypeSolver;
import nl.tue.win.collections.Counter;
import nl.tue.win.extractinator.ProjectLoader;
import nl.tue.win.extractinator.TypeSolvingVisitor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StereotypeExtractinator {

    public static void main(String[] args) {
        try {

            // Load project zip file (from args)
            ProjectLoader loader = new ProjectLoader(args);
            List<CompilationUnit> units = loader.getCompilationUnits();

            // Populate memory-based type solver
            units.forEach(unit -> {
                VoidVisitor<MemoryTypeSolver> visitor = new TypeSolvingVisitor();
                visitor.visit(unit, loader.getMemSolver());
            });

            StringBuilder classResult = new StringBuilder("class,")
                    .append(Arrays.stream(ClassFacts.Type.values())
                            .map(Object::toString)
                            .collect(Collectors.joining(",")))
                    .append("\n");

            units.forEach(unit -> {
                Map<String, ClassFacts> facts = new HashMap<>();
                VoidVisitor<Map<String, ClassFacts>> visitor = new ClassFactsCollector();
                visitor.visit(unit, facts);

                facts.keySet().stream()
                        .sorted()
                        .forEach(key -> {
                            ClassFacts f = facts.get(key);
                            classResult.append(key.contains(",")
                                            ? String.format("\"%s\"", key.replaceAll("\"", "\"\""))
                                            : key)
                                    .append(',')
                                    .append(Arrays.stream(ClassFacts.Type.values())
                                            .map(k -> f.getOrDefault(k, "").toString())
                                            .collect(Collectors.joining(",")))
                                    .append("\n");

                            String words = (String) f.get(ClassFacts.Type.words);
                            System.out.println("# " + key);
                            System.out.println(new Counter<>(Arrays.stream(words.split(" ")).filter(s -> !s.isBlank()).collect(Collectors.toList())).entrySet());
                        });
            });

            Path classOutput = Paths.get(String.format("%s-classFacts.csv", loader.getOutputPrefix()));
            Files.write(classOutput, classResult.toString().getBytes());

            StringBuilder methodResult = new StringBuilder("class,method,")
                    .append(Arrays.stream(MethodFacts.Type.values())
                            .map(Object::toString)
                            .collect(Collectors.joining(",")))
                    .append("\n");

            units.forEach(unit -> {
                Map<String, MethodFacts> facts = new HashMap<>();
                VoidVisitor<Map<String, MethodFacts>> visitor = new MethodFactsCollector();
                visitor.visit(unit, facts);

                facts.keySet().stream()
                        .sorted()
                        .forEach(key -> {
                            MethodFacts f = facts.get(key);
                            String[] keys = key.split("#");
                            methodResult
                                    .append(keys[0])
                                    .append(',')
                                    .append(keys[1].contains(",")
                                            ? String.format("\"%s\"", keys[1].replaceAll("\"", "\"\""))
                                            : keys[1])
                                    .append(',')
                                    .append(Arrays.stream(MethodFacts.Type.values())
                                            .map(k -> f.getOrDefault(k, "").toString())
                                            .collect(Collectors.joining(",")))
                                    .append("\n");
                        });
            });

            Path methodOutput = Paths.get(String.format("%s-methodFacts.csv", loader.getOutputPrefix()));
            Files.write(methodOutput, methodResult.toString().getBytes());

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
