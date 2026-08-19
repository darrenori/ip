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

    public static void main(String[] args) {
        System.out.print(BANNER);
        printResponse(false, "Hello! I'm Nori.", "What can I do for you?");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("bye")) {
                break;
            }
            printResponse(true, input);
        }
        scanner.close();

        printResponse(true, "Bye. Hope to see you again soon!");
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
