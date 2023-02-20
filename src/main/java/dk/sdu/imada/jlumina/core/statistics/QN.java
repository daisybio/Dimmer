package dk.sdu.imada.jlumina.core.statistics;

public class QN {

    public static void getQN(double[][] rawData) {
        System.out.println("\nPerforming quantile normalization:");
        //Calculate the average expression, when per sample all raw expression levels have been ordered:
        
        int probeCount = rawData.length;
        int sampleCount = rawData[probeCount-1].length;
        
        double[] rankedMean = new double[probeCount];
        for (int sampleID=0; sampleID<sampleCount; sampleID++) {
            double[] x = new double[probeCount]; 
            
            for (int probeID=0; probeID<probeCount; probeID++) {
                x[probeID] = rawData[probeID][sampleID];
            }
            java.util.Arrays.sort(x);
            for (int probeID=0; probeID<probeCount; probeID++) {
                rankedMean[probeID] += x[probeID];
            }
        }
        for (int probeID=0; probeID<probeCount; probeID++) {
            rankedMean[probeID]/=(double) sampleCount;
        }
        
        RankArray rda = new RankArray();
        //Iterate through each sample:
        for (int s=0; s<sampleCount; s++) {
            double[] probes = new double[probeCount]; for (int p=0; p<probeCount; p++) {
                probes[p]=rawData[p][s];
            }
            double[] probesRanked = rda.rank(probes);
            double[] probesQuantileNormalized = new double[probeCount];
            for (int p=0; p<probeCount; p++) {
                probesQuantileNormalized[p] = rankedMean[(int) probesRanked[p]];
            }
            for (int p=0; p<probeCount; p++) {
                rawData[p][s] = (float) probesQuantileNormalized[p];
            }
        }
    }
}
