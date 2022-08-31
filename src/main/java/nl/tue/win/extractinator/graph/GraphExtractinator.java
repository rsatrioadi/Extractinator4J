package nl.tue.win.extractinator.graph;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.symbolsolver.resolution.typesolvers.MemoryTypeSolver;
import nl.tue.win.extractinator.ProjectLoader;
import nl.tue.win.extractinator.TypeSolvingVisitor;
import nl.tue.win.graph.Graph;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GraphExtractinator {

    public static void main(String[] args) {
        try {

            // Load project zip file (from args)
            ProjectLoader loader = new ProjectLoader(args);
            Graph graph = new Graph(loader.getName());
            List<CompilationUnit> units = loader.getCompilationUnits();

            // Populate memory-based type solver
            units.forEach(unit -> {
                VoidVisitor<MemoryTypeSolver> visitor = new TypeSolvingVisitor();
                visitor.visit(unit, loader.getMemSolver());
            });

            // Extract nodes from AST
            units.forEach(unit -> {
                VoidVisitor<Graph> visitor = new NodeCollector();
                visitor.visit(unit, graph);
            });

            // Extract edges from AST
            units.forEach(unit -> {
                VoidVisitor<Graph> visitor = new EdgeCollector(loader.getMemSolver());
                visitor.visit(unit, graph);
            });

//            System.out.println(graph);
            Path nodeOutput = Paths.get(String.format("%s-nodes.csv", loader.getOutputPrefix()));
            Path edgeOutput = Paths.get(String.format("%s-edges.csv", loader.getOutputPrefix()));
            Files.write(nodeOutput, graph.getNodes().toString().getBytes());
            Files.write(edgeOutput, graph.getEdges().toString().getBytes());

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
