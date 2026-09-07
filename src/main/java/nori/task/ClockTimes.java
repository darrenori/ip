package nori.task;

import java.time.LocalTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the clock time a user wrote inside free-form task details.
 */
final class ClockTimes {
    /** Hours in half a day, which is what separates a morning hour from its afternoon reading. */
    private static final int HOURS_IN_HALF_DAY = 12;
    /**
     * Matches the first clock time in a detail, in any of the forms Nori accepts.
     *
     * The three alternatives are a separated time such as {@code 9:30}, a
     * compact 24-hour time such as {@code 0930}, and a whole hour that a
     * meridiem gives meaning to, such as {@code 2pm}. Digits either side of a
     * match rule it out, so a longer run of digits is never read as a time.
     */
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(?<![0-9])(?:"
                    + "(?<hour>[01]?[0-9]|2[0-3])[:.](?<minute>[0-5][0-9])\\s*(?<meridiem>[ap]\\.?m\\.?)?"
                    + "|(?<compactHour>[01][0-9]|2[0-3])(?<compactMinute>[0-5][0-9])"
                    + "|(?<wholeHour>1[0-2]|0?[1-9])\\s*(?<wholeMeridiem>[ap]\\.?m\\.?)"
                    + ")(?![0-9])",
            Pattern.CASE_INSENSITIVE);

    /** Prevents instantiation of this stateless reader. */
    private ClockTimes() {
    }

    /**
     * Returns the first clock time written in some task details.
     *
     * Every ISO-8601 date must already have been removed from {@code details},
     * because a year's four digits would otherwise read as a 24-hour time.
     *
     * A time may be written as {@code 0930}, {@code 9:30}, {@code 9.30},
     * {@code 2pm}, {@code 2:30 PM} or {@code 2 p.m.}. A meridiem is ignored
     * after an hour past noon, where it cannot say anything the hour has not
     * said already.
     *
     * @param details the task details to read, with every date removed.
     * @return the first time the details give, or empty when they give none.
     */
    static Optional<LocalTime> findFirstIn(String details) {
        Matcher matcher = TIME_PATTERN.matcher(details);
        if (!matcher.find()) {
            return Optional.empty();
        }

        if (matcher.group("compactHour") != null) {
            return Optional.of(LocalTime.of(Integer.parseInt(matcher.group("compactHour")),
                    Integer.parseInt(matcher.group("compactMinute"))));
        }
        if (matcher.group("wholeHour") != null) {
            int hourOfDay = toHourOfDay(Integer.parseInt(matcher.group("wholeHour")),
                    matcher.group("wholeMeridiem"));
            return Optional.of(LocalTime.of(hourOfDay, 0));
        }
        int hourOfDay = toHourOfDay(Integer.parseInt(matcher.group("hour")), matcher.group("meridiem"));
        return Optional.of(LocalTime.of(hourOfDay, Integer.parseInt(matcher.group("minute"))));
    }

    /**
     * Converts an hour as a user wrote it into an hour of the day.
     *
     * @param hour the hour as written.
     * @param meridiem the am or pm that followed it, or {@code null} when none did.
     * @return the hour of the day, from 0 to 23.
     */
    private static int toHourOfDay(int hour, String meridiem) {
        if (meridiem == null || hour > HOURS_IN_HALF_DAY) {
            return hour;
        }

        boolean isMorning = meridiem.toLowerCase(Locale.ROOT).startsWith("a");
        if (isMorning) {
            return hour == HOURS_IN_HALF_DAY ? 0 : hour;
        }
        return hour == HOURS_IN_HALF_DAY ? HOURS_IN_HALF_DAY : hour + HOURS_IN_HALF_DAY;
    }
}
