package dk.sdu.imada.jlumina.search.statistics;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.linear.SingularMatrixException;
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
		
		int counter = 0;
		double[] y_mlr = y;
		double[][] x_mlr = x;
		
		for(int i = 0; i < y.length; i++){
			if(Double.isNaN(y[i])){
				counter++;
			}
		}
		
		/*System.out.println("-----------------------------------------------------");
		for (int i = 0; i<x.length;i++) {
			for (int j = 0;j<x[i].length;j++) {
				System.out.print(x[i][j] + " ");
			}
			System.out.println(y[i]);
		}
		System.out.println("-----------------------------------------------------");*/
		
		// remove samples if their y value is nan
		if(counter > 0){
			
			y_mlr = new double[y.length - counter];
			x_mlr = new double[y.length - counter][];
			
			int index = 0;
			for(int i =  0; i < y.length; i++){
				if(!Double.isNaN(y[i])){
					y_mlr[index] = y[i];
					x_mlr[index] = x[i];
					index++;
				}
			}
		}
		
		mlr.newSampleData(y_mlr, x_mlr);
		try{
			float [] parameters = toFloat(mlr.estimateRegressionParameters());

			float [] standardErrror = toFloat(mlr.estimateRegressionParametersStandardErrors());

			float[] pvalues = new float[parameters.length];

			float degreesOfFreedom = x_mlr.length - parameters.length;

			TDistribution tDistribution = new TDistribution(degreesOfFreedom);

			for (int i = 0; i < parameters.length; i++) {

				float tvalue = Math.abs(parameters[i]/standardErrror[i]);

				pvalues[i] = (float) (2.0 * tDistribution.cumulativeProbability(-tvalue));
			}

			this.pvalues = pvalues;

			this.coefficients = parameters;
			this.pvalue = pvalues[target];
			
			if (Double.isNaN(this.pvalue)) {
				this.pvalue = 1.f;
			}
		}
		catch(SingularMatrixException e){
			this.pvalue = 1.f;
		}


	}
}

