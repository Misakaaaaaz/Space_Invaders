How to Run the Application

This project is a Gradle project. To run the application, follow the steps below:

Navigate to the root directory of the project.
You should run the program using  'gradle clean build run' in the terminal with the environment configuration below:
Gradle 7.4.2
JDK 17
Unix-based System
This should start the application. Please ensure you have Gradle installed and the required dependencies available.

Implemented Features

Difficulty Level: Adjust the level of difficulty for the game.
Time and Score: Display the elapsed time and player's score.
Undo and Redo: You can undo and redo actions using the 'S' key for save and 'U' key for undo.
Cheat: Use cheats for various advantages in the game. These can be accessed through the GameWindow's cheat buttons, such as "Delete All Fast Aliens", "Delete All Slow Aliens", "Delete All Fast Projectiles" or "Delete All Slow Projectiles".

Design Patterns Used

Singleton Pattern:

Class: ConfigReader
File: ConfigReader.java

Observer Pattern:

Classes: Observer, Observable, GameEngine, GameWindow
Files: Observer.java, Observable.java, GameEngine.java, GameWindow.java

Memento Pattern:

Classes: Classes: PlayerMemento, BunkerMemento, EnemyMemento, GameEngineMemento, GameWindow, Caretaker, Memento, Originator
Files: Bunker.java, Enemy.java, GameEngine.java, Player.java, Caretaker.java, Memento.java, Originator.java, GameWindow.java

Facade Pattern:

Class: CheatingFacade, GameEngine
File: CheatingFacade.java, GameEngine.java

How to Use the Features

Select Difficulty Level: This is done at the start of the game through the selectDifficulty() method in the App class. An appropriate difficulty level can be selected from the available options.

Undo and Redo:

To undo an action, press the 'U' key. This triggers the Caretaker's undo() method and reverts to a previous game state.
To save a state for undoing later, press the 'S' key.

Cheat: The GameWindow provides cheat buttons that, when clicked, execute specific cheat methods. For instance, to delete all fast aliens, click on the "Delete All Fast Aliens" button.
These buttons invoke methods from the CheatingFacade class, such as deleteAllFastAliens() or deleteAllSlowProjectiles().

Additional Information

The game uses the Singleton Pattern for reading configuration settings, ensuring only one instance of ConfigReader exists.

The Observer Pattern is implemented to automatically update the game UI elements, such as Time and Score, when any change occurs in the GameEngine class.

The Memento Pattern provides the undo functionality, allowing the game to revert to a previously saved state.

The Facade Pattern streamlines cheat functionalities in the game, offering a unified CheatingFacade interface to various subsystem interfaces.