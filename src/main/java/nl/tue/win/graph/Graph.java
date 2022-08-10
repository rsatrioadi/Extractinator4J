package nl.tue.win.graph;

import java.util.Optional;
import java.util.stream.Collectors;

public class Graph {
    private final String id;
    private final Nodes nodes;
    private final Edges edges;

    public Graph(String id, Nodes nodes, Edges edges) {
        this.id = id;
        this.nodes = nodes;
        this.edges = edges;
    }

    public Graph(String id) {
        this(id, new Nodes(), new Edges());
    }

    public String getId() {
        return id;
    }

    public Nodes getNodes() {
        return nodes;
    }

    public Edges getEdges() {
        return edges;
    }

    public Optional<Node> getNode(String nodeId) {
        return nodes.stream()
                .filter(n -> n.getId().equals(nodeId))
                .findFirst();
    }

    public Nodes getNodes(String attrKey, Object attrValue) {
        return nodes.stream()
                .filter(n -> n.containsKey(attrKey) && n.get(attrKey).equals(attrValue))
                .collect(Collectors.toCollection(Nodes::new));
    }

    @Override
    public String toString() {
        return "Graph:" +
                "\n  Nodes:\n    " + nodes.toString().replaceAll("\n", "\n    ") +
                "\n  Edges:\n    " + edges.toString().replaceAll("\n", "\n    ") +
                "\n";
    }
}
