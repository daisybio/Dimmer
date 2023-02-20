package dk.sdu.imada.gui.controllers;

import java.io.IOException;
import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProgressForm implements Runnable{

	ProgressBar progressBar;
	Stage dialogStage;
	ProgressController progressController;
	Button button;
	ArrayList<Thread> threads;
	Text text;

	public void setThreads(ArrayList<Thread> threads) {
		this.threads = threads;
	}

	@SuppressWarnings("deprecation")
	public void cancelThreads() {
		//System.out.println(threads.size());
		if (threads.size() > 0) {
			for (Thread t : threads) {
				t.stop();
			}
		}
		dialogStage.close();
	}
	
	
	public ProgressForm() {
		try {
			FXMLLoader loader = new FXMLLoader((getClass().getResource("ProgressForm.fxml")));
			dialogStage = new Stage();
			StackPane progressPane = (StackPane) loader.load();
			progressController = (ProgressController) loader.getController();
			Scene scene = new Scene(progressPane, 500, 170);
			dialogStage.setScene(scene);
			dialogStage.setResizable(false);
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setAlwaysOnTop(true);
			this.progressBar = progressController.getProgressBar();
			this.button = progressController.getButton();
			this.text = progressController.getLabel();

			button.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					cancelThreads();
				}
			});
		}catch(IOException e) {

		}
	}
	
	public ProgressForm(String s) {
		try {
			FXMLLoader loader = new FXMLLoader((getClass().getResource("ProgressForm.fxml")));
			dialogStage = new Stage();
			StackPane progressPane = (StackPane) loader.load();
			progressController = (ProgressController) loader.getController();
			Scene scene = new Scene(progressPane, 500, 170);
			dialogStage.setScene(scene);
			dialogStage.setResizable(false);
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setAlwaysOnTop(true);
			this.progressBar = progressController.getProgressBar();
			this.button = progressController.getButton();
			this.text = progressController.getLabel();
			this.text.setText(s);

			button.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					cancelThreads();
				}
			});
		}catch(IOException e) {

		}
	}

	public Stage getDialogStage() {
		return dialogStage;
	}

	public Text getText() {
		return text;
	}

	public Button getButton() {
		return button;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	@Override
	public void run() {
		dialogStage.show();
	}
}