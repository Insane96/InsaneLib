package insane96mcp.insanelib.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A thread-safe list backed by an immutable snapshot that is replaced as a whole on every change.
 * Iterating never throws {@link ConcurrentModificationException} and sees a consistent snapshot, and
 * {@link #replaceWith(Collection)} swaps all the contents at once, so readers never see it empty or half filled.<br/>
 * Meant for lists read from multiple threads, e.g. the lists of {@link insane96mcp.insanelib.core.JsonFeature.JsonConfig}
 * read on the client, as in singleplayer they're shared with the integrated server that rewrites them on {@code /reload}.
 * Writes are expensive (each one copies the list), so it's meant for lists that are rarely modified.
 */
public class SnapshotList<T> extends AbstractList<T> implements RandomAccess {
    private volatile List<T> snapshot = Collections.emptyList();

    public SnapshotList() {
    }

    public SnapshotList(Collection<? extends T> elements) {
        this.snapshot = copyOf(elements);
    }

    /**
     * Returns the current contents, which never change
     */
    public List<T> getSnapshot() {
        return this.snapshot;
    }

    /**
     * Replaces all the contents at once
     */
    public synchronized void replaceWith(Collection<? extends T> elements) {
        this.snapshot = copyOf(elements);
    }

    private static <T> List<T> copyOf(Collection<? extends T> elements) {
        // Not List.copyOf, as it doesn't allow null elements
        return Collections.unmodifiableList(new ArrayList<>(elements));
    }

    @Override
    public T get(int index) {
        return this.snapshot.get(index);
    }

    @Override
    public int size() {
        return this.snapshot.size();
    }

    @Override
    public Iterator<T> iterator() {
        return this.snapshot.iterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return this.snapshot.listIterator(index);
    }

    @Override
    public Spliterator<T> spliterator() {
        return this.snapshot.spliterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        this.snapshot.forEach(action);
    }

    @Override
    public boolean contains(Object o) {
        return this.snapshot.contains(o);
    }

    @Override
    public Object[] toArray() {
        return this.snapshot.toArray();
    }

    @Override
    public <E> E[] toArray(E[] a) {
        return this.snapshot.toArray(a);
    }

    @Override
    public synchronized T set(int index, T element) {
        List<T> list = new ArrayList<>(this.snapshot);
        T previous = list.set(index, element);
        this.snapshot = Collections.unmodifiableList(list);
        return previous;
    }

    @Override
    public synchronized void add(int index, T element) {
        List<T> list = new ArrayList<>(this.snapshot);
        list.add(index, element);
        this.snapshot = Collections.unmodifiableList(list);
    }

    @Override
    public synchronized T remove(int index) {
        List<T> list = new ArrayList<>(this.snapshot);
        T removed = list.remove(index);
        this.snapshot = Collections.unmodifiableList(list);
        return removed;
    }

    @Override
    public synchronized boolean addAll(Collection<? extends T> c) {
        if (c.isEmpty())
            return false;
        List<T> list = new ArrayList<>(this.snapshot);
        list.addAll(c);
        this.snapshot = Collections.unmodifiableList(list);
        return true;
    }

    @Override
    public synchronized boolean removeIf(Predicate<? super T> filter) {
        List<T> list = new ArrayList<>(this.snapshot);
        if (!list.removeIf(filter))
            return false;
        this.snapshot = Collections.unmodifiableList(list);
        return true;
    }

    // The ones below are overridden as AbstractList's implementations remove through the iterator, which is read only

    @Override
    public synchronized boolean remove(Object o) {
        List<T> list = new ArrayList<>(this.snapshot);
        if (!list.remove(o))
            return false;
        this.snapshot = Collections.unmodifiableList(list);
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.removeIf(c::contains);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.removeIf(element -> !c.contains(element));
    }

    @Override
    protected synchronized void removeRange(int fromIndex, int toIndex) {
        List<T> list = new ArrayList<>(this.snapshot);
        list.subList(fromIndex, toIndex).clear();
        this.snapshot = Collections.unmodifiableList(list);
    }

    @Override
    public synchronized void replaceAll(UnaryOperator<T> operator) {
        List<T> list = new ArrayList<>(this.snapshot);
        list.replaceAll(operator);
        this.snapshot = Collections.unmodifiableList(list);
    }

    @Override
    public synchronized void sort(Comparator<? super T> c) {
        List<T> list = new ArrayList<>(this.snapshot);
        list.sort(c);
        this.snapshot = Collections.unmodifiableList(list);
    }

    @Override
    public synchronized void clear() {
        this.snapshot = Collections.emptyList();
    }
}
