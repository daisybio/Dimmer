package dk.sdu.imada.jlumina.search.util;

public class PairedShuffle extends RandomizeLabels {

	public PairedShuffle(int[] values) {
		super(values);
	}
	
	public PairedShuffle(int[] values, long seed) {
		super(values,seed);
	}

	@Override
	/**
	 * switches the pairs with 50/50 chance each
	 */
	public void shuffle() {
//		for (int j = 0; j <  array.length; j+=2) {
//			int flip = rnd.nextInt(2);
//			array[j] = j + flip;
//			array[j+1] = j - flip + 1;
//		}
		int half = array.length / 2;
		for(int i = 0; i < half; i++){
			int flip = rnd.nextInt(2);
			if(flip==1){
				int temp = array[i];
				array[i] = array[i+half];
				array[i+half] = temp;
			}
		}
	}

}
