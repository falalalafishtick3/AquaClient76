# Aqua Client (Fabric 1.21.11)

A client-side Fabric mod project targeting Minecraft 1.21.11 and Java 21.

## Build

Install JDK 21 and Gradle 9.x, open a terminal in this folder, then run:

- Windows: `gradle build`
- Linux/macOS: `gradle build`

The remapped mod jar should be created in `build/libs/`.

Copy the jar into `.minecraft/mods/` alongside Fabric API for Minecraft 1.21.11.

## Controls

- Right Shift: open/close ClickGUI
- Click StorageFinder or SpawnerFinder to select its settings
- Click Save settings or close the GUI to save to `config/aquaclient.json`

## Current prototype scope

- Storage scan includes chests, trapped/ender chests, shulker boxes, droppers, dispensers and barrels.
- Spawner scan includes mob spawners.
- Current world renderer draws configurable-colour, configurable-opacity outlines.
- The GUI exposes additional settings as an early prototype; fill rendering, wall-occlusion control, tracers, pulse animation and true line-width control still need their rendering implementations. The current renderer uses Minecraft's line render layer, whose width is controlled by the graphics backend.

This project has not been compiled or tested in-game in this environment. It requires Gradle to resolve Fabric/Minecraft dependencies during the first build.
