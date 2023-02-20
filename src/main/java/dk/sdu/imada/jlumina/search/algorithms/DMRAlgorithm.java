package dk.sdu.imada.jlumina.search.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import dk.sdu.imada.jlumina.search.primitives.DMRDescription;
import dk.sdu.imada.jlumina.search.primitives.DMR;

public class DMRAlgorithm {

	// . parameters 
	int k, w, l, e;
	int [] positions;
	String [] chrs;
	
	private float[] cpgPValues;

	public int[] getPositions() {
		return positions;
	}

	public void setPositions(int[] positions) {
		this.positions = positions;
	}

	public String[] getChrs() {
		return chrs;
	}

	public void setChrs(String[] chrs) {
		this.chrs = chrs;
	}

	/**
	 * Constructor for the searching Islands
	 * 
	 * @param k	number of allowed 0's
	 * @param w	window size for searching
	 * @param l	limit in base pairs of how distant two consecutive CpGs can be 
	 * @param np	number of permutations for testing the search significance
	 * @param e	number of allowed CpGs to extend the island size
	 * @param cpgPValues pvalues to calculate a p-value score for each cpg
	 */
	public DMRAlgorithm(int k, int w, int l, int e, int [] positions,  String [] chrs, float[] cpgPValues) {

		this.k = k;

		this.w = w;

		this.l = l;


		this.e = e;


		this.positions = positions;

		this.chrs = chrs;
		
		this.cpgPValues = cpgPValues;
	}


	private boolean isBreak(int being, int end, int bp[]) {
		for (int i = being; i < end; i++) {
			if (bp[i]==1) {
				return true;
			}
		}

		return false;
	}

	public ArrayList<DMR> islandSearch(int[] binaryArray){
		// breaking points
		int [] bp = getBreakingPoints(positions, chrs, l); 
		return islandSearch(binaryArray, bp);
	}

	/**
	 * Perform the Island search in the Binary array
	 * @param binaryArray an array representing the differentially methylated (1) and non (0) differentially CpG sites
	 * @return the found DMRs 
	 */
	public ArrayList<DMR> islandSearch(int[] binaryArray, int[] bp) {


		// ArrayList positions in which you have ones in the binary array
		HashMap<Integer,Integer> dmrMap = new HashMap<>();

		// global index for accessing the binaryArray
		int i = 0;

		//max index to go
		int limit = binaryArray.length;

		// array which counts how many CpGs an Island has. 
		//Every methylated CpG is a potential Island and it's distribution 
		///is associated with how many more methylated CpGs it has. 
		
		//System.out.println("w: " + w);

		while (i < (limit - this.w)) {

			if (binaryArray[i] == 1) {

				int zeros = countElements(binaryArray, i , i + w + 1, 0);
				boolean bpCondition = isBreak(i + 1, i + w + 1, bp);

				int islandIndex = i;
				int dmr_size = w ;

				boolean slidingCondition = zeros <= k && !bpCondition;

				if (slidingCondition) {
					
					//slide while the exceptions are fine and no breaking points are ahead
					while (slidingCondition) {

						// sliding the window
						dmr_size++;
						i++;
						
						if(i+w<limit){
							zeros += binaryArray[i - 1];
							zeros -= binaryArray[i + w];
							bpCondition = isBreak(i + w, i + w + 1, bp); //only last position needs check
						}
						else{
							slidingCondition = false;
						}

						if (zeros > k || bpCondition) {
							slidingCondition = false;
						}
					
					}
					dmrMap.put(islandIndex,dmr_size);

				}else {
					i++;
				}


			} else {
				i++;
			}
		}
		// . 
		ArrayList<DMR> islands = getIslands(positions, dmrMap, binaryArray, cpgPValues);
		setScores(islands, positions);
		return islands;
	}


	/**
	 * Generate a binary array with breaking points
	 * @param positions	genomic positions of the CpGs 
	 * @param chrs	chromosomes names of each CpG
	 * @param fdr value in base pairs allowed between two cosecutive CpGs
	 * @return	 binary array with breaking points
	 */
	public int[] getBreakingPoints(int positions[], String[]chrs, int limit) {

		int [] breakingPoints = new int[positions.length];

		//for (int i = 0; i < breakingPoints.length; i++) breakingPoints[i] = 0;

		for (int i = 1; i < positions.length; i++) {

			if ((positions[i] - positions[i-1] > limit) || !chrs[i].equals(chrs[i-1])) {
				breakingPoints[i] = 1;
			}
		}

		return breakingPoints;
	}


	
	private ArrayList<DMR> getIslands(int [] positions, HashMap<Integer,Integer> dmrMap, int[] binaryArray, float[] cpgPValues) {

		ArrayList<DMR> islands = new ArrayList<DMR>();
		Integer[] dmrStartIndices = dmrMap.keySet().toArray(new Integer[dmrMap.keySet().size()]);
		Arrays.sort(dmrStartIndices);
		
		for(Integer startIndex : dmrStartIndices){
			DMR island = new DMR();
			island.beginPosition = startIndex;
			island.totalCpgs = dmrMap.get(startIndex);
			
			while (binaryArray[island.beginPosition + island.totalCpgs - 1] == 0) {
				island.totalCpgs--;
			}
			
			island.setCpgPValues(Arrays.copyOfRange(cpgPValues, island.beginPosition, island.beginPosition + island.totalCpgs));
			if(island.totalCpgs != Arrays.copyOfRange(cpgPValues, island.beginPosition, island.beginPosition + island.totalCpgs).length){
				System.out.println("error in getIslands()"); 
			}
			islands.add(island);
		}

		return islands;
	}
	
	
	/**
	 * Count how many e are in the array range
	 * @param array the array
	 * @param start inclusive start
	 * @param end exclusive end
	 * @param e the element to count
	 * @return
	 */

	private int countElements(int [] array, int start, int end, int e) {

		int count  = 0;
		
		for(int i = start; i < end; i++){
			if(array[i]==e){
				count++;
			}
		}
		return count;
	}


	/**
	 * Set the score values for a group of Island
	 * 
	 * @param islands	arraylist with the islands
	 * @param positions	array with the genomic positions of all CpGs
	 */
	private void setScores(ArrayList<DMR> islands, int[] positions) {

		for (DMR i : islands) {

			int begin = i.beginPosition;
			int end = i.beginPosition + i.totalCpgs - 1;
			int cBegin = positions[begin];
			int cEnd = positions[end];

			int distance = cEnd - cBegin + 1;
			i.islandLength = distance;
			i.score = (float) i.totalCpgs/(float) distance;
			

		}
	}

	/**
	 * Count how many Islands exists with at least s number of CpGs
	 * 
	 * @param s	reference number
	 * @param islands	results from a island search
	 * @return number of islands of at least "s" CpGs
	 */
	public int countIslandsOfAtLeastSize(int s, ArrayList<DMR> islands ) {

		int count = 0;

		for (DMR i : islands) {
			if (i.totalCpgs >= s) count++;
		}

		return count;
	}

	/**
	 * @param islands
	 * @return	array with the unique CpG distributions among the islands 
	 * 
	 */
	public int[] getUniqueCpGSDist(ArrayList<DMR> islands) {

		ArrayList<Integer> cpgs = new ArrayList<Integer>();
		for (DMR i : islands) {
			cpgs.add(i.totalCpgs);
		}

		HashSet<Integer> hValues = new HashSet<Integer>(cpgs);

		int values[] = new int[hValues.size()];

		int index = 0;
		for (Integer v : hValues) {
			values[index] = v;
			index++;
		}

		return values;
	}


	public void writeIslands(ArrayList<DMR> islands, String fname) {

		try {

			File file = new File(fname);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("begin.cpg, total.cpg, score, length.bp\n");
			for (DMR i : islands) {
				bw.write(i.log()+"\n");
			}

			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeBinaryArray(int[] bin, String fname) {

		try {

			File file = new File(fname);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("begin.cpg, total.cpg, score, length.bp\n");
			for (int i : bin) {
				bw.write(i + "\n");
			}

			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public int getMaxDMR(ArrayList<DMR> islands) {

		int max = 0;

		for (DMR i : islands) {
			if (i.totalCpgs > max) {
				max = i.totalCpgs; 
			}
		}
		return max;
	}


	public void writeDMRSummary(ArrayList<DMRDescription> dmrDescriptions, String fname) {
		try {
			File file = new File(fname);

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("Chr, Begin, End, begin.CpG, end.CpG, score\n");
			for (DMRDescription d : dmrDescriptions) {
				bw.write(d.getChromosome() + ", ");
				bw.write(d.getBeginPosition() + ", ");
				bw.write(d.getEndPosition() + ", ");
				bw.write(d.getBeginCPG()+ ", ");
				bw.write(d.getEndCPG() + ", ");
				bw.write(d.getIsland().score + "\n");
			}

			bw.close();

		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public int getW() {
		return w;
	}


	public void setW(int w) {
		this.w = w;
	}

	public int getL() {
		return l;
	}

	public void setL(int l) {
		this.l = l;
	}

	public int getE() {
		return e;
	}

	public void setE(int e) {
		this.e = e;
	}

}
