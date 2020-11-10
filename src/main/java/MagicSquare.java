public class MagicSquare {

  /**
   * Colors indexed by their numerical representation
   */
  /**
   * The size of the square.
   */
  private int n;
  /**
   * The board used to generate the magic square.
   */
  private int[][] board;
  /**
   * The value of the sum on rows/columns/diagonals
   */
  private int magicConstant;
  /**
   * Array keeping track if a number has been used or not.
   */
  private boolean[] possibleNumber;
  /**
   * The maximum value that can be placed inside a square cell.
   */
  private int maximumValue;
  /**
   * Boards storing the obtained results.
   */
  private int[][][] result;
  /**
   * The number of the valid magic squares found.
   */
  private int numberOfValidConfigurations;

  public MagicSquare(int n) {
    this.n = n;
    this.maximumValue = n * n;
    this.board = new int[n][n];
    this.result = new int[100][n][n];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        board[i][j] = 0;
      }
    }
    this.magicConstant = n * ((n * n + 1) / 2);
    this.possibleNumber = new boolean[maximumValue];
    this.numberOfValidConfigurations = 0;
    for (int i = 0; i < maximumValue; i++) {
      possibleNumber[i] = true;
    }
  }

  public void simpleBackTrack(int row, int column) {
    // drop invalid configurations
    if (!isMagic()) {
      return;
    }

    // If the board represents a magic square and we are processing on the last rows it means the board is fully
    // filled
    if (row == n && this.isMagic()) {
      copyResult();
      return;
    }

    for (int value = 0; value < maximumValue; value++) {
      // If the value is not already used, put it in the square and mark it as used
      if (possibleNumber[value]) {
        board[row][column] = value + 1;
        possibleNumber[value] = false;

        // Call method recursively
        int nextColumn = column + 1;
        int nextRow = row;
        if (nextColumn == n) {
          nextColumn = 0;
          nextRow = nextRow + 1;
        }

        simpleBackTrack(nextRow, nextColumn);

        board[row][column] = 0;
        possibleNumber[value] = true;
      }
    }
  }

  public void printSolutions() {
    for (int resultNo = 0; resultNo < this.numberOfValidConfigurations; resultNo++) {
      System.out.println("Magic square found!");
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          System.out.print(result[resultNo][i][j] + " ");
        }
        System.out.println();
      }
      System.out.println();
    }
  }

  private void copyResult() {
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        result[numberOfValidConfigurations][i][j] = board[i][j];
      }
    }
    numberOfValidConfigurations++;
  }

  /**
   * Checks if the state of the board represents a magic square.
   *
   * @return true if the sums on all rows and columns and diagonals are equal
   */
  private boolean isMagic() {
    return areRowsValid() && areColumnsValid() && areDiagonalsValid();
  }

  private boolean areRowsValid() {
    int sumRow = 0;
    boolean unfilled = false;
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        sumRow += board[i][j];
        if (board[i][j] == 0) {
          unfilled = true;
        }
      }
      if (!unfilled && sumRow != magicConstant) {
        return false;
      }
      sumRow = 0;
    }
    return true;
  }

  private boolean areColumnsValid() {
    int sumCol = 0;
    boolean unfilled = false;
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        sumCol += board[j][i];
        if (board[j][i] == 0) {
          unfilled = true;
        }
      }
      if (!unfilled && sumCol != magicConstant) {
        return false;
      }
      sumCol = 0;
    }
    return true;
  }

  private boolean areDiagonalsValid() {
    int sumDiag = 0;
    boolean unfilled = false;
    for (int i = 0; i < n; i++) {
      sumDiag += board[i][i];
      if (board[i][i] == 0) {
        unfilled = true;
      }
    }

    if (!unfilled && sumDiag != magicConstant) {
      return false;
    }
    sumDiag = 0;

    for (int i = 0; i < n; i++) {
      sumDiag += board[n - 1 - i][i];
      if (board[i][i] == 0) {
        unfilled = true;
      }
    }

    if (!unfilled && sumDiag != magicConstant) {
      return false;
    }

    return true;
  }

}
