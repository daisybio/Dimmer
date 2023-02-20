package dk.sdu.imada.gui.controllers;

import dk.sdu.imada.jlumina.search.primitives.DMRPermutationSummary;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class FXDMRPermutationSummary {

	// result from the search
	private final IntegerProperty cpgID;
	
	private  final IntegerProperty numberOfIslands;

	// average of islands in the permutation
	private  final FloatProperty averageOfIslands;

	// chance of observing in the permutation Islands of at least the same number of CpGs
	private final FloatProperty pvalue;
	
	//fraction of DMRs of a specific size that is expected to be noise 
	private final FloatProperty FDR;

	// 
	private final FloatProperty logRatio;


	public FXDMRPermutationSummary(DMRPermutationSummary dmrPermutationSummary){
		cpgID = new SimpleIntegerProperty(dmrPermutationSummary.getCpgID());
		numberOfIslands = new SimpleIntegerProperty(dmrPermutationSummary.getNumberOfIslands());
		averageOfIslands = new SimpleFloatProperty((float)dmrPermutationSummary.getAverageOfIslands());
		pvalue = new SimpleFloatProperty((float)dmrPermutationSummary.getpValue());
		logRatio = new SimpleFloatProperty((float)dmrPermutationSummary.getLogRatio());
		FDR = new SimpleFloatProperty((float)dmrPermutationSummary.getFDR());
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
	
	public float getFDR(){
		return this.FDR.get();
	}
}
