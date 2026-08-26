import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a task that starts and ends at specified dates or times.
 */
public class Event extends Task {
    private static final Pattern DATE_PATTERN =
            Pattern.compile("(?<![0-9])\\d{4}-\\d{1,2}-\\d{1,2}(?![0-9])");

    protected String from;
    protected String to;

    /**
     * Creates an incomplete event with the given description, start, and end times.
     *
     * @param description the event description
     * @param from the start date or time, stored as entered by the user
     * @param to the end date or time, stored as entered by the user
     * @throws NoriException if a date in the start or end details is invalid
     */
    public Event(String description, String from, String to) throws NoriException {
        super(description);
        validateDates(from);
        validateDates(to);
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
        return containsDate(from, date) || containsDate(to, date);
    }

    /**
     * Validates every date-like value in an event detail.
     *
     * @param eventDetail the start or end detail to validate
     * @throws NoriException if a date-like value is not a valid ISO-8601 date
     */
    private static void validateDates(String eventDetail) throws NoriException {
        if (eventDetail == null) {
            throw new NoriException("OOPS!!! An event cannot have a missing date or time.");
        }

        Matcher matcher = DATE_PATTERN.matcher(eventDetail);
        while (matcher.find()) {
            String dateText = matcher.group();
            try {
                LocalDate.parse(dateText);
            } catch (DateTimeParseException exception) {
                throw new NoriException("OOPS!!! I cannot understand \"" + dateText + "\" as an event date."
                        + " Use a date like \"2019-10-15\".");
            }
        }
    }

    /**
     * Returns whether an event detail contains the requested valid ISO-8601 date.
     *
     * @param eventDetail the start or end detail to inspect
     * @param date the date to find
     * @return {@code true} if the detail contains {@code date}
     */
    private static boolean containsDate(String eventDetail, LocalDate date) {
        if (eventDetail == null) {
            return false;
        }

        Matcher matcher = DATE_PATTERN.matcher(eventDetail);
        while (matcher.find()) {
            try {
                if (LocalDate.parse(matcher.group()).equals(date)) {
                    return true;
                }
            } catch (DateTimeParseException exception) {
                // Invalid event dates are rejected when events are created or restored.
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
