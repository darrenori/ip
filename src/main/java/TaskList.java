import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores tasks and performs operations on their task numbers and date queries.
 */
public class TaskList {
    private final List<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        tasks = new ArrayList<>();
    }

    /**
     * Creates a task list containing the supplied tasks.
     *
     * @param tasks the tasks with which to initialize the list
     */
    public TaskList(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task the task to add
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Inserts a task at the specified zero-based index.
     *
     * @param index the index at which to insert the task
     * @param task the task to insert
     */
    public void add(int index, Task task) {
        tasks.add(index, task);
    }

    /**
     * Removes and returns the task at the specified zero-based index.
     *
     * @param index the index of the task to remove
     * @return the removed task
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns the task at the specified zero-based index.
     *
     * @param index the index of the task to return
     * @return the task at the specified index
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Returns a valid zero-based index for a user-supplied task number.
     *
     * @param taskNumber the user-supplied task number
     * @param commandKeyword the command using the task number
     * @return the matching zero-based index
     * @throws NoriException if the task number is unusable
     */
    public int getTaskIndex(String taskNumber, String commandKeyword) throws NoriException {
        try {
            int taskIndex = Integer.parseInt(taskNumber) - 1;
            if (taskIndex >= 0 && taskIndex < size()) {
                return taskIndex;
            }
            throw new NoriException(getTaskNumberError(taskNumber, commandKeyword));
        } catch (NumberFormatException exception) {
            throw new NoriException(getTaskNumberError(taskNumber, commandKeyword));
        }
    }

    /**
     * Returns the task list as display lines.
     *
     * @return the task-list response lines
     */
    public String[] getDisplayLines() {
        if (isEmpty()) {
            return new String[] {"Your list is empty. Add something with \"todo borrow book\" lah."};
        }

        String[] lines = new String[size() + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int index = 0; index < size(); index++) {
            lines[index + 1] = (index + 1) + "." + get(index);
        }
        return lines;
    }

    /**
     * Returns deadline and event task lines that occur on a date.
     *
     * @param date the date to search
     * @return the date-query response lines
     */
    public String[] getTasksOnDateDisplayLines(LocalDate date) {
        List<String> matchingTasks = getMatchingTasks(date, null);
        if (matchingTasks.isEmpty()) {
            return new String[] {"There are no deadlines or events on " + date + "."};
        }
        return prependHeading("Here are the deadlines and events on " + date + ":", matchingTasks);
    }

    /**
     * Returns deadline and event task lines that occur within a date range.
     *
     * @param dateRange the inclusive date range to search
     * @return the date-range response lines
     */
    public String[] getTasksInDateRangeDisplayLines(DateRange dateRange) {
        List<String> matchingTasks = getMatchingTasks(null, dateRange);
        if (matchingTasks.isEmpty()) {
            return new String[] {"There are no deadlines or events from " + dateRange.getFrom()
                    + " to " + dateRange.getTo() + "."};
        }
        return prependHeading("Here are the deadlines and events from " + dateRange.getFrom()
                + " to " + dateRange.getTo() + ":", matchingTasks);
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return the task count
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns whether the list contains no tasks.
     *
     * @return {@code true} if no tasks are stored
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Returns a read-only view of the stored tasks.
     *
     * @return an unmodifiable task list view
     */
    public List<Task> asUnmodifiableList() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Explains why a command's task number cannot be used.
     *
     * @param taskNumber the user-supplied task number
     * @param commandKeyword the command using the task number
     * @return a corrective error message
     */
    private String getTaskNumberError(String taskNumber, String commandKeyword) {
        if (taskNumber.isEmpty()) {
            return "OOPS!!! \"" + commandKeyword + "\" needs a task number. Try \""
                    + commandKeyword + " 1\".";
        }
        try {
            int taskIndex = Integer.parseInt(taskNumber) - 1;
            if (taskIndex < 0) {
                return "OOPS!!! Task numbers start from 1, not " + taskNumber + ". Nice try lah.";
            }
            if (isEmpty()) {
                return "OOPS!!! There are no tasks yet, so there is nothing to " + commandKeyword + ".";
            }
            return "OOPS!!! You have only " + size() + " task(s), so \"" + commandKeyword + " "
                    + taskNumber + "\" is out of range. Don't anyhow point lah.";
        } catch (NumberFormatException exception) {
            if (isIntegerLiteral(taskNumber)) {
                return "ARE YOU DONEEEE????? \"" + taskNumber + "\" is far too large for a task number."
                        + " Use a whole number from 1 to " + Math.max(size(), 1) + ".";
            }
            return "OOPS!!! \"" + taskNumber + "\" is not a task number. Use \""
                    + commandKeyword + " 1\", not words lah.";
        }
    }

    /**
     * Returns whether text is an optionally signed whole-number literal.
     *
     * @param text the text to inspect
     * @return {@code true} if the text contains only an optional sign and digits
     */
    private boolean isIntegerLiteral(String text) {
        int firstDigitIndex = text.startsWith("-") || text.startsWith("+") ? 1 : 0;
        if (firstDigitIndex == text.length()) {
            return false;
        }
        for (int index = firstDigitIndex; index < text.length(); index++) {
            if (!Character.isDigit(text.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds task lines that match either a date or a date range.
     *
     * @param date the date to search, or {@code null} for a range search
     * @param dateRange the range to search, or {@code null} for a date search
     * @return the matching numbered task lines
     */
    private List<String> getMatchingTasks(LocalDate date, DateRange dateRange) {
        List<String> matchingTasks = new ArrayList<>();
        for (int index = 0; index < size(); index++) {
            Task task = get(index);
            boolean matches = date != null ? occursOn(task, date) : occursInDateRange(task, dateRange);
            if (matches) {
                matchingTasks.add((index + 1) + "." + task);
            }
        }
        return matchingTasks;
    }

    /**
     * Adds a heading before task lines.
     *
     * @param heading the response heading
     * @param taskLines the numbered task lines
     * @return the combined response lines
     */
    private String[] prependHeading(String heading, List<String> taskLines) {
        String[] lines = new String[taskLines.size() + 1];
        lines[0] = heading;
        for (int index = 0; index < taskLines.size(); index++) {
            lines[index + 1] = taskLines.get(index);
        }
        return lines;
    }

    /**
     * Returns whether a task is a deadline or event occurring on the given date.
     *
     * @param task the task to inspect
     * @param date the date to match
     * @return {@code true} if the task occurs on {@code date}
     */
    private boolean occursOn(Task task, LocalDate date) {
        if (task instanceof Deadline) {
            return ((Deadline) task).occursOn(date);
        }
        if (task instanceof Event) {
            return ((Event) task).occursOn(date);
        }
        return false;
    }

    /**
     * Returns whether a task is a deadline in, or an event overlapping, a date range.
     *
     * @param task the task to inspect
     * @param dateRange the inclusive date range to match
     * @return {@code true} if the task occurs within the date range
     */
    private boolean occursInDateRange(Task task, DateRange dateRange) {
        if (task instanceof Deadline) {
            return ((Deadline) task).occursInDateRange(dateRange.getFrom(), dateRange.getTo());
        }
        if (task instanceof Event) {
            return ((Event) task).occursInDateRange(dateRange.getFrom(), dateRange.getTo());
        }
        return false;
    }
}
