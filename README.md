# Tower Defence Game

<img src="towerdefence%20ss1.png" alt="Tower Defence Gameplay" width="500">

**TLDR:** A strategic tower defence game where players defend their base against increasingly difficult waves of enemies using different weapons, upgrades, and companions. Players must adapt their strategy based on each enemy's unique strengths and weaknesses.

**Empathize:**
I wanted to create a tower defence game that challenges players to think strategically, manage their resources, and adapt their approach to different enemy types.

**Research / Inspiration:**

- **Bloons TD 6:** Inspired by its variety of enemies, towers, and upgrade systems.
- **Plants vs. Zombies:** Inspired by its simple but strategic gameplay and progressively challenging waves.
- **Valorant:** Inspired by its shop system, where players manage currency to purchase equipment that changes how they approach each round.

**Target Audience:**
Players aged 12+ who enjoy strategy, action, and tower defence games.

**Define:**
I wanted to create a game where players defend their base against increasingly difficult waves of enemies. Players earn currency by defeating enemies and can spend it in the shop to purchase and upgrade weapons and companions.

Each enemy type has different abilities, strengths, and weaknesses, requiring players to change their strategy depending on the enemies they encounter.

**Gameplay / Features:**

- Multiple enemy types with unique abilities, strengths, and weaknesses.
- **Ranged Enemy:** Attacks the player's base from a distance, requiring players to manage positioning and movement.
- **Shielded Enemy:** Can withstand **two normal melee attacks**, requiring players to deal with it differently from standard enemies.
- **Armored Enemy:** Can withstand **three normal melee attacks** and is **immune to ranged attacks**, encouraging players to use melee attacks and adapt their strategy.
- Different weapons designed for different combat situations.
- Currency earned by defeating enemies.
- Shop system for purchasing weapons, upgrades, and companions.
- Companion system that provides additional support during combat.
- Increasingly difficult waves of enemies.
- Strategic resource management and combat decisions.

**Controls:**

- `Arrow Keys` — Movement
- `J` — Melee Attack
- `K` — Ranged Attack (after purchasing)
- `S` — Toggle Shop

**Setting:**
A defended base that is constantly attacked by waves of enemies. As the game progresses, players face increasingly challenging combinations of enemies, requiring them to adapt their weapons, positioning, and strategy to survive.

# libGDX
A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an empty `ApplicationListener` implementation.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
