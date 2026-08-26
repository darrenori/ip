/**
 * Represents each command that Nori recognises, together with its user-facing keyword.
 */
public enum Command {
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
     * Creates a command with the keyword entered by the user.
     *
     * @param keyword the command keyword
     */
    Command(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the keyword that represents this command in user input.
     *
     * @return the command keyword
     */
    public String getKeyword() {
        return keyword;
    }

}
