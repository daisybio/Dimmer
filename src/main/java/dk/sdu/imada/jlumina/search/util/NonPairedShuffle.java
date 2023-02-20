package dk.sdu.imada.jlumina.search.util;

public class NonPairedShuffle extends RandomizeLabels {

	public NonPairedShuffle(int[] values) {
		super(values);
	}
	
	public NonPairedShuffle(int[] values, long seed) {
		super(values,seed);
	}

	@Override
	public void shuffle() {
		for (int i = array.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			// Simple swap
			int a = array[index];
			array[index] = array[i];
			array[i] = a;
		}	
	}
}
