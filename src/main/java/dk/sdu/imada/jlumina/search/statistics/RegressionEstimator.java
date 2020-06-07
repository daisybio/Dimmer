package dk.sdu.imada.jlumina.search.statistics;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;


public class RegressionEstimator extends StatisticalEstimator {

	/* (non-Javadoc)
	 * @see dk.sdu.imada.statistics.StatisticalEstimator#compute(float[], float[][])
	 * Perform the linear regression, y is the array with methylation levels for each patient
	 * and x is a matrix of labels (patients X labels)
	 */
	float pvalues[];
	float coefficients[];
	double x[][];
	int target;
	OLSMultipleLinearRegression mlr = new OLSMultipleLinearRegression();
	
	public void setX(float[][] x) {
		this.x = toDouble(x);
	}
	
	public double[][] getX() {
		return x;
	}
	
	
	public RegressionEstimator(float x[][], int target) {
		this.x = toDouble(x);
		this.target = target;
	}
	
	@Override
	public void setSignificance(double[] y) {
		
		mlr.newSampleData(y, x);

		float [] parameters = toFloat(mlr.estimateRegressionParameters());

		float [] standardErrror = toFloat(mlr.estimateRegressionParametersStandardErrors());

		float[] pvalues = new float[parameters.length];

		float degreesOfFreedom = x.length - parameters.length;

		TDistribution tDistribution = new TDistribution(degreesOfFreedom);

		for (int i = 0; i < parameters.length; i++) {

			float tvalue = Math.abs(parameters[i]/standardErrror[i]);

			pvalues[i] = (float) (2.0 * tDistribution.cumulativeProbability(-tvalue));
		}

		this.pvalues = pvalues;

		this.coefficients = parameters;
		
		this.pvalue = pvalues[target];
	}
}

