package dk.sdu.imada.jlumina.search.util;

public class SearchUtil {
	
	/**
	 * 
	 * @param p0 an array of p-values
	 * @param diffs an array of mean diffs
	 * @param treshold a max p-value
	 * @param min_diff a min diff value
	 * @return a array with 1 in every position where p0 <= treshold and diff >= min_diff
	 */
	
	public static int[] getBinaryArray(float[] p0, float[] diffs, float threshold, float min_diff) {
		
		int []binaryArray = new int[p0.length];
		
		if(diffs != null){
			for(int i = 0; i < p0.length; i++){
				if(p0[i] <= threshold && Math.abs(diffs[i]) >= min_diff){
					binaryArray[i] = 1;
				}
			}
		}
		
		//case of regression
		else{
			for(int i = 0; i < p0.length; i++){
				if(p0[i] <= threshold){
					binaryArray[i] = 1;
				}
			}
		}

		return binaryArray;
	}
	


}
