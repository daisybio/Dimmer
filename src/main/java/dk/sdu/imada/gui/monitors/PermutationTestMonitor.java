package dk.sdu.imada.gui.monitors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.jfree.chart.JFreeChart;

import dk.sdu.imada.gui.controllers.MainController;
import dk.sdu.imada.gui.controllers.ProgressForm;
import dk.sdu.imada.gui.plots.HistogramPvalueDistribution;
import dk.sdu.imada.gui.plots.ScatterPlot;
import dk.sdu.imada.gui.plots.VolcanoPlot;
import dk.sdu.imada.gui.plots.XYData;
import dk.sdu.imada.gui.plots.XYLogData;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.statistics.BenjaminiHochberg;
import dk.sdu.imada.jlumina.core.util.MatrixUtil;
import dk.sdu.imada.jlumina.search.algorithms.CpGStatistics;
import javafx.application.Platform;

public class PermutationTestMonitor implements Runnable{

	MainController mainController;
	CpGStatistics[] permutations;
	ProgressForm progressForm;

	double progress;

	public PermutationTestMonitor(CpGStatistics[] permutations, MainController mainController, ProgressForm form) {
		this.permutations = permutations;
		this.progressForm = form;
		this.mainController = mainController;
		progress = 0.0;
	}

	private void log(float[] array, float treshold) {

		int count = 0;
		for (float f : array) {
			if (f <= treshold) {
				count++;
			}
		}
		System.out.println(count +" CpGs below or equal to " + treshold);
	}

	private void updateProgress() {

		int total = permutations.length;

		for (CpGStatistics s : permutations) {
			progress+= s.getProgress()/total;
		}

		progressForm.getProgressBar().setProgress(progress);
		progressForm.getText().setText("Done with " + (int)(progress * 100) + "% of the permutations");

		progress = 0;
	}

	private void doWait() {
		for (CpGStatistics s : permutations) {
			while (!s.isDone()) {
				synchronized(s) {
					try {
						s.wait(300000);
					}catch(InterruptedException e){
						System.out.println("Thread timeout...");
					}
				}
				updateProgress();
			}
		}
	}

	private float [] getEmpiricalPvalues() {

		float empiricalPvalue[] = new float[mainController.getBeta().length];

		for (CpGStatistics s : permutations) {
			for (int i = 0; i < empiricalPvalue.length; i++) {
				empiricalPvalue[i]+= s.getEmpiricalCounter()[i];
			}
		}
		for (int i = 0; i < empiricalPvalue.length; i++) {
			empiricalPvalue[i] = (empiricalPvalue[i] + 1 )/(mainController.getNumPermutations() + 1);
		}
		return empiricalPvalue;
	}


	private float[] getFwerPvalues() {

		float maxStatistics[] = new float[mainController.getBeta().length];

		for (CpGStatistics s : permutations) {
			for (int i = 0; i < maxStatistics.length; i++) {
				maxStatistics[i]+= s.getFwerCounter()[i];
			}
		}

		for (int i = 0; i < maxStatistics.length; i++) {
			maxStatistics[i] = (maxStatistics[i] + 1)/(mainController.getNumPermutations() + 1);
		}

		return maxStatistics;
	}


	private float[] getStepDownMinPvalues() {

		float stepDownPvalues[] = new float[mainController.getBeta().length];

		// summing with all threads
		for (CpGStatistics s : permutations) {
			for (int i = 0; i < stepDownPvalues.length; i++) {
				stepDownPvalues[i]+= s.getStepDownMinPCounter()[i]; 
			}
		}

		// calculating the p-value
		for (int i = 0 ; i < stepDownPvalues.length; i++) {
			stepDownPvalues[i] = stepDownPvalues[i]/(mainController.getNumPermutations());
		}

		//step up
		for (int i = 1; i < stepDownPvalues.length; i++) {
			if (stepDownPvalues[i-1] > stepDownPvalues[i] ) {
				stepDownPvalues[i] = stepDownPvalues[i-1];
			}
		}

		Integer [] indexList = getOriginalIndexes();

		float newPvalues[] = new float[indexList.length];
		for (int i = 0; i < indexList.length; i++) {
			newPvalues[indexList[i]] = stepDownPvalues[i];
		}

		return newPvalues;
	}

	private class ArrayIndexComparator implements Comparator<Integer> {
		private final float[] array;

		public ArrayIndexComparator(float[] array) {
			this.array = array;
		}

		public Integer[] createIndexArray() {
			Integer[] indexes = new Integer[array.length];
			for (int i = 0; i < array.length; i++) {
				indexes[i] = i;
			}
			return indexes;
		}

		@Override
		public int compare(Integer index1, Integer index2) {
			return Float.compare(array[index1], array[index2]);
		}
	}

	private Integer[] getOriginalIndexes() {

		ArrayIndexComparator cmp = new ArrayIndexComparator(mainController.getOriginalPvalues());

		Integer[] indexList = cmp.createIndexArray();

		Arrays.sort(indexList, cmp);

		return indexList;
	}

	@Override
	public void run() {

		long startTime = System.currentTimeMillis();

		doWait();

		progressForm.getProgressBar().setProgress(1.0);
		progressForm.getText().setText("Creating plots...");

		float empiricalPvalues [] = getEmpiricalPvalues();
		float fdrPvalues[] = BenjaminiHochberg.adjustPValues(mainController.getOriginalPvalues());
		float fwerPvalues[] = getFwerPvalues();
		float stepDownMinPvalues[] = getStepDownMinPvalues();


		System.out.println("Original p-values: ");
		log(mainController.getOriginalPvalues(), 0.05f);

		System.out.println("Step down p-values: ");
		log(stepDownMinPvalues, 0.05f);

		System.out.println("Empirical p-values:");
		log(empiricalPvalues, 0.05f);

		System.out.println("FDR p-values: ");
		log(fdrPvalues, 0.05f);

		int numBins = mainController.getNumPermutations()/11;
		if (numBins < 11) numBins = 11;

		mainController.setEmpiricalPvalues(empiricalPvalues);
		mainController.setFwerPvalues(fwerPvalues);
		mainController.setFdrPvalues(fdrPvalues);
		mainController.setStepDownMinPvalues(stepDownMinPvalues);

		mainController.setEmpiricalPvaluesDistributionChart(histogram(empiricalPvalues, numBins , "Emp. p-values distribution", "Emp. p-values", "Count"));
		mainController.setEmpiricalPvaluesScatterPlotChart(scatterPlot(empiricalPvalues, mainController.getOriginalPvalues(), false, "Emp. p-values Vs. p-values", "p-values", "Emp. p-values"));
		mainController.setEmpiricalPvaluesScatterPlotLogChart(scatterPlot(empiricalPvalues, mainController.getOriginalPvalues(), true, "Emp. p-values Vs. p-values", "p-values (-log10)", "Emp. p-values (-log10)" ));

		mainController.setStepDownDistributionChart(histogram(stepDownMinPvalues, numBins , "Step-down minP values distribution", "Step-down minP values", "Count"));
		mainController.setStepDownScatterPlotChart(scatterPlot(stepDownMinPvalues, mainController.getOriginalPvalues(), false, "Step-down minP. p-values Vs. p-values", "p-values", "Step-down minP values"));
		mainController.setStepDownLogScatterPlotChart(scatterPlot(stepDownMinPvalues, mainController.getOriginalPvalues(), true, "Step-down minP. p-values Vs. p-values", "p-values (-log10)", "Step-down minP values (-log10)" ));

		mainController.setFwerDistributionChart(histogram(fwerPvalues, numBins, "FWER p-values distribution", "Fwer. p-value", "Count"));
		mainController.setFwerScattePlotChart(scatterPlot(fwerPvalues, mainController.getOriginalPvalues(), false, "FWER p-values Vs. p-values", "p-values", "FWER p-values"));
		mainController.setFwerScatterPlotLogChart(scatterPlot(fwerPvalues, mainController.getOriginalPvalues(), true, "FWER p-values Vs. p-values", "p-values (-log10)", "FWER p-values (-log10)"));

		mainController.setFdrDistributionChart(histogram(fdrPvalues, numBins, "FDR p-values distribution", "FDR p-values", "Count"));
		mainController.setFdrScatterPlotChart(scatterPlot(fdrPvalues, mainController.getOriginalPvalues(), false, "FDR p-values Vs. p-values", "p-values", "FDR p-values"));
		mainController.setFdrLogScatterPlotChart(scatterPlot(fdrPvalues, mainController.getOriginalPvalues(), true, "FDR p-values Vs. p-values", "p-values (-log10)", "FDR p-values (-log10)" ));

		mainController.setOrigDistributionChart(histogram(mainController.getOriginalPvalues(), numBins, "Orig. p-values distribution", "p-values", "Count"));

		if (!mainController.isRegression()) {

			mainController.setEmpiricalPvaluesVulcanoPlotChart(vulcanoPlot(empiricalPvalues, mainController.getMethylationDifference(),
					"Methylation difference Vs. Emp. p-values", "Methylation difference", "Emp. p-values (-log10)"));

			mainController.setStepDownVulcanoPlotChart(vulcanoPlot(stepDownMinPvalues, mainController.getMethylationDifference(),
					"Methylation difference Vs. Step-down minP values", "Methylation difference", "Step-down minP values (-log10)"));

			mainController.setFdrVulcanoPlotChart(vulcanoPlot(fdrPvalues, mainController.getMethylationDifference(),
					"Methylation difference Vs. FDR p-values", "Methylation difference", "FDR p-values (-log10)"));

			mainController.setFwerVulcanoPlotChart(vulcanoPlot(fwerPvalues, mainController.getMethylationDifference(),
					"Methylation difference Vs. FWER p-values", "Methylation difference", "FWER p-values (-log10)"));

			mainController.setOrigVulcanoPlotChart(vulcanoPlot(mainController.getOriginalPvalues(), mainController.getMethylationDifference(),
					"Methylation difference Vs. Orig. p-values", "Methylation difference", "Orig. p-values (-log10)"));
		}

		savePermutationParameters(mainController.getOutputDirectory() + "dimmer_project.csv");

		mainController.setPermutationResultScreen();

		Platform.runLater(() -> progressForm.getDialogStage().close());

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				mainController.loadScreen("permutationResult");
			}
		});

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;

		System.out.println("Finsishing permutation test in " + (((double)totalTime/1000.0)/60.0) + " minutes");
	}

	private JFreeChart histogram(float p0[],int bins,  String title, String xlab, String ylab) {
		HistogramPvalueDistribution his = new HistogramPvalueDistribution(title, MatrixUtil.toDouble(p0), xlab, ylab, bins, java.awt.Color.BLUE, 0, 1);
		JFreeChart chart = his.getChart();
		return chart;
	}

	private JFreeChart scatterPlot(float correctedPvalues[], float originalPvalues[], boolean log, String title, String xlab, String ylab) {

		JFreeChart chart = null;

		if(log) {
			XYData data = new XYData(getLog10(MatrixUtil.toDouble(originalPvalues)), getLog10(MatrixUtil.toDouble(correctedPvalues)), -Math.log10(1.0/mainController.getNumPermutations()));
			ScatterPlot scatterPlot = new ScatterPlot(title, data, xlab, ylab, java.awt.Color.RED, log);
			chart = scatterPlot.getChart();
		}else {
			XYData data = new XYData(MatrixUtil.toDouble(originalPvalues), MatrixUtil.toDouble((correctedPvalues)));
			ScatterPlot scatterPlot = new ScatterPlot(title, data, xlab, ylab, java.awt.Color.RED, log);
			chart = scatterPlot.getChart();
		}
		return chart;
	}

	private JFreeChart vulcanoPlot(float correctedPvalues[], float meanDiff[], String title, String xlab, String ylab) {
		XYLogData xyLogData = new XYLogData(MatrixUtil.toDouble(meanDiff), 
				MatrixUtil.toDouble(correctedPvalues), (float) mainController.getPvalueCutoff(), 
				-Math.log10(1.0/(mainController.getNumPermutations()*10.0)));
		VolcanoPlot vp = new VolcanoPlot(title, xyLogData, xlab, ylab);
		JFreeChart chart = vp.getChart();
		return chart;
	}

	private double[] getLog10(double values[]) {

		double logValues [] = new double[values.length];

		int i = 0;
		for (double v : values) {
			logValues[i] = -Math.log10(v);
			i++;
		}
		return logValues;
	}

	public void savePermutationParameters(String fname) {

		float empirical[] = mainController.getEmpiricalPvalues();
		float original[] = mainController.getOriginalPvalues();
		float fdr[] = mainController.getFdrPvalues();
		float fwer[] = mainController.getFwerPvalues();
		float sdmp[] = mainController.getStepDownMinPvalues();

		float methylationDiff[] = mainController.getMethylationDifference();
		ReadManifest manifest = mainController.getManifest();

		try {
			File file = new File(fname);
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());

			fw.write("CPG, CHR, POS, ORG, EMP, FDR, FWER, SDMP, DIFF\n");

			for (int i = 0; i < empirical.length; i++) {	
				fw.write(manifest.getCpgList()[i].getCpgName() + ",");
				fw.write(manifest.getCpgList()[i].getChromosome() + ",");
				fw.write(manifest.getCpgList()[i].getMapInfo() + ",");
				fw.write(original[i] + ",");
				fw.write(empirical[i] + ",");
				fw.write(fdr[i] + ",");
				fw.write(fwer[i] + ",");
				fw.write(sdmp[i] + ",");

				if (methylationDiff!=null) {
					fw.write(methylationDiff[i] + "\n");
				} else { 
					fw.write("0.0\n");
				}
			}
			BufferedWriter bw = new BufferedWriter(fw);
			bw.close();
		}catch(IOException e) {
			System.out.println("Ignoring output file creation");
		}
	}
}
