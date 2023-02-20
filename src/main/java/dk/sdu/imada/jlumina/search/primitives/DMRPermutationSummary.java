package dk.sdu.imada.jlumina.search.primitives;

public class DMRPermutationSummary {
	
	// result from the search
	int numberOfIslands;
	
	// average of islands in the permutation
	double averageOfIslands;
	
	// all the islands in the permutation
	int totalOfIslands;
	
	// chance of observing in the permutation Islands of at least the same number of CpGs
	double pValue;
	
	//fraction of DMRs of a specific size that is expected to be noise 
	double FDR;
	
	// number of times a permutation had more DMRs than numberOfIslands
	int numMoreDMRs;
	
	// 
	double logRatio;
	
	int numberOfIslandsPerPermutation[];
	
	int cpgID;

	public int getCpgID() {
		return cpgID;
	}

	public void setCpgID(int cpgID) {
		this.cpgID = cpgID;
	}

	public int getNumberOfIslands() {
		return numberOfIslands;
	}

	public void setNumberOfIslands(int numberOfIslands) {
		this.numberOfIslands = numberOfIslands;
	}

	public double getAverageOfIslands() {
		return averageOfIslands;
	}
	

	public void setAverageOfIslands(double averageOfIslands) {
		this.averageOfIslands = averageOfIslands;
	}
	
	public int getTotalOfIslands(){
		return this.totalOfIslands;
	}
	
	public void setTotalOfIslands(int totalOfIslands){
		this.totalOfIslands = totalOfIslands;
	}
	
	public int getNumMoreDMRs(){
		return this.numMoreDMRs;
	}
	
	public void setNumMoreDMRs(int numMoreDMRs){
		this.numMoreDMRs = numMoreDMRs;
	}

	public double getpValue() {
		return pValue;
	}

	public void setpValue(double pValue) {
		this.pValue = pValue;
	}
	
	public double getFDR(){
		return this.FDR;
	}
	
	public void setFDR(double FDR){
		this.FDR = FDR;
	}

	public double getLogRatio() {
		return logRatio;
	}

	public void setLogRatio(double logRatio) {
		this.logRatio = logRatio;
	}
	
	public void setNumberOfIslandsPerPermutation(int[] numberOfIslandsPerPermutation) {
		this.numberOfIslandsPerPermutation = numberOfIslandsPerPermutation;
	}
	
	public int[] getNumberOfIslandsPerPermutation() {
		return numberOfIslandsPerPermutation;
	}
	
	public void allocate(int np) {
		this.numberOfIslandsPerPermutation = new int[np];
	}
	
	public void log() {
		System.out.println("Num DMRS: " + getNumberOfIslands());
		System.out.println("FDR: " + getFDR());
		System.out.println("Log-ratio: " + getLogRatio());
		System.out.println("Avg dmr: " + getAverageOfIslands());
		System.out.println("ID: " + getCpgID());
	}
	
	public DMRPermutationSummary() {
	}
	/**
	 * 
	 * @return #CpG, Num DMRs, Avg DMRs, FDR, Log-ratio
	 */
	public String logLine(){
		StringBuilder builder = new StringBuilder();
		builder.append(getCpgID()+"\t");
		builder.append(getNumberOfIslands()+"\t");
		builder.append(((double)Math.round(getAverageOfIslands()* 1000d) / 1000d)+"\t");
		builder.append(((double)Math.round(getFDR()* 1000d) / 1000d)+"\t");
		builder.append(((double)Math.round(getLogRatio()* 1000d) / 1000d));
		return builder.toString();
	}
}
