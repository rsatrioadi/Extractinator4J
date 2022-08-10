package nl.tue.win.javajj;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import nl.tue.win.javajj.model.ClassType;
import nl.tue.win.javajj.model.Package;
import nl.tue.win.javajj.model.Project;

public class StructureExtractor extends VoidVisitorAdapter<Project> {

    @Override
    public void visit(ClassOrInterfaceDeclaration decl, Project prj) {
        String fullName = decl.resolve().getQualifiedName();
        String pkgname = decl.resolve().getPackageName();
        if (pkgname == null) {
            pkgname = "";
        }
        Package pkg = prj.getPackage(pkgname).orElse(new Package(pkgname));
        prj.addPackage(pkg);

        ClassType cls = pkg
                .getClass(fullName)
                .orElse(new ClassType(decl));
        pkg.addClass(cls);
        super.visit(decl, prj);
    }

    @Override
    public void visit(EnumDeclaration decl, Project prj) {
        String fullName = decl.resolve().getQualifiedName();
        String pkgname = decl.resolve().getPackageName();
        if (pkgname == null) {
            pkgname = "";
        }
        Package pkg = prj.getPackage(pkgname).orElse(new Package(pkgname));
        prj.addPackage(pkg);

        ClassType cls = pkg
                .getClass(fullName)
                .orElse(new ClassType(decl));
        pkg.addClass(cls);
        super.visit(decl, prj);
    }
}
