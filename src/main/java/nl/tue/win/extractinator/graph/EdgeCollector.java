package nl.tue.win.extractinator.graph;

import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import nl.tue.win.graph.Edge;
import nl.tue.win.graph.Graph;
import nl.tue.win.graph.Node;

import java.util.Optional;
import java.util.stream.Stream;

public class EdgeCollector extends VoidVisitorAdapter<Graph> {

    private final TypeSolver solver;
    private String currentClass;

    public EdgeCollector(TypeSolver solver) {
        this.solver = solver;
    }

    private Stream<String> qualifiedNamesFromResolvables(Stream<? extends Resolvable<ResolvedType>> stream) {
        return stream
                .map(resolvable -> new Resolver<>(resolvable).getResolution())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ResolvedType::isReferenceType)
                .map(type -> type.asReferenceType().getQualifiedName());
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration decl, Graph g) {

        String prevClass = currentClass;
        currentClass = null;

        new Resolver<>(decl).getResolution().ifPresent(cls -> {
            String myName = cls.getQualifiedName();
            currentClass = myName;

            g.getNode(myName).ifPresent(self -> {

                // specializes (extends)
                qualifiedNamesFromResolvables(decl.getExtendedTypes().stream())
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "specializes"))));

                // specializes (implements)
                qualifiedNamesFromResolvables(decl.getImplementedTypes().stream())
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "specializes"))));

                // holds (has field of type)
                decl.getFields().stream()
                        .flatMap(field -> qualifiedNamesFromResolvables(field.getVariables().stream()
                                .map(VariableDeclarator::getType)))
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "holds"))));

                // returns (has method with return type)
                qualifiedNamesFromResolvables(decl.getMethods().stream().map(MethodDeclaration::getType))
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "returns"))));

                // accepts (has method with parameter type)
                decl.getMethods().stream()
                        .flatMap(method -> qualifiedNamesFromResolvables(method.getParameters().stream()
                                .map(Parameter::getType)))
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "accepts"))));

                // accepts (has constructor with parameter type)
                decl.getConstructors().stream()
                        .flatMap(ctor -> qualifiedNamesFromResolvables(ctor.getParameters().stream()
                                .map(Parameter::getType)))
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "accepts"))));

            });
        });

        super.visit(decl, g);
        currentClass = prevClass;
    }

    @Override
    public void visit(EnumDeclaration decl, Graph g) {

        String prevClass = currentClass;
        currentClass = null;

        new Resolver<>(decl).getResolution().ifPresent(enm -> {
            String myName = enm.getQualifiedName();
            currentClass = myName;

            g.getNode(myName).ifPresent(self -> {

                // specializes (implements)
                qualifiedNamesFromResolvables(decl.getImplementedTypes().stream())
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "specializes"))));

                // holds (has field of type)
                decl.getFields().stream()
                        .flatMap(field -> qualifiedNamesFromResolvables(field.getVariables().stream()
                                .map(VariableDeclarator::getType)))
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "holds"))));

                // returns (has method with return type)
                qualifiedNamesFromResolvables(decl.getMethods().stream().map(MethodDeclaration::getType))
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "returns"))));

                // accepts (has method with parameter type)
                decl.getMethods().stream()
                        .flatMap(method -> qualifiedNamesFromResolvables(method.getParameters().stream()
                                .map(Parameter::getType)))
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "accepts"))));

                // accepts (has constructor with parameter type)
                decl.getConstructors().stream()
                        .flatMap(ctor -> qualifiedNamesFromResolvables(ctor.getParameters().stream()
                                .map(Parameter::getType)))
                        .filter(typeName -> !typeName.equals(myName))
                        .forEach(typeName -> g.getNode(typeName)
                                .ifPresent(node -> g.getEdges()
                                        .addToWeight(new Edge(self, node, "accepts"))));

            });
        });

        super.visit(decl, g);
        currentClass = prevClass;
    }

    @Override
    public void visit(MethodCallExpr expr, Graph g) {
        g.getNode(currentClass).ifPresent(cls -> new Resolver<>(expr).getResolution()
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
        g.getNode(currentClass).ifPresent(cls -> new Resolver<>(expr).getResolution()
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

    @Override
    public void visit(FieldAccessExpr expr, Graph g) {
        g.getNode(currentClass).ifPresent(cls -> {
            try {
                ResolvedType scopeType = JavaParserFacade.get(solver).getType(expr.getScope());

                if (scopeType.isReferenceType()) {

                    // accesses a field of
                    String ownerName = scopeType.asReferenceType().getQualifiedName();
                    Optional<Node> owner = g.getNode(ownerName);
                    if (owner.isPresent() && !currentClass.equals(ownerName)) {
                        g.getEdges()
                                .addToWeight(new Edge(cls, owner.get(), "accesses"));
                    }
                }
            } catch (Exception e) {
                System.err.println("unresolved " + expr + " at " + currentClass);
            }
        });
        super.visit(expr, g);
    }
}
