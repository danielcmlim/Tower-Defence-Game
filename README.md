# Tower Defence Game

**TLDR:** A strategic tower defence game where players defend their base against waves of unique enemies using different weapon upgrades and companions.

**Empathize:**
To create a tower defence game that challenges players to use their strategic and mechanical skills to, manage their currency and adapt to different enemy types.

**Research / Inspiration:**

- Bloons TD 6: Inspired by the variety of enemies and upgrades
- Plants vs. Zombies: Like the simple but strategic gameplay and progressively harder waves
- Valorant: Buy/Shop system for better means of defending against enemies

**Target Audience:**
Players aged 12+ who enjoy strategy, action, and tower defence games.

**Define:**
I want to create a game where players defend their base against increasingly difficult waves of enemies. Players earn currency by defeating enemies and can spend it in a shop to purchase and upgrade weapons and companions. Different enemies will have unique abilities and weaknesses, requiring players to change their strategy depending on the wave.

**Gameplay / Features:**

- Multiple unique enemy types with different abilities and weaknesses.
- Different weapons/towers designed for different situations.
- Currency earned from defeating enemies.
- Shop for purchasing weapons, upgrades, and companions.
- Companion system that gives the player can buy for more help defending their base.
- Increasingly difficult waves of enemies.
- Strategic placement and upgrading of defences.

**Controls:**
- WASD for Movement 
- J for Melee Attack
- K for Ranged Attack(after bought)
- B FOr Toggle Shop View

**Setting:** A defended base that is constantly attacked by waves of enemies. The environment can change between levels, with different maps introducing new layouts, obstacles, and strategic challenges.

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
