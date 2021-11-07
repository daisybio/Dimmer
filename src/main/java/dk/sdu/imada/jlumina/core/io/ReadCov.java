package dk.sdu.imada.jlumina.core.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ReadCov implements Runnable{
	
	private String path;
	private String sample;
	private HashMap<String,HashMap<Integer,Float>> map; //used for data storage (chr -> positions -> counts)
	private int n_CpGs;
	private int n_removed_CpGs;
	
	private ArrayList<String> errors;
	private ArrayList<String> warnings;
	private int max_errors = 10; //maximum number of errors before quitting the reading process
	private int minCount = 10; //minimum number of reads mapped to each position

	
	
	public ReadCov(String path){
		this.errors = new ArrayList<>();
		this.warnings = new ArrayList<>();
		this.path = path.replace("\\","/");
		this.map = new HashMap<>();
		this.sample = this.path.split("/")[this.path.split("/").length-1];
		this.n_CpGs = 0;
		this.n_removed_CpGs = 0;
	}
	
	public ReadCov(String path, int minCount){
		this(path);
		this.minCount = minCount;
	}
	
	/**
	 * checks format of first line
	 */
	public void quickCheck() {
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			String line = br.readLine();
			String[] splitted = line.split("\t");
			String chr = splitted[0];
			int start = Integer.parseInt(splitted[1]);
			int methylated = Integer.parseInt(splitted[4]);
			int un_methylated = Integer.parseInt(splitted[5]);
			br.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch(IndexOutOfBoundsException e){
			errors.add("Column number error in sample " + sample + " line " + 1);
		}
		catch(NumberFormatException e){
			errors.add("Number format error in sample " + sample + " line " + 1);
		}
	}
	
	/**
	 * read data, data gets stored in a hashmap (chr -> positions -> counts)
	 * counts are stored in an int array -> methylated counts in first, unmethylated counts in second position
	 */
	public void read() throws OutOfMemoryError{
		try{
			
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			String line = null;
			int line_counter = 0;
			
			while((line = br.readLine())!=null){
				
				line_counter++;
				
				try{
					
					String[] splitted = line.split("\t");
					
					String chr = splitted[0];

					int start = Integer.parseInt(splitted[1]);
					int methylated = Integer.parseInt(splitted[4]);
					int un_methylated = Integer.parseInt(splitted[5]);
					
					if((methylated+un_methylated) >= minCount){
						HashMap<Integer,Float> chr_map = map.get(chr);
						if(chr_map == null){
							chr_map = new HashMap<Integer,Float>();
							map.put(chr, chr_map);
						}
						
						Float beta = chr_map.get(start);
						if(beta != null){
							warnings.add("Duplicate position " + chr + ":" + start + " in sample " +this.sample +" was overwritten.");
							n_CpGs--;
						}
						beta = (methylated / (float) (methylated + un_methylated));
						chr_map.put(start,beta);
						n_CpGs++;	
					}
					else{
						n_removed_CpGs++;
					}	
		
				}
				catch(NumberFormatException e){
					errors.add("Number format error while reading sample " + this.sample +  " line " + line_counter);
					if(errors.size()>=max_errors){
						br.close();
						return;
					}
				}
				catch(IndexOutOfBoundsException e){
					errors.add("Column number error while reading sample " + this.sample + " line " + line_counter);
					if(errors.size()>=max_errors){
						br.close();
						return;
					}
				}

				
			}
			br.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}


	}
	
	public HashMap<String,HashMap<Integer,Float>> getMap(){
		return this.map;
	}
	
	public void setMap(HashMap<String,HashMap<Integer,Float>> map){
		this.map = map;
	}
	
	public ArrayList<String> getErrors(){
		return this.errors;
	}
	
	public ArrayList<String> getWarnings(){
		return this.warnings;
	}
	
	public boolean check(){
		return this.errors.size() == 0;
	}
	
	public boolean hasErrors(){
		return this.errors.size()!=0;
	}

	public boolean hasWarnings(){
		return this.warnings.size()!=0;
	}
	
	public int hashCode(){
		return this.path.hashCode();
	}
	
	public String toString(){
		if(n_CpGs != 0 || n_removed_CpGs != 0){
			return this.sample + " Loaded CpGs: " + n_CpGs + " Ignored CpGs: " + n_removed_CpGs;
		}
		else{
			return this.sample;
		}

	}
	
	public void run() throws OutOfMemoryError{
		read();
	}
	

}
