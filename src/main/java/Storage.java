import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Stores Nori's tasks on the local disk.
 */
public class Storage {
    private static final String STORAGE_DIRECTORY_PROPERTY = "nori.storage.dir";
    private static final Path PROJECT_ROOT = findProjectRoot();
    private static final Path STORAGE_DIRECTORY = findStorageDirectory();
    private static final Path FILE_PATH = STORAGE_DIRECTORY.resolve("nori.txt");
    private static final Path BACKUP_FILE_PATH = STORAGE_DIRECTORY.resolve("nori.txt.bak");
    private static final Path CORRUPT_FILE_PATH = STORAGE_DIRECTORY.resolve("nori.txt.corrupt");
    private static final Path LEGACY_FILE_PATH = PROJECT_ROOT.resolve("src").resolve("main")
            .resolve("java").resolve("data").resolve("nori.txt");
    private static final Path LEGACY_BACKUP_FILE_PATH = PROJECT_ROOT.resolve("src").resolve("main")
            .resolve("java").resolve("data").resolve("nori.txt.bak");
    private static final String FIELD_PREFIX = "b64:";
    private static final String TASK_SEPARATOR = " | ";
    private static final String TEMP_FILE_PREFIX = "nori-";
    private static final String TEMP_FILE_SUFFIX = ".tmp";
    private static boolean canWriteToStorage = true;
    private static String loadingNotice;

    /**
     * Saves every task to the storage file, replacing its previous contents.
     *
     * @param tasks the tasks to save
     * @throws NoriException if the tasks cannot be written to disk
     */
    public static void saveTasks(List<Task> tasks) throws NoriException {
        if (!canWriteToStorage) {
            throw new NoriException("OOPS!!! I won't overwrite saved tasks that could not be read.");
        }
        if (tasks == null) {
            throw new NoriException("OOPS!!! I couldn't save an empty task list reference.");
        }

        List<String> taskLines = new ArrayList<>();
        for (Task task : tasks) {
            taskLines.add(formatTask(task));
        }

        Path temporaryFile = null;
        try {
            Path storageDirectory = FILE_PATH.getParent();
            Files.createDirectories(storageDirectory);
            temporaryFile = Files.createTempFile(storageDirectory, TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
            Files.write(temporaryFile, taskLines, StandardCharsets.UTF_8);
            replaceStorageFile(temporaryFile);
            updateBackupFile();
        } catch (IOException | SecurityException exception) {
            throw new NoriException("OOPS!!! I couldn't save your tasks to disk.");
        } finally {
            deleteTemporaryFile(temporaryFile);
        }
    }

    /**
     * Loads every stored task, or returns an empty list when no storage file exists.
     *
     * @return the tasks restored from disk
     * @throws NoriException if the storage file cannot be read or contains an invalid task
     */
    public static List<Task> loadTasks() throws NoriException {
        loadingNotice = null;
        migrateLegacyStorageIfNeeded();
        if (Files.notExists(FILE_PATH)) {
            return new ArrayList<>();
        }

        try {
            return readTasks(FILE_PATH);
        } catch (IOException | SecurityException exception) {
            return recoverFromBackup();
        } catch (NoriException exception) {
            return recoverFromBackup();
        }
    }

    /**
     * Returns a startup notice generated while loading storage, if any.
     *
     * @return the startup notice, or {@code null} when loading completed normally
     */
    public static String getLoadingNotice() {
        return loadingNotice;
    }

    /**
     * Locates the project root so storage remains independent of the directory used to launch Nori.
     *
     * @return the project root, or the current directory when no project source marker is found
     */
    private static Path findProjectRoot() {
        Path currentDirectory = Path.of("").toAbsolutePath().normalize();
        for (Path candidate = currentDirectory; candidate != null; candidate = candidate.getParent()) {
            try {
                Path sourceFile = candidate.resolve("src").resolve("main").resolve("java").resolve("Nori.java");
                if (Files.isRegularFile(sourceFile)) {
                    return candidate;
                }
            } catch (SecurityException exception) {
                break;
            }
        }
        return currentDirectory;
    }

    /**
     * Returns the configured storage directory or the normal project-root data directory.
     *
     * The {@code nori.storage.dir} property exists for isolated automated test launches.
     * Normal application launches do not set it and therefore retain project-root storage.
     *
     * @return the directory that contains Nori's storage files
     */
    private static Path findStorageDirectory() {
        String configuredDirectory = System.getProperty(STORAGE_DIRECTORY_PROPERTY);
        if (configuredDirectory != null && !configuredDirectory.trim().isEmpty()) {
            return Path.of(configuredDirectory).toAbsolutePath().normalize();
        }
        return PROJECT_ROOT.resolve("data");
    }

    /**
     * Copies legacy data created from {@code src/main/java} to the canonical project-root location.
     *
     * @throws NoriException if existing legacy data cannot be copied safely
     */
    private static void migrateLegacyStorageIfNeeded() throws NoriException {
        if (hasConfiguredStorageDirectory() || Files.exists(FILE_PATH) || Files.notExists(LEGACY_FILE_PATH)) {
            return;
        }

        try {
            Files.createDirectories(FILE_PATH.getParent());
            Files.copy(LEGACY_FILE_PATH, FILE_PATH);
            if (Files.isRegularFile(LEGACY_BACKUP_FILE_PATH)) {
                Files.copy(LEGACY_BACKUP_FILE_PATH, BACKUP_FILE_PATH);
            }
            loadingNotice = "I've imported saved tasks from src/main/java/data to data/nori.txt.";
        } catch (IOException | SecurityException exception) {
            throw new NoriException("OOPS!!! I couldn't import saved tasks from src/main/java/data.");
        }
    }

    /**
     * Returns whether this launch has explicitly selected an isolated storage directory.
     *
     * @return {@code true} when the test-storage property is set to nonblank text
     */
    private static boolean hasConfiguredStorageDirectory() {
        String configuredDirectory = System.getProperty(STORAGE_DIRECTORY_PROPERTY);
        return configuredDirectory != null && !configuredDirectory.trim().isEmpty();
    }

    /**
     * Formats one task as a line in the storage file.
     *
     * @param task the task to format
     * @return the formatted task line
     * @throws NoriException if the task type is unsupported
     */
    private static String formatTask(Task task) throws NoriException {
        if (task == null) {
            throw new NoriException("OOPS!!! I couldn't save a missing task.");
        }

        String status = task.isDone() ? "1" : "0";
        if (task instanceof Todo) {
            return "T" + TASK_SEPARATOR + status + TASK_SEPARATOR + encodeField(task.description);
        }
        if (task instanceof Deadline) {
            Deadline deadline = (Deadline) task;
            return "D" + TASK_SEPARATOR + status + TASK_SEPARATOR + encodeField(task.description)
                    + TASK_SEPARATOR + encodeField(deadline.getStorageDate());
        }
        if (task instanceof Event) {
            Event event = (Event) task;
            return "E" + TASK_SEPARATOR + status + TASK_SEPARATOR + encodeField(task.description)
                    + TASK_SEPARATOR + encodeField(event.from) + TASK_SEPARATOR + encodeField(event.to);
        }
        throw new NoriException("OOPS!!! I couldn't save an unsupported task type.");
    }

    /**
     * Rebuilds one task from a line in the storage file.
     *
     * @param taskLine the stored task line
     * @return the restored task
     * @throws NoriException if the stored task line has an invalid format
     */
    private static Task parseTask(String taskLine) throws NoriException {
        String[] taskParts = taskLine.split(" \\| ", -1);
        if (taskParts.length < 3 || !isValidStatus(taskParts[1])) {
            throw new NoriException("OOPS!!! I couldn't read your saved tasks from disk.");
        }

        Task task;
        if (taskParts[0].equals("T") && taskParts.length == 3) {
            task = new Todo(decodeField(taskParts[2]));
        } else if (taskParts[0].equals("D") && taskParts.length == 4) {
            task = Deadline.fromStorage(decodeField(taskParts[2]), decodeField(taskParts[3]));
        } else if (taskParts[0].equals("E") && taskParts.length == 5) {
            task = new Event(decodeField(taskParts[2]), decodeField(taskParts[3]), decodeField(taskParts[4]));
        } else {
            throw new NoriException("OOPS!!! I couldn't read your saved tasks from disk.");
        }

        if (taskParts[1].equals("1")) {
            task.markAsDone();
        }
        if (!hasTaskDetails(task)) {
            throw new NoriException("OOPS!!! I couldn't read your saved tasks from disk.");
        }
        return task;
    }

    /**
     * Returns whether text represents a stored task completion status.
     *
     * @param status the stored completion status
     * @return {@code true} when the status is 0 or 1
     */
    private static boolean isValidStatus(String status) {
        return status.equals("0") || status.equals("1");
    }

    /**
     * Reads and parses every task from a storage file.
     *
     * @param filePath the file to read
     * @return the parsed tasks
     * @throws IOException if the file cannot be read
     * @throws NoriException if a stored task is invalid
     */
    private static List<Task> readTasks(Path filePath) throws IOException, NoriException {
        List<Task> tasks = new ArrayList<>();
        for (String taskLine : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            tasks.add(parseTask(taskLine));
        }
        return tasks;
    }

    /**
     * Restores tasks from the backup file after the main storage file cannot be read.
     *
     * @return the restored tasks
     * @throws NoriException if no valid backup can be used
     */
    private static List<Task> recoverFromBackup() throws NoriException {
        if (Files.notExists(BACKUP_FILE_PATH)) {
            canWriteToStorage = false;
            throw new NoriException("OOPS!!! Your saved tasks are corrupted and no backup is available.");
        }

        try {
            List<Task> backupTasks = readTasks(BACKUP_FILE_PATH);
            Files.copy(FILE_PATH, CORRUPT_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(BACKUP_FILE_PATH, FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
            loadingNotice = "Your saved tasks were corrupted. I've restored the backup and kept the damaged "
                    + "file as data/nori.txt.corrupt.";
            return backupTasks;
        } catch (IOException | SecurityException | NoriException exception) {
            canWriteToStorage = false;
            throw new NoriException("OOPS!!! Your saved tasks are corrupted and the backup could not be restored.");
        }
    }

    /**
     * Returns whether a restored task contains every detail required by its type.
     *
     * @param task the restored task
     * @return {@code true} if every required task field is nonempty
     */
    private static boolean hasTaskDetails(Task task) {
        if (task.description.isEmpty()) {
            return false;
        }
        if (task instanceof Deadline) {
            return true;
        }
        if (task instanceof Event) {
            Event event = (Event) task;
            return !event.from.isEmpty() && !event.to.isEmpty();
        }
        return true;
    }

    /**
     * Encodes one task field so delimiters in user-entered text do not corrupt storage.
     *
     * @param field the task field to encode
     * @return the encoded task field
     * @throws NoriException if the field is missing
     */
    private static String encodeField(String field) throws NoriException {
        if (field == null) {
            throw new NoriException("OOPS!!! I couldn't save a task with missing details.");
        }
        return FIELD_PREFIX + Base64.getEncoder().encodeToString(field.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes a task field, retaining compatibility with the original plain-text format.
     *
     * @param storedField the stored task field
     * @return the decoded task field
     * @throws NoriException if the encoded field is invalid
     */
    private static String decodeField(String storedField) throws NoriException {
        if (!storedField.startsWith(FIELD_PREFIX)) {
            return storedField;
        }

        try {
            byte[] encodedBytes = Base64.getDecoder().decode(storedField.substring(FIELD_PREFIX.length()));
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(encodedBytes))
                    .toString();
        } catch (IllegalArgumentException | CharacterCodingException exception) {
            throw new NoriException("OOPS!!! I couldn't read your saved tasks from disk.");
        }
    }

    /**
     * Replaces the storage file with a completed temporary file.
     *
     * @param temporaryFile the complete replacement file
     * @throws IOException if the replacement cannot be completed
     */
    private static void replaceStorageFile(Path temporaryFile) throws IOException {
        try {
            Files.move(temporaryFile, FILE_PATH, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Copies the latest valid storage file as the recovery backup.
     */
    private static void updateBackupFile() {
        try {
            Files.copy(FILE_PATH, BACKUP_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | SecurityException exception) {
            // A successful main save is still usable when creating its optional recovery backup fails.
        }
    }

    /**
     * Removes a leftover temporary file without masking the primary storage error.
     *
     * @param temporaryFile the temporary file to remove, if one was created
     */
    private static void deleteTemporaryFile(Path temporaryFile) {
        if (temporaryFile == null) {
            return;
        }

        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException | SecurityException exception) {
            // The completed save or its original error is more useful than temporary-file cleanup details.
        }
    }
}
