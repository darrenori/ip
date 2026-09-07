# Nori User Guide

// Update the title above to match the actual product name

// Product screenshot goes here

// Product intro goes here

## Adding deadlines

// Describe the action and its outcome.

// Give examples of usage

Example: `keyword (optional arguments)`

// A description of the expected outcome goes here

```
expected output
```

## Viewing a schedule

Lays one date out in the order it happens. The events that start at a known time
on that date come first, earliest first; below them come the events that run over
the date without a time of their own, then the deadlines falling due, then your
to-dos, which belong to no particular day. A section with nothing in it is left
out.

Nori reads an event's start time out of the `/from` text you already type, so put
one there. `0900`, `9:30`, `9.30`, `2pm`, `2:30 PM` and `9 a.m.` are all
understood. An event with a date but no time still appears, under **All day**.

The numbers are the ones `list` uses, so a number read off a schedule can be
given straight to `mark`, `unmark` or `delete`.

Example: `schedule 2019-06-06`

```
Noot noot! Schedule for 2019-06-06:
09:00 3.[E][ ] exam (from: 2019-06-06 0900 to: 2019-06-06 1200)
10:00 5.[E][ ] book fair (from: 2019-06-06 1000 to: 1800)
All day:
      4.[E][ ] conference (from: 2019-06-06 to: 2019-06-08)
Due:
      2.[D][ ] return book (by: Jun 06 2019)
Anytime:
      1.[T][ ] read book
```

Leave the date out to see today instead.

Example: `schedule`

A date Nori cannot read is reported rather than guessed at.

Example: `schedule tomorrow`

```
NOOT?! I cannot understand "tomorrow" as a date. Use a date like "2019-10-15".
```

## Feature ABC

// Feature details


## Feature XYZ

// Feature details