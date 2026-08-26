import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores and provides controlled access to Nori's tasks.
 */
public class TaskList {
    private final List<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        tasks = new ArrayList<>();
    }

    /**
     * Creates a task list containing the supplied tasks.
     *
     * @param tasks the tasks with which to initialize the list
     */
    public TaskList(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task the task to add
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Inserts a task at the specified zero-based index.
     *
     * @param index the index at which to insert the task
     * @param task the task to insert
     */
    public void add(int index, Task task) {
        tasks.add(index, task);
    }

    /**
     * Removes and returns the task at the specified zero-based index.
     *
     * @param index the index of the task to remove
     * @return the removed task
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns the task at the specified zero-based index.
     *
     * @param index the index of the task to return
     * @return the task at the specified index
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return the task count
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns whether the list contains no tasks.
     *
     * @return {@code true} if no tasks are stored
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Returns a read-only view of the stored tasks.
     *
     * @return an unmodifiable task list view
     */
    public List<Task> asUnmodifiableList() {
        return Collections.unmodifiableList(tasks);
    }
}
