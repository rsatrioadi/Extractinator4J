package nl.tue.win.javajj;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.MemoryTypeSolver;

public class MemoryVisitor extends VoidVisitorAdapter<MemoryTypeSolver> {

    @Override
    public void visit(ClassOrInterfaceDeclaration decl, MemoryTypeSolver solver) {
        ResolvedReferenceTypeDeclaration refType = decl.resolve();
        solver.addDeclaration(refType.getQualifiedName(), refType);
        super.visit(decl, solver);
    }

    @Override
    public void visit(EnumDeclaration decl, MemoryTypeSolver solver) {
        ResolvedReferenceTypeDeclaration refType = decl.resolve();
        solver.addDeclaration(refType.getQualifiedName(), refType);
        super.visit(decl, solver);
    }
}
