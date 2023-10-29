package invaders.engine;

import java.util.List;
import java.util.ArrayList;

import invaders.ConfigReader;
import invaders.entities.EntityViewImpl;
import invaders.entities.SpaceBackground;
import invaders.memento.Memento;
import invaders.memento.Originator;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import invaders.entities.EntityView;
import invaders.rendering.Renderable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import org.json.simple.JSONObject;

public class GameWindow implements Observer, Originator {

    private VBox cheatsBox;
    private VBox difficultyBox;
    private Label timerLabel = new Label("Timer: 0");
    private Label scoreLabel = new Label("Score: 0"); // Assuming score is added in the future.
	private final int width;
    private final int height;
	private Scene scene;
    private Pane pane;
    private GameEngine model;
    private List<EntityView> entityViews =  new ArrayList<EntityView>();
    private Renderable background;

    private double xViewportOffset = 0.0;
    private double yViewportOffset = 0.0;
    // private static final double VIEWPORT_MARGIN = 280.0;

	public GameWindow(GameEngine model){
        this.model = model;
        this.model.attach(this);  // Register as an observer
		this.width =  model.getGameWidth();
        this.height = model.getGameHeight();

        pane = new Pane();
        scene = new Scene(pane, width, height);
        this.background = new SpaceBackground(model, pane);

        KeyboardInputHandler keyboardInputHandler = new KeyboardInputHandler(this.model, this);

        scene.setOnKeyPressed(keyboardInputHandler::handlePressed);
        scene.setOnKeyReleased(keyboardInputHandler::handleReleased);

        // Add labels to the pane
        pane.getChildren().add(timerLabel);
        pane.getChildren().add(scoreLabel);

        // Position the labels on the screen
        timerLabel.setLayoutX(10);
        timerLabel.setLayoutY(10);

        scoreLabel.setLayoutX(10);
        scoreLabel.setLayoutY(40);

        // Add to the GameWindow class constructor:
        Button btnDeleteFastAliens = new Button("Delete All Fast Aliens");
        Button btnDeleteSlowAliens = new Button("Delete All Slow Aliens");
        Button btnDeleteFastProjectiles = new Button("Delete All Fast Projectiles");
        Button btnDeleteSlowProjectiles = new Button("Delete All Slow Projectiles");

        btnDeleteFastAliens.setFocusTraversable(false);
        btnDeleteSlowAliens.setFocusTraversable(false);
        btnDeleteFastProjectiles.setFocusTraversable(false);
        btnDeleteSlowProjectiles.setFocusTraversable(false);


        btnDeleteFastAliens.setOnAction(event -> model.getCheatingFacade().deleteAllFastAliens());
        btnDeleteSlowAliens.setOnAction(event -> model.getCheatingFacade().deleteAllSlowAliens());
        btnDeleteFastProjectiles.setOnAction(event -> model.getCheatingFacade().deleteAllFastProjectiles());
        btnDeleteSlowProjectiles.setOnAction(event -> model.getCheatingFacade().deleteAllSlowProjectiles());

        VBox cheatsBox = new VBox(10, btnDeleteFastAliens, btnDeleteSlowAliens, btnDeleteFastProjectiles, btnDeleteSlowProjectiles);
        cheatsBox.setLayoutX(width - 160);
        cheatsBox.setLayoutY(10);
        pane.getChildren().add(cheatsBox);
        this.cheatsBox = cheatsBox;

//        Button btnEasy = new Button("Easy");
//        Button btnMedium = new Button("Medium");
//        Button btnHard = new Button("Hard");
//
//        btnEasy.setFocusTraversable(false);
//        btnMedium.setFocusTraversable(false);
//        btnHard.setFocusTraversable(false);
//
//
//        btnEasy.setOnAction(event -> changeDifficulty("src/main/resources/config_easy.json"));
//        btnMedium.setOnAction(event -> changeDifficulty("src/main/resources/config_medium.json"));
//        btnHard.setOnAction(event -> changeDifficulty("src/main/resources/config_hard.json"));
//
//        VBox difficultyBox = new VBox(10, btnEasy, btnMedium, btnHard);
//        difficultyBox.setLayoutX(width - 320); // Adjust position as needed
//        difficultyBox.setLayoutY(10);
//
//        pane.getChildren().add(difficultyBox);
//        this.difficultyBox = difficultyBox;
    }


    @Override
    public void update(Observable o) {
        double elapsedTime = model.getTimer();
        int minutes = (int) (elapsedTime / 60);
        int seconds = (int) (elapsedTime % 60);
        timerLabel.setText(String.format("Timer: %02d:%02d", minutes, seconds));


        int score = model.getScore();
        scoreLabel.setText("Score: " + score);

    }


	public void run() {
         Timeline timeline = new Timeline(new KeyFrame(Duration.millis(17), t -> this.draw()));

         timeline.setCycleCount(Timeline.INDEFINITE);
         timeline.play();
    }


    private void draw(){
        model.update();

        List<Renderable> renderables = model.getRenderables();
        for (Renderable entity : renderables) {
            boolean notFound = true;
            for (EntityView view : entityViews) {
                if (view.matchesEntity(entity)) {
                    notFound = false;
                    view.update(xViewportOffset, yViewportOffset);
                    break;
                }
            }
            if (notFound) {
                EntityView entityView = new EntityViewImpl(entity);
                entityViews.add(entityView);
                pane.getChildren().add(entityView.getNode());
            }
        }

        for (Renderable entity : renderables){
            if (!entity.isAlive()){
                for (EntityView entityView : entityViews){
                    if (entityView.matchesEntity(entity)){
                        entityView.markForDelete();
                    }
                }
            }
        }

        for (EntityView entityView : entityViews) {
            if (entityView.isMarkedForDelete()) {
                pane.getChildren().remove(entityView.getNode());
            }
        }


        model.getGameObjects().removeAll(model.getPendingToRemoveGameObject());
        model.getGameObjects().addAll(model.getPendingToAddGameObject());
        model.getRenderables().removeAll(model.getPendingToRemoveRenderable());
        model.getRenderables().addAll(model.getPendingToAddRenderable());

        model.getPendingToAddGameObject().clear();
        model.getPendingToRemoveGameObject().clear();
        model.getPendingToAddRenderable().clear();
        model.getPendingToRemoveRenderable().clear();

        entityViews.removeIf(EntityView::isMarkedForDelete);

    }

	public Scene getScene() {
        return scene;
    }


    private void changeDifficulty(String config) {
        // 3. Clear all entity views
        pane.getChildren().clear();
        model.detach(this); // Detach current observer

        // Create a new GameEngine with the chosen difficulty and reattach this GameWindow
        model = new GameEngine(config);
        model.attach(this);

        // Add labels, background, etc. back to the pane
        this.background = new SpaceBackground(model, pane);
        pane.getChildren().add(timerLabel);
        pane.getChildren().add(scoreLabel);
        pane.getChildren().addAll(cheatsBox, difficultyBox); // Make sure cheatsBox and difficultyBox are instance variables

        // 4. Reset the timer and score
        model.resetTimer(); // You'll need to add this method in GameEngine
        model.resetScore(); // You'll need to add this method in GameEngine

        // Update the game
        model.update();
    }

    @Override
    public void restore(Memento memento) {
        // Memento object is just a null value - nothing was saved.
        // But when a restore happens, we need to reset all the screen entities.
        for (EntityView entityView : entityViews) {
            pane.getChildren().remove(entityView.getNode());
        }
        entityViews.clear();
    }
}
