package nl.tue.win.spoony;

import nl.tue.win.collections.Counter;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;

import java.util.*;
import java.util.stream.Collectors;

public class MethodMapper {

    public static void main(String[] args) {

        boolean tokensAsColumns = false;

        ProjectLoader loader = new ProjectLoader(args);
//        System.out.printf("%s:%n", loader.getFileName());

        Launcher launcher = new Launcher();
        launcher.addInputResource(loader.getFileName());
        launcher.buildModel();

        CtModel model = launcher.getModel();

        Set<Class<?>> interesting = Set.of(

                CtFieldRead.class,
                CtFieldWrite.class,

                CtArrayRead.class,
                CtArrayWrite.class,

                CtVariableRead.class,
                CtVariableWrite.class,

                CtInvocation.class,
                CtConstructorCall.class,

                CtFor.class,
                CtForEach.class,

                CtWhile.class,
                CtDo.class,

                CtBinaryOperator.class,

                CtIf.class,
                CtSwitch.class,

                CtNewClass.class,
                CtLambda.class,

                CtReturn.class,

                CtLiteral.class
        );

        List<String> interestingStrings = interesting.stream()
                .map(c -> c.getSimpleName().replaceAll("Ct(\\w+)", "$1"))
                .collect(Collectors.toList());
        interestingStrings.replaceAll(s -> Set.of("For","ForEach").contains(s) ? "ForLoop" : s);
        interestingStrings.replaceAll(s -> Set.of("While","Do").contains(s) ? "WhileLoop" : s);
        interestingStrings.replaceAll(s -> Set.of("If","Switch").contains(s) ? "Conditional" : s);
        interestingStrings.replaceAll(s -> Set.of("NewClass","Lambda").contains(s) ? "Lambda" : s);
        interestingStrings = interestingStrings.stream().distinct().toList();

        if (tokensAsColumns) {
            System.out.printf("structType,structName,opType,opName,%s%n", String.join(",", interestingStrings));
        } else {
            System.out.printf("structType,structName,opType,opName,token,count%n");
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
                    elements.replaceAll(s -> Set.of("For","ForEach").contains(s) ? "ForLoop" : s);
                    elements.replaceAll(s -> Set.of("While","Do").contains(s) ? "WhileLoop" : s);
                    elements.replaceAll(s -> Set.of("If","Switch").contains(s) ? "Conditional" : s);
                    elements.replaceAll(s -> Set.of("NewClass","Lambda").contains(s) ? "Lambda" : s);

                    eleCounter.update(elements);
                    eleCounter.forEach((key, value) -> eleDiscrete.put(key, discretize(value)));
                    if (!tokensAsColumns) {
                        System.out.println(eleDiscrete.toString()
                                .replaceAll(": ", ",")
                                .replaceAll("- ",
                                        "%s,%s,%s,%s,".formatted(structType, structName, opType, opName)));
                    }
                }
                if (tokensAsColumns) {
                    System.out.printf("%s,%s,%s,%s,%s%n", structType, structName, opType, opName,
                            finalInterestingStrings.stream()
                                    .map(key -> eleDiscrete.get(key).toString())
                                    .collect(Collectors.joining(",")));
                }
            });
        });
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
