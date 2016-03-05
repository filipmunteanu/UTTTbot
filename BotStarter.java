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

	public int[] winningPatterns = { 0b111000000, 0b000111000, 0b000000111, // rows
			0b100100100, 0b010010010, 0b001001001, // cols
			0b100010001, 0b001010100 // diagonals
	};

	// Decides if player is the winner of a tile
	public boolean winner(int[][] tile, int player) {
		int pattern = 0b000000000; // 9-bit pattern for the 9 cells
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 3; ++col) {
				if (tile[row][col] == player)
					pattern |= (1 << (row * 3 + col));
			}
		}
		for (int winningPattern : winningPatterns)
			if ((pattern & winningPattern) == winningPattern)
				return true;
		return false;
	}

	// Returns the winner of a tile
	public int getTileWinner(int[][] tile) {
		if (winner(tile, 1))
			return 1;
		if (winner(tile, 2))
			return 2;
		return 0;
	}

	// Returns a specific tile
	public int[][] getTile(int x, int y, int[][] board) {
		int[][] tile = new int[3][3];
		int k1, k2;
		k1 = 0;
		for (int i = x * 3; i < (x * 3 + 3); i++) {
			k2 = 0;
			for (int j = y * 3; j < (y * 3 + 3); j++) {
				tile[k1][k2] = board[i][j];
				k2++;
			}
			k1++;
		}
		return tile;
	}

	/*
	 * Don't forget! getTileWinner(board, x, y): if (x,y) : outliers return
	 * outliers[(x,y)]
	 */

	// Returns the winner of a specific tile
	public int getSpecificTileWinner(int[][] board, int x, int y) {
		int[][] tile = getTile(x, y, board);
		return getTileWinner(tile);
	}

	// Decides the winner of the board
	public int globalWinner(int[][] winners) {
		if (winner(winners, 1))
			return 1;
		if (winner(winners, 2))
			return 2;
		return 0;
	}

	// Returns the winner of the game
	public int[][] getGlobalWinner(int[][] board) {
		int[][] winners = new int[3][3];
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				winners[i][j] = getSpecificTileWinner(board, i, j);
		return winners;
	}

	// Checks if the move wins a tile
	public Move checkWonTile(int[][] board, ArrayList<Move> moves) {
		int x, y;
		int[][] tile;
		for (int k = 0; k < moves.size(); k++) {
			x = moves.get(k).getX();
			y = moves.get(k).getY();
			tile = getTile(x / 3, y / 3, board); // Make it better
			tile[x % 3][y % 3] = BotParser.mBotId;
			if (winner(tile, BotParser.mBotId))
				return moves.get(k);
		}
		return null;
	}

	// Specifies how the bot playes
	public Move makeTurn(Field field) {
		Random r = new Random();
		ArrayList<Move> moves;
		int[][] board = field.getmBoard(), macroB = field.getmMacroboard();

		Move m = new Move();
		// Take the center first, if available.
		if (board[4][4] == 0) {
			m.mX = 4;
			m.mY = 4;
			return m;
		}

		// If more options available, put in the center tile
		if (macroB[1][1] == -1) {
			moves = field.getAvailableMovesTile(1, 1);
			if ((m = checkWonTile(board, moves)) != null)
				return m;
		}
		moves = field.getAvailableMoves();

		// Choose the spot which leads to winning a tile
		if ((m = checkWonTile(board, moves)) != null)
			return m;

		// Random, but not in the center of a tile, unless it's the center tile
		while (true) {
			// IMPLEMENT ALARM!!!!!
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
