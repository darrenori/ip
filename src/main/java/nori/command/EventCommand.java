package nori.command;

import java.util.List;
import java.util.Optional;

import nori.NoriException;
import nori.storage.Storage;
import nori.task.Event;
import nori.task.TaskList;
import nori.ui.Ui;

/**
 * Adds an event task.
 */
class EventCommand extends AddTaskCommand {
    /** Shown alongside every complaint, so a correction is always in view. */
    private static final String EVENT_EXAMPLE = "\"event team meeting /from Mon 2pm /to 4pm\"";

    /**
     * Creates a command that adds an event.
     *
     * @param details the description, start, and end after the {@code event} keyword.
     */
    EventCommand(String details) {
        super(details);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws NoriException {
        CommandOptions options = CommandOptions.parse(details);
        Optional<String> formatError = findFormatError(options);
        if (formatError.isPresent()) {
            ui.showResponse(formatError.get());
            return;
        }
        addTask(tasks, ui, storage, new Event(options.getDescription(),
                options.getValue(CommandOptions.OPTION_FROM),
                options.getValue(CommandOptions.OPTION_TO)));
    }

    /**
     * Reports the first fault in this command's details.
     *
     * @param options the options read from the details.
     * @return the correction to show the user, or empty when an event can be built.
     */
    private Optional<String> findFormatError(CommandOptions options) {
        Optional<String> unexpectedOption = options.findUnexpectedOption(
                CommandOptions.OPTION_FROM, CommandOptions.OPTION_TO);
        if (unexpectedOption.isPresent()) {
            return Optional.of("NOOT?! An event does not use \"" + unexpectedOption.get() + "\"."
                    + " Use " + EVENT_EXAMPLE + ".");
        }
        Optional<String> repeatedOption = options.findRepeatedOption();
        if (repeatedOption.isPresent()) {
            return Optional.of("NOOT?! An event takes only one \"" + repeatedOption.get() + "\"."
                    + " Use " + EVENT_EXAMPLE + ".");
        }
        boolean hasStart = options.hasOption(CommandOptions.OPTION_FROM);
        boolean hasEnd = options.hasOption(CommandOptions.OPTION_TO);
        if (!hasStart && !hasEnd) {
            return Optional.of("NOOT?! An event needs both \"/from\" and \"/to\"."
                    + " Use " + EVENT_EXAMPLE + ".");
        }
        if (!hasStart) {
            return Optional.of("NOOT?! An event is missing \"/from\" and its start time."
                    + " Tell me when to start waddling.");
        }
        if (!hasEnd) {
            return Optional.of("NOOT?! An event is missing \"/to\" and its end time."
                    + " Even penguin meetings eventually end.");
        }
        if (isEndBeforeStart(options)) {
            return Optional.of("NOOT?! Put \"/from\" before \"/to\"."
                    + " Time waddles forward, not backward.");
        }
        if (options.getDescription().isEmpty()) {
            return Optional.of("NOOT?! An event needs a description before \"/from\"."
                    + " Try " + EVENT_EXAMPLE + ".");
        }
        if (options.getValue(CommandOptions.OPTION_FROM).isEmpty()) {
            return Optional.of("NOOT?! \"/from\" needs a start time."
                    + " I cannot waddle in from the void.");
        }
        if (options.getValue(CommandOptions.OPTION_TO).isEmpty()) {
            return Optional.of("NOOT?! \"/to\" needs an end time."
                    + " Even penguin meetings eventually end.");
        }
        return Optional.empty();
    }

    /**
     * Returns whether the user wrote the end marker ahead of the start marker.
     *
     * @param options the options read from the details.
     * @return {@code true} when {@code /to} was typed before {@code /from}.
     */
    private static boolean isEndBeforeStart(CommandOptions options) {
        List<String> optionNames = options.getOptionNames();
        return optionNames.indexOf(CommandOptions.OPTION_TO)
                < optionNames.indexOf(CommandOptions.OPTION_FROM);
    }
}
