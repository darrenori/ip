package nori.command;

import nori.NoriException;
import nori.storage.Storage;
import nori.task.TaskList;
import nori.ui.Ui;

/**
 * Reports input that does not begin with a recognized command.
 */
class UnknownCommand extends Command {
    /** Creates a command that reports unrecognized input. */
    UnknownCommand() {
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        throw new NoriException("CONFUSED NOOT! My flippers do not understand that command."
                + " Try todo, deadline, event, on, list, find, mark, unmark, delete, help, or bye.");
    }
}
