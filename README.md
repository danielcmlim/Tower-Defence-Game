# Tower Defence Game

<p align="center">
  <img src="towerdefence%20ss1.png" alt="Gameplay screenshot from Tower Defence Game" width="700">
</p>

A strategic tower-defence game built with **Java** and **libGDX**. Defend your base against increasingly difficult enemy waves, earn currency through combat and spend it on weapons, upgrades and companions.

## Overview

The game challenges players to adapt their strategy to enemies with different strengths and weaknesses. Progression involves balancing combat decisions, positioning, weapon choices and resource management.

## Features

- Increasingly difficult waves of enemies
- Currency earned by defeating enemies
- Shop system for weapons, upgrades and companions
- Multiple weapons for different combat situations
- Companion system that supports the player during combat
- Enemy abilities that encourage different combat strategies

## Enemy Types

| Enemy | Behaviour | Strategy required |
|---|---|---|
| Ranged enemy | Attacks the player's base from a distance | Manage positioning and movement |
| Shielded enemy | Can withstand two normal melee attacks | Use attacks efficiently and plan combat |
| Armoured enemy | Can withstand three normal melee attacks and is immune to ranged attacks | Adapt weapon choice and use melee attacks |

## Design Decisions

- **Different enemy weaknesses**: Designed enemies with distinct strengths and weaknesses to encourage players to adapt their combat strategy rather than relying on a single attack.
- **Currency and shop system**: Added resource management by allowing players to earn currency through combat and choose how to spend it on weapons, upgrades and companions.
- **Multiple weapon types**: Melee and ranged weapons serve different purposes. Armoured enemies are immune to ranged attacks to encourage strategic weapon selection.
- **Increasing difficulty**: Enemy waves progressively become harder to provide a clear progression system and require players to continuously adapt.

## Controls

| Key | Action |
|---|---|
| `WASD` | Move |
| `J` | Melee attack |
| `K` | Ranged attack (available after purchase) |
| `S` | Open or close the shop |

## Technologies

- [Java](https://www.java.com/)
- [libGDX](https://libgdx.com/)
- [Gradle](https://gradle.org/)
- [LWJGL3](https://www.lwjgl.org/)

## What I Learned

- Building a game using Java and libGDX
- Designing gameplay systems such as enemies, weapons, shops and progression
- Applying object-oriented programming to organise game entities and systems
- Implementing combat mechanics and collision detection
- Managing game state and debugging gameplay interactions

## Running the Project

### Requirements

- Java Development Kit (JDK)
- Git
- No separate Gradle installation is required because the project includes the Gradle wrapper.

### Run on desktop

From the project root:

    # macOS / Linux
    ./gradlew lwjgl3:run

    # Windows
    gradlew.bat lwjgl3:run

### Build a runnable JAR

    # macOS / Linux
    ./gradlew lwjgl3:jar

    # Windows
    gradlew.bat lwjgl3:jar

The generated JAR will be placed in:

    lwjgl3/build/libs

## Project Structure

- `core` - Shared game logic, entities, screens and gameplay systems
- `lwjgl3` - Desktop launcher built with LWJGL3

## Inspiration

The game draws inspiration from:

- **Bloons TD 6** - Enemy variety, defensive systems and upgrades
- **Plants vs. Zombies** - Accessible strategic gameplay and escalating waves
- **Valorant** - Round-based currency management and purchasing decisions
