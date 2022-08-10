package nl.tue.win.javajj.model;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("HttpUrlsUsage")
public class Package {

    private final String name;
    private final Map<String, ClassType> classes = new HashMap<>();

    public Package(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isDefault() {
        return name.isEmpty();
    }

    public void addClass(ClassType cls) {
        classes.put(cls.getName(), cls);
    }

    public Map<String, ClassType> getClasses() {
        return Collections.unmodifiableMap(classes);
    }

    public Optional<ClassType> getClass(String name) {
        return Optional.ofNullable(classes.get(name));
    }

    public boolean hasClass(String name) {
        return classes.containsKey(name);
    }

    public Resource addToModel(Model model) {
        String uri = String.format("%s%s", Project.URI_PREFIX, getName());
        Resource res = model.createResource(uri)
                .addProperty(model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource("http://set.win.tue.nl/ontology#container"))
                .addProperty(model.getProperty("http://set.win.tue.nl/ontology#named"), getName(), "en");
        classes.values().forEach(cls -> {
            Resource resCls = cls.addToModel(model);
            res.addProperty(model.getProperty("http://set.win.tue.nl/ontology#contains"), resCls);
        });
        return res;
    }

    @Override
    public String toString() {
        return String.format("[%s]\n%s", name, classes.values().stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n")));
    }
}
