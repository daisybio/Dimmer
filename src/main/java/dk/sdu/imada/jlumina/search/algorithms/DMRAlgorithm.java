package dk.sdu.imada.jlumina.search.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import dk.sdu.imada.jlumina.search.primitives.DMRDescription;
import dk.sdu.imada.jlumina.search.primitives.DMR;

public class DMRAlgorithm {

	// . parameters 
	int k, w, l, e;
	int [] positions;
	String [] chrs;

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
	 */
	public DMRAlgorithm(int k, int w, int l, int e, int [] positions,  String [] chrs) {

		this.k = k;

		this.w = w;

		this.l = l;


		this.e = e;


		this.positions = positions;

		this.chrs = chrs;
	}


	private boolean isBreak(int being, int end, int bp[]) {
		for (int i = being; i < end; i++) {
			if (bp[i]==1) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Perform the Island search in the Binary array
	 * @param binaryArray an array representing the differentially methylated (1) and non (0) differentially CpG sites
	 * @return the found DMRs 
	 */
	public ArrayList<DMR> islandSearch(int[] binaryArray) {

		// breaking points
		int [] bp = getBreakingPoint(positions, chrs, l); 

		// ArrayList positions in which you have ones in the binary array
		ArrayList<Integer> cpgStartList = methylationStartPoints(binaryArray); 


		// Array of positions in which you have ones in the binary array
		int [] cpgStarts = new int[cpgStartList.size()];
		for (int i = 0; i < cpgStartList.size(); i++) cpgStarts[i] = cpgStartList.get(i);

		// global index for access the binaryArray
		int i = 0;

		//max index to go
		int limit = binaryArray.length;

		// array which counts how many CpGs an Island have. 
		//Every methylated CpG is a potential Island and it's distribution 
		///is associated with how many more methylated CpGs it has. 
		int [] islandDistribution = new int[cpgStarts.length];
		
		//System.out.println("w: " + w);

		while (i < (limit - this.w)) {

			if (binaryArray[i] == 1) {

				int [] slice = Arrays.copyOfRange(binaryArray, i, i + w+1);

				int zeros = countElements(slice, 0);
				boolean bpCondition = isBreak(i+1, i+w+1, bp);

				int islandIndex = 0;
				int it = 0;

				boolean slidingCondition = false;

				if (zeros <= k && !bpCondition) {
					
					slidingCondition = true;

					//slide while the exceptions are fine and no breaking points are ahead
					while (slidingCondition) {

						// if it is the first iteration we need to check where the DMR start
						if (it == 0) {
							islandIndex = cpgStartList.indexOf(i);
						}

						//telling how many CpG sites the DMR have
						islandDistribution[islandIndex]++;

						// sliding the window
						i++;
						
						//if statement added by Johannes
						if(i+w<limit){
							slice = Arrays.copyOfRange(binaryArray, i, i + w+1);
							zeros = countElements(slice, 0);
							bpCondition = isBreak(i+1, i+w+1, bp);
						}
						else{
							slidingCondition = false;
						}

						
						if (zeros > k || bpCondition) {
							slidingCondition = false;
						}
						
						it++;
					}

					// correcting the total number of CpG sites in the window
					islandDistribution[islandIndex]+=w;

				}else {
					i++;
				}


			} else {
				i++;
			}
		}
		// . 
		ArrayList<DMR> islands = getIslands(cpgStarts, islandDistribution, binaryArray);
		setScores(islands, positions);
		return islands;
	}
	/**
	 * Perform the Island search in the Binary array
	 * 
	 * @param binaryArray	int array with 0 for non-methylated 1 for diff methylated, up or down .
	 * @param positions	int array with genomic positions of each CpG
	 * @param chrs	String array with the chromosomes of each CpG
	 * @return	An array of Island, which contains: the first CpG position from the Island, the total of CpGs in the island, score, log ratio, lenght in base pairs
	 */
	/*public ArrayList<DMR> islandSearch(int[] binaryArray) {

		// breaking points
		int [] bp = getBreakingPoint(positions, chrs, l); 

		// ArrayList positions in which you have ones in the binary array
		ArrayList<Integer> cpgStartList = methylationStartPoints(binaryArray); 


		// Array of positions in which you have ones in the binary array
		int [] cpgStarts = new int[cpgStartList.size()];
		for (int i = 0; i < cpgStartList.size(); i++) cpgStarts[i] = cpgStartList.get(i);

		// global index for access the binaryArray
		int i = 0;

		//max index to go
		int limit = binaryArray.length;

		// array which counts how many CpGs an Island have. Every methylated CpG is a potential Island and it's distribution is associated with how many more methylated CpGs it has. 
		int [] islandDistribution = new int[cpgStarts.length];

		while (i < (limit - this.w)) {

			boolean searchCondition = false;

			if (binaryArray[i] == 1) {

				int [] sliceBP = Arrays.copyOfRange(bp, i+1, i + w + 1);

				boolean breakPointCondition = countElements(sliceBP, 1) == 0;

				if (breakPointCondition) {

					int [] slice = Arrays.copyOfRange(binaryArray, i, i + w + 1);

					int zeros = countElements(slice, 0);

					if (zeros <= k) {

						searchCondition = true;

						//int islandIndex = Arrays.asList(cpgStarts).indexOf(i);
						int islandIndex = cpgStartList.indexOf(i);

						islandDistribution[islandIndex] = w + 1;

						int increment = i + w + e;

						while (searchCondition) {

							if (increment <= limit) {

								if (bp[increment] == 0) {

									slice = Arrays.copyOfRange(binaryArray, i, increment + 1);

									zeros = countElements(slice, 0);

									if (zeros <= k) {

										increment += e;

										islandDistribution[islandIndex]+=e;

									}else {
										searchCondition = false;
										i = increment + 1;
									}

								} else {

									searchCondition = false;

									i = increment;
								}

							}else {

								searchCondition = false;
							}
						}

					} else {
						// correcting index if the non-methylated  > 2
						i = i + w + 1;
					} 
				} else {
					// correcting index if the island has breaking points
					i++;
				}

			} else {
				// correcting index if the island has methylation 0 
				i++;
			}
		}
		// . 
		ArrayList<DMR> islands = getIslands(cpgStarts, islandDistribution, binaryArray);

		setScores(islands, positions);

		return islands;
	}*/

	/**
	 * Generate a binary array with breaking points
	 * @param positions	genomic positions of the CpGs 
	 * @param chrs	chromosomes names of each CpG
	 * @param fdr value in base pairs allowed between two cosecultives CpGs
	 * @return	 binary array with breaking points
	 */
	private int[] getBreakingPoint(int positions[], String[]chrs, int limit) {

		int [] breakingPoints = new int[positions.length];

		for (int i = 0; i < breakingPoints.length; i++) breakingPoints[i] = 0;

		for (int i = 1; i < positions.length; i++) {

			if ((positions[i] - positions[i-1] > limit) || !chrs[i].equals(chrs[i-1])) {
				breakingPoints[i] = 1;
			}
		}

		return breakingPoints;
	}

	/**
	 * Every methylated (ones in the binaryArray) CpG is a potential Island for each position we associate a total number of Islands stored in islandDistribution. If islandDistribution > 0 you have an Island.
	 * However this value can be reduced if the edges are non-methylated CpGs (zeros in the binary array)
	 * 
	 * @param positions	of all methylated CpGs 
	 * @param islandDistribution	number of CpGs for the potential Island
	 * @param binaryArray	binary array 
	 * @return	return the and array of islands
	 */
	private ArrayList<DMR> getIslands(int [] positions, int [] islandDistribution, int[] binaryArray) {

		ArrayList<DMR> islands = new ArrayList<DMR>();

		for (int i = 0; i < positions.length; i++) {

			DMR island = new DMR();

			island.beginPosition = positions[i];

			island.totalCpgs = islandDistribution[i];

			if (islandDistribution[i] > 0) {

				while (binaryArray[island.beginPosition + island.totalCpgs - 1] == 0) {
					island.totalCpgs--;
				}
				// int aux [] = Arrays.copyOfRange(binaryArray, island.beginPosition, island.beginPosition + island.totalCpgs);
				islands.add(island);
			}
		}

		return islands;
	}

	/**
	 * Count how many e are in the array 
	 * @param array
	 * @param e
	 * @return
	 */
	private int countElements(int [] array, int e) {

		int count  = 0;

		for (int i : array) {
			if (i == e) {
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
	 * 
	 * @param binaryArray
	 * @return integer arraylist with the index positions of CpGs with value 1
	 */
	private ArrayList<Integer> methylationStartPoints(int [] binaryArray) {

		ArrayList<Integer> cpgStartList = new ArrayList<Integer>();

		for (int i = 0; i < binaryArray.length; i++) {
			if (binaryArray[i] == 1) {
				cpgStartList.add(i);
			}
		}

		return cpgStartList;
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
