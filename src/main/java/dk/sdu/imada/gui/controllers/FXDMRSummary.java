package dk.sdu.imada.gui.controllers;

import dk.sdu.imada.jlumina.search.primitives.DMRDescription;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FXDMRSummary {
	
	private final StringProperty chromosome;
	//private final StringProperty beginPosition;
	private final IntegerProperty beginPosition;
	//private final StringProperty endPosition;
	private final IntegerProperty endPosition;
	private final StringProperty beginCPG;
	private final StringProperty endCPG;
	//private final StringProperty size;
	private final IntegerProperty size;
	//private final StringProperty score;
	private final FloatProperty score;
	private final FloatProperty pvalue;
	
	public FXDMRSummary(DMRDescription description) {
		this.chromosome = new SimpleStringProperty(""+description.getChromosome());
		this.beginPosition = new SimpleIntegerProperty(description.getBeginPosition());
		this.endPosition = new SimpleIntegerProperty(description.getEndPosition());
		this.beginCPG = new SimpleStringProperty(description.getBeginCPG());
		this.endCPG = new SimpleStringProperty(description.getEndCPG());
		this.score = new SimpleFloatProperty(description.getIsland().score);
		this.size  = new SimpleIntegerProperty(description.getSize());
		this.pvalue = new SimpleFloatProperty(description.getIsland().getPValue());
	}

	public String getChromosome() {
		return chromosome.get();
	}

	public Integer getBeginPosition() {
		return beginPosition.get();
	}

	public Integer getEndPosition() {
		return endPosition.get();
	}

	public String getBeginCPG() {
		return beginCPG.get();
	}

	public String getEndCPG() {
		return endCPG.get();
	}

	public Integer getSize() {
		return size.get();
	}

	public Float getScore() {
		return score.get();
	}
	
	public float getPvalue() {
		return pvalue.get();
	}
}
