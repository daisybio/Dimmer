package dk.sdu.imada.console;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import dk.sdu.imada.jlumina.search.algorithms.DMRPermutation;
import dk.sdu.imada.jlumina.search.primitives.DMR;
import dk.sdu.imada.jlumina.search.primitives.DMRPermutationSummary;


public class ConsoleDMRPermutationMonitor {
	DMRPermutation dmrPermutation[];
	ArrayList<DMR> dmrs;
	ConsoleMainController mainController;
	double progress;
	int numPermutations;

	public ConsoleDMRPermutationMonitor(DMRPermutation[] dmrPermutation, ArrayList<DMR> dmrs, ConsoleMainController mainController, int np) {
		this.dmrPermutation = dmrPermutation;
		this.dmrs = dmrs;
		this.mainController = mainController;
		this.progress = 0;
		this.numPermutations = np;
	}
	 
	public void run() {
		
		long startTime = System.currentTimeMillis();

		checkProgress();
		updateProgess();

		System.out.println("\nMapping permutation results by CpG number...");
		ArrayList<Float> permutedScores = joinPermutedScores();
		TreeMap<Integer, DMRPermutationSummary> dmrPermutationMap = joinMapResults();		
		
		
		System.out.println("\nResult summary:");
		System.out.println("#CpG, Num. DMRs, Avg. DMRs, FDR, log-ratio");
		for (Integer key : dmrPermutationMap.keySet()) {
			System.out.println(dmrPermutationMap.get(key).logLine());
		}
		
		mainController.setDMRPermutationMap(dmrPermutationMap);
		mainController.setPermutedScores(permutedScores);
		
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Performing DMR permutation in " + (((double)totalTime/1000.0)) + " seconds");
		
		if (dmrs.size() > 0) {
			DMRSearchSaver saver = new DMRSearchSaver(mainController.getConfig(),mainController);
			saver.saveAll();
		}else {
			System.out.println("No DMRs were found!");
		}

	}
	
	private void updateProgess() {

		for (DMRPermutation dp : dmrPermutation) {
			progress+=dp.getProgress();
		}

		progress/=(double)dmrPermutation.length;
		System.out.print("\rDone with " + (int)(progress * 100) + "% of the permutations");
		progress = 0;
	}

	public void checkProgress() {
		for (DMRPermutation dp : dmrPermutation) {
			synchronized (dp) {
				while(!dp.isDone()) {
					updateProgess();
					try {
						dp.wait();
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private ArrayList<Float> joinPermutedScores() {

		ArrayList<Float> scores = new ArrayList<>();

		for(DMRPermutation p : dmrPermutation) {

			for (float d : p.getPermutedScores()) {
				scores.add(d);
			}
		}
		return scores;
	}


	
	private TreeMap<Integer, DMRPermutationSummary> joinMapResults() {

		HashSet<Integer> keys = new HashSet<>();
		for(DMRPermutation p : dmrPermutation) {
			for (Integer k : p.getResultMap().keySet())
				keys.add(k);
		}

		TreeMap<Integer, DMRPermutationSummary> map = new TreeMap<>();

		for (Integer key : keys) {
			
			int numMoreDMRs = 0;
			int numTotalDMRs = 0;
			int[] numIslandsPerPermutation = new int[numPermutations];
			int index = 0;
			int numIslands = dmrPermutation[0].getResultMap().get(key).getNumberOfIslands();
			
			for (int i = 0; i < dmrPermutation.length; i++) {
				DMRPermutationSummary sAux = dmrPermutation[i].getResultMap().get(key);
				numMoreDMRs+= sAux.getNumMoreDMRs();
				numTotalDMRs += sAux.getTotalOfIslands();
				for(int num : sAux.getNumberOfIslandsPerPermutation()){
					numIslandsPerPermutation[index++] = num;
				}
			}
			
			DMRPermutationSummary summary = new DMRPermutationSummary();
			summary.setCpgID(key);
			summary.setNumberOfIslands(numIslands);
			summary.setpValue(numMoreDMRs/(double)numPermutations);
			double averageIslands = numTotalDMRs/(double)numPermutations;
			summary.setFDR(averageIslands / (double) numIslands);
			summary.setAverageOfIslands(averageIslands);
			summary.setNumberOfIslandsPerPermutation(numIslandsPerPermutation);
			summary.setLogRatio(Math.log10(((double) numIslands + (1.0/(double)this.numPermutations))/(averageIslands + (1.0/(double)this.numPermutations))));

			map.put(key, summary);
		}

		return map;
	}


}
