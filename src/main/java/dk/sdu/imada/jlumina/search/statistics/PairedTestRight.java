package dk.sdu.imada.jlumina.search.statistics;

import java.util.Arrays;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

public class PairedTestRight extends AbstractTTestEstimator {

	/**
	 * Paired t-test constructor
	 * @param twoSided
	 * @param splitPoint
	 */
	public PairedTestRight(boolean twoSided, int splitPoint) {
		super(twoSided, splitPoint);
	}

	/* (non-Javadoc)
	 * @see dk.sdu.imada.statistics.AbstractTTestEstimator#compute(float[], float[][])
	 * 
	 * Computes the t-test assuming paired data.
	 */
	@Override
	public float compute(double[] sample1, double[] sample2) {


		float m = (float)StatUtils.meanDifference((sample1), (sample2));

		float mu = 0.f;

		float v = (float)StatUtils.varianceDifference((sample1), (sample2), m);

		int n = sample1.length;

		this.tvalue = (float)((m - mu) / FastMath.sqrt(v / n));

		float degreesOfFreedom = n - 1;

		TDistribution tDistribution = new TDistribution(degreesOfFreedom);

		float tvalue = (float)FastMath.abs(this.tvalue);

		Float pvalue = ((float)((2.0 * tDistribution.cumulativeProbability(-tvalue)/2.0)));

		if (this.tvalue < 0.0) {
			pvalue = (float)(1.0 - pvalue);
		}

		if (pvalue.isNaN()) {
			pvalue = 1.f;
		}
		this.meanDifference=(m);

		return pvalue;
	}

}
