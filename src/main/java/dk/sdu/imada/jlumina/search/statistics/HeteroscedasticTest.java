package dk.sdu.imada.jlumina.search.statistics;

import java.util.Arrays;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

public class HeteroscedasticTest extends AbstractTTestEstimator {

	/**
	 * Constructor
	 * @param twoSided
	 * @param splitPoint
	 */
	public HeteroscedasticTest(boolean twoSided, int splitPoint) {
		super(twoSided, splitPoint);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see dk.sdu.imada.statistics.AbstractTTestEstimator#compute(double[], double[][])
	 * Compute the t-test assuming unequal variance between the groups
	 */
	@Override
	public float compute(double [] sample1, double [] sample2) {
		
		float m1 = (float) StatUtils.mean(sample1);
		
		float m2 = (float) StatUtils.mean(sample2);
		
		float v1 = (float) StatUtils.variance(sample1);
        
		float v2 = (float) StatUtils.variance(sample2);
    	
		float n1 = sample1.length;
        
		float n2 = sample2.length;
		
		this.tvalue = (float)((m1 - m2) / FastMath.sqrt((v1 / n1) + (v2 / n2)));
		
		setMeanDifference( m1 - m2);

		double degreesOfFreedom = df(v1, v2, n1, n2);
		
		float tvalue = FastMath.abs(this.tvalue);
		
		TDistribution tDistribution = new TDistribution(degreesOfFreedom);
		
		Float pvalue = (float)(2.0 * tDistribution.cumulativeProbability(-tvalue));
		
		if (pvalue.isNaN()) {
			pvalue = 1.f;
		}
		
		return pvalue;
	}

	@Override
	public void setSignificance(double[] y) {
		double [] sample1 = Arrays.copyOfRange(y, 0, splitPoint);
		double [] sample2 = Arrays.copyOfRange(y, splitPoint, y.length);
		setPvalue(compute(sample1, sample2));
	}
}
