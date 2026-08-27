package nori.task;

import java.time.LocalDate;

/**
 * Represents an inclusive date range for a task query.
 */
public class DateRange {
    /** Inclusive first date of the range. */
    private final LocalDate from;
    /** Inclusive last date of the range. */
    private final LocalDate to;

    /**
     * Creates an inclusive range between two dates.
     *
     * @param from the first date in the range
     * @param to the last date in the range
     */
    public DateRange(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the first date in the range.
     *
     * @return the inclusive lower bound
     */
    public LocalDate getFrom() {
        return from;
    }

    /**
     * Returns the last date in the range.
     *
     * @return the inclusive upper bound
     */
    public LocalDate getTo() {
        return to;
    }
}
