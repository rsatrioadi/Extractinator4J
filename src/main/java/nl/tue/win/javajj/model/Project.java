package nl.tue.win.javajj.model;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("HttpUrlsUsage")
public class Project {

    public static final String URI_PREFIX = "http://set.win.tue.nl/model/";

    private final String name;
    private final Map<String, Package> packages = new HashMap<>();

    public Project(String name) {
        this.name = name;
    }

    public void addPackage(Package pkg) {
        packages.put(pkg.getName(), pkg);
    }

    public Map<String, Package> getPackages() {
        return Collections.unmodifiableMap(packages);
    }

    public Optional<Package> getPackage(String name) {
        return Optional.ofNullable(packages.get(name));
    }

    public boolean hasPackage(String name) {
        return packages.containsKey(name);
    }

    public Resource addToModel(Model model) {
        String uri = String.format("%s%s", Project.URI_PREFIX, name);
        Resource res = model.createResource(uri)
                .addProperty(model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource("http://set.win.tue.nl/ontology#project"))
                .addProperty(model.getProperty("http://set.win.tue.nl/ontology#named"), name, "en");
        packages.values().forEach(pkg -> {
            Resource resPkg = pkg.addToModel(model);
            res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#contains"), resPkg);
        });
        return res;
    }

    @Override
    public String toString() {
        return packages.values().stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }
}
