package dk.sdu.imada.jlumina.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PairIDCheck extends Check{
	
	private String[] pairID;
	private String[] variable;
	private boolean hasPairID;
	
	public PairIDCheck(String[] pairID, String[] variable){
		this.pairID = pairID;
		this.errors = new ArrayList<>();
		this.variable = variable;
	}
	
	public boolean check(){
		if(pairID == null){
			errors.add("No column containing the Pair_ID exists");
			return false;
		}
		
		if(variable == null){
			errors.add("No variable of interest selected");
			return false;
		}
		
		if(pairID.length != variable.length){
			errors.add("Pair_ID and the variable of interest differ in length");
		}
		checkPairID();
		
		if(errors.size() == 0){
			checkGrouping();
		}
		
		if(errors.size()==0){
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean hasPairID(){
		if(this.pairID == null){
			return false;
		}
		return true;
	}
	
	public boolean isEven(int length){
		if(length%2==0){
			return true;
		}
		errors.add("Uneven number of samples. No paired data type possible");
		return false;
	}
/**
 * checks if each pair id exists exactly twice
 */
	
	public void checkPairID(){
		HashMap<String,Integer> map = new HashMap<>();
		for(String id : pairID){
			if(!map.containsKey(id)){
				map.put(id, 1);
			}
			else{
				map.put(id, map.get(id)+1);
			}
		}
		for(String id: map.keySet()){
			if(map.get(id)<2){
				errors.add("The PairID " + id +" exists only once");
			}
			if(map.get(id)>2){
				errors.add("The PairID "+ id + " exists more than twice");
			}
		}
	}
	
	/**
	 * checks if the variable of interest groups the pairs
	 * assumes that pair_id and variable of interest are fine on their own
	 */
	public void checkGrouping(){
		HashMap<String,HashSet<String>> map = new HashMap<>();
		int index = 0;
		for(String id : pairID){
			if(!map.containsKey(id)){
				map.put(id, new HashSet());
			}
			map.get(id).add(variable[index++]);
		}
		
		for(String id: map.keySet()){
			if(map.get(id).size()!=2){
				errors.add("The selected variable of interest can't be used to group the pairs properly");
				return;
			}
		}
	}

}
