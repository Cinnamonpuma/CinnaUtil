# CinnaUtil Mod

CinnaUtil is a utility mod designed to enhance the Minecraft experience by providing additional functionality and tools for developers and players. This mod is built using the Fabric API and leverages the Meteor Addon API for seamless integration.

## Features

- **Mixin Support**: Modify and extend Minecraft's behavior using mixins.
- **Logging Utilities**: Easily log information for debugging and development.
- **Custom Commands, Modules, and HUDs**: Extend the game with your own features.

## How to Use

1. Clone this repository.
2. Use this project as a template to create your own modules, commands, or HUDs.
3. Build the mod using the Gradle `build` task.
4. Add the generated `.jar` file to your Minecraft `mods` folder.
5. Run Minecraft with the Fabric loader.

## Project Structure

```text
.
│── .github
│   ╰── workflows
│── gradle
│── src
│   ╰── main
│       │── java
│       │── resources
│── LICENSE
│── README.md
│── build.gradle
│── gradlew
│── settings.gradle
