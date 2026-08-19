/**
 * Represents a task that must be completed by a specified date or time.
 */
public class Deadline extends Task {
    protected String by;

    /**
     * Creates an incomplete deadline with the given description and due time.
     *
     * @param description the deadline description
     * @param by the due date or time, stored as entered by the user
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
