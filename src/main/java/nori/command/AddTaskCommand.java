package nori.command;

import nori.NoriException;
import nori.storage.Storage;
import nori.task.Task;
import nori.task.TaskList;
import nori.ui.Ui;

/**
 * Provides the shared step that adds a task, saves it, and confirms it.
 */
abstract class AddTaskCommand extends InputCommand {
    /**
     * Creates a task-adding command with its input details.
     *
     * @param details the text after the command keyword.
     */
    AddTaskCommand(String details) {
        super(details);
    }

    /**
     * Adds a task, restores the list when saving fails, and displays confirmation.
     *
     * @param tasks the task list to change.
     * @param ui the console user interface.
     * @param storage the persistent task storage.
     * @param task the task to add.
     * @throws NoriException if the task cannot be saved.
     */
    protected void addTask(TaskList tasks, Ui ui, Storage storage, Task task) throws NoriException {
        tasks.add(task);
        try {
            storage.saveTasks(tasks.asUnmodifiableList());
        } catch (NoriException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
        ui.showResponse("Noot noot! Task tucked safely under my wing:", "  " + task,
                "The iceberg now holds " + tasks.size() + " task(s).");
    }
}
