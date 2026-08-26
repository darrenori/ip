import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Runs automated end-to-end tests for Nori's disk storage behavior from the root test folder.
 */
public class StorageTest {
    private static final String DATA_DIRECTORY = "data";
    private static final String STORAGE_FILE = "nori.txt";
    private static final String BACKUP_FILE = "nori.txt.bak";
    private static final String CORRUPT_FILE = "nori.txt.corrupt";

    public static void main(String[] args) throws Exception {
        runTest("Creates missing data directory", StorageTest::saveTasks_missingDirectory_createsStorageFiles);
        runTest("Restores delimiter-containing task", StorageTest::loadTasks_encodedField_restoresTask);
        runTest("Restores a deadline date and time", StorageTest::loadTasks_deadline_restoresDateTime);
        runTest("Recovers corrupted storage from backup", StorageTest::loadTasks_corruptedFile_restoresBackup);
        runTest("Preserves corruption without backup", StorageTest::loadTasks_noBackup_preservesCorruptedFile);
        System.out.println("All storage tests passed.");
    }

    private static void saveTasks_missingDirectory_createsStorageFiles() throws Exception {
        Path testDirectory = Files.createTempDirectory("nori-storage-test-");
        try {
            runNori(testDirectory, "todo create storage\nbye\n");

            Path dataDirectory = testDirectory.resolve(DATA_DIRECTORY);
            assertTrue(Files.isDirectory(dataDirectory), "Expected the data directory to be created.");
            assertTrue(Files.isRegularFile(dataDirectory.resolve(STORAGE_FILE)), "Expected nori.txt to exist.");
            assertTrue(Files.isRegularFile(dataDirectory.resolve(BACKUP_FILE)), "Expected nori.txt.bak to exist.");
        } finally {
            deleteDirectory(testDirectory);
        }
    }

    private static void loadTasks_encodedField_restoresTask() throws Exception {
        Path testDirectory = Files.createTempDirectory("nori-storage-test-");
        try {
            runNori(testDirectory, "todo revise | annotate notes\nmark 1\nbye\n");
            String output = runNori(testDirectory, "list\nbye\n");

            assertContains(output, "1.[T][X] revise | annotate notes");
        } finally {
            deleteDirectory(testDirectory);
        }
    }

    private static void loadTasks_deadline_restoresDateTime() throws Exception {
        Path testDirectory = Files.createTempDirectory("nori-storage-test-");
        try {
            runNori(testDirectory, "deadline return book /by 2/12/2019 1800\nbye\n");
            String output = runNori(testDirectory, "list\nbye\n");

            assertContains(output, "1.[D][ ] return book (by: Dec 2 2019 6:00 PM)");
        } finally {
            deleteDirectory(testDirectory);
        }
    }

    private static void loadTasks_corruptedFile_restoresBackup() throws Exception {
        Path testDirectory = Files.createTempDirectory("nori-storage-test-");
        try {
            runNori(testDirectory, "todo recover this task\nbye\n");

            Path dataDirectory = testDirectory.resolve(DATA_DIRECTORY);
            Path storageFile = dataDirectory.resolve(STORAGE_FILE);
            String corruptContent = "this is not a Nori task record";
            Files.writeString(storageFile, corruptContent, StandardCharsets.UTF_8);

            String output = runNori(testDirectory, "list\nbye\n");

            assertContains(output, "Your saved tasks were corrupted. I've restored the backup");
            assertContains(output, "1.[T][ ] recover this task");
            assertEquals(corruptContent, Files.readString(dataDirectory.resolve(CORRUPT_FILE), StandardCharsets.UTF_8));
            assertContains(Files.readString(storageFile, StandardCharsets.UTF_8), "b64:cmVjb3ZlciB0aGlzIHRhc2s=");
        } finally {
            deleteDirectory(testDirectory);
        }
    }

    private static void loadTasks_noBackup_preservesCorruptedFile() throws Exception {
        Path testDirectory = Files.createTempDirectory("nori-storage-test-");
        try {
            Path dataDirectory = Files.createDirectory(testDirectory.resolve(DATA_DIRECTORY));
            Path storageFile = dataDirectory.resolve(STORAGE_FILE);
            String corruptContent = "T | invalid-status | corrupted task";
            Files.writeString(storageFile, corruptContent, StandardCharsets.UTF_8);

            String output = runNori(testDirectory, "list\nbye\n");

            assertContains(output, "Your saved tasks are corrupted and no backup is available.");
            assertEquals(corruptContent, Files.readString(storageFile, StandardCharsets.UTF_8));
            assertTrue(Files.notExists(dataDirectory.resolve(CORRUPT_FILE)),
                    "Expected no corrupt copy without a recoverable backup.");
        } finally {
            deleteDirectory(testDirectory);
        }
    }

    private static void runTest(String testName, StorageTestCase testCase) throws Exception {
        testCase.run();
        System.out.println("PASS: " + testName);
    }

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
     * Returns the test classpath as an absolute path so child Nori processes can use it from a temporary directory.
     *
     * @return the absolute classpath for the compiled test classes
     */
    private static String getAbsoluteClassPath() {
        return Path.of(System.getProperty("java.class.path")).toAbsolutePath().toString();
    }

    private static void deleteDirectory(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(StorageTest::deletePath);
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

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected \"" + expected + "\" but was \"" + actual + "\".");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * Represents one storage test that can report a checked failure.
     */
    @FunctionalInterface
    private interface StorageTestCase {
        void run() throws Exception;
    }
}
