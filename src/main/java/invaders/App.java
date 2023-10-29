package invaders;

import javafx.application.Application;
import javafx.scene.control.*;
import javafx.stage.Stage;
import invaders.engine.GameEngine;
import invaders.engine.GameWindow;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        String config = selectDifficulty();
        if (config == null) {
            System.out.println("User didn't select a difficulty.");
            return; // Or handle this in some other way.
        }
        GameEngine model = new GameEngine(config);
//        GameEngine model = new GameEngine("src/main/resources/config_medium.json");
        GameWindow window = new GameWindow(model);
        window.run();

        primaryStage.setTitle("Space Invaders");
        primaryStage.setScene(window.getScene());
        primaryStage.show();

        window.run();
    }

    private String selectDifficulty() {
        List<String> choices = new ArrayList<>();
        choices.add("Easy");
        choices.add("Medium");
        choices.add("Hard");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Medium", choices);
        dialog.setTitle("Select Difficulty");
        dialog.setHeaderText("Choose a difficulty level:");
        dialog.setContentText("Select your difficulty:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            switch (result.get()) {
                case "Easy":
                    return "src/main/resources/config_easy.json";
                case "Medium":
                    return "src/main/resources/config_medium.json";
                case "Hard":
                    return "src/main/resources/config_hard.json";
                default:
                    return null;
            }
        }
        return null;
    }

}
