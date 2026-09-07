package nori.command;

import java.util.Optional;

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
        Optional<String> formatError = findFormatError();
        if (formatError.isPresent()) {
            ui.showResponse(formatError.get());
            return;
        }
        addTask(tasks, ui, storage,
                new Deadline(getDescription(), Deadline.parseInput(getDueDateInput())));
    }

    /**
     * Reports the first fault in this command's details.
     *
     * @return the correction to show the user, or empty when a deadline can be built.
     */
    private Optional<String> findFormatError() {
        if (details.startsWith("/by ")) {
            return Optional.of("NOOT?! A deadline needs a description before \"/by\"."
                    + " Try \"deadline submit report /by 2019-10-15\".");
        }
        if (details.endsWith("/by")) {
            return Optional.of("NOOT?! A deadline needs a due date after \"/by\"."
                    + " Try \"deadline submit report /by 2019-10-15\".");
        }
        if (!details.contains(DEADLINE_SEPARATOR)) {
            return Optional.of("NOOT?! I cannot find the \"/by\" part of that deadline."
                    + " Use \"deadline submit report /by 2019-10-15\".");
        }
        return Optional.empty();
    }

    /**
     * Returns the text before the {@code /by} separator.
     *
     * @return the deadline description.
     */
    private String getDescription() {
        return details.substring(0, details.indexOf(DEADLINE_SEPARATOR)).trim();
    }

    /**
     * Returns the text after the {@code /by} separator.
     *
     * @return the unparsed due date.
     */
    private String getDueDateInput() {
        int separatorIndex = details.indexOf(DEADLINE_SEPARATOR);
        return details.substring(separatorIndex + DEADLINE_SEPARATOR.length()).trim();
    }
}
