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
import java.util.*;
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

            // extract class facts
            Map<String, ClassFacts> classFactsMap = new HashMap<>();
            units.forEach(unit -> {
                VoidVisitor<Map<String, ClassFacts>> visitor = new ClassFactsCollector();
                visitor.visit(unit, classFactsMap);
            });

            // extract method facts
            Map<String, MethodFacts> methodFactsMap = new HashMap<>();
            units.forEach(unit -> {
                VoidVisitor<Map<String, MethodFacts>> visitor = new MethodFactsCollector();
                visitor.visit(unit, methodFactsMap);
            });

            // write class facts
            StringBuilder classResult = new StringBuilder("class,")
                    .append(Arrays.stream(ClassFacts.Type.values())
                            .map(Object::toString)
                            .collect(Collectors.joining(",")))
                    .append("\n");

            Map<String, String> rsMap;
            Path rsPath = Paths.get("rolestereotypes.csv");
            if (Files.exists(rsPath)) {
                rsMap = Files.readAllLines(rsPath).stream()
                        .map(line -> line.split(","))
                        .collect(Collectors.toMap(a -> a[0], a -> a[1]));
            } else {
                rsMap = new HashMap<>();
            }

            classFactsMap.keySet().stream()
                    .sorted()
                    .forEach(key -> {
                        ClassFacts f = classFactsMap.get(key);

                        Set<MethodFacts> methodFacts = methodFactsMap.keySet().stream()
                                .filter(k -> k.startsWith(String.format("%s#", key)))
                                .map(methodFactsMap::get)
                                .collect(Collectors.toSet());

                        // maxNumUniqueWords,
                        f.put(ClassFacts.Type.maxNumUniqueWords, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numUniqueWords, 0))
                                .max().orElse(0));

                        // maxNumConditionals, maxNumLoops, maxNumStatements, maxNumExpressions,
                        f.put(ClassFacts.Type.maxNumConditionals, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numConditionals, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumLoops, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numLoops, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumStatements, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numStatements, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumExpressions, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numExpressions, 0))
                                .max().orElse(0));

                        // maxNumVars, maxNumBooleanVars, maxNumPrimitiveVars, maxNumStringVars, maxNumCollectionVars, maxNumMapVars, maxNumArrayVars,
                        f.put(ClassFacts.Type.maxNumVars, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numVars, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumBooleanVars, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numBooleanVars, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumPrimitiveVars, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numPrimitiveVars, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumStringVars, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numStringVars, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumCollectionVars, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numCollectionVars, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumMapVars, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numMapVars, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumArrayVars, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numArrayVars, 0))
                                .max().orElse(0));

                        // maxNumParams, maxNumBooleanParams, maxNumPrimitiveParams, maxNumStringParams, maxNumCollectionParams, maxNumMapParams, maxNumArrayParams
                        f.put(ClassFacts.Type.maxNumParams, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numParams, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumBooleanParams, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numBooleanParams, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumPrimitiveParams, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numPrimitiveParams, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumStringParams, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numStringParams, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumCollectionParams, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numCollectionParams, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumMapParams, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numMapParams, 0))
                                .max().orElse(0));
                        f.put(ClassFacts.Type.maxNumArrayParams, methodFacts.stream()
                                .mapToLong(k -> (long) k.getOrDefault(MethodFacts.Type.numArrayParams, 0))
                                .max().orElse(0));

                        f.put(ClassFacts.Type.roleStereotype, rsMap.getOrDefault(key, ""));
                        classResult.append(key.contains(",")
                                        ? String.format("\"%s\"", key.replaceAll("\"", "\"\""))
                                        : key)
                                .append(',')
                                .append(Arrays.stream(ClassFacts.Type.values())
                                        .map(k -> f.getOrDefault(k, "").toString())
                                        .collect(Collectors.joining(",")))
                                .append("\n");

                        System.out.println("# " + key);
                        System.out.println(new Counter<>((List<String>) f.get(ClassFacts.Type.words)));
                    });

            Path classOutput = Paths.get(String.format("%s-classFacts.csv", loader.getOutputPrefix()));
            Files.write(classOutput, classResult.toString().getBytes());

            // write method facts
            StringBuilder methodResult = new StringBuilder("class,method,")
                    .append(Arrays.stream(MethodFacts.Type.values())
                            .map(Object::toString)
                            .collect(Collectors.joining(",")))
                    .append("\n");

            methodFactsMap.keySet().stream()
                    .sorted()
                    .forEach(key -> {
                        MethodFacts f = methodFactsMap.get(key);
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

            Path methodOutput = Paths.get(String.format("%s-methodFacts.csv", loader.getOutputPrefix()));
            Files.write(methodOutput, methodResult.toString().getBytes());

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
