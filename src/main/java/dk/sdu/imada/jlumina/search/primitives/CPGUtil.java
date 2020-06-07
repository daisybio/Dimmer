package dk.sdu.imada.jlumina.search.primitives;

import java.util.Random;

/**
 * @author diogo
 * Misc functions for randomizing arrays in the paired and unpaired permutation
 */
public class CPGUtil {
	
	public CPGUtil() {}

	public int[] randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
		int index[] = new int[max];
		
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    
	    for (int i = 0; i < max; i++) {
	    	
	    	index[i] = rand.nextInt((max - min)) + min;
	    }

	    return index;
	}
	  
	/**
	 * Shuffle an given double array
	 * @param ar 
	 */
	public void shuffleArray(float[] ar) {
	    Random rnd = new Random();
	    
	    for (int i = ar.length - 1; i > 0; i--) {
	    	
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      float a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	      
	    }
	}
	
	/**
	 * Shuffle a given int array
	 * @param ar
	 */
	public void shuffleArray(int[] ar) {
	    Random rnd = new Random();
	    
	    for (int i = ar.length - 1; i > 0; i--) {
	    	
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      int a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	}
	
	/*public static void main(String args[]) {
		double[] solutionArray = { 1, 2, 3, 4, 5, 6, 7, 8};
		CPGUtil u = new CPGUtil();
		for (int i = 0; i < 1000; i++) {
			u.shuffleArray(solutionArray);
			System.out.println("");
		}
	}*/
}
