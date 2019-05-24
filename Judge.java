package hr.fer.mv.rl;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

public class Judge implements ActionListener {

	public static final int X_SYMBOL = 1;
	public static final int O_SYMBOL = 2;
	public static final int Empty_SYMBOL = 0;

	public static AtomicInteger RLPlayerTimeout;
	public static AtomicInteger gameoverTimeout;

	private AtomicBoolean humanPlaying;
	private AtomicInteger humanPlayerSimbol;
	private AtomicReference<GameMark> humanPlayerMark;
	private AtomicReference<GameMark> humanOpponentMark;
	private AtomicReference<RLPlayer> humanRLOpponent;

	private GameMark RLPlayer1Mark;
	private GameMark RLPlayer2Mark;

	private RLPlayer RLPlayer1;
	private RLPlayer RLPlayer2;

	// Trenutno stanje ploce
	private int[][] state;
	private GameBoardDisplay gameBoardDisplay;
	// Semafor koji sluzi za zaustavljanje igre sve dok covjek ne odabere polje
	private Semaphore waitForHumanPlayer;
	// red i stupac polja koje je covjek odabrao
	private int lastSelectedRow, lastSelectedColumn;
	private int moveCounter = 0;

	public Judge(GameBoardDisplay gbd) {
		gameBoardDisplay = gbd;
		state = new int[Dependencies.GAME_SIZE][Dependencies.GAME_SIZE];
		// Inicijalizacija semafora s jednom dozvolom
		waitForHumanPlayer = new Semaphore(1);
		// Potrosi jedinu dozvolu, dozvolu dobivamo tek kada igrac napravi odabir
		try {
			waitForHumanPlayer.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		RLPlayer1 = new RLPlayer(Dependencies.DEFAULT_EPSILON, Dependencies.DEFAULT_STEP_SIZE, X_SYMBOL);
		RLPlayer1Mark = GameMark.X;
		RLPlayer2 = new RLPlayer(Dependencies.DEFAULT_EPSILON, Dependencies.DEFAULT_STEP_SIZE, O_SYMBOL);
		RLPlayer2Mark = GameMark.O;

		RLPlayerTimeout = new AtomicInteger(Dependencies.DEFAULT_RLPLAYER_TIMEOUT);
		gameoverTimeout = new AtomicInteger(Dependencies.DEFAULT_GAMEOVER_TIMEOUT);
		humanPlaying = new AtomicBoolean(false);
		humanPlayerSimbol = new AtomicInteger(X_SYMBOL);
		humanPlayerMark = new AtomicReference<GameMark>(GameMark.X);
		humanOpponentMark = new AtomicReference<GameMark>(GameMark.O);
		humanRLOpponent = new AtomicReference<RLPlayer>(RLPlayer2);

		// Dodajemo event listenere na sva polja
		try {
			SwingUtilities.invokeAndWait(() -> {
				for (int i = 0; i < Dependencies.GAME_SIZE; i++)
					for (int j = 0; j < Dependencies.GAME_SIZE; j++)
						gameBoardDisplay.addPointListener(i, j, this);
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Oznacava polje u redu row i stupcu column s znakom gm
	private void setPointMark(int row, int column, GameMark gm) {
		try {
			SwingUtilities.invokeAndWait(() -> gameBoardDisplay.setPointMark(row, column, gm));
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Ispisuje broj number u polje u redu row i stupcu column
	private void setPointNumber(int row, int column, double number) {
		try {
			SwingUtilities.invokeAndWait(() -> gameBoardDisplay.setPointNumber(row, column, number));
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Ispisuje brojke estimations u polja ploce,
	// brojke se ispisuju samo u polja koja su prazna kako nebi prepisali neki od
	// znakova
	private void showEstimations(double[][] estimations) {
		for (int i = 0; i < Dependencies.GAME_SIZE; i++)
			for (int j = 0; j < Dependencies.GAME_SIZE; j++)
				if (state[i][j] == Empty_SYMBOL)
					setPointNumber(i, j, estimations[i][j]);
	}

	private int counter = 0;

	// Prazni plocu, signalizira igracima da spreme napredak te ih u konacnici
	// resetira. Tie oznacava nerijesenost.
	private void reset(boolean tie, boolean backup) {
		counter++;
		if (counter % 100000 == 0)
			System.out.println(counter);

		if (gameoverTimeout.get() > 0)
			try {
				Thread.sleep(gameoverTimeout.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		for (int i = 0; i < Dependencies.GAME_SIZE; i++)
			for (int j = 0; j < Dependencies.GAME_SIZE; j++)
				state[i][j] = Empty_SYMBOL;

		// Ponovno crtanje ploce nas jako usporava, kada su timeouti nula ne osvjezavamo
		// plocu jer ionako ne vidimo pojedinacne poteze
		if (RLPlayerTimeout.get() > 0 || gameoverTimeout.get() > 0)
			for (int i = 0; i < Dependencies.GAME_SIZE; i++)
				for (int j = 0; j < Dependencies.GAME_SIZE; j++)
					setPointMark(i, j, GameMark.Empty);
		moveCounter = 0;
		// Spremanje napretka
		if (backup) {
			RLPlayer1.backup(tie);
			RLPlayer2.backup(tie);
		}
		RLPlayer1.reset();
		RLPlayer2.reset();
	}

	// Ceka covjekov unos te ga obraduje. Vraca true kada je trenutna partija
	// gotova, partija
	// zavrsava kada covjek odustane od igre ili kada pobjedi
	private boolean reset = false;

	private boolean humanPlay() {
		while (true) {
			// cekamo dozvolu koja se pojavljuje tek kada covjek odabere jedno od polja
			try {
				waitForHumanPlayer.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (reset) {
				reset = false;
				reset(true, false);
				return true;
			}
			// izlazimo iz petlje tek onda kada covjek odabere prazno polje
			if (state[lastSelectedRow][lastSelectedColumn] == Empty_SYMBOL)
				break;
		}
		double stateValue = Util.getNextStateInitialValue(state, lastSelectedRow, lastSelectedColumn,
				humanPlayerSimbol.get());
		state[lastSelectedRow][lastSelectedColumn] = humanPlayerSimbol.get();
		moveCounter++;
		setPointMark(lastSelectedRow, lastSelectedColumn, humanPlayerMark.get());
		// Nerijeseno onda kada se ploca popuni
		boolean tie = moveCounter == Dependencies.GAME_SIZE * Dependencies.GAME_SIZE;
		if (stateValue == 1 || tie) {
			reset(tie, true);
			return true;
		}
		return false;
	}

	public void play() {
		for (int i = 0; i < Dependencies.GAME_SIZE; i++)
			for (int j = 0; j < Dependencies.GAME_SIZE; j++)
				state[i][j] = Empty_SYMBOL;
		System.out.println("Game started");
		while (true) {
			if (humanPlaying.get()) {
				if (humanPlayerSimbol.get() == X_SYMBOL) {
					showEstimations(
							(humanRLOpponent.get() == RLPlayer1 ? RLPlayer2 : RLPlayer1).getStateEstimates(state));
					// covjek prestao igrati ili pobjedio
					if (humanPlay())
						continue;
				}

				Point p2 = humanRLOpponent.get().act(state);
				moveCounter++;
				setPointMark(p2.x, p2.y, humanOpponentMark.get());
				if (humanRLOpponent.get().lastStateEnd()) {
					reset(!humanRLOpponent.get().lastStateWinner(), true);
					continue;
				}

				if (humanPlayerSimbol.get() == O_SYMBOL) {
					showEstimations(
							(humanRLOpponent.get() == RLPlayer1 ? RLPlayer2 : RLPlayer1).getStateEstimates(state));
					humanPlay();
				}
			} else {
				// Prikazujemo vrijednosti pojedinih akcija na ploci
				if (RLPlayerTimeout.get() > 0 || gameoverTimeout.get() > 0)
					showEstimations(RLPlayer1.getStateEstimates(state));
				Point p1 = RLPlayer1.act(state);
				moveCounter++;
				if (RLPlayerTimeout.get() > 0)
					try {
						Thread.sleep(RLPlayerTimeout.get());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				if (RLPlayerTimeout.get() > 0 || gameoverTimeout.get() > 0)
					setPointMark(p1.x, p1.y, RLPlayer1Mark);
				// printBoard(state);
				if (RLPlayer1.lastStateEnd()) {
					reset(!RLPlayer1.lastStateWinner(), true);
					continue;
				}

				if (RLPlayerTimeout.get() > 0 || gameoverTimeout.get() > 0)
					showEstimations(RLPlayer2.getStateEstimates(state));
				Point p2 = RLPlayer2.act(state);
				moveCounter++;
				if (RLPlayerTimeout.get() > 0)
					try {
						Thread.sleep(RLPlayerTimeout.get());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				if (RLPlayerTimeout.get() > 0 || gameoverTimeout.get() > 0)
					setPointMark(p2.x, p2.y, RLPlayer2Mark);
				// printBoard(state);
				if (RLPlayer2.lastStateEnd())
					reset(!RLPlayer2.lastStateWinner(), true);
			}
		}
	}

	public void setHumanPlayerSymbol(int symbol) {
		humanPlayerSimbol.set(symbol);
		humanPlayerMark.set(symbol == X_SYMBOL ? GameMark.X : GameMark.O);
		humanOpponentMark.set(symbol == X_SYMBOL ? GameMark.O : GameMark.X);
		humanRLOpponent.set(symbol == RLPlayer1.getSymbol() ? RLPlayer2 : RLPlayer1);
	}

	public void setHumanPlaying(boolean humanPlaying) {
		this.humanPlaying.set(humanPlaying);
		reset = true;
		waitForHumanPlayer.release();
	}

	public void setRLPlayer1Settings(double epsilon, double stepSize) {
		RLPlayer1.setParamaters(epsilon, stepSize);
	}

	public void setRLPlayer2Settings(double epsilon, double stepSize) {
		RLPlayer2.setParamaters(epsilon, stepSize);
	}

	public void setRLPlayerTimeout(int timeout) {
		RLPlayerTimeout.set(timeout);
	}

	public void setGameoverTimeout(int timeout) {
		gameoverTimeout.set(timeout);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		XOButton btn = (XOButton) ae.getSource();
		lastSelectedRow = btn.getRow();
		lastSelectedColumn = btn.getColumn();
		waitForHumanPlayer.release();
	}

	public static void printBoard(int[][] state) {
		for (int i = 0; i < Dependencies.GAME_SIZE; i++) {
			for (int j = 0; j < Dependencies.GAME_SIZE; j++) {
				if (state[i][j] == X_SYMBOL)
					System.out.print("X");
				else if (state[i][j] == O_SYMBOL)
					System.out.print("O");
				else
					System.out.print("_");
			}
			System.out.println("");
		}
		System.out.println("");
	}

}
