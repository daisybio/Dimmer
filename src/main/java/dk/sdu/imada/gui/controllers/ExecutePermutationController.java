package dk.sdu.imada.gui.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import dk.sdu.imada.gui.monitors.PermutationTestMonitor;
import dk.sdu.imada.jlumina.search.algorithms.CpGStatistics;
import dk.sdu.imada.jlumina.search.statistics.RegressionEstimator;
import dk.sdu.imada.jlumina.search.statistics.StatisticalEstimator;
import dk.sdu.imada.jlumina.search.statistics.StudentTTest;
import dk.sdu.imada.jlumina.search.util.NonPairedShuffle;
import dk.sdu.imada.jlumina.search.util.PairedShuffle;
import dk.sdu.imada.jlumina.search.util.RandomizeLabels;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ExecutePermutationController {

	MainController mainController;
	
	@FXML Label summary;
	@FXML Button start;

	// permutation results 
	float cpgDistance[];
	float cellComposition[][];
	ArrayList<String> colNames;

	TreeMap<String, int[]> patientsGroups;
	int [] originalIndex;
	int resultIndex ;

	@FXML public void pushBack(ActionEvent actionEvent) {
		mainController.loadScreen("permutationParameters");
	}

	@FXML public void pushExecutePermutation(ActionEvent actionEvent) {

		int numberOfPermutations = mainController.getNumPermutations();
		int numThreads = mainController.getNumThreads();
		float beta[][] = mainController.getBeta();
		float[] pvalue = null;
		float[] methylationDiff = new float[beta.length];		
		float[][] phenotype = loadPhenotype();
		boolean options [] = getOptions();

		mainController.setPhenotype(phenotype);
		resultIndex = getCoefficientIndexResult();

		ProgressForm progressForm = new ProgressForm();
		Platform.runLater(progressForm);

		if  (!mainController.dataTypeController.isPaired()) {

			if (mainController.inputController.hasGroupID()) {
				patientsGroups = getGroupMapping();
			}else {
				patientsGroups = getGroupMapping(mainController.inputController.getCoefficient());
			}
			
		}else {
			if (mainController.inputController.hasPairID) {
				patientsGroups = getPairIDMapping("Pair_ID");
			}
		}

		originalIndex = getGroupIndex(patientsGroups);

		CpGStatistics cpGSignificance = new CpGStatistics(beta, 0, beta.length);

		StatisticalEstimator se;
		StatisticalEstimator estimators[] = new StatisticalEstimator[numThreads];

		if (!options[1]) {
			
			int splitPoint = mainController.getBeta()[0].length/2;
			
			if (!mainController.isPaired()) { 
				splitPoint = getSplitPoint(patientsGroups);
			}
			se = new StudentTTest(options[2], splitPoint, false, false , options[0], options[3]).getTTestEstimator();
			pvalue = cpGSignificance.computeSignificances(se, originalIndex, methylationDiff);

			for (int i = 0; i < numThreads; i++) {
				estimators[i] = new StudentTTest(options[2], splitPoint, false, false , options[0], options[3]).getTTestEstimator();
			}

		}else {
			//long startTime = System.currentTimeMillis();

			//long regS = System.currentTimeMillis();
			se = new RegressionEstimator(phenotype, resultIndex);
			pvalue = cpGSignificance.computeSignificances(se, originalIndex, methylationDiff);
			//long regE = System.currentTimeMillis();
			//System.out.println("reg estimated: " + (( ( (double)(regE-regS)/1000.0))));
			for (int i = 0; i < numThreads; i++) {
				estimators[i] = new RegressionEstimator(phenotype.clone(), resultIndex);
			}
			

			//long endTime = System.currentTimeMillis();
			//long totalTime = endTime - startTime;
			//System.out.println("Time estimated: " + (( ( (double)totalTime/1000.0) * 1000.0 )/60.0) + " minutes");
		}

		mainController.setOriginalPvalues(pvalue);
		mainController.setMethylationDifference(methylationDiff);

		// ... Permutation of the CpGs
		RandomizeLabels rand;
		ArrayList<Thread> threads = new ArrayList<>();

		if (mainController.dataTypeController.isPaired()) {
			rand = new PairedShuffle(originalIndex);
		}else {
			rand = new NonPairedShuffle(originalIndex);
			//rand = new Bootstraping(originalIndex);
		}

		CpGStatistics[] permutations = new CpGStatistics[numThreads];
		Thread pThreads[] = new Thread[numThreads];

		for (int i = 0; i < numThreads; i++) {
			permutations[i] = new CpGStatistics(beta, pvalue, estimators[i], rand, numberOfPermutations/numThreads);
			pThreads[i] = new Thread(permutations[i]);
			threads.add(pThreads[i]);
		}

		PermutationTestMonitor pMonitor = new PermutationTestMonitor(permutations, mainController, progressForm);
		Thread monitorThread = new Thread(pMonitor, "monitor");
		threads.add(monitorThread);

		progressForm.setThreads(threads);

		monitorThread.start();
		for (int i = 0; i < numThreads; i++) {
			pThreads[i].start();
		}
	}

	private int getSplitPoint(TreeMap<String, int[]> patientsGroups) {

		if(patientsGroups == null) {
			return mainController.getBeta().length/2;
		}

		for (String key : patientsGroups.keySet()) {
			return patientsGroups.get(key).length;
		}

		return 0;
	}

	private int[] getGroupIndex(TreeMap<String, int[]> patientsGroups) {

		if (patientsGroups == null) {
			int seq[] = new int[mainController.getBeta()[0].length];
			for (int i = 0; i < seq.length; i++) {
				seq[i] = i;
			}
			return seq;
		}

		int count = 0;

		for (String key : patientsGroups.keySet()) {
			count += patientsGroups.get(key).length;
		}

		int v[] = new int[count];
		int index = 0;
		for (String key : patientsGroups.keySet()) {

			for (int e : patientsGroups.get(key)) {
				v[index++] = e;
			}
		}
		return v;
	}

	private boolean [] getOptions() {

		boolean isPaired = mainController.dataTypeController.isPaired();
		boolean isRegression = mainController.modelController.isRegression();
		boolean twoSided = mainController.modelController.selectTwoSided();
		boolean left = mainController.modelController.selectTwoSided();
		boolean right = mainController.modelController.selectTwoSided();
		boolean assumeEqualVariance = mainController.modelController.selectAssumeEqualVariance();

		boolean [] ttestOptions = {isPaired, isRegression, twoSided, left, right, assumeEqualVariance};

		return ttestOptions;
	}

	public void setCanvasController(MainController canvasController) {
		this.mainController = canvasController;
	}

	public void setSummaryText(String value) {
		this.summary.setText(value);
	}

	private TreeMap<String, int[]> getGroupMapping() {

		TreeMap<String, int[]> groups = new TreeMap<>();
		TreeMap<String, ArrayList<Integer>> groupsAux = new TreeMap<>();

		TreeSet<String> ids = new TreeSet<>();
		for (String s : mainController.inputController.getColumnMap().get("Group_ID")) {
			ids.add(s);
		}

		for (String s : ids) {
			groupsAux.put(s, new ArrayList<Integer>());
		}

		int rows = 0;
		for (String s :  mainController.inputController.getColumnMap().get("Group_ID")) {
			groupsAux.get(s).add(rows++);
		}


		for (String key : groupsAux.keySet()) {

			ArrayList<Integer> v = groupsAux.get(key);
			int newArray [] = new int[v.size()];
			int index = 0;
			for (int i : v) {
				newArray[index++] = i;
			}
			groups.put(key, newArray);
		}

		return groups;
	}

	private TreeMap<String, int[]> getGroupMapping(String id) {

		TreeMap<String, int[]> groups = new TreeMap<>();
		TreeMap<String, ArrayList<Integer>> groupsAux = new TreeMap<>();

		TreeSet<String> ids = new TreeSet<>();
		for (String s : mainController.inputController.getColumnMap().get(id)) {
			ids.add(s);
		}

		for (String s : ids) {
			groupsAux.put(s, new ArrayList<Integer>());
		}

		int rows = 0;
		for (String s :  mainController.inputController.getColumnMap().get(id)) {
			groupsAux.get(s).add(rows++);
		}


		for (String key : groupsAux.keySet()) {

			ArrayList<Integer> v = groupsAux.get(key);
			int newArray [] = new int[v.size()];
			int index = 0;
			for (int i : v) {
				newArray[index++] = i;
			}
			groups.put(key, newArray);
		}

		return groups;
	}

	private TreeMap<String, int[]> getPairIDMapping(String id) {

		TreeMap<String, int[]> groups = new TreeMap<>();
		TreeMap<String, ArrayList<Integer>> groupsAux = new TreeMap<>();

		TreeSet<String> ids = new TreeSet<>();
		for (String s : mainController.inputController.getColumnMap().get(id)) {
			ids.add(s);
		}

		for (String s : ids) {
			groupsAux.put(s, new ArrayList<Integer>());
		}

		int rows = 0;
		for (String s :  mainController.inputController.getColumnMap().get(id)) {
			groupsAux.get(s).add(rows++);
		}


		for (String key : groupsAux.keySet()) {

			ArrayList<Integer> v = groupsAux.get(key);
			int newArray [] = new int[v.size()];
			int index = 0;
			for (int i : v) {
				newArray[index++] = i;
			}
			groups.put(key, newArray);
		}

		return groups;
	}

	private float[][] loadPhenotype() {

		HashMap<String, String[]> map = mainController.inputController.columnMap;

		String coefficient = mainController.inputController.getCoefficient();
		colNames = mainController.inputController.getSelectedLabels();

		if(!mainController.modelController.isRegression()) {

			float phenotype[][] = new float[map.get(coefficient).length][1];

			int idx = 0;
			for (String s : map.get(coefficient)) {
				phenotype[idx++][0] = Float.parseFloat(s);
			}
			return phenotype;

		}else {

			int numCellTypes = 0;

			ArrayList<Integer> ids = new ArrayList<>();

			if (mainController.includeCellTypeCD8T()) {
				ids.add(0);
				numCellTypes++;
			}
			if (mainController.includeCellTypeCD4T()) {
				ids.add(1);
				numCellTypes++;
			}
			if (mainController.includeCellTypeNK()) {
				ids.add(2);
				numCellTypes++;
			}
			if (mainController.includeCellTypeBCell()) {
				ids.add(3);
				numCellTypes++;
			}
			if (mainController.includeCellTypeMono()) {
				ids.add(4);
				numCellTypes++;
			}
			if (mainController.includeCellTypeGran()) {
				ids.add(5);
				numCellTypes++;
			}

			int ncols = colNames.size();
			int nrows = map.get(coefficient).length;
			float[][] phenotype = new float[nrows][ncols];


			for (int i = 0; i < nrows; i++) {
				for (int j = 0; j < ncols; j++) {
					phenotype[i][j] = Float.parseFloat(map.get(colNames.get(j))[i]);
				}
			}

			if (mainController.useCellComposition()) {
				float cellCompositionAux [][] = new float[nrows][numCellTypes];
				int row = 0;
				for (float []d : mainController.getCellComposition()) {
					int col = 0;
					for (int i : ids) {
						cellCompositionAux[row][col++] = d[i];
					}
					row++;
				}

				float [][] merged = mergeMatrix(phenotype, cellCompositionAux);

				return merged;

			}else {
				return phenotype;
			}
		}
	}


	private float[][] mergeMatrix(float [][]m1, float[][]m2) {

		float m[][] = new float[m1.length][m1[0].length + m2[0].length];

		for (int i = 0; i < m1.length ; i++) {

			int col = 0;
			for (float v : m1[i]) {
				m[i][col++] = v;
			}

			for (float v : m2[i]) {
				m[i][col++] = v;
			}
		}


		return m;
	}

	private int getCoefficientIndexResult() {

		ArrayList<String> labels = mainController.inputController.getSelectedLabels();
		String coefficient = mainController.inputController.getCoefficient();

		if (mainController.modelController.isRegression()) {
			for (int i = 0; i < labels.size(); i++) {
				if (labels.get(i).equals(coefficient)) 
					return i + 1;
			}
			return 0;
		}else {
			return 0;
		}
	}
}
