package dk.sdu.imada.jlumina.search.statistics;

import java.util.Arrays;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

public class HomoscedasticTest extends AbstractTTestEstimator {

	/**
	 * Constructor
	 * @param twoSided
	 * @param splitPoint
	 */
	public HomoscedasticTest(boolean twoSided, int splitPoint) {
		super(twoSided, splitPoint);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see dk.sdu.imada.statistics.AbstractTTestEstimator#compute(double[], double[][])
	 * 
	 * Compute a t-test assuming equal variance between the groups
	 * 
	 */
	@Override
	public float compute(double[] sample1, double[] sample2) {

		float m1 = (float)StatUtils.mean(sample1);

		float m2 = (float)StatUtils.mean(sample2);

		float v1 = (float)StatUtils.variance(sample1,m1);

		float v2 = (float)StatUtils.variance(sample2,m2);

		float n1 = sample1.length;

		float n2 = sample2.length;
		
		//System.out.println(m1 + " " + m2 + " " + v1 + " " + v2+ " " + n1 + " " + n2);

		final float pooledVariance = ((n1  - 1) * v1 + (n2 -1) * v2 ) / (n1 + n2 - 2);

		this.tvalue = (float)((m1 - m2) / FastMath.sqrt(pooledVariance * (1d / n1 + 1d / n2)));
		//System.out.println(pooledVariance);
		//System.out.println(tvalue);

		double degreesOfFreedom = df(v1, v2, n1, n2);
		//System.out.println(degreesOfFreedom);

		double tvalue = FastMath.abs(this.tvalue);

		TDistribution tDistribution = new TDistribution(degreesOfFreedom);

		Float pvalue = ((float)((2.0 * tDistribution.cumulativeProbability(-tvalue))));
		//System.out.println(pvalue);
		
		if (pvalue.isNaN()) {
			pvalue = 1.f;
		}
		
		this.meanDifference = (m1-m2);

		return pvalue;
	}	
	
	/**
	 * for testing
	 * @param args
	 */
	
	public static void main(String[] args){
		HomoscedasticTest test  = new HomoscedasticTest(true,5);
		double[] y = new double[]{0.1,0.2,0.6,Double.NaN,Double.NaN,Double.NaN,0.3,0.4,0.5,0.6};
		test.setSignificance(y);
		PairedTest test_paired  = new PairedTest(true,5);
		test_paired.setSignificance(y);
	}
}
