import java.util.*;
import java.io.*;

public class SudokuDLXSolver1 {
    static final int N = 9;
    static final int SIZE = 3;
    static final int CONSTRAINTS = 4;
    static final int COLS = N * N * CONSTRAINTS;

    static int searchCalls = 0; // Counter for number of search() calls

    static class Node {
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

    static class ColumnNode extends Node {
        int size;
        String name;

        ColumnNode(String name) {
            super();
            this.name = name;
            size = 0;
            column = this;
        }
    }

    static class DLX {
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
            searchCalls++; // Increment counter each time search() is called
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
            int[][] board = new int[N][N];
            for (Node row : solution) {
                int r = -1, c = -1, d = -1;
                for (Node j = row;; j = j.right) {
                    String name = j.column.name;
                    int idx = Integer.parseInt(name.substring(1));
                    int group = idx / (N * N);
                    int pos = idx % (N * N);
                    if (group == 0) {
                        r = pos / N;
                        c = pos % N;
                    } else if (group == 1) {
                        d = pos % N;
                    }
                    if (j.right == row)
                        break;
                }
                board[r][c] = d + 1;
            }
            return board;
        }
    }

    static boolean[][] createExactCoverMatrix(int[][] grid) {
        boolean[][] matrix = new boolean[N * N * N][COLS];
        int index = 0;
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                for (int d = 0; d < N; d++) {
                    if (grid[r][c] != 0 && grid[r][c] != d + 1)
                        continue;
                    int rowIndex = index++;
                    int box = (r / SIZE) * SIZE + (c / SIZE);
                    matrix[rowIndex][r * N + c] = true;
                    matrix[rowIndex][N * N + r * N + d] = true;
                    matrix[rowIndex][2 * N * N + c * N + d] = true;
                    matrix[rowIndex][3 * N * N + box * N + d] = true;
                }
            }
        }
        return matrix;
    }

    public ArrayList<int[][]> readFile(String filepath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            ArrayList<int[][]> puzzles = new ArrayList<>();
            String line;
            int[][] puzzle = new int[N][N];
            int row = 0;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (row == N)
                        puzzles.add(puzzle);
                    puzzle = new int[N][N];
                    row = 0;
                } else {
                    String[] parts = line.trim().split(" ");
                    for (int i = 0; i < N; i++) {
                        puzzle[row][i] = Integer.parseInt(parts[i]);
                    }
                    row++;
                }
            }
            if (row == N)
                puzzles.add(puzzle);
            br.close();

            return puzzles;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ArrayList<>();
        }
    }

    public void solve(ArrayList<int[][]> puzzles) {
        for (int t = 0; t < puzzles.size(); t++) {
            searchCalls = 0; // Reset counter for each test
            System.out.println("Test " + (t + 1) + ":");
            int[][] p = puzzles.get(t);
            long startTime = System.nanoTime();
            boolean[][] matrix = createExactCoverMatrix(p);
            DLX solver = new DLX(matrix);
            if (solver.search()) {
                int[][] solution = solver.getSolution();
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        System.out.print(solution[i][j] + " ");
                    }
                    System.out.println();
                }
            } else {
                System.out.println("No solution found.");
            }
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            System.out.println("runtime: " + totalTime + ";");
            System.out.println("search() calls: " + searchCalls + ";");
            System.out.println();
        }
    }

    public static void main(String[] args) {
        SudokuDLXSolver1 s = new SudokuDLXSolver1();
        ArrayList<int[][]> puzzles = new ArrayList<int[][]>();
        puzzles = s.readFile("Easy.txt");
        s.solve(puzzles);

    }

}
