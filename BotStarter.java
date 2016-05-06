
import java.util.ArrayList;

public class BotStarter {
    /**
     * Makes a turn. Edit this method to make your bot smarter.
     *
     * @return The column where the turn was made.
     */
    public static Move makeTurn(Field field) {
        //  get the available moves for this game's state
        ArrayList<Move> moves = field.getAvailableMoves();

        Move nextMove = null;

        //  if the first game simulateMove is mine simulateMove in the center
        if (field.getRoundNr() == 1 && field.isInActiveSquare(1, 1)) {
            Move center = new Move(4, 4);
            if (moves.contains(center)) {
                return center;
            }
        }

        //  if I have to simulateMove in an empty square, do it so the opponent's simulateMove
        //  will be in the same square -> better control
        if (isEmptySquare(moves)) {
            System.err.println("empty square");
            Move aux = moves.get(0);
            return new Move(4 * (aux.getX() / 3), 4 * (aux.getY() / 3));
        }

        //  TODO: optimize
        //  if there is a square that can be won directly
        //  that will lead to the end of the game in my favor
        if (field.multipleActiveSquares()) {
            if ((nextMove = field.tryToWinGame(moves, BotParser.mBotId)) != null) {
                System.err.println("instantly win game");
                return nextMove;
            }
        }


        //  get best simulateMove using minimax
        int score = Integer.MIN_VALUE;

        //  dynamically compute depth, based upon the number of available moves
        int depth = getDepth(moves.size(), field);

        //  clone current field in order to be able to simulate moves
        Field clone = Field.clone(field);

        //  for each available simulateMove
        for (Move m : moves) {
            //  simulate current simulateMove
            clone.simulateMove(m, BotParser.mBotId);

            //  calculate this simulateMove's score
            int currentScore = minimax(clone, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, BotParser.mEnemyId);

            //  undo this simulateMove
            clone.undoMove(m, field);

            //  update general score and simulateMove
            if (currentScore > score) {
                score = currentScore;
                nextMove = m;
            }
        }

        System.err.println(nextMove);
        return nextMove;
    }


    //  dynamically compute depth
    //  depth is inversely proportional to the moves' size
    public static int getDepth(int movesSize, Field field) {
        int depth = 7;

        if (movesSize > 7) {
            depth = (int) (5 / Math.log10(movesSize));
            if (depth <= 0) {
                depth = 1;
            }
        }

        if (movesSize < 6) {
            depth = 8;
        }

        if (movesSize < 5) {
            depth = 9;
        }

        if (BotParser.mTimeLeft < 4000 && depth > 3) {
            depth = 3;
        } else if (BotParser.mTimeLeft < 2000 && depth > 1) {
            depth = 1;
        }
        if ((field.mMoveNr < 18) && (movesSize > 7))
            depth = 4;

        return depth;
    }


    //  minimax + alpha-beta pruning
    public static int minimax(Field field, int depth, int alpha, int beta, int player) {
        if (depth == 0 || field.isFull()) {
            return field.eval(BotParser.mBotId) - field.eval(BotParser.mEnemyId);
        }

        int score;

        ArrayList<Move> moves = field.getAvailableMoves();

        if (moves.size() > 7) {
            int newDepth = (int) (5 / Math.log10(moves.size()));

            if (newDepth <= 0)
                newDepth = 1;

            if (newDepth < depth)
                depth = newDepth;
        }

        //  clone current field
        Field clone = Field.clone(field);

        // my turn
        if (player == BotParser.mBotId) {
            //  start pessimistic
            score = Integer.MIN_VALUE;

            //  for each available simulateMove
            for (Move m : moves) {

                //  simulate current simulateMove m
                clone.simulateMove(m, BotParser.mBotId);

                //  calculate this simulateMove's score
                int currentScore = minimax(clone, depth - 1, alpha, beta, BotParser.mEnemyId);

                //  undo current simulateMove
                clone.undoMove(m, field);

                //  update scores
                if (currentScore > score) {
                    score = currentScore;
                    alpha = score;

                    //  pruning
                    if (alpha >= beta) {
                        return score;
                    }
                }
            }
        }
        //  enemy's turn
        else {
            //  start pessimistic
            score = Integer.MAX_VALUE;

            //  for each available simulateMove
            for (Move m : moves) {

                //  simulate current enemy's simulateMove
                clone.simulateMove(m, BotParser.mEnemyId);

                //  calculate this simulateMove's score
                int currentScore = minimax(clone, depth - 1, alpha, beta, BotParser.mBotId);

                //  undo current simulateMove
                clone.undoMove(m, field);

                //  update scores
                if (currentScore < score) {
                    score = currentScore;
                    beta = score;

                    //  pruning
                    if (alpha >= beta) {
                        return score;
                    }
                }
            }
        }

        return score;
    }


    //  checks whether a square is empty by looking at the available moves
    public static boolean isEmptySquare(ArrayList<Move> moves) {
        if (moves.size() != 9) {
            return false;
        }
        //  first and last indices must differ by 2
        else {
            return ((moves.get(0).getX() + 2 == moves.get(8).getX()) &&
                    (moves.get(0).getY() + 2 == moves.get(8).getY()));
        }
    }


    public static void main(String[] args) {
        BotParser parser = new BotParser(new BotStarter());
        parser.run();
//
//        Field field = new Field();
//
//        int[][] macro = {
//                {1, 0, 2},
//                {-1, 0, -1},
//                {1, 2, 0}
//        };
//
//        int[][] board = {
//                {1, 2, 1, 0, 1, 1, 1, 2, 1},
//                {0, 0, 2, 2, 0, 0, 2, 0, 1},
//                {0, 0, 1, 0, 1, 0, 2, 2, 2},
//                {0, 0, 2, 2, 0, 2, 0, 2, 1},
//                {1, 0, 1, 0, 1, 0, 0, 0, 0},
//                {0, 0, 0, 0, 1, 0, 0, 2, 1},
//                {0, 0, 1, 0, 0, 2, 2, 0, 0},
//                {0, 0, 0, 0, 2, 2, 2, 0, 0},
//                {1, 1, 2, 2, 1, 0, 2, 0, 1}
//        };
//
//        field.mMacroboard = macro;
//        field.mBoard = board;
//
//        //System.out.println(field.move(0, 4, 1));
//
////        for (int i = 0; i < 3; i++) {
////            for (int j = 0; j < 3; j++) {
////                System.out.print(field.mMacroboard[i][j] + " ");
////            }
////            System.out.println();
////        }
//
//        ArrayList<Move> moves = new ArrayList<>();
//        moves.add(new Move(4, 1));
//        moves.add(new Move(3, 0));
//        moves.add(new Move(5, 2));
//
//        int p = 0b100000100;
//        int wp = 0b100100100;
//
//        System.out.println(makeTurn(field));
//

    }
}
