package nl.tue.win.extractinator;

import com.github.javaparser.resolution.Resolvable;

import java.util.Optional;

public class Resolver<T> {
    private T resolution;

    public Resolver(Resolvable<T> resolvable) {
        try {
            resolution = resolvable.resolve();
        } catch (Exception e) {
            resolution = null;
        }
    }

    public boolean canResolve() {
        return resolution != null;
    }

    public Optional<T> getResolved() {
        return Optional.ofNullable(resolution);
    }
}
