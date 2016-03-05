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
	/*def is_winner(board, player):
	# check by row
	for i in range(N):
		found = True

		for j in range(N):
			if board[i][j]!=player:
				found = False
				break

		if found:
			return True
	
	# check by column
	for i in range(N):
		found = True

		for j in range(N):
			if board[j][i]!=player:
				found = False
				break

		if found:
			return True
	
	found = True

	# check diagnol
	for i in range(N):
		if board[i][i]!=player:
			found = False
			break

	if found:
		return True

	found = True

	# check diagnol
	for i in range(N):
		if board[i][N-i-1]!=player:
			found = False
			break

	if found:
		return True
	
	return False*/
	// Function decides if player in winner
	public boolean isPlayerWinner(int[][] board, int player){
		//check rows
		boolean yes;
		for(int i = 0; i < 3; i++) {
			yes = true;
			for(int j = 0)
		}
		# check by row
		for i in range(N):
			found = True

			for j in range(N):
				if board[i][j]!=player:
					found = False
					break

			if found:
				return True
		return false;
	}

	// Genereaza o matr de 3x3 in care adauga si mutarea curenta.
	public int[][] getBoard(int x, int y, Field field) {
		int[][] board = new int[3][3];
		int k1, k2;
		k1 = 0;
		for (int i = x / 3; i < x / 3 + 3; i++) {
			k2 = 0;
			for (int j = y / 3; j < y / 3 + 3; j++) {
				board[k1][k2] = field.mBoard[i][j];
				k2++;
			}
			k1++;
		}
		board[x % 3][y % 3] = BotParser.mBotId;

		return board;
	}

	// Functia asta ar trebui sa verifice daca mutarea castiga un patrat.
	// NEFUNCTIONAL :(
	public Move checkWonTile(Field field, ArrayList<Move> moves) {
		int x, y;
		for (int k = 0; k < moves.size(); k++) {
			x = moves.get(k).getX();
			y = moves.get(k).getY();
			int[][] board = getBoard(x, y, field);
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
			m = moves.get(r.nextInt(moves.size()));
			return m;
		}
		moves = field.getAvailableMoves();
		// Alege casuta care castiga patratul
		if ((m = checkWonTile(field, moves)) != null)
			return m;
		// Random, dar nu centrul patratului
		while (true) {
			m = moves.get(0);//r.nextInt(moves.size())); /* get random move from available moves */
			if ((m.getX() % 3 != 1 || m.getY() % 3 != 1) || moves.size() == 1)
				return m;
		}
	}

	public static void main(String[] args) {
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}
}
