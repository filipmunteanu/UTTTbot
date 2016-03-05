// // Copyright 2016 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package bot;

import java.util.ArrayList;
import java.util.Random;

/**
 * BotStarter class
 * 
 * Magic happens here. You should edit this file, or more specifically the
 * makeTurn() method to make your bot do more than random moves.
 * 
 * @author Jim van Eeden <jim@starapple.nl>
 */

public class BotStarter {

	public static int[] winningPatterns = { 0b111000000, 0b000111000, 0b000000111, // rows
			0b100100100, 0b010010010, 0b001001001, // cols
			0b100010001, 0b001010100 // diagonals
	};

	public static boolean winner(int[][] board, int player) {
		int pattern = 0b000000000; // 9-bit pattern for the 9 cells
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 3; ++col) {
				if (board[row][col] == player)
					pattern |= (1 << (row * 3 + col));
			}
		}
		for (int winningPattern : winningPatterns)
			if ((pattern & winningPattern) == winningPattern)
				return true;
		return false;
	}

	// Function decides if player in winner
	public static boolean isPlayerWinner(int[][] board, int player) {

		// check rows and columns
		boolean win = true;
		for (int i = 0; i < 3; i++) {
			win = true;
			for (int j = 0; j < 3; j++) {
				if (board[i][j] != player)
					win = false;
					break;
			}
		}

		if (win)
			return true;
		
		for (int i = 0; i < 3; i++) {
			win = true;
			for (int j = 0; j < 3; j++) {
				if (board[j][i] != player)
					win = false;
					break;
			}
		}
		if (win)
			return true;

		// check diagonals
		win = true;
		for (int i = 0; i < 3; i++)
			if (board[i][i] != player)
				win = false;	
		if (win)
			return true;
		
		win = true;
		for (int i = 0; i < 3; i++)
			if (board[i][2-i] != player)
				win = false;	
		if (win)
			return true;
		
		return false;
	}

	// Genereaza o matr de 3x3 in care adauga si mutarea curenta.
	public static int[][] getBoard(int x, int y, Field field) {
		int[][] board = new int[3][3];
		int k1, k2;
		k1 = 0;
		for (int i = x - (x % 3); i < (x - (x % 3) + 3); i++) {
			k2 = 0;
			for (int j = y - (y % 3); j < (y - (y % 3) + 3); j++) {
				board[k1][k2] = field.mBoard[i][j];
				k2++;
			}
			k1++;
		}
		board[x % 3][y % 3] = BotParser.mBotId;

		return board;
	}

	// Functia asta ar trebui sa verifice daca mutarea castiga un patrat.
	public Move checkWonTile(Field field, ArrayList<Move> moves) {
		int x, y;
		int[][] board;
		for (int k = 0; k < moves.size(); k++) {
			x = moves.get(k).getX();
			y = moves.get(k).getY();
			board = getBoard(x, y, field); // Make it better
			if (winner(board, BotParser.mBotId))
				return moves.get(k);
		}
		return null;
	}

	/**
	 * Makes a turn. Edit this method to make your bot smarter. Currently does
	 * only random moves.
	 *
	 * @return The column where the turn was made.
	 */

	public Move makeTurn(Field field) {
		Random r = new Random();
		ArrayList<Move> moves;

		Move m = new Move();
		// Ocupa intai centrul
		if (field.mBoard[4][4] == 0) {
			m.mX = 4;
			m.mY = 4;
			return m;
		}
		// Daca poti sa pui in mai multe patrate pune in centru
		if (field.mMacroboard[1][1] == -1) {
			moves = field.getAvailableMovesTile(1, 1);
			if ((m = checkWonTile(field, moves)) != null)
				return m;
		}
		moves = field.getAvailableMoves();
		// Alege casuta care castiga patratul
		if ((m = checkWonTile(field, moves)) != null)
			return m;
		// Random, dar nu centrul patratului
		while (true) {
			// IMPLEMENT ALARM!!!
			m = moves.get(r.nextInt(
					moves.size())); /* get random move from available moves */
			if ((m.getX() % 3 != 1 || m.getY() % 3 != 1) || moves.size() == 1)
				return m;
		}
	}

	public static void main(String[] args) {
		 BotParser parser = new BotParser(new BotStarter());
		 parser.run();

		// Test board
	/*	int[][] brd = { { 0, 0, 0, 0, 0, 0, 1, 0, 1 }, 
						{ 0, 2, 0, 1, 0, 0, 0, 0, 0 }, 
						{ 2, 0, 0, 0, 0, 0, 1, 0, 0 },
						{ 0, 0, 0, 1, 0, 0, 0, 0, 0 }, 
						{ 0, 2, 0, 1, 0, 0, 0, 0, 0 }, 
						{ 0, 0, 0, 1, 0, 1, 2, 0, 0 },
						{ 0, 0, 0, 1, 0, 2, 0, 1, 0 }, 
						{ 0, 0, 0, 1, 0, 1, 0, 0, 0 }, 
						{ 0, 0, 0, 1, 0, 0, 0, 0, 0 } };
		Field field = new Field();
		field.mBoard = brd;
		// BotParser parser = new BotParser();
		BotParser.mBotId = 1;
		int[][] matrix = getBoard(1, 7, field);
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.print("\n");
		}
		System.out.println(isPlayerWinner(matrix, 1));
		System.out.println(winner(matrix, 1));*/
	}
}
