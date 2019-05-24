package hr.fer.mv.rl;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.JButton;

public class XOButton extends JButton {

	private GameMark buttonSkin = GameMark.Empty;
	private int row;
	private int column;
	private double number;
	private Font font;
	private Stroke stroke;

	public XOButton(GameMark bs, int row, int column) {
		this.buttonSkin = bs;
		this.row = row;
		this.column = column;
		font = getFont().deriveFont(Dependencies.FONT_SIZE);
		stroke = new BasicStroke(Dependencies.SHAPE_STROKE);
	}

	public void setButtonMark(GameMark gm) {
		buttonSkin = gm;
	}

	public void setButtonNumber(double number) {
		this.number = number;
		buttonSkin = GameMark.Number;
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(stroke);
		g.setFont(font);
		int width = getSize().width;
		int height = getSize().height;
		if (buttonSkin == GameMark.X) {	
			g.drawLine(width / 4, height / 4, width * 3 / 4, height * 3 / 4);
			g.drawLine(width * 3 / 4, height / 4, width / 4, height * 3 / 4);
		} else if (buttonSkin == GameMark.O) {
			g.drawOval(width / 4, height / 4, width / 2, height / 2);
		} else if (buttonSkin == GameMark.Number) {
			g2d.drawString("" + number, width / 2, height / 2);
		}
	}
}
