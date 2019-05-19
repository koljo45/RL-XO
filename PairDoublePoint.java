package hr.fer.mv.rl;

import java.awt.Point;

public class PairDoublePoint extends Pair<Double, Point> implements Comparable<PairDoublePoint> {

	public PairDoublePoint(Double d, Point p) {
		super(d, p);
	}

	@Override
	public int compareTo(PairDoublePoint plp) {
		return first.compareTo(plp.first);
	}

}
