package nl.tue.win.collections;

import java.util.ArrayList;
import java.util.Collection;

public class StringList extends ArrayList<String> {

    public StringList(int initialCapacity) {
        super(initialCapacity);
    }

    public StringList() {
    }

    public StringList(Collection<? extends String> c) {
        super(c);
    }

    @Override
    public String toString() {
        return String.join(" ", this);
    }
}
