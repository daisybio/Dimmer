package dk.sdu.imada.jlumina.core.statistics;
import java.lang.Math;

import org.apache.commons.math3.stat.StatUtils;

public class MethylationValues {
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @return log_b(a)
	 */
	public static double log(double a, double b) {
		return (Math.log(a) / Math.log(b));
	}
	
	/** Converts a matrix of beta values to M-values. Changes are applied in place.
	 * 
	 * @param beta matrix of beta values
	 * @return M=log2(beta/(1-beta))
	 */
	
	public static float[][] betaToM(float[][] beta){
		
		// for values close to 0 or 1
		double offset = 0.000001;
		
		for(int i = 0; i < beta.length; i++) {
			for(int j = 0; j < beta[0].length; j++) {
				
				double beta_value = (double) beta[i][j];
				
				if(beta_value + offset >= 1) {
					beta_value = 1 - offset;
				}
				else if (beta_value - offset <= 0) {
					beta_value =  offset;
				}
				
				beta[i][j] =  (float) log(beta_value / (1 - beta_value), 2);
			}
		}
		
		return beta;
	}

	
	public static void main(String[] args) {
		
		float[][] test = {{0f,0.0001f,0.1f,0.5f},{1f,0.9999f,0.9f,Float.NaN}};
		betaToM(test);
		
		for(int i = 0; i < test.length; i++) {
			for(int j = 0; j < test[0].length; j++) {
				System.out.print(test[i][j]+"\t");
			}
			System.out.println();
		}
		
		double[][] test2 = {{0,0.0001,0.1,0.5},{1,0.9999,0.9,Double.NaN}};
		
		System.out.println(StatUtils.mean(test2[0]));
		System.out.println(StatUtils.mean(test2[1]));
	}
	
	

}


