# USER'S MANUAL

## Project Title
**The Last Ember: A Java RPG with Bubble Sort-Based Progression**

## System Requirements
- Operating System: Windows 10 or Windows 11
- Java Development Kit: JDK 17
- Gradle: Not required separately because the project already includes the Gradle wrapper
- Disk Space: Enough space for the game assets, videos, maps, and sound files

## How to Run the Program
1. Open the project folder in a terminal or command prompt.
2. Make sure Java 17 is installed on the computer.
3. Run the desktop version of the game using:

```bash
gradlew.bat desktop:run
```

4. Wait for the project to build.
5. After the build finishes, the game window will open.

## Main Menu Guide
When the game starts, the player will see the main menu with three options:
- **Start Game**: Begins a new game.
- **Load Game**: Opens available save files.
- **Exit**: Closes the game.

After selecting **Start Game**, the player will:
1. Enter a character name.
2. Choose a difficulty level.
3. Watch the intro sequence if the intro video is available.
4. Enter the game world.

## In-Game Controls
- `W`, `A`, `S`, `D` or Arrow Keys: Move the player
- `Left Mouse Button`: Basic attack
- `Space`: Dash
- `E`: Interact with objects or NPCs, and use the dash attack when unlocked
- `Enter`: Open chat or continue some dialogue prompts
- `M` or `Esc`: Open the in-game pause/menu panel

## Save and Load Guide
- The game uses an **autosave system**.
- Progress is saved automatically while playing.
- Saved games can be accessed from the **Load Game** menu.
- Save entries can also be deleted from the load screen.

## Notes for the User
- The game window title is **The Last Ember**.
- The default desktop resolution is `1280 x 720`.
- Intro and ending videos are supported in the current build.
- Some shortcut keys such as `F5`, `F6`, `F7`, and `F8` are mainly for testing and development.

---

# A. PROGRAM CODE

## Programming Language and Framework
The project was developed using **Java** as the main programming language.  
For graphics, game screens, input handling, sound, and map rendering, the project uses the **LibGDX** framework.

## Code Structure
The program code is organized into modules so that each part of the game has a clear responsibility.

### Main Source Folders
- `core/src/main/java/com/pixelrpg/game`
- `desktop/src/main/java/com/pixelrpg/game/desktop`
- `core/assets`

### Important Java Files
- `core/src/main/java/com/pixelrpg/game/PixelRpgGame.java`
  This is the main game class. It controls screen changes such as the main menu, character naming, difficulty selection, game screen, intro, and load game screen.

- `desktop/src/main/java/com/pixelrpg/game/desktop/DesktopLauncher.java`
  This is the desktop launcher. It reads the desktop configuration and starts the game window.

- `core/src/main/java/com/pixelrpg/game/screens/MainMenuScreen.java`
  This file handles the main menu interface and the navigation for Start Game, Load Game, and Exit.

- `core/src/main/java/com/pixelrpg/game/screens/CharacterNameScreen.java`
  This file allows the player to enter a character name before starting a new game.

- `core/src/main/java/com/pixelrpg/game/screens/DifficultySelectScreen.java`
  This file allows the player to choose the game difficulty.

- `core/src/main/java/com/pixelrpg/game/screens/LoadGameScreen.java`
  This file shows saved games and allows loading or deleting save entries.

- `core/src/main/java/com/pixelrpg/game/screens/GameScreen.java`
  This is the largest and most important gameplay file. It manages player movement, combat, map loading, quests, HUD display, autosave, NPC interaction, and environmental effects.

- `core/src/main/java/com/pixelrpg/game/save/SaveGameService.java`
  This file handles saving, loading, listing, and deleting save files.

- `core/src/main/java/com/pixelrpg/game/config/MapCatalog.java`
  This file reads map data from JSON and allows the game to load maps in an organized way.

## Configuration Files
- `core/assets/config/desktop.properties`
  Stores the desktop window title, resolution, and video configuration.

- `core/assets/config/maps.json`
  Stores the map list, default map, collision layers, and spawn layers used by the game.

## Example Code Snippet
The following code shows the command used by the desktop launcher to start the game:

```java
new Lwjgl3Application(new PixelRpgGame(introPlaybackService, endingPlaybackService), config);
```

This line creates the desktop game application and opens the main game window.

## Sorting Algorithm Note
The main sorting concept highlighted in this project is **Bubble Sort**.  
Although the full gameplay system includes many RPG mechanics, the project presentation and documentation focus on Bubble Sort as the main assigned sorting algorithm because it is the sorting method required for the final project.

Bubble Sort works by comparing two adjacent values and swapping them when they are in the wrong order. This continues repeatedly until the values are arranged correctly. In the game, this idea is presented through progression, arranged sequences, and algorithm-based mechanics shown during the visual simulation.

---

# B. TUTORIAL

## Tutorial for Playing the Game

### Step 1: Launch the Game
Open the project folder and run:

```bash
gradlew.bat desktop:run
```

Wait for the desktop application to open.

### Step 2: Use the Main Menu
At the main menu, choose one of the following:
- **Start Game** to begin a new adventure
- **Load Game** to continue a previous run
- **Exit** to close the game

### Step 3: Create a New Character
If **Start Game** is selected:
1. Type the desired character name.
2. Press **Confirm**.
3. Choose the preferred difficulty level.

### Step 4: Watch the Intro
If the intro video file is available, the story introduction will play before gameplay starts.

### Step 5: Learn the Controls
While inside the game:
- Move using `W`, `A`, `S`, `D` or the Arrow Keys
- Attack using the `Left Mouse Button`
- Dash using `Space`
- Interact using `E`
- Open the pause menu using `M` or `Esc`

### Step 6: Follow the Story and Objectives
The player should:
1. Talk to important NPCs.
2. Follow the quest objectives shown on the screen.
3. Explore the village, inn, laboratory, and other maps.
4. Defeat enemies and continue the story progression.

### Step 7: Use Save and Load Features
- The game autosaves while the player progresses.
- To continue later, return to the main menu and open **Load Game**.
- Select the desired save file.
- If needed, unwanted save files may be deleted in the load screen.

### Step 8: Finish the Adventure
Continue clearing quests and areas until the player reaches the ending sequence of the game.

## Tutorial for the Project Demonstration
During the classroom presentation, the following flow may be used:
1. Introduce the title of the project and its assigned sorting algorithm.
2. Run the Java program using the Gradle wrapper.
3. Show the main menu, naming screen, and difficulty selection.
4. Demonstrate movement, attack, dash, and interaction.
5. Explain how Bubble Sort is the main algorithm discussed in the documentation.
6. Show the load game or autosave feature.
7. Present screenshots and explain the visual simulation and gameplay mechanics.

## Summary of User Procedure
1. Run the Java project.
2. Start a new game or load an existing save.
3. Control the character using the keyboard and mouse.
4. Follow objectives and complete quests.
5. Use autosave and load features when needed.

