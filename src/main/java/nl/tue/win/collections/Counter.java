package nl.tue.win.collections;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class Counter<T> extends AbstractMap<T, Integer> {

    private final ConcurrentMap<T, Integer> counts;

    public Counter() {
        counts = new ConcurrentHashMap<>();
    }

    public Counter(Collection<T> items) {
        this();
        items.forEach(this::put);
    }

    public Counter(T... items) {
        this();
        Arrays.stream(items).forEach(this::put);
    }

    public Integer put(T item) {
        return put(item, 1);
    }

    @Override
    public Integer put(T item, Integer count) {
        return counts.merge(item, count, Integer::sum);
    }

    public Integer remove(Object item, Integer qty) {
        int count = get(item);
        if (count - qty > 0) {
            counts.put((T) item, count - qty);
        } else {
            counts.remove(item);
        }
        return count;
    }

    @Override
    public Integer remove(Object item) {
        return remove(item, 1);
    }

    @Override
    public Integer get(Object key) {
        return counts.getOrDefault(key, 0);
    }

    @Override
    public Set<Entry<T, Integer>> entrySet() {
        return counts.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
