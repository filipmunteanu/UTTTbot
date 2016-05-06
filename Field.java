import java.util.ArrayList;
import java.util.HashMap;

public class Field {
    public static int[] winningPatterns = {
            0b111000000, 0b000111000, 0b000000111, // rows
            0b100100100, 0b010010010, 0b001001001, // cols
            0b100010001, 0b001010100 // diagonals
    };

    public static HashMap<Integer, Integer[]> posPatterns;

    static {
        posPatterns = new HashMap<>();
        posPatterns.put(0, new Integer[]{0, 0});
        posPatterns.put(1, new Integer[]{0, 1});
        posPatterns.put(2, new Integer[]{0, 2});
        posPatterns.put(3, new Integer[]{1, 0});
        posPatterns.put(4, new Integer[]{1, 1});
        posPatterns.put(5, new Integer[]{1, 2});
        posPatterns.put(6, new Integer[]{2, 0});
        posPatterns.put(7, new Integer[]{2, 1});
        posPatterns.put(8, new Integer[]{2, 2});
    }

    public int mRoundNr;
    public int mMoveNr;
    public int[][] mBoard;
    public int[][] mMacroboard;

    private final static int COLS = 9;
    private final static int ROWS = 9;
    private String mLastError = "";

    //TODO
    final static int MACRO_WIN_SCORE = 1000000;
    final static int MICRO_WIN_SCORE = 1000;
    final static int PLAYER_CELL = 5;
    final static int EMPTY_CELL = 1;

    public Field() {
        mBoard = new int[COLS][ROWS];
        mMacroboard = new int[COLS / 3][ROWS / 3];
        clearBoard();
    }

    //  returns a cloned field
    public static Field clone(Field orig) {
        Field clone = new Field();
        for (int i = 0; i < COLS; i++)
            System.arraycopy(orig.mBoard[i], 0, clone.mBoard[i], 0, ROWS);

        for (int i = 0; i < COLS / 3; i++)
            System.arraycopy(orig.mMacroboard[i], 0, clone.mMacroboard[i], 0, ROWS / 3);

        clone.mRoundNr = orig.mRoundNr;
        clone.mMoveNr = orig.mMoveNr;

        return clone;
    }

    /**
     * Parse data about the game given by the engine
     *
     * @param key   : type of data given
     * @param value : value
     */
    public void parseGameData(String key, String value) {
        if (key.equals("round")) {
            mRoundNr = Integer.parseInt(value);
        } else if (key.equals("move")) {
            mMoveNr = Integer.parseInt(value);
        } else if (key.equals("field")) {
            parseFromString(value); /* Parse Field with data */
        } else if (key.equals("macroboard")) {
            parseMacroboardFromString(value); /* Parse macroboard with data */
        }
    }

    /**
     * Initialise field from comma separated String
     *
     * @param String :
     */
    public void parseFromString(String s) {
        System.err.println("Move " + mMoveNr);
        s = s.replace(";", ",");
        String[] r = s.split(",");
        int counter = 0;
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                mBoard[x][y] = Integer.parseInt(r[counter]);
                counter++;
            }
        }
    }

    /**
     * Initialise macroboard from comma separated String
     *
     * @param String :
     */
    public void parseMacroboardFromString(String s) {
        String[] r = s.split(",");
        int counter = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                mMacroboard[x][y] = Integer.parseInt(r[counter]);
                counter++;
            }
        }
    }

    public void clearBoard() {
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                mBoard[x][y] = 0;
            }
        }
    }

    public ArrayList<Move> getAvailableMoves() {
        ArrayList<Move> moves = new ArrayList<Move>();

        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (isInActiveMicroboard(x, y) && mBoard[x][y] == 0) {
                    moves.add(new Move(x, y));
                }
            }
        }

        return moves;
    }

    //  useful when given move's coordinates
    public Boolean isInActiveMicroboard(int x, int y) {
        return mMacroboard[(int) x / 3][(int) y / 3] == -1;
    }

    //  useful when given directly the square coordinates
    public Boolean isInActiveSquare(int x, int y) {
        return mMacroboard[x][y] == -1;
    }

    /**
     * Returns reason why addMove returns false
     *
     * @param args :
     * @return : reason why addMove returns false
     */
    public String getLastError() {
        return mLastError;
    }

    @Override
    /**
     * Creates comma separated String with player ids for the microboards.
     *
     * @param args
     *            :
     * @return : String with player names for every cell, or 'empty' when cell
     *         is empty.
     */
    public String toString() {
        String r = "";
        int counter = 0;
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (counter > 0) {
                    r += ",";
                }
                r += mBoard[x][y];
                counter++;
            }
        }
        return r;
    }

    /**
     * Checks whether the field is full
     *
     * @param args :
     * @return : Returns true when field is full, otherwise returns false.
     */
    public boolean isFull() {
        ArrayList<Move> moves = getAvailableMoves();
        if (moves.size() == 0) {
            return true;
        }
        for (int x = 0; x < COLS; x++)
            for (int y = 0; y < ROWS; y++)
                if (mBoard[x][y] == 0)
                    return false; // At least one cell is not filled
        // All cells are filled
        return true;
    }

    //  returns true if a given square contains no more available move
    public static boolean squareIsDraw(int[][] square) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (square[i][j] == 0)
                    return false;
            }
        }

        return true;
    }

    public int getNrColumns() {
        return COLS;
    }

    public int getNrRows() {
        return ROWS;
    }

    public boolean isEmpty() {
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                if (mBoard[x][y] > 0) {
                    return false;
                }
            }
        }
        return true;
    }


    /* Heuristics */
    //TODO
    public int evalMicro(int player, int[][] matr) {
        int opponent = (player == 1) ? (2) : (1);

        HashMap<Integer, Integer> val = new HashMap<>();
        val.put(0, 1);
        val.put(player, 10);
        val.put(opponent, 0);

        int score = 0;

        for (int i = 0; i < 3; i++) {

            int sl = 1, sc = 1;

            for (int j = 0; j < 3; j++) {
                sl *= val.get(matr[i][j]);
                sc *= val.get(matr[j][i]);
            }
            score += sl;

            score += sc;
        }

        int sd1 = 1, sd2 = 1;

        for (int i = 0; i < 3; ++i) {
            sd1 *= val.get(matr[i][i]);
            sd2 *= val.get(matr[2 - i][i]);
        }

        score += sd1;
        score += sd2;

        return score;
    }

    public int evalMacro(int[][] matr) {
        int score = 0;
        for (int i = 0; i < 3; i++) {

            int sl = 1, sc = 1;

            for (int j = 0; j < 3; j++) {
                sl *= matr[i][j];
                sc *= matr[j][i];
            }

            score += sl;
            score += sc;
        }

        int sd1 = 1, sd2 = 1;

        for (int i = 0; i < 3; ++i) {
            sd1 *= matr[i][i];
            sd2 *= matr[2 - i][i];
        }

        score += sd1;
        score += sd2;

        return score;
    }

    public int eval(int player) {
        int opponent = (player == 1) ? (2) : (1);

        if (isWinner(mMacroboard, player))
            return MACRO_WIN_SCORE;

        int[][] matr = new int[3][3];
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                if (mMacroboard[i][j] == player) {
                    matr[i][j] = MICRO_WIN_SCORE;
                } else if (mMacroboard[i][j] == opponent)
                    matr[i][j] = 0;
                else {
                    int[][] micro = getSquareFromBoard(i, j, mBoard);
                    matr[i][j] = evalMicro(player, micro);
                }
            }
        }
        return evalMacro(matr);
    }


    //  retrieves the square at the position (x, y) from the board
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


    //  simulates 'move' for 'player' on current field
    public boolean simulateMove(Move move, int player) {
        int x = move.x, y = move.y;
        int this_x = x / 3, this_y = y / 3;
        int sent_x = x % 3, sent_y = y % 3;

        mBoard[x][y] = player;

        int[][] thisSquare = getSquareFromBoard(this_x, this_y, mBoard);

        if (isWinner(thisSquare, player))
            mMacroboard[this_x][this_y] = player;
        else
            mMacroboard[this_x][this_y] = 0;

        int[][] sentSquare = getSquareFromBoard(sent_x, sent_y, mBoard);

        if (!squareIsDraw(sentSquare) && mMacroboard[sent_x][sent_y] <= 0) {
            mMacroboard[sent_x][sent_y] = -1;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (i == sent_x && j == sent_y)
                        continue;

                    if (!squareIsDraw(getSquareFromBoard(i, j, mBoard)) && mMacroboard[i][j] <= 0)
                        mMacroboard[i][j] = 0;
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int[][] aux = getSquareFromBoard(i, j, mBoard);

                    if (mMacroboard[i][j] == 0 && !squareIsDraw(aux))
                        mMacroboard[i][j] = -1;
                }
            }
        }


        return true;
    }


    //  undo 'move' and reset the field to 'orig'
    public void undoMove(Move move, Field orig) {
        int x = move.x, y = move.y;
        mBoard[x][y] = 0;
        for (int i = 0; i < COLS / 3; i++) {
            System.arraycopy(orig.mMacroboard[i], 0, this.mMacroboard[i], 0, ROWS / 3);
        }
    }


    //  used at final states: wins the entire game (without alpha-beta)
    public Move tryToWinGame(ArrayList<Move> moves, int player) {
        int pattern = constructPattern(mMacroboard, player);

        Integer[] squarePos;
        for (Integer wp : winningPatterns) {
            //  i know for sure that there is an optimal square
            if ((squarePos = getPos(pattern, wp)) != null) {
                int[][] aux_square = getSquareFromBoard(squarePos[0], squarePos[1], mBoard);

                int aux_pattern = constructPattern(aux_square, player);


                Integer[] movePos;
                for (Integer _wp : winningPatterns) {
                    //  i know for sure that there is an optimal move
                    if ((movePos = getPos(aux_pattern, _wp)) != null) {

                        int x = 3 * squarePos[0] + movePos[0];
                        int y = 3 * squarePos[1] + movePos[1];

                        Move m = new Move(x, y);
                        if (moves.contains(m))
                            return m;
                    }
                }
            }
        }

        return null;
    }

    static int constructPattern(int[][] square, int player) {
        int pattern = 0b000000000; // 9-bit pattern for the 9 cells
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (square[row][col] == player)
                    pattern |= (1 << (row * 3 + col));
            }
        }

        return pattern;
    }

    //  returns the position of the square that, if won, can lead to the end of the game in my favor
    static Integer[] getPos(int p, int wp) {
        int count = _f(p, wp);

        if (count == 2) {
            for (int i = 0; i < 9; i++) {
                int bit_p = (p >> i) & 1;
                int bit_wp = (wp >> i) & 1;

                //  lookup
                if ((bit_wp & 1) == 1 && (bit_p & 1) == 0)
                    return posPatterns.get(i);
            }
            return null;
        }

        return null;
    }


    // if one square matches a winning pattern, then 'player' wins the square
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

    public static int _f(int b1, int b2) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += ((b1 >> i) & 1) & ((b2 >> i) & 1);
        }
        return sum;
    }

    boolean multipleActiveSquares() {
        int count = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (mMacroboard[i][j] == -1)
                    count++;
                if (count >= 2)
                    return true;
            }
        }
        return false;
    }

    /**
     * Returns the player id on given column and row
     *
     * @param args : int column, int row
     * @return : int
     */
    public int getPlayerId(int column, int row) {
        return mBoard[column][row];
    }

    public int getRoundNr() {
        return this.mRoundNr;
    }
}