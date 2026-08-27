package nori.command;

/**
 * Identifies the words that begin Nori commands.
 */
public enum CommandType {
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event"),
    LIST("list"),
    HELP("help"),
    ON("on"),
    MARK("mark"),
    UNMARK("unmark"),
    DELETE("delete"),
    BYE("bye");

    private final String keyword;

    /**
     * Creates a command type with its input keyword.
     *
     * @param keyword the command's input keyword
     */
    CommandType(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the keyword used to recognize this command.
     *
     * @return the command keyword
     */
    public String getKeyword() {
        return keyword;
    }
}
