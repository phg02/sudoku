import java.io.*;
import java.util.*;

public class RMIT_Sudoku_Solver {
    int[] rowMask = new int[9];
    int[] colMask = new int[9];
    int[] boxMask = new int[9];
    public int recursionCount = 0; // Counter for recursion calls

    public int[][] solve(int[][] puzzle) {
        recursionCount = 0;
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

        boolean solved = backtrack(board, 0, 0);
        return solved ? board : null;
    }

    private boolean backtrack(int[][] board, int r, int c) {
        recursionCount++;
        if (r == 9)
            return true;
        if (c == 9)
            return backtrack(board, r + 1, 0);
        if (board[r][c] != 0)
            return backtrack(board, r, c + 1);

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

            if (backtrack(board, r, c + 1))
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

    public static List<int[][]> readPuzzlesFromFile(String filename) {
        List<int[][]> puzzles = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int[][] puzzle = new int[9][9];
            int row = 0;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (row == 9) {
                        puzzles.add(puzzle);
                        puzzle = new int[9][9];
                        row = 0;
                    }
                    continue;
                }

                String[] tokens = line.trim().split("\\s+");
                if (tokens.length != 9)
                    continue;

                for (int col = 0; col < 9; col++) {
                    puzzle[row][col] = Integer.parseInt(tokens[col]);
                }
                row++;
            }

            // Add the last puzzle if the file doesn't end with a blank line
            if (row == 9) {
                puzzles.add(puzzle);
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return puzzles;
    }

    public static void main(String[] args) {
        List<int[][]> puzzles = readPuzzlesFromFile("easy.txt");
        RMIT_Sudoku_Solver solver = new RMIT_Sudoku_Solver();

        int count = 1;
        for (int[][] puzzle : puzzles) {
            System.out.println("Solving puzzle #" + count);
            long startTime = System.nanoTime();
            int[][] solution = solver.solve(puzzle);
            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            if (solution != null) {
                for (int[] row : solution) {
                    for (int num : row) {
                        System.out.print(num + " ");
                    }
                    System.out.println();
                }
            } else {
                System.out.println("This Sudoku puzzle is not solvable.");
            }

            System.out.println("Total recursive calls: " + solver.recursionCount);
            System.out.println("Execution time: " + duration + " ns");
            System.out.println("=====================================");
            count++;
        }
    }
}