package nori.task;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import nori.NoriException;

/**
 * Tests the task list's keyword search and its display lines.
 */
public class TaskListTest {

    @Test
    public void getTasksMatchingKeywordDisplayLines_singleMatch_returnsHeadingAndTask() throws NoriException {
        TaskList tasks = buildSampleList();

        assertArrayEquals(new String[] {
            "Here are the matching tasks in your list:",
            "3.[E][ ] book fair (from: 2019-06-06 1000 to: 2019-06-06 1800)",
        }, tasks.getTasksMatchingKeywordDisplayLines("fair"));
    }

    @Test
    public void getTasksMatchingKeywordDisplayLines_multipleMatches_keepsOriginalTaskNumbers()
            throws NoriException {
        TaskList tasks = buildSampleList();

        assertArrayEquals(new String[] {
            "Here are the matching tasks in your list:",
            "1.[T][X] read book",
            "2.[D][ ] return book (by: Jun 06 2019)",
            "3.[E][ ] book fair (from: 2019-06-06 1000 to: 2019-06-06 1800)",
            "4.[T][ ] Bookshop errand",
        }, tasks.getTasksMatchingKeywordDisplayLines("book"));
    }

    @Test
    public void getTasksMatchingKeywordDisplayLines_differentCase_stillMatches() throws NoriException {
        TaskList tasks = buildSampleList();

        String[] upperCaseResult = tasks.getTasksMatchingKeywordDisplayLines("BOOK");
        String[] mixedCaseResult = tasks.getTasksMatchingKeywordDisplayLines("BoOk");

        assertArrayEquals(tasks.getTasksMatchingKeywordDisplayLines("book"), upperCaseResult);
        assertArrayEquals(tasks.getTasksMatchingKeywordDisplayLines("book"), mixedCaseResult);
    }

    @Test
    public void getTasksMatchingKeywordDisplayLines_matchInsideWord_returnsTask() throws NoriException {
        TaskList tasks = buildSampleList();

        assertArrayEquals(new String[] {
            "Here are the matching tasks in your list:",
            "4.[T][ ] Bookshop errand",
        }, tasks.getTasksMatchingKeywordDisplayLines("shop"));
    }

    @Test
    public void getTasksMatchingKeywordDisplayLines_noMatch_reportsNoMatchingTasks() throws NoriException {
        TaskList tasks = buildSampleList();

        assertArrayEquals(new String[] {"There are no matching tasks in your list."},
                tasks.getTasksMatchingKeywordDisplayLines("bicycle"));
    }

    @Test
    public void getTasksMatchingKeywordDisplayLines_emptyList_reportsNoMatchingTasks() {
        assertArrayEquals(new String[] {"There are no matching tasks in your list."},
                new TaskList().getTasksMatchingKeywordDisplayLines("book"));
    }

    @Test
    public void getTasksMatchingKeywordDisplayLines_dateOutsideDescription_doesNotMatch()
            throws NoriException {
        TaskList tasks = buildSampleList();

        // "2019-06-06" is the deadline's due date and the event's start and end,
        // but it appears in no description, so the description-only search misses it.
        assertArrayEquals(new String[] {"There are no matching tasks in your list."},
                tasks.getTasksMatchingKeywordDisplayLines("2019-06-06"));
    }

    @Test
    public void getTasksMatchingKeywordDisplayLines_emptyKeyword_returnsEveryTask() throws NoriException {
        TaskList tasks = buildSampleList();

        assertEquals(tasks.size() + 1, tasks.getTasksMatchingKeywordDisplayLines("").length);
    }

    @Test
    public void getDisplayLines_emptyList_promptsForATask() {
        assertArrayEquals(new String[] {"Your list is empty. Add something with \"todo borrow book\" lah."},
                new TaskList().getDisplayLines());
    }

    @Test
    public void getTaskIndex_validNumber_returnsZeroBasedIndex() throws NoriException {
        TaskList tasks = buildSampleList();

        assertEquals(0, tasks.getTaskIndex("1", "mark"));
        assertEquals(3, tasks.getTaskIndex("4", "mark"));
    }

    /**
     * Returns a list holding one task of each type, with a completed first task.
     *
     * @return the sample task list used by these tests
     * @throws NoriException if a sample task cannot be created
     */
    private static TaskList buildSampleList() throws NoriException {
        Task readBook = new Todo("read book");
        readBook.markAsDone();
        List<Task> tasks = Arrays.asList(
                readBook,
                new Deadline("return book", LocalDate.of(2019, 6, 6)),
                new Event("book fair", "2019-06-06 1000", "2019-06-06 1800"),
                new Todo("Bookshop errand"),
                new Todo("buy milk"));
        return new TaskList(tasks);
    }
}
