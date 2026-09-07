package nori.command;

/**
 * Creates concrete commands from their recognized types and details.
 */
public final class Commands {
    /** Prevents instantiation of this command factory. */
    private Commands() {
    }

    /**
     * Creates the command represented by a recognized type.
     *
     * @param commandType the recognized command type.
     * @param details the text after the command keyword.
     * @return the corresponding executable command.
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
     * @return an unrecognized-command handler.
     */
    public static Command createUnknown() {
        return new UnknownCommand();
    }
}
