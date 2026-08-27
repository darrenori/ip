package nori.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import nori.NoriException;

/**
 * Represents a task that must be completed by a specified date.
 */
public class Deadline extends Task {
    /** Renders the due date for display, as in {@code Feb 29 2024}. */
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd uuuu", Locale.ENGLISH);

    /** The date this deadline is due on. */
    private final LocalDate by;

    /**
     * Creates an incomplete deadline with the given description and due date.
     *
     * @param description the deadline description
     * @param by the due date
     */
    public Deadline(String description, LocalDate by) {
        super(description);
        this.by = by;
    }

    /**
     * Parses a deadline date entered in Nori's command format.
     *
     * @param input the date in {@code yyyy-MM-dd} format
     * @return the parsed date
     * @throws NoriException if the input is not a valid date
     */
    public static LocalDate parseInput(String input) throws NoriException {
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException exception) {
            throw new NoriException("OOPS!!! I cannot understand \"" + input + "\" as a deadline."
                    + " Use a date like \"2019-10-15\".");
        }
    }

    /**
     * Rebuilds a deadline from its ISO-8601 date storage representation.
     *
     * @param description the deadline description
     * @param storedDate the ISO-8601 date string from storage
     * @return the reconstructed deadline
     * @throws NoriException if the stored date is invalid
     */
    public static Deadline fromStorage(String description, String storedDate) throws NoriException {
        try {
            return new Deadline(description, LocalDate.parse(storedDate));
        } catch (DateTimeParseException exception) {
            throw new NoriException("OOPS!!! I couldn't read your saved tasks from disk.");
        }
    }

    /**
     * Returns this deadline's ISO-8601 date representation for storage.
     *
     * @return the ISO-8601 due date
     */
    public String getStorageDate() {
        return by.toString();
    }

    /**
     * Returns whether this deadline is due on the given date.
     *
     * @param date the date to compare with this deadline
     * @return {@code true} if this deadline is due on {@code date}
     */
    public boolean occursOn(LocalDate date) {
        return by.equals(date);
    }

    /**
     * Returns whether this deadline is due in the inclusive date range.
     *
     * @param from the earliest date in the range
     * @param to the latest date in the range
     * @return {@code true} if this deadline is due in the range
     */
    public boolean occursInDateRange(LocalDate from, LocalDate to) {
        return !by.isBefore(from) && !by.isAfter(to);
    }

    /**
     * Returns this deadline with its {@code [D]} type icon and readable due date.
     *
     * @return this deadline rendered as {@code [D][<status>] <description> (by: MMM dd uuuu)}
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by.format(DISPLAY_FORMATTER) + ")";
    }
}
