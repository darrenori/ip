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

    public static void main(String[] args) {
        System.out.print(BANNER);
        printResponse(false, "Hello! I'm Nori.", "What can I do for you?");

        String[] tasks = new String[MAX_TASKS];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("bye")) {
                break;
            }
            if (input.equals("list")) {
                printResponse(true, formatTaskList(tasks, taskCount));
            } else {
                tasks[taskCount] = input;
                taskCount++;
                printResponse(true, "added: " + input);
            }
        }
        scanner.close();

        printResponse(true, "Bye. Hope to see you again soon!");
    }

    /**
     * Builds the numbered task list as individual lines (e.g. "1. read book"),
     * one array element per line, so each can be indented consistently by printResponse.
     */
    private static String[] formatTaskList(String[] tasks, int taskCount) {
        String[] lines = new String[taskCount];
        for (int i = 0; i < taskCount; i++) {
            lines[i] = (i + 1) + ". " + tasks[i];
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
