package nori.command;

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
        int fromSeparatorIndex = details.indexOf(EVENT_FROM_SEPARATOR);
        int toSeparatorIndex = details.indexOf(EVENT_TO_SEPARATOR);
        if (details.startsWith("/from ")) {
            ui.showResponse("NOOT?! An event needs a description before \"/from\"."
                    + " Try \"event team meeting /from Mon 2pm /to 4pm\".");
        } else if (fromSeparatorIndex == -1 && toSeparatorIndex == -1) {
            ui.showResponse("NOOT?! An event needs both \"/from\" and \"/to\"."
                    + " Use \"event team meeting /from Mon 2pm /to 4pm\".");
        } else if (fromSeparatorIndex == -1) {
            ui.showResponse("NOOT?! An event is missing \"/from\" and its start time."
                    + " Tell me when to start waddling.");
        } else if (toSeparatorIndex == -1) {
            ui.showResponse("NOOT?! An event is missing \"/to\" and its end time."
                    + " Even penguin meetings eventually end.");
        } else if (toSeparatorIndex < fromSeparatorIndex) {
            ui.showResponse("NOOT?! Put \"/from\" before \"/to\"."
                    + " Time waddles forward, not backward.");
        } else {
            addEvent(tasks, ui, storage, fromSeparatorIndex, toSeparatorIndex);
        }
    }

    /**
     * Validates an event's details and adds the resulting task.
     *
     * @param tasks the task list to change.
     * @param ui the console user interface.
     * @param storage the persistent task storage.
     * @param fromSeparatorIndex the position of the {@code /from} separator.
     * @param toSeparatorIndex the position of the {@code /to} separator.
     * @throws NoriException if the event cannot be parsed or saved.
     */
    private void addEvent(TaskList tasks, Ui ui, Storage storage, int fromSeparatorIndex,
            int toSeparatorIndex) throws NoriException {
        String description = details.substring(0, fromSeparatorIndex).trim();
        String from = details.substring(fromSeparatorIndex + EVENT_FROM_SEPARATOR.length(),
                toSeparatorIndex).trim();
        String to = details.substring(toSeparatorIndex + EVENT_TO_SEPARATOR.length()).trim();
        if (description.isEmpty()) {
            ui.showResponse("NOOT?! An event needs a description before \"/from\"."
                    + " Meeting whom, the invisible seals?");
        } else if (from.isEmpty()) {
            ui.showResponse("NOOT?! \"/from\" needs a start time."
                    + " I cannot waddle in from the void.");
        } else if (to.isEmpty()) {
            ui.showResponse("NOOT?! \"/to\" needs an end time."
                    + " Even penguin meetings eventually end.");
        } else {
            addTask(tasks, ui, storage, new Event(description, from, to));
        }
    }
}
