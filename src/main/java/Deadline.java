import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Represents a task that must be completed by a specified date or time.
 */
public class Deadline extends Task {
    private static final DateTimeFormatter INPUT_FORMATTER =
            DateTimeFormatter.ofPattern("d/M/uuuu HHmm", Locale.ENGLISH)
                    .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d uuuu h:mm a", Locale.ENGLISH);

    private final LocalDateTime by;

    /**
     * Creates an incomplete deadline with the given description and due time.
     *
     * @param description the deadline description
     * @param by the due date and time
     */
    public Deadline(String description, LocalDateTime by) {
        super(description);
        this.by = by;
    }

    /**
     * Parses a deadline date and time entered in Nori's command format.
     *
     * @param input the date and time in {@code d/M/yyyy HHmm} format
     * @return the parsed date and time
     * @throws NoriException if the input is not a valid date and time
     */
    public static LocalDateTime parseInput(String input) throws NoriException {
        try {
            return LocalDateTime.parse(input, INPUT_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new NoriException("OOPS!!! I cannot understand \"" + input + "\" as a deadline."
                    + " Use a date and time like \"2/12/2019 1800\".");
        }
    }

    /**
     * Rebuilds a deadline from its ISO-8601 storage representation.
     *
     * @param description the deadline description
     * @param storedDateTime the ISO-8601 date-time string from storage
     * @return the reconstructed deadline
     * @throws NoriException if the stored date-time is invalid
     */
    public static Deadline fromStorage(String description, String storedDateTime) throws NoriException {
        try {
            return new Deadline(description, LocalDateTime.parse(storedDateTime));
        } catch (DateTimeParseException exception) {
            throw new NoriException("OOPS!!! I couldn't read your saved tasks from disk.");
        }
    }

    /**
     * Returns this deadline's ISO-8601 date-time representation for storage.
     *
     * @return the ISO-8601 due date and time
     */
    public String getStorageDateTime() {
        return by.toString();
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by.format(DISPLAY_FORMATTER) + ")";
    }
}
