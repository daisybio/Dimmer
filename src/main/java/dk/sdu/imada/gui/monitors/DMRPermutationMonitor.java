package dk.sdu.imada.gui.monitors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import dk.sdu.imada.gui.controllers.FXPopOutMsg;
import dk.sdu.imada.gui.controllers.MainController;
import dk.sdu.imada.gui.controllers.ProgressForm;
import dk.sdu.imada.gui.controllers.util.DMRResultsUtil;
import dk.sdu.imada.jlumina.search.algorithms.DMRPermutation;
import dk.sdu.imada.jlumina.search.primitives.DMRPermutationSummary;
import dk.sdu.imada.jlumina.search.primitives.DMR;
import javafx.application.Platform;

public class DMRPermutationMonitor implements Runnable {

	DMRPermutation dmrPermutation[];
	ArrayList<DMR> dmrs;
	ProgressForm  progressForm;
	MainController mainController;
	double progress;
	int numPermutations;

	public DMRPermutationMonitor(DMRPermutation[] dmrPermutation, ArrayList<DMR> dmrs, ProgressForm progressForm, MainController mainController, int np) {
		super();
		this.dmrPermutation = dmrPermutation;
		this.dmrs = dmrs;
		this.progressForm = progressForm;
		this.mainController = mainController;
		this.progress = 0;
		this.numPermutations = np;
	}

	private void updateProgess() {

		for (DMRPermutation dp : dmrPermutation) {
			progress+=dp.getProgress();
		}

		progress/=(double)dmrPermutation.length;
		progressForm.getProgressBar().setProgress(progress);
		progressForm.getText().setText("Done with " + (int)(progress * 100) + "% of the permutations");
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

	public void run() {
		
		long startTime = System.currentTimeMillis();

		checkProgress();

		System.out.println("Done with DMR permutation");
		updateProgess();

		progressForm.getText().setText("Mapping permutation results by CpG number");
		ArrayList<Float> permutedScores = joinPermutedScores();
		TreeMap<Integer, DMRPermutationSummary> dmrPermutationMap = joinMapResults();		

		for (Integer key : dmrPermutationMap.keySet()) {
			dmrPermutationMap.get(key).log();
		}

		progressForm.getText().setText("Generating plots");

		mainController.setDMRPermutationMap(dmrPermutationMap);
		mainController.setPermutedScores(permutedScores);


		if (dmrs.size() > 0) {
			Platform.runLater(()->mainController.loadScreen("resultDMR"));
			DMRResultsUtil util = new DMRResultsUtil(mainController, permutedScores, dmrPermutationMap);
			util.setPlots();
		}else {
			Platform.runLater(()->FXPopOutMsg.showWarning("No DMRs were found! We are done."));
		}
		Platform.runLater(() -> progressForm.getDialogStage().close());
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Performing DMR permutation in " + (((double)totalTime/1000.0)) + " seconds");
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
			summary.setAverageOfIslands(averageIslands);
			summary.setNumberOfIslandsPerPermutation(numIslandsPerPermutation);
			summary.setLogRatio(Math.log10(((double) numIslands + (1.0/(double)this.numPermutations))/(averageIslands + (1.0/(double)this.numPermutations))));

			map.put(key, summary);
		}

		return map;
	}

	
}
