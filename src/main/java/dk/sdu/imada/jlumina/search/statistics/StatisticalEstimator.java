package dk.sdu.imada.jlumina.search.statistics;

public abstract class StatisticalEstimator {

	/**
	 * Methods which computes p-values, t-statistics and in case of a linear regression the coefficients
	 * @param y is an array with the methylation levels of each patient from a single CpG
	 * @param x is a matrix of labels, for a case x control it's a one column table labeling case and control
	 * @return a CPGResult object containing p-value(s), t-value(s) and Coefficients
	 */
	
	float meanDifference;
	float pvalue;

	public void setPvalue(float pvalue) {
		this.pvalue = pvalue;
	}
	
	public void setMeanDifference(float meanDifference) {
		this.meanDifference = meanDifference;
	}
	
	public float getPvalue() {
		return pvalue;
	}
	
	public float getDiff() {
		return meanDifference;
	}
	
	public double[] toDouble(float[] input) {
		
		double[] output = new double[input.length];
		
		for (int i = 0; i < input.length; i++) {
			output[i] = input[i];
		}
		return output;
	}

	public double[][] toDouble(float[][] input)
	{
		if (input == null)
		{
			return null; // Or throw an exception - your choice
		}
		
		double[][] output = new double[input.length][input[0].length];
		for (int i = 0; i < input.length; i++) {
			for (int j = 0; j < input[0].length; j++) {
				output[i][j] = input[i][j];
			}
		}
		return output;
	}

	public float[] toFloat(double[] input)
	{
		if (input == null)
		{
			return null; // Or throw an exception - your choice
		}
		float[] output = new float[input.length];
		for (int i = 0; i < input.length; i++)
		{
			output[i] = (float)input[i];
		}
		return output;
	}
	
	public double[] getMValue(double[] beta) {
		
		double[] m = new double[beta.length];
		
		int i= 0;
		for (double b : beta) {
			m[i++] = (double) Math.log10((1-b)/b);
		}
		
		return m;
	}
	
	public abstract void setSignificance(double[]y);
}
