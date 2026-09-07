package nori.command;

import nori.NoriException;
import nori.storage.Storage;
import nori.task.Deadline;
import nori.task.Event;
import nori.task.Task;
import nori.task.TaskList;
import nori.task.Todo;
import nori.ui.Ui;

/**
 * Creates concrete commands from their recognized types and details.
 */
public final class Commands {
    /** Prevents instantiation of this command factory. */
    private Commands() {
    }

    /**
     * Creates the command represented by a recognized type.
     *
     * @param commandType the recognized command type.
     * @param details the text after the command keyword.
     * @return the corresponding executable command.
     */
    public static Command create(CommandType commandType, String details) {
        assert commandType != null : "Unrecognised input is turned into a command by createUnknown instead.";
        assert details != null : "The parser always supplies the text after the keyword, empty at the least.";

        switch (commandType) {
            case TODO:
                return new TodoCommand(details);
            case DEADLINE:
                return new DeadlineCommand(details);
            case EVENT:
                return new EventCommand(details);
            case LIST:
                return new ListCommand(details);
            case HELP:
                return new HelpCommand();
            case ON:
                return new OnCommand(details);
            case FIND:
                return new FindCommand(details);
            case MARK:
                return new MarkCommand(details);
            case UNMARK:
                return new UnmarkCommand(details);
            case DELETE:
                return new DeleteCommand(details);
            case BYE:
                return new ExitCommand();
            default:
                assert false : "Every command type needs a command, but " + commandType + " has none.";
                return new UnknownCommand();
        }
    }

    /**
     * Creates a command that reports unrecognized input.
     *
     * @return an unrecognized-command handler.
     */
    public static Command createUnknown() {
        return new UnknownCommand();
    }
}

/**
 * Provides common input storage for commands entered with trailing details.
 */
abstract class InputCommand extends Command {
    /** The trimmed text the user typed after the command keyword. */
    protected final String details;

    /**
     * Creates a command with its input details.
     *
     * @param details the text after the command keyword.
     */
    InputCommand(String details) {
        this.details = details;
    }
}

/**
 * Displays the complete task list or tasks in a date range.
 */
class ListCommand extends InputCommand {
    /**
     * Creates a list command.
     *
     * @param details the optional date range after the {@code list} keyword.
     */
    ListCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        if (details.isEmpty()) {
            ui.showResponse(tasks.getDisplayLines());
            return;
        }
        ui.showResponse(tasks.getTasksInDateRangeDisplayLines(Parser.parseListDateRange(details)));
    }
}

/**
 * Displays Nori's supported commands.
 */
class HelpCommand extends Command {
    /** Creates a help command. */
    HelpCommand() {
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showHelp();
    }
}

/**
 * Displays tasks that occur on a specified date.
 */
class OnCommand extends InputCommand {
    /**
     * Creates a date-query command.
     *
     * @param details the date after the {@code on} keyword.
     */
    OnCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        ui.showResponse(tasks.getTasksOnDateDisplayLines(Parser.parseDate(details)));
    }
}

/**
 * Displays tasks whose descriptions contain a keyword.
 */
class FindCommand extends InputCommand {
    /**
     * Creates a keyword-search command.
     *
     * @param details the keyword after the {@code find} keyword.
     */
    FindCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        if (details.isEmpty()) {
            ui.showResponse("NOOT?! \"find\" needs a keyword."
                    + " Try \"find book\"; even penguins need a clue.");
            return;
        }
        ui.showResponse(tasks.getTasksMatchingKeywordDisplayLines(details));
    }
}

/**
 * Marks a task as complete.
 */
class MarkCommand extends InputCommand {
    /**
     * Creates a command that completes a task.
     *
     * @param details the task number after the {@code mark} keyword.
     */
    MarkCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        int taskIndex = tasks.getTaskIndex(details, "mark");
        Task task = tasks.get(taskIndex);
        if (task.isDone()) {
            ui.showResponse("Noot noot! That task is already frozen solid (done).");
            return;
        }

        task.markAsDone();
        try {
            storage.saveTasks(tasks.asUnmodifiableList());
        } catch (NoriException exception) {
            task.markAsNotDone();
            throw exception;
        }
        ui.showResponse("Noot noot! This task is now ice-cold complete:", "  " + task);
    }
}

/**
 * Marks a task as incomplete.
 */
class UnmarkCommand extends InputCommand {
    /**
     * Creates a command that reopens a task.
     *
     * @param details the task number after the {@code unmark} keyword.
     */
    UnmarkCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        int taskIndex = tasks.getTaskIndex(details, "unmark");
        Task task = tasks.get(taskIndex);
        if (!task.isDone()) {
            ui.showResponse("Noot noot! That task is already thawed (not done).");
            return;
        }

        task.markAsNotDone();
        try {
            storage.saveTasks(tasks.asUnmodifiableList());
        } catch (NoriException exception) {
            task.markAsDone();
            throw exception;
        }
        ui.showResponse("Brrr... thawing this task back out:", "  " + task);
    }
}

/**
 * Removes a task from the list.
 */
class DeleteCommand extends InputCommand {
    /**
     * Creates a command that removes a task.
     *
     * @param details the task number after the {@code delete} keyword.
     */
    DeleteCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        int taskIndex = tasks.getTaskIndex(details, "delete");
        Task deletedTask = tasks.remove(taskIndex);
        assert taskIndex <= tasks.size()
                : "Removing at taskIndex leaves that index insertable, which a failed save relies on.";
        try {
            storage.saveTasks(tasks.asUnmodifiableList());
        } catch (NoriException exception) {
            tasks.add(taskIndex, deletedTask);
            throw exception;
        }
        ui.showResponse("Splash! I kicked this task off the iceberg:", "  " + deletedTask,
                "The iceberg now holds " + tasks.size() + " task(s).");
    }
}

/**
 * Adds a todo task.
 */
class TodoCommand extends InputCommand {
    /**
     * Creates a command that adds a todo.
     *
     * @param details the description after the {@code todo} keyword.
     */
    TodoCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        if (details.isEmpty()) {
            ui.showResponse("NOOT?! A todo needs a description."
                    + " Try \"todo borrow book\"; my flippers cannot read minds.");
            return;
        }
        addTask(tasks, ui, storage, new Todo(details));
    }

    /**
     * Adds a task, restores the list when saving fails, and displays confirmation.
     *
     * @param tasks the task list to change.
     * @param ui the console user interface.
     * @param storage the persistent task storage.
     * @param task the task to add.
     * @throws NoriException if the task cannot be saved.
     */
    static void addTask(TaskList tasks, Ui ui, Storage storage, Task task) throws NoriException {
        tasks.add(task);
        assert tasks.get(tasks.size() - 1) == task
                : "The added task must be the last one, because a failed save removes the last task.";
        try {
            storage.saveTasks(tasks.asUnmodifiableList());
        } catch (NoriException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
        ui.showResponse("Noot noot! Task tucked safely under my wing:", "  " + task,
                "The iceberg now holds " + tasks.size() + " task(s).");
    }
}

/**
 * Adds a deadline task.
 */
class DeadlineCommand extends InputCommand {
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
            TodoCommand.addTask(tasks, ui, storage,
                    new Deadline(description, Deadline.parseInput(deadlineInput)));
        }
    }
}

/**
 * Adds an event task.
 */
class EventCommand extends InputCommand {
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
            TodoCommand.addTask(tasks, ui, storage, new Event(description, from, to));
        }
    }
}

/**
 * Ends Nori's command loop.
 */
class ExitCommand extends Command {
    /** Creates an exit command. */
    ExitCommand() {
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        // Nori ends its loop after this command has executed.
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExit() {
        return true;
    }
}

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
