package nl.tue.win.graph;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class Edges extends HashSet<Edge> {

    public void addToWeight(Edge edge) {
        add(edge);
        stream().filter(e -> e.equals(edge)).findFirst().ifPresent(e -> {
            if (e.containsKey("weight") && e.get("weight") instanceof Number) {
                Number weight = (Number) e.get("weight");
                e.put("weight", weight.longValue() + 1);
            } else {
                e.put("weight", 1);
            }
        });
    }

    @Override
    public String toString() {
        LinkedHashSet<String> attrKeys = this.stream()
                .flatMap(e -> e.keySet().stream())
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Comparator<Edge> bySource = Comparator.comparing(e -> e.getSource().getId());
        Comparator<Edge> byTarget = Comparator.comparing(e -> e.getTarget().getId());
        Comparator<Edge> byLabel = Comparator.comparing(Edge::getInteraction);
        return String.format("source,target,%s\n%s", String.join(",", attrKeys), this.stream()
                .sorted(bySource.thenComparing(byTarget).thenComparing(byLabel))
                .map(e -> String.format("%s,%s,%s", e.getSource().getId(), e.getTarget().getId(),
                        attrKeys.stream()
                                .map(k -> {
                                    String s = e.getOrDefault(k, "").toString();
                                    if (s.contains(",")) {
                                        s = String.format("\"%s\"", s.replaceAll("\"", "\\\""));
                                    }
                                    return s;
                                })
                                .collect(Collectors.joining(","))))
                .collect(Collectors.joining("\n")));
    }

    public String toRSF() {
        Comparator<Edge> bySource = Comparator.comparing(e -> e.getSource().getId());
        Comparator<Edge> byTarget = Comparator.comparing(e -> e.getTarget().getId());
        Comparator<Edge> byLabel = Comparator.comparing(Edge::getInteraction);
        return this.stream()
                .sorted(bySource.thenComparing(byTarget).thenComparing(byLabel))
                .map(e -> String.format("%s %s %s", e.getInteraction(), e.getSource().getId(), e.getTarget().getId()))
                .collect(Collectors.joining("\n"));
    }
}
