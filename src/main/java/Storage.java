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
}
