package nl.tue.win.model;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
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
    private static final boolean EXPAND_MEMBERS = false;
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
                .addProperty(model.getProperty(A), model.getResource("http://set.win.tue.nl/ontology#class"))
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

        // Extract fields to populate "has"
        List<FieldDeclaration> fields = decl.getFields();
        fields.forEach(field -> field.getVariables().forEach(variable -> {
            try {
                if (EXPAND_MEMBERS) {
                    String varName = variable.getNameAsString();
                    Resource varRes = model.createResource(uri + "#" + varName)
                            .addProperty(model.getProperty(A), model.getResource("http://set.win.tue.nl/ontology#variable"))
                            .addProperty(model.getProperty("http://set.win.tue.nl/ontology#named"), varName, "en");
                    res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#has_member"), varRes);
                    if (variable.getType().resolve().isReferenceType()) {
                        ResolvedReferenceType type = variable.getType().resolve().asReferenceType();
                        Resource resFtype = model.createResource(String.format("%s%s", Project.URI_PREFIX, type.getQualifiedName()));
                        varRes.addProperty(model.getProperty(A), resFtype);
                    }
                }
                if (variable.getType().resolve().isReferenceType()) {
                    ResolvedReferenceType type = variable.getType().resolve().asReferenceType();
                    Resource resFtype = model.createResource(String.format("%s%s", Project.URI_PREFIX, type.getQualifiedName()));
                    res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#has"), resFtype);
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
                    String metSig = method.getSignature().asString();
                    Resource metRes = model.createResource(uri + "#" + metSig)
                            .addProperty(model.getProperty(A), model.getResource("http://set.win.tue.nl/ontology#function"))
                            .addProperty(model.getProperty("http://set.win.tue.nl/ontology#named"), method.getNameAsString(), "en");
                    res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#has_member"), metRes);
                    // Populate "returns"
                    if (method.getType().resolve().isReferenceType()) {
                        ResolvedReferenceType type = method.getType().resolve().asReferenceType();
                        Resource resFtype = model.createResource(String.format("%s%s", Project.URI_PREFIX, type.getQualifiedName()));
                        metRes.addProperty(model.getProperty("http://set.win.tue.nl/ontology#has_return_type"), resFtype);
                    }
                }
                // Populate "returns"
                if (method.getType().resolve().isReferenceType()) {
                    ResolvedReferenceType type = method.getType().resolve().asReferenceType();
                    Resource resFtype = model.createResource(String.format("%s%s", Project.URI_PREFIX, type.getQualifiedName()));
                    res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#returns"), resFtype);
                }
            } catch (UnsolvedSymbolException ex) {
                ex.printStackTrace(System.err);
            }
        });

        return res;
    }

    @Override
    public String toString() {
        return refType.getQualifiedName();
    }
}
