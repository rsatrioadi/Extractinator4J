package nl.tue.win.spoony;

import nl.tue.win.collections.Counter;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtBodyHolder;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

public class SpoonTrial {

    public static void main(String[] args) {
        ProjectLoader loader = new ProjectLoader(args);
        Launcher launcher = new Launcher();
        System.out.println(loader.getFileName());
        launcher.addInputResource(loader.getFileName());
        launcher.buildModel();
        CtModel model = launcher.getModel();
        model.getAllTypes().forEach(type -> {
            System.out.println("---");
            System.out.println(type.getQualifiedName());

            System.out.println("* Specializes:");
            System.out.println("  - " + type.getSuperclass());
            type.getSuperInterfaces().forEach(itf -> System.out.println("  - " + itf));

            System.out.println("* Holds:");
            List<? extends CtTypeReference<?>> fieldTypes = type.getFields().stream()
                    .map(CtField::getType)
                    .toList();
            Counter<CtTypeReference<?>> fieldTypeCounter = new Counter<>(fieldTypes);
            fieldTypeCounter.update(fieldTypes.stream()
                    .flatMap(t -> t.getActualTypeArguments().stream())
                    .toList());
            fieldTypeCounter.forEach((ft, count) -> System.out.printf("  - %s (%d)%n", ft, count));

            System.out.println("* Returns:");
            List<? extends CtTypeReference<?>> methodTypes = type.getMethods().stream()
                    .map(CtMethod::getType)
                    .toList();
            Counter<CtTypeReference<?>> methodTypeCounter = new Counter<>(methodTypes);
            methodTypeCounter.update(methodTypes.stream()
                    .flatMap(t -> t.getActualTypeArguments().stream())
                    .toList());
            methodTypeCounter.forEach((mt, count) -> System.out.printf("  - %s (%d)%n", mt, count));

            System.out.println("* Accepts:");
            List<? extends CtTypeReference<?>> paramTypes = type.getMethods().stream()
                    .flatMap(method -> method.getParameters().stream())
                    .map(CtParameter::getType)
                    .toList();
            Counter<CtTypeReference<?>> paramTypeCounter = new Counter<>(paramTypes);
            paramTypeCounter.update(paramTypes.stream()
                    .flatMap(t -> t.getActualTypeArguments().stream())
                    .toList());
            paramTypeCounter.forEach((mt, count) -> System.out.printf("  - %s (%d)%n", mt, count));

            System.out.println("* Constructs:");
            List<? extends CtTypeReference<?>> constructedTypes = type.getTypeMembers().stream()
                    .filter(m -> m instanceof CtBodyHolder)
                    .map(m -> (CtBodyHolder) m)
                    .filter(m -> m.getBody() != null)
                    .flatMap(m -> m.getBody().getElements(new TypeFilter<>(CtConstructorCall.class)).stream())
                    .map(ctConstructorCall -> (CtTypeReference<?>) ctConstructorCall.getType())
                    .toList();
            Counter<CtTypeReference<?>> constructedTypeCounter = new Counter<>(constructedTypes);
            constructedTypeCounter.update(constructedTypes.stream()
                    .flatMap(t -> t.getActualTypeArguments().stream()).toList());
            System.out.printf("  - %s%n", constructedTypeCounter.toString().replaceAll("\n", "\n  - "));

//            System.out.println("Class level:");
//            System.out.println("> Fields:");
//            type.getFields().stream()
//                    .filter(CtField::isStatic)
//                    .forEach(field -> System.out.printf("  %s %s: %s%n", field.isPublic()?"+":"-", field.getSimpleName(), field.getType()));
//            System.out.println("> Methods:");
//            type.getMethods().stream()
//                    .filter(CtMethod::isStatic)
//                    .forEach(method -> System.out.printf("  %s %s: %s%n", method.isPublic()?"+":"-", method.getSignature(), method.getType()));
//            System.out.println("Instance level:");
//            System.out.println("> Fields:");
//            type.getFields().stream()
//                    .filter(ctField -> !ctField.isStatic())
//                    .forEach(field -> System.out.printf("  %s %s: %s%n", field.isPublic()?"+":"-", field.getSimpleName(), field.getType()));
//            System.out.println("> Methods:");
//            type.getMethods().stream()
//                    .filter(ctMethod -> !ctMethod.isStatic())
//                    .forEach(method -> System.out.printf("  %s %s: %s%n", method.isPublic()?"+":"-", method.getSignature(), method.getType()));
        });
    }
}
