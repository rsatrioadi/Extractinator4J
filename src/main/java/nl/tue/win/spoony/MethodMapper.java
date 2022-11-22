package nl.tue.win.spoony;

import nl.tue.win.collections.Counter;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MethodMapper {

    public static void main(String[] args) {

        /* 0: vector, 1: treemap, 2: stereotype */
        int outputType = 2;

        ProjectLoader loader = new ProjectLoader(args);
//        System.out.printf("%s:%n", loader.getFileName());

        Launcher launcher = new Launcher();
        launcher.addInputResource(loader.getFileName());
        launcher.buildModel();

        CtModel model = launcher.getModel();

        List<Class<?>> interesting = List.of(

                CtFieldRead.class,
                CtFieldWrite.class,

                CtVariableRead.class,
                CtVariableWrite.class,

                CtNewClass.class,
                CtLambda.class,

                CtLiteral.class,

                CtIf.class,
                CtSwitch.class,
                CtConditional.class,

                CtFor.class,
                CtForEach.class,

                CtWhile.class,
                CtDo.class,

                CtConstructorCall.class,
                CtInvocation.class,

                CtReturn.class,

                CtBinaryOperator.class
        );

        List<String> interestingStrings = interesting.stream()
                .map(c -> c.getSimpleName().replaceAll("Ct(\\w+)", "$1"))
                .collect(Collectors.toList());
        simplify(interestingStrings);
        interestingStrings = interestingStrings.stream().distinct().toList();

        List<String> stereotypeNames = MethodVector.stereotypes().stream().map(Object::toString).toList();
        if (outputType == 0) {
            System.out.printf("structType,structName,opType,opName,%s%n", String.join(",", interestingStrings));
        } else if (outputType == 1) {
            System.out.printf("structType,structName,opType,opName,token,count%n");
        } else if (outputType == 2) {
            System.out.printf("structType,structName,opType,opName,labels,%s%n", String.join(",", stereotypeNames));
        }

        List<String> finalInterestingStrings = interestingStrings;
        model.getAllTypes().forEach(type -> {

            String structName = type.getSimpleName();
            String structType = type.isInterface() ? "interface"
                    : type.isEnum() ? "enum"
                    : type.isClass() && type.isAbstract() ? "abstractClass"
                    : type.isClass() ? "class"
                    : "other";

            List<CtExecutable<?>> operations = new ArrayList<>(type.getMethods());
            if (type instanceof CtClass<?> clasz) {
                operations.addAll(clasz.getConstructors());
            }

            operations.forEach(op -> {

                String opName = op.getSimpleName();
                String opType = op instanceof CtMethod<?> ? "method"
                        : op instanceof CtConstructor<?> ? "constructor"
                        : "other";

                Counter<String> eleCounter = new Counter<>();
                Counter<String> eleDiscrete = new Counter<>();
                if (op.getBody() != null) {

                    List<String> elements = op.getBody()
                            .getElements(e -> interesting.stream()
                                    .anyMatch(c -> c.isInstance(e)))
                            .stream()
                            .map(e -> e.getClass()
                                    .getSimpleName()
                                    .replaceAll("Ct(\\w+)Impl", "$1"))
                            .collect(Collectors.toList());
                    simplify(elements);

                    eleCounter.update(elements);
                    eleCounter.forEach((key, value) -> eleDiscrete.put(key, discretize(value)));
                    if (outputType == 1) {
                        System.out.println(eleDiscrete.toString()
                                .replaceAll(": ", ",")
                                .replaceAll("- ",
                                        "%s,%s,%s,%s,".formatted(structType, structName, opType, opName)));
                    }
                }
                if (outputType == 0) {
                    System.out.printf("%s,%s,%s,%s,%s%n", structType, structName, opType, opName,
                            finalInterestingStrings.stream()
                                    .map(key -> eleDiscrete.get(key).toString())
                                    .collect(Collectors.joining(",")));
                }

                if (outputType == 2) {
                    MethodVector vector = new MethodVector(eleDiscrete);
                    List<Double> distances = vector.distancesFromStereotype();
                    System.out.printf("%s,%s,%s,%s,%s,%s%n", structType, structName, opType, opName,
                            vector.likelyStereotypes().stream()
                                    .map(Object::toString)
                                    .collect(Collectors.joining(" ")),
                            distances.stream()
                                    .map("%.2f"::formatted)
                                    .collect(Collectors.joining(",")));
                }

            });
        });
    }

    private static void simplify(List<String> elements) {
        elements.replaceAll(s -> Set.of("For", "ForEach").contains(s) ? "ForLoop" : s);
        elements.replaceAll(s -> Set.of("While", "Do").contains(s) ? "WhileLoop" : s);
        elements.replaceAll(s -> Set.of("If", "Switch", "Conditional").contains(s) ? "Conditional" : s);
        elements.replaceAll(s -> Set.of("NewClass", "Lambda").contains(s) ? "Lambda" : s);
    }

    static int discretize(int value) {
        return switch (value) {
            case 0 -> 0;
            case 1 -> 1;
            case 2, 3, 4, 5 -> 2;
            default -> 3;
        };
    }
}
