# Nori UI Test Plan

This file is the source of truth for Nori console UI tests. Each test case is one
complete program session and must end with `bye`.

## Test 1: Mark and unmark task status

**Aim:** Verify that marking and unmarking tasks updates both the confirmation message and the task list.

### Input
```text
read book
return book
mark 1
mark 2
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
     added: read book
    ____________________________________________________________

    ____________________________________________________________
     added: return book
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [X] read book
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [X] return book
    ____________________________________________________________

    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [ ] return book
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[X] read book
     2.[ ] return book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```
