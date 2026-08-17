# Tower Defence Game

<p align="center">
  <img src="towerdefence%20ss1.png" alt="Gameplay screenshot from Tower Defence Game" width="700">
</p>

A strategic tower-defence game built with **Java** and **libGDX**. Defend your base against increasingly difficult enemy waves, earn currency through combat and spend it on weapons, upgrades and companions.

## Overview

The game challenges players to adapt their strategy to enemies with different strengths and weaknesses. Progression depends on balancing combat decisions, positioning, weapon choices and resource management.

## Features

- Increasingly difficult waves of enemies
- Currency earned by defeating enemies
- Shop system for purchasing weapons, upgrades and companions
- Multiple weapons for different combat situations
- Companion system that supports the player during combat
- Strategic decisions based on enemy abilities and defences

## Enemy Types

| Enemy | Behaviour | Strategy required |
|---|---|---|
| Ranged enemy | Attacks the player's base from a distance | Manage positioning and movement |
| Shielded enemy | Can withstand two normal melee attacks | Use attacks efficiently and plan combat |
| Armoured enemy | Can withstand three normal melee attacks and is immune to ranged attacks | Use melee attacks and adapt weapon choice |

## Controls

| Key | Action |
|---|---|
| `Arrow Keys` | Move |
| `J` | Melee attack |
| `K` | Ranged attack — available after purchase |
| `S` | Open or close the shop |

## Technologies

- Java
- [libGDX](https://libgdx.com/)
- Gradle
- LWJGL3

## Running the Project

### Requirements

- Java Development Kit (JDK)
- Git
- No separate Gradle installation is required because this project includes the Gradle wrapper.

### Run on desktop

From the project root, run:

```bash
# macOS / Linux
./gradlew lwjgl3:run
```

```bat
:: Windows
gradlew.bat lwjgl3:run
```

### Build a runnable JAR

```bash
# macOS / Linux
./gradlew lwjgl3:jar
```

```bat
:: Windows
gradlew.bat lwjgl3:jar
```

The generated JAR will be placed in:

```text
lwjgl3/build/libs
```

## Project Structure

- `core` — Shared game logic, entities, screens and gameplay systems
- `lwjgl3` — Desktop launcher built with LWJGL3

## Inspiration

The game draws inspiration from:

- **Bloons TD 6** — enemy variety, defensive systems and upgrades
- **Plants vs. Zombies** — accessible strategic gameplay and escalating waves
- **Valorant** — round-based currency management and purchasing decisions
