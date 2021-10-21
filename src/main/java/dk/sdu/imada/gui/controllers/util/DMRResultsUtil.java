package dk.sdu.imada.gui.controllers.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jfree.chart.JFreeChart;

import dk.sdu.imada.gui.controllers.MainController;
import dk.sdu.imada.gui.plots.BarPlotDMRPvalues;
import dk.sdu.imada.gui.plots.HistogramPvalueDistribution;
import dk.sdu.imada.gui.plots.HistogramScoreDistribution;
import dk.sdu.imada.jlumina.search.primitives.DMRDescription;
import dk.sdu.imada.jlumina.search.primitives.DMRPermutationSummary;
import dk.sdu.imada.jlumina.core.util.MatrixUtil;
import dk.sdu.imada.jlumina.search.primitives.DMR;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class DMRResultsUtil {

	MainController mainController;

	ArrayList<Float> permutedScores = new ArrayList<>();
	TreeMap<Integer, DMRPermutationSummary> permutationResultMapping;

	public DMRResultsUtil(MainController mainController, ArrayList<Float> permutedScores,
			TreeMap<Integer, DMRPermutationSummary> permutationResultMapping) {

		super();

		this.mainController = mainController;
		this.permutedScores = permutedScores;
		this.permutationResultMapping = permutationResultMapping;
	}

	
	public void setPlots() {
		setPvaluesChart();
		setDMRScoreDistribution();
		setDMRPValueDistribution();
		setLinks();

		// tables are not as numerical for sorting!
		Platform.runLater(()->mainController.getDmrResultController().setPermutationSummaryPreview());
		Platform.runLater(()->mainController.getDmrResultController().setFullPermutationSummaryPreview());
		Platform.runLater(()->mainController.getDmrResultController().setDMRSummaryPreview());
	}

	private void setPvaluesChart() {
		mainController.getDmrResultController().getCpgList().getItems().removeAll(mainController.getDmrResultController().getCpgList().getItems());
		TreeSet<Integer> keys = new TreeSet<>(permutationResultMapping.keySet());
		TreeMap<Integer, JFreeChart> pvaluesChart = new TreeMap<>();
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

		Image mini = SwingFXUtils.toFXImage(pvaluesChart.get(keys.iterator().next()).createBufferedImage(600, 400), null);
		mainController.getDmrResultController().getView1().setImage(mini);
		mainController.getDmrResultController().setCpgIDS(cpgIds);

		mainController.setPvaluesChart(pvaluesChart);
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
		Image mini = SwingFXUtils.toFXImage(chart.createBufferedImage(600, 400), null);

		mainController.setDmrScoresDistributionChart(chart);

		mainController.getDmrResultController().getView2().setImage(mini);
	}
	
	private void setDMRPValueDistribution() {
		
		int index = 0;
		double[] pValues = new double[mainController.getDMRs().size()];
		for (DMR dmr : mainController.getDMRs()) {
			pValues[index++] = dmr.getPValue();
		}

		HistogramPvalueDistribution histogram = new HistogramPvalueDistribution("DMR p-value distribution", pValues, "p-values", "Count", 100, java.awt.Color.BLUE, 0, 1);
		JFreeChart chart = histogram.getChart();
		Image mini = SwingFXUtils.toFXImage(chart.createBufferedImage(600, 400), null);
		
		mainController.setDmrPValueDistributionChart(chart);
		mainController.getDmrResultController().getView3().setImage(mini);
	}
	
	

	private void setLinks() {

		/*Platform.runLater(() -> {
			if (mainController.getDmrResultController().getLinks().getItems().isEmpty()) {
				ObservableList<DMRDescription> observableList = FXCollections.observableArrayList(mainController.getDmrDescriptions());
				mainController.getDmrResultController().getLinks().setItems(observableList); 
			}else {
				mainController.getDmrResultController().getLinks().getItems().removeAll();
				ObservableList<DMRDescription> observableList = FXCollections.observableArrayList(mainController.getDmrDescriptions());
				mainController.getDmrResultController().getLinks().setItems(observableList);
			}
		});*/
	}
}
