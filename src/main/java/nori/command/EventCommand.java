package nori.command;

import java.util.Optional;

import nori.NoriException;
import nori.storage.Storage;
import nori.task.Event;
import nori.task.TaskList;
import nori.ui.Ui;

/**
 * Adds an event task.
 */
class EventCommand extends AddTaskCommand {
    /** Separates an event description from its start details. */
    private static final String EVENT_FROM_SEPARATOR = " /from ";
    /** Separates an event start from its end details. */
    private static final String EVENT_TO_SEPARATOR = " /to ";

    /**
     * Creates a command that adds an event.
     *
     * @param details the description, start, and end after the {@code event} keyword.
     */
    EventCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        Optional<String> formatError = findFormatError();
        if (formatError.isPresent()) {
            ui.showResponse(formatError.get());
            return;
        }
        addTask(tasks, ui, storage, new Event(getDescription(), getStartInput(), getEndInput()));
    }

    /**
     * Reports the first fault in this command's details.
     *
     * @return the correction to show the user, or empty when an event can be built.
     */
    private Optional<String> findFormatError() {
        int fromSeparatorIndex = details.indexOf(EVENT_FROM_SEPARATOR);
        int toSeparatorIndex = details.indexOf(EVENT_TO_SEPARATOR);
        if (details.startsWith("/from ")) {
            return Optional.of("NOOT?! An event needs a description before \"/from\"."
                    + " Try \"event team meeting /from Mon 2pm /to 4pm\".");
        }
        if (fromSeparatorIndex == -1 && toSeparatorIndex == -1) {
            return Optional.of("NOOT?! An event needs both \"/from\" and \"/to\"."
                    + " Use \"event team meeting /from Mon 2pm /to 4pm\".");
        }
        if (fromSeparatorIndex == -1) {
            return Optional.of("NOOT?! An event is missing \"/from\" and its start time."
                    + " Tell me when to start waddling.");
        }
        if (toSeparatorIndex == -1) {
            return Optional.of("NOOT?! An event is missing \"/to\" and its end time."
                    + " Even penguin meetings eventually end.");
        }
        if (toSeparatorIndex < fromSeparatorIndex) {
            return Optional.of("NOOT?! Put \"/from\" before \"/to\"."
                    + " Time waddles forward, not backward.");
        }
        if (getStartInput().isEmpty()) {
            return Optional.of("NOOT?! \"/from\" needs a start time."
                    + " I cannot waddle in from the void.");
        }
        return Optional.empty();
    }

    /**
     * Returns the text before the {@code /from} separator.
     *
     * @return the event description.
     */
    private String getDescription() {
        return details.substring(0, details.indexOf(EVENT_FROM_SEPARATOR)).trim();
    }

    /**
     * Returns the text between the {@code /from} and {@code /to} separators.
     *
     * @return the event start details.
     */
    private String getStartInput() {
        int fromSeparatorIndex = details.indexOf(EVENT_FROM_SEPARATOR);
        int toSeparatorIndex = details.indexOf(EVENT_TO_SEPARATOR);
        return details.substring(fromSeparatorIndex + EVENT_FROM_SEPARATOR.length(),
                toSeparatorIndex).trim();
    }

    /**
     * Returns the text after the {@code /to} separator.
     *
     * @return the event end details.
     */
    private String getEndInput() {
        int toSeparatorIndex = details.indexOf(EVENT_TO_SEPARATOR);
        return details.substring(toSeparatorIndex + EVENT_TO_SEPARATOR.length()).trim();
    }
}
