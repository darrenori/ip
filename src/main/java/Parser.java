/**
 * Interprets raw user input as a Nori command and its accompanying details.
 */
public class Parser {
    /**
     * Finds the command that starts the given input, respecting command-word boundaries.
     *
     * @param input the trimmed user input
     * @return the matching command, or {@code null} if the input is unrecognized
     */
    public static Command parse(String input) {
        for (Command command : Command.values()) {
            if (input.equals(command.getKeyword())
                    || command != Command.BYE && input.startsWith(command.getKeyword() + " ")) {
                return command;
            }
        }
        return null;
    }

    /**
     * Returns the trimmed text after a command keyword.
     *
     * @param input the complete user input
     * @param commandKeyword the command keyword
     * @return the command details, without surrounding whitespace
     */
    public static String getCommandDetails(String input, String commandKeyword) {
        return input.substring(commandKeyword.length()).trim();
    }
}
