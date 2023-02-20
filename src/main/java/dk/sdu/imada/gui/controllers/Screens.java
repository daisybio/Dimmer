package dk.sdu.imada.gui.controllers;

import java.util.HashMap;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Screens extends StackPane {

	HashMap <String, StackPane> screenMap = new HashMap<String, StackPane>();

	public void addScreen(String key, StackPane stackPane) {
		screenMap.put(key, stackPane);
	}

	public StackPane getMap(String key) {
		return screenMap.get(key);
	}


	public boolean setScreen(final String key) { 

		if(screenMap.get(key) != null) { //screen loaded 
			final DoubleProperty opacity = opacityProperty(); 

			//Is there is more than one screen 
			if(!getChildren().isEmpty()){ 
				@SuppressWarnings({ "rawtypes", "unchecked" })
				Timeline fade = new Timeline( 
						new KeyFrame(Duration.ZERO, 
								new KeyValue(opacity,1.0)), 
						new KeyFrame(new Duration(100), new EventHandler() { 
							public void handle(Event event) {
								//remove displayed screen 
								getChildren().remove(0); 
								//add new screen 
								getChildren().add(0, screenMap.get(key)); 
								Timeline fadeIn = new Timeline( 
										new KeyFrame(Duration.ZERO, 
												new KeyValue(opacity, 0.0)), 
										new KeyFrame(new Duration(100), 
												new KeyValue(opacity, 1.0))); 
								fadeIn.play();
							}
						}, new KeyValue(opacity, 0.0))); 
				fade.play(); 
			} else { 
				//no one else been displayed, then just show 
				setOpacity(0.0); 
				getChildren().add(screenMap.get(key)); 
				Timeline fadeIn = new Timeline( 
						new KeyFrame(Duration.ZERO, 
								new KeyValue(opacity, 0.0)), 
						new KeyFrame(new Duration(2500), 
								new KeyValue(opacity, 1.0))); 
				fadeIn.play(); 
			} 
			return true; 
		} else { 
			//System.out.println("screen hasn't been loaded!\n"); 
			return false; 
		}
	}

}
