# Project context

This repository is a starter template for a greenfield Java project used in an introductory software engineering course in an undergraduate computer science program. Students use it as the starting point for their own projects.

# Default user context

Unless the user says otherwise, assume that you are assisting a student working on a project in this repository. If the user identifies themselves as an instructor or another project stakeholder, adapt your response to that role.

# Student profile

* Prior knowledge: Basic Java and OOP concepts.
* Level of programming experience: Advanced
* IDE and level of expertise: VSC, Advanced

# Guidance for interacting with users

* Explain the rationale for significant actions: what you did and why.
* Keep explanations brief but instructive, supporting learning through responsible use of AI. For example:

  * When suggesting a Git command, briefly explain what it does.
  * Add explanatory Javadoc comments to all classes and to nontrivial methods and fields when their purpose or behavior is not obvious.
  * Make generated code as self-explanatory as possible, and include explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is sufficient for the requirements, while briefly explaining relevant more advanced alternatives.

# Project-specific requirements

## Java version:

Ensure that Java 25 is used when running the application or build tasks. On macOS, use `sdk use java 25.0.3.fx-zulu` to switch to Java 25 if needed.

## Java coding standard

All Java code in this project, in `src/` and `test/` alike, must follow the SE-EDU
Java coding standard at basic and intermediate level
(<https://se-education.org/guides/conventions/java/intermediate.html>).

Invoke the project-specific `seedu-java-coding-standard` skill before writing,
editing, or reviewing any Java file, and follow it for all new code. This is
mandatory, not advisory: a change that does not comply is not finished.

Non-compliant code found while working on something else may be corrected in the
same change when the fix is small and in scope; otherwise leave it and say so.

## UI testing after code changes

After each code update, complete both steps before handing the work back to the user:

1. Review `test/ui-test-plan.md` and update it when the change affects UI behaviour, coverage, inputs, or expected output. Do not rewrite expected output merely to conceal a regression.
2. Invoke the project-specific `$test-ui` skill to run the UI test plan. If a case fails, report its recorded input plus expected and actual output; the skill stops the test session at the first failure.

## JUnit testing after code changes

Maintain JUnit tests for approximately the top 50% of methods by testing value. Prioritize complex, core, and
critical business logic over trivial accessors and boilerplate. After every code change, review the affected JUnit
tests and update or extend them as needed to continue meeting this target. Run the complete JUnit test suite before
handing the work back to the user.

## Git

Use lightweight tags unless the user requests an annotated tag.
When proposing or creating a commit message, include enough detail to explain the rationale for the change.
Do not commit or push unless explicitly asked.
