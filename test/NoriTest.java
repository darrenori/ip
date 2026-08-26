import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Runs end-to-end regression tests for Nori's commands, date queries, and persisted task state.
 */
public class NoriTest {
    /**
     * Runs every end-to-end regression test.
     *
     * @param args ignored command-line arguments
     * @throws Exception if a test cannot create its isolated environment or Nori cannot run
     */
    public static void main(String[] args) throws Exception {
        runTest("Completes a mixed task workflow", NoriTest::mixedTaskWorkflow_updatesListAndStatuses);
        runTest("Rejects malformed input without adding tasks", NoriTest::malformedCommands_leaveTaskListEmpty);
        runTest("Finds deadlines and events on a date", NoriTest::onCommand_matchesDeadlineAndEventDates);
        runTest("Persists every task type and completion state", NoriTest::savedTasks_restoreAcrossLaunches);
        System.out.println("All Nori regression tests passed.");
    }

    private static void mixedTaskWorkflow_updatesListAndStatuses() throws Exception {
        Path testDirectory = Files.createTempDirectory("nori-regression-test-");
        try {
            String output = runNori(testDirectory, "todo read book\n"
                    + "deadline return book /by 2024-02-29\n"
                    + "event exam /from 2024-02-29 0900 /to 2024-02-29 1200\n"
                    + "mark 2\n"
                    + "unmark 2\n"
                    + "delete 1\n"
                    + "list\n"
                    + "bye\n");

            String finalList = getFinalList(output);
            assertContains(finalList, "1.[D][ ] return book (by: Feb 29 2024)");
            assertContains(finalList, "2.[E][ ] exam (from: 2024-02-29 0900 to: 2024-02-29 1200)");
            assertNotContains(finalList, "read book");
            assertContains(output, "Nice! I've marked this task as done:");
            assertContains(output, "OK, I've marked this task as not done yet:");
        } finally {
            deleteDirectory(testDirectory);
        }
    }

    private static void malformedCommands_leaveTaskListEmpty() throws Exception {
        Path testDirectory = Files.createTempDirectory("nori-regression-test-");
        try {
            String output = runNori(testDirectory, "todo\n"
                    + "deadline impossible /by 2024-02-30\n"
                    + "event invalid /from 2024-2-29 0900 /to 2024-02-29 1000\n"
                    + "on\n"
                    + "on 2024-02-30\n"
                    + "list\n"
                    + "bye\n");

            assertContains(output, "OOPS!!! A todo needs a description.");
            assertContains(output, "OOPS!!! I cannot understand \"2024-02-30\" as a deadline.");
            assertContains(output, "OOPS!!! I cannot understand \"2024-2-29\" as an event date.");
            assertContains(output, "OOPS!!! \"on\" needs a date.");
            assertContains(output, "OOPS!!! I cannot understand \"2024-02-30\" as a date.");
            assertContains(output, "Your list is empty.");
        } finally {
            deleteDirectory(testDirectory);
        }
    }

    private static void onCommand_matchesDeadlineAndEventDates() throws Exception {
        Path testDirectory = Files.createTempDirectory("nori-regression-test-");
        try {
            String output = runNori(testDirectory, "todo unrelated task\n"
                    + "deadline submit work /by 2024-03-01\n"
                    + "event workshop /from 2024-03-01 0900 /to 2024-03-01 1100\n"
                    + "event overnight session /from 2024-02-29 2300 /to 2024-03-01 0100\n"
                    + "on 2024-03-01\n"
                    + "on 2024-03-02\n"
                    + "bye\n");

            String matchingTasks = getSectionAfter(output, "Here are the deadlines and events on 2024-03-01:");
            assertContains(matchingTasks, "2.[D][ ] submit work (by: Mar 01 2024)");
            assertContains(matchingTasks, "3.[E][ ] workshop (from: 2024-03-01 0900 to: 2024-03-01 1100)");
            assertContains(matchingTasks, "4.[E][ ] overnight session (from: 2024-02-29 2300 to: 2024-03-01 0100)");
            assertNotContains(matchingTasks, "unrelated task");
            assertContains(output, "There are no deadlines or events on 2024-03-02.");
        } finally {
            deleteDirectory(testDirectory);
        }
    }

    private static void savedTasks_restoreAcrossLaunches() throws Exception {
        Path testDirectory = Files.createTempDirectory("nori-regression-test-");
        try {
            runNori(testDirectory, "todo pack bag\n"
                    + "deadline return book /by 2024-12-31\n"
                    + "event celebration /from 2024-12-31 2000 /to 2025-01-01 0100\n"
                    + "mark 1\n"
                    + "mark 3\n"
                    + "bye\n");
            String output = runNori(testDirectory, "list\n"
                    + "on 2025-01-01\n"
                    + "bye\n");

            assertContains(output, "1.[T][X] pack bag");
            assertContains(output, "2.[D][ ] return book (by: Dec 31 2024)");
            assertContains(output, "3.[E][X] celebration (from: 2024-12-31 2000 to: 2025-01-01 0100)");
            assertContains(output, "Here are the deadlines and events on 2025-01-01:");
            assertContains(output, "3.[E][X] celebration");
        } finally {
            deleteDirectory(testDirectory);
        }
    }

    private static void runTest(String testName, NoriTestCase testCase) throws Exception {
        testCase.run();
        System.out.println("PASS: " + testName);
    }

    /**
     * Runs Nori in the given directory with one complete standard-input session.
     *
     * @param workingDirectory the isolated working directory for this Nori launch
     * @param input the standard-input content to send to Nori
     * @return Nori's complete console output
     * @throws IOException if the child process cannot be started or communicated with
     * @throws InterruptedException if the current thread is interrupted while Nori is running
     */
    private static String runNori(Path workingDirectory, String input) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("java", "-cp", getAbsoluteClassPath(), "Nori")
                .directory(workingDirectory.toFile())
                .redirectErrorStream(true)
                .start();
        process.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8));
        process.getOutputStream().close();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new AssertionError("Nori exited with status " + exitCode + ":\n" + output);
        }
        return output;
    }

    /**
     * Returns the final displayed task list from a Nori session.
     *
     * @param output Nori's complete console output
     * @return the part of the output beginning with the final task-list heading
     */
    private static String getFinalList(String output) {
        return getSectionAfter(output, "Here are the tasks in your list:");
    }

    /**
     * Returns the final output section beginning with a required heading.
     *
     * @param output Nori's complete console output
     * @param heading the required section heading
     * @return the output beginning at the final occurrence of {@code heading}
     */
    private static String getSectionAfter(String output, String heading) {
        int headingIndex = output.lastIndexOf(heading);
        if (headingIndex == -1) {
            throw new AssertionError("Expected output to contain \"" + heading + "\" but was:\n" + output);
        }
        return output.substring(headingIndex);
    }

    /**
     * Returns the compiled test classpath as an absolute path for child Nori processes.
     *
     * @return the absolute classpath for the compiled test classes
     */
    private static String getAbsoluteClassPath() {
        return Path.of(System.getProperty("java.class.path")).toAbsolutePath().toString();
    }

    /**
     * Deletes an isolated test directory and all of its contents.
     *
     * @param directory the directory to delete
     * @throws IOException if the directory tree cannot be read
     */
    private static void deleteDirectory(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(NoriTest::deletePath);
        }
    }

    private static void deletePath(Path path) {
        try {
            Files.delete(path);
        } catch (IOException exception) {
            throw new AssertionError("Could not clean up test path: " + path, exception);
        }
    }

    private static void assertContains(String actual, String expected) {
        if (!actual.contains(expected)) {
            throw new AssertionError("Expected output to contain \"" + expected + "\" but was:\n" + actual);
        }
    }

    private static void assertNotContains(String actual, String unexpected) {
        if (actual.contains(unexpected)) {
            throw new AssertionError("Expected output not to contain \"" + unexpected + "\" but was:\n" + actual);
        }
    }

    /**
     * Represents one Nori regression test that can report a checked failure.
     */
    @FunctionalInterface
    private interface NoriTestCase {
        void run() throws Exception;
    }
}
