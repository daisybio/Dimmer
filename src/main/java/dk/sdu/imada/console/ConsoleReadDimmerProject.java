package dk.sdu.imada.console;

import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;
import dk.sdu.imada.jlumina.core.util.CSVUtil;

public class ConsoleReadDimmerProject {
	
	// fulfills the functionality of ReadDimmerProjectUtil and DimmerProjectMonitor
	
	private ConsoleMainController mainController;
	private String input;
	private boolean done;
	private boolean check;
	
	float original[];
	float emp[];
	float fwer[];
	float fdr[];
	float sdc[];
	float diff[];
	String data [][];
	double progress;
	
	public ConsoleReadDimmerProject(ConsoleMainController mainController, String input) {
		super();
		this.mainController = mainController;
		this.input = input;
		this.done = false;
		check = true;
		progress = 0;
	}
	
	public void loadPermutationfile() {

		this.done = false;
		progress = 0;
		double oldPro = 0;

		try {
			int numRows = CSVUtil.countRows(input, 0) - 1;


			
			//second dimension size seems weird
			data = new String[numRows][3];
			CSVReader reader = new CSVReader(new FileReader(input));

			String [] nextLine = reader.readNext();
			int i = 0;
			boolean has_diff = false;

			if (nextLine[0].equals("CPG")) {
				original = new float[numRows];
				emp = new float[numRows];
				fwer = new float[numRows];
				sdc = new float[numRows];
				fdr = new float[numRows];
				if(nextLine.length>=9){
					diff = new float[numRows];
					has_diff = true;
				}

				while ((nextLine = reader.readNext()) != null) {
					data[i][0] = nextLine[0];
					data[i][1] = nextLine[1];
					data[i][2] = nextLine[2];
					
					original[i] = Float.parseFloat(nextLine[3]);
					emp[i] = Float.parseFloat(nextLine[4]);
					fdr[i] = Float.parseFloat(nextLine[5]);
					fwer[i] = Float.parseFloat(nextLine[6]);
					sdc[i] = Float.parseFloat(nextLine[7]);
					if(has_diff){
						diff[i] = Float.parseFloat(nextLine[8]);
					}
					i++;

					progress = (double) i/(double) numRows;
					if(progress-oldPro >= 0.05){
						System.out.print("\r"+Math.round(progress*100)+"% of the file are loaded...");
						oldPro = progress;
					}
				}
				System.out.println();
				reader.close();

			}else {
				check = false;
				System.out.println("It seems this is not a Dimmer Project File");
			}

		}catch(IOException e) {
			System.out.println("Error io exception");
			System.out.println("The file can't be loaded ");
			check = false;
		}catch(NumberFormatException e) {
			System.out.println("Error number exception");
			System.out.println("The file can't be loaded ");
			check = false;
		} catch (Exception e) {
			System.out.println("other " + e.getMessage());
			System.out.println("The file can't be loaded ");
			e.printStackTrace();
			check = false;
		}
		done = true;
		
		if(check){
			setData();
		}
		else{
			System.exit(1);
		}
	}
	
	public void setData(){
		mainController.setEmpiricalPvalues(this.emp);
		mainController.setOriginalPvalues(this.original);
		mainController.setFwerPvalues(this.fwer);
		mainController.setStepDownMinPvalues(this.sdc);
		mainController.setFdrPvalues(this.fdr);
		mainController.setMethylationDifference(this.diff);
		if(!this.mainController.loadManifest(data)){
			System.exit(1);
		}
	}
	
	



}
