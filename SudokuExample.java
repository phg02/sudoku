import java.util.*;
import java.io.*;

public class SudokuExample {
  
    private static final int GRID_SIZE = 9;
    public static int numbersTried = 0;
    public static void main(String[] args) {
      
      int[][] boardExample = {
          {7, 0, 2, 0, 5, 0, 6, 0, 0},
          {0, 0, 0, 0, 0, 3, 0, 0, 0},
          {1, 0, 0, 0, 0, 9, 5, 0, 0},
          {8, 0, 0, 0, 0, 0, 0, 9, 0},
          {0, 4, 3, 0, 0, 0, 7, 5, 0},
          {0, 9, 0, 0, 0, 0, 0, 0, 8},
          {0, 0, 9, 7, 0, 0, 0, 0, 5},
          {0, 0, 0, 2, 0, 0, 0, 0, 0},
          {0, 0, 7, 0, 4, 0, 2, 0, 3} 
        }; //example board
      
      
        SudokuExample s = new SudokuExample();
        ArrayList<int[][]> boards = new ArrayList<int[][]>();
        boards = s.readFile("easy.txt");
        solveBoard(boards);
    }
    
    private static void solveBoard(ArrayList<int[][]> boards){
      for(int t = 0; t < boards.size(); t++){
        int[][] board = boards.get(t);
        long startTime = System.nanoTime();
        if (getSolution(board)) {
          printBoard(board);
          System.out.println("Solved successfully!");
          System.out.println("Numbers tried:" + numbersTried);

          numbersTried = 0;
        }
        else {
          System.out.println("Unsolvable board :(");
        }
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        System.out.println("runtime: " + totalTime + ";");
        System.out.println("--------------------------------------");
      }
    }
    
    private static void printBoard(int[][] board) {
      for (int row = 0; row < GRID_SIZE; row++) {
        if (row % 3 == 0 && row != 0) {
          System.out.println("-----------");
        }
        for (int column = 0; column < GRID_SIZE; column++) {
          if (column % 3 == 0 && column != 0) {
            System.out.print("|");
          }
          System.out.print(board[row][column]);
        }
        System.out.println();
      }
    }
  
  
    private static boolean isNumberInRow(int[][] board, int number, int row) {
      for (int i = 0; i < GRID_SIZE; i++) {
        if (board[row][i] == number) {
          return true;
        }
      }
      return false;
    }
    
    private static boolean isNumberInColumn(int[][] board, int number, int column) {
      for (int i = 0; i < GRID_SIZE; i++) {
        if (board[i][column] == number) {
          return true;
        }
      }
      return false;
    }
    
    private static boolean isNumberInBox(int[][] board, int number, int row, int column) {
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
    
    private static boolean isValidPlacement(int[][] board, int number, int row, int column) {
      return !isNumberInRow(board, number, row) &&
          !isNumberInColumn(board, number, column) &&
          !isNumberInBox(board, number, row, column);
    }
    
    private static boolean getSolution(int[][] board) {
      for (int row = 0; row < GRID_SIZE; row++) {
        for (int column = 0; column < GRID_SIZE; column++) {
          if (board[row][column] == 0) {
            for (int numberToTry = 1; numberToTry <= GRID_SIZE; numberToTry++) {
              if (isValidPlacement(board, numberToTry, row, column)) {
                board[row][column] = numberToTry;
                numbersTried++;
                if (getSolution(board)) {
                  return true;
                }
                else {
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

    public ArrayList<int[][]> readFile(String filepath) {
        try{
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        ArrayList<int[][]> puzzles = new ArrayList<>();
        String line;
        int[][] puzzle = new int[GRID_SIZE][GRID_SIZE];
        int row = 0;
        

        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) {
                if (row == GRID_SIZE)
                    puzzles.add(puzzle);
                puzzle = new int[GRID_SIZE][GRID_SIZE];
                row = 0;
            } else {
                String[] parts = line.trim().split(" ");
                for (int i = 0; i < GRID_SIZE; i++) {
                    puzzle[row][i] = Integer.parseInt(parts[i]);
                }
                row++;
            }
        }
        if (row == GRID_SIZE)
            puzzles.add(puzzle);
        br.close();

        return puzzles;
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return new ArrayList<>();
        }
    }
    
    
}
