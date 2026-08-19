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
    private static final String TODO_COMMAND = "todo ";

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
            if (input.equals("list")) {
                printResponse(true, formatTaskList(tasks, taskCount));
            } else if (input.startsWith("mark ")) {
                int taskIndex = Integer.parseInt(input.substring("mark ".length())) - 1;
                if (tasks[taskIndex].isDone()) {
                    printResponse(true, "Yo! You've already marked this task.");
                } else {
                    tasks[taskIndex].markAsDone();
                    printResponse(true, "Nice! I've marked this task as done:",
                            "  " + tasks[taskIndex]);
                }
            } else if (input.startsWith("unmark ")) {
                int taskIndex = Integer.parseInt(input.substring("unmark ".length())) - 1;
                if (!tasks[taskIndex].isDone()) {
                    printResponse(true, "Yo! You've already unmarked this task.");
                } else {
                    tasks[taskIndex].markAsNotDone();
                    printResponse(true, "OK, I've marked this task as not done yet:",
                            "  " + tasks[taskIndex]);
                }
            } else if (input.startsWith(TODO_COMMAND)) {
                String description = input.substring(TODO_COMMAND.length());
                tasks[taskCount] = new Task(description);
                taskCount++;
                printResponse(true, "Got it. I've added this task:", "  " + tasks[taskCount - 1],
                        "Now you have " + taskCount + " tasks in the list.");
            } else {
                tasks[taskCount] = new Task(input);
                taskCount++;
                printResponse(true, "added: " + input);
            }
        }
        scanner.close();

        printResponse(true, "Bye. Hope to see you again soon!");
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
