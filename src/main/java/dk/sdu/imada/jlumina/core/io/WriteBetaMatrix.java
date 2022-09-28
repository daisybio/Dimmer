package dk.sdu.imada.jlumina.core.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import dk.sdu.imada.console.Variables;
import dk.sdu.imada.gui.controllers.ProgressForm;
import dk.sdu.imada.jlumina.core.primitives.CpG;
import dk.sdu.imada.jlumina.core.util.DataProgress;

public class WriteBetaMatrix extends DataProgress implements Runnable {
	
	private String path;
	private CpG[] cpgs;
	private float[][] beta;
	
	private String input_type;
	private String array_type;
	private String[] header;
	private HashMap<String,String[]> columnMap;
	
	
	public WriteBetaMatrix(String path, String[] header, CpG[] cpgs, float[][] beta){
		this.path = path;
		this.cpgs = cpgs;
		this.beta = beta;
		
		this.header = header;
	}
	
	public WriteBetaMatrix(String path, HashMap<String,String[]> columnMap, CpG[] cpgs, float[][] beta, String input_type, String array_type){
		this.path = path;
		this.cpgs = cpgs;
		this.beta = beta;
		
		this.columnMap = columnMap;
		this.input_type = input_type;
		this.array_type = array_type;
	}
	
	public void write(){
		
		this.setMaxSteps(beta.length);
		setMsg("Saving beta-matrix in " + this.path);
		System.out.println("Saving beta-matrix in " + this.path);
		double progress = 0;
		
		if(this.header == null){
			this.header = createHeader();
		} 
		
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File (path +"beta_matrix.csv")));
			
			// write header
			bw.write("CpG_ID");
			for(String sample : this.header){
				bw.write(","+sample);
			}
			bw.write("\n");
			
			//write corpus
			
			for(int i = 0; i < this.cpgs.length; i++){
				
				bw.write(this.cpgs[i].toString());
				
				for(int j = 0; j < this.beta[i].length; j++){
					bw.write(","+beta[i][j]);
				}
				
				bw.write("\n");
				if(i % 10000 == 0){
					this.setProgress(i);
				}
				
			}
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		this.setDone(true);
	}
	
	private String[] createHeader(){
		
		String[] header = null;
		
		if(this.input_type.equals(Variables.IDAT) || Variables.EPIC.equals(array_type) || Variables.INFINIUM.equals(array_type)){
			String[] sentrix_id = columnMap.get(Variables.SENTRIX_ID);
			String[] sentrix_pos = columnMap.get(Variables.SENTRIX_POS);
			
			header = new String[sentrix_id.length];
			
			for(int i = 0; i < header.length; i++){
				header[i] = sentrix_id[i]+"_"+sentrix_pos[i];
			}
		}
		
		//header for bisulfite or custom input
		
		else if(this.input_type.equals(Variables.BISULFITE) || Variables.CUSTOM.equals(array_type)){
			String[] samples = columnMap.get(Variables.BISULFITE_SAMPLE);
			header = new String[samples.length];
			
			for(int i = 0; i < header.length; i++){
				header[i] = samples[i];
			}
		}
		
		return header;
	}
	
	public void run(){
		this.write();
	}
	

}
