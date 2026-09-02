package nori;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import nori.ui.GuiUi;

/**
 * Tests the command-response boundary used by Nori's graphical interface.
 */
public class NoriGuiTest {
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
    public void executeCommand_addTodo_returnsResponseWithoutExiting() {
        GuiUi guiUi = new GuiUi();
        Nori nori = new Nori(guiUi);

        boolean isExitRequested = nori.executeCommand("todo finish tutorial");

        assertFalse(isExitRequested);
        assertEquals("Got it. I've added this task:\n"
                + "  [T][ ] finish tutorial\n"
                + "Now you have 1 tasks in the list.", guiUi.consumeResponse());
    }

    @Test
    public void executeCommand_bye_returnsGoodbyeAndRequestsExit() {
        GuiUi guiUi = new GuiUi();
        Nori nori = new Nori(guiUi);

        boolean isExitRequested = nori.executeCommand("bye");

        assertTrue(isExitRequested);
        assertEquals("Bye. Hope to see you again soon!", guiUi.consumeResponse());
    }

    @Test
    public void executeCommand_multiDayEventQuery_returnsEventOnMiddleDate() {
        GuiUi guiUi = new GuiUi();
        Nori nori = new Nori(guiUi);
        nori.executeCommand("event conference /from 2026-09-01 /to 2026-09-03");
        guiUi.consumeResponse();

        boolean isExitRequested = nori.executeCommand("on 2026-09-02");

        assertFalse(isExitRequested);
        assertEquals("Here are the deadlines and events on 2026-09-02:\n"
                + "1.[E][ ] conference (from: 2026-09-01 to: 2026-09-03)", guiUi.consumeResponse());
    }

    @Test
    public void executeCommand_reversedEventDates_rejectsEventWithoutSaving() {
        GuiUi guiUi = new GuiUi();
        Nori nori = new Nori(guiUi);

        boolean isExitRequested = nori.executeCommand("event backwards /from 2026-09-03 /to 2026-09-01");

        assertFalse(isExitRequested);
        assertEquals("OOPS!!! An event cannot end before it starts.", guiUi.consumeResponse());
        nori.executeCommand("list");
        assertEquals("Your list is empty. Add something with \"todo borrow book\" lah.",
                guiUi.consumeResponse());
    }

    @Test
    public void executeCommand_hostileLookingDescription_preservesLiteralTextAfterReload() {
        String description = "<script>alert('x')</script> | ../data & command";
        GuiUi firstGuiUi = new GuiUi();
        Nori firstNori = new Nori(firstGuiUi);
        firstNori.executeCommand("todo " + description);
        firstGuiUi.consumeResponse();

        GuiUi reloadedGuiUi = new GuiUi();
        Nori reloadedNori = new Nori(reloadedGuiUi);
        reloadedNori.executeCommand("list");

        assertEquals("Here are the tasks in your list:\n1.[T][ ] " + description,
                reloadedGuiUi.consumeResponse());
    }

    @Test
    public void executeCommand_oversizedTaskNumber_returnsRangeErrorWithoutCrashing() {
        GuiUi guiUi = new GuiUi();
        Nori nori = new Nori(guiUi);
        nori.executeCommand("todo safe task");
        guiUi.consumeResponse();

        boolean isExitRequested = nori.executeCommand("delete 999999999999999999999999999999999999");

        assertFalse(isExitRequested);
        assertEquals("ARE YOU DONEEEE????? \"999999999999999999999999999999999999\""
                + " is far too large for a task number. Use a whole number from 1 to 1.",
                guiUi.consumeResponse());
    }
}
