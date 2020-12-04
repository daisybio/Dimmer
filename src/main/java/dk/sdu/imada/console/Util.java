package dk.sdu.imada.console;

import java.util.HashSet;

import dk.sdu.imada.jlumina.core.primitives.Grouping;

public class Util {
	
	
	/**
	 * Distributes a number of permutations as  evenly as possible on a number of threads 
	 * 
	 * @param numThreads
	 * @param numPermutations
	 * @return an int array (one position for each thread) with the number of permutations that the thread should execute
	 */
	public static int[] distributePermutations(int numThreads, int numPermutations){
		
		if(numThreads <= 0 || numPermutations <= 0){
			System.out.println("Impossible to distribute "+numPermutations+" permutations to "+numThreads+" threads!");
			return null;
		}
		
		int[] result = new int[numThreads];
		int count = numPermutations / numThreads;
		int rest = numPermutations % numThreads;
		
		for(int i = 0; i < result.length; i++){
			result[i] = count;
			if(i<rest){
				result[i]++;
			}
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param array
	 * @return true, if the array contains exactly two distinct values
	 */
	public static boolean checkBinary(String[] array){
		HashSet<String> set = new HashSet<>();
		for(String s: array){
			set.add(s);
		}
		if(set.size()==2){
			return true;
		}
		else{
			return false;
		}
	}
	
	//main for testing
	public static void main(String[] args){
		String[] col = new String[] {"2","2","3","3","2"};
		Grouping gr = new Grouping(col);
		System.out.println("indices:");
		gr.getIndices();
		System.out.println("SplitPoint:");
		System.out.println(gr.getSplitPoint());
		System.out.println("SplitPoints:");
		gr.getSplitPoints();
		System.out.println(gr.log());
	} 
	
	

}
