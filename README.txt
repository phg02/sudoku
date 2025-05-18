# RMIT Sudoku Solver
## Group 6 - Algorithm and Analysis

## Team Members and Contributions

| Member Name          | Contribution |
|----------------------|--------------|
| Nguyen Minh Phuong   | 22.5%        |
| Dao Thao Phuong      | 22.5%        |
| Pham Trung Nghia     | 22.5%        |
| Victor Smith         | 10%          |

## Project Presentation
[Link to Group 6 Presentation](https://rmit.instructure.com/courses/123456/assignments/1234567)

A Java implementation of a Sudoku solver that compares three different solving algorithms: Bit Manipulation, Dancing Links (DLX), and Basic Backtracking. The project demonstrates the performance differences between these approaches in terms of speed and memory usage.

## Features

- Three solving algorithms:
  - Bit Manipulation
  - Dancing Links (DLX)
  - Basic Backtracking
- Performance comparison metrics
- Support for different difficulty levels
- Detailed timing and memory usage analysis


### Testing with Different Sudokus

We define difficulty based on the number of pre-filled clues (known values in the 9×9 grid):

| Difficulty | Clues    | Description                                    |
|------------|----------|------------------------------------------------|
| Easy       | ≥ 36     | Mostly filled, direct logic                    |
| Medium     | 28–35    | Balanced mix of logic and trial                |
| Hard       | ≤ 27     | Requires deeper recursion and pruning          |
| Extreme    | ≤ 20     | More complex grid, significant branching       |


## Changing Difficulty Levels

To change the difficulty level of puzzles, modify line 476 in RMIT_Sudoku_Solver.java:

```java
String SudokuDataSet = "extreme.txt"; // Change to:
// "easy.txt"
// "medium.txt"
// "hard.txt"
// "extreme.txt"
```

## Using the Solver

The solver can be used with any of the three methods. The solve method takes two parameters:

1. `puzzle`: A 9x9 2D integer array representing the Sudoku puzzle
   - Use 0 for empty cells
   - Use 1-9 for filled cells
   - Example: `int[][] puzzle = new int[9][9];`

2. `method`: A String specifying the solving algorithm
   - "recursiveBacktracking": Basic backtracking algorithm
   - "bitManipulation": Bit manipulation approach
   - "dancingLinks": Dancing Links (DLX) algorithm

```java
// Example usage:
int[][] puzzle = new int[9][9]; // Your 9x9 Sudoku puzzle
int[][] result = solver.solve(puzzle, "recursiveBacktracking");
// or
int[][] result = solver.solve(puzzle, "bitManipulation");
// or
int[][] result = solver.solve(puzzle, "dancingLinks");

// To display the solved puzzle:
printBoard(result);
```

## Comparing Solvers

To compare the performance of all three solvers simultaneously, use the `solveAndCompare()` method:

```java
// Example usage:
int[][] puzzle = new int[9][9]; // Your 9x9 Sudoku puzzle
SolverResult result = solver.solveAndCompare(puzzle);
```

The `solveAndCompare()` method returns a `SolverResult` object containing:
- Solution grid
- Time taken for each method
- Number of recursive calls for each method
- Memory usage for each method
- Success status for each method

This is useful for:
- Performance benchmarking
- Algorithm comparison
- Memory usage analysis
- Identifying the most efficient solver for specific puzzle types

## Testing and Output

The program provides comprehensive performance analysis:

1. **Individual Puzzle Results**
   - Shows the solved puzzle for each method
   - Displays time taken in nanoseconds
   - Shows memory usage in bytes

2. **Comparison Table**
   - Lists each puzzle with timing and call counts for all three methods
   - Format:
     ```
     Puzzle | Bit Manipulation    | Dancing Links (DLX)  | Basic Backtracking
     -------|-------------------|---------------------|------------------
     #      | Time(ms) Calls    | Time(ms) Calls      | Time(ms) Calls
     ```

3. **Detailed Summary**
   - Average time for each method
   - Minimum and maximum times
   - Total and average memory usage
   - Speed comparison ratios
   - Memory usage comparison ratios

## Performance Characteristics

- **Bit Manipulation**: Good balance of speed and memory efficiency
- **Dancing Links (DLX)**: Fastest for extreme level but most memory-intensive
- **Basic Backtracking**: Slowest but uses less memory than DLX

## Output Example

```
Total puzzles loaded from [difficulty].txt: [number]

Example Puzzle:
[Sudoku grid display]

Bit Manipulation Result:
[Solved grid]
Time taken: [X] nanoseconds
Memory used: [Y] bytes

[Similar displays for DLX and Backtracking]

Solver timing comparison for [N] puzzles:
[Detailed comparison table]

Detailed Summary (milliseconds):
[Performance metrics and comparisons] 