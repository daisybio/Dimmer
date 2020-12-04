package dk.sdu.imada.jlumina.core.primitives;

import java.util.ArrayList;
import java.util.HashMap;

public class Grouping {
	
	
	// contains the distinct group ids
	private ArrayList<String> groupIDs;
	// points from a group id to the corresponding indices
	private HashMap <String,ArrayList<Integer>> groupSamples;
	// contains the number of samples for each group id, first place belongs to first group id and so on
	private ArrayList<Integer> numSamples;

	private String[] groupColumn;
	
	/**
	 * 
	 * @param groupColumn indices will be grouped by the values in this array
	 */
	public Grouping(String[] groupColumn){
		this.groupColumn = groupColumn;
	}
	
	/**
	 * 
	 * @return returns the grouped indices, use getSplitPoint()/getSplitPoints to get the separation point/points
	 */
	public int[] getIndices(){
		
		int[] indices = new int[groupColumn.length];
		
		//groups if not already done
		if(groupIDs == null){
			group();
		}

		int i = 0;
		for(String key: groupIDs){
			ArrayList<Integer> indexList = groupSamples.get(key);
			for(int index: indexList){
				indices[i++] = index;
			}
		}	

		return indices;
	}
	
	/**
	 * performs the grouping and saves meta information in groupIDs, groupSamples and numSamples
	 */
	public void group(){
		
		groupIDs = new ArrayList<>();
		groupSamples = new HashMap<>(); 
		numSamples = new ArrayList<>();
		int currentIndex = 0;
		
		for(String value : groupColumn){
			ArrayList<Integer> indices = groupSamples.get(value);
			if(indices == null){
				indices = new ArrayList<>();
				indices.add(currentIndex);
				groupSamples.put(value, indices);
				groupIDs.add(value);
			}
			else{
				indices.add(currentIndex);
			}
			currentIndex++;
		}
		
		for(String key: groupIDs){
			ArrayList<Integer> indices = groupSamples.get(key);
			numSamples.add(indices.size());
		}
	}
	
	/**
	 * 
	 * @return the separation point for getIndices() (only if there are 2 groups)
	 */
	public int getSplitPoint(){
		//groups if not already done
		if(groupIDs == null){
			group();
		}
		if(groupIDs.size()!=2){
			System.err.println("In Grouping, getSplitPoint(): There need to be exactly two groups!");
			return 0;
		}
		return numSamples.get(0);
	}
	
	/**
	 * 
	 * @return the separation points or getIndices() (only if there are 2 group or more
	 */
	public int[] getSplitPoints(){
		//groups if not already done
		if(groupIDs == null){
			group();
		}
		if(groupIDs.size()<2){
			System.err.println("In Grouping, getSplitPoint(): There need to be at least two groups!");
			return null;
		}
		
		int[] splitPoints = new int[groupIDs.size()-1];
		int oldPoint = 0;
		
		for(int i = 0; i < numSamples.size()-1;i++){
			splitPoints[i] = oldPoint + numSamples.get(i);
			oldPoint += numSamples.get(i);
		}
		
		return splitPoints;
	}
	
	public String log(){
		StringBuilder builder = new StringBuilder();
		int groupNumber = 1;
		for(String key: groupIDs){
			
			builder.append("Group "+groupNumber+": "+key);
			builder.append("\n");
			builder.append("Samples: ");
			
			boolean first = true;
			for(int index : groupSamples.get(key)){
				if(!first){
					builder.append(", ");
				}
				builder.append((index+1)+"");
				first = false;
			}
			
			builder.append("\n");
			groupNumber++;
		}
		return builder.toString();
	}
	
	/**
	 * 
	 * @return the ungrouped indices (so basically an array with the values 0,...,groupColumn.length-1, used for regression
	 */
	public int[] unGroupedIndices(){
		int[] indices = new int[groupColumn.length];
		for(int i = 0; i < groupColumn.length; i++){
			indices[i] = i;
		}
		return indices;
	}

}
