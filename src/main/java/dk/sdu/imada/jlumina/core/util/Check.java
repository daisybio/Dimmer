package dk.sdu.imada.jlumina.core.util;

import java.util.ArrayList;

public abstract class Check {
	
	// class that is able to store and return error logs

	ArrayList<String> errors;
	
	/**
	 * 
	 * @return the file errors as a single String
	 */
	public String errorLog(){
		StringBuilder builder = new StringBuilder();
		for(String error: errors){
			builder.append(error);
			builder.append("\n");
		}
		return builder.toString();
	}
	
	
}
