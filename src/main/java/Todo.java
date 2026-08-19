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

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
