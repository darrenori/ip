package nori.task;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nori.NoriException;

/**
 * Represents a task that starts and ends at specified dates or times.
 */
public class Event extends Task {
    /** Finds date-like text in an event detail, ignoring digits that are part of a longer run. */
    private static final Pattern DATE_PATTERN =
            Pattern.compile("(?<![0-9])\\d{4}-\\d{1,2}-\\d{1,2}(?![0-9])");

    /** Start details, kept exactly as the user typed them. */
    private final String from;
    /** End details, kept exactly as the user typed them. */
    private final String to;

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
     * Returns the event's start details as entered by the user.
     *
     * @return the event start details
     */
    public String getFrom() {
        return from;
    }

    /**
     * Returns the event's end details as entered by the user.
     *
     * @return the event end details
     */
    public String getTo() {
        return to;
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
     * Returns whether this event overlaps the inclusive date range.
     *
     * An event with dates in both its start and end details is treated as a date interval.
     * An event with a date in only one detail is treated as occurring on that date.
     *
     * @param rangeStart the earliest date in the range
     * @param rangeEnd the latest date in the range
     * @return {@code true} if this event occurs in the range
     */
    public boolean occursInDateRange(LocalDate rangeStart, LocalDate rangeEnd) {
        LocalDate eventStart = findDate(from);
        LocalDate eventEnd = findDate(to);
        if (eventStart != null && eventEnd != null) {
            return !eventStart.isAfter(rangeEnd) && !eventEnd.isBefore(rangeStart);
        }
        if (eventStart != null) {
            return !eventStart.isBefore(rangeStart) && !eventStart.isAfter(rangeEnd);
        }
        if (eventEnd != null) {
            return !eventEnd.isBefore(rangeStart) && !eventEnd.isAfter(rangeEnd);
        }
        return false;
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

    /**
     * Returns the first ISO-8601 date in an event detail, if any.
     *
     * @param eventDetail the start or end detail to inspect
     * @return the first valid date, or {@code null} when the detail has no date
     */
    private static LocalDate findDate(String eventDetail) {
        if (eventDetail == null) {
            return null;
        }

        Matcher matcher = DATE_PATTERN.matcher(eventDetail);
        if (!matcher.find()) {
            return null;
        }
        try {
            return LocalDate.parse(matcher.group());
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    /**
     * Returns this event with its {@code [E]} type icon and its start and end details.
     *
     * @return this event rendered as {@code [E][<status>] <description> (from: <from> to: <to>)}
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
