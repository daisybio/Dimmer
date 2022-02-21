package dk.sdu.imada.gui.controllers;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.jfree.chart.JFreeChart;

import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.search.primitives.DMRDescription;
import dk.sdu.imada.jlumina.search.primitives.DMRPermutationSummary;
import dk.sdu.imada.jlumina.search.primitives.DMR;
import dk.sdu.imada.jlumina.search.util.DataUtil;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class MainController implements Initializable {
	// maping for activate step buttons
	@FXML ImageView logo;
	@FXML Button stepDataType_1; 
	@FXML Button stepModel_2;
	@FXML Button stepPermutationParameters_3;
	@FXML Button input_4;
	@FXML Button summary_5;
	@FXML Button permutationResult_6;
	@FXML Button dmrParameters_7;
	@FXML Button dmrExecute_8;
	@FXML Button dmrResult_9;

	TreeMap<Integer, Button> treeMapSteps = new TreeMap<Integer, Button>();
	// controllers ...
	DataTypeController dataTypeController;
	ModelController modelController;
	PermutationParametersController permutationParametersController;
	InputController inputController;
	ExecutePermutationController executePermutationController;
	PermutationResultController permutationResultController;
	DMRParametersController dmrParametersController;
	ExecuteDMRFinderController executeDMRFinder;
	DMRResultController dmrResultController;

	boolean infinium;
	boolean epic;

	public boolean isEpic() {
		return epic;
	}

	public boolean isInfinium() {
		return infinium;
	}

	public void setEpic(boolean epic) {
		this.epic = epic;
	}

	public void setInfinium(boolean infinium) {
		this.infinium = infinium;
	}

	Stage primaryStage;
	Screens screens;

	ReadManifest manifest;

	float[][] phenotype;
	float[][]beta;
	float[][] cellComposition;
	float[]coefficients;

	float[] methylationDifference;
	float[] originalPvalues;

	// how often a given p-value is <= to the permuted p-values for one CpG
	float[] empiricalPvalues;
	// how often a given p-value is <= to all permuted p-values
	float[] fdrPvalues;
	// similar to fwer p-value
	float[] stepDownMinPvalues;
	// how the p-values are <= to the best permuted p-value
	float[] fwerPvalues;

	public void setStepDownMinPvalues(float[] stepDownPvalues) {
		this.stepDownMinPvalues = stepDownPvalues;
	}

	public float[] getStepDownMinPvalues() {
		return stepDownMinPvalues;
	}

	// reference to dmr search only.
	float [] searchPvalues;

	ArrayList<DMR> dmrs;
	ArrayList<DMRDescription> dmrDescriptions;

	TreeMap<Integer, DMRPermutationSummary> dmrPermutationMap;
	ArrayList<Float> permutedScores;

	JFreeChart dmrScoresDistributionChart;
	JFreeChart dmrPValueDistributionChart;
	JFreeChart cpgDistanceChart;
	JFreeChart cpgDiffChart;
	TreeMap<Integer, JFreeChart> pvaluesChart;

	JFreeChart empiricalPvaluesDistributionChart;
	JFreeChart empiricalPvaluesScatterPlotChart;
	JFreeChart empiricalPvaluesScatterPlotLogChart;
	JFreeChart empiricalPvaluesVulcanoPlotChart;

	JFreeChart fdrDistributionChart;
	JFreeChart fdrScatterPlotChart;
	JFreeChart fdrLogScatterPlotChart;
	JFreeChart fdrVulcanoPlotChart;

	JFreeChart fwerDistributionChart;
	JFreeChart fwerScatterPlotChart;
	JFreeChart fwerScatterPlotLogChart;
	JFreeChart fwerVulcanoPlotChart;

	JFreeChart origDistributionChart;
	JFreeChart origVulcanoPlotChart;

	JFreeChart steDownDistributionChart;
	JFreeChart stepDownScatterPlotChart;
	JFreeChart stepDownLogScatterPlotChart;
	JFreeChart stepDownVulcanoPlotChart;

	public void setStepDownDistributionChart(JFreeChart steDownDistributionChart) {
		this.steDownDistributionChart = steDownDistributionChart;
	}

	public void setStepDownLogScatterPlotChart(JFreeChart stepDownLogScatterPlotChart) {
		this.stepDownLogScatterPlotChart = stepDownLogScatterPlotChart;
	}

	public void setStepDownScatterPlotChart(JFreeChart stepDownScatterPlotChart) {
		this.stepDownScatterPlotChart = stepDownScatterPlotChart;
	}

	public void setStepDownVulcanoPlotChart(JFreeChart stepDownVulcanoPlotChart) {
		this.stepDownVulcanoPlotChart = stepDownVulcanoPlotChart;
	}

	public JFreeChart getSteDownDistributionChart() {
		return steDownDistributionChart;
	}

	public JFreeChart getStepDownLogScatterPlotChart() {
		return stepDownLogScatterPlotChart;
	}

	public JFreeChart getStepDownScatterPlotChart() {
		return stepDownScatterPlotChart;
	}

	public JFreeChart getStepDownVulcanoPlotChart() {
		return stepDownVulcanoPlotChart;
	}

	public void setOrigVulcanoPlotChart(JFreeChart origFdrVulcanoPlotChart) {
		this.origVulcanoPlotChart = origFdrVulcanoPlotChart;
	}

	public JFreeChart getOrigVulcanoPlotChart() {
		return origVulcanoPlotChart;
	}

	public JFreeChart getOrigDistributionChart() {
		return origDistributionChart;
	}

	public void setOrigDistributionChart(JFreeChart origFdrDistributionChart) {
		this.origDistributionChart = origFdrDistributionChart;
	}

	public float[] getFdrPvalues() {
		return fdrPvalues;
	}

	public void setFdrPvalues(float[] fdrPvalues) {
		this.fdrPvalues = fdrPvalues;
	}

	public void setFwerDistributionChart(JFreeChart maxStatisticsHistogram) {
		this.fwerDistributionChart = maxStatisticsHistogram;
	}

	public void setFwerScattePlotChart(JFreeChart maxStatisticsScattePlot) {
		this.fwerScatterPlotChart = maxStatisticsScattePlot;
	}

	public void setFwerScatterPlotLogChart(JFreeChart maxStatisticsScattePlotLog) {
		this.fwerScatterPlotLogChart = maxStatisticsScattePlotLog;
	}

	public JFreeChart getFwerDistributionChart() {
		return fwerDistributionChart;
	}

	public JFreeChart getFwerScatterPlotChart() {
		return fwerScatterPlotChart;
	}

	public JFreeChart getFwerScatterPlotLogChart() {
		return fwerScatterPlotLogChart;
	}

	public void setFwerVulcanoPlotChart(JFreeChart maxStatisticsVulcanoPlot) {
		this.fwerVulcanoPlotChart = maxStatisticsVulcanoPlot;
	}

	public JFreeChart getFwerVulcanoPlotChart() {
		return fwerVulcanoPlotChart;
	}

	public MainController() {

	}

	public void setSearchPvalues(float[] searchPvalues) {
		this.searchPvalues = searchPvalues;
	}

	public float[] getSearchPvalues() {
		return searchPvalues;
	}

	public JFreeChart getFdrLogScatterPlotChart() {
		return fdrLogScatterPlotChart;
	}

	public void setFdrLogScatterPlotChart(JFreeChart fdrLogScatterPlotChart) {
		this.fdrLogScatterPlotChart = fdrLogScatterPlotChart;
	}

	public void setPermutedScores(ArrayList<Float> permutedScores) {
		this.permutedScores = permutedScores;
	}

	public ArrayList<Float> getPermutedScores() {
		return permutedScores;
	}

	public void setFwerPvalues(float[] fwFDRPvalues) {
		this.fwerPvalues = fwFDRPvalues;
	}

	public float[] getFwerPvalues() {
		return fwerPvalues;
	}

	public void setDMRs(ArrayList<DMR> dmrs) {
		this.dmrs = dmrs;
	}

	public ArrayList<DMR> getDMRs() {
		return dmrs;
	}

	public void setDmrScoresDistributionChart(JFreeChart dmrScoresDistributionChart) {
		this.dmrScoresDistributionChart = dmrScoresDistributionChart;
	}

	public JFreeChart getDmrScoresDistributionChart() {
		return dmrScoresDistributionChart;
	}
	
	public JFreeChart  getDmrPValueDistributionChart(){
		return this.dmrPValueDistributionChart;
	}
	
	public void setDmrPValueDistributionChart(JFreeChart dmrPValueDistributionChart){
		this.dmrPValueDistributionChart = dmrPValueDistributionChart;
	}

	public void setPvaluesChart(TreeMap<Integer, JFreeChart> pvaluesChart) {
		this.pvaluesChart = pvaluesChart;
	}

	public TreeMap<Integer, JFreeChart> getPvaluesChart() {
		return pvaluesChart;
	}

	public void loadManifest(int v) {

		if (v < 500000) {
			setInfinium(true);
			setEpic(false);
		}else {
			setInfinium(false);
			setEpic(true);
		}

		String mf = null;
		if (isInfinium()) { 

			System.out.println("Using 450k data type");
			mf = "resources/manifest_summary.csv";

			if (getClass().getClassLoader().getResourceAsStream(mf)==null) {
				mf = "manifest_summary.csv";
			}

		}else {
			System.out.println("Using epic data type");
			mf = "resources/epic_manifest.csv";

			if (getClass().getClassLoader().getResourceAsStream(mf)==null) {
				mf = "epic_manifest.csv";
			}
		}

		this.manifest = new ReadManifest(mf);
		this.manifest.loadManifest();
	}
	
	public boolean loadManifest(String[][] data) {

		this.manifest = new ReadManifest();
		return(this.manifest.loadManifest(data));
		
	}

	public void setDmrDescriptions(ArrayList<DMRDescription> dmrDescriptions) {
		this.dmrDescriptions = dmrDescriptions;
	}

	public ArrayList<DMRDescription> getDmrDescriptions() {
		return dmrDescriptions;
	}

	public void setDMRPermutationMap(TreeMap<Integer, DMRPermutationSummary> dmrPermutationMap) {
		this.dmrPermutationMap = dmrPermutationMap;
	}

	public TreeMap<Integer, DMRPermutationSummary> getDMRPermutationMap() {
		return dmrPermutationMap;
	}

	public PermutationResultController getPermutationResultController() {
		return permutationResultController;
	}

	public void setMethylationDifference(float[] methylationDifference) {
		this.methylationDifference = methylationDifference;
	}

	//
	public float[] getMethylationDifference() {
		return methylationDifference;
	}

	public void setCoefficients(float[] coefficients) {
		this.coefficients = coefficients;
	}

	public float[] getCoefficients() {
		return coefficients;
	}

	public void setPhenotype(float[][] phenotype) {
		this.phenotype = phenotype;
	}

	public void setCpgDistanceChart(JFreeChart cpgDistanceChart) {
		this.cpgDistanceChart = cpgDistanceChart;
	}
	
	public void setCpgDiffChart(JFreeChart cpgDiffChart) {
		this.cpgDiffChart = cpgDiffChart;
	}

	public void setEmpiricalPvaluesDistributionChart(JFreeChart empiricalPvaluesDistributionChart) {
		this.empiricalPvaluesDistributionChart = empiricalPvaluesDistributionChart;
	}

	public JFreeChart getEmpiricalPvaluesDistributionChart() {
		return empiricalPvaluesDistributionChart;
	}

	public void setEmpiricalPvaluesScatterPlotChart(JFreeChart empiricalPvaluesScatterPlotChart) {
		this.empiricalPvaluesScatterPlotChart = empiricalPvaluesScatterPlotChart;
	}

	public JFreeChart getEmpiricalPvaluesScatterPlotChart() {
		return empiricalPvaluesScatterPlotChart;
	}

	public void setEmpiricalPvaluesScatterPlotLogChart(JFreeChart empiricalPvaluesScatterPlotLogChart) {
		this.empiricalPvaluesScatterPlotLogChart = empiricalPvaluesScatterPlotLogChart;
	}

	public JFreeChart getEmpiricalPvaluesScatterPlotLogChart() {
		return empiricalPvaluesScatterPlotLogChart;
	}

	public void setEmpiricalPvaluesVulcanoPlotChart(JFreeChart vulcanoPlotChart) {
		this.empiricalPvaluesVulcanoPlotChart = vulcanoPlotChart;
	}

	public JFreeChart getEmpiricalPvaluesVulcanoPlotChart() {
		return empiricalPvaluesVulcanoPlotChart;
	}

	public JFreeChart getCpgDistanceChart() {
		return cpgDistanceChart;
	}
	
	public JFreeChart getCpgDiffChart() {
		return cpgDiffChart;
	}

	public JFreeChart getFdrDistributionChart() {
		return fdrDistributionChart;
	}

	public void setFdrDistributionChart(JFreeChart fdrDistributionChart) {
		this.fdrDistributionChart = fdrDistributionChart;
	}

	public JFreeChart getFdrScatterPlotChart() {
		return fdrScatterPlotChart;
	}

	public void setFdrScatterPlotChart(JFreeChart fdrScatterPlotChart) {
		this.fdrScatterPlotChart = fdrScatterPlotChart;
	}

	public JFreeChart getFdrVulcanoPlotChart() {
		return fdrVulcanoPlotChart;
	}

	public void setFdrVulcanoPlotChart(JFreeChart fdrVulcanoPlotChart) {
		this.fdrVulcanoPlotChart = fdrVulcanoPlotChart;
	}

	public void setOriginalPvalues(float[] originalPvalues) {
		this.originalPvalues = originalPvalues;
	}

	public void setEmpiricalPvalues(float[] empiricalPvalues) {
		this.empiricalPvalues = empiricalPvalues;
	}

	public void setManifest(ReadManifest manifest) {
		this.manifest = manifest;
	}

	public ReadManifest getManifest() {
		return manifest;
	}

	public void setBeta(float[][] beta) {
		this.beta = beta;
	}

	public float[][] getBeta() {
		return beta;
	}

	public void setCellComposition(float[][] cellComposition) {
		this.cellComposition = cellComposition;
	}

	public float[][] getCellComposition() {
		return cellComposition;
	}

	public void setScreens(Screens screens) {
		this.screens = screens;
	}

	public void setDataTypeController(DataTypeController dataTypeController) {
		this.dataTypeController = dataTypeController;
		this.dataTypeController.setCanvasController(this);
	}

	public void setModelController(ModelController modelController) {
		this.modelController = modelController;
		this.modelController.setCanvasController(this);
	}

	public void setPermutationParametersController(PermutationParametersController permutationParametersController) {
		this.permutationParametersController = permutationParametersController;
		this.permutationParametersController.setCanvasController(this);
	}

	public void setInputController(InputController inputController) {
		this.inputController = inputController;
		this.inputController.setCanvasController(this);
	}

	public void setExecutePermutationController(ExecutePermutationController executePermutationController) {
		this.executePermutationController = executePermutationController;
		this.executePermutationController.setCanvasController(this);
	}

	public void setPermutationResultController(PermutationResultController permutationResultController) {
		this.permutationResultController = permutationResultController;
		this.permutationResultController.setCanvasController(this);
	}

	public void setDmrParametersController(DMRParametersController dmrParametersController) {
		this.dmrParametersController = dmrParametersController;
		this.dmrParametersController.setCanvasController(this);
	}

	public void setExecuteDMRFinderController(ExecuteDMRFinderController executeDMRFinder) {
		this.executeDMRFinder = executeDMRFinder;
		this.executeDMRFinder.setCanvasController(this);
	}

	public void setDmrResultController(DMRResultController dmrResultController) {
		this.dmrResultController = dmrResultController;
		this.dmrResultController.setCanvasController(this);
	}

	public void enable(int key) {
		treeMapSteps.get(key).setDisable(false);
		/*for (Integer i : treeMapSteps.keySet()) {
			if (i <= key) {
				treeMapSteps.get(key).setDisable(false);
			}else {
				treeMapSteps.get(i).setDisable(true);
			}
		}*/
	}

	// . generate the mapping for the stepButtons
	public void initialize(URL location, ResourceBundle resources) {
		treeMapSteps.put(1, stepDataType_1);
		treeMapSteps.put(2, stepModel_2);
		treeMapSteps.put(3, stepPermutationParameters_3);
		treeMapSteps.put(4, input_4);
		treeMapSteps.put(5, summary_5);
		treeMapSteps.put(6, permutationResult_6);
		treeMapSteps.put(7, dmrParameters_7);
		treeMapSteps.put(8, dmrExecute_8);
		treeMapSteps.put(9, dmrResult_9);
		//File file = new File((getClass().getResource("/resources/dimmer_logo.png").getFile()));
		InputStream	is = getClass().getClassLoader().getResourceAsStream("resources/dimmer_logo.png");
		if (is==null) {
			is = getClass().getClassLoader().getResourceAsStream("dimmer_logo.png");
		}

		Image image = new Image(is);
		logo.setImage(image);
	}

	private void setPermutationSummary() {

		String outputMsg = "";
		String ttest = "";
		String regression = "";

		if (!modelController.isRegression()) {
			ttest+="You have selected to test the significance of the CpGs using a ";

			if (modelController.selectRight()) {
				ttest+="right-sided t-test assuming ";
			}

			if (modelController.selectTwoSided()) {
				ttest+="both-sided t-test assuming ";
			}

			if (modelController.selectLeft()) {
				ttest+="left-sided t-test assuming ";
			}

			if (modelController.selectAssumeEqualVariance()) {
				ttest+="equal variance on both the control and the case group. ";
			}else {
				ttest+="different variance on both the control and the case group. ";
			}

			ttest+="The p-values, empirical p-values, family-wise error rate (FWER), and step-down minP values for the CpGs' significance will be based on " 
					+ permutationParametersController.getNumPermutations() + " permutations. ";

			ttest+="The label/variable of interest is " + inputController.getCoefficient() + "."
					+ " Go get a coffee or two, this might take some time.";

			outputMsg = ttest;

		}else {
			//You have selected to run a regression model on your data.
			//The regression will explain the variable <state> with the \
			//differential methylation using the variable <name1>, <name2>,... as co-factors. 
			//The p-value ..... Take a coffee

			regression+="You have selected to use a regression model on your data to test the significance of your CpGs. "
					+ "The regression will seek to explain the label/variable "+ inputController.getCoefficient();


			if (inputController.getSelectedLabels().size() > 1) {
				regression+=" using the differential methylation levels of each CpG incorporating the labels/variables ";
				for (int i = 0 ; i < inputController.getSelectedLabels().size(); i++) {
					if (!inputController.getSelectedLabels().get(i).equals(inputController.getCoefficient())) { 
						if (i == inputController.getSelectedLabels().size() - 1) {
							regression+=inputController.getSelectedLabels().get(i) + " as co-factors. ";
						}else {
							regression+=inputController.getSelectedLabels().get(i) + ", ";
						}
					}
				}
			}else {
				regression+=". ";
			}
			regression+="The p-values, empirical p-values, family-wise error rate (FWER), "
					+ "and step-down minP values for the CpGs' significance will be based on " + permutationParametersController.getNumPermutations() + " permutations" +
					". Go get a coffee or two, this might take a while.";

			outputMsg=regression;
		}
		this.executePermutationController.setSummaryText(outputMsg);
	}

	private void setDMRExecute() {

		String outputMsg = "You have selected to look for regions of consecutive significantly differentially methylated CpGs in a window of at least size " + dmrParametersController.getWindowSize() + " ";
		outputMsg+=" with at most " + dmrParametersController.getNumException() + " exceptions (i.e. non-significant CpGs). ";
		outputMsg+="Here, a CpG will count as differentially methylated when the selected p-values is below " +  + dmrParametersController.getP0Cutoff() + ".";
		outputMsg+=" Neighboring CpGs pairs will only count as consecutive when they occur within " + dmrParametersController.getCpgDistance() + " base pairs. ";
		outputMsg+="You decided to perform " + dmrParametersController.getNumPermutations() + 
				" permutation runs will be performed to calculate the statistical significance of the DMRs.";

		this.executeDMRFinder.setSummaryText(outputMsg);
	}

	public void setDMRResult(String log) {
		dmrResultController.setSummary(log);
	}

	boolean skiptToDMRParameters = false;
	// . load the screen and change stepButton
	public void loadScreen(String key) {
		if (key.equals("dataType")) {
			enable(1);
		}else if (key.equals("model") || key.equals("ttest") ) {
			enable(2);
			checkStep(stepDataType_1);
		}else if (key.equals("permutationParameters")) {
			enable(3);
			checkStep(input_4);
		}else if (key.equals("inputFiles") || key.equals("labels") || key.equals("coefficients") ) {
			if (modelController.isRegression()) {
				inputController.getCellComposition().setVisible(true);
				inputController.setLabelHeader("Select your input data (the comma-separated CSV file with the annotation) and an output directory, which will be used to write all results and plots. The CSV annotation file comes with your Illumina results, and you will have to add columns with your labels/variables describing the phenotypes/confounders. The number of CPU threads refers to the number of CPU cores that will be used to parallelize the upcoming computations. In doubt, set it to the number of CPU cores of your computer minus one. Further, we can estimate the cell type composition (for whole-blood samples) and select which cell type abundances to include as potential (confounding) factors in the regression model.");
			}else {
				inputController.getCellComposition().setVisible(false);
				inputController.setLabelHeader("Select your input data (the comma-separated CSV file with the annotation) and an output directory, which will be used to write all results and plots. The CSV annotation file comes with your Illumina results, and you will have to add columns with your labels/variables describing the phenotypes/confounders. The number of CPU threads refers to the number of CPU cores that will be used to parallelize the upcoming computations. In doubt, set it to the number of CPU cores of your computer minus one. Further, select your variable of interest (e.g. the phenotype or the disease \"state\")");
			}
			enable(4);
			checkStep(stepModel_2);
		}else if (key.equals("summary")) {
			setPermutationSummary();
			enable(5);
			checkStep(stepPermutationParameters_3);
		}else if (key.equals("permutationResult")){
			//setPermutationResult();
			enable(6);
			checkStep(summary_5);
		}else if (key.equals("dmrParameters")){
			enable(7);
			if (skiptToDMRParameters) {
				checkStep(stepDataType_1);
			}else {
				checkStep(permutationResult_6);
			}
		}else if (key.equals("executeDMR")){
			setDMRExecute();
			enable(8);
			checkStep(dmrParameters_7);
		}else if (key.equals("resultDMR")){
			enable(9);
			checkStep(dmrExecute_8);
		}
		this.screens.setScreen(key);
	}

	@FXML public void screenDataType(ActionEvent actionEvent) {
		this.screens.setScreen("dataType");
	}

	@FXML public void screenModelTtest(ActionEvent actionEvent) {
		this.screens.setScreen("model");
	}


	@FXML public void screenPermutationParameters(ActionEvent actionEvent) {
		this.screens.setScreen("permutationParameters");
	}


	@FXML public void screenInput(ActionEvent actionEvent) {
		this.screens.setScreen("inputFiles");
	}


	@FXML public void screenSummary(ActionEvent actionEvent) {
		setPermutationSummary();
		this.screens.setScreen("summary");
	}


	@FXML public void screenPermutationResults(ActionEvent actionEvent) {
		this.screens.setScreen("permutationResult");
	}


	@FXML public void screenDMRParameters(ActionEvent actionEvent) {
		this.screens.setScreen("dmrParameters");
		//enable(7);
	}

	@FXML public void screenExecuteDMR(ActionEvent actionEvent) {
		setDMRExecute();
		this.screens.setScreen("executeDMR");
	}

	@FXML public void screenResultDMR(ActionEvent actionEvent) {
		this.screens.setScreen("resultDMR");
	}

	public void restart() {
		for (Integer k : treeMapSteps.keySet()) {
			treeMapSteps.get(k).setDisable(true);
		}
		treeMapSteps.get(1).setDisable(false);
		//this.inputController.methylation.setText("");
		//this.inputController.annotation.setText("");
		this.inputController.labels.setText("");
		this.inputController.source.getItems().removeAll(inputController.source.getItems());
		this.inputController.target.getItems().removeAll(inputController.target.getItems());
		this.inputController.output.setText("");
		this.inputController.source.setVisible(false);
		this.inputController.target.setVisible(false);
		this.dmrResultController.cpgList.getItems().removeAll(this.dmrResultController.cpgList.getItems());
		dmrParametersController.getBackButton().setDisable(false);
		loadScreen("dataType");
	}

	public void checkStep(Button b) {
		String text = b.getText();
		if (!text.substring(text.length() - 1).equals("\u221A")) {
			b.setText(b.getText() + " \u221A");
		}
	}

	public void uncheckStep(Button b) {
		String text = b.getText();
		text = text.replace(" \u221A", "");
		b.setText(text);
		b.setDisable(true);
	}

	public void reset() {
		dataTypeController.permutationFile.setText("");
		uncheckStep(stepDataType_1); 
		uncheckStep(stepModel_2);
		uncheckStep(stepPermutationParameters_3);
		uncheckStep(input_4);
		uncheckStep(summary_5);
		uncheckStep(permutationResult_6);
		uncheckStep(dmrParameters_7);
		uncheckStep(dmrExecute_8);
		uncheckStep(dmrResult_9);
		skiptToDMRParameters = false;
	}

	public void setStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	// auxh methods ..............................
	public boolean isPaired() {
		return this.dataTypeController.isPaired();
	}

	public boolean isRegression() {
		return this.modelController.isRegression();
	}

	public boolean isTwoSided() {
		return this.modelController.selectTwoSided();
	}

	public boolean isLeftSided() {
		return this.modelController.selectLeft();
	}

	public boolean isRightSided() {
		return this.modelController.selectRight();
	}

	public boolean includeCellTypeCD8T() {
		return inputController.cd8t.isSelected();
	}

	public boolean includeCellTypeCD4T() {
		return inputController.cd4t.isSelected();
	}

	public boolean includeCellTypeGran() {
		return inputController.gran.isSelected();
	}

	public boolean includeCellTypeMono() {
		return inputController.mono.isSelected();
	}

	public boolean includeCellTypeBCell() {
		return inputController.ncell.isSelected();
	}

	public boolean includeCellTypeNK() {
		return inputController.nk.isSelected();
	}

	public float getPvalueCutoff() {
		return permutationParametersController.getPvalueCutoff();
	}

	public float[] getEmpiricalPvalues() {
		return empiricalPvalues;
	}

	public float[] getOriginalPvalues() {
		return originalPvalues;
	}

	public float[] getCoefficient() {
		return coefficients;
	}

	public float[] getTvalue() {
		return null;
	}

	public String getSampleAnnotationFile() {
		return inputController.getLabelsPath();
	}

	public boolean useCellComposition() {
		return inputController.cellComposition.isSelected();
	}

	public int getNumThreads() {
		return permutationParametersController.getNumThreads();
	}

	public int getNumPermutations() {
		return permutationParametersController.getNumPermutations();
	}

	public float [][] getPhenotype() {
		return phenotype;
	}

	public int getSplitPoint() {
		return DataUtil.getSplitPoint(phenotype);
	}

	public boolean estimateCellComposition() {
		return inputController.getCellComposition().isSelected();
	}

	public void setPermutationResultScreen() {

		Image miniEmpHist = SwingFXUtils.toFXImage(empiricalPvaluesDistributionChart.createBufferedImage(600, 400), null);
		Image miniEmpScatter = SwingFXUtils.toFXImage(empiricalPvaluesScatterPlotChart.createBufferedImage(600, 400), null);
		permutationResultController.empiricalHist.setImage(miniEmpHist);
		permutationResultController.empiricalScatter.setImage(miniEmpScatter);

		Image miniFwerHist = SwingFXUtils.toFXImage(fwerDistributionChart.createBufferedImage(600, 400), null);
		Image miniFwerScatter = SwingFXUtils.toFXImage(fwerScatterPlotChart.createBufferedImage(600, 400), null);
		permutationResultController.fwerHist.setImage(miniFwerHist);
		permutationResultController.fwerScatter.setImage(miniFwerScatter);

		Image miniFdrHist = SwingFXUtils.toFXImage(fdrDistributionChart.createBufferedImage(600, 400), null);
		Image miniFdrScatter = SwingFXUtils.toFXImage(fdrScatterPlotChart.createBufferedImage(600, 400), null);
		permutationResultController.fdrHist.setImage(miniFdrHist);
		permutationResultController.fdrScatter.setImage(miniFdrScatter);


		Image miniSdcHist = SwingFXUtils.toFXImage(steDownDistributionChart.createBufferedImage(600, 400), null);
		Image miniSdcScatter = SwingFXUtils.toFXImage(stepDownScatterPlotChart.createBufferedImage(600, 400), null);
		permutationResultController.sdmHist.setImage(miniSdcHist);
		permutationResultController.sdmScatter.setImage(miniSdcScatter);


		Image miniOrigHist = SwingFXUtils.toFXImage(origDistributionChart.createBufferedImage(600, 400), null);
		permutationResultController.origHist.setImage(miniOrigHist);

		if (empiricalPvaluesVulcanoPlotChart!=null) {
			Image miniEmpVulcano = SwingFXUtils.toFXImage(empiricalPvaluesVulcanoPlotChart.createBufferedImage(600, 400), null);
			permutationResultController.empiricalVulcano.setImage(miniEmpVulcano);

			Image miniFwerVulcano = SwingFXUtils.toFXImage(fwerVulcanoPlotChart.createBufferedImage(600, 400), null);
			permutationResultController.fwerVulcano.setImage(miniFwerVulcano);

			Image miniFdrVulcano = SwingFXUtils.toFXImage(fdrVulcanoPlotChart.createBufferedImage(600, 400), null);
			permutationResultController.fdrVulcano.setImage(miniFdrVulcano);

			Image miniSdcVulcano = SwingFXUtils.toFXImage(stepDownVulcanoPlotChart.createBufferedImage(600, 400), null);
			permutationResultController.sdmVulcano.setImage(miniSdcVulcano);

			Image miniOrigVulcano = SwingFXUtils.toFXImage(origVulcanoPlotChart.createBufferedImage(600, 400), null);
			permutationResultController.origVulcano.setImage(miniOrigVulcano);
		}
	}

	public void setDMRParametersScreen() {
		dmrParametersController.p0Cutoff.setText(""+permutationParametersController.getPvalueCutoff());
		
		Image mini1 = SwingFXUtils.toFXImage(cpgDistanceChart.createBufferedImage(600, 400), null);
		dmrParametersController.imageView.setImage(mini1);
		
		if(cpgDiffChart != null){
			Image mini2 = SwingFXUtils.toFXImage(cpgDiffChart.createBufferedImage(600, 400), null);
			dmrParametersController.imageViewDiff.setImage(mini2);
		}else{
			dmrParametersController.paneDiff.setVisible(false);
			dmrParametersController.paneMinDiff.setVisible(false);
		}

	}

	public String getOutputDirectory() {
		return inputController.getOutputPath();
	}

	public float getP0Cutoff() {
		return dmrParametersController.getP0Cutoff();
	}
	
	public float getMinDiff(){
		return dmrParametersController.getMinDiff();
	}

	public DMRResultController getDmrResultController() {
		return dmrResultController;
	}

	public ImageView getLogo() {
		return logo;
	}

	public void setLogo(ImageView logo) {
		this.logo = logo;
	}

	public Button getStepDataType_1() {
		return stepDataType_1;
	}

	public void setStepDataType_1(Button stepDataType_1) {
		this.stepDataType_1 = stepDataType_1;
	}

	public Button getStepModel_2() {
		return stepModel_2;
	}

	public void setStepModel_2(Button stepModel_2) {
		this.stepModel_2 = stepModel_2;
	}

	public Button getStepPermutationParameters_3() {
		return stepPermutationParameters_3;
	}

	public void setStepPermutationParameters_3(Button stepPermutationParameters_3) {
		this.stepPermutationParameters_3 = stepPermutationParameters_3;
	}

	public Button getInput_4() {
		return input_4;
	}

	public void setInput_4(Button input_4) {
		this.input_4 = input_4;
	}

	public Button getSummary_5() {
		return summary_5;
	}

	public void setSummary_5(Button summary_5) {
		this.summary_5 = summary_5;
	}

	public Button getPermutationResult_6() {
		return permutationResult_6;
	}

	public void setPermutationResult_6(Button permutationResult_6) {
		this.permutationResult_6 = permutationResult_6;
	}

	public Button getDmrParameters_7() {
		return dmrParameters_7;
	}

	public void setDmrParameters_7(Button dmrParameters_7) {
		this.dmrParameters_7 = dmrParameters_7;
	}

	public Button getDmrExecute_8() {
		return dmrExecute_8;
	}

	public void setDmrExecute_8(Button dmrExecute_8) {
		this.dmrExecute_8 = dmrExecute_8;
	}

	public Button getDmrResult_9() {
		return dmrResult_9;
	}

	public void setDmrResult_9(Button dmrResult_9) {
		this.dmrResult_9 = dmrResult_9;
	}

	public TreeMap<Integer, Button> getTreeMapSteps() {
		return treeMapSteps;
	}

	public void setTreeMapSteps(TreeMap<Integer, Button> treeMapSteps) {
		this.treeMapSteps = treeMapSteps;
	}

	public ExecuteDMRFinderController getExecuteDMRFinder() {
		return executeDMRFinder;
	}

	public void setExecuteDMRFinder(ExecuteDMRFinderController executeDMRFinder) {
		this.executeDMRFinder = executeDMRFinder;
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	public ArrayList<DMR> getDmrs() {
		return dmrs;
	}

	public void setDmrs(ArrayList<DMR> dmrs) {
		this.dmrs = dmrs;
	}

	public TreeMap<Integer, DMRPermutationSummary> getDmrPermutationMap() {
		return dmrPermutationMap;
	}

	public void setDmrPermutationMap(TreeMap<Integer, DMRPermutationSummary> dmrPermutationMap) {
		this.dmrPermutationMap = dmrPermutationMap;
	}

	public boolean isSkiptToDMRParameters() {
		return skiptToDMRParameters;
	}

	public void setSkiptToDMRParameters(boolean skiptToDMRParameters) {
		this.skiptToDMRParameters = skiptToDMRParameters;
	}

	public DataTypeController getDataTypeController() {
		return dataTypeController;
	}

	public ModelController getModelController() {
		return modelController;
	}

	public PermutationParametersController getPermutationParametersController() {
		return permutationParametersController;
	}

	public InputController getInputController() {
		return inputController;
	}

	public ExecutePermutationController getExecutePermutationController() {
		return executePermutationController;
	}

	public DMRParametersController getDmrParametersController() {
		return dmrParametersController;
	}

	public Screens getScreens() {
		return screens;
	}


}
