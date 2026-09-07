package nori.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import nori.NoriException;

/**
 * Tests event date validation and matching at interval boundaries.
 */
public class EventTest {

    @Test
    public void constructor_endBeforeStart_throwsNoriException() {
        assertThrows(NoriException.class, () ->
                new Event("backwards", "2026-09-03", "2026-09-01"));
    }

    @Test
    public void constructor_sameDate_acceptsEvent() {
        assertDoesNotThrow(() -> new Event("workshop", "2026-09-03 0900", "2026-09-03 1700"));
    }

    @Test
    public void constructor_onlyStartHasDate_acceptsEvent() {
        assertDoesNotThrow(() -> new Event("meeting", "2026-09-03 0900", "later"));
    }

    @Test
    public void occursOn_multiDayEvent_matchesEverySpannedDate() throws NoriException {
        Event event = new Event("conference", "2026-09-01 0900", "2026-09-03 1700");

        assertTrue(event.occursOn(LocalDate.of(2026, 9, 1)));
        assertTrue(event.occursOn(LocalDate.of(2026, 9, 2)));
        assertTrue(event.occursOn(LocalDate.of(2026, 9, 3)));
    }

    @Test
    public void occursOn_dateOutsideEvent_returnsFalse() throws NoriException {
        Event event = new Event("conference", "2026-09-01", "2026-09-03");

        assertFalse(event.occursOn(LocalDate.of(2026, 8, 31)));
        assertFalse(event.occursOn(LocalDate.of(2026, 9, 4)));
    }

    @Test
    public void occursOn_onlyEndHasDate_matchesThatDate() throws NoriException {
        Event event = new Event("release", "after testing", "2026-09-03 1700");

        assertTrue(event.occursOn(LocalDate.of(2026, 9, 3)));
        assertFalse(event.occursOn(LocalDate.of(2026, 9, 2)));
    }

    @Test
    public void occursOn_undatedEvent_returnsFalse() throws NoriException {
        Event event = new Event("lunch", "Monday noon", "1pm");

        assertFalse(event.occursOn(LocalDate.of(2026, 9, 1)));
    }

    @Test
    public void occursInDateRange_eventEnclosesRange_returnsTrue() throws NoriException {
        Event event = new Event("conference", "2026-09-01", "2026-09-10");

        assertTrue(event.occursInDateRange(LocalDate.of(2026, 9, 4), LocalDate.of(2026, 9, 6)));
    }

    @Test
    public void occursInDateRange_adjacentRange_returnsFalse() throws NoriException {
        Event event = new Event("conference", "2026-09-02", "2026-09-03");

        assertFalse(event.occursInDateRange(LocalDate.of(2026, 9, 4), LocalDate.of(2026, 9, 5)));
    }

    @Test
    public void findStartTimeOn_startNamesDateAndTime_returnsThatTime() throws NoriException {
        Event event = new Event("exam", "2026-09-01 0900", "2026-09-01 1200");

        assertEquals(Optional.of(LocalTime.of(9, 0)), event.findStartTimeOn(LocalDate.of(2026, 9, 1)));
    }

    @Test
    public void findStartTimeOn_startNamesDateOnly_returnsEmpty() throws NoriException {
        Event event = new Event("conference", "2026-09-01", "2026-09-03");

        assertTrue(event.findStartTimeOn(LocalDate.of(2026, 9, 1)).isEmpty());
    }

    @Test
    public void findStartTimeOn_dayAfterAMultiDayEventStarts_returnsEmpty() throws NoriException {
        Event event = new Event("conference", "2026-09-01 0900", "2026-09-03 1700");

        assertTrue(event.findStartTimeOn(LocalDate.of(2026, 9, 2)).isEmpty());
    }

    @Test
    public void findStartTimeOn_undatedEvent_returnsEmpty() throws NoriException {
        Event event = new Event("lunch", "Monday 1pm", "2pm");

        assertTrue(event.findStartTimeOn(LocalDate.of(2026, 9, 1)).isEmpty());
    }

    @Test
    public void findStartTimeOn_dateDigitsBesideTheTime_readsOnlyTheTime() throws NoriException {
        Event event = new Event("standup", "2026-09-01 2pm", "2026-09-01 3pm");

        assertEquals(Optional.of(LocalTime.of(14, 0)), event.findStartTimeOn(LocalDate.of(2026, 9, 1)));
    }
}
