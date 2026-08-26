import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Nori chatbot.
 */
public class Nori {
    private static final String DEADLINE_SEPARATOR = " /by ";
    private static final String EVENT_FROM_SEPARATOR = " /from ";
    private static final String EVENT_TO_SEPARATOR = " /to ";
    private static final String LIST_FROM_PREFIX = "/from ";
    private static final String LIST_TO_SEPARATOR = " /to ";

    public static void main(String[] args) {
        Ui ui = new Ui();
        List<Task> tasks = new ArrayList<>();
        String loadingError = null;
        String loadingNotice = null;
        try {
            tasks = Storage.loadTasks();
            loadingNotice = Storage.getLoadingNotice();
        } catch (NoriException exception) {
            loadingError = exception.getMessage();
        }

        ui.showWelcome();
        if (loadingError != null) {
            ui.showLoadingError(loadingError);
        } else if (loadingNotice != null) {
            ui.showLoadingNotice(loadingNotice);
        }

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            Command command = Command.fromInput(input);
            if (command == Command.BYE) {
                break;
            }
            try {
            if (command == Command.LIST) {
                String listDetails = getCommandDetails(input, command.getKeyword());
                if (listDetails.isEmpty()) {
                    ui.showResponse(formatTaskList(tasks));
                } else {
                    DateRange dateRange = parseListDateRange(listDetails);
                    ui.showResponse(formatTasksInDateRange(tasks, dateRange));
                }
            } else if (command == Command.HELP) {
                ui.showHelp();
            } else if (command == Command.ON) {
                LocalDate date = parseDate(getCommandDetails(input, command.getKeyword()));
                ui.showResponse(formatTasksOnDate(tasks, date));
            } else if (command == Command.MARK) {
                int taskIndex = getTaskIndex(input, command.getKeyword(), tasks);
                if (tasks.get(taskIndex).isDone()) {
                    ui.showResponse("Yo! You've already marked this task.");
                } else {
                    tasks.get(taskIndex).markAsDone();
                    try {
                        Storage.saveTasks(tasks);
                    } catch (NoriException exception) {
                        tasks.get(taskIndex).markAsNotDone();
                        throw exception;
                    }
                    ui.showResponse("Nice! I've marked this task as done:",
                            "  " + tasks.get(taskIndex));
                }
            } else if (command == Command.UNMARK) {
                int taskIndex = getTaskIndex(input, command.getKeyword(), tasks);
                if (!tasks.get(taskIndex).isDone()) {
                    ui.showResponse("Yo! You've already unmarked this task.");
                } else {
                    tasks.get(taskIndex).markAsNotDone();
                    try {
                        Storage.saveTasks(tasks);
                    } catch (NoriException exception) {
                        tasks.get(taskIndex).markAsDone();
                        throw exception;
                    }
                    ui.showResponse("OK, I've marked this task as not done yet:",
                            "  " + tasks.get(taskIndex));
                }
            } else if (command == Command.DELETE) {
                int taskIndex = getTaskIndex(input, command.getKeyword(), tasks);
                deleteTask(ui, tasks, taskIndex);
            } else if (command == Command.TODO) {
                String description = getCommandDetails(input, command.getKeyword());
                if (description.isEmpty()) {
                    ui.showResponse("OOPS!!! A todo needs a description."
                            + " Try \"todo borrow book\" — I cannot read your mind lah.");
                } else {
                    addTask(ui, tasks, new Todo(description));
                }
            } else if (command == Command.DEADLINE) {
                String deadlineDetails = getCommandDetails(input, command.getKeyword());
                int separatorIndex = deadlineDetails.indexOf(DEADLINE_SEPARATOR);
                if (deadlineDetails.startsWith("/by ")) {
                    ui.showResponse("OOPS!!! A deadline needs a description before \"/by\"."
                            + " Try \"deadline submit report /by 2019-10-15\".");
                } else if (deadlineDetails.endsWith("/by")) {
                    ui.showResponse("OOPS!!! A deadline needs a due date after \"/by\"."
                            + " Try \"deadline submit report /by 2019-10-15\".");
                } else if (separatorIndex == -1) {
                    ui.showResponse("OOPS!!! I cannot find the \"/by\" part of that deadline."
                            + " Use \"deadline submit report /by 2019-10-15\".");
                } else {
                    String description = deadlineDetails.substring(0, separatorIndex).trim();
                    String deadlineInput = deadlineDetails.substring(separatorIndex + DEADLINE_SEPARATOR.length()).trim();
                    if (description.isEmpty()) {
                        ui.showResponse("OOPS!!! A deadline needs a description before \"/by\"."
                                + " Due for what exactly, boss?");
                    } else if (deadlineInput.isEmpty()) {
                        ui.showResponse("OOPS!!! A deadline needs a due date after \"/by\"."
                                + " Don't leave me hanging lah.");
                    } else {
                        addTask(ui, tasks, new Deadline(description, Deadline.parseInput(deadlineInput)));
                    }
                }
            } else if (command == Command.EVENT) {
                String eventDetails = getCommandDetails(input, command.getKeyword());
                int fromSeparatorIndex = eventDetails.indexOf(EVENT_FROM_SEPARATOR);
                int toSeparatorIndex = eventDetails.indexOf(EVENT_TO_SEPARATOR);
                if (eventDetails.startsWith("/from ")) {
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
                    String description = eventDetails.substring(0, fromSeparatorIndex).trim();
                    String from = eventDetails.substring(fromSeparatorIndex + EVENT_FROM_SEPARATOR.length(),
                            toSeparatorIndex).trim();
                    String to = eventDetails.substring(toSeparatorIndex + EVENT_TO_SEPARATOR.length()).trim();
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
                        addTask(ui, tasks, new Event(description, from, to));
                    }
                }
            } else {
                throw new NoriException("OOPS!!! I'm sorry, but I don't know what that means :-("
                        + " Try todo, deadline, event, on, list, mark, unmark, delete, help, or bye lah.");
            }
            } catch (NoriException exception) {
                ui.showResponse(exception.getMessage());
            }
        }
        scanner.close();

        ui.showResponse("Bye. Hope to see you again soon!");
    }

    /**
     * Returns the trimmed text after a command keyword.
     *
     * @param input the complete user input
     * @param command the command keyword
     * @return the command details, without surrounding whitespace
     */
    private static String getCommandDetails(String input, String command) {
        return input.substring(command.length()).trim();
    }

    /**
     * Parses the ISO-8601 date provided to the {@code on} command.
     *
     * @param dateInput the date text after the {@code on} command
     * @return the parsed date
     * @throws NoriException if the date is missing or invalid
     */
    private static LocalDate parseDate(String dateInput) throws NoriException {
        if (dateInput.isEmpty()) {
            throw new NoriException("OOPS!!! \"on\" needs a date. Try \"on 2019-10-15\".");
        }

        try {
            return LocalDate.parse(dateInput);
        } catch (DateTimeParseException exception) {
            throw new NoriException("OOPS!!! I cannot understand \"" + dateInput + "\" as a date."
                    + " Use a date like \"2019-10-15\".");
        }
    }

    /**
     * Parses the inclusive date range supplied to the {@code list} command.
     *
     * @param listDetails the text after the {@code list} command
     * @return the parsed inclusive date range
     * @throws NoriException if the range format, dates, or date order is invalid
     */
    private static DateRange parseListDateRange(String listDetails) throws NoriException {
        if (!listDetails.startsWith(LIST_FROM_PREFIX)) {
            throw new NoriException("OOPS!!! Use either \"list\" or \"list /from 2019-01-01 /to 2021-01-01\".");
        }

        int toSeparatorIndex = listDetails.indexOf(LIST_TO_SEPARATOR);
        if (toSeparatorIndex == -1) {
            throw new NoriException("OOPS!!! A date-range list needs \"/to\" and an end date."
                    + " Try \"list /from 2019-01-01 /to 2021-01-01\".");
        }

        String fromInput = listDetails.substring(LIST_FROM_PREFIX.length(), toSeparatorIndex).trim();
        String toInput = listDetails.substring(toSeparatorIndex + LIST_TO_SEPARATOR.length()).trim();
        if (fromInput.isEmpty()) {
            throw new NoriException("OOPS!!! \"/from\" needs a start date."
                    + " Try \"list /from 2019-01-01 /to 2021-01-01\".");
        }
        if (toInput.isEmpty()) {
            throw new NoriException("OOPS!!! \"/to\" needs an end date."
                    + " Try \"list /from 2019-01-01 /to 2021-01-01\".");
        }

        LocalDate fromDate = parseListDate(fromInput, "/from");
        LocalDate toDate = parseListDate(toInput, "/to");
        if (toDate.isBefore(fromDate)) {
            throw new NoriException("OOPS!!! The \"/to\" date cannot be before the \"/from\" date.");
        }
        return new DateRange(fromDate, toDate);
    }

    /**
     * Parses one date from a date-range list command.
     *
     * @param dateInput the ISO-8601 date text to parse
     * @param rangePart the range separator that introduced the date
     * @return the parsed date
     * @throws NoriException if the date is invalid
     */
    private static LocalDate parseListDate(String dateInput, String rangePart) throws NoriException {
        try {
            return LocalDate.parse(dateInput);
        } catch (DateTimeParseException exception) {
            throw new NoriException("OOPS!!! I cannot understand \"" + dateInput + "\" as the "
                    + rangePart + " date. Use a date like \"2019-10-15\".");
        }
    }

    /**
     * Converts a command's task number to a valid zero-based task index.
     *
     * @param input the complete user input
     * @param command the command keyword
     * @param tasks the task list
     * @return the validated zero-based index
     * @throws NoriException if the input does not identify a stored task
     */
    private static int getTaskIndex(String input, String command, List<Task> tasks) throws NoriException {
        try {
            int taskIndex = Integer.parseInt(getCommandDetails(input, command)) - 1;
            if (taskIndex >= 0 && taskIndex < tasks.size()) {
                return taskIndex;
            }
            throw new NoriException(getTaskNumberError(input, command, tasks.size()));
        } catch (NumberFormatException exception) {
            throw new NoriException(getTaskNumberError(input, command, tasks.size()));
        }
    }

    /**
     * Explains why a mark or unmark command does not refer to a stored task.
     *
     * @param input the complete user input
     * @param command the command keyword
     * @param taskCount the number of tasks currently stored
     * @return a specific corrective error message
     */
    private static String getTaskNumberError(String input, String command, int taskCount) {
        String taskNumber = getCommandDetails(input, command);
        if (taskNumber.isEmpty()) {
            return "OOPS!!! \"" + command + "\" needs a task number. Try \"" + command + " 1\".";
        }
        try {
            int taskIndex = Integer.parseInt(taskNumber) - 1;
            if (taskIndex < 0) {
                return "OOPS!!! Task numbers start from 1, not " + taskNumber + ". Nice try lah.";
            }
            if (taskCount == 0) {
                return "OOPS!!! There are no tasks yet, so there is nothing to " + command + ".";
            }
            return "OOPS!!! You have only " + taskCount + " task(s), so \"" + command + " "
                    + taskNumber + "\" is out of range. Don't anyhow point lah.";
        } catch (NumberFormatException exception) {
            if (isIntegerLiteral(taskNumber)) {
                return "ARE YOU DONEEEE????? \"" + taskNumber + "\" is far too large for a task number."
                        + " Use a whole number from 1 to " + Math.max(taskCount, 1) + ".";
            }
            return "OOPS!!! \"" + taskNumber + "\" is not a task number."
                    + " Use \"" + command + " 1\", not words lah.";
        }
    }

    /**
     * Returns whether text is an optionally signed whole-number literal.
     *
     * @param text the text to inspect
     * @return {@code true} if the text contains only an optional sign and digits
     */
    private static boolean isIntegerLiteral(String text) {
        int firstDigitIndex = text.startsWith("-") || text.startsWith("+") ? 1 : 0;
        if (firstDigitIndex == text.length()) {
            return false;
        }
        for (int index = firstDigitIndex; index < text.length(); index++) {
            if (!Character.isDigit(text.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a task to the list and prints its confirmation.
     *
     * @param ui the console interface used to show the confirmation
     * @param tasks the task list
     * @param task the task to add
     */
    private static void addTask(Ui ui, List<Task> tasks, Task task) throws NoriException {
        tasks.add(task);
        try {
            Storage.saveTasks(tasks);
        } catch (NoriException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
        ui.showResponse("Got it. I've added this task:", "  " + task,
                "Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Removes a task and prints its confirmation.
     *
     * @param ui the console interface used to show the confirmation
     * @param tasks the task list
     * @param taskIndex the zero-based index of the task to remove
     */
    private static void deleteTask(Ui ui, List<Task> tasks, int taskIndex) throws NoriException {
        Task deletedTask = tasks.remove(taskIndex);
        try {
            Storage.saveTasks(tasks);
        } catch (NoriException exception) {
            tasks.add(taskIndex, deletedTask);
            throw exception;
        }
        ui.showResponse("Noted. I've removed this task:", "  " + deletedTask,
                "Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Builds the list heading and numbered task lines (e.g. "1.[X] read book"),
     * one list element per line, so each can be indented consistently by {@link Ui}.
     */
    private static String[] formatTaskList(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return new String[] {"Your list is empty. Add something with \"todo borrow book\" lah."};
        }
        String[] lines = new String[tasks.size() + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < tasks.size(); i++) {
            lines[i + 1] = (i + 1) + "." + tasks.get(i);
        }
        return lines;
    }

    /**
     * Builds the date-query heading and the matching deadline and event task lines.
     *
     * @param tasks the task list to search
     * @param date the date to match
     * @return the formatted date-query response lines
     */
    private static String[] formatTasksOnDate(List<Task> tasks, LocalDate date) {
        List<String> matchingTasks = new ArrayList<>();
        for (int index = 0; index < tasks.size(); index++) {
            Task task = tasks.get(index);
            if (occursOn(task, date)) {
                matchingTasks.add((index + 1) + "." + task);
            }
        }

        if (matchingTasks.isEmpty()) {
            return new String[] {"There are no deadlines or events on " + date + "."};
        }

        String[] lines = new String[matchingTasks.size() + 1];
        lines[0] = "Here are the deadlines and events on " + date + ":";
        for (int index = 0; index < matchingTasks.size(); index++) {
            lines[index + 1] = matchingTasks.get(index);
        }
        return lines;
    }

    /**
     * Builds the date-range heading and the matching deadline and event task lines.
     *
     * @param tasks the task list to search
     * @param dateRange the inclusive date range to match
     * @return the formatted date-range response lines
     */
    private static String[] formatTasksInDateRange(List<Task> tasks, DateRange dateRange) {
        List<String> matchingTasks = new ArrayList<>();
        for (int index = 0; index < tasks.size(); index++) {
            Task task = tasks.get(index);
            if (occursInDateRange(task, dateRange)) {
                matchingTasks.add((index + 1) + "." + task);
            }
        }

        if (matchingTasks.isEmpty()) {
            return new String[] {"There are no deadlines or events from " + dateRange.getFrom()
                    + " to " + dateRange.getTo() + "."};
        }

        String[] lines = new String[matchingTasks.size() + 1];
        lines[0] = "Here are the deadlines and events from " + dateRange.getFrom()
                + " to " + dateRange.getTo() + ":";
        for (int index = 0; index < matchingTasks.size(); index++) {
            lines[index + 1] = matchingTasks.get(index);
        }
        return lines;
    }

    /**
     * Returns whether a task is a deadline or event occurring on the given date.
     *
     * @param task the task to inspect
     * @param date the date to match
     * @return {@code true} if the task occurs on {@code date}
     */
    private static boolean occursOn(Task task, LocalDate date) {
        if (task instanceof Deadline) {
            return ((Deadline) task).occursOn(date);
        }
        if (task instanceof Event) {
            return ((Event) task).occursOn(date);
        }
        return false;
    }

    /**
     * Returns whether a task is a deadline in, or an event overlapping, a date range.
     *
     * @param task the task to inspect
     * @param dateRange the inclusive date range to match
     * @return {@code true} if the task occurs within the date range
     */
    private static boolean occursInDateRange(Task task, DateRange dateRange) {
        if (task instanceof Deadline) {
            return ((Deadline) task).occursInDateRange(dateRange.getFrom(), dateRange.getTo());
        }
        if (task instanceof Event) {
            return ((Event) task).occursInDateRange(dateRange.getFrom(), dateRange.getTo());
        }
        return false;
    }

    /**
     * Represents an inclusive start and end date for a list query.
     */
    private static final class DateRange {
        private final LocalDate from;
        private final LocalDate to;

        private DateRange(LocalDate from, LocalDate to) {
            this.from = from;
            this.to = to;
        }

        private LocalDate getFrom() {
            return from;
        }

        private LocalDate getTo() {
            return to;
        }
    }

}
