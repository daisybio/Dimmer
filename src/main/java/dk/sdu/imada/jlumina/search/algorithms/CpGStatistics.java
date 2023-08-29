package dk.sdu.imada.jlumina.search.algorithms;

import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import dk.sdu.imada.console.Config;
import dk.sdu.imada.jlumina.search.statistics.MixedModelEstimator;
import dk.sdu.imada.jlumina.search.statistics.StatisticalEstimator;
import dk.sdu.imada.jlumina.search.statistics.TimeSeriesEstimator;
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
	Config config = null;

	int empiricalCounter[];
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
		fwerCounter = new int[beta.length];
		stepDownMinPCounter = new int[beta.length];

		float[] sortedOriginalPvalues = this.pvalues.clone();
		Arrays.sort(sortedOriginalPvalues);

		double[] y = new double[beta[0].length];
		
		int last_progress = -1;
		
		for (int np = 0; np < numberPermutations; np++) {

			randomizer.shuffle();

			int[] indexes = randomizer.getShuffledArray();

			if(config.getModel().equals("mixedModel")){
				// since we run the mixed model R script only once, there is no need to go through the for loop over
				// the whole beta matrix in this case
				statisticalEstimator.setSignificance(y, indexes);
				pvalues = ((MixedModelEstimator) statisticalEstimator).pvalues;
			}else if (config.isRM_ANOVA() || config.isFriedmanTest()){
				// since we run the time series model R script only once, there is no need to go through the for loop
				// over the whole beta matrix in this case (similar to mixedModel)
				statisticalEstimator.setPermutationValue(np+1);
				statisticalEstimator.setSignificance(y, indexes);
				pvalues = ((TimeSeriesEstimator) statisticalEstimator).pvalues;
			}else{
				for (int i = 0; i < beta.length; i++) {
					int k = 0;
					for (int j : indexes) {
						y[k++] = beta[i][j];
					}
					statisticalEstimator.setSignificance(y, indexes);
					pvalues[i] = statisticalEstimator.getPvalue();

					int progress = i/beta.length*100;
					if (last_progress< progress) {
						System.out.println("Computing Significance for Permutation " + np + " from " + numberPermutations + " at " + progress + "%," + i + " from " + beta.length + " finished");
						last_progress = progress;
					}
				}
			}

			countEmpirical(pvalues);
			countStepDownMinP(sortedOriginalPvalues, pvalues.clone());
			Arrays.sort(pvalues);
			countFwerCounter(pvalues[0]);

			setProgress(np, 0.05);
		}

		pvalues = null; System.gc();
		setDone(true);
	}
	
	/**
	 *
	 * @param statisticalEstimator
	 * @param indexes
	 * @param diff place holder for methylation diff
	 * @return pvalues
	 */
	public float [] computeSignificances(StatisticalEstimator statisticalEstimator, int indexes[], float diff[])  {

		float[] originalPvalues;

		double[] y = new double[indexes.length];

		if(config.getModel().equals("mixedModel")){
			// since we run the mixed model R script only once, there is no need to go through the for loop over
			// the whole beta matrix in this case
			statisticalEstimator.setSignificance(y, indexes);
			originalPvalues = ((MixedModelEstimator) statisticalEstimator).pvalues;
		}else if(config.isFriedmanTest() || config.isRM_ANOVA()){
			// since we run the time series model R script only once, there is no need to go through the for loop over
			// the whole beta matrix in this case (similar to mixedModel)
			statisticalEstimator.setSignificance(y, indexes);
			originalPvalues = ((TimeSeriesEstimator) statisticalEstimator).pvalues;
		}else{
			originalPvalues =  new float[beta.length];
			for (int i = 0; i < beta.length; i++) {

				int k = 0;
				for (int j : indexes) {
					y[k++] = beta[i][j];
				}

				statisticalEstimator.setSignificance(y, indexes);
				if(diff!=null){
					diff[i] = statisticalEstimator.getDiff();
				}
				originalPvalues[i] = statisticalEstimator.getPvalue();
			}
		}

		System.out.println("Finished original p-value calculation.");
		return originalPvalues;
	}

	private void countEmpirical(float[]newPvalues) {
		for (int i = 0; i < beta.length; i++) {
			if (newPvalues[i] <= pvalues[i]) {
				empiricalCounter[i]++;
			}
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
	
	public void setConfig(Config config) {
		this.config = config;
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
