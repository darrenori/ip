# Nori

Nori is a desktop task companion with a JavaFX chat interface and a retained console interface for automated testing.

## Running Nori

Ensure that JDK 25 is active, then run the application from the project root:

```powershell
.\gradlew.bat run
```

On macOS or Linux, run `./gradlew run`. Enter commands in the composer at the bottom of the window and press Enter or
select **Send**. Type `help` to see the available commands and `bye` to close the application.

## Setting up in Intellij

Prerequisites: JDK 25, update Intellij to the most recent version.

1. Open Intellij (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into Intellij as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, run the Gradle `run` task or locate `src/main/java/nori/Launcher.java`, right-click it, and choose
   `Run Launcher.main()` (if the code editor is showing compile errors, try restarting the IDE).

The separate `Launcher` class is the application entry point recommended by the SE-EDU JavaFX tutorial. The original
`nori.Nori` entry point still launches the console UI when text-based testing or debugging is more convenient.

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.

## Building the executable JAR

Nori uses the Shadow plugin to create a fat JAR containing the application and its runtime dependencies. Ensure that
Java 25 is active, then run the appropriate command from the project root:

```powershell
.\gradlew.bat clean shadowJar
```

On macOS or Linux, run:

```bash
./gradlew clean shadowJar
```

`clean` removes stale build output, while `shadowJar` creates `build/libs/Nori.jar`. The `build` directory is ignored
by Git because the JAR can be regenerated from the source code.

To run the packaged application independently:

1. Copy `build/libs/Nori.jar` into an empty folder.
2. Open a command window in that folder.
3. Run `java -jar "Nori.jar"`.

Nori creates its `data` folder beside the JAR when it first saves a task. See the
[SE-EDU guide to working with JAR files](https://se-education.org/guides/tutorials/jar.html) for additional context.

For the maintainer workflow, including isolated verification and attaching the binary to GitHub, see
[Releasing Nori](docs/releasing.md).
