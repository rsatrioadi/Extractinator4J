package nl.tue.win.graph;

import java.util.HashMap;

public class Edge extends HashMap<String, Object> {
    private final Node source;
    private final Node target;
    private final String interaction;

    public Edge(Node source, Node target) {
        this(source, target, "");
    }

    public Edge(Node source, Node target, String interaction) {
        this.source = source;
        this.target = target;
        this.interaction = interaction;
        put("interaction", interaction);
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }

    public String getInteraction() {
        return interaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
//        if (!super.equals(o)) return false;

        Edge edge = (Edge) o;

        if (!getSource().equals(edge.getSource())) return false;
        if (!getTarget().equals(edge.getTarget())) return false;
        return getInteraction().equals(edge.getInteraction());
    }

    @Override
    public int hashCode() {
        int result = getSource().hashCode();
        result = 31 * result + getTarget().hashCode();
        result = 31 * result + getInteraction().hashCode();
        return result;
    }
}
