package nori.task;

/**
 * Represents the common state and behaviour shared by every task type in Nori.
 */
public abstract class Task {
    /** What the task is, as the user described it. */
    private final String description;
    /** Whether the task has been completed. */
    private boolean isDone;

    /**
     * Creates a task that has not yet been completed.
     *
     * @param description the task description.
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
     * @return {@code true} if this task is completed; otherwise, {@code false}.
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Returns this task's description.
     *
     * @return the task description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns whether another task records the same thing as this one.
     *
     * Completion is deliberately left out of the comparison, because a
     * finished "read book" and an unfinished one are one task a user has
     * entered twice, not two tasks. Descriptions are compared exactly rather
     * than ignoring case, so a user who deliberately keeps "Draft" and "draft"
     * apart is allowed to.
     *
     * @param other the task to compare this one with.
     * @return {@code true} when both are of one type and describe the same thing.
     */
    public boolean isSameTask(Task other) {
        return other != null && other.getClass() == getClass()
                && other.getDescription().equals(description);
    }

    /**
     * Returns the marker used in task lists to show whether this task is complete.
     *
     * @return the single character {@code "X"} when this task is done, or
     *         {@code " "} when it is not.
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /**
     * Returns this task's status icon and description, as shown in a task list.
     *
     * @return the status icon in square brackets, a space, then the
     *         description, for example {@code [X] read book}.
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
