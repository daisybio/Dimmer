package dk.sdu.imada.gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class ModelController {

	//boolean regression;
	MainController canvasController;


	@FXML ToggleGroup group;
	@FXML RadioButton regression; 
	@FXML RadioButton ttest;

	@FXML ToggleGroup group1;
	@FXML RadioButton twoSided; 
	@FXML RadioButton left; 
	@FXML RadioButton  right;
	@FXML CheckBox assumeEqualVariance;

	@FXML VBox ttestBox;

	boolean check = true;

	private void setSize(VBox scrollVbox, int v) {
		scrollVbox.setPrefHeight(v);
		scrollVbox.setMinHeight(v);
		scrollVbox.setMaxHeight(v);
	}

	@FXML public void selectEvent(ActionEvent actionEvent) {

		boolean isaChange = !(check ^ regression.isSelected());

		if (isaChange) {
			canvasController.uncheckStep(canvasController.stepPermutationParameters_3);
			canvasController.uncheckStep(canvasController.input_4);
			canvasController.uncheckStep(canvasController.summary_5);
			canvasController.uncheckStep(canvasController.permutationResult_6);
			canvasController.uncheckStep(canvasController.dmrParameters_7);
			canvasController.uncheckStep(canvasController.dmrExecute_8);
			canvasController.uncheckStep(canvasController.dmrResult_9);
			canvasController.inputController.setRegressionLabelsElementsVisible(false);
			canvasController.inputController.setTTestLabelsElementsVisible(false);
			canvasController.inputController.resetTextFields();

			canvasController.permutationResultController.paneEmp.setVisible(false);
			canvasController.permutationResultController.paneFdr.setVisible(false);
			canvasController.permutationResultController.paneFwer.setVisible(false);

			setSize(canvasController.permutationResultController.scrollVboxEmp, 500);
			setSize(canvasController.permutationResultController.scrollVboxFdr, 500);
			setSize(canvasController.permutationResultController.scrollVboxFwer, 500);
			setSize(canvasController.permutationResultController.scrollVboxMax, 500);
		}

		if (regression.isSelected()) {
			check = false;
			ttestBox.setVisible(false);
			//canvasController.coefficientController.subtitle.setText("4.2 Select variable of interest ");
			canvasController.inputController.cd4t.setSelected(true);
			canvasController.inputController.cd8t.setSelected(true);
			canvasController.inputController.gran.setSelected(true);
			canvasController.inputController.nk.setSelected(true);
			canvasController.inputController.mono.setSelected(true);
			canvasController.inputController.ncell.setSelected(true);

			canvasController.permutationResultController.paneEmp.setVisible(false);
			canvasController.permutationResultController.paneFdr.setVisible(false);
			canvasController.permutationResultController.paneFwer.setVisible(false);
			canvasController.permutationResultController.peaneMax.setVisible(false);
			canvasController.permutationResultController.paneOrig.setVisible(false);

			setSize(canvasController.permutationResultController.scrollVboxEmp, 500);
			setSize(canvasController.permutationResultController.scrollVboxFdr, 500);
			setSize(canvasController.permutationResultController.scrollVboxFwer, 500);
			setSize(canvasController.permutationResultController.scrollVboxMax, 500);

		}else {
			check = true;
			ttestBox.setVisible(true);
			canvasController.inputController.paneCC.setVisible(false);

			canvasController.permutationResultController.paneEmp.setVisible(true);
			canvasController.permutationResultController.paneFdr.setVisible(true);
			canvasController.permutationResultController.paneFwer.setVisible(true);
			canvasController.permutationResultController.peaneMax.setVisible(true);
			canvasController.permutationResultController.paneOrig.setVisible(true);

			setSize(canvasController.permutationResultController.scrollVboxEmp, 750);
			setSize(canvasController.permutationResultController.scrollVboxFdr, 750);
			setSize(canvasController.permutationResultController.scrollVboxFwer, 750);
			setSize(canvasController.permutationResultController.scrollVboxMax, 750);

			/*setSize(canvasController.permutationResultController.scrollVboxEmp, 1900);
			setSize(canvasController.permutationResultController.scrollVboxFdr, 1900);
			setSize(canvasController.permutationResultController.scrollVboxFwer, 1900);
			setSize(canvasController.permutationResultController.scrollVboxMax, 1900);
			setSize(canvasController.permutationResultController.scrollVboxOrig, 1900);*/

			//canvasController.coefficientController.subtitle.setText("4.1 Select variable of interest ");
			//canvasController.permutationResultController.vHbox.setVisible(true);
			//canvasController.permutationResultController.scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
			/*canvasController.permutationResultController.scrollVbox.setPrefHeight(2350);
			canvasController.permutationResultController.scrollVbox.setMinHeight(2350);
			canvasController.permutationResultController.scrollVbox.setMaxHeight(2350);*/
		}
	}

	@FXML public void help(ActionEvent actionEvent) {
		String msg = "testing ";
		FXPopOutMsg.showHelp(msg);
	}

	@FXML public void pushBack(ActionEvent actionEvent) {
		this.canvasController.loadScreen("dataType");
	}

	@FXML public void pushContinue(ActionEvent actionEvent) {
		if (regression.isSelected() || ttest.isSelected()) {
			this.canvasController.loadScreen("inputFiles");
			//this.canvasController.loadScreen("permutationParameters");
		}else {
			FXPopOutMsg.showWarning("Select a model");
		}
	}

	public void setCanvasController(MainController canvasController) {
		this.canvasController = canvasController;
	}

	public boolean isRegression() {
		return regression.isSelected();
	}

	public void log() {
		//System.out.println("Regression: " + isRegression());
	}

	public boolean selectTwoSided() {
		return twoSided.selectedProperty().get();
	}

	public boolean selectLeft() {
		return left.selectedProperty().get();
	}

	public boolean selectRight() {
		return right.selectedProperty().get();
	}

	public boolean selectAssumeEqualVariance() {
		return assumeEqualVariance.selectedProperty().get();
	}
}
