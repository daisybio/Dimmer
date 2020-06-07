package dk.sdu.imada.gui.controllers;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class StartGUI extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {

		try {
			// FXML Loaders
			FXMLLoader canvasLoader = new FXMLLoader((getClass().getResource("canvas.fxml")));
			FXMLLoader dataTypeLoader = new FXMLLoader((getClass().getResource("dataType.fxml")));
			FXMLLoader modelLoader = new FXMLLoader((getClass().getResource("model.fxml")));
			//FXMLLoader ttestLoader = new FXMLLoader((getClass().getResource("ttest.fxml")));
			FXMLLoader permutationParametersLoader = new FXMLLoader((getClass().getResource("permutationParameters.fxml")));
			FXMLLoader inputLoader = new FXMLLoader((getClass().getResource("input.fxml")));
			//FXMLLoader labelsLoader = new FXMLLoader((getClass().getResource("labelSelection.fxml")));
			//FXMLLoader coefficientsLoader = new FXMLLoader((getClass().getResource("coefficientSelection.fxml")));
			FXMLLoader executePermutationLoader = new FXMLLoader((getClass().getResource("executePermutation.fxml")));
			FXMLLoader permutationResultLoader = new FXMLLoader((getClass().getResource("permutationResult.fxml")));
			FXMLLoader dmrParametersLoader = new FXMLLoader((getClass().getResource("dmrParameters.fxml")));
			FXMLLoader executeDMRLoader = new FXMLLoader((getClass().getResource("executeDMR.fxml")));
			FXMLLoader dmrResultLoader = new FXMLLoader((getClass().getResource("DMRResult.fxml")));
			//FXMLLoader consoleLoader = new FXMLLoader((getClass().getResource("Console.fxml")));

			// FXGUI Objects
			BorderPane borderPane = (BorderPane) canvasLoader.load();
			StackPane dataTypePane = (StackPane) dataTypeLoader.load();
			StackPane modelPane = (StackPane) modelLoader.load();
			//StackPane ttestPane = (StackPane) ttestLoader.load();
			StackPane permutationParametersPane = (StackPane) permutationParametersLoader.load();
			StackPane inputPane = (StackPane) inputLoader.load();
			//StackPane labelPane = (StackPane) labelsLoader.load();
			//StackPane coefficientsPane = (StackPane) coefficientsLoader.load();
			StackPane executePermutationPane = (StackPane) executePermutationLoader.load();
			StackPane permutationResultPane = (StackPane) permutationResultLoader.load();
			StackPane dmrParametersPane = (StackPane) dmrParametersLoader.load();
			StackPane executeDMRPane = (StackPane) executeDMRLoader.load();
			StackPane dmrResultPane = (StackPane) dmrResultLoader.load();
			//StackPane consolePane = (StackPane) consoleLoader.load();


			// adding transition screens
			Screens screens = new Screens();
			screens.addScreen("dataType", dataTypePane); 
			screens.addScreen("model", modelPane);
			//screens.addScreen("ttest", ttestPane);
			screens.addScreen("permutationParameters", permutationParametersPane);
			screens.addScreen("inputFiles", inputPane);
			//screens.addScreen("labels", labelPane);
			//screens.addScreen("coefficients", coefficientsPane);
			screens.addScreen("summary", executePermutationPane);
			screens.addScreen("permutationResult", permutationResultPane);
			screens.addScreen("dmrParameters", dmrParametersPane);
			screens.addScreen("executeDMR", executeDMRPane);
			screens.addScreen("resultDMR", dmrResultPane);

			// getting and setting controllers
			MainController canvasController = (MainController) canvasLoader.getController();
			DataTypeController dataTypeController = (DataTypeController) dataTypeLoader.getController();
			ModelController modelController = (ModelController) modelLoader.getController();
			//TTestController tTestController = (TTestController) ttestLoader.getController();
			PermutationParametersController permutationParametersController = (PermutationParametersController) permutationParametersLoader.getController();
			InputController  inputController = (InputController) inputLoader.getController();
			//LabelsController labelsController = (LabelsController) labelsLoader.getController();
			//CoefficientController coefficientController = (CoefficientController) coefficientsLoader.getController();
			ExecutePermutationController executePermutationController = (ExecutePermutationController) executePermutationLoader.getController();
			PermutationResultController permutationResultController = (PermutationResultController) permutationResultLoader.getController();
			DMRParametersController dmrParametersController = (DMRParametersController) dmrParametersLoader.getController();
			ExecuteDMRFinderController executeDMRFinderController = (ExecuteDMRFinderController) executeDMRLoader.getController();
			DMRResultController dmrResultController = (DMRResultController) dmrResultLoader.getController();
			//ConsoleController consoleController = (ConsoleController) consoleLoader.getController();

			canvasController.setScreens(screens);
			canvasController.setDataTypeController(dataTypeController);
			canvasController.setModelController(modelController);
			//canvasController.settTestController(tTestController);
			canvasController.setPermutationParametersController(permutationParametersController);
			canvasController.setInputController(inputController);
			//canvasController.setLabelsController(labelsController);
			//canvasController.setCoefficientController(coefficientController);
			canvasController.setExecutePermutationController(executePermutationController);
			canvasController.setPermutationResultController(permutationResultController);
			canvasController.setDmrParametersController(dmrParametersController);
			canvasController.setExecuteDMRFinderController(executeDMRFinderController);
			canvasController.setDmrResultController(dmrResultController);
			//canvasController.setConsoleController(consoleController);

			//adjust canvas center
			borderPane.setCenter(screens);
			//borderPane.setBottom(consolePane);

			//setting first screen and setting up Scene
			canvasController.loadScreen("dataType");
			Scene scene = new Scene(borderPane, 1050, 770);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.centerOnScreen();
			primaryStage.show();

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					System.exit(0);
				}
			});       

		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
