package hr.fer.mv.rl;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class MainFrame extends JFrame implements Runnable, PropertyChangeListener, ActionListener {

	private GameBoardDisplay gamePanel;
	private Judge judge;
	private double epsilonRL1 = Dependencies.DEFAULT_EPSILON;
	private double stepSizeRL1 = Dependencies.DEFAULT_STEP_SIZE;
	private double epsilonRL2 = Dependencies.DEFAULT_EPSILON;
	private double stepSizeRL2 = Dependencies.DEFAULT_STEP_SIZE;
	private JFormattedTextField epsilonRL1TF;
	private JFormattedTextField stepSizeRL1TF;
	private JFormattedTextField epsilonRL2TF;
	private JFormattedTextField stepSizeRL2TF;
	private JToggleButton toggleX;
	private JToggleButton toggleO;
	private int RLTimeout = Dependencies.DEFAULT_RLPLAYER_TIMEOUT;
	private int gameoverTimeout = Dependencies.DEFAULT_GAMEOVER_TIMEOUT;

	public MainFrame() {
		// Frame initialization
		setSize(Dependencies.FRAME_SIZE);
		//setResizable(false);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle(Dependencies.GAME_NAME);
		this.setFocusable(true);
		setVisible(true);

		gamePanel = new GameBoardDisplay();
		add(gamePanel, BorderLayout.CENTER);

		// Stvaramo tekstualna polja koja kao unos primaju samo brojeve
		epsilonRL1TF = new JFormattedTextField(NumberFormat.getNumberInstance());
		epsilonRL1TF.setValue(epsilonRL1);
		epsilonRL1TF.setColumns(5);
		epsilonRL1TF.addPropertyChangeListener("value", this);
		stepSizeRL1TF = new JFormattedTextField(NumberFormat.getNumberInstance());
		stepSizeRL1TF.setValue(stepSizeRL1);
		stepSizeRL1TF.setColumns(5);
		stepSizeRL1TF.addPropertyChangeListener("value", this);
		epsilonRL2TF = new JFormattedTextField(NumberFormat.getNumberInstance());
		epsilonRL2TF.setValue(epsilonRL2);
		epsilonRL2TF.setColumns(5);
		epsilonRL2TF.addPropertyChangeListener("value", this);
		stepSizeRL2TF = new JFormattedTextField(NumberFormat.getNumberInstance());
		stepSizeRL2TF.setValue(stepSizeRL2);
		stepSizeRL2TF.setColumns(5);
		stepSizeRL2TF.addPropertyChangeListener("value", this);

		JPanel panelRLSettings = new JPanel();
		panelRLSettings.setLayout(new GridLayout(0, 4));
		panelRLSettings.add(new JLabel("epsilon(RL Player 1): "));
		panelRLSettings.add(epsilonRL1TF);
		panelRLSettings.add(new JLabel("step size(RL Player 1): "));
		panelRLSettings.add(stepSizeRL1TF);

		panelRLSettings.add(new JLabel("epsilon(RL Player 2): "));
		panelRLSettings.add(epsilonRL2TF);
		panelRLSettings.add(new JLabel("step size(RL Player 2): "));
		panelRLSettings.add(stepSizeRL2TF);
		add(panelRLSettings, BorderLayout.NORTH);

		JPanel panelGameSettings = new JPanel();
		panelGameSettings.setLayout(new GridLayout(0, 2));

		toggleX = new JToggleButton("Play X");
		toggleO = new JToggleButton("Play O");
		toggleX.addActionListener(this);
		toggleO.addActionListener(this);
		toggleX.setSelected(true);

		panelGameSettings.add(toggleX);
		panelGameSettings.add(toggleO);
		add(panelGameSettings, BorderLayout.SOUTH);

		JSlider sliderRLTimeout = new JSlider(JSlider.VERTICAL, 0, 2000, Dependencies.DEFAULT_RLPLAYER_TIMEOUT);
		JSlider sliderGameoverTimeout = new JSlider(JSlider.VERTICAL, 0, 2000, Dependencies.DEFAULT_GAMEOVER_TIMEOUT);
		sliderRLTimeout.addChangeListener(e -> {
			boolean oldZero = RLTimeout == 0;
			RLTimeout = sliderRLTimeout.getValue();
			judge.setRLPlayerTimeout(RLTimeout);
			if (RLTimeout == 0 && gameoverTimeout == 0 && !oldZero && !toggleX.isSelected() && !toggleO.isSelected())
				showTimeoutNoticeDialog();

		});
		sliderGameoverTimeout.addChangeListener(e -> {
			boolean oldZero = gameoverTimeout == 0;
			gameoverTimeout = sliderGameoverTimeout.getValue();
			judge.setGameoverTimeout(gameoverTimeout);
			if (gameoverTimeout == 0 && RLTimeout == 0 && !oldZero && !toggleX.isSelected() && !toggleO.isSelected())
				showTimeoutNoticeDialog();
		});

		JPanel sliderPanel1 = new JPanel();
		sliderPanel1.setLayout(new GridLayout(0, 2));
		sliderPanel1.add(new JLabel(Util.transformStringToHtml("RL Timeout Slider")));
		sliderPanel1.add(sliderRLTimeout);
		add(sliderPanel1, BorderLayout.WEST);

		JPanel sliderPanel2 = new JPanel();
		sliderPanel2.setLayout(new GridLayout(0, 2));
		sliderPanel2.add(sliderGameoverTimeout);
		sliderPanel2.add(new JLabel(Util.transformStringToHtml("Gameover Timeout Slider")));
		add(sliderPanel2, BorderLayout.EAST);

		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();

	}

	private void showTimeoutNoticeDialog() {
		JOptionPane.showMessageDialog(null,
				"Vremenske pauze za odigrani potez i za kraj partije su sada 0.\n"
						+ "Korisnièko suèelje više neæe biti osvježavano.\n"
						+ "Sustav u pozadini igra partije maksimalnom brzinom.",
				"Napomena", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void run() {
		judge = new Judge(gamePanel);
		judge.setHumanPlaying(true);
		judge.play();
	}

	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		if (pce.getSource() instanceof JFormattedTextField) {
			JFormattedTextField source = (JFormattedTextField) pce.getSource();
			double number = ((Number) source.getValue()).doubleValue();
			if (source == epsilonRL1TF || source == epsilonRL2TF) {
				if (number > 1)
					number = 1;
				else if (number < 0)
					number = 0;
			} else if (source == stepSizeRL1TF || source == stepSizeRL2TF) {
				if (number > 1)
					number = 1;
				else if (number < -1)
					number = -1;
			}
			source.setValue(Double.valueOf(number));

			if (source == epsilonRL1TF)
				epsilonRL1 = number;
			else if (source == stepSizeRL1TF)
				stepSizeRL1 = number;
			else if (source == epsilonRL2TF)
				epsilonRL2 = number;
			else if (source == stepSizeRL2TF)
				stepSizeRL2 = number;

			judge.setRLPlayer1Settings(epsilonRL1, stepSizeRL1);
			judge.setRLPlayer2Settings(epsilonRL2, stepSizeRL2);
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() instanceof JToggleButton) {
			JToggleButton source = (JToggleButton) ae.getSource();
			JToggleButton other = toggleO;
			int symbol = Judge.X_SYMBOL;
			if (source == toggleO) {
				other = toggleX;
				symbol = Judge.O_SYMBOL;
			}

			if (source.isSelected()) {
				other.setSelected(false);
				judge.setHumanPlayerSymbol(symbol);
				judge.setHumanPlaying(true);
			} else {
				if (RLTimeout == 0 && gameoverTimeout == 0)
					showTimeoutNoticeDialog();
				judge.setHumanPlaying(false);
			}
		}
	}

	public static void main(String[] args) {
		try {
			SwingUtilities.invokeAndWait(() -> {
				MainFrame mainFrame = new MainFrame();
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
