package dk.sdu.imada.jlumina.search.primitives;

import dk.sdu.imada.jlumina.search.statistics.StatisticsUtil;

/**
 * @author diogo
 *	This class represents the DMR refering only the features of the binary vector
 *	for genomic information check the class DMRDescription
 */
public class DMR {

	public int 
		beginPosition,	// position, in the integer binary array which, of the first methylated CpG   
		totalCpgs,	// total CpGs in the Island.
		islandLength; // length in base pairs of the Island
	
	public float 	score, // score totalCpgs/islandLength
					ratio1; // log10(totalCpgs + 0.001)/(permutedAverageCpgs + 0.001)
	
	private float[] cpgPValues; //the p-values of the individual CpGs
	private float pValueScore;
	private float pValue;
	
	public DMR (){
	}
	
	public DMR(int beginPosition, int totalCpgs, float score,
			float ratio1, int islandLength) {
		super();
		this.beginPosition = beginPosition;
		this.totalCpgs = totalCpgs;
		this.score = score;
		this.ratio1 = ratio1;
		this.islandLength = islandLength;
	}
	
	public String log() {
		/*System.out.println(beginPosition + "," + totalCpgs + "," + score + "," + islandLength);*/
		return(beginPosition + "," + totalCpgs + "," + score + "," + islandLength);
	}
	
	/**
	 * sets the cpgPvalues and calculates a cpg score 
	 * @param cpgPValues
	 */
	public void setCpgPValues(float[] cpgPValues){
		this.cpgPValues = cpgPValues;
		this.pValueScore = StatisticsUtil.fisherStatistic(cpgPValues);
	}
	
	public int getTotalCpgs(){
		return this.totalCpgs;
	}
	
	public float getPValueScore(){
		return this.pValueScore;
	}
	
	public void setPValue(float pValue){
		this.pValue = pValue;
	}
	
	public float getPValue(){
		return this.pValue;
	}
}
