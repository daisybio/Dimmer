package dk.sdu.imada.jlumina.search.primitives;

public class DMRPermutationSummary {
	
	// result from the search
	int numberOfIslands;
	
	// average of islands in the permutation
	double averageOfIslands;
	
	// chance of observing in the permutation Islands of at least the same number of CpGs
	double pValue;
	
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

	public double getpValue() {
		return pValue;
	}

	public void setpValue(double pValue) {
		this.pValue = pValue;
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
		System.out.println("p-value: " + getpValue());
		System.out.println("Log-ratio: " + getLogRatio());
		System.out.println("Avg dmr: " + getAverageOfIslands());
		System.out.println("ID: " + getCpgID());
	}
	
	public DMRPermutationSummary() {
	}
}
