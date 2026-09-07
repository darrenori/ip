package nori.command;

import nori.NoriException;
import nori.storage.Storage;
import nori.task.Deadline;
import nori.task.TaskList;
import nori.ui.Ui;

/**
 * Adds a deadline task.
 */
class DeadlineCommand extends AddTaskCommand {
    /** Separates a deadline description from its due date. */
    private static final String DEADLINE_SEPARATOR = " /by ";

    /**
     * Creates a command that adds a deadline.
     *
     * @param details the description and due date after the {@code deadline} keyword.
     */
    DeadlineCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        int separatorIndex = details.indexOf(DEADLINE_SEPARATOR);
        if (details.startsWith("/by ")) {
            ui.showResponse("NOOT?! A deadline needs a description before \"/by\"."
                    + " Try \"deadline submit report /by 2019-10-15\".");
        } else if (details.endsWith("/by")) {
            ui.showResponse("NOOT?! A deadline needs a due date after \"/by\"."
                    + " Try \"deadline submit report /by 2019-10-15\".");
        } else if (separatorIndex == -1) {
            ui.showResponse("NOOT?! I cannot find the \"/by\" part of that deadline."
                    + " Use \"deadline submit report /by 2019-10-15\".");
        } else {
            addDeadline(tasks, ui, storage, separatorIndex);
        }
    }

    /**
     * Validates a deadline's details and adds the resulting task.
     *
     * @param tasks the task list to change.
     * @param ui the console user interface.
     * @param storage the persistent task storage.
     * @param separatorIndex the position of the {@code /by} separator.
     * @throws NoriException if the deadline cannot be parsed or saved.
     */
    private void addDeadline(TaskList tasks, Ui ui, Storage storage, int separatorIndex)
            throws NoriException {
        String description = details.substring(0, separatorIndex).trim();
        String deadlineInput = details.substring(separatorIndex + DEADLINE_SEPARATOR.length()).trim();
        if (description.isEmpty()) {
            ui.showResponse("NOOT?! A deadline needs a description before \"/by\"."
                    + " Even a penguin needs to know what is due.");
        } else if (deadlineInput.isEmpty()) {
            ui.showResponse("NOOT?! A deadline needs a due date after \"/by\"."
                    + " My calendar is colder than that empty space.");
        } else {
            addTask(tasks, ui, storage,
                    new Deadline(description, Deadline.parseInput(deadlineInput)));
        }
    }
}
