package nori.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import nori.NoriException;
import nori.task.DateRange;

/**
 * Tests the parsing of command input and date arguments.
 */
public class ParserTest {

    @Test
    public void parse_supportedKeywords_returnsMatchingCommands() {
        assertInstanceOf(TodoCommand.class, Parser.parse("todo read book"));
        assertInstanceOf(DeadlineCommand.class, Parser.parse("deadline return book /by 2019-12-02"));
        assertInstanceOf(EventCommand.class, Parser.parse("event meeting /from 2pm /to 4pm"));
        assertInstanceOf(ListCommand.class, Parser.parse("list"));
        assertInstanceOf(HelpCommand.class, Parser.parse("help"));
        assertInstanceOf(OnCommand.class, Parser.parse("on 2019-10-15"));
        assertInstanceOf(MarkCommand.class, Parser.parse("mark 1"));
        assertInstanceOf(UnmarkCommand.class, Parser.parse("unmark 1"));
        assertInstanceOf(DeleteCommand.class, Parser.parse("delete 1"));
        assertInstanceOf(ExitCommand.class, Parser.parse("bye"));
    }

    @Test
    public void parse_commandWithExtraSpacing_trimsCommandDetails() {
        TodoCommand command = assertInstanceOf(TodoCommand.class, Parser.parse("todo    read book"));

        assertEquals("read book", command.details);
    }

    @Test
    public void parse_keywordPrefix_returnsUnknownCommand() {
        assertInstanceOf(UnknownCommand.class, Parser.parse("todos read book"));
    }

    @Test
    public void parse_byeWithDetails_returnsUnknownCommand() {
        assertInstanceOf(UnknownCommand.class, Parser.parse("bye now"));
    }

    @Test
    public void parse_emptyInput_returnsUnknownCommand() {
        assertInstanceOf(UnknownCommand.class, Parser.parse(""));
    }

    @Test
    public void parseDate_validDate_returnsParsedDate() throws NoriException {
        assertEquals(LocalDate.of(2019, 10, 15), Parser.parseDate("2019-10-15"));
    }

    @Test
    public void parseDate_validLeapDay_returnsParsedDate() throws NoriException {
        assertEquals(LocalDate.of(2024, 2, 29), Parser.parseDate("2024-02-29"));
    }

    @Test
    public void parseDate_emptyInput_throwsHelpfulException() {
        NoriException exception = assertThrows(NoriException.class, () -> Parser.parseDate(""));

        assertEquals("OOPS!!! \"on\" needs a date. Try \"on 2019-10-15\".", exception.getMessage());
    }

    @Test
    public void parseDate_nonIsoDate_throwsHelpfulException() {
        NoriException exception = assertThrows(NoriException.class, () -> Parser.parseDate("15-10-2019"));

        assertEquals("OOPS!!! I cannot understand \"15-10-2019\" as a date."
                + " Use a date like \"2019-10-15\".", exception.getMessage());
    }

    @Test
    public void parseDate_impossibleDate_throwsHelpfulException() {
        NoriException exception = assertThrows(NoriException.class, () -> Parser.parseDate("2023-02-29"));

        assertEquals("OOPS!!! I cannot understand \"2023-02-29\" as a date."
                + " Use a date like \"2019-10-15\".", exception.getMessage());
    }

    @Test
    public void parseListDateRange_validRange_returnsInclusiveBounds() throws NoriException {
        DateRange range = Parser.parseListDateRange("/from 2019-01-01 /to 2021-01-01");

        assertEquals(LocalDate.of(2019, 1, 1), range.getFrom());
        assertEquals(LocalDate.of(2021, 1, 1), range.getTo());
    }

    @Test
    public void parseListDateRange_sameStartAndEnd_returnsSingleDayRange() throws NoriException {
        DateRange range = Parser.parseListDateRange("/from 2024-02-29 /to 2024-02-29");

        assertEquals(LocalDate.of(2024, 2, 29), range.getFrom());
        assertEquals(LocalDate.of(2024, 2, 29), range.getTo());
    }

    @Test
    public void parseListDateRange_unexpectedDetails_throwsHelpfulException() {
        assertRangeParsingFails("unexpected details",
                "OOPS!!! Use either \"list\" or \"list /from 2019-01-01 /to 2021-01-01\".");
    }

    @Test
    public void parseListDateRange_missingToSeparator_throwsHelpfulException() {
        assertRangeParsingFails("/from 2019-01-01",
                "OOPS!!! A date-range list needs \"/to\" and an end date."
                        + " Try \"list /from 2019-01-01 /to 2021-01-01\".");
    }

    @Test
    public void parseListDateRange_missingStartDate_throwsHelpfulException() {
        assertRangeParsingFails("/from  /to 2021-01-01",
                "OOPS!!! \"/from\" needs a start date."
                        + " Try \"list /from 2019-01-01 /to 2021-01-01\".");
    }

    @Test
    public void parseListDateRange_missingEndDate_throwsHelpfulException() {
        assertRangeParsingFails("/from 2019-01-01 /to ",
                "OOPS!!! \"/to\" needs an end date."
                        + " Try \"list /from 2019-01-01 /to 2021-01-01\".");
    }

    @Test
    public void parseListDateRange_invalidStartDate_throwsHelpfulException() {
        assertRangeParsingFails("/from 2019-02-29 /to 2021-01-01",
                "OOPS!!! I cannot understand \"2019-02-29\" as the /from date."
                        + " Use a date like \"2019-10-15\".");
    }

    @Test
    public void parseListDateRange_invalidEndDate_throwsHelpfulException() {
        assertRangeParsingFails("/from 2019-01-01 /to tomorrow",
                "OOPS!!! I cannot understand \"tomorrow\" as the /to date."
                        + " Use a date like \"2019-10-15\".");
    }

    @Test
    public void parseListDateRange_endBeforeStart_throwsHelpfulException() {
        assertRangeParsingFails("/from 2021-01-02 /to 2021-01-01",
                "OOPS!!! The \"/to\" date cannot be before the \"/from\" date.");
    }

    /**
     * Verifies that parsing a date range fails with the expected user-facing message.
     *
     * @param input the date-range details to parse
     * @param expectedMessage the expected error message
     */
    private static void assertRangeParsingFails(String input, String expectedMessage) {
        NoriException exception = assertThrows(NoriException.class, () -> Parser.parseListDateRange(input));

        assertEquals(expectedMessage, exception.getMessage());
    }
}
