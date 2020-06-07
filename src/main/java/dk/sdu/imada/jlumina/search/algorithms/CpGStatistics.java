package dk.sdu.imada.jlumina.search.algorithms;

import java.util.Arrays;

import dk.sdu.imada.jlumina.search.statistics.StatisticalEstimator;
import dk.sdu.imada.jlumina.search.util.RandomizeLabels;

public class CpGStatistics extends PermutationProgress implements Runnable  {

	float[][] beta;
	float[][] labels;
	float fdrCutoff;

	int bottom, top;

	float [] pvalues; 
	float diff[];
	int numberPermutations;
	RandomizeLabels randomizer;

	StatisticalEstimator statisticalEstimator;

	int empiricalCounter[];
	int fdrCounter[];
	int fwerCounter[];
	int stepDownMinPCounter[];

	public CpGStatistics(float[][] beta, float[] pvalues, StatisticalEstimator statisticalEstimator, RandomizeLabels randomizer, int numberPermutations) {
		this.beta = beta;
		this.pvalues = pvalues;
		this.statisticalEstimator = statisticalEstimator;
		this.randomizer = randomizer;
		this.numberPermutations = numberPermutations;
	}

	public CpGStatistics(float[][] beta, int bottom, int top) {
		this.beta = beta;
		this.bottom = bottom;
		this.top = top;
	}

	public void computePermutation() {

		setMaxIterations(numberPermutations);
		setDone(false);

		float pvalues[] = new float[beta.length];

		empiricalCounter = new int[beta.length];
		fdrCounter = new int[beta.length];
		fwerCounter = new int[beta.length];
		stepDownMinPCounter = new int[beta.length];

		float[] sortedOriginalPvalues = this.pvalues.clone();
		Arrays.sort(sortedOriginalPvalues);

		double[] y = new double[beta[0].length];
		
		for (int np = 0; np < numberPermutations; np++) {

			randomizer.shuffle();

			int [] indexes = randomizer.getShuffledArray();

			for (int i = 0; i < beta.length; i++) {
				int k = 0;
				for (int j : indexes) {
					y[k++] = beta[i][j];
				}
				statisticalEstimator.setSignificance(y);
				pvalues[i] = statisticalEstimator.getPvalue();
			}

			countEmpirical(pvalues);
			countStepDownMinP(sortedOriginalPvalues, pvalues.clone());
			Arrays.sort(pvalues);
			countFdr(pvalues);
			countFwerCounter(pvalues[0]);

			setProgress(np, 0.05);
		}

		pvalues = null; System.gc();
		setDone(true);
	}

	public float [] computeSignificances(StatisticalEstimator statisticalEstimator, int indexes[], float diff[])  {

		float originalPvalues[] = new float[beta.length];

		double[] y = new double[indexes.length];

		for (int i = 0; i < beta.length; i++) {

			int k = 0;
			for (int j : indexes) {
				y[k++] = beta[i][j];
			}

			statisticalEstimator.setSignificance(y);
			diff[i] = statisticalEstimator.getDiff();
			originalPvalues[i] = statisticalEstimator.getPvalue();
		}
		
		return originalPvalues;
	}

	private void countEmpirical(float[]newPvalues) {
		for (int i = 0; i < beta.length; i++) {
			if (newPvalues[i] <= pvalues[i]) {
				empiricalCounter[i]++;
			}
		}
	}

	private void countFdr(float sortedPvalues[]) {
		for (int i = 0; i < pvalues.length; i++) {
			fdrCounter[i]+=countPvalues(sortedPvalues, pvalues[i]);
		}
	}

	//
	private void countFwerCounter(float f) {
		for (int i = 0; i < this.pvalues.length; i++) {
			if (f <= this.pvalues[i]) {
				this.fwerCounter[i]++;
			}
		}
	}

	// Couting how many p-values are greater than the whole p-values...
	private int countPvalues(float [] sortedPvalues, float referencePvalue) {

		int index = java.util.Arrays.binarySearch(sortedPvalues, referencePvalue);

		if (index < 0) {
			int i = Math.abs(index);
			return i;
		}else {
			while(index < sortedPvalues.length && referencePvalue == sortedPvalues[index]) {
				index++;
			}
			return index;
		}
	}

	private void countStepDownMinP(float[] sortedOriginalPvalues, float[] permutedPvalues) {
		for (int i = permutedPvalues.length - 2; i >= 0; i--) {
			if (permutedPvalues[i] > permutedPvalues[i+1]) {
				permutedPvalues[i] = permutedPvalues[i+1];
			}
		}

		for (int i = 0; i < sortedOriginalPvalues.length;i++) {
			if (permutedPvalues[i] <= sortedOriginalPvalues[i]) {
				stepDownMinPCounter[i]++;
			}
		}
	}

	public int getBottom() {
		return bottom;
	}

	public int[] getEmpiricalCounter() {
		return empiricalCounter;
	}

	public int[] getFdrCounter() {
		return fdrCounter;
	}

	public int[] getFwerCounter() {
		return fwerCounter;
	}

	public int[] getStepDownMinPCounter() {
		return stepDownMinPCounter;
	}

	public int getTop() {
		return top;
	}

	@Override
	public void run() {
		computePermutation();
	}

	public void setBeta(float[][] beta) {
		this.beta = beta;
	}

	public void setBottom(int bottom) {
		this.bottom = bottom;
	}

	public void setDiff(float[] diff) {
		this.diff = diff;
	}

	public void setNumberPermutations(int numberPermutations) {
		this.numberPermutations = numberPermutations;
	}

	public void setPvalues(float[] pvalues) {
		this.pvalues = pvalues;
	}

	public void setRandomizer(RandomizeLabels randomizer) {
		this.randomizer = randomizer;
	}

	public void setStatisticalEstimator(StatisticalEstimator statisticalEstimator) {
		this.statisticalEstimator = statisticalEstimator;
	}

	public void setTop(int top) {
		this.top = top;
	}
}
