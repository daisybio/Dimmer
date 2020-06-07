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

	public DMRPermutationMonitor(DMRPermutation[] dmrPermutation, ArrayList<DMR> dmrs, ProgressForm progressForm, MainController mainController) {
		super();
		this.dmrPermutation = dmrPermutation;
		this.dmrs = dmrs;
		this.progressForm = progressForm;
		this.mainController = mainController;
		this.progress = 0;
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
			DMRPermutationSummary s0 = dmrPermutation[0].getResultMap().get(key);

			for (int i = 1; i < dmrPermutation.length; i++) {
				DMRPermutationSummary sAux = dmrPermutation[i].getResultMap().get(key);
				s0 = mergePermutationSummaryBasicData(s0, sAux);
				//mergePermutationSummaryBasicData(s0, s2)
			}
			s0.setpValue(s0.getpValue()/(double)dmrPermutation.length);
			s0.setLogRatio(s0.getLogRatio()/(double)dmrPermutation.length);
			s0.setAverageOfIslands(s0.getAverageOfIslands()/(double)dmrPermutation.length);
			map.put(key, s0);
		}

		return map;
	}

	private DMRPermutationSummary mergePermutationSummaryBasicData(DMRPermutationSummary s1, DMRPermutationSummary s2 ) {

		DMRPermutationSummary s = new DMRPermutationSummary();
		s.setCpgID(s1.getCpgID());
		s.setNumberOfIslands(s1.getNumberOfIslands());

		s.setAverageOfIslands(s1.getAverageOfIslands() + s2.getAverageOfIslands());
		s.setpValue((s1.getpValue() + s2.getpValue()));
		s.setLogRatio((s1.getLogRatio() + s2.getLogRatio()));

		int v [] = new int[s1.getNumberOfIslandsPerPermutation().length + s2.getNumberOfIslandsPerPermutation().length];

		int index = 0;

		for (int i : s1.getNumberOfIslandsPerPermutation()) {
			v[index++] = i;
		}

		for (int i : s2.getNumberOfIslandsPerPermutation()) {
			v[index++] = i;
		}

		s.setNumberOfIslandsPerPermutation(v);

		return s;
	}
}
