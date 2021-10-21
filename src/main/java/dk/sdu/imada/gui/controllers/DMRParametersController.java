package dk.sdu.imada.gui.controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import dk.sdu.imada.gui.plots.HistogramPvalueDistribution;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.CpG;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class DMRParametersController {
	
	@FXML TextField nRandomRegions;
	@FXML TextField p0Cutoff;
	@FXML TextField minDiff;
	@FXML TextField numPermutations;
	@FXML TextField windowSize;
	@FXML TextField cpgDistance;
	@FXML TextField numException;
	@FXML ImageView imageView;
	@FXML Button backButton;
	
	@FXML RadioButton empPvalueRadioButton;
	@FXML RadioButton fwerPvalueRadioButton;
	@FXML RadioButton maxFdrPvalueRadioButton;
	@FXML RadioButton origPvalueRadioButton;
	@FXML RadioButton sdcPvalueRadioButton;

	MainController canvasController;

	public void setCanvasController(MainController canvasController) {
		this.canvasController = canvasController;
	}

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}

	@FXML public void pushBack(ActionEvent actionEvent) {
		try {
			canvasController.loadScreen("permutationResult");
		}catch(NullPointerException e) {
			FXPopOutMsg.showWarning("Step not avaliable");
		}
	}
	
	@FXML public void selectFwer(ActionEvent actionEvent) {
		canvasController.setSearchPvalues(canvasController.getFwerPvalues());
		System.out.println("Using Fwer");
	}
	
	@FXML public void selectEmpirical(ActionEvent actionEvent) {
		canvasController.setSearchPvalues(canvasController.getEmpiricalPvalues());
		System.out.println("Using empirical p-values");
	}
	
	@FXML public void selectMaxFdr(ActionEvent actionEvent) {
		canvasController.setSearchPvalues(canvasController.getFdrPvalues());
		System.out.println("Using Fdr.");
	}
	
	@FXML public void selectOrig(ActionEvent actionEvent) {
		canvasController.setSearchPvalues(canvasController.getOriginalPvalues());
		System.out.println("Using original p-values");
	}

	@FXML public void selectSdc(ActionEvent actionEvent) {
		canvasController.setSearchPvalues(canvasController.getStepDownMinPvalues());
		System.out.println("Using Step-down minP");
	}
	
	@FXML public void pushContinue(ActionEvent actionEvent) {

		boolean condition = true;

		if (checkDoubleFormat(p0Cutoff.getText())) {
			condition&=true;
		}else {
			condition&=false;
			FXPopOutMsg.showWarning("You need a numerical value for p0 between [0, 1]");
		}
		
		if (checkDoubleFormat(minDiff.getText())) {
			condition&=true;
		}else {
			condition&=false;
			FXPopOutMsg.showWarning("You need a numerical value for the minimum mean methylation difference between [0, 1]");
		}

		if (checkIntegerFormat(numPermutations.getText())) {
			condition&=true;
		}else {
			condition&=false;
			FXPopOutMsg.showWarning("You need a integer numerical value > 0 for permutations");
		}
		
		if (checkIntegerFormat(nRandomRegions.getText())) {
			condition&=true;
		}else {
			condition&=false;
			FXPopOutMsg.showWarning("You need a integer numerical value > 0 for permutations");
		}

		if (checkIntegerFormat(windowSize.getText())) {
			condition&=true;
		}else {
			condition&=false;
			FXPopOutMsg.showWarning("You need a integer numerical value for the > 0 window size");
		}

		if (checkIntegerFormat(cpgDistance.getText())) {
			condition&=true;
		}else {
			condition&=false;
			FXPopOutMsg.showWarning("You need a integer numerical value > 0 for CpG distance");
		}		

		if (checkIntegerFormat(numException.getText())) {
			condition&=true;
		}else {
			condition&=false;
			FXPopOutMsg.showWarning("You need a integer numerical value >= 0 for the number of exceptions");
		}

		if (condition){
			
			if (empPvalueRadioButton.isSelected()) {
				canvasController.setSearchPvalues(canvasController.getEmpiricalPvalues());
				System.out.println("Using empirical p-values");
			}else if (fwerPvalueRadioButton.isSelected()){
				canvasController.setSearchPvalues(canvasController.getFwerPvalues());
				System.out.println("Using global Fwer. p-values");
			}else if (maxFdrPvalueRadioButton.isSelected()) {
				canvasController.setSearchPvalues(canvasController.getFdrPvalues());
				System.out.println("Using Fdr. p-values");
			}else if (origPvalueRadioButton.isSelected()) {
				canvasController.setSearchPvalues(canvasController.getOriginalPvalues());
				System.out.println("Using original p-values");
			}else if (sdcPvalueRadioButton.isSelected())  {
				canvasController.setSearchPvalues(canvasController.getStepDownMinPvalues());
				System.out.println("Using step down min p-values");
			}
			canvasController.loadScreen("executeDMR");
		}
	}

	@FXML public void plotDistribution(ActionEvent actionEvent) {

		JFreeChart chart = canvasController.getCpgDistanceChart();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JPanel panel = new ChartPanel(chart);
				JFrame frame = new JFrame("");
				frame.setSize(600, 400);
				frame.setLocationRelativeTo(null);
				frame.add(panel);
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			}
		});
	}

	private boolean checkDoubleFormat(String str) {
		Double d;
		try  
		{  
			d = Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  

		if (d >= 0 && d <= 1.0)
			return true;

		else return false;
	}

	private boolean checkIntegerFormat(String str) {
		Integer d;
		try  
		{  
			d = Integer.parseInt(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  

		if (d > 0)
			return true;

		else return false;
	}

	@FXML public void pushSaveHistogram(ActionEvent e) {
		JFreeChart chart = canvasController.getCpgDistanceChart();
		BufferedImage img = chart.createBufferedImage(1200, 800);
		exportChart(img);
	}

	private void exportChart(BufferedImage bufferedImage) {
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(new File((canvasController.inputController.getOutputPath())));

			File outPutFile = fileChooser.showSaveDialog(null);

			if (outPutFile!=null) {
				ImageIO.write(bufferedImage, "png", outPutFile);
			}

		}catch(IOException e) {
			FXPopOutMsg.showWarning("Can't save file");
		}
	}

	public Float getP0Cutoff() {
		return Float.parseFloat(p0Cutoff.getText());
	}
	
	public Float getMinDiff(){
		return Float.parseFloat(minDiff.getText());
	}

	public Integer getWindowSize() {
		return Integer.parseInt(windowSize.getText());
	}

	public Integer getNumPermutations() {
		return Integer.parseInt(numPermutations.getText());
	}
	
	public Integer getNRandomRegions(){
		return Integer.parseInt(nRandomRegions.getText());
	}

	public Integer getNumException() {
		return Integer.parseInt(numException.getText());
	}

	public Integer getCpgDistance() {
		return Integer.parseInt(cpgDistance.getText());
	}

	public void setCpgAnnotation(String [][] cpgAnnotation) {

		double []distribution = getCpGDistribution(10000.0);
		HistogramPvalueDistribution his = new HistogramPvalueDistribution("CpG distance distribution", distribution, "CpG difference distance (bp)", "Frequency", 100, java.awt.Color.BLUE, 0.0, 10000.0, 0, 10000);
		JFreeChart chart = his.getChart();
		canvasController.setCpgDistanceChart(chart);
		Image mni = SwingFXUtils.toFXImage(chart.createBufferedImage(600, 400), null);
		imageView.setImage(mni);
	}

	private double[] getCpGDistribution(double treshold) {

		ReadManifest m = canvasController.getManifest();

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

	public Button getBackButton() {
		return backButton;
	}
}
