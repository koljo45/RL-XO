package hr.fer.mv.rl;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RLPlayer {
	private RLSettings settings;
	private Map<Long, Double> estimates;
	private List<Long> states;
	private List<Boolean> greedy;
	private int symbol;
	private boolean lastStateEnd;
	private boolean lastStateWinner;
	private double[][] stateEstimates;

	public RLPlayer(double epsilon, double stepSize, int symbol) {
		estimates = new HashMap<>();
		states = new ArrayList<>();
		greedy = new ArrayList<>();
		estimates.put(Long.valueOf(0), Double.valueOf(0));
		settings = new RLSettings(epsilon, stepSize);
		this.symbol = symbol;
		stateEstimates = new double[Dependencies.GAME_SIZE][Dependencies.GAME_SIZE];
	}

	public void reset() {
		lastStateEnd = false;
		lastStateWinner = false;
		states.clear();
		greedy.clear();
	}

	private double getNextStateEstimate(int[][] state, int i, int j) {
		long nextStateHash = Util.hashNextState(state, i, j, symbol);
		Double nextStateEstimate = estimates.get(nextStateHash);
		if (nextStateEstimate == null) {
			nextStateEstimate = Util.getNextStateInitialValue(state, i, j, symbol);
			estimates.put(nextStateHash, nextStateEstimate);
		}
		return nextStateEstimate;
	}

	int counter = 0;

	// Sprema napredak racunajuci nove vrijednosti odigranih stanja.
	// S(t) = S(t) + (S(t+1) - S(t))*stepSize
	// S(t) je stanje ploce u koraku t, a S(t+1) stanje ploce u koraku t+1
	public void backup(boolean tie) {
		counter++;
		if (counter % 100000 == 0)
			System.out.println(symbol + ": " + estimates.size());
		// Ako smo izgubili moramo dodati stanje vrijednosti 0(minimalna vrijednost) na
		// zadnje mjesto kako bi se vrijednost dotad odigranih stanja spustila
		if (!lastStateWinner && !tie)
			states.add((long) 0);
		for (int i = states.size() - 2; i >= 0; i--) {
			long stateHash = states.get(i);
			double stateEstimate = estimates.get(stateHash);
			double nextStateEstimate = estimates.get(states.get(i + 1));
			double td_error = (greedy.get(i) ? 1 : 0) * (nextStateEstimate - stateEstimate);
			estimates.replace(stateHash, (stateEstimate + stateEstimate * td_error * settings.getStepSize()));
		}
	}

	public void setParamaters(double epsilon, double stepSize) {
		settings.setEpsilon(epsilon);
		settings.setStepSize(stepSize);
	}

	public void setSymbol(int symbol) {
		this.symbol = symbol;
	}

	public int getSymbol() {
		return symbol;
	}

	public boolean lastStateEnd() {
		return lastStateEnd;
	}

	public boolean lastStateWinner() {
		return lastStateWinner;
	}

	public double[][] getStateEstimates(int[][] state) {
		for (int i = 0; i < Dependencies.GAME_SIZE; i++)
			for (int j = 0; j < Dependencies.GAME_SIZE; j++)
				if (state[i][j] == Judge.Empty_SYMBOL)
					stateEstimates[i][j] = getNextStateEstimate(state, i, j);
		return stateEstimates;
	}

	private List<PairDoublePoint> next_positions_estimates = new ArrayList<>();

	public Point act(int[][] state) {
		next_positions_estimates.clear();
		// Prolazimo kroz sva prazna polja kako bi provjerili koliko je
		// vrijedno oznaciti ih
		for (int i = 0; i < Dependencies.GAME_SIZE; i++)
			for (int j = 0; j < Dependencies.GAME_SIZE; j++)
				if (state[i][j] == Judge.Empty_SYMBOL) {
					next_positions_estimates
							.add(new PairDoublePoint(getNextStateEstimate(state, i, j), new Point(i, j)));
				}
		// Ako je samo jedno polje slobodno oznaci da je kraj igre
		if (next_positions_estimates.size() == 1)
			lastStateEnd = true;

		PairDoublePoint next_position;
		// Generiramo nasumicnu broj u rasponu [0,1], ako je manji od epsilona
		// izvrsavamo nasumican potez
		if (Util.rnd.nextDouble() < settings.getEpsilon()) {
			next_position = next_positions_estimates.get(Util.rnd.nextInt(next_positions_estimates.size()));
			greedy.add(false);
		} else {
			// Shuffle jer ne zelimo da nam se medu jednako vrijednim akcijama uvijek
			// odabire ista
			Collections.shuffle(next_positions_estimates);
			Collections.sort(next_positions_estimates);
			next_position = next_positions_estimates.get(next_positions_estimates.size() - 1);
			greedy.add(true);
		}
		state[next_position.second.x][next_position.second.y] = symbol;
		// Dodaj novo stanje u povijest odigranih stanja
		states.add(Util.hashState(state));

		// Provjeri vrijednost upravo odigranog stanja, ako je 1 pobjedili smo
		if (estimates.get(states.get(states.size() - 1)) == 1) {
			lastStateEnd = true;
			lastStateWinner = true;
		}
		return next_position.second;
	}

}
