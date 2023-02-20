package dk.sdu.imada.gui.controllers;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class DMRLinksForm implements Runnable{

	Stage dialogStage;
	private TextArea texArea;
	DRMLinksController controller;

	public DMRLinksForm(String link) {
		try {
			FXMLLoader loader = new FXMLLoader((getClass().getResource("LinkForm.fxml")));
			dialogStage = new Stage();
			StackPane progressPane = (StackPane) loader.load();
			controller = (DRMLinksController) loader.getController();
			Scene scene = new Scene(progressPane, 500, 170);
			
			this.texArea = controller.getTextArea();
			this.texArea.setText(link);
			dialogStage.setScene(scene);
			dialogStage.setResizable(false);
			
		} catch (IOException e) {
		}
	}
	
	@Override
	public void run() {
		dialogStage.show();
	}

}

