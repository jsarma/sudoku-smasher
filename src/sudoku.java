/**************************
Author:				Justin Sarma
Project:			Sudoku Solver
Date: 				3/22/2014
Description: 	Solve a sudoku puzzle, which is passed in as a tab separated file with 9 lines.
Status:				
	Currently, has only been tested on 4 boards. 
	It solved the world's hardest sudoku (puzzle3) easily.  
	The 4th puzzle is a blank board, which generated a valid sudoku puzzle solution.
Game Rules:		
	The unique valid solution to a sudoku puzzle must have unique numbers from 1-9 in each row,
	each column, and each 3X3 square. 
	See this site for details: http://en.wikipedia.org/wiki/Sudoku 
***************************/

import java.util.*;
import java.io.*;

class SudokuSolver {
	//MAIN FUNCTION: Takes 1 argument which is a file name containing 9 tab separated lines.	
	public static void main(String [] args) {
		if (args.length!=1) {
			System.out.println("Invalid argument count.");
			return;
		}

		//read from file and set up
		SudokuInstance root = new SudokuInstance(args[0]); 

		//make a copy, and solve on the copy recursively on child
		SudokuInstance child = new SudokuInstance(root); 

		//pass in a recursion depth of 0. Used to prevent stack overflow.
		child.solve(0); 
	}

}


class SudokuInstance {

	//STATIC MEMBER DATA: 

	//count number of times we copy the board for performance evaluation purposes
	static int num_tries = 0;

	//keep track of max recursion depth for performance evaluation
	static int max_depth = 0;


	//MEMBER DATA:
	//all sudoku puzzles are 9X9
	final int NUMROWS = 9;
	final int NUMCOLS = 9; 

	//All puzzle data is stored in a 9X9 array of cells
	SudokuCell [][] data;


	//MEMBER FUNCTIONS:

	//Constructor
	//Read puzzle from file and use to populate sudoku board with initial values 1-9.
	//value=0 means the cell has not yet been solved.
	public SudokuInstance(String file_name) {
		data = new SudokuCell[NUMROWS][NUMCOLS];
		try {
			BufferedReader br = new BufferedReader(new FileReader(file_name));
			int i = 0;
			String line = br.readLine(); 
			while (line!=null) {
				if (!line.startsWith("#")) {
					String [] a = line.split("\t");
					Integer [] b = new Integer[a.length];
					for (int j=0; j < a.length; j++) { //convert
						data[i][j] = new SudokuCell();
						data[i][j].value = Integer.parseInt(a[j]); //the current value in the cell
						data[i][j].row = i;
						data[i][j].col = j;
					}
					i++;
				}
				line = br.readLine();
			}
			fill_options(); //calculate all possibilities for each cell given constraints of sudoku problem.
			print_data();
			print_options();
		}
		catch (Exception e) { //TODO: add more detailed exception handling.
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);		
		}
	}

	//Copy Constructor: Each recursion gets a new deep copy of the board to update.
	public SudokuInstance(SudokuInstance other) {
		SudokuInstance.num_tries++; //count number of board copies for performance eval
		System.out.println("Copy Constructor Count: " + SudokuInstance.num_tries);
		this.data = new SudokuCell[NUMROWS][NUMCOLS];

		for (int i=0; i<NUMROWS; i++) {
			for (int j=0; j<NUMCOLS; j++) {
				this.data[i][j] = new SudokuCell();
				this.data[i][j].value = other.data[i][j].value;
				this.data[i][j].options = new HashSet<Integer>();
				for (Integer value : other.data[i][j].options) {
					this.data[i][j].options.add(value);
				}
				this.data[i][j].row = i;
				this.data[i][j].col = j;
				this.data[i][j].dirty = other.data[i][j].dirty;
			}
		}
	}

	//fill_options: Fill all possible options for all cells
	public void fill_options() {
		for (int i=0; i<NUMROWS; i++) {
			for (int j=0; j<NUMCOLS; j++) {
				if (data[i][j].value==0) {
					fill_single_option(i, j);
				}
			}
		}
	}

	//fill_single_option: Find all possible options for a given cell based on neighbors
	public void fill_single_option(int row, int col) { 
		//Start with full options set (1-9). We will then widdle it down.
		for (int i=0; i<NUMROWS; i++) {
			(data[row][col].options).add((Integer)(i+1));
		}

		//If the COLUMN already has a given digit, remove it from option set
		for (int i=0; i<NUMROWS; i++) {
			if (data[i][col].value>0) {
				(data[row][col].options).remove(data[i][col].value);
			}
		}

		//If the ROW already has a given digit, remove it from option set
		for (int j=0; j<NUMCOLS; j++) {
			if (data[row][j].value>0) {
				(data[row][col].options).remove(data[row][j].value);
			}
		}

		//If the 3X3 square already has a given digit, remove it from the option set
		//Example squares: (0-3,0-3), (3-6,0,3), etc...
		int top = ((row / 3) * 3);
		int bottom = top + 3;
		int left = ((col / 3) * 3);
		int right = left + 3;
		for (int i=top; i<bottom; i++) {
			for (int j=left; j<right; j++) {
				//if (i==row && j==col) continue; //skip self - not needed, because self has value=0
				if (data[i][j].value>0) { //don't look at things with value=0
					(data[row][col].options).remove(data[i][j].value); //remove match
				}				
			}
		} 
	}

	/*solve: recursive solver
	**return value: false=impossible solution. true=found a valid solution. Go home.
	**Algorithm: Solve cells in order of their option count. If only 1 option. No recursion required.
	**If N, options, we recurse with N branches, one for each possibility.
	**Max recursion depth will never be more than the number of blank cells in the original problem.
	**Normal recursion depth is much less, because many squares have only 1 option.
	*/
	public boolean solve(int depth) {
		int count_remaining = this.count_remaining(); //count how many cells haven't been solved yet.
		System.out.println("solve: depth=" + depth + " remaining:" + count_remaining);

		if (depth > SudokuInstance.max_depth) SudokuInstance.max_depth = depth; //keep track of max depth

		if (depth>70) { //Don't let the recursion go deeper than 81. Shouldn't ever happen unless there's a bug.
			System.out.println("MI5 says we're in way too deep. Abort mission.");
			return false;
		}

		//print the current board with latest changes *'ed.
		this.print_data();
		//this.print_options(); //print all possible options for each unfilled square.

		//after print, remove dirty bit, so old changes don't show up in the new iteration.
		this.remove_dirty();

		//Now make progress on solution...


		//Shallow copy cells to 1D array and sort cells by option count
		//We do this so we can make our guesses in order of difficulty.
		SudokuCell [] sorted_data = new SudokuCell[NUMROWS * NUMCOLS];
		int count = 0;
		for (int i=0; i<NUMROWS; i++) {
			for (int j=0; j<NUMCOLS; j++) {
				sorted_data[count] = this.data[i][j]; //shallow copy
				count++;
			}
		}
		Arrays.sort(sorted_data);

		//Now solve cells in order of the number of valid possibilities. 
		//If there's more than one valid possibility for a cell, recurse on each guess.
		for (int i=0; i < sorted_data.length; i++) {
			//System.out.println("sorted_data[" + i + "] size:" + sorted_data[i].options.size());
			if (sorted_data[i].value>0) continue; //ignore cells we've already solved
			Integer row = sorted_data[i].row;
			Integer col = sorted_data[i].col;
			Iterator<Integer> option_itr = sorted_data[i].options.iterator(); //iterate through valid possibilities.
			Integer num_options = sorted_data[i].options.size();
			SudokuCell cell = this.data[row][col];
			Integer new_value;
			switch (num_options) { //Number of possibile options for a cell.
				case 0: //no solution possible if there are no options for any cell, so return false
					System.out.println("No solution. Dead End. Return to base because of cell: " + cell.state_string());
					return false; 					
				case 1: //only one possibility, so we choose it and continue...
					new_value = option_itr.next();
					System.out.println("ACTION: Option Count 1: Setting " + cell.state_string() + " to " + new_value);
					cell.value = new_value;
					cell.dirty = true;
					break;
				default:
					//expensive recursion case: Here, we try all possibilities until one leads to a solution.
					//if none leads to a solution, return up the tree to find a solution in another branch.
					do { 
						new_value = option_itr.next(); //current "guess"
						//Copy board, make a "guess", and solve on the copy recursively on child
						SudokuInstance child = new SudokuInstance(this); 
						SudokuCell child_cell = child.data[row][col];
						System.out.println("ACTION: Option Count " + num_options + ": Setting " + child_cell.state_string() + " to " + new_value);
						child_cell.value = new_value;
						child_cell.dirty = true;

						child.fill_options(); //need to update the options list to account for new values

						//recursion magic. If a solution is found, true is returned, causing us to exit
						if (child.solve(depth+1)) return true;
					}
					while (option_itr.hasNext()); //Oh well. Try another guess.
					break;
			}
		}

		//this.print_data(); //print current board.
		//this.print_options();

		count_remaining = this.count_remaining(); //count how many cells haven't been solved yet.
		if (count_remaining==0) {
			this.print_data(); //print current board, (THE SOLUTION)
			System.out.println("Max recursion depth: " + SudokuInstance.max_depth);
			System.out.println("Board Copy Count: " + SudokuInstance.num_tries);
			System.out.println("Mission Succeeded. Return to base. Get laid, etc...");			
			//System.exit(0);
			return true;
		}
		else {
			System.out.println("Returning from branch without solution.");
		}

		return false;

	}

	//HELPER FUNCTIONS

	//print_data: Board print function
	public void print_data() {
		System.out.println("Current Board:");
		for (int i=0; i<NUMROWS; i++) {
			for (int j=0; j<NUMCOLS; j++) {
				System.out.print(data[i][j].print_value() + "\t");
			}
			System.out.println();
		}
	}

	//print_options: For each cell, print the array of possible options.
	public void print_options() {
		System.out.println("Current Valid Options:");
		for (int i=0; i<NUMROWS; i++) {
			for (int j=0; j<NUMCOLS; j++) {
				if (data[i][j].options!=null) {
					System.out.println(i + "," + j + ": " + data[i][j].options.toString());
				}
			}
		}
	}

	//remove_dirty: Remove the dirty bit from all cells.
	public void remove_dirty() {
		for (int i=0; i<NUMROWS; i++) {
			for (int j=0; j<NUMCOLS; j++) {
				data[i][j].dirty = false;
			}
		}
	}

	//count_remaining: Count the number of unsolved cells. returns 0 when the puzzle is solved.
	public int count_remaining() {
		int count = 0;
		for (int i=0; i<NUMROWS; i++) {
			for (int j=0; j<NUMCOLS; j++) {
				if (data[i][j].value==0) count++;
			}
		}
		return count;
	}




	//SudokuCell: Class representing each cell.
	//Comparable: Can be sorted by the number of valid possibilities.
	//SudokuInstance currently treats this class like a struct, accessing its data directly.
	class SudokuCell implements Comparable<SudokuCell> {
		public Integer row;
		public Integer col;
		public Integer value;
		public boolean dirty;
		public HashSet<Integer> options;

		//dummy constructor. 
		SudokuCell() {
			value = 0;
			options = new HashSet<Integer>();
			row = -1;
			col = -1;
			dirty = false;
		}

		//compareTo: It's important to be able to sort the cells by difficulty (eg: num options)
		public int compareTo(SudokuCell other) {
			if (this.options.size() < other.options.size()) return -1;
			else if (this.options.size() == other.options.size()) return 0;
			else return 1;


		}

		//return representation of the cell for printing purposes.
		public String state_string() {
			return "(" + row + ", " + col + "): " + value + (dirty ? "*" : "");
		}

		//return cell value string, with dirty marker possibly appended.
		public String print_value() {
			return value.toString() + (dirty ? "*" : "");
		}

	}

}