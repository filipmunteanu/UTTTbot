import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Alex on 05/03/2016.
 */
public class BotOps {

    // TODO: 08/03/2016 : check for inconsistencies
    public static int botID = BotParser.mBotId;
    public static int opponentID = (botID == 1) ? (2) : (1);

    public static int[] winningPatterns = {
            0b111000000, 0b000111000, 0b000000111, // rows
            0b100100100, 0b010010010, 0b001001001, // cols
            0b100010001, 0b001010100 // diagonals
    };

    //  returns the best available move the bot can make
    public static Move getMove(Field field, ArrayList<Move> moves) {
        int[][] board = field.mBoard;
        int[][] macroboard = field.mMacroboard;
        Move nextMove;

        /* Initial moves */

        //  make the first move in the center, if it's my turn
        if (field.getRoundNr() == 1 && field.isInActiveSquare(1, 1)) {
            Move center = new Move(4, 4);
            if (moves.contains(center)) {
                return center;
            }
        }

        //  if I have to move in an empty square, do it so the opponent's move
        //  will be in the same square -> better control
        if (isEmptySquare(moves)) {
            System.err.println("empty square");
            Move aux = moves.get(0);
            return new Move(4 * (aux.getX() / 3), 4 * (aux.getY() / 3));
        }



        /* Deal with multiple actives squares (two or more -1 entries in the macroboard */

        //  if there are multiple active squares
        //  move into the best one, or try to win or block the opponent
        if ((nextMove = optimalSquareMove(board, macroboard, moves)) != null) {
            System.err.println("if: optimalSquareMove");
            return nextMove;
        }



        /* Deal with a single square (only one -1 entry in the macroboard) */

        //  try to make a move such that it will win the current square
        if ((nextMove = canWinSquare(board, macroboard, moves, botID)) != null) {
            System.err.println("if: canWinSquare");
            return nextMove;
        }

        //  try to minimize the opponent's winning chances
        if ((nextMove = canWinSquare(board, macroboard, moves, opponentID)) != null) {
            System.err.println("if: canBlockOpponent");
            return nextMove;
        }

        //  try to make an optimal move when the bot has to move in a single square
        if ((nextMove = moveInSingleSquare(board, macroboard, moves)) != null) {
            System.err.println("if: moveInSingleSquare");
            return nextMove;
        }

        //  otherwise, make a random move, but not in a middle cell
        else {
            System.err.println("else: random move");
            Random rand = new Random();
            Move r = moves.get(rand.nextInt(moves.size()));

            while (isMiddleMove(r)) {
                r = moves.get(rand.nextInt(moves.size()));
            }

            return r;
        }
    }


    /* Algorithms */

    public static Move optimalSquareMove(int[][] board, int[][] macroboard, ArrayList<Move> moves) {
        //  if there are not multiple active squares, then there is not a best square to move in
        if (!multipleActiveSquares(macroboard)) {
            System.err.println("no multiple active squares");
            return null;
        }

        //  get the best square our bot can move in
        int[] squarePosition = getOptimalSquarePosition(macroboard, board);

        if (squarePosition == null) {
            System.err.println("getOptimalSquarePosition returned null");
            return null;
        }

        int squareX = squarePosition[0];
        int squareY = squarePosition[1];

        int[][] square = getSquareFromBoard(squareX, squareY, board);

        System.err.println("found a best square: " + squareX + " " + squareY);

        //  find a move that will make the best move in the square
        //  either win or block
        for (int k = 0; k < moves.size(); k++) {
            Move move = moves.get(k);
            int x = move.getX();
            int y = move.getY();

            int[][] squareClone = new int[3][3];
            for (int c = 0; c < 3; c++) {
                System.arraycopy(square[c], 0, squareClone[c], 0, 3);
            }

            //  only check moves that exist in the optimal square
            if (x / 3 == squareX && y / 3 == squareY) {
                squareClone[x % 3][y % 3] = botID;

                if (isWinner(squareClone, botID)) {
                    System.err.println("I can Win");
                    return move;
                }
            }
        }

        /* TODO
        *  if there is no move that will win the square
        *  then return a move from that square
        */

        System.err.println("best square move: for is done: returned null");
        return null;
    }

    public static int[] getOptimalSquarePosition(int[][] macroboard, int[][] board) {
        int[] squarePos = new int[2];

        int[][] square;
        int[][] macroboardClone = new int[3][3];

        boolean botCanWinSquare = false;
        boolean opponentCanWinSquare = false;

        //  if the bot can win the square
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                //  reset the macroboard copy
                for (int k = 0; k < 3; k++) {
                    System.arraycopy(macroboard[k], 0, macroboardClone[k], 0, 3);
                }

                //  if the current square is active
                if (macroboard[i][j] == -1) {
                    square = getSquareFromBoard(i, j, board);

                    //  put player on that
                    macroboardClone[i][j] = botID;

                    //  if there exists a square that can be won and therefore win the game
                    if (isWinner(macroboardClone, botID) && playerCanWin(square, botID)) {
                        System.out.println("ME: " + i + " " + j + " can win the GAME");
                        squarePos[0] = i;
                        squarePos[1] = j;
                        return squarePos;
                    }
                    //  if there exists a square that can be won, but it will not win the game
                    else if (!isWinner(macroboardClone, botID) && playerCanWin(square, botID)) {
                        if (!botCanWinSquare) {
                            botCanWinSquare = true;
                            squarePos[0] = i;
                            squarePos[1] = j;
                        }
                    }
                }
            }
        }
        if (botCanWinSquare) {
            return squarePos;
        }

        System.out.println("I can't win any squares, try to block");

        //  if the bot can't win the square, try to block the opponent
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                //  reset the macroboard copy
                for (int k = 0; k < 3; k++) {
                    System.arraycopy(macroboard[k], 0, macroboardClone[k], 0, 3);
                }

                //  if the current square is active
                if (macroboard[i][j] == -1) {
                    square = getSquareFromBoard(i, j, board);

                    //  put opponent on that
                    macroboardClone[i][j] = opponentID;

                    //  if there exists a square that can be won by the opponent and therefore win the game
                    if (isWinner(macroboardClone, opponentID) && playerCanWin(square, opponentID)) {
                        System.out.println("HIM: " + i + " " + j + " can win the GAME");
                        squarePos[0] = i;
                        squarePos[1] = j;
                        return squarePos;
                    }
                    //  if there exists a square that can be won by the opponent, but it will not win the game
                    else if (!isWinner(macroboardClone, opponentID) && playerCanWin(square, opponentID)) {
                        if (!opponentCanWinSquare) {
                            opponentCanWinSquare = true;
                            squarePos[0] = i;
                            squarePos[1] = j;
                        }
                    }
                }
            }
        }
        if (opponentCanWinSquare) {
            return squarePos;
        }

        return null;
    }

    //  returns a move which will win the square or block the opponent; if not any, return null
    // TODO: might need improving, it's not always good to rush to win the square
    public static Move canWinSquare(int[][] board, int[][] macroboard, ArrayList<Move> moves, int player) {
        int[][] square;

        for (int k = 0; k < moves.size(); k++) {
            Move move = moves.get(k);
            int x = move.getX();
            int y = move.getY();

            //  construct the square containing the current (x, y) move
            //  simulate the current move
            square = getSquareFromBoard(x / 3, y / 3, board);
            square[x % 3][y % 3] = player;

            //  in case of winning the square
            if (isWinner(square, player)) {
                //  return the move only if it won't send the opponent in a taken square
                if (!isTakenSquare(x / 3, y / 3, macroboard)) {
                    return move;
                }
            }
        }

        return null;
    }

    //  TODO: improve it! there's no need for multiple (same) calculations
    //  TODO: fork / triangle must be implemented here
    public static Move moveInSingleSquare(int[][] board, int[][] macroboard, ArrayList<Move> moves) {
        boolean canMoveInCenter = canMoveInCenter(board, macroboard);

        for (int k = 0; k < moves.size(); k++) {
            Move move = moves.get(k);
            int x = move.getX();
            int y = move.getY();

            int X = x % 3;
            int Y = y % 3;

            System.err.println("[[" + move.toString() + "]]");

            if (canMoveInCenter) {
                System.err.println("Can move in center");
                Move center = getCenterCellMove(x / 3, y / 3);
                if (moves.contains(center)) {
                    System.err.println("Moved in center");
                    return center;
                }
            }
            System.err.println("Didn't move in center");

            //  if the square is not taken
            if (macroboard[X][Y] <= 0) {
                System.err.println("M:" + X + "," + Y + " " + macroboard[X][Y]);
                int[][] square = getSquareFromBoard(X, Y, board);
                if (!playerCanWin(square, opponentID)) {
                    System.err.println("square is not taken and opt cant win => " + move.toString());
                    return move;
                }
            }
        }

        System.err.println("EXITED WITH NULL");
        return null;
    }

    //  checks whether a square is empty by looking at the available moves
    public static boolean isEmptySquare(ArrayList<Move> moves) {
        if (moves.size() != 9) {
            return false;
        } else {
            return ((moves.get(0).getX() + 2 == moves.get(8).getX()) &&
                    (moves.get(0).getY() + 2 == moves.get(8).getY()));
        }
    }

    //  returns the center cell of a square: (x, y) = coordinates of the square
    public static Move getCenterCellMove(int x, int y) {
        return new Move((x * 3) + 1, (y * 3) + 1);
    }

    //  returns a specific square based on the macroboard coordinates
    public static int[][] getSquareFromBoard(int x, int y, int[][] board) {
        int[][] square = new int[3][3];
        int k1, k2;
        k1 = 0;
        for (int i = x * 3; i < (x * 3 + 3); i++) {
            k2 = 0;
            for (int j = y * 3; j < (y * 3 + 3); j++) {
                square[k1][k2] = board[i][j];
                k2++;
            }
            k1++;
        }
        return square;
    }

    //  checks whether the bot can move in the center of the square
    public static boolean canMoveInCenter(int[][] board, int[][] macroboard) {
        if ((macroboard[1][1] == 0) && (!playerCanWin(getSquareFromBoard(1, 1, board), opponentID))) {
            return true;
        } else {
            return false;
        }

    }

    //  checks whether the player can win the square
    public static boolean playerCanWin(int[][] square, int player) {
        int[][] squareClone = new int[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                //  reset the square clone
                for (int k = 0; k < 3; k++) {
                    System.arraycopy(square[k], 0, squareClone[k], 0, 3);
                }

                if (squareClone[i][j] == 0) {
                    squareClone[i][j] = player;

                    if (isWinner(squareClone, player)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    //  checks whether there are multiple active squares in which the bot can move
    public static boolean multipleActiveSquares(int[][] macroboard) {
        int count = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (macroboard[i][j] == -1) {
                    count++;
                }
                if (count == 2) {
                    return true;
                }
            }
        }

        return false;
    }

    //  if one square matches a winning pattern, then 'player' wins the square
    public static boolean isWinner(int[][] square, int player) {
        int pattern = 0b000000000; // 9-bit pattern for the 9 cells
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (square[row][col] == player)
                    pattern |= (1 << (row * 3 + col));
            }
        }
        for (int winningPattern : winningPatterns)
            if ((pattern & winningPattern) == winningPattern)
                return true;
        return false;
    }

    public static boolean isMiddleMove(Move m) {
        if (m.getX() == 1 || m.getX() == 4 || m.getX() == 7) {
            return (m.getY() == 1 || m.getY() == 4 || m.getY() == 7);
        }
        return false;
    }

    //// TODO: 11.03.2016
    public static boolean isTakenSquare(int x, int y, int[][] macroboard) {
        return macroboard[x][y] > 0;
    }
}
