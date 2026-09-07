package nori.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import nori.Nori;
import nori.ui.GuiUi;

/**
 * Tests the fault each malformed deadline and event command is answered with.
 */
public class DatedTaskInputTest {
    private static final String STORAGE_DIRECTORY_PROPERTY = "nori.storage.dir";

    @TempDir
    private Path temporaryDirectory;
    private String previousStorageDirectory;

    @BeforeEach
    public void redirectStorage() {
        previousStorageDirectory = System.getProperty(STORAGE_DIRECTORY_PROPERTY);
        System.setProperty(STORAGE_DIRECTORY_PROPERTY, temporaryDirectory.toString());
    }

    @AfterEach
    public void restoreStorage() {
        if (previousStorageDirectory == null) {
            System.clearProperty(STORAGE_DIRECTORY_PROPERTY);
        } else {
            System.setProperty(STORAGE_DIRECTORY_PROPERTY, previousStorageDirectory);
        }
    }

    @Test
    public void execute_deadlineWithoutDescription_reportsMissingDescription() {
        assertResponse("NOOT?! A deadline needs a description before \"/by\"."
                        + " Try \"deadline submit report /by 2019-10-15\".",
                "deadline /by 2019-10-15");
    }

    @Test
    public void execute_deadlineEndingAtSeparator_reportsMissingDueDate() {
        assertResponse("NOOT?! A deadline needs a due date after \"/by\"."
                        + " Try \"deadline submit report /by 2019-10-15\".",
                "deadline submit report /by");
    }

    @Test
    public void execute_deadlineWithoutSeparator_reportsMissingSeparator() {
        assertResponse("NOOT?! I cannot find the \"/by\" part of that deadline."
                        + " Use \"deadline submit report /by 2019-10-15\".",
                "deadline submit report");
    }

    @Test
    public void execute_deadlineWithWhitespaceDescription_reportsMissingDescription() {
        assertResponse("NOOT?! A deadline needs a description before \"/by\"."
                        + " Try \"deadline submit report /by 2019-10-15\".",
                "deadline    /by 2019-10-15");
    }

    @Test
    public void execute_deadlineWithWhitespaceDueDate_reportsMissingDueDate() {
        assertResponse("NOOT?! A deadline needs a due date after \"/by\"."
                        + " Try \"deadline submit report /by 2019-10-15\".",
                "deadline submit report /by    ");
    }

    @Test
    public void execute_deadlineWithUnparsableDate_reportsUnreadableDate() {
        assertResponse("NOOT?! I cannot understand \"tomorrow\" as a deadline."
                        + " Use a date like \"2019-10-15\".",
                "deadline submit report /by tomorrow");
    }

    @Test
    public void execute_eventWithoutDescription_reportsMissingDescription() {
        assertResponse("NOOT?! An event needs a description before \"/from\"."
                        + " Try \"event team meeting /from Mon 2pm /to 4pm\".",
                "event /from Mon 2pm /to 4pm");
    }

    @Test
    public void execute_eventWithoutEitherSeparator_reportsBothMissing() {
        assertResponse("NOOT?! An event needs both \"/from\" and \"/to\"."
                        + " Use \"event team meeting /from Mon 2pm /to 4pm\".",
                "event team meeting");
    }

    @Test
    public void execute_eventWithoutStartSeparator_reportsMissingStart() {
        assertResponse("NOOT?! An event is missing \"/from\" and its start time."
                        + " Tell me when to start waddling.",
                "event team meeting /to 4pm");
    }

    @Test
    public void execute_eventWithoutEndSeparator_reportsMissingEnd() {
        assertResponse("NOOT?! An event is missing \"/to\" and its end time."
                        + " Even penguin meetings eventually end.",
                "event team meeting /from Mon 2pm");
    }

    @Test
    public void execute_eventWithSeparatorsReversed_reportsWrongOrder() {
        assertResponse("NOOT?! Put \"/from\" before \"/to\"."
                        + " Time waddles forward, not backward.",
                "event team meeting /to 4pm /from Mon 2pm");
    }

    @Test
    public void execute_eventWithWhitespaceDescription_reportsMissingDescription() {
        assertResponse("NOOT?! An event needs a description before \"/from\"."
                        + " Try \"event team meeting /from Mon 2pm /to 4pm\".",
                "event    /from Mon 2pm /to 4pm");
    }

    @Test
    public void execute_eventWithBlankStart_reportsEmptyStart() {
        assertResponse("NOOT?! \"/from\" needs a start time."
                        + " I cannot waddle in from the void.",
                "event team meeting /from   /to 4pm");
    }

    @Test
    public void execute_eventWithWhitespaceEnd_reportsMissingEnd() {
        assertResponse("NOOT?! An event is missing \"/to\" and its end time."
                        + " Even penguin meetings eventually end.",
                "event team meeting /from Mon 2pm /to    ");
    }

    @Test
    public void execute_validDeadline_addsTheTask() {
        assertResponse("Noot noot! Task tucked safely under my wing:\n"
                        + "  [D][ ] submit report (by: Oct 15 2019)\n"
                        + "The iceberg now holds 1 task(s).",
                "deadline submit report /by 2019-10-15");
    }

    @Test
    public void execute_validEvent_addsTheTask() {
        assertResponse("Noot noot! Task tucked safely under my wing:\n"
                        + "  [E][ ] team meeting (from: Mon 2pm to: 4pm)\n"
                        + "The iceberg now holds 1 task(s).",
                "event team meeting /from Mon 2pm /to 4pm");
    }

    /**
     * Runs one command in a fresh session and checks the whole response.
     *
     * @param expectedResponse the response the command should produce.
     * @param input the command to run.
     */
    private void assertResponse(String expectedResponse, String input) {
        GuiUi guiUi = new GuiUi();
        Nori nori = new Nori(guiUi);

        nori.executeCommand(input);

        assertEquals(expectedResponse, guiUi.consumeResponse());
    }
}
