package invaders.facade;

import invaders.engine.GameEngine;

public class CheatingFacade {

    private GameEngine gameEngine;

    public CheatingFacade(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public void deleteAllFastAliens() {
        gameEngine.deleteFastAliens();
    }

    public void deleteAllSlowAliens() {
        gameEngine.deleteSlowAliens();
    }

    public void deleteAllFastProjectiles() {
        gameEngine.deleteFastProjectiles();
    }

    public void deleteAllSlowProjectiles() {
        gameEngine.deleteSlowProjectiles();
    }

}

