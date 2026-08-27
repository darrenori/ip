package nori.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import nori.NoriException;

/**
 * Tests the parsing of command input and date arguments.
 */
public class ParserTest {

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
}
