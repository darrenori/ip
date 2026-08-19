/**
 * Represents a to-do task in Nori, including its description and completion state.
 */
public class Task {
    protected String description;
    protected boolean isDone;

    /**
     * Creates a task that has not yet been completed.
     *
     * @param description the task description
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /** Marks this task as completed. */
    public void markAsDone() {
        isDone = true;
    }

    /** Marks this task as not completed. */
    public void markAsNotDone() {
        isDone = false;
    }

    /**
     * Returns whether this task has been completed.
     *
     * @return {@code true} if this task is completed; otherwise, {@code false}
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Returns the marker used in task lists to show whether this task is complete.
     *
     * @return {@code "X"} when complete; otherwise, a space
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    @Override
    public String toString() {
        return "[T][" + getStatusIcon() + "] " + description;
    }
}
