package hr.fer.mv.rl;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class Util {

	public static Random rnd = ThreadLocalRandom.current();

	public static long hashState(int[][] state) {
		long hash = 0;
		for (int i = 0; i < Dependencies.GAME_SIZE; i++)
			for (int j = 0; j < Dependencies.GAME_SIZE; j++)
				hash = hash * 3 + state[i][j];
		return hash;
	}

	public static long hashNextState(int[][] state, int k, int l, int symbol) {
		int temp_symbol = state[k][l];
		state[k][l] = symbol;
		long hash = hashState(state);
		state[k][l] = temp_symbol;
		return hash;
	}

	// Raèuna vrijednost stanja state nakon što dodamo simbol symbol na mjesto u
	// redu row i stupcu column. Raèun se temlji na igri
	// križiæ kružiè tako da æe stanje biti maksimalne vrijednosti ako je igraè
	// postavio svoj simbol u horizontalnu, vertikalnu ili dijagonalnu liniju. Ako
	// igraè nije pobjedio pretpostavlja se da runda još traje. Vrijednost pobjednog
	// stanj je 1, vrijednost nedovršene partije je 0.5. Provjeravamo jeli novo
	// dodanoi simbol formirao ijednu punu liniju.
	public static double getNextStateInitialValue(int[][] state, int row, int column, int symbol) {
		int temp = state[row][column];
		state[row][column] = symbol;
		// Provjera horizontale
		int cnt = 0;
		for (int k = column; k < Dependencies.GAME_SIZE; k++)
			if (state[row][k] == symbol)
				cnt++;
			else
				break;
		for (int k = column - 1; k >= 0; k--)
			if (state[row][k] == symbol)
				cnt++;
			else
				break;
		if (cnt == Dependencies.GAME_LEVEL) {
			state[row][column] = temp;
			return 1;
		}
		// Provjera vertikale
		cnt = 0;
		for (int k = row; k < Dependencies.GAME_SIZE; k++)
			if (state[k][column] == symbol)
				cnt++;
			else
				break;
		for (int k = row - 1; k >= 0; k--)
			if (state[k][column] == symbol)
				cnt++;
			else
				break;
		if (cnt == Dependencies.GAME_LEVEL) {
			state[row][column] = temp;
			return 1;
		}
		// Provjera prve dijagonale
		cnt = 0;
		for (int k = 0; (row - k >= 0) && (column + k < Dependencies.GAME_SIZE); k++)
			if (state[row - k][column + k] == symbol)
				cnt++;
			else
				break;
		for (int k = 1; (row + k < Dependencies.GAME_SIZE) && (column - k >= 0); k++)
			if (state[row + k][column - k] == symbol)
				cnt++;
			else
				break;
		if (cnt == Dependencies.GAME_LEVEL) {
			state[row][column] = temp;
			return 1;
		}
		// Provjera druge dijagonale
		cnt = 0;
		for (int k = 0; (row + k < Dependencies.GAME_SIZE) && (column + k < Dependencies.GAME_SIZE); k++)
			if (state[row + k][column + k] == symbol)
				cnt++;
			else
				break;
		for (int k = 1; (row - k >= 0) && (column - k >= 0); k++)
			if (state[row - k][column - k] == symbol)
				cnt++;
			else
				break;
		if (cnt == Dependencies.GAME_LEVEL) {
			state[row][column] = temp;
			return 1;
		}
		state[row][column] = temp;
		return 0.5;
	}
	
	// Vraæa vertikalni tekst
	public static String transformStringToHtml(String strToTransform) {
	    String ans = "<html>";
	    String br = "<br>";
	    String[] lettersArr = strToTransform.split("");
	    for (String letter : lettersArr) {
	        ans += letter + br;
	    }
	    ans += "</html>";
	    return ans;
	}

	private Util() {
	}
}