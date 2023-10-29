package invaders.gameobject;

import invaders.engine.GameEngine;
import invaders.memento.Originator;
//import invaders.memento.GameObjectCaretaker;

// contains basic methods that all GameObjects must implement
public interface GameObject extends Originator {

    public void start();
    public void update(GameEngine model);
//    public default GameObjectCaretaker getCaretaker() { return new GameObjectCaretaker(this); }
}
