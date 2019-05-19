package hr.fer.mv.rl;

public class RLSettings {
	private final Object lockEpsilon = new Object();
	private final Object lockStepSize = new Object();
	private double epsilon;
	private double stepSize;

	public RLSettings(double epsilon, double stepSize) {
		this.epsilon = epsilon;
		this.stepSize = stepSize;
	}

	public double getEpsilon() {
		synchronized (lockEpsilon) {
			return epsilon;
		}
	}

	public void setEpsilon(double epsilon) {
		synchronized (lockEpsilon) {
			this.epsilon = epsilon;
		}
	}

	public double getStepSize() {
		synchronized (lockStepSize) {
			return stepSize;
		}
	}

	public void setStepSize(double stepSize) {
		synchronized (lockStepSize) {
			this.stepSize = stepSize;
		}
	}
}
