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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            StringBuilder result = new StringBuilder("scope,words,numConditionals,numLoops,numStatements,numExpressions\n");

            // Extract words
            units.forEach(unit -> {
                Map<String, Facts> facts = new HashMap<>();
                VoidVisitor<Map<String, Facts>> visitor = new FactsCollector();
                visitor.visit(unit, facts);

                facts.keySet().stream()
                        .sorted()
                        .forEach(key -> {
                            Facts f = facts.get(key);
                            List<String> words = f.getWords();
                            result.append(key.contains(",")
                                            ? String.format("\"%s\"", key.replaceAll("\"", "\\\""))
                                            : key)
                                    .append(",")
                                    .append(String.join(" ", words))
                                    .append(",")
                                    .append(f.getNumConditionals())
                                    .append(",")
                                    .append(f.getNumLoops())
                                    .append(",")
                                    .append(f.getNumStatements())
                                    .append(",")
                                    .append(f.getNumExpressions())
                                    .append("\n");

                            System.out.println("# " + key);
                            System.out.println(new Counter<>(words).entrySet());

                        });
            });

            Path nodeOutput = Paths.get(String.format("%s-facts.csv", loader.getOutputPrefix()));
            Files.write(nodeOutput, result.toString().getBytes());

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
