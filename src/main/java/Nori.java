import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Nori chatbot.
 */
public class Nori {
    private static final String INDENT = "    ";
    private static final String DIVIDER_LINE = "____________________________________________________________";
    private static final String DIVIDER = INDENT + DIVIDER_LINE;
    private static final String BANNER = "  _   _  ____  _____  _____ \n"
            + " | \\ | |/ __ \\|  __ \\|_   _|\n"
            + " |  \\| | |  | | |__) | | |  \n"
            + " | . ` | |  | |  _  /  | |  \n"
            + " | |\\  | |__| | | \\ \\ _| |_ \n"
            + " |_| \\_|\\____/|_|  \\_\\_____|\n\n\n";

    private static final String DEADLINE_SEPARATOR = " /by ";
    private static final String EVENT_FROM_SEPARATOR = " /from ";
    private static final String EVENT_TO_SEPARATOR = " /to ";

    public static void main(String[] args) {
        List<Task> tasks = new ArrayList<>();
        String loadingError = null;
        String loadingNotice = null;
        try {
            tasks = Storage.loadTasks();
            loadingNotice = Storage.getLoadingNotice();
        } catch (NoriException exception) {
            loadingError = exception.getMessage();
        }

        System.out.print(BANNER);
        printResponse(false, "Hello! I'm Nori.", "What can I do for you?");
        if (loadingError != null) {
            printResponse(true, loadingError);
        } else if (loadingNotice != null) {
            printResponse(true, loadingNotice);
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
                printResponse(true, formatTaskList(tasks));
            } else if (command == Command.ON) {
                LocalDate date = parseDate(getCommandDetails(input, command.getKeyword()));
                printResponse(true, formatTasksOnDate(tasks, date));
            } else if (command == Command.MARK) {
                int taskIndex = getTaskIndex(input, command.getKeyword(), tasks);
                if (tasks.get(taskIndex).isDone()) {
                    printResponse(true, "Yo! You've already marked this task.");
                } else {
                    tasks.get(taskIndex).markAsDone();
                    try {
                        Storage.saveTasks(tasks);
                    } catch (NoriException exception) {
                        tasks.get(taskIndex).markAsNotDone();
                        throw exception;
                    }
                    printResponse(true, "Nice! I've marked this task as done:",
                            "  " + tasks.get(taskIndex));
                }
            } else if (command == Command.UNMARK) {
                int taskIndex = getTaskIndex(input, command.getKeyword(), tasks);
                if (!tasks.get(taskIndex).isDone()) {
                    printResponse(true, "Yo! You've already unmarked this task.");
                } else {
                    tasks.get(taskIndex).markAsNotDone();
                    try {
                        Storage.saveTasks(tasks);
                    } catch (NoriException exception) {
                        tasks.get(taskIndex).markAsDone();
                        throw exception;
                    }
                    printResponse(true, "OK, I've marked this task as not done yet:",
                            "  " + tasks.get(taskIndex));
                }
            } else if (command == Command.DELETE) {
                int taskIndex = getTaskIndex(input, command.getKeyword(), tasks);
                deleteTask(tasks, taskIndex);
            } else if (command == Command.TODO) {
                String description = getCommandDetails(input, command.getKeyword());
                if (description.isEmpty()) {
                    printResponse(true, "OOPS!!! A todo needs a description."
                            + " Try \"todo borrow book\" — I cannot read your mind lah.");
                } else {
                    addTask(tasks, new Todo(description));
                }
            } else if (command == Command.DEADLINE) {
                String deadlineDetails = getCommandDetails(input, command.getKeyword());
                int separatorIndex = deadlineDetails.indexOf(DEADLINE_SEPARATOR);
                if (deadlineDetails.startsWith("/by ")) {
                    printResponse(true, "OOPS!!! A deadline needs a description before \"/by\"."
                            + " Try \"deadline submit report /by 2019-10-15\".");
                } else if (deadlineDetails.endsWith("/by")) {
                    printResponse(true, "OOPS!!! A deadline needs a due date after \"/by\"."
                            + " Try \"deadline submit report /by 2019-10-15\".");
                } else if (separatorIndex == -1) {
                    printResponse(true, "OOPS!!! I cannot find the \"/by\" part of that deadline."
                            + " Use \"deadline submit report /by 2019-10-15\".");
                } else {
                    String description = deadlineDetails.substring(0, separatorIndex).trim();
                    String deadlineInput = deadlineDetails.substring(separatorIndex + DEADLINE_SEPARATOR.length()).trim();
                    if (description.isEmpty()) {
                        printResponse(true, "OOPS!!! A deadline needs a description before \"/by\"."
                                + " Due for what exactly, boss?");
                    } else if (deadlineInput.isEmpty()) {
                        printResponse(true, "OOPS!!! A deadline needs a due date after \"/by\"."
                                + " Don't leave me hanging lah.");
                    } else {
                        addTask(tasks, new Deadline(description, Deadline.parseInput(deadlineInput)));
                    }
                }
            } else if (command == Command.EVENT) {
                String eventDetails = getCommandDetails(input, command.getKeyword());
                int fromSeparatorIndex = eventDetails.indexOf(EVENT_FROM_SEPARATOR);
                int toSeparatorIndex = eventDetails.indexOf(EVENT_TO_SEPARATOR);
                if (eventDetails.startsWith("/from ")) {
                    printResponse(true, "OOPS!!! An event needs a description before \"/from\"."
                            + " Try \"event team meeting /from Mon 2pm /to 4pm\".");
                } else if (fromSeparatorIndex == -1 && toSeparatorIndex == -1) {
                    printResponse(true, "OOPS!!! An event needs both \"/from\" and \"/to\"."
                            + " Use \"event team meeting /from Mon 2pm /to 4pm\".");
                } else if (fromSeparatorIndex == -1) {
                    printResponse(true, "OOPS!!! An event is missing \"/from\" and its start time."
                            + " Put \"/from\" before \"/to\", can?");
                } else if (toSeparatorIndex == -1) {
                    printResponse(true, "OOPS!!! An event is missing \"/to\" and its end time."
                            + " I need to know when you escape the meeting leh.");
                } else if (toSeparatorIndex < fromSeparatorIndex) {
                    printResponse(true, "OOPS!!! Put \"/from\" before \"/to\"."
                            + " Time flows forward, not backwards, sia.");
                } else {
                    String description = eventDetails.substring(0, fromSeparatorIndex).trim();
                    String from = eventDetails.substring(fromSeparatorIndex + EVENT_FROM_SEPARATOR.length(),
                            toSeparatorIndex).trim();
                    String to = eventDetails.substring(toSeparatorIndex + EVENT_TO_SEPARATOR.length()).trim();
                    if (description.isEmpty()) {
                        printResponse(true, "OOPS!!! An event needs a description before \"/from\"."
                                + " Meeting with who, your imaginary friend ah?");
                    } else if (from.isEmpty()) {
                        printResponse(true, "OOPS!!! \"/from\" needs a start time."
                                + " I cannot schedule an event that starts in the void leh.");
                    } else if (to.isEmpty()) {
                        printResponse(true, "OOPS!!! \"/to\" needs an end time."
                                + " Even meetings eventually end, right?");
                    } else {
                        addTask(tasks, new Event(description, from, to));
                    }
                }
            } else {
                throw new NoriException("OOPS!!! I'm sorry, but I don't know what that means :-("
                        + " Try todo, deadline, event, on, list, mark, unmark, delete, or bye lah.");
            }
            } catch (NoriException exception) {
                printResponse(true, exception.getMessage());
            }
        }
        scanner.close();

        printResponse(true, "Bye. Hope to see you again soon!");
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
     * @param tasks the task list
     * @param task the task to add
     */
    private static void addTask(List<Task> tasks, Task task) throws NoriException {
        tasks.add(task);
        try {
            Storage.saveTasks(tasks);
        } catch (NoriException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
        printResponse(true, "Got it. I've added this task:", "  " + task,
                "Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Removes a task and prints its confirmation.
     *
     * @param tasks the task list
     * @param taskIndex the zero-based index of the task to remove
     */
    private static void deleteTask(List<Task> tasks, int taskIndex) throws NoriException {
        Task deletedTask = tasks.remove(taskIndex);
        try {
            Storage.saveTasks(tasks);
        } catch (NoriException exception) {
            tasks.add(taskIndex, deletedTask);
            throw exception;
        }
        printResponse(true, "Noted. I've removed this task:", "  " + deletedTask,
                "Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Builds the list heading and numbered task lines (e.g. "1.[X] read book"),
     * one list element per line, so each can be indented consistently by printResponse.
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
     * Prints one or more lines wrapped between dividers, followed by a blank line
     * separating it from the next block. The greeting is flush-left (no indent);
     * every response after that is indented to line up under the banner.
     */
    private static void printResponse(boolean indent, String... lines) {
        String divider = indent ? DIVIDER : DIVIDER_LINE;
        String prefix = indent ? INDENT + " " : "";
        System.out.println(divider);
        for (String line : lines) {
            System.out.println(prefix + line);
        }
        System.out.println(divider);
        System.out.println();
    }
}
