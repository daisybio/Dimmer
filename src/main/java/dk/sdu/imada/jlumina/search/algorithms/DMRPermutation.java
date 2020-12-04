package dk.sdu.imada.jlumina.search.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;

import dk.sdu.imada.jlumina.search.primitives.DMRPermutationSummary;
import dk.sdu.imada.jlumina.search.primitives.DMR;

public class DMRPermutation extends PermutationProgress {

	DMRAlgorithm dmrAlgorithm;
	int binaryArray[];

	ArrayList<DMR> dmrs;
	ArrayList<Float> permutedScores;
	TreeMap<Integer, DMRPermutationSummary> resultMap;

	public DMRPermutation(DMRAlgorithm dmrAlgorithm, ArrayList<DMR> dmrs, int[] binaryArray, int numPermutations) {
		this.maxIterations = numPermutations;
		this.dmrAlgorithm = dmrAlgorithm;
		this.binaryArray = binaryArray;
		this.progress = 0;
		this.progressAux  = 0;
		this.dmrs = dmrs;
	}

	public void computePermutation() {

		this.progress = 0;
		this.done = false;

		TreeMap<Integer, ArrayList<DMR>> permutationIslandsMapping = new TreeMap<Integer, ArrayList<DMR>>();

		// . A copy of the binary array which will be shuffled np times .
		int [] binaryP = Arrays.copyOf(binaryArray, binaryArray.length);

		// . labels 0 to 450k
		ArrayList<Integer> labels = new ArrayList<Integer>();
		for (int i = 0; i < binaryArray.length; i++) labels.add(i);

		// . search in a shuffled binary array .
		permutedScores = new ArrayList<>();

		for (int i = 0; i < this.maxIterations; i++) {
			Collections.shuffle(labels);
			int index = 0;
			// changing methylation of CpGs
			for (int l : labels) {
				binaryP[index] = binaryArray[l];
				index++;
			}

			ArrayList<DMR> permutedIslands = dmrAlgorithm.islandSearch(binaryP);
			permutationIslandsMapping.put(i, permutedIslands);

			for(DMR island : permutedIslands) {
				permutedScores.add(island.score);
			}
			setProgress(i+1, 0.1);
		}

		summarizePermutation(permutationIslandsMapping);
		setDone(true);
	}

	private void summarizePermutation(TreeMap<Integer, ArrayList<DMR>> permutationIslandsMapping) {

		resultMap = new TreeMap<Integer, DMRPermutationSummary>();

		int[] uniqueValues = dmrAlgorithm.getUniqueCpGSDist(dmrs);

		for (int key : uniqueValues) {

			int numIslands = dmrAlgorithm.countIslandsOfAtLeastSize(key, dmrs);

			DMRPermutationSummary summary = new DMRPermutationSummary();
			ArrayList<DMR> islandList = new ArrayList<DMR>();

			int countIsladsArray [] = new int[this.maxIterations];
			
			double pValue = 0.0;

			for (int i = 0; i < this.maxIterations; i++) {
				int count = 0;
				for (DMR island : permutationIslandsMapping.get(i)) {
					if (island.totalCpgs >= key) {
						count++;
						islandList.add(island);
					}
				}

				if (count >= numIslands) {
					pValue+=1.0;
				}
				countIsladsArray[i] = count;
			}
			
			summary.setTotalOfIslands(islandList.size());
			summary.setNumMoreDMRs((int) pValue);
			double averageIslands = (double)islandList.size()/(double)this.maxIterations;

			summary.setAverageOfIslands(averageIslands);

			summary.setLogRatio(Math.log10(((double) numIslands + (1.0/(double)this.maxIterations))/(averageIslands + (1.0/(double)this.maxIterations))));
			//System.out.println(key + ":: " + numIslands + ":::" + averageIslands);

			summary.setNumberOfIslands(numIslands);

			summary.setpValue((double)pValue/(double)this.maxIterations);

			summary.setCpgID(key);

			summary.setNumberOfIslandsPerPermutation(countIsladsArray);

			resultMap.put(key, summary);
		}
	}

	public TreeMap<Integer, DMRPermutationSummary> getResultMap() {
		return resultMap;
	}

	public ArrayList<Float> getPermutedScores() {
		return permutedScores;
	}

	public void writePermutationSummary(TreeMap<Integer, DMRPermutationSummary> permutationResultMapping, String fname) {

		try {

			File file = new File(fname);

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("#CpG, Num.Islands, Average.Islands, p-value, log.ratio\n");
			for (int key : permutationResultMapping.keySet()) {
				DMRPermutationSummary summary = permutationResultMapping.get(key);
				bw.write(key + "," + summary.getNumberOfIslands() + "," + summary.getAverageOfIslands() + "," + summary.getpValue() + "," + summary.getLogRatio() + "\n");
			}

			bw.close();

		} catch (IOException e) {
			System.out.println("Ignore " + fname + " creation. " + e.getMessage());
		}
	}

}
