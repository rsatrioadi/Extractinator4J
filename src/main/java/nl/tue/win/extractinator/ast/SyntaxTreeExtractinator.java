package nl.tue.win.extractinator.ast;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.symbolsolver.resolution.typesolvers.MemoryTypeSolver;
import nl.tue.win.extractinator.ProjectLoader;
import nl.tue.win.extractinator.TypeSolvingVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SyntaxTreeExtractinator {

    private static StringBuilder treeAsString(String parentName, Node node) {
        List<Node> childNodes = node.getChildNodes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < childNodes.size(); i++) {
            Node c = childNodes.get(i);
            String cName = "%s_%d".formatted(parentName, i);
            sb.append("%s_%s %s_%s\n".formatted(parentName, node.getClass().getSimpleName(),
                    cName, c.getClass().getSimpleName()));
            sb.append(treeAsString(cName, c));
        }
        return sb;
    }

    private static StringBuilder treeAsGVRec(String parentName, Node node) {
        List<Node> childNodes = node.getChildNodes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < childNodes.size(); i++) {
            Node c = childNodes.get(i);
            String cName = "%s_%d".formatted(parentName, i);
            sb.append("  %s_%s -> %s_%s\n".formatted(parentName, node.getClass().getSimpleName(),
                    cName, c.getClass().getSimpleName()));
            sb.append(treeAsGVRec(cName, c));
        }
        return sb;
    }

    private static StringBuilder treeLabeling(String parentName, Node node) {
        List<Node> childNodes = node.getChildNodes();
        StringBuilder sb = new StringBuilder();
        sb.append("  %s_%s [label=\"%s\"]\n".formatted(parentName, node.getClass().getSimpleName(), node.getClass().getSimpleName()));
        for (int i = 0; i < childNodes.size(); i++) {
            Node c = childNodes.get(i);
            String cName = "%s_%d".formatted(parentName, i);
            sb.append(treeLabeling(cName, c));
        }
        return sb;
    }

    private static StringBuilder treeAsGV(NodeWithSimpleName<?> n) {
        return new StringBuilder("digraph ")
                .append(n.getNameAsString())
                .append(" {\n")
                .append(treeLabeling("n", (Node) n))
                .append(treeAsGVRec("n", (Node) n))
                .append("}");
    }

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

            units.stream()
                    .flatMap(unit -> unit.stream()
                            .filter(node -> node instanceof TypeDeclaration<?>))
                    .forEach(node -> {
                        try {
                            TypeDeclaration<?> tDeclaration = (TypeDeclaration<?>) node.clone();
                            tDeclaration.findAll(TypeDeclaration.class)
                                    .forEach(Node::remove);

                            String className = ((TypeDeclaration<?>) node).resolve().getQualifiedName();

                            Files.createDirectories(Paths.get("%s/gv/".formatted(loader.getOutputPrefix())));
                            Files.createDirectories(Paths.get("%s/txt/".formatted(loader.getOutputPrefix())));

                            String ts = treeAsString("n", tDeclaration).toString();
                            String gv = treeAsGV(tDeclaration).toString();

                            Path treeTextOutput = Paths.get(String.format("%s/txt/%s.txt", loader.getOutputPrefix(), className));
                            Path gvOutput = Paths.get(String.format("%s/gv/%s.gv", loader.getOutputPrefix(), ((TypeDeclaration<?>) node).resolve().getQualifiedName()));

                            Files.write(treeTextOutput, ts.getBytes());
                            Files.write(gvOutput, gv.getBytes());

                            node.findAll(CallableDeclaration.class)
                                    .forEach(cDeclaration -> {
                                        try {
                                            Files.createDirectories(Paths.get("%s/gv/%s/".formatted(loader.getOutputPrefix(), className)));
                                            Files.createDirectories(Paths.get("%s/txt/%s/".formatted(loader.getOutputPrefix(), className)));

                                            String callableName = "%s%d".formatted(cDeclaration.getNameAsString(), cDeclaration.getSignature().hashCode());

                                            String mts = treeAsString("n", cDeclaration).toString();
                                            String mgv = treeAsGV(cDeclaration).toString();

                                            Path mtreeTextOutput = Paths.get(String.format("%s/txt/%s/%s.txt", loader.getOutputPrefix(), className, callableName));
                                            Path mgvOutput = Paths.get(String.format("%s/gv/%s/%s.gv", loader.getOutputPrefix(), ((TypeDeclaration<?>) node).resolve().getQualifiedName(), callableName));

                                            Files.write(mtreeTextOutput, mts.getBytes());
                                            Files.write(mgvOutput, mgv.getBytes());

                                        } catch (IOException e) {
                                            System.err.println(e.getMessage());
                                        }
                                    });
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                    });


        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
