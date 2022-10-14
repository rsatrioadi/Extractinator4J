package nl.tue.win.graph;

import java.util.HashMap;

public class Node extends HashMap<String, Object> {
    private final String id;

    public Node(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
//        if (!super.equals(o)) return false;

        Node node = (Node) o;

        return getId().equals(node.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
