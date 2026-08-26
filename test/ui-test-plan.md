# Nori UI Test Plan

This file is the source of truth for Nori console UI tests. Each test case is one
complete program session and must end with `bye`.

## How to run

From the repository root, with Java 25 active:

```powershell
python .codex/skills/test-ui/scripts/run_ui_tests.py test/ui-test-plan.md
```

## Test 10: Save every successful task-list change

**Aim:** Verify that task additions, status changes, and deletion still complete normally while exercising each operation that writes the task list to disk.

### Input
```text
todo pack bag
deadline submit report /by Friday
event team meeting /from Mon 2pm /to 4pm
mark 2
delete 1
unmark 1
bye
```

### Expected output
```text
  _   _  ____  _____  _____ 
 | \ | |/ __ \|  __ \|_   _|
 |  \| | |  | | |__) | | |  
 | . ` | |  | |  _  /  | |  
 | |\  | |__| | | \ \ _| |_ 
 |_| \_|\____/|_|  \_\_____|


____________________________________________________________
Hello! I'm Nori.
What can I do for you?
____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] pack bag
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] submit report (by: Friday)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] team meeting (from: Mon 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [D][X] submit report (by: Friday)
    ____________________________________________________________

    ____________________________________________________________
     Noted. I've removed this task:
       [T][ ] pack bag
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [D][ ] submit report (by: Friday)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

The runner compares output exactly after normalising line endings, so trailing spaces
matter: the first banner line genuinely ends in a space. Every case needs both an
`### Input` and an `### Expected output` block; a case missing either one aborts the
whole run before any test executes.

## Known gaps

* **Startup loading.** The runner isolates every test session's data directory, so it
  cannot cover a task list surviving from one application launch to another. Check this
  manually by add tasks in one session, then run `list` in a new session; the stored
  task types and completion states should be restored.
* **Input ending without `bye`.** The program treats end-of-input as `bye` and exits
  cleanly, but the runner always supplies a `bye`, so this path cannot be expressed as a
  case here. Check it manually with `printf 'todo a\n' | java -cp _temp/ui-test-classes Nori`.
* **A capacity limit.** The task list is an unbounded `ArrayList`, so there is no
  full-list message left to test.

## Test 1: Display task subclasses and prevent repeated status changes

**Aim:** Verify that Todo, Deadline, and Event tasks display correctly in one task list and that repeated marking or unmarking reports the existing state.

### Input
```text
todo borrow book
deadline return book /by Sunday
event project meeting /from Mon 2pm /to 4pm
mark 2
mark 2
unmark 2
unmark 2
list
bye
```

### Expected output
```text
  _   _  ____  _____  _____ 
 | \ | |/ __ \|  __ \|_   _|
 |  \| | |  | | |__) | | |  
 | . ` | |  | |  _  /  | |  
 | |\  | |__| | | \ \ _| |_ 
 |_| \_|\____/|_|  \_\_____|


____________________________________________________________
Hello! I'm Nori.
What can I do for you?
____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] borrow book
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: Sunday)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Mon 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [D][X] return book (by: Sunday)
    ____________________________________________________________

    ____________________________________________________________
     Yo! You've already marked this task.
    ____________________________________________________________

    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [D][ ] return book (by: Sunday)
    ____________________________________________________________

    ____________________________________________________________
     Yo! You've already unmarked this task.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] borrow book
     2.[D][ ] return book (by: Sunday)
     3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

## Test 2: Delete a task and renumber the list

**Aim:** Verify that delete removes the selected task, reports it, and shifts the remaining tasks down, so that later commands addressing the new task numbers reach the intended tasks.

**Covers:** removal and renumbering, including the old last position becoming out of range.

### Input
```text
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
todo join sports club
todo borrow book
mark 1
mark 2
mark 4
list
delete 3
list
unmark 3
mark 4
mark 5
list
bye
```

### Expected output
```text
  _   _  ____  _____  _____ 
 | \ | |/ __ \|  __ \|_   _|
 |  \| | |  | | |__) | | |  
 | . ` | |  | |  _  /  | |  
 | |\  | |__| | | \ \ _| |_ 
 |_| \_|\____/|_|  \_\_____|


____________________________________________________________
Hello! I'm Nori.
What can I do for you?
____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] join sports club
     Now you have 4 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] borrow book
     Now you have 5 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] read book
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [D][X] return book (by: June 6th)
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] join sports club
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][X] return book (by: June 6th)
     3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     4.[T][X] join sports club
     5.[T][ ] borrow book
    ____________________________________________________________

    ____________________________________________________________
     Noted. I've removed this task:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 4 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][X] return book (by: June 6th)
     3.[T][X] join sports club
     4.[T][ ] borrow book
    ____________________________________________________________

    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [T][ ] join sports club
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] borrow book
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! You have only 4 task(s), so "mark 5" is out of range. Don't anyhow point lah.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][X] return book (by: June 6th)
     3.[T][ ] join sports club
     4.[T][X] borrow book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

## Test 3: Reject task-number overflow without changing tasks

**Aim:** Verify that an integer-overflow task number receives the overflow message and does not alter the existing list.

### Input
```text
todo read book
mark 999999999999999999999999
list
bye
```

### Expected output
```text
  _   _  ____  _____  _____ 
 | \ | |/ __ \|  __ \|_   _|
 |  \| | |  | | |__) | | |  
 | . ` | |  | |  _  /  | |  
 | |\  | |__| | | \ \ _| |_ 
 |_| \_|\____/|_|  \_\_____|


____________________________________________________________
Hello! I'm Nori.
What can I do for you?
____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     ARE YOU DONEEEE????? "999999999999999999999999" is far too large for a task number. Use a whole number from 1 to 1.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

## Test 4: Reject invalid commands without crashing

**Aim:** Verify that incomplete commands, invalid task numbers, and unknown commands produce helpful error messages instead of ending the program.

### Input
```text
todo
deadline do homework
event project meeting /from Monday
mark eat food
unmark 1
blah
bye
```

### Expected output
```text
  _   _  ____  _____  _____ 
 | \ | |/ __ \|  __ \|_   _|
 |  \| | |  | | |__) | | |  
 | . ` | |  | |  _  /  | |  
 | |\  | |__| | | \ \ _| |_ 
 |_| \_|\____/|_|  \_\_____|


____________________________________________________________
Hello! I'm Nori.
What can I do for you?
____________________________________________________________

    ____________________________________________________________
     OOPS!!! A todo needs a description. Try "todo borrow book" — I cannot read your mind lah.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! I cannot find the "/by" part of that deadline. Use "deadline submit report /by Friday".
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! An event is missing "/to" and its end time. I need to know when you escape the meeting leh.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! "eat food" is not a task number. Use "mark 1", not words lah.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! There are no tasks yet, so there is nothing to unmark.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! I'm sorry, but I don't know what that means :-( Try todo, deadline, event, list, mark, unmark, delete, or bye lah.
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

## Test 5: Preserve task state after interleaved invalid commands

**Aim:** Verify that rejected commands neither add tasks nor change task statuses when mixed between valid task operations.

### Input
```text
todo
todo finish lab
mark 2
mark 1
deadline /by Friday
deadline submit report /by Friday
event project meeting /to 4pm /from Mon 2pm
event project meeting /from Mon 2pm /to 4pm
unmark 4
unmark 1
list
bye
```

### Expected output
```text
  _   _  ____  _____  _____ 
 | \ | |/ __ \|  __ \|_   _|
 |  \| | |  | | |__) | | |  
 | . ` | |  | |  _  /  | |  
 | |\  | |__| | | \ \ _| |_ 
 |_| \_|\____/|_|  \_\_____|


____________________________________________________________
Hello! I'm Nori.
What can I do for you?
____________________________________________________________

    ____________________________________________________________
     OOPS!!! A todo needs a description. Try "todo borrow book" — I cannot read your mind lah.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] finish lab
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! You have only 1 task(s), so "mark 2" is out of range. Don't anyhow point lah.
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] finish lab
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! A deadline needs a description before "/by". Try "deadline submit report /by Friday".
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] submit report (by: Friday)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! Put "/from" before "/to". Time flows forward, not backwards, sia.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Mon 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! You have only 3 task(s), so "unmark 4" is out of range. Don't anyhow point lah.
    ____________________________________________________________

    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [T][ ] finish lab
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] finish lab
     2.[D][ ] submit report (by: Friday)
     3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

## Test 6: Delete at both ends of the list and empty it

**Aim:** Verify that delete works at the first and last positions and that a fully emptied list says so instead of printing a heading with nothing under it.

**Covers:** delete at the first, last, and last-remaining positions, and the empty-list branch of `formatTaskList`.

### Input
```text
todo alpha
todo beta
todo charlie
delete 1
list
delete 2
list
delete 1
list
bye
```

### Expected output
```text
  _   _  ____  _____  _____ 
 | \ | |/ __ \|  __ \|_   _|
 |  \| | |  | | |__) | | |  
 | . ` | |  | |  _  /  | |  
 | |\  | |__| | | \ \ _| |_ 
 |_| \_|\____/|_|  \_\_____|


____________________________________________________________
Hello! I'm Nori.
What can I do for you?
____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] alpha
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] beta
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] charlie
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Noted. I've removed this task:
       [T][ ] alpha
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] beta
     2.[T][ ] charlie
    ____________________________________________________________

    ____________________________________________________________
     Noted. I've removed this task:
       [T][ ] charlie
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] beta
    ____________________________________________________________

    ____________________________________________________________
     Noted. I've removed this task:
       [T][ ] beta
     Now you have 0 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Your list is empty. Add something with "todo borrow book" lah.
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

## Test 7: Reject task numbers that are missing, zero, negative, or out of range

**Aim:** Verify that mark, unmark, and delete each explain exactly why a task number is unusable, and that no such command changes the list.

**Covers:** Every branch of `getTaskNumberError` except the overflow branch, which Test 3 covers.

### Input
```text
mark 1
delete 1
todo alpha
mark
mark 0
mark -3
unmark 2
delete 7
list
bye
```

### Expected output
```text
  _   _  ____  _____  _____ 
 | \ | |/ __ \|  __ \|_   _|
 |  \| | |  | | |__) | | |  
 | . ` | |  | |  _  /  | |  
 | |\  | |__| | | \ \ _| |_ 
 |_| \_|\____/|_|  \_\_____|


____________________________________________________________
Hello! I'm Nori.
What can I do for you?
____________________________________________________________

    ____________________________________________________________
     OOPS!!! There are no tasks yet, so there is nothing to mark.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! There are no tasks yet, so there is nothing to delete.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] alpha
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! "mark" needs a task number. Try "mark 1".
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! Task numbers start from 1, not 0. Nice try lah.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! Task numbers start from 1, not -3. Nice try lah.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! You have only 1 task(s), so "unmark 2" is out of range. Don't anyhow point lah.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! You have only 1 task(s), so "delete 7" is out of range. Don't anyhow point lah.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] alpha
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

## Test 8: Report each missing part of a deadline or event

**Aim:** Verify that a deadline or event missing a description, a date, or a separator names the part that is missing, and that none of them is stored.

**Covers:** The `/by`-without-a-date branch, and the both-separators-missing, description-before-`/from`, and missing-`/from` event branches.

### Input
```text
deadline submit report /by
event team meeting
event /from Mon 2pm /to 4pm
event team meeting /to 4pm
list
bye
```

### Expected output
```text
  _   _  ____  _____  _____ 
 | \ | |/ __ \|  __ \|_   _|
 |  \| | |  | | |__) | | |  
 | . ` | |  | |  _  /  | |  
 | |\  | |__| | | \ \ _| |_ 
 |_| \_|\____/|_|  \_\_____|


____________________________________________________________
Hello! I'm Nori.
What can I do for you?
____________________________________________________________

    ____________________________________________________________
     OOPS!!! A deadline needs a due date or time after "/by". Try "deadline submit report /by Friday".
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! An event needs both "/from" and "/to". Use "event team meeting /from Mon 2pm /to 4pm".
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! An event needs a description before "/from". Try "event team meeting /from Mon 2pm /to 4pm".
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! An event is missing "/from" and its start time. Put "/from" before "/to", can?
    ____________________________________________________________

    ____________________________________________________________
     Your list is empty. Add something with "todo borrow book" lah.
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

## Test 9: Tolerate stray whitespace but reject near-miss commands

**Aim:** Verify that surrounding whitespace never turns a valid command into an unknown one, while blank input, wrong capitalisation, and words that merely start with a command keyword are still rejected.

**Covers:** Input trimming in the main loop and the keyword-boundary check in `isCommand`.

### Input
```text

TODO read book
todos read book
listen
marker 1
todo    read book   
   list   
bye
```

### Expected output
```text
  _   _  ____  _____  _____ 
 | \ | |/ __ \|  __ \|_   _|
 |  \| | |  | | |__) | | |  
 | . ` | |  | |  _  /  | |  
 | |\  | |__| | | \ \ _| |_ 
 |_| \_|\____/|_|  \_\_____|


____________________________________________________________
Hello! I'm Nori.
What can I do for you?
____________________________________________________________

    ____________________________________________________________
     OOPS!!! I'm sorry, but I don't know what that means :-( Try todo, deadline, event, list, mark, unmark, delete, or bye lah.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! I'm sorry, but I don't know what that means :-( Try todo, deadline, event, list, mark, unmark, delete, or bye lah.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! I'm sorry, but I don't know what that means :-( Try todo, deadline, event, list, mark, unmark, delete, or bye lah.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! I'm sorry, but I don't know what that means :-( Try todo, deadline, event, list, mark, unmark, delete, or bye lah.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! I'm sorry, but I don't know what that means :-( Try todo, deadline, event, list, mark, unmark, delete, or bye lah.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```
