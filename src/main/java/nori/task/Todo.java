package nori.task;

/**
 * Represents a task with no associated date or time.
 */
public class Todo extends Task {

    /**
     * Creates an incomplete to-do with the given description.
     *
     * @param description the to-do description
     */
    public Todo(String description) {
        super(description);
    }

    /**
     * Returns this to-do prefixed with its {@code [T]} type icon.
     *
     * @return this to-do rendered as {@code [T][<status>] <description>}
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
