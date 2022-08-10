package nl.tue.win.extractinator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.symbolsolver.resolution.typesolvers.MemoryTypeSolver;
import nl.tue.win.graph.Graph;
import nl.tue.win.javajj.MemoryVisitor;
import nl.tue.win.javajj.ProjectLoader;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {

            // Load project zip file (from args)
            ProjectLoader loader = new ProjectLoader(args);
            Graph graph = new Graph(loader.getName());
            List<CompilationUnit> units = loader.getCompilationUnits();

            // Populate memory-based type solver
            units.forEach(unit -> {
                VoidVisitor<MemoryTypeSolver> visitor = new MemoryVisitor();
                visitor.visit(unit, loader.getMemSolver());
            });

            // Extract structure from AST
            units.forEach(unit -> {
                VoidVisitor<Graph> visitor = new StructureVisitor();
                visitor.visit(unit, graph);
            });

            units.forEach(unit -> {
                VoidVisitor<Graph> visitor = new MemberVisitor();
                visitor.visit(unit, graph);
            });

            System.out.println(graph);

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
