package dk.sdu.imada.gui.controllers.util;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jfree.chart.JFreeChart;

import dk.sdu.imada.gui.controllers.DMRResultController;
import dk.sdu.imada.gui.controllers.FXDMRFullSummary;
import dk.sdu.imada.gui.controllers.MainController;
import dk.sdu.imada.gui.controllers.PermutationResultController;
import dk.sdu.imada.jlumina.search.primitives.DMRDescription;
import dk.sdu.imada.jlumina.search.primitives.DMRPermutationSummary;
import javafx.collections.ObservableList;

public class SaveAll implements  Runnable{

	boolean permutationStep;
	boolean dmrStep;
	PermutationResultController pController;
	DMRResultController dmrController;
	String input;
	double progress;
	boolean done;
	String dir;
	
	MainController  mainController;

	public SaveAll(MainController mainController, PermutationResultController controller, String dir) {
		
		this.dir = dir;
		this.mainController = mainController;
		this.pController = controller;
		permutationStep = true;
		dmrStep = false;

		done = false;
		progress = 0.0;
		
	}

	public SaveAll(MainController mainController, DMRResultController controller, String dir) {
		
		this.dir = dir;
		this.mainController = mainController;
		
		this.dmrController = controller;
		this.dmrStep = true;
		this.permutationStep = false;
		done = false;
		progress = 0.0;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}

	public double getProgress() {
		return progress;
	}

	public boolean isDone() {
		return done;
	}

	@Override
	public void run() {
		if (permutationStep) {
			progress = 0.0;
			//pController.saveAll();
			
			pController.exportChart(mainController.getOrigDistributionChart().createBufferedImage(1800, 1200), dir+"orig_hist.png");

			pController.exportChart(mainController.getEmpiricalPvaluesDistributionChart().createBufferedImage(1800, 1200), dir+"emp_histogram.png");
			pController.exportChart(mainController.getEmpiricalPvaluesScatterPlotLogChart().createBufferedImage(1800, 1200), dir+"emp_scatter_plot_log.png");
			pController.exportChart(mainController.getEmpiricalPvaluesScatterPlotChart().createBufferedImage(1800, 1200), dir+"emp_scatter_plot.png");

			if (!mainController.isRegression()) {
				pController.exportChart(mainController.getEmpiricalPvaluesVulcanoPlotChart().createBufferedImage(1800, 1200), dir+"emp_vulcano_plot.png");
				pController.exportChart(mainController.getFdrVulcanoPlotChart().createBufferedImage(1800, 1200), dir+"max_fdr_vulcano_plot.png");
				pController.exportChart(mainController.getFwerVulcanoPlotChart().createBufferedImage(1800, 1200), dir+"fwer_vulcano_plot.png");
				pController.exportChart(mainController.getStepDownVulcanoPlotChart().createBufferedImage(1800, 1200), dir+"sdc_vulcano_plot.png");
				pController.exportChart(mainController.getOrigVulcanoPlotChart().createBufferedImage(1800, 1200), dir+"orig_vulcano_plot.png");
			}
			
			progress = 0.2;

			pController.exportChart(mainController.getFdrDistributionChart().createBufferedImage(1800, 1200), dir+"max_fdr_hist.png");
			pController.exportChart(mainController.getFdrLogScatterPlotChart().createBufferedImage(1800, 1200), dir+"max_fdr_scatter_plot_log.png");
			pController.exportChart(mainController.getFdrScatterPlotChart().createBufferedImage(1800, 1200), dir+"max_fdr_scatter_plot.png");

			progress = 0.6;
			pController.exportChart(mainController.getFwerDistributionChart().createBufferedImage(1800, 1200), dir+"fwer_hist.png");
			pController.exportChart(mainController.getFwerScatterPlotChart().createBufferedImage(1800, 1200), dir+"fwer_scatter_plot.png");
			pController.exportChart(mainController.getFwerScatterPlotLogChart().createBufferedImage(1800, 1200), dir+"fwer_scatter_plot_log.png");
			progress = 0.8;
			pController.exportChart(mainController.getSteDownDistributionChart().createBufferedImage(1800, 1200), dir+"sdc_hist.png");
			pController.exportChart(mainController.getStepDownScatterPlotChart().createBufferedImage(1800, 1200), dir+"sdc_scatter_plot.png");
			pController.exportChart(mainController.getStepDownLogScatterPlotChart().createBufferedImage(1800, 1200), dir+"sdc_scatter_plot_log.png");
			progress = 1.0;
			
			done = true;
		}else {
			progress = 0.0;
			// save p-values
			try {
				for (String s : dmrController.getCpgList().getItems()){
					int selectedKey = Integer.parseInt(s);
					JFreeChart chart = mainController.getPvaluesChart().get(selectedKey);
					BufferedImage img = chart.createBufferedImage(1200, 800);
					dmrController.exportChart2(img, dir+"permutations_cpg_"+s);
				}
			}catch(NumberFormatException e1) {
			}
			progress = 0.2;

			JFreeChart chartScore = mainController.getDmrScoresDistributionChart();
			BufferedImage imgScore = chartScore.createBufferedImage(1200, 600);
			dmrController.exportChart2(imgScore, dir+"score_distribution");
			
			JFreeChart chartPValue = mainController.getDmrPValueDistributionChart();
			BufferedImage imgPValue = chartPValue.createBufferedImage(1200, 600);
			dmrController.exportChart2(imgPValue, dir+"p-value_distribution");

			try {
				File file = new File(dir+ "DMRs.csv");
				if (file != null) {

					if (!file.exists()) {
						file.createNewFile();
					}

					FileWriter fw = new FileWriter(file.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write("Chr, Begin, End, begin.CpG, end.CpG, score, p-value\n");
					for (DMRDescription d : mainController.getDmrDescriptions()) {
						bw.write(d.getChromosome() + ",");
						bw.write(d.getBeginPosition() + ",");
						bw.write(d.getEndPosition() + ",");
						bw.write(d.getBeginCPG()+ ",");
						bw.write(d.getEndCPG() + ",");
						bw.write(d.getIsland().score + ",");
						bw.write(d.getIsland().getPValue() + "\n");
					}

					bw.close();
				}

			} catch (IOException e1) {
				//Platform.runLater(()->FXPopOutMsg.showWarning("Files were not saved. Something went wrong"));
			}
			progress = 0.5;
			try {

				File file = new File(dir+ "permutation.csv");

				if (file != null) {
					if (!file.exists()) {
						file.createNewFile();
					}

					if (!file.exists()) {
						file.createNewFile();
					}

					FileWriter fw = new FileWriter(file.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write("#CpG, Num.DMRs, Average.DMRs, FDR, log.ratio\n");
					for (int key : mainController.getDMRPermutationMap().keySet()) {
						DMRPermutationSummary summary = mainController.getDMRPermutationMap().get(key);
						bw.write(key + "," + summary.getNumberOfIslands() + "," + summary.getAverageOfIslands() + "," + summary.getFDR() + "," + summary.getLogRatio() + "\n");
					} 
					bw.close();
				}
			} catch (IOException e1) {
				//Platform.runLater(()->FXPopOutMsg.showWarning("Files were not saved. Something went wrong"));
			}
			
			progress = 0.8;

			try {

				File file = new File(dir + "merged_table.csv");

				if (file != null) {
					if (!file.exists()) {
						file.createNewFile();
					}

					if (!file.exists()) {
						file.createNewFile();
					}

					FileWriter fw = new FileWriter(file.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);

					bw.write("Chr, Begin, End, begin.CpG, end.CpG, score, #CpG, Num.DMRs, Average.DMRs, p-value, log.ratio, Link\n");

					ObservableList<FXDMRFullSummary> list = dmrController.getTableViewFullSummary().getItems();

					for (FXDMRFullSummary l : list) {
						bw.write( 
								 l.getChromosome() + ","
								+ l.getBeginPosition() + ","
								+ l.getEndPosition() + ","
								+ l.getBeginCPG() + ","
								+ l.getEndCPG() + ","
								+ l.getScore() + ","
								+ l.getCpgID() + ","
								+ l.getNumberOfIslands() + ","
								+ l.getAverageOfIslands() + ","
								+ l.getPvalue() + ","
								+l.getLogRatio() + ","
								+l.getURL() + "\n");
					}

					bw.close();
				}
				progress = 1.0;
			}catch(IOException e1) {
				//Platform.runLater(()->FXPopOutMsg.showWarning("Files were not saved. Something went wrong"));
			}
			done = true;
		}
		done = true;
	}

}
