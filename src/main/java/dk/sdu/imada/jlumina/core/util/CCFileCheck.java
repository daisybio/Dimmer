package dk.sdu.imada.jlumina.core.util;

import java.io.File;
import java.util.ArrayList;

public class CCFileCheck {
	
	private String dir_path;
	private String m_path;
	private String u_path;
	ArrayList<String> errors;
	/**
	 * 
	 * @param path to a folder containing the files M.csv and U.csv
	 */
	public CCFileCheck(String path){
		
		if(path.endsWith("/")){
			this.dir_path = path;
		}
		else{
			this.dir_path = path+"/";
		}

		this.m_path = this.dir_path+"M.csv";
		this.u_path = this.dir_path+"U.csv";
		
		errors = new ArrayList<String>();
	}
	
	/**
	 * 
	 * @return true, if the required files exist and are intact 
	 */
	public boolean checkFiles(){
		//files exist
		if(exists(m_path) & exists(u_path)){
			
			//files are readable
			if(readable(m_path) & readable(u_path)){
				
				//files are in correct format
				if(formatCorrect(m_path) & formatCorrect(u_path)){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean exists(String path){
		File f = new File(path);
		
		if(f.exists()){
			return true;
		}
		else{
			errors.add("Required file "+ path + " doesn't exist");
			return false;
		}
	}
	
	public boolean readable(String path){
		File f = new File(path);
		
		if(f.canRead()){
			return true;
		}
		else{
			errors.add("Required file "+ path + " isn't readable");
			return false;
		}
	}
	
	public boolean formatCorrect(String path){
		return true;
	}
	
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
