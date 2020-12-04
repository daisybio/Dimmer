package dk.sdu.imada.gui.controllers;

import java.awt.Desktop;
import java.net.URI;

import dk.sdu.imada.jlumina.search.primitives.DMRDescription;
import dk.sdu.imada.jlumina.search.primitives.DMRPermutationSummary;
import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public class FXDMRFullSummary {

	private final Button hyperlink;

	// result from the search
	private final IntegerProperty cpgID;

	private  final IntegerProperty numberOfIslands;

	// average of islands in the permutation
	private  final FloatProperty averageOfIslands;

	// chance of observing in the permutation Islands of at least the same number of CpGs
	private final FloatProperty pvalue;

	// 
	private final FloatProperty logRatio;

	private final StringProperty chromosome;
	private final IntegerProperty beginPosition;
	private final IntegerProperty endPosition;
	private final StringProperty beginCPG;
	private final StringProperty endCPG;
	private final IntegerProperty size;
	private final FloatProperty score;
	String url;

	public FXDMRFullSummary(DMRPermutationSummary dmrPermutationSummary, DMRDescription description){
		this.url = description.getLink();
		this.hyperlink = new Button("UCSC");
		this.hyperlink.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				
				DMRLinksForm dmrLinksForm = new DMRLinksForm(url);
				Platform.runLater(dmrLinksForm);
				
				/*if(Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI(url));
					} catch (Exception exception) {
						DMRLinksForm dmrLinksForm = new DMRLinksForm(url);
						Platform.runLater(dmrLinksForm);
					}
				}else {
					DMRLinksForm dmrLinksForm = new DMRLinksForm(url);
					Platform.runLater(dmrLinksForm);
				}*/
			}
		});

		cpgID = new SimpleIntegerProperty(dmrPermutationSummary.getCpgID());
		numberOfIslands = new SimpleIntegerProperty(dmrPermutationSummary.getNumberOfIslands());
		averageOfIslands = new SimpleFloatProperty((float)dmrPermutationSummary.getAverageOfIslands());
		pvalue = new SimpleFloatProperty((float)dmrPermutationSummary.getpValue());
		logRatio = new SimpleFloatProperty((float)dmrPermutationSummary.getLogRatio());

		this.chromosome = new SimpleStringProperty(""+description.getChromosome());
		this.beginPosition = new SimpleIntegerProperty(description.getBeginPosition());
		this.endPosition = new SimpleIntegerProperty(description.getEndPosition());
		this.beginCPG = new SimpleStringProperty(""+description.getBeginCPG());
		this.endCPG = new SimpleStringProperty(""+description.getEndCPG());
		this.score = new SimpleFloatProperty((float)description.getIsland().score);
		this.size  = new SimpleIntegerProperty(description.getSize());
	}

	public Button getHyperlink() {
		return hyperlink;
	}

	public int getNumberOfIslands() {
		return numberOfIslands.get();
	}

	public float getAverageOfIslands() {
		return averageOfIslands.get();
	}

	public float getPvalue() {
		return pvalue.get();
	}

	public float getLogRatio() {
		return logRatio.get();
	}

	public int getCpgID() {
		return cpgID.get();
	}

	public String getChromosome() {
		return chromosome.get();
	}

	public int getBeginPosition() {
		return beginPosition.get();
	}

	public int getEndPosition() {
		return endPosition.get();
	}

	public String getBeginCPG() {
		return beginCPG.get();
	}

	public String getEndCPG() {
		return endCPG.get();
	}

	public int getSize() {
		return size.get();
	}

	public float getScore() {
		return score.get();
	}
	
	public String getURL(){
		return this.url;
	}
}
