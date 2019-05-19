package hr.fer.mv.rl;

public class Pair<K, V> {
	public K first;
	public V second;

	public Pair(K f, V s) {
		first = f;
		second = s;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair))
			return false;
		if (!first.equals(((Pair) o).first) || !second.equals(((Pair) o).second))
			return false;
		return true;
	}

}
