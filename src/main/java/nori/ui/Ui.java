package nori.ui;

import java.util.Scanner;

/**
 * Handles Nori's console output.
 */
public class Ui {
    private static final String INDENT = "    ";
    private static final String DIVIDER_LINE = "____________________________________________________________";
    private static final String DIVIDER = INDENT + DIVIDER_LINE;
    private static final String BANNER = "  _   _  ____  _____  _____ \n"
            + " | \\ | |/ __ \\|  __ \\|_   _|\n"
            + " |  \\| | |  | | |__) | | |  \n"
            + " | . ` | |  | |  _  /  | |  \n"
            + " | |\\  | |__| | | \\ \\ _| |_ \n"
            + " |_| \\_|\\____/|_|  \\_\\_____|\n\n\n";
    private static final String[] HELP_LINES = {
        "Here are the commands you can use:",
        "todo <description>",
        "deadline <description> /by yyyy-MM-dd",
        "event <description> /from <start> /to <end>",
        "on yyyy-MM-dd",
        "list",
        "list /from yyyy-MM-dd /to yyyy-MM-dd",
        "mark <task number>",
        "unmark <task number>",
        "delete <task number>",
        "help",
        "bye",
        "Use yyyy-MM-dd in an event's /from or /to to find it with on."
    };
    private final Scanner scanner;

    /**
     * Creates Nori's console user interface.
     */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /**
     * Reads and trims one command from standard input.
     *
     * @return the command, or {@code null} if standard input has ended
     */
    public String readCommand() {
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine().trim();
    }

    /**
     * Closes the console input stream.
     */
    public void close() {
        scanner.close();
    }

    /**
     * Displays Nori's banner and greeting.
     */
    public void showWelcome() {
        System.out.print(BANNER);
        printResponse(false, "Hello! I'm Nori.", "What can I do for you?");
    }

    /**
     * Displays an error that occurred while loading saved tasks.
     *
     * @param message the loading error to display
     */
    public void showLoadingError(String message) {
        showResponse(message);
    }

    /**
     * Displays a notice produced while loading saved tasks.
     *
     * @param message the loading notice to display
     */
    public void showLoadingNotice(String message) {
        showResponse(message);
    }

    /**
     * Displays Nori's help text.
     */
    public void showHelp() {
        showResponse(HELP_LINES);
    }

    /**
     * Displays one or more response lines between indented dividers.
     *
     * @param lines the response lines to display
     */
    public void showResponse(String... lines) {
        printResponse(true, lines);
    }

    /**
     * Prints one or more lines between dividers, followed by a blank line.
     *
     * @param shouldIndent whether the response should align under the banner
     * @param lines the lines to print
     */
    private void printResponse(boolean shouldIndent, String... lines) {
        String divider = shouldIndent ? DIVIDER : DIVIDER_LINE;
        String prefix = shouldIndent ? INDENT + " " : "";
        System.out.println(divider);
        for (String line : lines) {
            System.out.println(prefix + line);
        }
        System.out.println(divider);
        System.out.println();
    }
}
