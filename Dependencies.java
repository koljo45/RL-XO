package hr.fer.mv.rl;

import java.awt.Dimension;

public final class Dependencies {

	public static final String GAME_NAME = "RL XO";
	public static final Dimension FRAME_SIZE = new Dimension(836, 900);
	public static final int GAME_SIZE = 3;
	public static final int GAME_LEVEL = 3;
	public static final int DEFAULT_RLPLAYER_TIMEOUT = 500;
	public static final int DEFAULT_GAMEOVER_TIMEOUT = 500;
	public static final float FONT_SIZE = 12;
	public static final float SHAPE_STROKE = 3;
	public static final double DEFAULT_EPSILON = 0.01;
	public static final double DEFAULT_STEP_SIZE = 0.1;

	private Dependencies() {
	}

}
