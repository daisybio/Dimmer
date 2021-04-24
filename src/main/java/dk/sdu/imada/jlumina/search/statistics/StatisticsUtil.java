package dk.sdu.imada.jlumina.search.statistics;

public class StatisticsUtil {
	
	/**
	 * 
	 * @param pValues a float array containing p-values
	 * @return the fisher test statistic to estimate a combined p-value
	 */
	
	public static float fisherStatistic(float[] pValues){
		float score = 0;
		for(float pval : pValues){
			score += Math.log(pval);
		}
		score *= -2;
		return score;
	}
	
	/**
	 * same as fisherStatistic(float[] pValues) but for a region in the float[]
	 * @param pValues a float array containing p-values
	 * @param start point (inclusive)
	 * @param end point (exclusive)
	 * @return the fisher test statistic to estimate a combined p-value
	 */
	public static float fisherStatistic(float[] pValues, int start, int end){
		float score = 0;
		for(int i = start; i < end; i++){
			score += Math.log(pValues[i]);
		}
		score *= -2;
		return score;
	}
	
	/**
	 * 
	 * @param array a sorted (ascending) array 
	 * @param value 
	 * @return the index of largest smaller value in the array, -1 if there is none
	 */
	
	public static int largestSmallerElementIndex(float[] array, float value)  
    {  
        int start = 0, end = array.length-1;  
        
        int index = -1;  
        while (start <= end) {  
            int mid = (start + end) / 2;  
    
            // Move to the left side if the target is smaller  
            if (array[mid] >= value) {  
                end = mid - 1;  
            }  
    
            // Move right side  
            else {  
                index = mid;  
                start = mid + 1;  
            }  
        }  
        return index;  
    } 
	
	/**
	 * 
	 * @param distribution a sorted array (ascending)
	 * @param value 
	 * @return a right-sided p-value for "value" (likelihood that a value in the distribution is equally big or bigger)
	 */
	public static float pValueFromDistribution(float[] distribution, float value){
		int index = largestSmallerElementIndex(distribution, value);
		return ((float) distribution.length - (index+1) + 1) / (distribution.length + 1); 
	}
	
	
	
	//tests
	public static void main(String[] args){
		float[] arr = {0,0,0,1,1,2,3,4,5,5};
		float target = 5;
		System.out.println(largestSmallerElementIndex(arr,target));
		System.out.println(pValueFromDistribution(arr,target));
	}

}
