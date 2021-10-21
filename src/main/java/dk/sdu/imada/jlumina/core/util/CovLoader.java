package dk.sdu.imada.jlumina.core.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import dk.sdu.imada.console.Util;
import dk.sdu.imada.console.Variables;
import dk.sdu.imada.jlumina.core.io.ReadCov;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.CpG;

public class CovLoader extends DataProgress{
	
	private String path;
	private int numThreads = 1;
	private String[] samples;
	
	private ArrayList<String> samples_path;
	private ArrayList<String> errors;
	private ArrayList<String> warnings;
	private ArrayList<ReadCov> reader_list;
	
	private ReadManifest manifest;
	private float[][] beta;
	
	/**
	 * A class to load .cov file produced by bismark
	 * @param path leading to a sample file
	 */
	public CovLoader(String path){
		this.path = path;
		this.errors = new ArrayList<>();
		this.warnings = new ArrayList<>();
		this.samples_path = new ArrayList<>();
		this.samples = getSamples(path); //extract sample names from the sample file
	}
	
	/**
	 * loads .cov files specified by the constructor path
	 * @param numThreads number of threads used for parallelization
	 * @param minCount minimum reads mapped to a position
	 */
	public void load(int numThreads, int minCount, int missingValues)  throws OutOfMemoryError{
		
		this.numThreads = numThreads;
		quickCheck(); //checks samples, sets samples_path variables
		System.out.println("Reading data...");
		readData(minCount); //reads the data with multiple threads
		addReaderErrorsAndWarnings(); //Add reader errors and warnings to own list
		if(!this.check()){ //stops if there are errors
			return;
		}
		System.out.println("Merging and filtering...");
		mergeData(missingValues); //retains only positions that are featured in all samples, creates manifest file and creates methylated/unmethylated matrices
		System.out.println("Creating beta-matrix...");
		initBeta();
		System.out.println("Succesfully loaded " + this.manifest.getCpgList().length + " CpGs\n");
	}
	
	/**
	 * load function with a default minCount of 10, and 1 missing value
	 * @param numThreads number of threads used for parallelization
	 * @throws OutOfMemoryError
	 */
	
	public void load(int numThreads)  throws OutOfMemoryError{
		this.load(numThreads, 10, 1);
	
	}
	
	/**
	 * loads beta matrix
	 * @param offset optional offset for the beta values
	 */
	
	public void initBeta() throws OutOfMemoryError{
		//create matrices for methylated and unmethylated counts, stored data in the ReadCov objects is deleted after every chromosome
		CpG[] cpg_list = this.manifest.getCpgList();
		this.beta = new float[cpg_list.length][];
		String current_chr = null; //keeps track of current chromosome for data deletion
		
		for(int i = 0; i < cpg_list.length; i++){
			
			CpG cpg = cpg_list[i];
			
			//delete data if chromosome changes
			if(cpg.getChromosome().equals(current_chr)){ 
				for(ReadCov reader: this.reader_list){
					reader.getMap().remove(current_chr);
				}
				current_chr = cpg.getChromosome();
			}
			
			//init new row
			float[] beta_row = new float[reader_list.size()];
			
			//iterate through readers to fill rows
			for(int j = 0; j < reader_list.size(); j++){
				HashMap<Integer,Float> pos_map = reader_list.get(j).getMap().get(cpg.getChromosome());
				if(pos_map!=null){
					Float beta_value = pos_map.getOrDefault(cpg.getMapInfo(),(float)-1);
					if(beta_value>=0){
						beta_row[j] = beta_value;
					}
					else{
						beta_row[j] = Float.NaN;
					}			
				}
				else{
					beta_row[j] = Float.NaN;
				}
			}
			
			//add rows
			this.beta[i] = beta_row;
		}
		
		//delete data maps of the readers
		for(ReadCov reader: this.reader_list){
			reader.setMap(null);
		}
	}
	

	/**
	 * sorts out CpGs, that don't appear in all samples, creates a manifest and raw count matrices
	 */
	private void mergeData()  throws OutOfMemoryError {
		
		//retain only chromosomes that are in all samples
		
		HashSet<String> retained_chrs = null;
		
		for(ReadCov reader: reader_list){
			
			HashMap<String,HashMap<Integer,Float>> reader_map = reader.getMap();
			
			if(retained_chrs == null){
				retained_chrs = new HashSet<>();
				retained_chrs.addAll(reader_map.keySet());
			}
			
			else{
				retained_chrs.retainAll(reader_map.keySet());
			}
		}
			
		//retrain only positions that are in all samples
		HashMap<String,HashSet<Integer>> intersect_map = null;
		
		for(ReadCov reader : reader_list){
			
			HashMap<String,HashMap<Integer,Float>> reader_map = reader.getMap();
			
			if(intersect_map == null){
				intersect_map = new HashMap<>();
				for(String chr: retained_chrs){
					HashSet<Integer> positions = new HashSet<>();
					positions.addAll(reader_map.get(chr).keySet());
					intersect_map.put(chr, positions);
				}
			}
			
			else{
				for(String chr: retained_chrs){
					intersect_map.get(chr).retainAll(reader_map.get(chr).keySet());					
				}
			}
		}
		
		//init manifest 
		this.manifest = new ReadManifest();
		int n_CpGs = 0; //count CpGs to get size of the cpg list
		for(String chr: retained_chrs){
			n_CpGs += intersect_map.get(chr).size();
		}
		CpG[] cpg_list = new CpG[n_CpGs];
		

		int i = 0; 	//fill cpg_list
		for(String chr: retained_chrs){
			ArrayList<Integer> position_list = new ArrayList<>(intersect_map.get(chr));
			intersect_map.remove(chr);
			Collections.sort(position_list); //sorts the positions, in case they aren't sorted in the input files
			for(int position : position_list){
				cpg_list[i] = new CpG(chr,position);
				i++;
			}
		}
		manifest.setCpGList(cpg_list);
		
	}
	
	private void mergeData(int missingValues)  throws OutOfMemoryError {
		
		//get all chromosomes
		
		HashSet<String> all_chrs = new HashSet<>();
		
		for(ReadCov reader: reader_list){
			
			HashMap<String,HashMap<Integer,Float>> reader_map = reader.getMap();
			
			all_chrs.addAll(reader_map.keySet());
		}
			
		//get all positions
		HashMap<String,HashMap<Integer,Short>> full_map = new HashMap<>();
		for(String chr : all_chrs){
			full_map.put(chr, new HashMap<Integer,Short>());
		}
		
		for(ReadCov reader : reader_list){
			
			HashMap<String,HashMap<Integer,Float>> reader_map = reader.getMap();
			
			for(String chr : all_chrs){
				HashMap<Integer,Short> count_map = full_map.get(chr);
				HashMap<Integer,Float> pos_map = reader_map.get(chr);
				
				if(pos_map!=null){
					for(int pos : reader_map.get(chr).keySet()){
						count_map.put(pos, (short) (count_map.getOrDefault(pos, (short) 0) +  1)); //count appearance
					}
				}
			}
		}
		
		//filter
		int min_count = reader_list.size() - missingValues;
		for(String chr: all_chrs){
			HashMap<Integer,Short> count_map = full_map.get(chr);
			count_map.entrySet().removeIf(
                entry -> (entry.getValue()) < min_count);
		}
		
		//init manifest 
		this.manifest = new ReadManifest();
		int n_CpGs = 0; //count CpGs to get size of the cpg list
		for(String chr: all_chrs){
			n_CpGs += full_map.get(chr).size();
		}
		CpG[] cpg_list = new CpG[n_CpGs];
		

		int i = 0; 	//fill cpg_list
		for(String chr: all_chrs){
			ArrayList<Integer> position_list = new ArrayList<>(full_map.get(chr).keySet());
			full_map.remove(chr);
			Collections.sort(position_list); //sorts the positions, in case they aren't sorted in the input files
			for(int position : position_list){
				cpg_list[i] = new CpG(chr,position);
				i++;
			}
		}
		manifest.setCpGList(cpg_list);
		
	}
	/**
	 * reads the data specified in the annotation file (path variable in the constructor) with multiple threads
	 * results get stored in hashmaps (chr -> position -> counts)
	 * @param minCount minimum reads mapped to a position
	 */
	private void readData(int minCount)  throws OutOfMemoryError {
		
		Queue<ReadCov> queue = new ConcurrentLinkedQueue<>(); // need to be read
		LoadingQueue<ReadCov> loaded = new LoadingQueue<>(); // finished reading
		this.reader_list = new ArrayList<>(); //overall reader list
		
		//create reader objects
		for(String path : samples_path){
			ReadCov reader = new ReadCov(path,minCount);
			queue.add(reader);
			reader_list.add(reader);
		}
		
		//create threads and start them
		boolean overflow = false;
		
		
		ArrayList<QueueThread<ReadCov>> threads = new ArrayList<>();
		for(int i = 0; i < numThreads; i++){
			QueueThread<ReadCov> thread = new QueueThread<>(queue,loaded,i, overflow); 
			threads.add(thread);
			thread.start();
		}
		
		//monitor progress
		boolean done = false;
		try{
			while(!done){
				synchronized(loaded){
					loaded.wait();
					if(loaded.size() == reader_list.size()){
						done = true;
					}
					if(loaded.isOverflow()){
						throw new OutOfMemoryError();
					}
				}
			}
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * gets errors and warnings from the readers and adds them to own list
	 */
	private void addReaderErrorsAndWarnings(){
		for(ReadCov reader : reader_list){
			if(reader.hasWarnings()){
				this.warnings.addAll(reader.getWarnings());
			}
			if(reader.hasErrors()){
				this.errors.addAll(reader.getErrors());
			}
		}
	}
	
	/**
	 * extracts sample names from a annotation file
	 * @param path a path leading to a sample annotation file
	 * @return the sample names
	 */
	private String[] getSamples(String path){
		ArrayList<String> samples_list = new ArrayList<>();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			String line = br.readLine();
			String[] header = line.split(",");
			int sample_column = -1;
			for(int i = 0; i < header.length; i++){
				if(header[i].equals(Variables.BISULFITE_SAMPLE)){
					sample_column = i;
					break;
				}
			}
			while((line = br.readLine())!=null){
				samples_list.add(line.split(",")[sample_column].replace("\\","/"));
			}
			br.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		return samples_list.toArray(new String[samples_list.size()]);
		
	}
	
	/**
	 * checks existence, reading access and format correctness of the first line of all input files
	 * sets the actual path for every sample (can be total or in the same directory as the annotation file)
	 * @return true, if test is passed, else false
	 */
	public boolean quickCheck(){
		HashSet<String> path_set = new HashSet<>();
		for(int i = 0; i < samples.length; i++){
			File sample_file = new File(samples[i]);
			if(!sample_file.exists()){
				sample_file = new File(new File(this.path).getParentFile() + "/" + samples[i]);
			}
			if(!sample_file.exists()){
				errors.add("Sample " + samples[i] +" doesn't exist as full path or in the sample sheets directory.");
			}
			else{
				samples_path.add(sample_file.getAbsolutePath());
				if(path_set.contains(sample_file.getAbsolutePath())){
					errors.add("Duplicate sample: " + samples[i]);
				}
				else{
					path_set.add(sample_file.getAbsolutePath());
					if(!sample_file.canRead()){
						errors.add("No reading access: " + samples[i]);
					}
					else{
						ReadCov readCov = new ReadCov(sample_file.getAbsolutePath());
						readCov.quickCheck();
						if(!readCov.check()){
							errors.addAll(readCov.getErrors());
						}
					}
				}
			}
		}
		return errors.size() == 0;
	}
	
	public ArrayList<String> getErrors(){
		return this.errors;
	}
	
	public String errorLog(){
		return Util.errorLog(this.errors);
	}
	
	public boolean hasWarnings(){
		return this.warnings.size()!=0;
	}
	public ArrayList<String> getWarnings(){
		return this.warnings;
	}
	
	public String warningLog(){
		return Util.warningLog(this.warnings);
	}
	
	public boolean check(){
		return this.errors.size() == 0;
	}
	
	public float[][] getBeta(){
		return this.beta;
	}
	
	public ReadManifest getManifest(){
		return this.manifest;
	}
	

}
