package nori.command;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import nori.NoriException;
import nori.task.DateRange;

/**
 * Interprets raw user input and creates the corresponding executable command.
 */
public class Parser {
    /** Introduces the start date of a date-range list. */
    private static final String LIST_FROM_PREFIX = "/from ";
    /** Separates the start and end dates of a date-range list. */
    private static final String LIST_TO_SEPARATOR = " /to ";

    /** Prevents instantiation of this stateless parser. */
    private Parser() {
    }

    /**
     * Creates an executable command from trimmed user input.
     *
     * @param input the trimmed user input
     * @return the command represented by the input
     */
    public static Command parse(String input) {
        CommandType commandType = findCommandType(input);
        if (commandType == null) {
            return Commands.createUnknown();
        }

        String details = getCommandDetails(input, commandType.getKeyword());
        return Commands.create(commandType, details);
    }

    /**
     * Parses the ISO-8601 date supplied to the {@code on} command.
     *
     * @param dateInput the date text after the {@code on} command
     * @return the parsed date
     * @throws NoriException if the date is missing or invalid
     */
    public static LocalDate parseDate(String dateInput) throws NoriException {
        if (dateInput.isEmpty()) {
            throw new NoriException("OOPS!!! \"on\" needs a date. Try \"on 2019-10-15\".");
        }
        try {
            return LocalDate.parse(dateInput);
        } catch (DateTimeParseException exception) {
            throw new NoriException("OOPS!!! I cannot understand \"" + dateInput + "\" as a date."
                    + " Use a date like \"2019-10-15\".");
        }
    }

    /**
     * Parses the inclusive date range supplied to the {@code list} command.
     *
     * @param listDetails the text after the {@code list} command
     * @return the parsed inclusive date range
     * @throws NoriException if the range format, dates, or order is invalid
     */
    public static DateRange parseListDateRange(String listDetails) throws NoriException {
        if (!listDetails.startsWith(LIST_FROM_PREFIX)) {
            throw new NoriException("OOPS!!! Use either \"list\" or"
                    + " \"list /from 2019-01-01 /to 2021-01-01\".");
        }
        int toSeparatorIndex = listDetails.indexOf(LIST_TO_SEPARATOR);
        if (toSeparatorIndex == -1) {
            throw new NoriException("OOPS!!! A date-range list needs \"/to\" and an end date."
                    + " Try \"list /from 2019-01-01 /to 2021-01-01\".");
        }

        String fromInput = listDetails.substring(LIST_FROM_PREFIX.length(), toSeparatorIndex).trim();
        String toInput = listDetails.substring(toSeparatorIndex + LIST_TO_SEPARATOR.length()).trim();
        if (fromInput.isEmpty()) {
            throw new NoriException("OOPS!!! \"/from\" needs a start date."
                    + " Try \"list /from 2019-01-01 /to 2021-01-01\".");
        }
        if (toInput.isEmpty()) {
            throw new NoriException("OOPS!!! \"/to\" needs an end date."
                    + " Try \"list /from 2019-01-01 /to 2021-01-01\".");
        }

        LocalDate fromDate = parseRangeDate(fromInput, "/from");
        LocalDate toDate = parseRangeDate(toInput, "/to");
        if (toDate.isBefore(fromDate)) {
            throw new NoriException("OOPS!!! The \"/to\" date cannot be before the \"/from\" date.");
        }
        return new DateRange(fromDate, toDate);
    }

    /**
     * Finds the command type at the start of an input line.
     *
     * @param input the trimmed user input
     * @return the matching type, or {@code null} if the input is unrecognized
     */
    private static CommandType findCommandType(String input) {
        for (CommandType commandType : CommandType.values()) {
            String keyword = commandType.getKeyword();
            boolean isExactMatch = input.equals(keyword);
            boolean isPrefixMatch = commandType != CommandType.BYE && input.startsWith(keyword + " ");
            if (isExactMatch || isPrefixMatch) {
                return commandType;
            }
        }
        return null;
    }

    /**
     * Returns the trimmed text after a command keyword.
     *
     * @param input the complete user input
     * @param commandKeyword the command keyword
     * @return the command details, without surrounding whitespace
     */
    private static String getCommandDetails(String input, String commandKeyword) {
        return input.substring(commandKeyword.length()).trim();
    }

    /**
     * Parses one date from a date-range list command.
     *
     * @param dateInput the date text to parse
     * @param rangePart the range separator introducing the date
     * @return the parsed date
     * @throws NoriException if the date is invalid
     */
    private static LocalDate parseRangeDate(String dateInput, String rangePart) throws NoriException {
        try {
            return LocalDate.parse(dateInput);
        } catch (DateTimeParseException exception) {
            throw new NoriException("OOPS!!! I cannot understand \"" + dateInput + "\" as the "
                    + rangePart + " date. Use a date like \"2019-10-15\".");
        }
    }
}
