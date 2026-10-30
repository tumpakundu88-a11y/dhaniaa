# Dhania Client (Fabric, Minecraft 1.21.11)

## Build
1. Install **JDK 21**.
2. In this folder run:  `gradle wrapper --gradle-version 8.12`  then  `./gradlew build`
   (or just open the folder in IntelliJ IDEA and let it import - it creates the wrapper for you)
3. Jar: `build/libs/dhania-client-1.0.0.jar`
4. Put the jar + **Fabric API** (1.21.4) in your `.minecraft/mods` folder, launch with Fabric Loader.

## Keys
- Right Shift - Dhania menu (toggle modules, Edit HUD, Waypoints)
- B - Waypoints screen
- C (hold) - Zoom

## Easiest: build online (no install)
Upload this folder to a new GitHub repo -> Actions tab -> "Build Dhania Client" -> run -> download artifact `DhaniaClient-jar`.

## This is the 1.21.11 version
Needs: Fabric Loader 0.18.1+ and Fabric API for 1.21.11. Build uses Gradle 9.2.1 + Loom 1.14.
