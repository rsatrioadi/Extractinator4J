package nl.tue.win.javajj;

import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import nl.tue.win.model.ClassType;
import nl.tue.win.model.Package;
import nl.tue.win.model.Project;

public class StructureExtractor extends VoidVisitorAdapter<Project> {

    private Package currentPackage;
    private ClassType currentClass;

    @Override
    public void visit(PackageDeclaration decl, Project prj) {
        currentPackage = prj.getPackage(decl.getNameAsString()).orElse(new Package(decl.getNameAsString()));
        prj.addPackage(currentPackage);
        super.visit(decl, prj);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration decl, Project prj) {
        String fullName = decl.resolve().getQualifiedName();
        currentClass = currentPackage
                .getClass(fullName)
                .orElse(new ClassType(decl));
        currentPackage.addClass(currentClass);
        super.visit(decl, prj);
    }

    @Override
    public void visit(EnumDeclaration decl, Project prj) {
        String fullName = decl.resolve().getQualifiedName();
        currentClass = currentPackage
                .getClass(fullName)
                .orElse(new ClassType(decl));
        currentPackage.addClass(currentClass);
        super.visit(decl, prj);
    }
}
