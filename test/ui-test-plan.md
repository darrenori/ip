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
