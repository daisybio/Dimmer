package dk.sdu.imada.jlumina.search.util;

import java.util.Arrays;
import java.util.Random;

public abstract class RandomizeLabels {
	
	int array[];
	Random rnd;
	
	public RandomizeLabels(int [] values) {
		this.array = Arrays.copyOf(values,values.length);
		rnd = new Random(System.currentTimeMillis());
	}
	
	public RandomizeLabels(int [] values, long seed) {
		this.array = Arrays.copyOf(values,values.length);
		rnd = new Random(seed);
	}
	
	public int[] getShuffledArray() {
		return array;
	}
	
	public abstract void shuffle();
}
