package dk.sdu.imada.console;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import dk.sdu.imada.jlumina.core.io.WriteBetaMatrix;
import dk.sdu.imada.jlumina.core.primitives.CpG;
import dk.sdu.imada.jlumina.core.primitives.Grouping;
import dk.sdu.imada.jlumina.core.util.PairIDCheck;
import dk.sdu.imada.jlumina.search.algorithms.CpGStatistics;
import dk.sdu.imada.jlumina.search.statistics.MixedModelEstimator;
import dk.sdu.imada.jlumina.search.statistics.RegressionEstimator;
import dk.sdu.imada.jlumina.search.statistics.StatisticalEstimator;
import dk.sdu.imada.jlumina.search.statistics.StudentTTest;
import dk.sdu.imada.jlumina.search.util.NonPairedShuffle;
import dk.sdu.imada.jlumina.search.util.PairedShuffle;
import dk.sdu.imada.jlumina.search.util.RandomizeLabels;


public class ConsolePermutationController {
	
	private ConsoleMainController mainController;
	private Config config;
	
	
	// permutation results 
	private float cpgDistance[];
	private float cellComposition[][];
	private ArrayList<String> colNames;
	
	TreeMap<String, int[]> patientsGroups;
	
	//position of the variable of interest (only needed for regression)
	private int resultIndex;
	//grouped sample indices
	private int[] originalIndex;
	
	private ConsolePermutationMonitor pMonitor;

	
	public ConsolePermutationController(Config config, ConsoleMainController mainController){
		this.config = config;
		this.mainController = mainController;
	}

	public void start(){
		pushExecutePermutation();
	}
	
	public void pushExecutePermutation() {

		int numberOfPermutations = config.getNPermutationsCpG();
		int numThreads = config.getThreads();
		
		if(numberOfPermutations < numThreads){
			numThreads = numberOfPermutations;
		}
		
		float beta[][] = mainController.getBeta();
		float[] pvalue = null;
		float[] methylationDiff = null;
		
		//for regression
		float[][] phenotype = null;
		
		//for t-test
		int splitPoint = mainController.getBeta()[0].length/2;
		
		//for grouping
		HashMap<String,String[]> columnMap = mainController.getInputController().getColumnMap();
		Grouping gr;
		
		//pre loading of variables
		if(config.isRegression()){
			gr = new Grouping(columnMap.get(config.getVariable()));
			phenotype = loadPhenotype();
			mainController.setPhenotype(phenotype);
			resultIndex = getCoefficientIndexResult();
			originalIndex = gr.unGroupedIndices();
		}
		if(config.isTTest()){
			methylationDiff = new float[beta.length];
			if(!config.isPaired()){
				gr = new Grouping(columnMap.get(config.getVariable()));
				originalIndex = gr.getIndices();
				splitPoint = gr.getSplitPoint();
				System.out.println(gr.log());
			}else{
				gr = new Grouping(columnMap.get(Variables.PAIR_ID));
				originalIndex = gr.pairedIndices(columnMap.get(config.getVariable()));
				splitPoint = mainController.getBeta()[0].length/2;
				System.out.println(gr.log());
			}
		}
		if (config.isMixedModel()) {
			gr = new Grouping(columnMap.get(config.getVariable()));
			phenotype = loadPhenotype();
			mainController.setPhenotype(phenotype);
			resultIndex = getCoefficientIndexResult();
			originalIndex = gr.unGroupedIndices();
		}
				
		CpGStatistics cpGSignificance = new CpGStatistics(beta, 0, beta.length);
		StatisticalEstimator se;
		StatisticalEstimator estimators[] = new StatisticalEstimator[numThreads];

		if (config.isTTest()) {

			StudentTTest test = new StudentTTest(config.isTwoSided(), splitPoint, config.isLeftSided(), config.isRightSided() , config.isPaired(), config.getAssumeEqualVariance());
			System.out.println(test.status());
			se = test.getTTestEstimator();
			pvalue = cpGSignificance.computeSignificances(se, originalIndex, methylationDiff);

			for (int i = 0; i < numThreads; i++) {
				estimators[i] = new StudentTTest(config.isTwoSided(), splitPoint, config.isLeftSided(), config.isRightSided() , config.isPaired(), config.getAssumeEqualVariance()).getTTestEstimator();
			}

		}else if (config.isRegression()){

			System.out.println("Performing linear regression for original p-value estimation...");
			se = new RegressionEstimator(phenotype, resultIndex);
			pvalue = cpGSignificance.computeSignificances(se, originalIndex, methylationDiff);

			for (int i = 0; i < numThreads; i++) {
				estimators[i] = new RegressionEstimator(phenotype.clone(), resultIndex);
			}
		} else if (config.isMixedModel()) {

			LocalTime now = LocalTime.now();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
			
			System.out.println(now.format(dtf) + ": Running mixed model for original p-value estimation...");
			se = new MixedModelEstimator(phenotype, resultIndex, 0, config);
			se.setPvalues(new float[beta.length]);
			cpGSignificance.setConfig(config);
			// Need to write beta-matrix as file (use existing Dimmer code to write beta-matrix)
			String path = mainController.getConfig().getOutputDirectory();
			CpG[] cpgs = mainController.getManifest().getCpgList();
			String input_type = mainController.getConfig().getInputType();
			String array_type = mainController.getConfig().getArrayType();
			WriteBetaMatrix betaWriter = new WriteBetaMatrix(path, columnMap, cpgs, beta, input_type, array_type);
			betaWriter.write();

			pvalue = cpGSignificance.computeSignificances(se, originalIndex, methylationDiff);

			int runCounter = 1;
			for (int i = 0; i < numThreads; i++) {
				estimators[i] = new MixedModelEstimator(phenotype.clone(), resultIndex, runCounter, config);
				estimators[i].setPvalues(new float[beta.length]);
				runCounter++;
			}
		}

		mainController.setOriginalPvalues(pvalue);
		mainController.setMethylationDifference(methylationDiff);

		// ... Permutation of the CpGs

		long seed = System.currentTimeMillis();

		RandomizeLabels rand;
		CpGStatistics[] permutations = new CpGStatistics[numThreads];
		Thread pThreads[] = new Thread[numThreads];	
		int[] permuDist = Util.distributePermutations(numThreads, numberOfPermutations);

		for (int i = 0; i < numThreads; i++) {
			
			if (config.isPaired()) {
				rand = new PairedShuffle(originalIndex,seed+i);
			}else {
				rand = new NonPairedShuffle(originalIndex,seed+i);
			}

			permutations[i] = new CpGStatistics(beta, pvalue, estimators[i], rand, permuDist[i]);
			permutations[i].setConfig(config);
			pThreads[i] = new Thread(permutations[i]);
		}
		
		System.out.println("Performing " + numberOfPermutations + " permutations on " + numThreads + " threads to determine empirical p-values...");

		this.pMonitor = new ConsolePermutationMonitor(permutations, mainController);
		
		for (int i = 0; i < numThreads; i++) {
			pThreads[i].start();
		}
		pMonitor.run();
	}
	
	private float[][] loadPhenotype() {

		HashMap<String, String[]> map = mainController.getInputController().getColumnMap();

		String coefficient = config.getVariable();


		if(config.getModel().equals("T-test") || config.getModel().equals("mixedModel")) {
			float phenotype[][] = new float[map.get(coefficient).length][1];

			int idx = 0;
			for (String s : map.get(coefficient)) {
				phenotype[idx++][0] = Float.parseFloat(s);
			}
			return phenotype;

		}else { 
			
			colNames = new ArrayList<>(config.getConfoundingVariables());
			colNames.add(coefficient);
			
			int ncols = colNames.size();
			int nrows = map.get(coefficient).length;
			float[][] phenotype = new float[nrows][ncols];


			for (int i = 0; i < nrows; i++) {
				for (int j = 0; j < ncols; j++) {
					phenotype[i][j] = Float.parseFloat(map.get(colNames.get(j))[i]);
				}
			}

			if (config.getCellComposition()) {
				
				int numCellTypes = 0;

				ArrayList<Integer> ids = new ArrayList<>();

				if (config.getCd8t()) {
					ids.add(0);
					numCellTypes++;
				}
				if (config.getCd4t()) {
					ids.add(1);
					numCellTypes++;
				}
				if (config.getNk()) {
					ids.add(2);
					numCellTypes++;
				}
				if (config.getNCell()) {
					ids.add(3);
					numCellTypes++;
				}
				if (config.getMono()) {
					ids.add(4);
					numCellTypes++;
				}
				if (config.getGran()) {
					ids.add(5);
					numCellTypes++;
				}
				
				float cellCompositionAux [][] = new float[nrows][numCellTypes];
				int row = 0;
				for (float [] d : mainController.getCellComposition()) {
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
	
	
	/**
	 * 
	 * @return the index of the coefficient in the colnames + 1 ( or 0 if the model is T-Test 
	 */
	private int getCoefficientIndexResult() {

		ArrayList<String> labels = colNames;
		String coefficient = config.getVariable();

		if (config.isRegression()) {
			for (int i = 0; i < labels.size(); i++) {
				if (labels.get(i).equals(coefficient)) 
					return i + 1;
			}
			return 0;
		}else {
			return 0;
		}
	}
	
	
	public void saveAll(){
		this.pMonitor.saveAll();
	}
}
