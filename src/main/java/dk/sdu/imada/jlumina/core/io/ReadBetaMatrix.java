package dk.sdu.imada.jlumina.core.io;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.jar.Manifest;

import au.com.bytecode.opencsv.CSVReader;
import dk.sdu.imada.console.Util;
import dk.sdu.imada.console.Variables;
import dk.sdu.imada.jlumina.core.primitives.CpG;
import dk.sdu.imada.jlumina.core.util.Check;
import dk.sdu.imada.jlumina.core.util.DataProgress;

public class ReadBetaMatrix extends DataProgress{
	
	
	private String path;
	
	private HashMap<String,float[]> cpgMap;
	private float[][] beta;
	private ReadManifest manifest;
	
	private String[] sampleIds;
	
	private int numSamples;
	
	private ArrayList<String> errors;
	private ArrayList<String> warnings;
	
	public ReadBetaMatrix(String path){
		this.path = path;
		this.errors = new ArrayList<>();
		this.warnings = new ArrayList<>();
	}
	
	/**
	 * reads the beta matrix file, specified in the constructor
	 */
	private void read() throws OutOfMemoryError{
		int counter = 0;
		try{
			
			cpgMap = new HashMap<>(); //a map from cpg_id to corresponding beta values
			
			CSVReader reader =  new CSVReader(new FileReader(path));
			
			String[] line;
			String[] header = reader.readNext();
			
			boolean first = true;
			
			while((line = reader.readNext())!=null){
				
				counter++;
				if(first){
					this.numSamples = line.length - 1;
					first = false;
					mapLine(line);
				}
				else if(line.length == (this.numSamples + 1)){
					mapLine(line);
				}
				else{
					errors.add("Line " + counter + " contains an unexpected number of values");
					cpgMap = null;
					reader.close();
					this.setDone(true);
					return;
				}
				
			}
			reader.close();
		}
		catch(IOException e){
			cpgMap = null;
			errors.add("The beta-matrix file is no correctly formatted .csv file");
			this.setDone(true);
		}
		catch(NumberFormatException e){
			cpgMap = null;
			errors.add("The values in line " + counter + " couldn't be parsed to floats");
			this.setDone(true);
		}
	}
	
	/**
	 * adds a beta-matrix line to the cpg map
	 * @param line
	 * @throws NumberFormatException
	 */
	
	private void mapLine(String[] line) throws NumberFormatException, OutOfMemoryError{
		String id = line[0];
		
		if(cpgMap.get(id)!=null){
			errors.add("Duplicate CpG id: " + id);
			this.setDone(true);
			return;
		}
		
		float[] betaValues = new float[line.length - 1];
		for(int i = 1;  i < line.length; i++){
			betaValues[i-1] = Float.parseFloat(line[i]);
		}
		
		this.cpgMap.put(id, betaValues);
		
	}
	
	/** reads the data and checks files
	 * initializes manifest and beta-matrix
	 * 
	 * @param sentrixIds
	 * @param sentrixPositions
	 * @param chipType -> infinium or epic
	 */
	public void initBetaMatrix(String[] sentrixIds, String[] sentrixPositions, String chipType) throws OutOfMemoryError{
		

			this.setMaxSteps(4);
			setMsg("Checking header...");
			int p = 0;
			setProgress(p++);
			
			System.out.println("Reading beta-matrix input...");
			
			if(!checkHeader(sentrixIds,sentrixPositions)){
				this.setDone(true);
				return;
			}
			setMsg("Reading matrix...");
			setProgress(p++);
			//read data in (if not already the case)
			if(this.cpgMap==null){
				read();
			}
			
			//check for errors so far
			if(this.errors.size()!=0){
				this.setDone(true);
				return;
			}
			
			setMsg("Map positions...");
			setProgress(p++);
			
			//map the sample ids to the position in the sample annotation file
			HashMap<String,Integer> positionMap = new HashMap<>();
			for(int i = 0; i < sentrixIds.length; i++){
				positionMap.put(sentrixIds[i]+"_"+sentrixPositions[i],i);
			}
			
			//used to reorder the values if necessary
			int[] positions = new int[sampleIds.length];
			for(int i = 0; i < sampleIds.length; i++){
				positions[i] = positionMap.get(this.sampleIds[i]);
			}
			
			setMsg("Loading manifest...");
			setProgress(p++);
			//load manifest
			String mf = null;
			String mfProbes = null;
			if (chipType.equals("infinium")) { 
				System.out.println("Using infinium data type");
				mf = Variables.RES_INFINIUM_MANIFEST;
				if (getClass().getClassLoader().getResourceAsStream(mf)==null) {
					mf = Variables.INFINIUM_MANIFEST;
				}
			}else if (chipType.equals("epic")){
				System.out.println("Using epic data type");
				mf = Variables.RES_EPIC_MANIFEST;
				if (getClass().getClassLoader().getResourceAsStream(mf)==null) {
					mf = Variables.EPIC_MANIFEST;
				}
			}
			else{
				errors.add("Array type " + chipType +" is unknown");
				this.setDone(true);
				return;
			}
			ReadManifest manifest = new ReadManifest(mf);
			manifest.loadManifest();
			
			setMsg("Creating beta-matrix...");
			setProgress(p++);
			
			//Lists to store valid CpGs and beta rows from the matrix file
			CpG[] cpgList = manifest.getCpgList();
			ArrayList<CpG> newCpgList = new ArrayList<>();
			ArrayList<float[]> betaList = new ArrayList<>();
			
	
			
			int missing = 0;
			for(CpG cpg : cpgList){
				
				float[] betas = this.cpgMap.get(cpg.getCpgName());
				
				if(betas!=null){
					
					float[] orderedBetas = new float[betas.length];
					for(int i = 0; i < betas.length; i++){
						orderedBetas[positions[i]] = betas[i];
					}
					betaList.add(orderedBetas);
					newCpgList.add(cpg);
				}
				else{
					missing++;
				}
			}
			
			//create beta-matrix
			float[][] beta = new float[betaList.size()][];
			for(int i = 0; i < betaList.size(); i++){
				beta[i] = betaList.get(i);
			}
			
			//check if some ids weren't in the manifest file
			int notInManifest =   missing + cpgMap.keySet().size() - cpgList.length;
			if(notInManifest != 0){
				warnings.add(notInManifest + " CpG ids from the beta-matrix file weren't in the manifest");
			}
			
			if(missing>0){
				warnings.add(missing + " CpG ids from the manifest weren't in the beta-matrix file");
			}
	
			manifest.setCpGList(newCpgList.toArray(new CpG[newCpgList.size()]));
			
			this.beta = beta;
			this.manifest = manifest;
			
			System.gc();
			setProgress(p++);
			this.setDone(true);


	}
	
	public boolean checkHeader(String[] sentrixIds, String[] sentrixPositions) throws OutOfMemoryError{
		
		String[] header = null;
		try{
			CSVReader reader =  new CSVReader(new FileReader(path));
			header = reader.readNext();
			reader.close();
		}
		catch(IOException e){
			errors.add("The beta-matrix file is no correctly formatted .csv file");
		}
		
		HashSet<String> sampleIdSet = new HashSet<>();
		for(int i = 0; i < sentrixIds.length; i++){
			sampleIdSet.add(sentrixIds[i]+"_"+sentrixPositions[i]);
		}
		
		//remove first entry if header is too long
		if(header.length - 1 == sampleIdSet.size()){
			header = Arrays.copyOfRange(header, 1, header.length);
		} 
		
		//now header and samples from the annotation file should have equal numbers
		boolean check = true;
		if(header.length == sampleIdSet.size()){
			
			this.sampleIds = new String[header.length];

			for(int i = 0; i < header.length; i++){
				
				this.sampleIds[i] = header[i];
				if(!sampleIdSet.contains(header[i])){
					errors.add("The column name  \"" + header[i] + "\" isn't specified in the sample annotation file");
					check = false;
				}
			}
		}
		else{
			errors.add("The number of samples specified in the annotation file and the number of sample ids in the matrix file differ");
			return false;
		}
		if(check==false){
			this.sampleIds=null;
		}
		return check;
	}
	
	public float[][] getBeta(){
		return this.beta;
	}
	
	public ReadManifest getManifest(){
		return this.manifest;
	}
	
	//testing
//	public static void main(String[] args){
//		ReadBetaMatrix readBetaMatrix = new ReadBetaMatrix("C:/Users/kerst/Desktop/Dimmer/dimmer_test_data/beta.csv");
//		readBetaMatrix.read();
//	}
	
	public String errorLog(){
		return Util.errorLog(this.errors);
	}
	
	public String warningLog(){
		return Util.errorLog(this.warnings);
	}
	
	public boolean check(){
		return this.errors.size()==0;
	}
	
	public boolean hasWarnings(){
		return this.warnings.size() != 0;
	}
}
