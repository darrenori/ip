/**
 * Represents a to-do task in Nori, including its description and completion state.
 */
public class Task {
    private static final String TODO_TYPE = "T";
    private static final String DEADLINE_TYPE = "D";

    protected String description;
    protected boolean isDone;
    protected String type;
    protected String by;

    /**
     * Creates a task that has not yet been completed.
     *
     * @param description the task description
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
        this.type = TODO_TYPE;
        this.by = null;
    }

    /**
     * Creates an incomplete deadline with the given description and due time.
     *
     * @param description the deadline description
     * @param by the due date or time, stored as entered by the user
     */
    public Task(String description, String by) {
        this.description = description;
        this.isDone = false;
        this.type = DEADLINE_TYPE;
        this.by = by;
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
        String deadlineDetails = by == null ? "" : " (by: " + by + ")";
        return "[" + type + "][" + getStatusIcon() + "] " + description + deadlineDetails;
    }
}
