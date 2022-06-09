package nl.tue.javajj;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<CompilationUnit> units = new ProjectLoader(args).getCompilationUnits();
        units.forEach(unit -> {
            VoidVisitor<?> visitor = new CUPrinter();
            visitor.visit(unit, null);
        });
    }
}
