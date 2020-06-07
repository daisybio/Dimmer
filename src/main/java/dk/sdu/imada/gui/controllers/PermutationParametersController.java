package dk.sdu.imada.gui.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class PermutationParametersController {

	@FXML TextField numThreads;
	@FXML TextField numPermutations;
	@FXML TextField pvalueCutoff;

	MainController canvasController;

	@FXML public void pushContinue(ActionEvent actionEvent) {

		boolean condition = true;

		if (checkNumericalFormat(numThreads.getText())) {
			condition &= true;
		}else {
			condition &= false;
			FXPopOutMsg.showWarning("Num threads must be a number and >= 1");
		}

		if (checkNumericalFormat(numPermutations.getText())) {
			condition &= true;
		}else {
			condition &= false;
			FXPopOutMsg.showWarning("Num permutations must be a number >= 0");
		}

		if (checkNumericalFormat(pvalueCutoff.getText())) {
			condition &= true;
		}else {
			condition &= false;
			FXPopOutMsg.showWarning("Num permutations must be a number and between [0, 1]");
		}

		if (condition) {
			this.canvasController.loadScreen("summary");
			//this.canvasController.loadScreen("inputFiles");
			//this.log();
		}
	}
	
	@FXML public void help(ActionEvent actionEvent) {
		String msg = "testing ";
		
		FXPopOutMsg.showHelp(msg);
	}


	@FXML public void pushBack(ActionEvent actionEvent) {
		this.canvasController.loadScreen("inputFiles");
	}

	public void setCanvasController(MainController canvasController) {
		this.canvasController = canvasController;
	}

	private boolean checkNumericalFormat(String str) {
		Double d;
		try  
		{  
			d = Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  

		if (d > 0)
			return true;

		else return false;
	}

	public int getNumThreads() {
		return Integer.parseInt(numThreads.getText());
	}

	public int getNumPermutations() {
		return Integer.parseInt(numPermutations.getText());
	}

	public float getPvalueCutoff() {
		return Float.parseFloat(pvalueCutoff.getText());
	}

	public void log() {
		System.out.println("testing permuation parameters:");
		System.out.println(getNumThreads());
		System.out.println(getNumPermutations());
		System.out.println(getPvalueCutoff());
	}
}
