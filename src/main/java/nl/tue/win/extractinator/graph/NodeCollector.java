package nl.tue.win.extractinator.graph;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import nl.tue.win.graph.Graph;
import nl.tue.win.graph.Node;

public class NodeCollector extends VoidVisitorAdapter<Graph> {

    private void visitStructure(TypeDeclaration<?> decl, Graph g) {
        ResolvedReferenceTypeDeclaration rdecl = decl.resolve();

//        Node pkgNode = new Node(rdecl.getPackageName());
//        pkgNode.put("name", rdecl.getPackageName());
//        pkgNode.put("type", "Container");
//        g.getNodes().add(pkgNode);

        Node classNode = new Node(rdecl.getQualifiedName());
        classNode.put("package", rdecl.getPackageName());
        classNode.put("name", rdecl.getClassName());
//        classNode.put("qualifiedName", rdecl.getQualifiedName());
        classNode.put("type", "Structure");
        g.getNodes().add(classNode);

//        g.getEdges().add(new Edge(pkgNode, classNode, "contains"));
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration decl, Graph g) {
        visitStructure(decl, g);
        super.visit(decl, g);
    }

    @Override
    public void visit(EnumDeclaration decl, Graph g) {
        visitStructure(decl, g);
        super.visit(decl, g);
    }
}
