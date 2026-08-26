import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores Nori's tasks on the local disk.
 */
public class Storage {
    private static final Path FILE_PATH = Path.of("data", "nori.txt");

    /**
     * Saves every task to the storage file, replacing its previous contents.
     *
     * @param tasks the tasks to save
     * @throws NoriException if the tasks cannot be written to disk
     */
    public static void saveTasks(List<Task> tasks) throws NoriException {
        List<String> taskLines = new ArrayList<>();
        for (Task task : tasks) {
            taskLines.add(formatTask(task));
        }

        try {
            Files.createDirectories(FILE_PATH.getParent());
            Files.write(FILE_PATH, taskLines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new NoriException("OOPS!!! I couldn't save your tasks to disk.");
        }
    }

    /**
     * Loads every stored task, or returns an empty list when no storage file exists.
     *
     * @return the tasks restored from disk
     * @throws NoriException if the storage file cannot be read or contains an invalid task
     */
    public static List<Task> loadTasks() throws NoriException {
        if (Files.notExists(FILE_PATH)) {
            return new ArrayList<>();
        }

        try {
            List<Task> tasks = new ArrayList<>();
            for (String taskLine : Files.readAllLines(FILE_PATH, StandardCharsets.UTF_8)) {
                tasks.add(parseTask(taskLine));
            }
            return tasks;
        } catch (IOException exception) {
            throw new NoriException("OOPS!!! I couldn't read your saved tasks from disk.");
        }
    }

    /**
     * Formats one task as a line in the storage file.
     *
     * @param task the task to format
     * @return the formatted task line
     * @throws NoriException if the task type is unsupported
     */
    private static String formatTask(Task task) throws NoriException {
        String status = task.isDone() ? "1" : "0";
        if (task instanceof Todo) {
            return "T | " + status + " | " + task.description;
        }
        if (task instanceof Deadline) {
            Deadline deadline = (Deadline) task;
            return "D | " + status + " | " + task.description + " | " + deadline.by;
        }
        if (task instanceof Event) {
            Event event = (Event) task;
            return "E | " + status + " | " + task.description + " | " + event.from + " | " + event.to;
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
            task = new Todo(taskParts[2]);
        } else if (taskParts[0].equals("D") && taskParts.length == 4) {
            task = new Deadline(taskParts[2], taskParts[3]);
        } else if (taskParts[0].equals("E") && taskParts.length == 5) {
            task = new Event(taskParts[2], taskParts[3], taskParts[4]);
        } else {
            throw new NoriException("OOPS!!! I couldn't read your saved tasks from disk.");
        }

        if (taskParts[1].equals("1")) {
            task.markAsDone();
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
}
