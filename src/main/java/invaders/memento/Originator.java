package invaders.memento;

/**
 * This is a generic originator
 */
public interface Originator {
    public default Memento save() { return null; }
    public default void restore(Memento memento) { }
}
