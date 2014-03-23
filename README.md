sudoku-smasher
==============

#Description

Java Implementation of a sudoku solver. It's very barebones right now, but it solved the hardest sudoku problem in the world easily. 

It takes one argument, which is a 9 line tab separated file representing the puzzle.

This is really just something I wrote this weekend. It isn't very developed at all at this point. But it does work.


#Use Instructions

1. javac src/sudoku_solver.java
2. cd src
3. java SudokuSolver ../puzzles/puzzle1.txt

#To-Do

##Error Checking

1. Currently doesn't check that the file input is a valid puzzle.
2. Exception handling could be more fine grain in general.

##Puzzle Generation:
Currently, Sudoku Smasher can generate a solved sudoku board from an empty board. What's missing from generating sudoku puzzles are:

1. Randomization 
2. Detection of when a given board has a unique solution. 

Neither of these should be hard. 
