package nl.tue.win.javajj;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.owlcs.ontapi.internal.AxiomParserProvider;
import com.github.owlcs.ontapi.jena.OntModelFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.semanticweb.owlapi.model.AxiomType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            Model model = loadModel();
            AxiomType.AXIOM_TYPES.stream()
                    .map(AxiomParserProvider::get)
                    .forEach(t -> t.axioms(OntModelFactory.createModel(model.getGraph()))
                            .forEach(System.out::println));
            List<CompilationUnit> units = new ProjectLoader(args).getCompilationUnits();
            units.forEach(unit -> {
                VoidVisitor<?> visitor = new CUPrinter();
                visitor.visit(unit, null);
            });
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public static Model loadModel() throws IOException {
        Model model = ModelFactory.createDefaultModel();
        try (InputStream stream = Main.class.getClassLoader().getResourceAsStream("schema.owl")) {
            RDFDataMgr.read(model, stream, Lang.TURTLE);
        }
        return model;
    }
}
