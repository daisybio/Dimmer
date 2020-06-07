package dk.sdu.imada.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;

public class ProgressController {

	@FXML ProgressBar progressBar;
	@FXML Button button;
	@FXML Text text;
	
	public Button getButton() {
		return button;
	}
	
	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public Text getLabel() {
		return text;
	}
	
}
