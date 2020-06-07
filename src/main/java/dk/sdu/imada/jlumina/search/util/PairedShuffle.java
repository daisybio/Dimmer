package dk.sdu.imada.jlumina.search.util;

public class PairedShuffle extends RandomizeLabels {

	public PairedShuffle(int[] values) {
		super(values);
	}

	@Override
	public void shuffle() {
		for (int j = 0; j <  array.length; j+=2) {
			int flip = rnd.nextInt(2);
			array[j] = j + flip;
			array[j+1] = j - flip + 1;
		}
	}

}
