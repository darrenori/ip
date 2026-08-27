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
    private Commands() {
    }

    /**
     * Creates the command represented by a recognized type.
     *
     * @param commandType the recognized command type
     * @param details the text after the command keyword
     * @return the corresponding executable command
     */
    public static Command create(CommandType commandType, String details) {
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
            return new UnknownCommand();
        }
    }

    /**
     * Creates a command that reports unrecognized input.
     *
     * @return an unrecognized-command handler
     */
    public static Command createUnknown() {
        return new UnknownCommand();
    }
}

/**
 * Provides common input storage for commands entered with trailing details.
 */
abstract class InputCommand extends Command {
    protected final String details;

    /**
     * Creates a command with its input details.
     *
     * @param details the text after the command keyword
     */
    InputCommand(String details) {
        this.details = details;
    }
}

/**
 * Displays the complete task list or tasks in a date range.
 */
class ListCommand extends InputCommand {
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
    FindCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        if (details.isEmpty()) {
            ui.showResponse("OOPS!!! \"find\" needs a keyword."
                    + " Try \"find book\" — searching for nothing finds everything lah.");
            return;
        }
        ui.showResponse(tasks.getTasksMatchingKeywordDisplayLines(details));
    }
}

/**
 * Marks a task as complete.
 */
class MarkCommand extends InputCommand {
    MarkCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        int taskIndex = tasks.getTaskIndex(details, "mark");
        Task task = tasks.get(taskIndex);
        if (task.isDone()) {
            ui.showResponse("Yo! You've already marked this task.");
            return;
        }

        task.markAsDone();
        try {
            storage.saveTasks(tasks.asUnmodifiableList());
        } catch (NoriException exception) {
            task.markAsNotDone();
            throw exception;
        }
        ui.showResponse("Nice! I've marked this task as done:", "  " + task);
    }
}

/**
 * Marks a task as incomplete.
 */
class UnmarkCommand extends InputCommand {
    UnmarkCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        int taskIndex = tasks.getTaskIndex(details, "unmark");
        Task task = tasks.get(taskIndex);
        if (!task.isDone()) {
            ui.showResponse("Yo! You've already unmarked this task.");
            return;
        }

        task.markAsNotDone();
        try {
            storage.saveTasks(tasks.asUnmodifiableList());
        } catch (NoriException exception) {
            task.markAsDone();
            throw exception;
        }
        ui.showResponse("OK, I've marked this task as not done yet:", "  " + task);
    }
}

/**
 * Removes a task from the list.
 */
class DeleteCommand extends InputCommand {
    DeleteCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        int taskIndex = tasks.getTaskIndex(details, "delete");
        Task deletedTask = tasks.remove(taskIndex);
        try {
            storage.saveTasks(tasks.asUnmodifiableList());
        } catch (NoriException exception) {
            tasks.add(taskIndex, deletedTask);
            throw exception;
        }
        ui.showResponse("Noted. I've removed this task:", "  " + deletedTask,
                "Now you have " + tasks.size() + " tasks in the list.");
    }
}

/**
 * Adds a todo task.
 */
class TodoCommand extends InputCommand {
    TodoCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        if (details.isEmpty()) {
            ui.showResponse("OOPS!!! A todo needs a description."
                    + " Try \"todo borrow book\" — I cannot read your mind lah.");
            return;
        }
        addTask(tasks, ui, storage, new Todo(details));
    }

    /**
     * Adds a task, restores the list when saving fails, and displays confirmation.
     *
     * @param tasks the task list to change
     * @param ui the console user interface
     * @param storage the persistent task storage
     * @param task the task to add
     * @throws NoriException if the task cannot be saved
     */
    static void addTask(TaskList tasks, Ui ui, Storage storage, Task task) throws NoriException {
        tasks.add(task);
        try {
            storage.saveTasks(tasks.asUnmodifiableList());
        } catch (NoriException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
        ui.showResponse("Got it. I've added this task:", "  " + task,
                "Now you have " + tasks.size() + " tasks in the list.");
    }
}

/**
 * Adds a deadline task.
 */
class DeadlineCommand extends InputCommand {
    private static final String DEADLINE_SEPARATOR = " /by ";

    DeadlineCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        int separatorIndex = details.indexOf(DEADLINE_SEPARATOR);
        if (details.startsWith("/by ")) {
            ui.showResponse("OOPS!!! A deadline needs a description before \"/by\"."
                    + " Try \"deadline submit report /by 2019-10-15\".");
        } else if (details.endsWith("/by")) {
            ui.showResponse("OOPS!!! A deadline needs a due date after \"/by\"."
                    + " Try \"deadline submit report /by 2019-10-15\".");
        } else if (separatorIndex == -1) {
            ui.showResponse("OOPS!!! I cannot find the \"/by\" part of that deadline."
                    + " Use \"deadline submit report /by 2019-10-15\".");
        } else {
            addDeadline(tasks, ui, storage, separatorIndex);
        }
    }

    /**
     * Validates a deadline's details and adds the resulting task.
     *
     * @param tasks the task list to change
     * @param ui the console user interface
     * @param storage the persistent task storage
     * @param separatorIndex the position of the {@code /by} separator
     * @throws NoriException if the deadline cannot be parsed or saved
     */
    private void addDeadline(TaskList tasks, Ui ui, Storage storage, int separatorIndex) throws NoriException {
        String description = details.substring(0, separatorIndex).trim();
        String deadlineInput = details.substring(separatorIndex + DEADLINE_SEPARATOR.length()).trim();
        if (description.isEmpty()) {
            ui.showResponse("OOPS!!! A deadline needs a description before \"/by\"."
                    + " Due for what exactly, boss?");
        } else if (deadlineInput.isEmpty()) {
            ui.showResponse("OOPS!!! A deadline needs a due date after \"/by\"."
                    + " Don't leave me hanging lah.");
        } else {
            TodoCommand.addTask(tasks, ui, storage, new Deadline(description, Deadline.parseInput(deadlineInput)));
        }
    }
}

/**
 * Adds an event task.
 */
class EventCommand extends InputCommand {
    private static final String EVENT_FROM_SEPARATOR = " /from ";
    private static final String EVENT_TO_SEPARATOR = " /to ";

    EventCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        int fromSeparatorIndex = details.indexOf(EVENT_FROM_SEPARATOR);
        int toSeparatorIndex = details.indexOf(EVENT_TO_SEPARATOR);
        if (details.startsWith("/from ")) {
            ui.showResponse("OOPS!!! An event needs a description before \"/from\"."
                    + " Try \"event team meeting /from Mon 2pm /to 4pm\".");
        } else if (fromSeparatorIndex == -1 && toSeparatorIndex == -1) {
            ui.showResponse("OOPS!!! An event needs both \"/from\" and \"/to\"."
                    + " Use \"event team meeting /from Mon 2pm /to 4pm\".");
        } else if (fromSeparatorIndex == -1) {
            ui.showResponse("OOPS!!! An event is missing \"/from\" and its start time."
                    + " Put \"/from\" before \"/to\", can?");
        } else if (toSeparatorIndex == -1) {
            ui.showResponse("OOPS!!! An event is missing \"/to\" and its end time."
                    + " I need to know when you escape the meeting leh.");
        } else if (toSeparatorIndex < fromSeparatorIndex) {
            ui.showResponse("OOPS!!! Put \"/from\" before \"/to\"."
                    + " Time flows forward, not backwards, sia.");
        } else {
            addEvent(tasks, ui, storage, fromSeparatorIndex, toSeparatorIndex);
        }
    }

    /**
     * Validates an event's details and adds the resulting task.
     *
     * @param tasks the task list to change
     * @param ui the console user interface
     * @param storage the persistent task storage
     * @param fromSeparatorIndex the position of the {@code /from} separator
     * @param toSeparatorIndex the position of the {@code /to} separator
     * @throws NoriException if the event cannot be parsed or saved
     */
    private void addEvent(TaskList tasks, Ui ui, Storage storage, int fromSeparatorIndex,
            int toSeparatorIndex) throws NoriException {
        String description = details.substring(0, fromSeparatorIndex).trim();
        String from = details.substring(fromSeparatorIndex + EVENT_FROM_SEPARATOR.length(), toSeparatorIndex).trim();
        String to = details.substring(toSeparatorIndex + EVENT_TO_SEPARATOR.length()).trim();
        if (description.isEmpty()) {
            ui.showResponse("OOPS!!! An event needs a description before \"/from\"."
                    + " Meeting with who, your imaginary friend ah?");
        } else if (from.isEmpty()) {
            ui.showResponse("OOPS!!! \"/from\" needs a start time."
                    + " I cannot schedule an event that starts in the void leh.");
        } else if (to.isEmpty()) {
            ui.showResponse("OOPS!!! \"/to\" needs an end time."
                    + " Even meetings eventually end, right?");
        } else {
            TodoCommand.addTask(tasks, ui, storage, new Event(description, from, to));
        }
    }
}

/**
 * Ends Nori's command loop.
 */
class ExitCommand extends Command {
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
    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        throw new NoriException("OOPS!!! I'm sorry, but I don't know what that means :-("
                + " Try todo, deadline, event, on, list, mark, unmark, delete, help, or bye lah.");
    }
}
