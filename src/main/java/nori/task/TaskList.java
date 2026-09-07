package nori.task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import nori.NoriException;

/**
 * Stores tasks and performs operations on their task numbers and date queries.
 */
public class TaskList {
    /** The tasks, in the order the user added them. */
    private final List<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        tasks = new ArrayList<>();
    }

    /**
     * Creates a task list containing the supplied tasks in argument order.
     *
     * @param tasks the tasks to place in the list.
     */
    public TaskList(Task... tasks) {
        this(List.of(tasks));
    }

    /**
     * Creates a task list containing the supplied tasks.
     *
     * @param tasks the tasks with which to initialize the list.
     */
    public TaskList(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task the task to add.
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Inserts a task at the specified zero-based index.
     *
     * @param index the index at which to insert the task.
     * @param task the task to insert.
     */
    public void add(int index, Task task) {
        tasks.add(index, task);
    }

    /**
     * Removes and returns the task at the specified zero-based index.
     *
     * @param index the index of the task to remove.
     * @return the removed task.
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns the task at the specified zero-based index.
     *
     * @param index the index of the task to return.
     * @return the task at the specified index.
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Returns a valid zero-based index for a user-supplied task number.
     *
     * @param taskNumber the user-supplied task number.
     * @param commandKeyword the command using the task number.
     * @return the matching zero-based index.
     * @throws NoriException if the task number is unusable.
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
     * @return the task-list response lines.
     */
    public String[] getDisplayLines() {
        if (isEmpty()) {
            return new String[] {"The iceberg is empty. Try \"todo borrow book\". Noot noot!"};
        }

        String[] lines = new String[size() + 1];
        lines[0] = "Noot noot! Tasks currently chilling on the iceberg:";
        for (int index = 0; index < size(); index++) {
            lines[index + 1] = (index + 1) + "." + get(index);
        }
        return lines;
    }

    /**
     * Returns deadline and event task lines that occur on a date.
     *
     * @param date the date to search.
     * @return the date-query response lines.
     */
    public String[] getTasksOnDateDisplayLines(LocalDate date) {
        List<String> matchingTasks = getMatchingTasks(date, null);
        if (matchingTasks.isEmpty()) {
            return new String[] {"Nothing is hatching on " + date + ". The ice is quiet."};
        }
        return prependHeading("Noot noot! Things hatching on " + date + ":", matchingTasks);
    }

    /**
     * Returns deadline and event task lines that occur within a date range.
     *
     * @param dateRange the inclusive date range to search.
     * @return the date-range response lines.
     */
    public String[] getTasksInDateRangeDisplayLines(DateRange dateRange) {
        assert dateRange != null : "The parser builds the range before a range listing is requested.";

        List<String> matchingTasks = getMatchingTasks(null, dateRange);
        if (matchingTasks.isEmpty()) {
            return new String[] {"Nothing is hatching from " + dateRange.getFrom()
                    + " to " + dateRange.getTo() + ". The ice is quiet."};
        }
        return prependHeading("Noot noot! Things hatching from " + dateRange.getFrom()
                + " to " + dateRange.getTo() + ":", matchingTasks);
    }

    /**
     * Returns task lines whose descriptions contain a keyword.
     *
     * The search ignores case and matches anywhere in the description, so
     * {@code book} finds both "read book" and "Bookshop trip". Only the
     * description is searched; a deadline's date and an event's start and end
     * details are not. Matching lines keep their numbers from the full list, so
     * a number shown here can be used directly with mark, unmark or delete.
     *
     * @param keyword the text to search for in task descriptions.
     * @return the keyword-search response lines.
     */
    public String[] getTasksMatchingKeywordDisplayLines(String keyword) {
        List<String> matchingTasks = getTasksContainingKeyword(keyword);
        if (matchingTasks.isEmpty()) {
            return new String[] {"No matching fish in this sea. Try another keyword!"};
        }
        return prependHeading("Noot noot! I found these fish:", matchingTasks);
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return the task count.
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns whether the list contains no tasks.
     *
     * @return {@code true} if no tasks are stored.
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Returns a read-only view of the stored tasks.
     *
     * @return an unmodifiable task list view.
     */
    public List<Task> asUnmodifiableList() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Explains why a command's task number cannot be used.
     *
     * @param taskNumber the user-supplied task number.
     * @param commandKeyword the command using the task number.
     * @return a corrective error message.
     */
    private String getTaskNumberError(String taskNumber, String commandKeyword) {
        if (taskNumber.isEmpty()) {
            return "NOOT?! \"" + commandKeyword + "\" needs a task number. Try \""
                    + commandKeyword + " 1\".";
        }
        try {
            int taskIndex = Integer.parseInt(taskNumber) - 1;
            if (taskIndex < 0) {
                return "NOOT?! Task numbers start from 1, not " + taskNumber + ". Penguins can count!";
            }
            if (isEmpty()) {
                return "NOOT?! The iceberg is empty, so there is nothing to " + commandKeyword + ".";
            }
            return "NOOT?! The iceberg has only " + size() + " task(s), so \"" + commandKeyword + " "
                    + taskNumber + "\" points straight into the sea.";
        } catch (NumberFormatException exception) {
            if (isIntegerLiteral(taskNumber)) {
                return "GIANT NOOT! \"" + taskNumber + "\" is far too large for a task number."
                        + " Use a whole number from 1 to " + Math.max(size(), 1) + ".";
            }
            return "NOOT?! \"" + taskNumber + "\" is not a task number. Use \""
                    + commandKeyword + " 1\"; penguins count with digits.";
        }
    }

    /**
     * Returns whether text is an optionally signed whole-number literal.
     *
     * @param text the text to inspect.
     * @return {@code true} if the text contains only an optional sign and digits.
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
     * @param date the date to search, or {@code null} for a range search.
     * @param dateRange the range to search, or {@code null} for a date search.
     * @return the matching numbered task lines.
     */
    private List<String> getMatchingTasks(LocalDate date, DateRange dateRange) {
        assert (date == null) != (dateRange == null)
                : "A search is by single date or by range, never by both and never by neither.";

        List<String> matchingTasks = new ArrayList<>();
        for (int index = 0; index < size(); index++) {
            Task task = get(index);
            boolean isMatch = date != null ? occursOn(task, date) : occursInDateRange(task, dateRange);
            if (isMatch) {
                matchingTasks.add((index + 1) + "." + task);
            }
        }
        return matchingTasks;
    }

    /**
     * Finds numbered task lines whose descriptions contain a keyword.
     *
     * Case folding uses {@link Locale#ROOT} so a search behaves the same way
     * whatever locale the machine running Nori is set to.
     *
     * @param keyword the text to search for in task descriptions.
     * @return the matching numbered task lines.
     */
    private List<String> getTasksContainingKeyword(String keyword) {
        String foldedKeyword = keyword.toLowerCase(Locale.ROOT);
        List<String> matchingTasks = new ArrayList<>();
        for (int index = 0; index < size(); index++) {
            Task task = get(index);
            boolean isMatch = task.getDescription().toLowerCase(Locale.ROOT).contains(foldedKeyword);
            if (isMatch) {
                matchingTasks.add((index + 1) + "." + task);
            }
        }
        return matchingTasks;
    }

    /**
     * Adds a heading before task lines.
     *
     * @param heading the response heading.
     * @param taskLines the numbered task lines.
     * @return the combined response lines.
     */
    private String[] prependHeading(String heading, List<String> taskLines) {
        assert !taskLines.isEmpty() : "A heading is added only when at least one task matched.";

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
     * @param task the task to inspect.
     * @param date the date to match.
     * @return {@code true} if the task occurs on {@code date}.
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
     * @param task the task to inspect.
     * @param dateRange the inclusive date range to match.
     * @return {@code true} if the task occurs within the date range.
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
