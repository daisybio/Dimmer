package dk.sdu.imada.jlumina.search.util;

public class Bootstraping extends RandomizeLabels {

	public Bootstraping(int[] values) {
		super(values);
	}

	@Override
	public void shuffle() {
		int total = array.length;
		for (int i = 0; i < total; i++) {
			array[i] = rnd.nextInt(total);
		}
	}

}
