package nl.tue.win.model;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import java.util.List;
import java.util.Set;

@SuppressWarnings("HttpUrlsUsage")
public class ClassType {

    private static final boolean EXTRACT_CLASS_TYPE = false;

    private final ResolvedReferenceTypeDeclaration refType;

    public ClassType(TypeDeclaration<?> decl) {
        this.refType = decl.resolve();
    }

    public String getName() {
        return refType.getQualifiedName();
    }

    public Resource addToModel(Model model) {
        String uri = String.format("%s%s", Project.URI_PREFIX, refType.getQualifiedName());

        // Create the class resource
        Resource res = model.createResource(uri)
                .addProperty(model.getProperty("http://www.w3.org/2000/01/rdf-schema#type"), model.getResource("http://set.win.tue.nl/ontology#class"))
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
        List<ResolvedReferenceType> superclasses = refType.getAncestors();
        superclasses.forEach(sup -> {
            if (!sup.isJavaLangObject()) {
                Resource resSup = model.createResource(String.format("%s%s", Project.URI_PREFIX, sup.asReferenceType().getQualifiedName()));
                res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#specializes"), resSup);
            }
        });

        // Extract fields to populate "has"
        List<ResolvedFieldDeclaration> fields = refType.getDeclaredFields();
        fields.forEach(field -> {
            ResolvedType ftype = field.getType().isArray()
                    ? field.getType().asArrayType().getComponentType()
                    : field.getType();
            if (!ftype.isPrimitive()) {
                Resource resFtype = model.createResource(String.format("%s%s", Project.URI_PREFIX, ftype.asReferenceType().getQualifiedName()));
                res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#has"), resFtype);
                ftype.asReferenceType().typeParametersValues().forEach(param -> {
                    ResolvedType aParam = param.isWildcard()
                            ? param.asWildcard().getBoundedType()
                            : param;
                    Resource resParam = model.createResource(String.format("%s%s", Project.URI_PREFIX, aParam.asReferenceType().getQualifiedName()));
                    res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#has"), resParam);
                });
            }
        });

        // Extract methods
        Set<ResolvedMethodDeclaration> methods = refType.getDeclaredMethods();
        methods.forEach(method -> {

            // Populate "returns"
            ResolvedType returnType = method.getReturnType();
            ResolvedType ftype = returnType.isArray()
                    ? returnType.asArrayType().getComponentType()
                    : returnType;
            if (!ftype.isPrimitive() && !ftype.isVoid()) {
                Resource resFtype = model.createResource(String.format("%s%s", Project.URI_PREFIX, ftype.asReferenceType().getQualifiedName()));
                res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#returns"), resFtype);
                ftype.asReferenceType().typeParametersValues().forEach(param -> {
                    ResolvedType aParam = param.isWildcard()
                            ? param.asWildcard().getBoundedType()
                            : param;
                    Resource resParam = model.createResource(String.format("%s%s", Project.URI_PREFIX, aParam.asReferenceType().getQualifiedName()));
                    res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#returns"), resParam);
                });
            }
        });

        return res;
    }

    @Override
    public String toString() {
        return refType.getQualifiedName();
    }
}
