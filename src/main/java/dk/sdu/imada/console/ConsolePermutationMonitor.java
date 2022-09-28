package dk.sdu.imada.console;


import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.jfree.chart.JFreeChart;

import dk.sdu.imada.gui.plots.HistogramDistanceDistribution;
import dk.sdu.imada.gui.plots.HistogramPvalueDistribution;
import dk.sdu.imada.gui.plots.ScatterPlot;
import dk.sdu.imada.gui.plots.VolcanoPlot;
import dk.sdu.imada.gui.plots.XYData;
import dk.sdu.imada.gui.plots.XYLogData;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.io.WriteBetaMatrix;
import dk.sdu.imada.jlumina.core.primitives.CpG;
import dk.sdu.imada.jlumina.core.statistics.BenjaminiHochberg;
import dk.sdu.imada.jlumina.core.util.MatrixUtil;
import dk.sdu.imada.jlumina.search.algorithms.CpGStatistics;



public class ConsolePermutationMonitor implements Runnable{
	
	ConsoleMainController mainController;
	CpGStatistics[] permutations;


	double progress;
	
	public ConsolePermutationMonitor(CpGStatistics[] permutations, ConsoleMainController mainController) {
		this.permutations = permutations;
		this.mainController = mainController;
		progress = 0.0;
	}
	
	@Override
	public void run() {

		long startTime = System.currentTimeMillis();

		doWait();
		
		System.out.print("\rDone with 100% of the permutations\n");
		
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;

		System.out.println("Finishing permutation test in " + (((double)totalTime/1000.0)/60.0) + " minutes\n");
	

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
		

		mainController.setEmpiricalPvalues(empiricalPvalues);
		mainController.setFwerPvalues(fwerPvalues);
		mainController.setFdrPvalues(fdrPvalues);
		mainController.setStepDownMinPvalues(stepDownMinPvalues);
		
		if(mainController.getConfig().getSavePermuPlots()){
			System.out.println("\nCreating plots...");
			saveAll();
		}
		else{
			System.out.println("\nNo plots will be saved...");
		}
		
		if(mainController.getConfig().getSaveBeta()){
			String path = mainController.getConfig().getOutputDirectory();
			HashMap<String,String[]> columnMap = mainController.getInputController().getColumnMap();
			CpG[] cpgs = mainController.getManifest().getCpgList();
			float[][] beta = mainController.getBeta();
			String input_type = mainController.getConfig().getInputType();
			String array_type = mainController.getConfig().getArrayType();
			WriteBetaMatrix betaWriter = new WriteBetaMatrix(path, columnMap, cpgs, beta, input_type, array_type);
			betaWriter.write();
		}
		mainController.setBeta(null);
		
		savePermutationParameters(mainController.getConfig().getOutputDirectory() + "dimmer_project.csv");
		
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
	
	private void updateProgress() {

		int total = permutations.length;

		for (CpGStatistics s : permutations) {
			progress+= s.getProgress()/total;
		}

		System.out.print("\rDone with " + (int)(progress * 100) + "% of the permutations");

		progress = 0;
	}
	
	private float [] getEmpiricalPvalues() {

		float empiricalPvalue[] = new float[mainController.getBeta().length];

		for (CpGStatistics s : permutations) {
			for (int i = 0; i < empiricalPvalue.length; i++) {
				empiricalPvalue[i]+= s.getEmpiricalCounter()[i];
			}
		}
		for (int i = 0; i < empiricalPvalue.length; i++) {
			empiricalPvalue[i] = (empiricalPvalue[i] + 1 )/(mainController.getConfig().getNPermutationsCpG() + 1);
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
			maxStatistics[i] = (maxStatistics[i] + 1 )/(mainController.getConfig().getNPermutationsCpG() + 1);
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
			stepDownPvalues[i] = stepDownPvalues[i]/(mainController.getConfig().getNPermutationsCpG());
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
	
	private Integer[] getOriginalIndexes() {

		ArrayIndexComparator cmp = new ArrayIndexComparator(mainController.getOriginalPvalues());

		Integer[] indexList = cmp.createIndexArray();

		Arrays.sort(indexList, cmp);

		return indexList;
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
	
	private void log(float[] array, float treshold) {

		int count = 0;
		for (float f : array) {
			if (f <= treshold) {
				count++;
			}
		}
		System.out.println(count +" CpGs below or equal to " + treshold);
	}
	
	private JFreeChart histogram(float p0[],int bins,  String title, String xlab, String ylab) {
		HistogramPvalueDistribution his = new HistogramPvalueDistribution(title, MatrixUtil.toDouble(p0), xlab, ylab, bins, java.awt.Color.BLUE, 0, 1);
		JFreeChart chart = his.getChart();
		return chart;
	}

	private JFreeChart scatterPlot(float correctedPvalues[], float originalPvalues[], boolean log, String title, String xlab, String ylab) {

		JFreeChart chart = null;

		if(log) {
			XYData data = new XYData(getLog10(MatrixUtil.toDouble(originalPvalues)), getLog10(MatrixUtil.toDouble(correctedPvalues)), -Math.log10(1.0/mainController.getConfig().getNPermutationsCpG()));
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
				MatrixUtil.toDouble(correctedPvalues), (float) mainController.getConfig().getPvalueCutoff(), 
				-Math.log10(1.0/(mainController.getConfig().getNPermutationsCpG()*10.0)));
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

			fw.write("CPG, CHR, POS, ORG, EMP, FDR, FWER, SDMP");
			if(methylationDiff!= null){
				fw.write(", DIFF\n");
			}
			else{
				fw.write("\n");
			}

			for (int i = 0; i < empirical.length; i++) {	
				fw.write(manifest.getCpgList()[i].getCpgName() + ",");
				fw.write(manifest.getCpgList()[i].getChromosome() + ",");
				fw.write(manifest.getCpgList()[i].getMapInfo() + ",");
				fw.write(original[i] + ",");
				fw.write(empirical[i] + ",");
				fw.write(fdr[i] + ",");
				fw.write(fwer[i] + ",");
				fw.write(sdmp[i] + "");

				if (methylationDiff!=null) {
					fw.write("," + methylationDiff[i] + "\n");
				} else { 
					fw.write("\n");
				}
			}
			BufferedWriter bw = new BufferedWriter(fw);
			bw.close();
		}catch(IOException e) {
			System.out.println("Ignoring output file creation");
		}
	}
	
	public void exportChart(BufferedImage bufferedImage, String name) {
		try {
			File outPutFile = new File(name);
			outPutFile.getParentFile().mkdirs();
			if (outPutFile!=null) {
				ImageIO.write(bufferedImage, "png", outPutFile);
			}
		}catch(IOException e) {
			System.out.println("Can't save file " + name);
		}
	}
	
	public void saveAll(){
		
		Date d = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
		String datetime = ft.format(d)+"_permutationTest/";
		String dir =  this.mainController.getConfig().getOutputDirectory() + datetime;
		
		float[] empiricalPvalues = mainController.getEmpiricalPvalues();
		float[] fwerPvalues = mainController.getFwerPvalues();
		float[] fdrPvalues = mainController.getFdrPvalues();
		float[] stepDownMinPvalues = mainController.getStepDownMinPvalues();
		
		int numBins = mainController.getConfig().getNPermutationsCpG()/11;
		if (numBins < 11) numBins = 11;
		
		System.out.println("Saving permutation result plots in "+dir);
		
		//empirical
		exportChart(histogram(empiricalPvalues, numBins , "Emp. p-values distribution", "Emp. p-values", "Count").createBufferedImage(1800, 1200), dir+"emp_histogram.png");
		exportChart(scatterPlot(empiricalPvalues, mainController.getOriginalPvalues(), true, "Emp. p-values Vs. p-values", "p-values (-log10)", "Emp. p-values (-log10)" ).createBufferedImage(1800, 1200), dir+"emp_scatter_plot_log.png");
		exportChart(scatterPlot(empiricalPvalues, mainController.getOriginalPvalues(), false, "Emp. p-values Vs. p-values", "p-values", "Emp. p-values").createBufferedImage(1800, 1200), dir+"emp_scatter_plot.png");
		
		//step down
		exportChart(histogram(stepDownMinPvalues, numBins , "Step-down minP values distribution", "Step-down minP values", "Count").createBufferedImage(1800, 1200), dir+"sdc_hist.png");
		exportChart(scatterPlot(stepDownMinPvalues, mainController.getOriginalPvalues(), false, "Step-down minP. p-values Vs. p-values", "p-values", "Step-down minP values").createBufferedImage(1800, 1200), dir+"sdc_scatter_plot.png");
		exportChart(scatterPlot(stepDownMinPvalues, mainController.getOriginalPvalues(), true, "Step-down minP. p-values Vs. p-values", "p-values (-log10)", "Step-down minP values (-log10)" ).createBufferedImage(1800, 1200), dir+"sdc_scatter_plot_log.png");

		//fwer
		exportChart(histogram(fwerPvalues, numBins, "FWER p-values distribution", "Fwer. p-value", "Count").createBufferedImage(1800, 1200), dir+"fwer_hist.png");
		exportChart(scatterPlot(fwerPvalues, mainController.getOriginalPvalues(), false, "FWER p-values Vs. p-values", "p-values", "FWER p-values").createBufferedImage(1800, 1200), dir+"fwer_scatter_plot.png");
		exportChart(scatterPlot(fwerPvalues, mainController.getOriginalPvalues(), true, "FWER p-values Vs. p-values", "p-values (-log10)", "FWER p-values (-log10)").createBufferedImage(1800, 1200), dir+"fwer_scatter_plot_log.png");

		//fdr
		exportChart(histogram(fdrPvalues, numBins, "FDR p-values distribution", "FDR p-values", "Count").createBufferedImage(1800, 1200), dir+"max_fdr_hist.png");
		exportChart(scatterPlot(fdrPvalues, mainController.getOriginalPvalues(), true, "FDR p-values Vs. p-values", "p-values (-log10)", "FDR p-values (-log10)" ).createBufferedImage(1800, 1200), dir+"max_fdr_scatter_plot_log.png");
		exportChart(scatterPlot(fdrPvalues, mainController.getOriginalPvalues(), false, "FDR p-values Vs. p-values", "p-values", "FDR p-values").createBufferedImage(1800, 1200), dir+"max_fdr_scatter_plot.png");

		//orig
		exportChart(histogram(mainController.getOriginalPvalues(), numBins, "Orig. p-values distribution", "p-values", "Count").createBufferedImage(1800, 1200), dir+"orig_hist.png");

		
		//vulcano
		if (!mainController.getConfig().isRegression()) {
			exportChart(vulcanoPlot(empiricalPvalues, mainController.getMethylationDifference(),
					"Methylation difference Vs. Emp. p-values", "Methylation difference", "Emp. p-values (-log10)").createBufferedImage(1800, 1200), dir+"emp_vulcano_plot.png");
			exportChart(vulcanoPlot(fdrPvalues, mainController.getMethylationDifference(),
					"Methylation difference Vs. FDR p-values", "Methylation difference", "FDR p-values (-log10)").createBufferedImage(1800, 1200), dir+"max_fdr_vulcano_plot.png");
			exportChart(vulcanoPlot(fwerPvalues, mainController.getMethylationDifference(),
					"Methylation difference Vs. FWER p-values", "Methylation difference", "FWER p-values (-log10)").createBufferedImage(1800, 1200), dir+"fwer_vulcano_plot.png");
			exportChart(vulcanoPlot(stepDownMinPvalues, mainController.getMethylationDifference(),
					"Methylation difference Vs. Step-down minP values", "Methylation difference", "Step-down minP values (-log10)").createBufferedImage(1800, 1200), dir+"sdc_vulcano_plot.png");
			exportChart(vulcanoPlot(mainController.getOriginalPvalues(), mainController.getMethylationDifference(),
					"Methylation difference Vs. Orig. p-values", "Methylation difference", "Orig. p-values (-log10)").createBufferedImage(1800, 1200), dir+"orig_vulcano_plot.png");
		}
		
		if(mainController.getMethylationDifference() != null){
			float[] diff = mainController.getMethylationDifference();
			float[] min_max = Util.getMinMax(diff, -1f, 1f);
			float min = min_max[0];
			float max = min_max[1];
			
			HistogramPvalueDistribution his = new HistogramPvalueDistribution("Methylation difference distribution", MatrixUtil.toDouble(diff), "Methylation difference", "Count", 100, java.awt.Color.BLUE, min, max, min, max);
			exportChart( his.getChart().createBufferedImage(1800, 1200), dir + "mean_diff_methylation_hist.png");
		}
		
		//distance
		System.out.println("Saving CpG distance distribution plot in "+dir);
		exportChart(getDistanceDistributionChart(getCpGDistribution(10000)).createBufferedImage(1800, 1200), dir+"cpg_dist.png");
		
	}
	
	//distance chart (util of DMRParametersUtil class)
	
	private JFreeChart getDistanceDistributionChart(double distance[]) {
		HistogramDistanceDistribution his = new HistogramDistanceDistribution("CpG distance distribution", distance, 
				"distance difference (bp)", "Frequency", 100, java.awt.Color.BLUE);
		JFreeChart chart = his.getChart();
		return chart;
	}
	
	private double[] getCpGDistribution(double treshold) {

		ReadManifest m = mainController.getManifest();

		double distance[] = new double[m.getCpgList().length];
		String chr[] = new String[m.getCpgList().length];

		int i = 0;
		for (CpG cpg : m.getCpgList()) {
			distance[i] = cpg.getMapInfo();
			chr[i] = cpg.getChromosome();
			i++;
		}

		ArrayList<Double> distribution = new ArrayList<Double>();
		for (i = 1; i < distance.length; i++) {

			double d = distance[i] - distance[i-1];

			if (chr[i].equals(chr[i-1]) && d<= treshold) {
				distribution.add(d);
			}
		}
		distance=null;
		chr=null;

		double[] list = new double[distribution.size()];
		i = 0;
		for (Double d : distribution) {
			list[i] = d;
			i++;
		}

		distribution = null;

		return list;
	}


}
