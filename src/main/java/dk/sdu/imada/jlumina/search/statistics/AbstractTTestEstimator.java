package dk.sdu.imada.jlumina.search.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * @author diogo
 * 
 * HeteroscedasticsTest, HomoscedasticTest, PairedTest and StudentTTest inherit from this class
 * 
 */
public abstract class AbstractTTestEstimator extends StatisticalEstimator {
	
	float tvalue;
	boolean twoSided;
	float divide = 2.f;
	int splitPoint;
	double[] sample1;
	double[] sample2;
	
	/**
	 * Constructor this constructor set the division of the p-value in case one-sided. One has to check the 
	 * direction of the t-value in order to see if it's on the right or on the left. 
	 * 
	 * @param twoSided
	 * @param splitPoint
	 */
	
	public AbstractTTestEstimator(boolean twoSided, int splitPoint) {
		
		this.splitPoint = splitPoint;
		
		this.twoSided = twoSided;
		
		if (twoSided) divide = 1.f;
	}
	
	public float getTvalue() {
		return tvalue;
	}
	
	public void setDiff(float diff) {
		this.meanDifference = diff;
	}
	
	/**
	 * Computes approximate degrees of freedom for 2-sample t-test.
	 *
	 * @param v1 first sample variance
	 * @param v2 second sample variance
	 * @param n1 first sample n
	 * @param n2 second sample n
	 * @return approximate degrees of freedom
	 */
	protected double df(double v1, double v2, double n1, double n2) {
		return (((v1 / n1) + (v2 / n2)) * ((v1 / n1) + (v2 / n2))) /
				((v1 * v1) / (n1 * n1 * (n1 - 1d)) + (v2 * v2) /
						(n2 * n2 * (n2 - 1d)));
	}
	
	public void setSplitPoint(int splitPoint) {
		this.splitPoint = splitPoint;
		
	}
	
	public abstract float compute(double[] sample1, double[] sample2);
	
	@Override
	public void setSignificance(double[] y) {
		//double [] sample1 = Arrays.copyOfRange(y, 0, splitPoint);
		//double [] sample2 = Arrays.copyOfRange(y, splitPoint, y.length);
		//sample1 = StatisticsUtil.filterNaN(sample1);
		//sample2 = StatisticsUtil.filterNaN(sample2);
		setSamples(y);
		setPvalue(compute(this.sample1, this.sample2));		
	}
	
	public void setSamples(double[] y){
		
		int counter1 = 0;
		int counter2 = 0;
		
		for(int i = 0; i < y.length; i++){
			if(Double.isNaN(y[i])){
				if(i<splitPoint){
					counter1++;
				}else{
					counter2++;
				}
			}
		}
		
		this.sample1 = new double[splitPoint - counter1];
		this.sample2 = new double[y.length - splitPoint - counter2];
		
		int index = 0;
		for(int i = 0; i < splitPoint; i++){
			if(!Double.isNaN(y[i])){
				sample1[index++] = y[i];
			}
		}
		
		index = 0;
		for(int i = splitPoint; i < y.length; i++){
			if(!Double.isNaN(y[i])){
				sample2[index++] = y[i];
			}
		}
	}
	
	
}
