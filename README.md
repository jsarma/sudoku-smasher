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

##Puzzle Generation:
Currently, Sudoku Smasher can generate a solved sudoku board from an empty board. What's missing from generating sudoku puzzles are:
1. Randomization 
2. Detection of when a given board has a unique solution. 
3. 
4. Neither of these should be hard. 
