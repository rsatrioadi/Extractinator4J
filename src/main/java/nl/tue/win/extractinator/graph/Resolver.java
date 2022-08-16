package nl.tue.win.extractinator.graph;

import com.github.javaparser.resolution.Resolvable;

import java.util.Optional;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
public class Resolver<T> {
    private final Resolvable<T> resolvable;
    private Optional<T> resolution = null;

    public Resolver(Resolvable<T> resolvable) {
        this.resolvable = resolvable;
    }

    public Optional<T> getResolution() {
        if (resolution == null) {
            try {
                resolution = Optional.ofNullable(resolvable.resolve());
            } catch (Exception e) {
                resolution = Optional.empty();
            }
        }
        return resolution;
    }
}
