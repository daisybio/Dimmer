package dk.sdu.imada.jlumina.search.statistics;

import java.util.Arrays;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

public class HeteroscedasticTestRight extends AbstractTTestEstimator {

	/**
	 * Constructor
	 * @param twoSided
	 * @param splitPoint
	 */
	public HeteroscedasticTestRight(boolean twoSided, int splitPoint) {
		super(twoSided, splitPoint);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see dk.sdu.imada.statistics.AbstractTTestEstimator#compute(double[], double[][])
	 * Compute the t-test assuming unequal variance between the groups
	 */
	@Override
	public float compute(double[] sample1, double[] sample2) {
		
		float m1 = (float)StatUtils.mean(sample1);
		
		float m2 = (float)StatUtils.mean(sample2);
		
		float v1 = (float)StatUtils.variance(sample1,m1);
        
		float v2 = (float)StatUtils.variance(sample2,m2);
    	
		float n1 = sample1.length;
        
		float n2 = sample2.length;
		
		this.tvalue = (float)((m1 - m2) / FastMath.sqrt((v1 / n1) + (v2 / n2)));
		
		double degreesOfFreedom = df(v1, v2, n1, n2);
		
		double tvalue = FastMath.abs(this.tvalue);
		
		TDistribution tDistribution = new TDistribution(degreesOfFreedom);
		
		Float pvalue = ((float)(2.0 * tDistribution.cumulativeProbability(-tvalue)/2.0));

		if (this.tvalue < 0.0) {
			pvalue = (float)(1.0 - pvalue);
		}
		
		if (pvalue.isNaN()) {
			pvalue = 1.f;
		}
		
		this.meanDifference = (m1-m2);
		
		return pvalue;
	}

}
