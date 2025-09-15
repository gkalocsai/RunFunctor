## RunFunctor

A modern, Java‑based mini‑Prolog that blends declarative logic with controlled “imperative” steps. If you’re into logic programming, parsers, or backtracking engines, this is for you.

Why it’s cool:

-   Clean syntax: constants in  `"double quotes"`  and  `@Thing@`  literals (allowing spaces, digits, letters).
-   Runnable built‑ins with  `!`  prefix: e.g.,  `!CONCAT("A","B")->*x`,  `!NEW()->id`.
-   Overrideable variables: prefix with  `*`  (like  `*x`) so built‑ins can safely rebind them.
-   Backtracking solver: DFS, unification, variable trailing, and undoable choicepoints.
-   “First solution” mode:  `solveFirst(...)`  stops as soon as it finds a solution.
-   Parser handles escaped quotes and backslashes (" and \).

Tiny examples:

-   Rule:  `F1(B,c) :- F2(D,E,c), F3(AB).`
-   Built‑in:  `!CONCAT("Hello ", Y)->*greet`
-   Thing:  `@user 123@`

Great for:

-   Rapid prototyping with domain rules
-   Built‑in string ops with immediate binding
-   Teaching unification and backtracking mechanics

Want a quick demo? 

Drop a comment on LinkedIn or DM me. #Java #Prolog #DSL #Parsing #Backtracking #LogicProgramming #AI
