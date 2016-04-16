import java.util.ArrayList;

/**
 * Created by Alex on 05/03/2016.
 */
public class BotOps {

    public static int botID = BotParser.mBotId;
    public static int opponentID = (botID == 1) ? (2) : (1);

    public static ArrayList<Move> finalScores = new ArrayList<>();

    public static int[] winningPatterns = {
            0b111000000, 0b000111000, 0b000000111, // rows
            0b100100100, 0b010010010, 0b001001001, // cols
            0b100010001, 0b001010100 // diagonals
    };

    public static int[][] weights = {{3, 2, 3}, {2, 4, 2}, {3, 2, 3}};

    public static final int WIN_GAME_SCORE = Integer.MAX_VALUE;
    public static final int LOSE_GAME_SCORE = Integer.MIN_VALUE;
    public static final int TIE_GAME_SCORE = 0;
    public static final int SQUARE_MAX_SCORE = 24;
    public static final int SQUARE_TIE_SCORE = 0;

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


        System.err.println("Call minimax");

        double x = System.nanoTime();
        minimax(field.mBoard, field.mMacroboard, 0, 4, botID);

        System.err.println((System.nanoTime() - x) * 1e-9);

        nextMove = bestMoveFromMinimax();
        System.err.println(nextMove);

        System.err.println("took: " + (System.nanoTime() - x) * 1e-9);

        return nextMove;
    }


    /* Minimax */

    public static ArrayList<Move> getAvailableMoves(int[][] board, int[][] macro) {
        ArrayList<Move> moves = new ArrayList<Move>();

        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {

                if (macro[x / 3][y / 3] == -1 && board[x][y] == 0) {
                    moves.add(new Move(x, y));
                }
            }
        }

        return moves;
    }

    //  main method
    public static int minimax(int[][] board, int[][] macro, int depth, int maxDepth, int player) {
        if (gameHasEnded(macro) || depth == maxDepth) {
            return evaluate(board, macro);
        }

        ArrayList<Integer> scores = new ArrayList<>();

        for (Move move : getAvailableMoves(board, macro)) {
            //  my turn
            if (player == botID) {
                //  simulate this possible move
                int[][] macroClone = cloneMatrix(macro);
                simulate(board, macroClone, move, botID);


                //  get the score for the next move
                int score = minimax(board, macroClone, depth + 1, maxDepth, opponentID);

                scores.add(score);

                //  update the scores list when coming back from recursive calls
                if (depth == 0) {
                    finalScores.add(new Move(move.x, move.y, score));
                }
            }

            //  his turn
            else if (player == opponentID) {
                //  simulate this possible move
                int[][] macroClone = cloneMatrix(macro);
                simulate(board, macroClone, move, opponentID);

                //  get the score for the next move
                int score = minimax(board, macroClone, depth + 1, maxDepth, botID);

                scores.add(score);
            }

            //  reset move
            board[move.x][move.y] = 0;
        }

        if (player == botID)
            return maxFrom(scores);
        else return minFrom(scores);
    }


    //  minimax utils

    public static void print(int[][] source) {
        for (int i = 0; i < source.length; i++) {
            for (int j = 0; j < source.length; i++) {
                System.err.println(source[j][i] + " ");
            }
            System.err.println();
        }
    }

    public static int[][] cloneMatrix(int[][] source) {
        int[][] res = new int[source.length][];

        for (int i = 0; i < source.length; i++) {
            res[i] = source[i].clone();
        }

        return res;
    }

    public static boolean gameHasEnded(int[][] macro) {
        boolean botWin = isWinner(macro, botID);
        boolean opponentWin = isWinner(macro, opponentID);

        return botWin || opponentWin;
    }

    public static Move bestMoveFromMinimax() {
        int max = Integer.MIN_VALUE;
        int index = 0;

        System.err.println(finalScores);

        for (int i = 0; i < finalScores.size(); i++) {
            if (max < finalScores.get(i).score) {
                max = finalScores.get(i).score;
                index = i;
            }
        }

        int x = finalScores.get(index).x;
        int y = finalScores.get(index).y;
        int score = finalScores.get(index).score;

        Move m = new Move(x, y, score);
        finalScores.clear();

        return m;
    }

    public static int maxFrom(ArrayList<Integer> scores) {
        int max = Integer.MIN_VALUE;

        for (Integer s : scores) {
            if (s > max) {
                max = s;
            }
        }

        return max;
    }

    public static int minFrom(ArrayList<Integer> scores) {
        int min = Integer.MAX_VALUE;

        for (Integer s : scores) {
            if (s < min) {
                min = s;
            }
        }

        return min;
    }

    public static boolean isFull(int[][] square) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (square[i][j] == 0)
                    return false;
            }
        }

        return true;
    }

    //  seems good
    public static void simulate(int[][] board, int[][] macroClone, Move move, int player) {
        int x = move.x, y = move.y;
        int this_x = x / 3, this_y = y / 3;
        int sent_x = x % 3, sent_y = y % 3;

        board[x][y] = player;

        int[][] thisSquare = getSquareFromBoard(this_x, this_y, board);

        //  player wins the square
        if (isWinner(thisSquare, player)) {
            macroClone[this_x][this_y] = player;
        } else {
            macroClone[this_x][this_y] = 0;
        }

        int[][] sentSquare = getSquareFromBoard(sent_x, sent_y, board);

        //  still playable, but wasn't active
        if (!isFull(sentSquare) && macroClone[sent_x][sent_y] <= 0) {
            macroClone[sent_x][sent_y] = -1;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (i == sent_x && j == sent_y)
                        continue;

                    if (!isFull(getSquareFromBoard(i, j, board)) && macroClone[i][j] <= 0) {
                        macroClone[i][j] = 0;
                    }
                }
            }
        }
        //  either full, either won
        else {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int[][] auxSquare = getSquareFromBoard(i, j, board);

                    if (macroClone[i][j] == 0 && !isFull(auxSquare)) {
                        macroClone[i][j] = -1;
                    }
                }
            }
        }
    }

    //  evaluation methods
    public static int evaluate(int[][] board, int[][] macro) {
        int score = 0;

        if (isWinner(macro, botID)) {
            return WIN_GAME_SCORE;
        }
        if (isWinner(macro, opponentID)) {
            return LOSE_GAME_SCORE;
        }
        if (isFull(macro)) {
            return TIE_GAME_SCORE;
        }


        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int[][] square = getSquareFromBoard(i, j, board);
                int squareEval = evalSquare(square);

                score += squareEval * weights[i][j];
            }
        }

        return score;
    }

    public static int evalSquare(int[][] square) {
        //  base-cases

        if (isWinner(square, botID))
            return SQUARE_MAX_SCORE;

        if (isWinner(square, opponentID))
            return -SQUARE_MAX_SCORE;

        if (isFull(square))
            return SQUARE_TIE_SCORE;

        int score = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (square[i][j] == botID)
                    score += weights[i][j];
                else if (square[i][j] == opponentID)
                    score -= weights[i][j];
            }
        }

        return score;
    }



    /* Non-minimax */

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
        for (Move move : moves) {
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
                        System.err.println("ME: " + i + " " + j + " can win the GAME");
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

        System.err.println("I can't win any squares, try to block");

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
                        System.err.println("HIM: " + i + " " + j + " can win the GAME");
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
    public static Move canWinSquare(int[][] board, ArrayList<Move> moves, int player) {
        int[][] square;

        for (Move move : moves) {
            int x = move.getX();
            int y = move.getY();

            //  construct the square containing the current (x, y) move
            //  simulate the current move
            square = getSquareFromBoard(x / 3, y / 3, board);
            square[x % 3][y % 3] = player;

            //  in case of winning the square, return the move
            if (isWinner(square, player)) {
                System.err.println("can win square !!!");
                return move;
            }
        }

        System.err.println("cannot win square????");
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
        return (macroboard[1][1] == 0) && (!playerCanWin(getSquareFromBoard(1, 1, board), opponentID));

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
}
