package dk.sdu.imada.gui.controllers.util;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;


public class Browser extends Region {

	private HBox toolBar;
	private boolean needDocumentationButton = false;
	final WebView browser = new WebView();


	public Browser(String link) {

		final WebEngine webEngine = browser.getEngine();
		final Button showPrevDoc = new Button("Toggle Previous Docs");
		final WebView smallView = new WebView();
		//apply the styles
		getStyleClass().add("browser");

		// create the toolbar
		toolBar = new HBox();
		toolBar.setAlignment(Pos.CENTER);
		toolBar.getStyleClass().add("browser-toolbar");
		toolBar.getChildren().add(createSpacer());

		smallView.setPrefSize(120, 80);

		//handle popup windows
		webEngine.setCreatePopupHandler(
				new Callback<PopupFeatures, WebEngine>() {
					@Override public WebEngine call(PopupFeatures config) {
						smallView.setFontScale(0.8);
						if (!toolBar.getChildren().contains(smallView)) {
							toolBar.getChildren().add(smallView);
						}
						return smallView.getEngine();
					}
				}
				);

		// process page loading
		webEngine.getLoadWorker().stateProperty().addListener(
				new ChangeListener<State>() {
					@Override
					public void changed(ObservableValue<? extends State> ov,
							State oldState, State newState) {
						toolBar.getChildren().remove(showPrevDoc);    
						if (newState == State.SUCCEEDED) {
							if (needDocumentationButton) {
								toolBar.getChildren().add(showPrevDoc);
							}
						}
					}
				}
				);

		// load the home page        
		webEngine.load(link);

		//add components
		getChildren().add(toolBar);
		getChildren().add(browser);
	}



	// JavaScript interface object
	public class JavaApp {

		public void exit() {
			Platform.exit();
		}
	}

	private Node createSpacer() {
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		return spacer;
	}

	@Override
	protected void layoutChildren() {
		double w = getWidth();
		double h = getHeight();
		double tbHeight = toolBar.prefHeight(w);
		layoutInArea(browser,0,0,w,h-tbHeight,0,HPos.CENTER, VPos.CENTER);
		layoutInArea(toolBar,0,h-tbHeight,w,tbHeight,0,HPos.CENTER,VPos.CENTER);
	}

	@Override
	protected double computePrefWidth(double height) {
		return 900;
	}

	@Override
	protected double computePrefHeight(double width) {
		return 600;
	}
}