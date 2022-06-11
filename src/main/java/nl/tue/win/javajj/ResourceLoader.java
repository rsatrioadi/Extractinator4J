package nl.tue.win.javajj;

import java.io.InputStream;

public class ResourceLoader {
    public InputStream loadResource(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }
}
