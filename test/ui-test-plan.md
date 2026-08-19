# Nori UI Test Plan

This file is the source of truth for Nori console UI tests. Each test case is one
complete program session and must end with `bye`.

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

## Test 2: Reject task-number overflow without changing tasks

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

## Test 3: Reject invalid commands without crashing

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
     OOPS!!! I'm sorry, but I don't know what that means :-( Try todo, deadline, event, list, mark, unmark, or bye lah.
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

## Test 4: Preserve task state after interleaved invalid commands

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
