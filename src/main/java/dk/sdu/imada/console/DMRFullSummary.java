package dk.sdu.imada.console;

import dk.sdu.imada.jlumina.search.primitives.DMRDescription;
import dk.sdu.imada.jlumina.search.primitives.DMRPermutationSummary;


public class DMRFullSummary {
	
	// result from the search
	private final int cpgID;

	private  final int numberOfIslands;

	// average of islands in the permutation
	private  final float averageOfIslands;

	// chance of observing in the permutation Islands of at least the same number of CpGs
	private final float pvalue;

	// 
	private final float logRatio;

	private final String chromosome;
	private final int beginPosition;
	private final int endPosition;
	private final String beginCPG;
	private final String endCPG;
	private final int size;
	private final float score;
	String url;

	public DMRFullSummary(DMRPermutationSummary dmrPermutationSummary, DMRDescription description){
		this.url = description.getLink();
	
		cpgID = dmrPermutationSummary.getCpgID();
		numberOfIslands = dmrPermutationSummary.getNumberOfIslands();
		averageOfIslands = (float)dmrPermutationSummary.getAverageOfIslands();
		pvalue = (float)dmrPermutationSummary.getpValue();
		logRatio = (float)dmrPermutationSummary.getLogRatio();

		this.chromosome = description.getChromosome();
		this.beginPosition = description.getBeginPosition();
		this.endPosition = description.getEndPosition();
		this.beginCPG = description.getBeginCPG();
		this.endCPG = description.getEndCPG();
		this.score = (float)description.getIsland().score;
		this.size  = description.getSize();
	}



	public int getNumberOfIslands() {
		return numberOfIslands;
	}

	public float getAverageOfIslands() {
		return averageOfIslands;
	}

	public float getPvalue() {
		return pvalue;
	}

	public float getLogRatio() {
		return logRatio;
	}

	public int getCpgID() {
		return cpgID;
	}

	public String getChromosome() {
		return chromosome;
	}

	public int getBeginPosition() {
		return beginPosition;
	}

	public int getEndPosition() {
		return endPosition;
	}

	public String getBeginCPG() {
		return beginCPG;
	}

	public String getEndCPG() {
		return endCPG;
	}

	public int getSize() {
		return size;
	}

	public float getScore() {
		return score;
	}
	
	public String getURL(){
		return this.url;
	}

}
