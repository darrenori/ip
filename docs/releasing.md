# Releasing Nori

Use this checklist to publish a distributable version of Nori without adding generated binaries to Git.

## Build and verify the artifact

1. Confirm that Java 25 is active with `java -version`.
2. Run `./gradlew clean check shadowJar` on macOS or Linux, or
   `.\gradlew.bat clean check shadowJar` on Windows.
3. Find the generated artifact at `build/libs/Nori.jar`.
4. Copy only `Nori.jar` into an empty folder outside the repository.
5. Open a command window in that folder and run `java -jar "Nori.jar"`.
6. Add a task, exit Nori, and launch it again to confirm that the adjacent `data` folder preserves the task.

The `build` directory is ignored by Git. Do not force-add `Nori.jar`; regenerate it for each release instead.

## Create the GitHub release

1. Open the repository's **Releases** page on GitHub and choose **Draft a new release**.
2. Create a version tag such as `v0.1` and target the commit intended for distribution.
3. Use a short release title and summarize the user-visible functionality.
4. Attach `build/libs/Nori.jar` under **Attach binaries by dropping them here or selecting them**.
5. Publish the release, then download its attached JAR and repeat the empty-folder launch check.

For later versions, increment the release tag rather than replacing an existing published tag.
