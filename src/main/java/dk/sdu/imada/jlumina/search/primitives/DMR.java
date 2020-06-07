package dk.sdu.imada.jlumina.search.primitives;

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
}
