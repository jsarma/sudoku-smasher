sudoku-smasher
==============

Java Implementation of a sudoku solver. It's very barebones right now, but it solved the hardest sudoku problem in the world easily. 

It takes one argument, which is a 9 line tab separated file representing the puzzle.

This is really just something I wrote this weekend. It isn't very developed at all at this point. But it does work.

I'd like to tweak it so that it can generate sudoku puzzles. It can generate a solved sudoku board from an empty board. What's missing from generating sudoku puzzles is 1) Randomization 2) Detection of when a given board has a unique solution. Neither of these should be hard. 

USE INSTRUCTIONS:

1) javac src/sudoku_solver.java
2) cd src
3) java SudokuSolver ../puzzles/puzzle1.txt
