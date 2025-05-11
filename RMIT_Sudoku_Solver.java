import java.io.*;
import java.util.*;

public class RMIT_Sudoku_Solver {
    private static final int GRID_SIZE = 9;
    private static final int SIZE = 3;
    private static final int CONSTRAINTS = 4;
    private static final int COLS = GRID_SIZE * GRID_SIZE * CONSTRAINTS;

    // Bit manipulation solver fields
    private int[] rowMask = new int[9];
    private int[] colMask = new int[9];
    private int[] boxMask = new int[9];
    public int bitManipulationCalls = 0;

    // DLX solver counter
    private int dlxCalls = 0;

    // Basic backtracking solver counter
    private int basicBacktrackingCalls = 0;

    // Main solving method as required
    public int[][] solve(int[][] puzzle) {
        // Try the bit manipulation solver
        int[][] result = solveBitManipulation(puzzle);
        if (result != null) {
            return result;
        }

        // If that fails, try the DLX solver
        result = solveDLX(puzzle);
        if (result != null) {
            return result;
        }

        // Last resort - try the basic backtracking solver
        return solveBasicBacktracking(puzzle);
    }

    // Method to evaluate and compare all three solvers
    public SolverResult solveAndCompare(int[][] puzzle) {
        SolverResult result = new SolverResult();
        Runtime runtime = Runtime.getRuntime(); // Monitor memory

        // Test bit manipulation solver
        runtime.gc(); // JVM run garbage collection to get a clean baseline
        long memBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();
        bitManipulationCalls = 0;
        int[][] bitSolution = solveBitManipulation(puzzle);
        long endTime = System.nanoTime();
        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        result.bitManipulationTime = endTime - startTime;
        result.bitManipulationCalls = bitManipulationCalls;
        result.bitManipulationSuccess = (bitSolution != null);
        result.bitManipulationMemory = memAfter - memBefore;

        // Test DLX solver
        runtime.gc();
        memBefore = runtime.totalMemory() - runtime.freeMemory();
        startTime = System.nanoTime();
        dlxCalls = 0;
        int[][] dlxSolution = solveDLX(puzzle);
        endTime = System.nanoTime();
        memAfter = runtime.totalMemory() - runtime.freeMemory();
        result.dlxTime = endTime - startTime;
        result.dlxCalls = dlxCalls;
        result.dlxSuccess = (dlxSolution != null);
        result.dlxMemory = memAfter - memBefore;

        // Test basic backtracking solver
        runtime.gc();
        memBefore = runtime.totalMemory() - runtime.freeMemory();
        startTime = System.nanoTime();
        basicBacktrackingCalls = 0;
        int[][] basicSolution = solveBasicBacktracking(puzzle);
        endTime = System.nanoTime();
        memAfter = runtime.totalMemory() - runtime.freeMemory();
        result.basicBacktrackingTime = endTime - startTime;
        result.basicBacktrackingCalls = basicBacktrackingCalls;
        result.basicBacktrackingSuccess = (basicSolution != null);
        result.basicBacktrackingMemory = memAfter - memBefore;

        // Use the solution from the first successful method
        if (result.bitManipulationSuccess) {
            result.solution = bitSolution;
        } else if (result.dlxSuccess) {
            result.solution = dlxSolution;
        } else if (result.basicBacktrackingSuccess) {
            result.solution = basicSolution;
        }

        return result;
    }

    // Class to hold comparison results
    public static class SolverResult {
        public int[][] solution;

        // Bit manipulation metrics
        public long bitManipulationTime;
        public int bitManipulationCalls;
        public boolean bitManipulationSuccess;
        public long bitManipulationMemory;

        // DLX metrics
        public long dlxTime;
        public int dlxCalls;
        public boolean dlxSuccess;
        public long dlxMemory;

        // Basic backtracking metrics
        public long basicBacktrackingTime;
        public int basicBacktrackingCalls;
        public boolean basicBacktrackingSuccess;
        public long basicBacktrackingMemory;

        // Helper method to check if any solver was successful
        public boolean hasValidSolution() {
            return bitManipulationSuccess || dlxSuccess || basicBacktrackingSuccess;
        }
    }

    // ----------------------------------------
    // BitManipulation Solver
    // ----------------------------------------

    private int[][] solveBitManipulation(int[][] puzzle) {
        bitManipulationCalls = 0;
        Arrays.fill(rowMask, 0);
        Arrays.fill(colMask, 0);
        Arrays.fill(boxMask, 0);

        int[][] board = new int[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                board[r][c] = puzzle[r][c];
                if (puzzle[r][c] != 0) {
                    int digit = puzzle[r][c] - 1;
                    int bit = 1 << digit;
                    rowMask[r] |= bit;
                    colMask[c] |= bit;
                    boxMask[boxIndex(r, c)] |= bit;
                }
            }
        }

        boolean solved = backtrackBit(board, 0, 0);
        return solved ? board : null;
    }

    private boolean backtrackBit(int[][] board, int r, int c) {
        bitManipulationCalls++;
        if (r == 9)
            return true;
        if (c == 9)
            return backtrackBit(board, r + 1, 0);
        if (board[r][c] != 0)
            return backtrackBit(board, r, c + 1);

        int box = boxIndex(r, c);
        for (int d = 0; d < 9; d++) {
            int bit = 1 << d;
            if ((rowMask[r] & bit) != 0 || (colMask[c] & bit) != 0 || (boxMask[box] & bit) != 0) {
                continue;
            }

            board[r][c] = d + 1;
            rowMask[r] |= bit;
            colMask[c] |= bit;
            boxMask[box] |= bit;

            if (backtrackBit(board, r, c + 1))
                return true;

            board[r][c] = 0;
            rowMask[r] &= ~bit;
            colMask[c] &= ~bit;
            boxMask[box] &= ~bit;
        }

        return false;
    }

    private int boxIndex(int r, int c) {
        return (r / 3) * 3 + (c / 3);
    }

    // ----------------------------------------
    // DLX Solver
    // ----------------------------------------

    private static class Node {
        Node left, right, up, down;
        ColumnNode column;

        Node() {
            left = right = up = down = this;
        }

        Node(ColumnNode col) {
            this();
            column = col;
        }
    }

    private static class ColumnNode extends Node {
        int size;
        String name;

        ColumnNode(String name) {
            super();
            this.name = name;
            size = 0;
            column = this;
        }
    }

    private class DLX {
        ColumnNode header;
        List<Node> solution;
        List<ColumnNode> columns;

        DLX(boolean[][] matrix) {
            header = new ColumnNode("root");
            columns = new ArrayList<>();
            solution = new ArrayList<>();

            int cols = matrix[0].length;
            for (int i = 0; i < cols; i++) {
                ColumnNode col = new ColumnNode("C" + i);
                columns.add(col);
                header.left.right = col;
                col.right = header;
                col.left = header.left;
                header.left = col;
                col.up = col.down = col;
            }

            for (boolean[] row : matrix) {
                Node prev = null;
                for (int j = 0; j < cols; j++) {
                    if (row[j]) {
                        ColumnNode col = columns.get(j);
                        Node node = new Node(col);
                        node.up = col.up;
                        node.down = col;
                        col.up.down = node;
                        col.up = node;
                        col.size++;
                        if (prev != null) {
                            node.left = prev;
                            node.right = prev.right;
                            prev.right.left = node;
                            prev.right = node;
                        } else {
                            node.left = node.right = node;
                        }
                        prev = node;
                    }
                }
            }
        }

        boolean search() {
            dlxCalls++;
            if (header.right == header)
                return true;
            ColumnNode col = selectColumn();
            cover(col);

            for (Node row = col.down; row != col; row = row.down) {
                solution.add(row);
                for (Node j = row.right; j != row; j = j.right)
                    cover(j.column);
                if (search())
                    return true;
                solution.remove(solution.size() - 1);
                for (Node j = row.left; j != row; j = j.left)
                    uncover(j.column);
            }
            uncover(col);
            return false;
        }

        ColumnNode selectColumn() {
            int min = Integer.MAX_VALUE;
            ColumnNode best = null;
            for (ColumnNode c = (ColumnNode) header.right; c != header; c = (ColumnNode) c.right) {
                if (c.size < min) {
                    min = c.size;
                    best = c;
                }
            }
            return best;
        }

        void cover(ColumnNode col) {
            col.right.left = col.left;
            col.left.right = col.right;
            for (Node i = col.down; i != col; i = i.down) {
                for (Node j = i.right; j != i; j = j.right) {
                    j.down.up = j.up;
                    j.up.down = j.down;
                    j.column.size--;
                }
            }
        }

        void uncover(ColumnNode col) {
            for (Node i = col.up; i != col; i = i.up) {
                for (Node j = i.left; j != i; j = j.left) {
                    j.column.size++;
                    j.down.up = j;
                    j.up.down = j;
                }
            }
            col.right.left = col;
            col.left.right = col;
        }

        int[][] getSolution() {
            int[][] board = new int[GRID_SIZE][GRID_SIZE];
            for (Node row : solution) {
                int r = -1, c = -1, d = -1;
                for (Node j = row;; j = j.right) {
                    String name = j.column.name;
                    int idx = Integer.parseInt(name.substring(1));
                    int group = idx / (GRID_SIZE * GRID_SIZE);
                    int pos = idx % (GRID_SIZE * GRID_SIZE);
                    if (group == 0) {
                        r = pos / GRID_SIZE;
                        c = pos % GRID_SIZE;
                    } else if (group == 1) {
                        d = pos % GRID_SIZE;
                    }
                    if (j.right == row)
                        break;
                }
                board[r][c] = d + 1;
            }
            return board;
        }
    }

    private boolean[][] createExactCoverMatrix(int[][] grid) {
        boolean[][] matrix = new boolean[GRID_SIZE * GRID_SIZE * GRID_SIZE][COLS];
        int index = 0;
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                for (int d = 0; d < GRID_SIZE; d++) {
                    if (grid[r][c] != 0 && grid[r][c] != d + 1)
                        continue;
                    int rowIndex = index++;
                    int box = (r / SIZE) * SIZE + (c / SIZE);
                    matrix[rowIndex][r * GRID_SIZE + c] = true;
                    matrix[rowIndex][GRID_SIZE * GRID_SIZE + r * GRID_SIZE + d] = true;
                    matrix[rowIndex][2 * GRID_SIZE * GRID_SIZE + c * GRID_SIZE + d] = true;
                    matrix[rowIndex][3 * GRID_SIZE * GRID_SIZE + box * GRID_SIZE + d] = true;
                }
            }
        }
        return matrix;
    }

    private int[][] solveDLX(int[][] puzzle) {
        dlxCalls = 0;
        try {
            boolean[][] matrix = createExactCoverMatrix(puzzle);
            DLX solver = new DLX(matrix);
            if (solver.search()) {
                return solver.getSolution();
            }
        } catch (Exception e) {
            System.err.println("DLX solver error: " + e.getMessage());
        }
        return null;
    }

    // ----------------------------------------
    // Basic Backtracking Solver
    // ----------------------------------------

    private int[][] solveBasicBacktracking(int[][] puzzle) {
        basicBacktrackingCalls = 0;
        int[][] board = new int[GRID_SIZE][GRID_SIZE];
        // Copy the puzzle to avoid modifying the original
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(puzzle[i], 0, board[i], 0, GRID_SIZE);
        }

        if (getSolution(board)) {
            return board;
        }
        return null;
    }

    private boolean isNumberInRow(int[][] board, int number, int row) {
        for (int i = 0; i < GRID_SIZE; i++) {
            if (board[row][i] == number) {
                return true;
            }
        }
        return false;
    }

    private boolean isNumberInColumn(int[][] board, int number, int column) {
        for (int i = 0; i < GRID_SIZE; i++) {
            if (board[i][column] == number) {
                return true;
            }
        }
        return false;
    }

    private boolean isNumberInBox(int[][] board, int number, int row, int column) {
        int localBoxRow = row - row % 3;
        int localBoxColumn = column - column % 3;

        for (int i = localBoxRow; i < localBoxRow + 3; i++) {
            for (int j = localBoxColumn; j < localBoxColumn + 3; j++) {
                if (board[i][j] == number) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidPlacement(int[][] board, int number, int row, int column) {
        return !isNumberInRow(board, number, row) &&
                !isNumberInColumn(board, number, column) &&
                !isNumberInBox(board, number, row, column);
    }

    private boolean getSolution(int[][] board) {
        basicBacktrackingCalls++;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int column = 0; column < GRID_SIZE; column++) {
                if (board[row][column] == 0) {
                    for (int numberToTry = 1; numberToTry <= GRID_SIZE; numberToTry++) {
                        if (isValidPlacement(board, numberToTry, row, column)) {
                            board[row][column] = numberToTry;
                            if (getSolution(board)) {
                                return true;
                            } else {
                                board[row][column] = 0;
                            }
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    // ----------------------------------------
    // Main method and file reading
    // ----------------------------------------

    public static void main(String[] args) {
        List<int[][]> puzzles = readPuzzlesFromFile("hard.txt");
        RMIT_Sudoku_Solver solver = new RMIT_Sudoku_Solver();
        System.out.println("Total puzzles loaded: " + puzzles.size());

        int[][] examplePuzzle = puzzles.get(0);

        Runtime runtime = Runtime.getRuntime();

        runtime.gc();
        long memBefore1 = runtime.totalMemory() - runtime.freeMemory();
        long startTime1 = System.nanoTime();
        int[][] result1 = solver.solveBitManipulation(examplePuzzle);
        long endTime1 = System.nanoTime();
        long memAfter1 = runtime.totalMemory() - runtime.freeMemory();
        long bitManipulationTime = endTime1 - startTime1;
        long bitManipulationMemory = memAfter1 - memBefore1;

        runtime.gc();
        long memBefore2 = runtime.totalMemory() - runtime.freeMemory();
        long startTime2 = System.nanoTime();
        int[][] result2 = solver.solveDLX(examplePuzzle);
        long endTime2 = System.nanoTime();
        long memAfter2 = runtime.totalMemory() - runtime.freeMemory();
        long dlxTime = endTime2 - startTime2;
        long dlxMemory = memAfter2 - memBefore2;

        runtime.gc();
        long memBefore3 = runtime.totalMemory() - runtime.freeMemory();
        long startTime3 = System.nanoTime();
        int[][] result3 = solver.solveBasicBacktracking(examplePuzzle);
        long endTime3 = System.nanoTime();
        long memAfter3 = runtime.totalMemory() - runtime.freeMemory();
        long basicBacktrackingTime = endTime3 - startTime3;
        long basicBacktrackingMemory = memAfter3 - memBefore3;

        System.out.println("Example Puzzle:");
        printBoard(examplePuzzle);

        System.out.println("\nBit Manipulation Result:");
        printBoard(result1);
        System.out.println("Time taken for Bit Manipulation: " + bitManipulationTime + " nanoseconds");
        System.out.println("Memory used for Bit Manipulation: " + bitManipulationMemory + " bytes");

        System.out.println("\nDLX Result:");
        printBoard(result2);
        System.out.println("Time taken for DLX: " + dlxTime + " nanoseconds");
        System.out.println("Memory used for DLX: " + dlxMemory + " bytes");

        System.out.println("\nBasic Backtracking Result:");
        printBoard(result3);
        System.out.println("Time taken for Basic Backtracking: " + basicBacktrackingTime + " nanoseconds");
        System.out.println("Memory used for Basic Backtracking: " + basicBacktrackingMemory + " bytes");

        System.out.println();

        System.out.println("Solver timing comparison for " + puzzles.size() + " puzzles:");
        System.out
                .println("===========================================================================================");
        System.out.printf("%-6s | %-25s | %-25s | %-25s\n",
                "Puzzle", "Bit Manipulation", "Dancing Links (DLX)", "Basic Backtracking");
        System.out.println(
                "-------|---------------------------|---------------------------|---------------------------");
        System.out.printf("%-6s | %-10s %-14s | %-10s %-14s | %-10s %-14s\n",
                "", "Time (ms)", "Calls", "Time (ms)", "Calls", "Time (ms)", "Calls");
        System.out.println(
                "-------|---------------------------|---------------------------|---------------------------");

        for (int count = 0; count < puzzles.size(); count++) {
            int[][] puzzle = puzzles.get(count);
            SolverResult result = solver.solveAndCompare(puzzle);

            if (!result.bitManipulationSuccess)
                System.out.println("Bit failed on puzzle " + (count + 1));
            if (!result.dlxSuccess)
                System.out.println("DLX failed on puzzle " + (count + 1));
            if (!result.basicBacktrackingSuccess)
                System.out.println("Backtracking failed on puzzle " + (count + 1));

            if (result.solution != null) {
                double bitTimeMs = result.bitManipulationTime / 1_000_000.0;
                double dlxTimeMs = result.dlxTime / 1_000_000.0;
                double basicTimeMs = result.basicBacktrackingTime / 1_000_000.0;

                System.out.printf("%-6d | %-10.3f %-14d | %-10.3f %-14d | %-10.3f %-14d\n",
                        count + 1,
                        bitTimeMs, result.bitManipulationCalls,
                        dlxTimeMs, result.dlxCalls,
                        basicTimeMs, result.basicBacktrackingCalls);
            } else {
                System.out.printf("%-6d | Unsolvable puzzle (all 3 solvers failed)\n", count + 1);
            }

        }

        // Detailed timing summary
        System.out.println("\nDetailed Timing Summary (milliseconds):");
        System.out.println("=======================================================================================");

        // Calculate aggregate timing statistics
        double totalBitTimeMs = 0, totalDlxTimeMs = 0, totalBasicTimeMs = 0;
        double minBitTime = Double.MAX_VALUE, minDlxTime = Double.MAX_VALUE, minBasicTime = Double.MAX_VALUE;
        double maxBitTime = 0, maxDlxTime = 0, maxBasicTime = 0;
        long totalBitMemory = 0, totalDlxMemory = 0, totalBasicMemory = 0;

        for (int[][] puzzle : puzzles) {
            SolverResult result = solver.solveAndCompare(puzzle);

            // Convert to milliseconds
            double bitTimeMs = result.bitManipulationTime / 1_000_000.0;
            double dlxTimeMs = result.dlxTime / 1_000_000.0;
            double basicTimeMs = result.basicBacktrackingTime / 1_000_000.0;

            // Update totals
            totalBitTimeMs += bitTimeMs;
            totalDlxTimeMs += dlxTimeMs;
            totalBasicTimeMs += basicTimeMs;

            totalBitMemory += result.bitManipulationMemory;
            totalDlxMemory += result.dlxMemory;
            totalBasicMemory += result.basicBacktrackingMemory;

            // Update min/max values
            minBitTime = Math.min(minBitTime, bitTimeMs);
            minDlxTime = Math.min(minDlxTime, dlxTimeMs);
            minBasicTime = Math.min(minBasicTime, basicTimeMs);

            maxBitTime = Math.max(maxBitTime, bitTimeMs);
            maxDlxTime = Math.max(maxDlxTime, dlxTimeMs);
            maxBasicTime = Math.max(maxBasicTime, basicTimeMs);
        }

        int count = puzzles.size();
        System.out.printf("%-20s | %-20s | %-20s | %-20s\n",
                "Metric", "Bit Manipulation", "Dancing Links", "Basic Backtracking");
        System.out.println("---------------------|----------------------|----------------------|-------------------");
        System.out.printf("%-20s | %-20f | %-20f | %-20f\n",
                "Average time (ms)", totalBitTimeMs / count, totalDlxTimeMs / count, totalBasicTimeMs / count);
        System.out.printf("%-20s | %-20f | %-20f | %-20f\n",
                "Min time (ms)", minBitTime, minDlxTime, minBasicTime);
        System.out.printf("%-20s | %-20f | %-20f | %-20f\n",
                "Max time (ms)", maxBitTime, maxDlxTime, maxBasicTime);
        System.out.printf("%-20s | %-20f | %-20f | %-20f\n",
                "Total time (ms)", totalBitTimeMs, totalDlxTimeMs, totalBasicTimeMs);
        System.out.printf("%-20s | %-20d | %-20d | %-20d\n",
                "Total memory (bytes)", totalBitMemory, totalDlxMemory, totalBasicMemory);
        System.out.printf("%-20s | %-20d | %-20d | %-20d\n",
                "Avg memory (bytes)", totalBitMemory / count, totalDlxMemory / count, totalBasicMemory / count);

        // Determine the fastest method
        String fastestMethod;
        if (totalBitTimeMs <= totalDlxTimeMs && totalBitTimeMs <= totalBasicTimeMs) {
            fastestMethod = "Bit Manipulation";
        } else if (totalDlxTimeMs <= totalBitTimeMs && totalDlxTimeMs <= totalBasicTimeMs) {
            fastestMethod = "Dancing Links (DLX)";
        } else {
            fastestMethod = "Basic Backtracking";
        }

        System.out.println("\nFastest method overall: " + fastestMethod);

        // Show speedup ratios
        System.out.println("\nSpeed comparison (higher is better):");
        System.out.printf("Bit Manipulation vs DLX:          %.2fx\n", totalDlxTimeMs / totalBitTimeMs);
        System.out.printf("Bit Manipulation vs Backtracking: %.2fx\n", totalBasicTimeMs / totalBitTimeMs);
        System.out.printf("DLX vs Backtracking:             %.2fx\n", totalBasicTimeMs / totalDlxTimeMs);

        // Show memory comparison
        System.out.println("\nMemory usage comparison (lower is better):");
        System.out.printf("Bit Manipulation vs DLX:          %.2fx\n", (double) totalDlxMemory / totalBitMemory);
        System.out.printf("Bit Manipulation vs Backtracking: %.2fx\n", (double) totalBasicMemory / totalBitMemory);
        System.out.printf("DLX vs Backtracking:             %.2fx\n", (double) totalBasicMemory / totalDlxMemory);
    }

    private static void printBoard(int[][] board) {
        for (int row = 0; row < GRID_SIZE; row++) {
            if (row % 3 == 0 && row != 0) {
                System.out.println("---------------------");
            }
            for (int column = 0; column < GRID_SIZE; column++) {
                if (column % 3 == 0 && column != 0) {
                    System.out.print("| ");
                }
                System.out.print(board[row][column] + " ");
            }
            System.out.println();
        }
    }

    private static boolean isValidPuzzle(int[][] puzzle) {
        boolean[][] rows = new boolean[9][10];
        boolean[][] cols = new boolean[9][10];
        boolean[][] boxes = new boolean[9][10];

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int num = puzzle[r][c];
                if (num == 0)
                    continue;
                int box = (r / 3) * 3 + c / 3;
                if (rows[r][num] || cols[c][num] || boxes[box][num]) {
                    return false; // Duplicate found
                }
                rows[r][num] = cols[c][num] = boxes[box][num] = true;
            }
        }
        return true;
    }

    public static List<int[][]> readPuzzlesFromFile(String filename) {
        List<int[][]> puzzles = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            List<int[]> currentPuzzle = new ArrayList<>();
            int puzzleNumber = 1;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    if (currentPuzzle.size() == 9) {
                        int[][] puzzle = currentPuzzle.toArray(new int[9][9]);
                        if (isValidPuzzle(puzzle)) {
                            puzzles.add(puzzle);
                        } else {
                            System.out.println("Skipping invalid puzzle #" + puzzleNumber);
                        }
                    }
                    currentPuzzle.clear();
                    puzzleNumber++;
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length != 9) {
                    System.err.println("Invalid line (not 9 numbers): " + line);
                    currentPuzzle.clear();
                    puzzleNumber++;
                    continue;
                }

                int[] row = new int[9];
                for (int i = 0; i < 9; i++) {
                    row[i] = Integer.parseInt(parts[i]);
                }

                currentPuzzle.add(row);
            }

            // Check for last puzzle in case no trailing blank line
            if (currentPuzzle.size() == 9) {
                int[][] puzzle = currentPuzzle.toArray(new int[9][9]);
                if (isValidPuzzle(puzzle)) {
                    puzzles.add(puzzle);
                } else {
                    System.out.println("Skipping invalid puzzle #" + puzzleNumber);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return puzzles;
    }
}