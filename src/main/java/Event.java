import java.time.LocalDate;

/**
 * Represents a task that starts and ends at specified dates or times.
 */
public class Event extends Task {
    protected String from;
    protected String to;

    /**
     * Creates an incomplete event with the given description, start, and end times.
     *
     * @param description the event description
     * @param from the start date or time, stored as entered by the user
     * @param to the end date or time, stored as entered by the user
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns whether the event's start or end details explicitly contain the given ISO-8601 date.
     *
     * @param date the date to find in this event's start or end details
     * @return {@code true} if the event explicitly includes {@code date}
     */
    public boolean occursOn(LocalDate date) {
        String dateText = date.toString();
        return from.contains(dateText) || to.contains(dateText);
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
