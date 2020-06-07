package dk.sdu.imada.jlumina.search.util;

import java.util.Random;

public abstract class RandomizeLabels {
	
	int array[];
	Random rnd;
	
	public RandomizeLabels(int [] values) {
		this.array = values;
		rnd = new Random(System.currentTimeMillis());
	}
	
	public int[] getShuffledArray() {
		return array;
	}
	
	public abstract void shuffle();
}
