package dk.sdu.imada.jlumina.core.statistics;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.TiesStrategy;

public class BenjaminiHochberg {
    public static double[] adjustPValues(double[] pValues){

        NaturalRanking ranking = new NaturalRanking(TiesStrategy.SEQUENTIAL);

        double[] pValueRanks = ranking.rank(pValues);
        int[] pValueIndices = new int[pValues.length];

        for(int i = 0; i < pValues.length; i++){
            pValueIndices[pValues.length - (int) pValueRanks[i]] = i;
        }
        double[] pValuesAdjusted = new double[pValues.length];

        double cumulativeMin = 1.0;

        for(int i = 0; i < pValues.length; i++){
            int k = pValueIndices[i];

            pValuesAdjusted[k] = Math.min(1.0, Math.min(cumulativeMin, pValues[k] * (pValues.length / pValueRanks[k])));
            cumulativeMin = Math.min(cumulativeMin, pValuesAdjusted[k]);
        }

        return(pValuesAdjusted);
    }
    
    public static float[] adjustPValues(float[] pValues){
    	
    	double[] tempPValues = new double[pValues.length];
    	for(int i = 0; i < pValues.length; i++){
    		tempPValues[i] = pValues[i];
    	}
        NaturalRanking ranking = new NaturalRanking(TiesStrategy.SEQUENTIAL);

        double[] pValueRanks = ranking.rank(tempPValues);
        int[] pValueIndices = new int[pValues.length];

        for(int i = 0; i < pValues.length; i++){
            pValueIndices[pValues.length - (int) pValueRanks[i]] = i;
        }
        float[] pValuesAdjusted = new float[pValues.length];

        double cumulativeMin = 1.0;

        for(int i = 0; i < pValues.length; i++){
            int k = pValueIndices[i];

            pValuesAdjusted[k] = (float) Math.min(1.0, Math.min(cumulativeMin, pValues[k] * (pValues.length / pValueRanks[k])));
            cumulativeMin = Math.min(cumulativeMin, pValuesAdjusted[k]);
        }

        return(pValuesAdjusted);
    }
}
