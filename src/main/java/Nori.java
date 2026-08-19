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

    private static final int MAX_TASKS = 100;
    private static final String TODO_COMMAND = "todo";
    private static final String DEADLINE_COMMAND = "deadline";
    private static final String DEADLINE_SEPARATOR = " /by ";
    private static final String EVENT_COMMAND = "event";
    private static final String EVENT_FROM_SEPARATOR = " /from ";
    private static final String EVENT_TO_SEPARATOR = " /to ";
    private static final String MARK_COMMAND = "mark";
    private static final String UNMARK_COMMAND = "unmark";

    public static void main(String[] args) {
        System.out.print(BANNER);
        printResponse(false, "Hello! I'm Nori.", "What can I do for you?");

        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("bye")) {
                break;
            }
            try {
            if (input.equals("list")) {
                printResponse(true, formatTaskList(tasks, taskCount));
            } else if (isCommand(input, MARK_COMMAND)) {
                int taskIndex = getTaskIndex(input, MARK_COMMAND, taskCount);
                if (taskIndex == -1) {
                    printResponse(true, getTaskNumberError(input, MARK_COMMAND, taskCount));
                } else if (tasks[taskIndex].isDone()) {
                    printResponse(true, "Yo! You've already marked this task.");
                } else {
                    tasks[taskIndex].markAsDone();
                    printResponse(true, "Nice! I've marked this task as done:",
                            "  " + tasks[taskIndex]);
                }
            } else if (isCommand(input, UNMARK_COMMAND)) {
                int taskIndex = getTaskIndex(input, UNMARK_COMMAND, taskCount);
                if (taskIndex == -1) {
                    printResponse(true, getTaskNumberError(input, UNMARK_COMMAND, taskCount));
                } else if (!tasks[taskIndex].isDone()) {
                    printResponse(true, "Yo! You've already unmarked this task.");
                } else {
                    tasks[taskIndex].markAsNotDone();
                    printResponse(true, "OK, I've marked this task as not done yet:",
                            "  " + tasks[taskIndex]);
                }
            } else if (isCommand(input, TODO_COMMAND)) {
                String description = getCommandDetails(input, TODO_COMMAND);
                if (description.isEmpty()) {
                    printResponse(true, "OOPS!!! A todo needs a description."
                            + " Try \"todo borrow book\" — I cannot read your mind lah.");
                } else {
                    taskCount = addTask(tasks, taskCount, new Todo(description));
                }
            } else if (isCommand(input, DEADLINE_COMMAND)) {
                String deadlineDetails = getCommandDetails(input, DEADLINE_COMMAND);
                int separatorIndex = deadlineDetails.indexOf(DEADLINE_SEPARATOR);
                if (deadlineDetails.startsWith("/by ")) {
                    printResponse(true, "OOPS!!! A deadline needs a description before \"/by\"."
                            + " Try \"deadline submit report /by Friday\".");
                } else if (deadlineDetails.endsWith("/by")) {
                    printResponse(true, "OOPS!!! A deadline needs a due date or time after \"/by\"."
                            + " Try \"deadline submit report /by Friday\".");
                } else if (separatorIndex == -1) {
                    printResponse(true, "OOPS!!! I cannot find the \"/by\" part of that deadline."
                            + " Use \"deadline submit report /by Friday\".");
                } else {
                    String description = deadlineDetails.substring(0, separatorIndex).trim();
                    String by = deadlineDetails.substring(separatorIndex + DEADLINE_SEPARATOR.length()).trim();
                    if (description.isEmpty()) {
                        printResponse(true, "OOPS!!! A deadline needs a description before \"/by\"."
                                + " Due for what exactly, boss?");
                    } else if (by.isEmpty()) {
                        printResponse(true, "OOPS!!! A deadline needs a due date or time after \"/by\"."
                                + " Don't leave me hanging lah.");
                    } else {
                        taskCount = addTask(tasks, taskCount, new Deadline(description, by));
                    }
                }
            } else if (isCommand(input, EVENT_COMMAND)) {
                String eventDetails = getCommandDetails(input, EVENT_COMMAND);
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
                        taskCount = addTask(tasks, taskCount, new Event(description, from, to));
                    }
                }
            } else {
                throw new NoriException("OOPS!!! I'm sorry, but I don't know what that means :-("
                        + " Try todo, deadline, event, list, mark, unmark, or bye lah.");
            }
            } catch (NoriException exception) {
                printResponse(true, exception.getMessage());
            }
        }
        scanner.close();

        printResponse(true, "Bye. Hope to see you again soon!");
    }

    /**
     * Returns whether the input is a command by itself or followed by command details.
     *
     * @param input the complete user input
     * @param command the command keyword
     * @return {@code true} if the input starts a command; otherwise, {@code false}
     */
    private static boolean isCommand(String input, String command) {
        return input.equals(command) || input.startsWith(command + " ");
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
     * Converts a command's task number to a valid zero-based task index.
     *
     * @param input the complete user input
     * @param command the command keyword
     * @param taskCount the number of tasks currently stored
     * @return the zero-based index, or {@code -1} when the input is invalid
     */
    private static int getTaskIndex(String input, String command, int taskCount) throws NoriException {
        try {
            int taskIndex = Integer.parseInt(getCommandDetails(input, command)) - 1;
            if (taskIndex >= 0 && taskIndex < taskCount) {
                return taskIndex;
            }
            throw new NoriException(getTaskNumberError(input, command, taskCount));
        } catch (NumberFormatException exception) {
            throw new NoriException(getTaskNumberError(input, command, taskCount));
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
     * Adds a task to the list and prints its confirmation, unless the list is full.
     *
     * @param tasks the task list
     * @param taskCount the number of tasks currently stored
     * @param task the task to add
     * @return the updated task count
     */
    private static int addTask(Task[] tasks, int taskCount, Task task) throws NoriException {
        if (taskCount == MAX_TASKS) {
            throw new NoriException("ARE YOU DONEEEE????? The list already has 100 tasks, which is the limit."
                    + " No task was added; start a new session before adding more lah.");
        }
        tasks[taskCount] = task;
        int newTaskCount = taskCount + 1;
        printResponse(true, "Got it. I've added this task:", "  " + task,
                "Now you have " + newTaskCount + " tasks in the list.");
        return newTaskCount;
    }

    /**
     * Builds the list heading and numbered task lines (e.g. "1.[X] read book"),
     * one array element per line, so each can be indented consistently by printResponse.
     */
    private static String[] formatTaskList(Task[] tasks, int taskCount) {
        String[] lines = new String[taskCount + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < taskCount; i++) {
            lines[i + 1] = (i + 1) + "." + tasks[i];
        }
        return lines;
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
