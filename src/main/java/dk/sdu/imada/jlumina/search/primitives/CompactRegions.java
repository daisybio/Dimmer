package dk.sdu.imada.jlumina.search.primitives;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import dk.sdu.imada.jlumina.search.statistics.StatisticsUtil;

public class CompactRegions{
	private int minSize;
	private ArrayList<float[]> regions;
	private int[] regionIndices; 
	private int[] startIndices;
	private Random random;
	
	public CompactRegions(int minSize){
		this.minSize = minSize;
		regions = new ArrayList<>();
		random = new Random();
	}
	
	public void add(float[] region){
		if(region.length<minSize){
			System.out.println("Region is not long (#CgG) enough!");
		}
		else{
			regions.add(region);
		}
	}
	
	public ArrayList<float[]> getRegions(){
		return this.regions;
	}
	
	public float[] getScoreDistribution(int draws){
		float[] distribution = new float[draws];
		initIndices();

		for(int i = 0 ; i < draws; i++){
			distribution[i] = getRandomlyDrawnScore();
		}
	
		this.regionIndices = null;
		this.startIndices = null;
		
		//sort ascending
		Arrays.sort(distribution);
		return distribution;
	}
	
	public float getRandomlyDrawnScore(){
		int index = random.nextInt(regionIndices.length);
		int regionIndex = regionIndices[index];
		int startIndex = startIndices[index];
		float score = StatisticsUtil.fisherStatistic(regions.get(regionIndex), startIndex, startIndex + minSize);
		//System.out.println("Index:\t"+index+"\tRegionIndex:\t"+regionIndex+"\tStart:\t"+startIndex+"\tEnd:\t"+(startIndex + minSize)+"\tScore:\t"+score);
		return score;
	}
	
	public void initIndices(){
		int length = 0;
		for(float[] region : regions){
			length +=  region.length - minSize + 1;
		}
		
		regionIndices = new int[length];
		startIndices = new int[length];
		int index = 0;
		int regionIndex = 0;
		for(float[] region : regions){
			for(int i = 0; i < region.length - minSize + 1; i++){
				regionIndices[index] = regionIndex;
				startIndices[index] = i;
				index++;
			}
			regionIndex++;
		}
	}
}
