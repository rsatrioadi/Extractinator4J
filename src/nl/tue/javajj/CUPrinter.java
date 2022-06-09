package nl.tue.javajj;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class CUPrinter extends VoidVisitorAdapter<Void> {

    @Override
    public void visit(PackageDeclaration decl, Void arg) {
        System.out.println("package is: " + decl.getName());
        super.visit(decl, arg);
    }

    @Override
    public void visit(ImportDeclaration decl, Void arg) {
        System.out.println("importing: " + decl.getName());
        super.visit(decl, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration decl, Void arg) {
        System.out.println("  class/interface is: " + decl.getName() + " (" + decl.getFullyQualifiedName().orElse("<no fully qualified name>") + ")");
        super.visit(decl, arg);
    }

    @Override
    public void visit(MethodDeclaration decl, Void arg) {
        System.out.println("    declaring method: " + decl.getName());
        super.visit(decl, arg);
    }

    @Override
    public void visit(ConstructorDeclaration decl, Void arg) {
        System.out.println("    declaring ctor: " + decl.getName());
        super.visit(decl, arg);
    }

    @Override
    public void visit(Parameter decl, Void arg) {
        System.out.println("      with param: " + decl.getName());
        super.visit(decl, arg);
    }

    @Override
    public void visit(FieldDeclaration decl, Void arg) {
        System.out.println("    declaring fields: " + decl.getVariables());
        super.visit(decl, arg);
    }
}
