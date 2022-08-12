package nl.tue.win.extractinator;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedType;
import nl.tue.win.graph.Edge;
import nl.tue.win.graph.Graph;
import nl.tue.win.graph.Node;

import java.util.Optional;

public class MemberVisitor extends VoidVisitorAdapter<Graph> {

    private String currentClass;

    @Override
    public void visit(ClassOrInterfaceDeclaration decl, Graph g) {

        currentClass = null;

        new Resolver<>(decl).getResolved().ifPresent(cls -> {
            String myName = cls.getQualifiedName();
            currentClass = myName;

            g.getNode(myName).ifPresent(self -> {

                // specializes (extends)
                decl.getExtendedTypes().stream()
                        .map(type -> new Resolver<>(type).getResolved())
                        .filter(Optional::isPresent)
                        .map(type -> type.get().asReferenceType().getQualifiedName())
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "specializes"))));

                // specializes (implements)
                decl.getImplementedTypes().stream()
                        .map(type -> new Resolver<>(type).getResolved())
                        .filter(Optional::isPresent)
                        .map(type -> type.get().asReferenceType().getQualifiedName())
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "specializes"))));

                // holds (has field of type)
                decl.getFields().stream()
                        .flatMap(field -> field.getVariables().stream()
                                .map(variable -> new Resolver<>(variable.getType()).getResolved())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(ResolvedType::isReferenceType)
                                .map(type -> type.asReferenceType().getQualifiedName()))
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "holds"))));

                // returns (has method with return type)
                decl.getMethods().stream()
                        .map(method -> new Resolver<>(method.getType()).getResolved())
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(ResolvedType::isReferenceType)
                        .map(returnType -> returnType.asReferenceType().getQualifiedName())
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "returns"))));

                // accepts (has method with parameter type)
                decl.getMethods().stream()
                        .flatMap(method -> method.getParameters().stream()
                                .map(param -> new Resolver<>(param.getType()).getResolved())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(ResolvedType::isReferenceType)
                                .map(type -> type.asReferenceType().getQualifiedName()))
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "accepts"))));

                // accepts (has constructor with parameter type)
                decl.getConstructors().stream()
                        .flatMap(ctor -> ctor.getParameters().stream()
                                .map(param -> new Resolver<>(param.getType()).getResolved())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(ResolvedType::isReferenceType)
                                .map(type -> type.asReferenceType().getQualifiedName()))
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "accepts"))));

            });
        });

        super.visit(decl, g);
    }

    @Override
    public void visit(EnumDeclaration decl, Graph g) {

        currentClass = null;

        new Resolver<>(decl).getResolved().ifPresent(enm -> {
            String myName = enm.getQualifiedName();
            currentClass = myName;

            g.getNode(myName).ifPresent(self -> {

                // specializes (implements)
                decl.getImplementedTypes().stream()
                        .map(type -> new Resolver<>(type).getResolved())
                        .filter(Optional::isPresent)
                        .map(type -> type.get().asReferenceType().getQualifiedName())
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "specializes"))));

                // holds (has field of type)
                decl.getFields().stream()
                        .flatMap(field -> field.getVariables().stream()
                                .map(variable -> new Resolver<>(variable.getType()).getResolved())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(ResolvedType::isReferenceType)
                                .map(type -> type.asReferenceType().getQualifiedName()))
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "holds"))));

                // returns (has method with return type)
                decl.getMethods().stream()
                        .map(method -> new Resolver<>(method.getType()).getResolved())
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(ResolvedType::isReferenceType)
                        .map(returnType -> returnType.asReferenceType().getQualifiedName())
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "returns"))));

                // accepts (has method with parameter type)
                decl.getMethods().stream()
                        .flatMap(method -> method.getParameters().stream()
                                .map(param -> new Resolver<>(param.getType()).getResolved())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(ResolvedType::isReferenceType)
                                .map(type -> type.asReferenceType().getQualifiedName()))
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "accepts"))));

                // accepts (has constructor with parameter type)
                decl.getConstructors().stream()
                        .flatMap(ctor -> ctor.getParameters().stream()
                                .map(param -> new Resolver<>(param.getType()).getResolved())
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(ResolvedType::isReferenceType)
                                .map(type -> type.asReferenceType().getQualifiedName()))
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "accepts"))));

            });
        });

        super.visit(decl, g);
    }

    @Override
    public void visit(MethodCallExpr expr, Graph g) {
        g.getNode(currentClass).ifPresent(cls -> new Resolver<>(expr).getResolved()
                .ifPresent(methodCall -> {

                    // depends (calls a method of)
                    String ownerName = methodCall.declaringType().asReferenceType().getQualifiedName();
                    Optional<Node> owner = g.getNode(ownerName);
                    if (owner.isPresent() && !currentClass.equals(ownerName)) {
                        g.getEdges()
                                .addToWeight(new Edge(cls, owner.get(), "depends"));
                    }
                }));

        super.visit(expr, g);
    }

    @Override
    public void visit(ObjectCreationExpr expr, Graph g) {
        g.getNode(currentClass).ifPresent(cls -> new Resolver<>(expr).getResolved()
                .ifPresent(ctorCall -> {

                    // constructs (calls a constructor of)
                    String ownerName = ctorCall.declaringType().asReferenceType().getQualifiedName();
                    Optional<Node> owner = g.getNode(ownerName);
                    if (owner.isPresent() && !currentClass.equals(ownerName)) {
                        g.getEdges()
                                .addToWeight(new Edge(cls, owner.get(), "constructs"));
                    }
                }));

        super.visit(expr, g);
    }
}
