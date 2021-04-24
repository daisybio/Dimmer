package dk.sdu.imada.jlumina.core.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.joptimizer.functions.LinearMultivariateRealFunction;
import com.joptimizer.functions.PDQuadraticMultivariateRealFunction;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.OptimizationRequest;

import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.util.MatrixUtil;

public class CellCompositionCorrection {

	//MSet mSet;
	//USet uSet;

	ReadManifest manifest;
	
	int progress;

	//char[] gender;
	HashMap<String, int[]> cellMapping = new HashMap<>();
	int cellTypeIndexes[] = {12, 13, 14, 36, 37, 38, // cd8t
			9, 10, 11, 33, 34, 35,	// cd4t
			21, 22, 23, 45, 46, 47, // nk
			15, 16, 17, 39, 40, 41, // ncell
			18, 19, 20, 42, 43, 44, // mono
			6, 7,  8, 30, 31, 32	// grans
	};

	boolean done;
	float cellComposition [][];

	public CellCompositionCorrection() {
		setCellTypeMapping();
	}

	public void setDone(boolean done) {
		this.done = done;
	}
	
	public boolean isDone()  {
		return done;
	}
	/*public void setMethylationData(MSet mSet, USet uSet) {
		this.mSet = mSet;
		this.uSet = uSet;
	}

	public void setMSet(MSet mSet) {
		this.mSet = mSet;
	}

	public void setUSet(USet uSet) {
		this.uSet = uSet;
	}*/

	/*public void setGender(char[] gender) {
		this.gender = gender;
	}*/

	public void setManifest(ReadManifest manifest) {
		this.manifest = manifest;
	}

	/*public CellCompositionCorrection(MSet mSet, USet uSet, ReadManifest manifest, char[]gender) {
		this.mSet = mSet;
		this.uSet = uSet;
		this.manifest = manifest;
		this.gender = gender;

		setCellTypeMapping();
	}*/

	/**
	 * 
	 * 
	 * @param beta merged beta values with the user data
	 * @param numSamples number of samples from the user data
	 */
	public synchronized void estimateCellComposition(HashMap<String, float[]> beta, int numSamples) {
		done = false;
		progress = 0;
		// need this matrix for correction
		//float[][] cellTypeStatistics = getSummaryStatisticsFromCellTypes(beta);
		System.out.println("Estimating cell types");
		// get cpgs p-values, from all cell types (t-test 1 sample X samples), with p-value <= 1e-8f 
		ArrayList<String> trainingProbes = getTrainingProbes(beta, 1e-8f, 50);
		System.out.println("Number of training probes: " + trainingProbes.size());
		
		float referenceSamplesBeta[][] = MatrixUtil.getSubSetFromMap(beta, trainingProbes.toArray(new String[trainingProbes.size()]), cellTypeIndexes);
		//float[] colMeans = getColMean(referenceSamplesBeta);
		float coef[][] = getCoefficients(referenceSamplesBeta);
		referenceSamplesBeta = null; System.gc();

		int userIndexes[] = new int[numSamples];
		int index = 0;
		for (int i = 60; i < 60 + numSamples; i++) {
			userIndexes[index++] = i;
		}

		float[][] userBeta = MatrixUtil.getSubSetFromMap(beta, trainingProbes.toArray(new String[trainingProbes.size()]), userIndexes);
		beta=null; System.gc();
		this.cellComposition = getCompositionI(userBeta, coef);
		userBeta = null; System.gc();
		done = true;
		progress++;
		notify();
		
	}

	/*public synchronized void estimateCellComposition(Normalization normalization) {

		System.out.println("Starting cell composition correction");
		MSet refMset = new MSet();
		USet refUset = new USet();

		refMset.loadData("/Users/diogo/Desktop/flow.sorted.blood.data/M.csv");
		refUset.loadData("/Users/diogo/Desktop/flow.sorted.blood.data/U.csv");

		System.out.println("Loading reference dataset");

		MSet combinedMset = new MSet();
		combinedMset.setData(MatrixUtil.combineDataSets(refMset, mSet, manifest));
		USet combinedUset = new USet();
		combinedUset.setData(MatrixUtil.combineDataSets(refUset, uSet, manifest));
		refUset=null; refMset=null; System.gc();
		System.out.println("Combining user and reference datasets");

		if(gender==null) {
			System.out.println("Running gender detection");
			CheckGender checkGender = new CheckGender(combinedUset, combinedMset, manifest, -2);
			gender = checkGender.getGender();
		}


		System.out.println("Normalizing combined data set, it can take a  while");
		normalization.performNormalization(combinedMset, manifest, gender);
		normalization.checkProgress();

		normalization.performNormalization(combinedUset, manifest, gender);
		normalization.checkProgress();

		HashMap<String, float[]> beta = MatrixUtil.getBeta(combinedUset.getData(),  combinedMset.getData(), manifest, 0.0f);

		combinedMset=null; combinedUset=null; System.gc();

		// need this matrix for correction
		//float[][] cellTypeStatistics = getSummaryStatisticsFromCellTypes(beta);
		System.out.println("Estimating cell types");
		ArrayList<String> trainingProbes = getTrainingProbes(beta, 1e-8f, 50);
		float referenceSamplesBeta[][] = MatrixUtil.getSubSetFromMap(beta, trainingProbes.toArray(new String[trainingProbes.size()]), cellTypeIndexes);
		//float[] colMeans = getColMean(referenceSamplesBeta);
		float coef[][] = getCoefficients(referenceSamplesBeta);
		referenceSamplesBeta = null; System.gc();

		int userIndexes[] = new int[mSet.getNumSamples()];
		int index = 0;
		for (int i = 60; i < 60 + mSet.getNumSamples(); i++) {
			userIndexes[index++] = i;
		}

		float[][] userBeta = MatrixUtil.getSubSetFromMap(beta, trainingProbes.toArray(new String[trainingProbes.size()]), userIndexes);
		beta=null; System.gc();
		this.cellComposition = getCompositionI(userBeta, coef);

		done = true;
		notifyAll();
	}*/

	public synchronized void checkProgress() {
		while(!done) {
			try {
				wait();
			}catch(InterruptedException e) {
			}
		}
	}

	public float[][] getCellCompositoin() {
		return cellComposition;
	}

	private float[][] getCompositionI(float beta[][], float[][] coef) {

		float [][] solutions = new float[beta[0].length][];

		RealMatrix m = new Array2DRowRealMatrix(MatrixUtil.toDouble(coef));
		RealMatrix tm = m.transpose();
		RealMatrix product = tm.multiply(m);
		//Dmat
		float[][] dmat = MatrixUtil.toFloat(product.getData());

		//inequalities 
		ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[6];
		inequalities[0] = new LinearMultivariateRealFunction(new double[]{-1,0,0,0,0,0}, 0);
		inequalities[1] = new LinearMultivariateRealFunction(new double[]{0,-1,0,0,0,0}, 0);
		inequalities[2] = new LinearMultivariateRealFunction(new double[]{0,0,-1,0,0,0}, 0);
		inequalities[3] = new LinearMultivariateRealFunction(new double[]{0,0,0,-1,0,0}, 0);
		inequalities[4] = new LinearMultivariateRealFunction(new double[]{0,0,0,0,-1,0}, 0);
		inequalities[5] = new LinearMultivariateRealFunction(new double[]{0,0,0,0,0,-1}, 0);

		for (int i = 0; i < beta[0].length; i++) {

			RealMatrix dm1 = new Array2DRowRealMatrix(MatrixUtil.toDouble(coef)).transpose(); 
			RealMatrix dm2 = new Array2DRowRealMatrix(MatrixUtil.toDouble(MatrixUtil.getColumnArray(beta, i)));
			float dvec[] = MatrixUtil.getColumnArray(MatrixUtil.toFloat(dm1.multiply(dm2).getData()), 0);
			for (int j = 0; j < dvec.length; j++) {
				dvec[j] *= -1.0;
			}

			PDQuadraticMultivariateRealFunction function = new PDQuadraticMultivariateRealFunction(MatrixUtil.toDouble(dmat), MatrixUtil.toDouble(dvec), 0);
			OptimizationRequest or = new OptimizationRequest();
			or.setF0(function);
			or.setFi(inequalities);
			try {
				JOptimizer optimizer = new JOptimizer();
				optimizer.setOptimizationRequest(or);
				@SuppressWarnings("unused")
				int s = optimizer.optimize();
				solutions[i] = MatrixUtil.toFloat(optimizer.getOptimizationResponse().getSolution());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		beta=null;

		return solutions;
	}

	private float[][] getPhenoMatrix() {
		float pheno[][] = new float[36][6];
		int aux = 0;
		for (int j = 0; j < 6; j++){
			int aux2 = aux + 6;
			for (int i = aux; i < aux2; i++) {
				pheno[i][j] = 1;
			}
			aux = aux2;
		}
		return pheno;
	}

	// the cell types are in the order 
	//CD8T, CD4T, NK, Bcell, Mono, Gran
	// y ~ CD8T + CD4T + NK + Bcell + Mono + Gran - 1
	private float[][] getCoefficients(float[][] data) {

		double pheno [][] = MatrixUtil.toDouble(getPhenoMatrix());

		double coefficients[][] = new double[600][];

		for (int i = 0; i < 600; i++) {
			OLSMultipleLinearRegression linearRegression =  new OLSMultipleLinearRegression();
			double[] y = MatrixUtil.toDouble(data[i]);
			linearRegression.setNoIntercept(true);
			linearRegression.newSampleData(y, pheno);
			coefficients[i] = linearRegression.estimateRegressionParameters();
		}

		return MatrixUtil.toFloat(coefficients);
		
	}

	/**
	 * This function computes a one way anova for each cell type in the following order
	 * CD8T, CD4T, NK, Bcell, Mono, Gran; computes methylation mean of each cell type per
	 * CpG, the min, max, and max-min methylation of each cell type per CpG.
	 * 
	 * @param beta values of all samples
	 * @return matrix with fstatistics, mean beta values per cell type
	 */
	@SuppressWarnings("unused")
	private float[][] getSummaryStatisticsFromCellTypes(HashMap<String, float[]> beta ) {

		HashMap<String, float[]> columnFstatistic = fTestRow(beta);
		HashMap<String, float[]> rowMeansByCellType = getRowMeansByCellType(beta);
		HashMap<String, float[]> minMaxRange = getMinMaxRangeMethylationByCellType(beta);

		float matrix[][] = new float[beta.size()][];
		int i = 0;
		for (String cpg : manifest.getCpGsIDs()) {
			float v1 [] = ArrayUtils.addAll(columnFstatistic.get(cpg), rowMeansByCellType.get(cpg));
			float v2 [] = ArrayUtils.addAll(v1, minMaxRange.get(cpg));
			matrix[i++] = v2;
		}
		return matrix;
	}

	@SuppressWarnings("unused")
	private float[] getColMean(HashMap<String, float[]> beta, ArrayList<String> probes) {

		int ncols = beta.get(probes.get(0)).length;
		float [] column = new float[beta.size()];
		float [] colMeans = new float[ncols];

		for (int j = 0; j < ncols; j++) {
			int index = 0;
			for (String cpg : probes) {
				float row[] = beta.get(cpg);
				column[index++] = row[j];
			}
			colMeans[j] = (float) StatUtils.mean(MatrixUtil.toDouble(column));
		}
		return colMeans;
	}


	@SuppressWarnings("unused")
	private float[] getColMean(float [][] beta) {

		int ncols = beta[0].length;
		float [] colMeans = new float[ncols];

		for (int j = 0; j < ncols; j++) {
			float [] column = new float[beta.length];
			for (int i = 0 ; i < beta.length; i++) {
				column[i] = beta[i][j];
			}
			colMeans[j] = (float)StatUtils.mean(MatrixUtil.toDouble(column));
		}
		return colMeans;
	}


	/**
	 Compute the t-test of one cell type (group1) and the remaining types (group2) for each CpG. 
	 A p-value, t-statistics and mean difference for each CpG are stored by cell type in the following
	 order CD8T, CD4T, NK, Bcell, Mono, Gran; The p-value is used as cutoff for selecting the the siginficant
	 cpgs. 2*N unique CpG IDs are selected where half of them have the lowest mean difference and the other half
	 have the highest mean difference.

	 * @param beta values of all samples
	 * @param cutoff p-value cutoff 
	 * @param n number of CpGs for training
	 * @return the select CpGs IDs
	 */
	private ArrayList<String> getTrainingProbes(HashMap<String, float[]> beta, float cutoff, int n) {

		HashMap<String, HashMap<String, float[]>> statMap = new HashMap<>();
		statMap.put("cd8t", ttestCellTypes("cd8t", beta));
		statMap.put("cd4t", ttestCellTypes("cd4t", beta));
		statMap.put("nk", ttestCellTypes("nk", beta));
		statMap.put("ncell", ttestCellTypes("ncell", beta));
		statMap.put("mono", ttestCellTypes("mono", beta));
		statMap.put("gran", ttestCellTypes("gran", beta));

		TreeSet<String> ids = new  TreeSet<>();

		for (String cellType : statMap.keySet()) {

			HashMap<String, float[]> map = statMap.get(cellType);

			HashMap<String, Float> meanDifferenceList = new HashMap<>();

			for (String key : map.keySet()) {
				float[] values = map.get(key);
				if (values[1] <= cutoff) {
					meanDifferenceList.put(key, values[2]);
				}
			}

			HashMap<String, Float> sortedMap = (HashMap<String, Float>) sortByValue(meanDifferenceList);
			ArrayList<String> cpgs = new ArrayList<>();
			for (String s : sortedMap.keySet()) {
				cpgs.add(s);
			}

			for (int i = cpgs.size() - 1; i > cpgs.size()-n - 1; i--) {
				ids.add(cpgs.get(i));
			}

			for (int i = 0; i < n; i++) {
				ids.add(cpgs.get(i));
			}
		}
		return new ArrayList<String>(ids);
	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
		List<Map.Entry<K, V>> list =
				new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>()
		{
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				return (o1.getValue()).compareTo( o2.getValue() );
			}
		} );

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}

	private HashMap<String, float[]> ttestCellTypes(String cellType, HashMap<String, float[]> beta) {

		HashMap<String, float[]> hashMap = new HashMap<>();

		TTest ttest = new TTest();

		for (String cpg : manifest.getCpGsIDs()) {

			double []group1 = MatrixUtil.toDouble(getRowFromCellType(cellType, cpg, beta));
			double [] group2 = new double[30];

			int index = 0;
			for (String key : cellMapping.keySet()) {
				if(!key.equals(cellType)) {
					float row[] =  getRowFromCellType(key, cpg, beta);
					for (float v : row) {
						group2[index++] = v;
					}
				}
			}
			double tvalue = ttest.homoscedasticT(group2, group1);
			double pvalue = ttest.homoscedasticTTest(group2, group1);
			double diff = StatUtils.mean(group2) - StatUtils.mean(group1);

			float data[] = {(float)tvalue, (float)pvalue, (float)diff};

			hashMap.put(cpg, data);
		}
		return hashMap;
	}

	private HashMap<String, float[]> getMinMaxRangeMethylationByCellType(HashMap<String, float[]> beta) {
		HashMap<String, float[]> hashMap = new HashMap<>();

		for (String cpg : manifest.getCpGsIDs()) {

			double[] values = MatrixUtil.toDouble(getRowFromAllCellTypes(cpg, beta));
			float min = (float) StatUtils.min(values);
			float max = (float) StatUtils.max(values);
			float diff = max - min;
			float [] minMaxDiff = {min, max, diff};
			hashMap.put(cpg, minMaxDiff);
		}
		return hashMap;
	}

	private float[] getRowFromAllCellTypes(String cpg, HashMap<String, float[]> beta) {

		float data[] = new float[36];

		int i = 0;
		for (float v : getRowFromCellType("cd8t", cpg, beta)) {
			data[i++] = v;
		}
		for (float v : getRowFromCellType("cd4t", cpg, beta)) {
			data[i++] = v;
		}
		for (float v : getRowFromCellType("nk", cpg, beta)) {
			data[i++] = v;
		}
		for (float v : getRowFromCellType("ncell", cpg, beta)) {
			data[i++] = v;
		}
		for (float v : getRowFromCellType("mono", cpg, beta)) {
			data[i++] = v;
		}
		for (float v : getRowFromCellType("gran", cpg, beta)) {
			data[i++] = v;
		}

		return data;
	}

	private HashMap<String, float[]> getRowMeansByCellType(HashMap<String, float[]> beta) {

		HashMap<String, float[]> hashMap = new HashMap<>();

		for (String cpg : manifest.getCpGsIDs()) {

			float cd8t = (float) StatUtils.mean(MatrixUtil.toDouble(getRowFromCellType("cd8t", cpg, beta)));
			float cd4t = (float) StatUtils.mean(MatrixUtil.toDouble(getRowFromCellType("cd4t", cpg, beta)));
			float nk = (float) StatUtils.mean(MatrixUtil.toDouble(getRowFromCellType("nk", cpg, beta)));
			float ncell = (float) StatUtils.mean(MatrixUtil.toDouble(getRowFromCellType("ncell", cpg, beta)));
			float mono = (float) StatUtils.mean(MatrixUtil.toDouble(getRowFromCellType("mono", cpg, beta)));
			float gran = (float) StatUtils.mean(MatrixUtil.toDouble(getRowFromCellType("gran", cpg, beta)));			

			float [] cellTypeMeans = {cd8t, cd4t, nk, ncell, mono, gran};

			hashMap.put(cpg, cellTypeMeans);
		}

		return hashMap;
	}

	private float[] getRowFromCellType(String type, String cpg, HashMap<String, float[]> beta) {

		float[] rowValues = beta.get(cpg);
		int columns [] = cellMapping.get(type);
		float values[] = new float[columns.length];
		int index = 0;

		for (int i : columns) {
			values[index++] = rowValues[i];
		}

		return values;
	}

	private HashMap<String, float[]> fTestRow(HashMap<String, float[]> beta) {

		HashMap<String, float[]> hashMap = new HashMap<>();

		for (String cpg : manifest.getCpGsIDs()) {

			double cd8t[] = MatrixUtil.toDouble(getRowFromCellType("cd8t", cpg, beta));
			double cd4t[] = MatrixUtil.toDouble(getRowFromCellType("cd4t", cpg, beta));
			double nk[] = MatrixUtil.toDouble(getRowFromCellType("nk", cpg, beta));
			double ncell[] = MatrixUtil.toDouble(getRowFromCellType("ncell", cpg, beta));
			double mono[] = MatrixUtil.toDouble(getRowFromCellType("mono", cpg, beta));
			double gran[] = MatrixUtil.toDouble(getRowFromCellType("gran", cpg, beta));

			ArrayList<double[]> set = new ArrayList<>();
			set.add(cd8t);
			set.add(cd4t);
			set.add(nk);
			set.add(ncell);
			set.add(mono);
			set.add(gran);

			OneWayAnova anova = new OneWayAnova();
			float[] statistic = new float[2];

			statistic[0] = (float) anova.anovaFValue(set);
			statistic[1] = (float) anova.anovaPValue(set);

			hashMap.put(cpg, statistic);
		}

		return hashMap;
	}

	private void setCellTypeMapping() {

		int cd8t[] = {12, 13, 14, 36, 37, 38};
		int cd4t[] = {9, 10, 11, 33, 34, 35};
		int nk[] = {21, 22, 23, 45, 46, 47};
		int ncell[] = {15, 16, 17, 39, 40, 41};
		int mono[] = {18, 19, 20, 42, 43, 44};
		int gran[] = {6, 7,  8, 30, 31, 32};
		cellMapping = new HashMap<>();
		cellMapping.put("cd8t",cd8t);
		cellMapping.put("cd4t",cd4t);
		cellMapping.put("nk",nk);
		cellMapping.put("ncell",ncell);
		cellMapping.put("mono",mono);
		cellMapping.put("gran",gran);
	}
	
	public int getProgress() {
		return progress;
	}
	
	public void write(String path){
		System.out.println("Writing cell composition estimations to "+ path);
		try{
			BufferedWriter br = new BufferedWriter(new FileWriter(new File(path)));
			for( float[] row : cellComposition){
				for( float value: row){
					br.write(value+"\t");
				}
				br.write("\n");
			}
			br.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}

	}
}

