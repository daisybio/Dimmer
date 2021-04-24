package dk.sdu.imada.jlumina.search.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

import dk.sdu.imada.jlumina.search.primitives.CompactRegions;
import dk.sdu.imada.jlumina.search.primitives.DMR;
import dk.sdu.imada.jlumina.search.statistics.StatisticsUtil;

public class DMRScoring {
	
	private ArrayList<DMR> dmrs;
	private TreeSet<Integer> sizes;
	private HashMap<Integer, CompactRegions> compactRegionMap;
	private HashMap<Integer, ArrayList<DMR>> dmrSizeMap;
	
	public DMRScoring(ArrayList<DMR> dmrs){
		System.out.println("Calculating p-values for the individual DMRs...");
		this.dmrs = dmrs;
		this.sizes = new TreeSet<>();
		this.dmrSizeMap = new HashMap<>();
		
		//get unique sizes (#CpG) and add dmrs to map (based on their number of CpGs)
		for(DMR dmr : this.dmrs){
			
			int totalCpgs = dmr.getTotalCpgs();
			sizes.add(totalCpgs);
			
			if(!dmrSizeMap.containsKey(totalCpgs)){
				dmrSizeMap.put(totalCpgs, new ArrayList<>());
			}
			dmrSizeMap.get(totalCpgs).add(dmr);
		}	
	}
	
	public void calcPValues(float[] cpgPValues, int[] breakPoints){
		
		//get regions for a score distribution
		initCompactRegionMap(cpgPValues, breakPoints);
		
		//get pvalue for each dmr
		for(int size : sizes){
			
			float[] distribution = compactRegionMap.get(size).getScoreDistribution(100000);
			
			for(DMR dmr : dmrSizeMap.get(size)){
				float pValue = StatisticsUtil.pValueFromDistribution(distribution, dmr.getPValueScore());
				dmr.setPValue(pValue);
				//System.out.println(pValue);
			}
			
		}

	}
	
	/**
	 * 
	 * @param cpgPValues the p values
	 * @param breakPoints containing one and zeros, a 1 means that two adjacent cpgs are too far away from each other
	 */
	
	public void initCompactRegionMap(float[] cpgPValues, int[] breakPoints){
		
		this.compactRegionMap = new HashMap<>();

		//create CompactRegions for each minSize
		for(int size: sizes){
			compactRegionMap.put(size, new CompactRegions(size));
		}
		
		ArrayList<Integer> splitIndices = new ArrayList<>();
		ArrayList<float[]> regions = new ArrayList<>();
		
		//get the split indices
		splitIndices.add(0);
		for(int i = 0; i < breakPoints.length; i++){
			if(breakPoints[i] == 1){
				splitIndices.add(i);
			}
		}
		splitIndices.add(breakPoints.length);
		
		//get the compact regions
		for(int i = 0; i < splitIndices.size() - 1; i++){
			regions.add(Arrays.copyOfRange(cpgPValues, splitIndices.get(i), splitIndices.get(i+1)));
		}
		
		//add regions to their compatible CompactRegions objects
		for(float[] compactRegion : regions){
			for(int size: sizes){
				if(compactRegion.length >= size){
					compactRegionMap.get(size).add(compactRegion);
				}
				else{ break;}
			}
		}
//		System.out.print(1+"\t");
//		System.out.println(regions.size());
//		for(int size : sizes){
//			System.out.print(size+"\t");
//			System.out.println(compactRegionMap.get(size).getRegions().size());
//		} 
	}
	

}
