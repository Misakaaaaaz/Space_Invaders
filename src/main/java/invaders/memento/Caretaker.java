package invaders.memento;

public class Caretaker {
    private Originator originator;
    private Memento memento;
    public Caretaker(Originator originator) {
        this.originator = originator;
        this.memento = originator.save();
    }
    public void undo() {
        originator.restore(memento);
    }

    /**
     * Overwrite the current saved state with a new saved state
     */
    public void save() {
        this.memento = originator.save();
    }
}
