package dk.sdu.imada.console;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
	
	public static boolean containsNegatives(float[] array){
		for(int i = 0; i< array.length; i++){
			if(array[i]<0){
				return true;
			}
		}
		return false;
	}
	
	public static void writeBetaMatrix(String path, float[][] beta, String[] sentrixID, String[] sentrixPosition, String[] cpgIDs){
		System.out.println("Writing beta matrix to "+path);
		try{
			BufferedWriter br = new BufferedWriter(new FileWriter(new File(path)));	
			//write header
			br.write("CpGid");
			for(int i = 0; i < sentrixID.length; i++){
				br.write(","+sentrixID[i]+"_"+sentrixPosition[i]);
			}
			br.write("\n");
			
			for(int i = 0; i < beta.length; i++){
				br.write(cpgIDs[i]);
				for(int j = 0; j < beta[0].length; j++){
					br.write(","+beta[i][j]);
				}
				br.write("\n");
			}
			br.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}

	}
	
	/**
	 * 
	 * @return the  errors as a single String
	 */
	public static String errorLog(ArrayList<String> errors){
		StringBuilder builder = new StringBuilder();
		for(String error: errors){
			builder.append("ERROR\t");
			builder.append(error);
			builder.append("\n");
		}
		return builder.toString();
	}
	
	public static String warningLog(ArrayList<String> warnings){
		StringBuilder builder = new StringBuilder();
		for(String error: warnings){
			builder.append("WARN\t");
			builder.append(error);
			builder.append("\n");
		}
		return builder.toString();
	}
	

	public static String errorLog(String error){
		return "ERROR\t"+error;
	}
	
	public static String warningLog(String warning){
		return "WARN\t"+warning;
	}
	
	public static String log(ArrayList<String> logs){
		StringBuilder builder = new StringBuilder();
		for(String log: logs){
			builder.append(log);
			builder.append("\n");
		}
		return builder.toString();
	}
	
	public static long getMaxMemory() {
	    return Runtime.getRuntime().maxMemory();
	}

	public static long getUsedMemory() {
	    return getMaxMemory() - getFreeMemory();
	}

	public static long getTotalMemory() {
	    return Runtime.getRuntime().totalMemory();
	}

	public static long getFreeMemory() {
	    return Runtime.getRuntime().freeMemory();
	}
	
	public static String memoryLog(){
		long max = getMaxMemory();
		long used = getUsedMemory();
		long total = getTotalMemory();
		long free = getFreeMemory();
		return "Memory in GB:\nMax: " + (max/(double)1000000000) + " Used: " + (used/(double)1000000000) + " Total: " + (total/(double)1000000000) + " Free: " + (free/(double)1000000000);
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
