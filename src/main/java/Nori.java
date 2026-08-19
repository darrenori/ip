/**
 * Entry point for the Nori chatbot.
 */
public class Nori {
    private static final String DIVIDER = "____________________________________________________________";
    private static final String BANNER = "  _   _  ____  _____  _____ \n"
            + " | \\ | |/ __ \\|  __ \\|_   _|\n"
            + " |  \\| | |  | | |__) | | |  \n"
            + " | . ` | |  | |  _  /  | |  \n"
            + " | |\\  | |__| | | \\ \\ _| |_ \n"
            + " |_| \\_|\\____/|_|  \\_\\_____|\n\n\n";

    public static void main(String[] args) {
        System.out.println(DIVIDER);
        System.out.print(BANNER);
        System.out.println("Hello! I'm Nori.");
        System.out.println("What can I do for you?");
        System.out.println(DIVIDER);

        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(DIVIDER);
    }
}
