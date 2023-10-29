package invaders.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import invaders.ConfigReader;
import invaders.builder.BunkerBuilder;
import invaders.builder.Director;
import invaders.builder.EnemyBuilder;
import invaders.facade.CheatingFacade;
import invaders.factory.EnemyProjectile;
import invaders.factory.Projectile;
import invaders.gameobject.Bunker;
import invaders.gameobject.Enemy;
import invaders.gameobject.GameObject;
import invaders.entities.Player;
import invaders.memento.Caretaker;
import invaders.memento.Originator;
import invaders.memento.Memento;
import invaders.rendering.Renderable;
import invaders.strategy.FastProjectileStrategy;
import org.json.simple.JSONObject;
//import invaders.memento.GameEngineMemento;

/**
 * This class manages the main loop and logic of the game
 */
public class GameEngine implements Observable, Originator {
    private CheatingFacade cheatingFacade;
    private List<GameObject> gameObjects = new ArrayList<>(); // A list of game objects that gets updated each frame
    private List<GameObject> pendingToAddGameObject = new ArrayList<>();
    private List<GameObject> pendingToRemoveGameObject = new ArrayList<>();

    private List<Renderable> pendingToAddRenderable = new ArrayList<>();
    private List<Renderable> pendingToRemoveRenderable = new ArrayList<>();

    private List<Renderable> renderables = new ArrayList<>();

    private Player player;

    private boolean left;
    private boolean right;
    private int gameWidth;
    private int gameHeight;
    private int timer = 45;
    private double elapsedTimeInSeconds;
    private int score = 0;


    String configPath;

    private List<Observer> observers = new ArrayList<>();


    public GameEngine(String config) {
        // Read the config here
//		ConfigReader.parse(config);
        String difficulty;
        if (config.contains("easy")) {
            difficulty = "easy";
        } else if (config.contains("medium")) {
            difficulty = "medium";
        } else if (config.contains("hard")) {
            difficulty = "hard";
        } else {
            throw new IllegalArgumentException("Invalid config path. Difficulty cannot be determined.");
        }

        ConfigReader.getInstance().parse(difficulty, config);


        // Get game width and height
        gameWidth = ((Long) ((JSONObject) ConfigReader.getInstance().getGameInfo(difficulty).get("size")).get("x")).intValue();
        gameHeight = ((Long) ((JSONObject) ConfigReader.getInstance().getGameInfo(difficulty).get("size")).get("y")).intValue();

        //Get player info
        this.player = new Player(ConfigReader.getInstance().getPlayerInfo(difficulty));

        renderables.add(player);


        Director director = new Director();
        BunkerBuilder bunkerBuilder = new BunkerBuilder();
        //Get Bunkers info
        for (Object eachBunkerInfo : ConfigReader.getInstance().getBunkersInfo(difficulty)) {
            Bunker bunker = director.constructBunker(bunkerBuilder, (JSONObject) eachBunkerInfo);
            gameObjects.add(bunker);
            renderables.add(bunker);
        }


        EnemyBuilder enemyBuilder = new EnemyBuilder();
        //Get Enemy info
        for (Object eachEnemyInfo : ConfigReader.getInstance().getEnemiesInfo(difficulty)) {
            Enemy enemy = director.constructEnemy(this, enemyBuilder, (JSONObject) eachEnemyInfo);
            gameObjects.add(enemy);
            renderables.add(enemy);
        }

        this.cheatingFacade = new CheatingFacade(this);

    }

    public CheatingFacade getCheatingFacade() {
        return cheatingFacade;
    }


    @Override
    public void attach(Observer o) {
        observers.add(o);
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this);
        }
    }

    /**
     * Updates the game/simulation
     */
    public void update() {
        timer += 1;

        movePlayer();

        for (GameObject go : gameObjects) {
            go.update(this);
        }
//        score = 0;

        for (int i = 0; i < renderables.size(); i++) {
            Renderable renderableA = renderables.get(i);

            for (int j = i + 1; j < renderables.size(); j++) {
                Renderable renderableB = renderables.get(j);

                if (renderableA.isColliding(renderableB) &&
                        renderableA.getHealth() > 0 &&
                        renderableB.getHealth() > 0 &&
                        !((renderableA.getRenderableObjectName().equals("Enemy") && renderableB.getRenderableObjectName().equals("EnemyProjectile"))
                                || (renderableA.getRenderableObjectName().equals("EnemyProjectile") && renderableB.getRenderableObjectName().equals("Enemy"))
                                || (renderableA.getRenderableObjectName().equals("EnemyProjectile") && renderableB.getRenderableObjectName().equals("EnemyProjectile")))) {

                    boolean isAEnemyOrProjectile = renderableA.getRenderableObjectName().equals("Enemy") || renderableA.getRenderableObjectName().equals("EnemyProjectile");
                    boolean isBEnemyOrProjectile = renderableB.getRenderableObjectName().equals("Enemy") || renderableB.getRenderableObjectName().equals("EnemyProjectile");
                    boolean isAPlayerProjectile = renderableA.getRenderableObjectName().equals("PlayerProjectile");
                    boolean isBPlayerProjectile = renderableB.getRenderableObjectName().equals("PlayerProjectile");

                    if ((isAEnemyOrProjectile && isBPlayerProjectile) || (isBEnemyOrProjectile && isAPlayerProjectile)) {


                        // Add scores based on the type and properties of RenderableA and RenderableB
                        score += getScoreFromRenderable(renderableA);
                        score += getScoreFromRenderable(renderableB);
                    }

                    // Apply damage to both objects
                    renderableA.takeDamage(1);
                    renderableB.takeDamage(1);
                }
            }
        }


        // ensure that renderable foreground objects don't go off-screen
        int offset = 1;
        for (Renderable ro : renderables) {
            if (!ro.getLayer().equals(Renderable.Layer.FOREGROUND)) {
                continue;
            }
            if (ro.getPosition().getX() + ro.getWidth() >= gameWidth) {
                ro.getPosition().setX((gameWidth - offset) - ro.getWidth());
            }

            if (ro.getPosition().getX() <= 0) {
                ro.getPosition().setX(offset);
            }

            if (ro.getPosition().getY() + ro.getHeight() >= gameHeight) {
                ro.getPosition().setY((gameHeight - offset) - ro.getHeight());
            }

            if (ro.getPosition().getY() <= 0) {
                ro.getPosition().setY(offset);
            }
        }


        if (!allEnemiesDestroyed() && player.isAlive()) {
            elapsedTimeInSeconds += (double) 1 / 60;
        }

        notifyObservers();

    }

    public double getTimer() {
        return elapsedTimeInSeconds;
    }

    public int getScore() {
        return score;
    }

    public List<Renderable> getRenderables() {
        return renderables;
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public List<GameObject> getPendingToAddGameObject() {
        return pendingToAddGameObject;
    }

    public List<GameObject> getPendingToRemoveGameObject() {
        return pendingToRemoveGameObject;
    }

    public List<Renderable> getPendingToAddRenderable() {
        return pendingToAddRenderable;
    }

    public List<Renderable> getPendingToRemoveRenderable() {
        return pendingToRemoveRenderable;
    }


    public void leftReleased() {
        this.left = false;
    }

    public void rightReleased() {
        this.right = false;
    }

    public void leftPressed() {
        this.left = true;
    }

    public void rightPressed() {
        this.right = true;
    }

    public boolean shootPressed() {
        if (timer > 45 && player.isAlive()) {
            Projectile projectile = player.shoot();
            gameObjects.add(projectile);
            renderables.add(projectile);
            timer = 0;
            return true;
        }
        return false;
    }

    private void movePlayer() {
        if (left) {
            player.left();
        }

        if (right) {
            player.right();
        }
    }

    public int getGameWidth() {
        return gameWidth;
    }

    public int getGameHeight() {
        return gameHeight;
    }

    public Player getPlayer() {
        return player;
    }

    private boolean allEnemiesDestroyed() {
        for (GameObject gameObject : gameObjects) {
            if (gameObject instanceof Enemy && ((Enemy) gameObject).isAlive()) {
                return false;
            }
        }
        return true;
    }

    private int getScoreFromRenderable(Renderable renderable) {
        int localScore = 0;
        if (renderable.getRenderableObjectName().equals("Enemy")) {
            if (((Enemy) renderable).getProjectileStrategy() instanceof FastProjectileStrategy) {
                localScore = 4;
            } else {
                localScore = 3;
            }
        } else if (renderable.getRenderableObjectName().equals("EnemyProjectile")) {
            if (((EnemyProjectile) renderable).getStrategy() instanceof FastProjectileStrategy) {
                localScore = 2;
            } else {
                localScore = 1;
            }
        }
        return localScore;
    }


    public void deleteFastAliens() {
        List<GameObject> aliensToRemove = gameObjects.stream()
                .filter(gameObject ->
                        gameObject instanceof Enemy &&
                                ((Enemy) gameObject).getProjectileStrategy() instanceof FastProjectileStrategy &&
                                ((Enemy) gameObject).getHealth() > 0)
                .collect(Collectors.toList());
        score += 4 * aliensToRemove.size(); // 4 points per fast alien
        aliensToRemove.forEach(gameObject -> ((Enemy) gameObject).setLives(0));
    }

    public void deleteSlowAliens() {
        List<GameObject> aliensToRemove = gameObjects.stream()
                .filter(gameObject ->
                        gameObject instanceof Enemy &&
                                !(((Enemy) gameObject).getProjectileStrategy() instanceof FastProjectileStrategy) &&
                                ((Enemy) gameObject).getHealth() > 0)
                .collect(Collectors.toList());
        score += 3 * aliensToRemove.size(); // 3 points per slow alien
        aliensToRemove.forEach(gameObject -> ((Enemy) gameObject).setLives(0));
    }

    public void deleteFastProjectiles() {
        List<GameObject> projectilesToRemove = gameObjects.stream()
                .filter(gameObject ->
                        gameObject instanceof EnemyProjectile &&
                                ((EnemyProjectile) gameObject).getStrategy() instanceof FastProjectileStrategy &&
                                ((EnemyProjectile) gameObject).getHealth() > 0)
                .collect(Collectors.toList());
        score += 2 * projectilesToRemove.size(); // 2 points per fast projectile
        projectilesToRemove.forEach(gameObject -> ((EnemyProjectile) gameObject).takeDamage(1));
    }

    public void deleteSlowProjectiles() {
        List<GameObject> projectilesToRemove = gameObjects.stream()
                .filter(gameObject ->
                        gameObject instanceof EnemyProjectile &&
                                !(((EnemyProjectile) gameObject).getStrategy() instanceof FastProjectileStrategy) &&
                                ((EnemyProjectile) gameObject).getHealth() > 0)
                .collect(Collectors.toList());
        score += 1 * projectilesToRemove.size(); // 1 point per slow projectile
        projectilesToRemove.forEach(gameObject -> ((EnemyProjectile) gameObject).takeDamage(1));
    }


    public void resetTimer() {
        this.elapsedTimeInSeconds = 0;
    }

    public void resetScore() {
        this.score = 0;
    }


    private static class GameEngineMemento implements Memento {

        public double elapsedTimeInSeconds;
        public int score = 0;
        public List<Caretaker> caretakers;
        public List<GameObject> gameObjects;
        public List<Renderable> renderables;

        private List<GameObject> pendingToAddGameObject = new ArrayList<>();
        private List<GameObject> pendingToRemoveGameObject = new ArrayList<>();

        private List<Renderable> pendingToAddRenderable = new ArrayList<>();
        private List<Renderable> pendingToRemoveRenderable = new ArrayList<>();

        public GameEngineMemento(List<GameObject> gameObjects, List<Renderable> renderables,
                                 List<GameObject> pendingToAddGameObject, List<GameObject> pendingToRemoveGameObject,
                                 List<Renderable> pendingToAddRenderable, List<Renderable> pendingToRemoveRenderable,
                                 double elapsedTimeInSeconds, int score) {
            caretakers = new ArrayList<Caretaker>();
            for (Renderable renderable : renderables) {
                caretakers.add(new Caretaker(renderable));
            }
            this.gameObjects = new ArrayList<GameObject>(gameObjects);
            this.renderables = new ArrayList<Renderable>(renderables);
            this.pendingToAddGameObject = new ArrayList<GameObject>(pendingToAddGameObject);
            this.pendingToRemoveGameObject = new ArrayList<GameObject>(pendingToRemoveGameObject);
            this.pendingToAddRenderable = new ArrayList<Renderable>(pendingToAddRenderable);
            this.pendingToRemoveRenderable = new ArrayList<Renderable>(pendingToRemoveRenderable);
            this.elapsedTimeInSeconds = elapsedTimeInSeconds;
            this.score = score;
        }
    }

    @Override
    public Memento save() {
        return new GameEngineMemento(gameObjects, renderables, pendingToAddGameObject, pendingToRemoveGameObject,
                pendingToAddRenderable, pendingToRemoveRenderable, elapsedTimeInSeconds, score);
    }

    @Override
    public void restore(Memento memento) {
        GameEngineMemento gameEngineMemento = (GameEngineMemento) memento;
        for (Caretaker caretaker : gameEngineMemento.caretakers) {
            caretaker.undo();
        }
        this.gameObjects = gameEngineMemento.gameObjects;
        this.renderables = gameEngineMemento.renderables;
        this.pendingToAddGameObject = gameEngineMemento.pendingToAddGameObject;
        this.pendingToRemoveGameObject = gameEngineMemento.pendingToRemoveGameObject;
        this.pendingToAddRenderable = gameEngineMemento.pendingToAddRenderable;
        this.pendingToRemoveRenderable = gameEngineMemento.pendingToRemoveRenderable;
        this.elapsedTimeInSeconds = gameEngineMemento.elapsedTimeInSeconds;
        this.score = gameEngineMemento.score;
    }
}
