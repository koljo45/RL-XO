package hr.fer.mv.rl;

import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

public class GameBoardDisplay extends JPanel {

	private XOButton[][] buttons;

	public GameBoardDisplay() {
		buttons = new XOButton[Dependencies.GAME_SIZE][Dependencies.GAME_SIZE];

		this.setLayout(new GridLayout(0, Dependencies.GAME_SIZE));
		for (int i = 0; i < Dependencies.GAME_SIZE; i++)
			for (int j = 0; j < Dependencies.GAME_SIZE; j++)
				this.add((buttons[i][j] = new XOButton(GameMark.Empty, i, j)));
	}

	public void setPointMark(int row, int column, GameMark gm) {
		buttons[row][column].setButtonMark(gm);
		repaint();
	}

	public void setPointNumber(int row, int column, double number) {
		setPointMark(row, column, GameMark.Number);
		buttons[row][column].setButtonNumber(number);
	}

	public void addPointListener(int row, int column, ActionListener al) {
		buttons[row][column].addActionListener(al);
	}
}
