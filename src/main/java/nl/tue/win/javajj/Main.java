package nl.tue.win.javajj;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.symbolsolver.resolution.typesolvers.MemoryTypeSolver;
import nl.tue.win.model.Project;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        try {

            // Load ontology
            OntModel ontology = loadOntologyModel();

            // Load project zip file (from args)
            ProjectLoader loader = new ProjectLoader(args);
            Project project = loader.getProject();
            List<CompilationUnit> units = loader.getCompilationUnits();

            // Populate memory-based type solver
            units.forEach(unit -> {
                VoidVisitor<MemoryTypeSolver> visitor = new MemoryVisitor();
                visitor.visit(unit, loader.getMemSolver());
            });

            // Extract structure from AST
            units.forEach(unit -> {
                VoidVisitor<Project> visitor = new StructureExtractor();
                visitor.visit(unit, project);
            });

            // Create knowledge model
            Model model = ModelFactory.createDefaultModel();
            model.setNsPrefixes(ontology.getNsPrefixMap());
            project.addToModel(model);

            // Print turtle
            model.write(System.out, "TURTLE");

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static OntModel loadOntologyModel() throws IOException {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        try (InputStream stream = new ResourceLoader().loadResource("schema.owl")) {
            RDFDataMgr.read(model, Objects.requireNonNull(stream), Lang.TURTLE);
        }
        return model;
    }
}
