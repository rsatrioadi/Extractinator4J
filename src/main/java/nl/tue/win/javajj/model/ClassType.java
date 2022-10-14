package nl.tue.win.javajj.model;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("HttpUrlsUsage")
public class ClassType {

    private static final boolean EXTRACT_CLASS_TYPE = false;
    private static final boolean EXPAND_MEMBERS = true;
    private static final String A = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private final TypeDeclaration<?> decl;
    private final ResolvedReferenceTypeDeclaration refType;

    public ClassType(TypeDeclaration<?> decl) {
        this.decl = decl;
        this.refType = decl.resolve();
    }

    public String getName() {
        return refType.getQualifiedName();
    }

    public Resource addToModel(Model model) {
        String uri = String.format("%s%s", Project.URI_PREFIX, refType.getQualifiedName());

        // Create the class resource
        Resource res = model.createResource(uri)
                .addProperty(model.getProperty(A), model.getResource("http://set.win.tue.nl/ontology#structure"))
                .addProperty(model.getProperty("http://set.win.tue.nl/ontology#named"), refType.getClassName(), "en");

        // Extract & populate class type
        if (EXTRACT_CLASS_TYPE) {
            if (refType.isInterface()) {
                res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#kind"), "interface", "en");
            }
            if (refType.isEnum()) {
                res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#kind"), "enum", "en");
            }
            if (refType.isClass()) {
                res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#kind"), "class", "en");
            }
        }

        // Extract ancestors to populate "specializes"
        if (decl.isClassOrInterfaceDeclaration()) {
            decl.asClassOrInterfaceDeclaration().getExtendedTypes().forEach(sup -> {
                Optional<ClassOrInterfaceType> supType = sup.toClassOrInterfaceType();
                if (supType.isPresent() && supType.get().isReferenceType()) {
                    try {
                        ResolvedReferenceType supRefType = supType.get().resolve().asReferenceType();
                        Resource resSup = model.createResource(String.format("%s%s", Project.URI_PREFIX, supRefType.getQualifiedName()));
                        res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#specializes"), resSup);
                    } catch (UnsolvedSymbolException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            });
            decl.asClassOrInterfaceDeclaration().getImplementedTypes().forEach(itf -> {
                Optional<ClassOrInterfaceType> supType = itf.toClassOrInterfaceType();
                if (supType.isPresent() && supType.get().isReferenceType()) {
                    try {
                        ResolvedReferenceType supRefType = supType.get().resolve().asReferenceType();
                        Resource resSup = model.createResource(String.format("%s%s", Project.URI_PREFIX, supRefType.getQualifiedName()));
                        res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#specializes"), resSup);
                    } catch (UnsolvedSymbolException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            });
        }

        // Extract fields to populate "references"
        List<FieldDeclaration> fields = decl.getFields();
        fields.forEach(field -> field.getVariables().forEach(variable -> {
            try {
                if (EXPAND_MEMBERS) {
                    String varName = variable.getNameAsString();
                    Resource varRes = model.createResource(String.format("%s#%s", uri, varName))
                            .addProperty(model.getProperty(A), model.getResource("http://set.win.tue.nl/ontology#variable"))
                            .addProperty(model.getProperty("http://set.win.tue.nl/ontology#named"), varName, "en");
                    res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#hasVariable"), varRes);
                    if (variable.getType().resolve().isReferenceType()) {
                        ResolvedReferenceType type = variable.getType().resolve().asReferenceType();
                        Resource resFtype = model.createResource(String.format("%s%s", Project.URI_PREFIX, type.getQualifiedName()));
                        varRes.addProperty(model.getProperty("http://set.win.tue.nl/ontology#typed"), resFtype);
                    }
                }
                if (variable.getType().resolve().isReferenceType()) {
                    ResolvedReferenceType type = variable.getType().resolve().asReferenceType();
                    Resource resFtype = model.createResource(String.format("%s%s", Project.URI_PREFIX, type.getQualifiedName()));
                    res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#holds"), resFtype);
                }
            } catch (UnsolvedSymbolException ex) {
                ex.printStackTrace(System.err);
            }
        }));

        // Extract methods
        List<MethodDeclaration> methods = decl.getMethods();
        methods.forEach(method -> {
            try {
                if (EXPAND_MEMBERS) {
                    String metSig = method.getSignature().asString()
                            .replace("(", "_")
                            .replace(")", "_")
                            .replaceAll(", ", ",");
                    Resource metRes = model.createResource(String.format("%s#%s", uri, metSig))
                            .addProperty(model.getProperty(A), model.getResource("http://set.win.tue.nl/ontology#operation"))
                            .addProperty(model.getProperty("http://set.win.tue.nl/ontology#named"), method.getNameAsString(), "en");
                    res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#hasScript"), metRes);
                    // Populate "hasReturnType"
                    if (method.getType().isReferenceType()) {
                        ResolvedReferenceType type = method.getType().resolve().asReferenceType();
                        Resource resFtype = model.createResource(String.format("%s%s", Project.URI_PREFIX, type.getQualifiedName()));
                        metRes.addProperty(model.getProperty("http://set.win.tue.nl/ontology#hasReturnType"), resFtype);
                    }
                    // Populate "hasParameterType"
                    method.getSignature().getParameterTypes().stream().filter(Type::isReferenceType).forEach(t -> {
                        try {
                            ResolvedReferenceType type = t.resolve().asReferenceType();
                            Resource resFtype = model.createResource(String.format("%s%s", Project.URI_PREFIX, type.getQualifiedName()));
                            metRes.addProperty(model.getProperty("http://set.win.tue.nl/ontology#hasParameterType"), resFtype);
                        } catch (Exception ex) {
                            ex.printStackTrace(System.err);
                        }
                    });
                }
                // Populate "returns"
                if (method.getType().isReferenceType()) {
                    ResolvedReferenceType type = method.getType().resolve().asReferenceType();
                    Resource resFtype = model.createResource(String.format("%s%s", Project.URI_PREFIX, type.getQualifiedName()));
                    res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#returns"), resFtype);
                }
                // Populate "accepts"
                method.getSignature().getParameterTypes().stream().filter(Type::isReferenceType).forEach(t -> {
                    try {
                        ResolvedReferenceType type = t.resolve().asReferenceType();
                        Resource resFtype = model.createResource(String.format("%s%s", Project.URI_PREFIX, type.getQualifiedName()));
                        res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#accepts"), resFtype);
                    } catch (Exception ex) {
                        ex.printStackTrace(System.err);
                    }
                });

                System.out.println();
                System.out.println(this);
                System.out.println("method: " + method.getSignature().asString());
                method.getBody().ifPresent(body -> body.getStatements().forEach(this::processStatement));
            } catch (UnsolvedSymbolException ex) {
                ex.printStackTrace(System.err);
            }
        });

        return res;
    }

    void processStatement(Statement s) {
        s.ifExpressionStmt(st -> processExpression(st.getExpression()));
        s.ifBlockStmt(st -> st.getStatements().forEach(this::processStatement));
        s.ifWhileStmt(st -> {
            processStatement(st.getBody());
            processExpression(st.getCondition());
        });
        s.ifForStmt(st -> processStatement(st.getBody()));
        s.ifForEachStmt(st -> processStatement(st.getBody()));
        s.ifDoStmt(st -> processStatement(st.getBody()));
        s.ifIfStmt(st -> {
            processStatement(st.getThenStmt());
            st.getElseStmt().ifPresent(this::processStatement);
        });
        s.ifSwitchStmt(st -> {
            processExpression(st.getSelector());
            st.getEntries().stream()
                    .flatMap(e -> e.getStatements().stream())
                    .forEach(this::processStatement);
        });
    }

    void processExpression(Expression e) {
        System.out.println("expression: " + e);
    }

    @Override
    public String toString() {
        return refType.getQualifiedName();
    }
}
