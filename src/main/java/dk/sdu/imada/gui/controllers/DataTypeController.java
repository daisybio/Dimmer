package dk.sdu.imada.gui.controllers;
 
import java.io.File;
import java.util.ArrayList;

import dk.sdu.imada.gui.controllers.util.ReadDimmerProjectUtil;
import dk.sdu.imada.gui.monitors.DimmerProjectMonitor;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;

public class DataTypeController {

	@FXML TextField permutationFile;
	@FXML ToggleGroup group;
	@FXML RadioButton paired; 
	@FXML RadioButton unpaired; 

	@FXML public void help(ActionEvent actionEvent) {
		String msg = "testing ";
		FXPopOutMsg.showHelp(msg);
	}

	@FXML public void pushContinue(ActionEvent actionEvent) {
		mainController.uncheckStep(mainController.stepModel_2);
		mainController.uncheckStep(mainController.stepPermutationParameters_3);
		mainController.uncheckStep(mainController.input_4);
		mainController.uncheckStep(mainController.summary_5);
		mainController.uncheckStep(mainController.permutationResult_6);
		mainController.uncheckStep(mainController.dmrParameters_7);
		mainController.uncheckStep(mainController.dmrExecute_8);
		mainController.uncheckStep(mainController.dmrResult_9);
		mainController.getDmrParametersController().getBackButton().setDisable(false);
		this.mainController.loadScreen("model");
	}

	MainController mainController;

	public void setCanvasController(MainController canvasController) {
		this.mainController = canvasController;
	}

	public boolean isPaired() {
		return paired.isSelected();
	}

	int progress = 0;
	
	@FXML public void openPermutationFile(ActionEvent actionEvent) {

		FileChooser fileChooser = new FileChooser();

		fileChooser.setTitle("Open your dimmer project ");

		File f = fileChooser.showOpenDialog(null);

		if (f!=null) {
			permutationFile.setText(f.getAbsolutePath());
			try {
				String input = f.getAbsolutePath();
				String directory = f.getParent() + "/";
				mainController.inputController.output.setText(directory);

				ProgressForm progressForm = new ProgressForm();
				
				ReadDimmerProjectUtil projectUtil = new ReadDimmerProjectUtil(mainController, input);
				Thread readerThread = new Thread(projectUtil);
				
				DimmerProjectMonitor monitor = new DimmerProjectMonitor(mainController, projectUtil, progressForm);
				Thread monitorThread = new Thread(monitor);

				ArrayList<Thread> threads = new ArrayList<>();
				
				threads.add(monitorThread);
				threads.add(readerThread);
				
				progressForm.setThreads(threads);
				
				Platform.runLater(progressForm);
				monitorThread.start();
				readerThread.start();

			}catch(NumberFormatException e) {
				//System.out.println("Error number exception");
				FXPopOutMsg.showWarning("The file can't be loaded");
				//e.printStackTrace();
			} catch (Exception e) {
				//System.out.println("other " + e.getMessage());
				FXPopOutMsg.showWarning("The file can't be loaded");
				//e.printStackTrace();
			}
		}
	}
	
	/*private void loadPermutationfile(String input) {
		
	}*/

}
