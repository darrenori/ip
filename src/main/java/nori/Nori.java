package nori;

import nori.command.Command;
import nori.command.Parser;
import nori.storage.Storage;
import nori.task.TaskList;
import nori.ui.Ui;

/**
 * Coordinates Nori's user interface, task list, command parser, and storage.
 */
public class Nori {
    private final Storage storage;
    private final TaskList tasks;
    private final Ui ui;
    private final String loadingError;
    private final String loadingNotice;

    /**
     * Creates Nori with its standard console user interface and disk storage.
     */
    public Nori() {
        ui = new Ui();
        storage = new Storage();

        TaskList loadedTasks;
        String savedLoadingError = null;
        String savedLoadingNotice = null;
        try {
            loadedTasks = new TaskList(storage.loadTasks());
            savedLoadingNotice = storage.getLoadingNotice();
        } catch (NoriException exception) {
            savedLoadingError = exception.getMessage();
            loadedTasks = new TaskList();
        }
        tasks = loadedTasks;
        loadingError = savedLoadingError;
        loadingNotice = savedLoadingNotice;
    }

    /**
     * Runs Nori until the user exits or standard input ends.
     */
    public void run() {
        ui.showWelcome();
        showLoadingMessage();

        String input;
        while ((input = ui.readCommand()) != null) {
            try {
                Command command = Parser.parse(input);
                command.execute(tasks, ui, storage);
                if (command.isExit()) {
                    break;
                }
            } catch (NoriException exception) {
                ui.showResponse(exception.getMessage());
            }
        }
        ui.showResponse("Bye. Hope to see you again soon!");
        ui.close();
    }

    /**
     * Displays the result of loading saved tasks, if one was produced.
     */
    private void showLoadingMessage() {
        if (loadingError != null) {
            ui.showLoadingError(loadingError);
        } else if (loadingNotice != null) {
            ui.showLoadingNotice(loadingNotice);
        }
    }

    /**
     * Starts Nori with its standard configuration.
     *
     * @param args unused command-line arguments
     */
    public static void main(String[] args) {
        new Nori().run();
    }
}
