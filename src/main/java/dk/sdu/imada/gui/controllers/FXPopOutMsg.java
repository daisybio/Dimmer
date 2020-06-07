package dk.sdu.imada.gui.controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class FXPopOutMsg {
	
	public static void showWarning(String msg) {
		
		Alert alert = new Alert(AlertType.WARNING);

		alert.setContentText(msg);

		alert.showAndWait();
	}
	
	public static void showMsg(String msg) {
		
		Alert alert = new Alert(AlertType.INFORMATION);

		alert.setContentText(msg);

		alert.showAndWait();
	}
	
	public static void showHelp(String msg) {
		
		Alert alert = new Alert(AlertType.INFORMATION);

		//alert.setContentText(msg);
		alert.setHeaderText(msg);

		alert.showAndWait();
		
	}
}
