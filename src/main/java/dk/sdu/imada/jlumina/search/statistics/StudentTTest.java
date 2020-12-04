package dk.sdu.imada.jlumina.search.statistics;

/**
 * @author diogo
 * This class is the one which is actually called and is responsible to call the 
 * t-test with several options, such as one.sided, two.sided, eq.variance, paired, etc
 */
public class StudentTTest {

	boolean twoSided;
	
	boolean left;
	
	boolean right;

	boolean paired;

	boolean equalVariance;
	
	int splitPoint;

	/**
	 * Constructor 
	 * @param twoSided one-sided or two sided t-test
	 * @param paired paired or unpaired t-test
	 * @param equalVariance assume or not equal variance
	 * @param splitPoint Position which splits the two groups
	 * 
	 */
	public StudentTTest(boolean twoSided, int splitPoint, boolean left, boolean right, boolean paired, boolean equalVariance) {

		this.twoSided = twoSided;
		
		this.left = left;
		
		this.right = right;

		this.paired =  paired;

		this.equalVariance = equalVariance;
		
		this.splitPoint = splitPoint;

	}
	
	public AbstractTTestEstimator getTTestEstimator() {
		
		if (paired) {
			if (twoSided) {
				return new PairedTest(true, splitPoint);
			}else if (left) {
				return new PairedTestLeft(false, splitPoint);
			}else {
				return new PairedTestRight(false, splitPoint);
			}
		}else {
			if (equalVariance) {
				if (twoSided) {
				return new HomoscedasticTest(true, splitPoint);
				}else if (left) {
					return new HomoscedasticTestLeft(false, splitPoint);	
				}else {
					return new HomoscedasticTestRight(false, splitPoint);
				}
			}else {
				if(twoSided) {
					return new HeteroscedasticTest(true, splitPoint);
				}else if(left) {
					return new HeteroscedasticTestLeft(false, splitPoint);
				}else {
					return new HeteroscedasticTestRight(false, splitPoint);
				}
			}
		}
	}
	
	public String status(){
		if (paired) {
			if (twoSided) {
				return "Performing a paired, two-sided T-Test...";
			}else if (left) {
				return "Performing a paired, left-sided T-Test...";
			}else {
				return "Performing a paired, right-sided T-Test...";
			}
		}else {
			if (equalVariance) {
				if (twoSided) {
					return "Performing two-sided T-Test with assumed equal variance...";
				}else if (left) {
					return "Performing left-sided T-Test with assumed equal variance...";	
				}else {
					return "Performing right-sided T-Test with assumed equal variance...";
				}
			}else {
				if(twoSided) {
					return "Performing two-sided T-Test without assumed equal variance...";
				}else if(left) {
					return "Performing left-sided T-Test without assumed equal variance...";
				}else {
					return "Performing left-sided T-Test without assumed equal variance...";
				}
			}
		}
		
	}
}
