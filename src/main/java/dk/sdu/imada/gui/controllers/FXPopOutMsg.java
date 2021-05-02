package dk.sdu.imada.gui.controllers;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;

public class FXPopOutMsg {
	
	public static void showWarning(String msg) {
		
		Alert alert = new Alert(AlertType.WARNING);

		alert.getDialogPane().setContent(new Label(msg));

		alert.showAndWait();
	}
	
	public static void showMsg(String msg) {
		
		Alert alert = new Alert(AlertType.INFORMATION);

		alert.getDialogPane().setContent(new Label(msg));

		alert.showAndWait();
	}
	
	public static void showHelp(String msg) {
		
		Alert alert = new Alert(AlertType.INFORMATION);

		//alert.setContentText(msg);
		alert.setHeaderText(msg);

		alert.showAndWait();
		
	}
}
