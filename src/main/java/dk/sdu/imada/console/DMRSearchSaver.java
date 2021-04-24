package dk.sdu.imada.console;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.jfree.chart.JFreeChart;

import dk.sdu.imada.gui.plots.BarPlotDMRPvalues;
import dk.sdu.imada.gui.plots.HistogramPvalueDistribution;
import dk.sdu.imada.gui.plots.HistogramScoreDistribution;
import dk.sdu.imada.jlumina.search.primitives.DMR;
import dk.sdu.imada.jlumina.search.primitives.DMRDescription;
import dk.sdu.imada.jlumina.search.primitives.DMRPermutationSummary;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;



public class DMRSearchSaver {
	
	private Config config;
	private ConsoleMainController mainController;
	private ArrayList<Float> permutedScores;
	private TreeMap<Integer, DMRPermutationSummary> permutationResultMapping;
	
	private TreeMap<Integer, JFreeChart> pvaluesChart;
	JFreeChart dmrScoreDistribution;
	JFreeChart dmrPValueDistribution;
	
	public DMRSearchSaver(Config config, ConsoleMainController mainController){
		this.config = config;
		this.mainController = mainController;
		this.permutedScores = mainController.getPermutedScores();
		this.permutationResultMapping = mainController.getDMRPermutationMap();
	}
	
	public void saveAll() {
		Date d = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
		String datetime = ft.format(d)+"_DMRSearch/";
		String dir = config.getOutputDirectory() + datetime;
		
		System.out.println();
		
		if(config.getSaveSearchPlots()){
			System.out.println("Saving DMR search plots in " + datetime);
			savePlots(dir);
		}
		else{
			System.out.println("No DMR search plots will be saved");
		}
		
		if(config.getSaveSearchTables()){
			System.out.println("Saving DMR search tables in " + datetime);
			saveTables(dir);
		}
		else{
			System.out.println("No DMR search tables will be saved");
		}

		


	}
	
	public void savePlots(String dir){
		
		setPvaluesChart();
		setDMRScoreDistribution();
		setDMRPValueDistribution();
		
		//save p-value charts
		for (int selectedKey :mainController.getDMRPermutationMap().keySet()){
			JFreeChart chart = this.pvaluesChart.get(selectedKey);
			BufferedImage img = chart.createBufferedImage(1200, 800);
			exportChart2(img, dir+"permutations_cpg_"+selectedKey);
		}
		
		//save permuted score dist
		JFreeChart chartScore = this.dmrScoreDistribution;
		BufferedImage imgScore = chartScore.createBufferedImage(1200, 600);
		exportChart2(imgScore, dir+"score_distribution");
		
		JFreeChart chartPValue= this.dmrPValueDistribution;
		BufferedImage imgPValue = chartPValue.createBufferedImage(1200, 600);
		exportChart2(imgPValue, dir+"p-value_distribution");
	}
	 
	public void saveTables(String dir){
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
					bw.write(d.getChromosome() + ", ");
					bw.write(d.getBeginPosition() + ", ");
					bw.write(d.getEndPosition() + ", ");
					bw.write(d.getBeginCPG()+ ", ");
					bw.write(d.getEndCPG() + ", ");
					bw.write(d.getIsland().score +", " );
					bw.write(d.getIsland().getPValue()+"\n");
				}

				bw.close();
			}

		} catch (IOException e1) {
			System.out.println("DMRs.csv wasn't saved, something went wrong!");
		}
		try {

			File file = new File(dir + "permutation.csv");

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
			System.out.println("permutation.csv wasn't saved, something went wrong!");
		}
		

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

				ArrayList<DMRFullSummary> list = fullSummary();

				for (DMRFullSummary l : list) {
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
							+ l.getURL() + "\n");
				}

				bw.close();
			}
		}catch(IOException e1) {
			//Platform.runLater(()->FXPopOutMsg.showWarning("Files were not saved. Something went wrong"));
		}
	}

	
	private void setPvaluesChart() {
		TreeSet<Integer> keys = new TreeSet<>(permutationResultMapping.keySet());
		pvaluesChart = new TreeMap<>();
		int cpgIds[] = new int[keys.size()];

		int k = 0;
		for (int key : keys) {
			cpgIds[k++] = key;
			int [] valuesInt = permutationResultMapping.get(key).getNumberOfIslandsPerPermutation();
			double values[] = new double[valuesInt.length];
			for (int i = 0 ; i < valuesInt.length; i++) {
				values[i] = (double) valuesInt[i];
			}	

			double reference = (double) permutationResultMapping.get(key).getNumberOfIslands();
			BarPlotDMRPvalues histogramComparison = new BarPlotDMRPvalues("#CpGs: " + key, 
					values, reference, "Permutation", "#DMRs", values.length, Color.BLUE);
			JFreeChart chart = histogramComparison.getChart();

			pvaluesChart.put(key, chart);
		}

	}

	private void setDMRScoreDistribution() {

		double [] scoresPermuted = new double[permutedScores.size()];
		int index = 0;
		for (double d : permutedScores) {
			scoresPermuted[index++] = d;
		}

		double [] scoresNonPermuted = new double[mainController.getDMRs().size()];
		index = 0;
		for (DMR dmr : mainController.getDMRs()) {
			scoresNonPermuted[index++] = dmr.score;
		}

		HistogramScoreDistribution histogramComparison = new HistogramScoreDistribution("Score distribution", 
				scoresPermuted, scoresNonPermuted, "Score", "Frequency", 100, Color.BLUE);
		JFreeChart chart = histogramComparison.getChart();
		this.dmrScoreDistribution = chart;

	}
	
	private void setDMRPValueDistribution() {
		
		int index = 0;
		double[] pValues = new double[mainController.getDMRs().size()];
		for (DMR dmr : mainController.getDMRs()) {
			pValues[index++] = dmr.getPValue();
		}

		HistogramPvalueDistribution histogram = new HistogramPvalueDistribution("DMR p-value distribution", pValues, "p-values", "Count", 11, java.awt.Color.BLUE, 0, 1);
		JFreeChart chart = histogram.getChart();
		Image mini = SwingFXUtils.toFXImage(chart.createBufferedImage(600, 400), null);
		
		this.dmrPValueDistribution = chart;

	}
	
	public void exportChart2(BufferedImage bufferedImage, String name) {
		try {
			File outPutFile = new File(name + ".png");
			outPutFile.getParentFile().mkdirs();
			if (outPutFile!=null) {
				ImageIO.write(bufferedImage, "png", outPutFile);
			}
		}catch(IOException e) {
			System.out.println("Can't save file " + name);
		}
	}
	
	public ArrayList<DMRFullSummary> fullSummary(){
		ArrayList<DMRFullSummary> arrayList = new ArrayList<DMRFullSummary>();

		TreeMap<Integer, DMRPermutationSummary> map = mainController.getDMRPermutationMap();
		ArrayList<DMRDescription> dmrs = mainController.getDmrDescriptions();

		for (DMRDescription d : dmrs ) {
			int id = d.getIsland().totalCpgs;

			for (int i : map.keySet()) {
				if (id  == map.get(i).getCpgID()) {
					arrayList.add(new DMRFullSummary(map.get(i), d));
				}
			}
		}
		return arrayList;
	}

}
