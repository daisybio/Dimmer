package dk.sdu.imada.jlumina.search.statistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.linear.SingularMatrixException;

import au.com.bytecode.opencsv.CSVReader;
import dk.sdu.imada.console.Config;
import dk.sdu.imada.jlumina.search.algorithms.CpGStatistics;
import dk.sdu.imada.jlumina.search.util.NonPairedShuffle;
import dk.sdu.imada.jlumina.search.util.PairedShuffle;

/*
 * Performs the mixed Model, y is the array with methylation levels for each patient
 * and x is a matrix of labels (patients X labels).
 */
public class MixedModelEstimator extends StatisticalEstimator{
	float pvalues[];
	float coefficients[];
	double x[][];
	int target;
	Config config;
	
	String inputPath;
	String outputPath;
	String mixedModelCode;
	String formula;
	int threadNumber;
	
	List<String[]> dataLines = new ArrayList<>();
	
	public void setX(float[][] x) {
		this.x = toDouble(x);
	}
	
	public double[][] getX() {
		return x;
	}
	
	/*
	 * Creates an Instance of the MixedModelEstimator.
	 * @param dataLines is used to save all information for the mixed Model in one Table.
	 * @param ThreadNumber is used to manage saving and reading information on different threads.
	 * @param x matrix of labels (patients X labels)
	 * @param target 
	 * @param config a Configuration file, with the properties for the mixed Model
	 * @param inputPath Path to the folder, which should contain the csv file for the mixed Model
	 * @param outputPath Path to the output folder, which contains the results of the mixed Model
	 * @param formula is used in the mixed Model.
	 */
	public MixedModelEstimator(float x[][], int target, int threadNumber, Config config) {
		this.x = toDouble(x);
		this.target = target;
		this.config = config;

		this.inputPath = config.getOutputDirectory() + "mm_tmp_in_" + threadNumber + ".csv";
		this.outputPath = config.getOutputDirectory() + "mm_tmp_out_" + threadNumber + ".csv";
		
		this.mixedModelCode = config.get("MixedModelCode");
		this.formula = ("beta_value ~ " + config.get("Formula")).replaceAll("\\s+","");
		
	}
	
	public MixedModelEstimator() {
		
	}
	
	/*
	 * Saves matrix of labels, x, and y in the arrayList with their columnname.
	 * @param methylation levels
	 */
	private void prepareData() {
		dataLines.clear();
		String [] var = new String[config.getConfoundingVariables().size()+1];
		String [] v;
		v = config.getConfoundingVariables().toString().split(", " );
		for (int j = 0; j<v.length;j++) {
			var[j] = v[j].replace("[", "").replace("]", "");
		}
		var[var.length-1] = config.getVariable();
		
		dataLines.add(var);
		
		for (int i = 0; i<this.x.length; i++) {
			String[] tmp = {};
			tmp = new String [this.x[i].length];
			for (int j = 0; j<this.x[i].length;j++) {
				tmp [j] = Double.toString(this.x[i][j]);
			}
			this.dataLines.add(tmp);
		}
		
		/*for (int i = 0; i<dataLines.size();i++) {
			for (int j = 0; j<dataLines.get(i).length;j++) {
				System.out.print(dataLines.get(i)[j] +" ");
			}
			System.out.println();
		}
		System.exit(0);*/
	}
	
	/*
	 * remove Files for the next iteration
	 * Stops if the File couldn't get deleted
	 */
	public void removeFiles() {
		try {
			File file = new File(inputPath);
			if (file.isFile()) {
				if(file.delete()) {
					//System.out.println("InputFile got deleted");
				} else {
					System.out.println("Couldn't delete InputFile");
					System.exit(1);
				}
			} else {
				System.out.println("InputFile isn't File");
				System.exit(1);
			}
		
			File file1 = new File(outputPath);
			if (file1.isFile()) {
				if(file1.delete()) {
					//System.out.println("OutputFile got deleted");
				} else {
					System.out.println("Couldn't delete OutputFile");
					System.exit(1);
				}
			}else {
				System.out.println("OutputFile isn't File");
				System.exit(1);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Reads the parameters and standarderror of the mixed Model and computes them based on the beta values
	 * @param y  methylation levels
	 */
	@Override
	public void setSignificance(double[] y) {
		prepareData();
		givenDataArray_whenConvertToCSV_thenOutputCreated();
		runRCode();
		
		try { //
			CSVReader csvReader = new CSVReader(new FileReader(outputPath));
			String [] line = csvReader.readNext();
			/**
			parameters = new float [line.length/2];
			standardErrror = new float [line.length/2];
			//standardErrror [standardErrror.length] = Float.parseFloat(line[line.length-1]);

			while (line != null) {
				for (int i = 0; i < line.length/2; i++) {
					 parameters [i] = Float.parseFloat(line[i]);
					 standardErrror [i] = Float.parseFloat(line[line.length-i-1]);
				}
				line = csvReader.readNext();
			}**/
			int k = 0;
			while (line != null) {
				this.pvalues[k++] = Float.parseFloat(line[0]);
			}
			this.pvalue = this.pvalues[target];
			csvReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File \"" + outputPath + "\" not Found");
		} catch (IOException e) {
			System.out.println("Fehler " + e);
		}
		
		removeFiles(); 
		/**
		try {

			float[] pvalues = new float[parameters.length];

			float degreesOfFreedom = x.length - parameters.length;

			TDistribution tDistribution = new TDistribution(degreesOfFreedom);

			for (int i = 0; i < parameters.length; i++) {
	
				float tvalue = Math.abs(parameters[i]/standardErrror[i]);
	
				pvalues[i] = (float) (2.0 * tDistribution.cumulativeProbability(-tvalue));
			}
	
			
			this.pvalues = pvalues;
	
			this.coefficients = parameters;
			this.pvalue = pvalues[target];**/
			
		if (Double.isNaN(this.pvalue)) {
			this.pvalue = 1.f;
		}
	}
	
	/*
	 * Convert data to csv File seperated by ","
	 * @param dataLines
	 */
	public String convertToCSV(String[] data) {
	    return Stream.of(data)
	      .map(this::escapeSpecialCharacters)
	      .collect(Collectors.joining(","));
	}
	
	/*
	 * Possibility to escape Data.
	 * Firstly used for testing now, no real purpose.
	 * @parameter dataLines
	 * @return escaped Data
	 */
	public String escapeSpecialCharacters(String data) {
	    String escapedData = data.replaceAll("\\R", " ");
	    if (data.contains("\"") || data.contains(",")) {
	        data = data.replace("\"", "");
	        escapedData = "\"" + data + "\"";
	    }
	    return escapedData;
	}
	
	/* 
	 * Writes for each row in DataLines a row in a csv File for further use.
	 * @param y methylation levels
	 */
	public void givenDataArray_whenConvertToCSV_thenOutputCreated() {
	    File csvOutputFile = new File(inputPath);
	    
	    /*for (int i = 0; i < dataLines.size(); i++) {
	    	String [] tmp = new String[dataLines.get(i).length + 1];
	    	tmp[0] = Double.toString(y[i]);
	    	for (int j = 1; j<dataLines.get(i).length + 1;j++) {
	    		tmp[j] = dataLines.get(i)[j - 1];
	    	}
	    	dataLines.remove(i);
	    	dataLines.add(i, tmp);
	    }*/
	    try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
	        dataLines.stream()
	          .map(this::convertToCSV)
	          .forEach(pw::println);
	    } catch(IOException e) {
	    	System.out.println("Mistake");
	    }
	}
	
	/*
	 * Starts the Rscript and prints possible outputs from the script.
	 * In case of an Error its stops and writes an error-message.
	 */
	public void runRCode() {
		try {
			//System.out.println(mixedModelCode+"\n"+inputPath+"\n"+outputPath+"\n"+formula);
			Process p = Runtime.getRuntime().exec(
					"Rscript " + mixedModelCode + " " + inputPath + " " + outputPath + " "+formula + " " + config.getOutputDirectory()+"/betaFile.csv" + " " + config.getOutputDirectory()+"/indexFile.csv");
			
			BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
		    while ((line = is.readLine()) != null)
		      System.out.println(line);
		    
			int exitCode = p.waitFor();
			switch (exitCode) {
			case 0:
				//System.out.println("Mixed Model finished");
				break;
			case 2:
				System.out.println("Not enough Arguments for the Rscript");
				System.exit(-1);
			case 3:
				System.out.println("Your input File does not Exists");
				System.exit(-1);
			case 4:
				System.out.println("Not a valid formula");
				System.exit(-1);
			default:
				System.out.println("Something in the mixed Model went wrong. ExitCode: " + exitCode);
				System.exit(-1);
			}
			//p.destroy();
		} catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        } catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	/*
	 * Reads the config file
	 * @return prop properties of the config file
	 */
	public Properties loadConfig() {
		Properties prop = new Properties();
		try {
			FileInputStream fis = new FileInputStream("dimmer_console.config");
			prop.load(fis);
		} catch (FileNotFoundException e) {
			System.out.println("File not Found");
		} catch (IOException e) {
			System.out.println(e);
		}
		return prop;
	}
	
	/*
	 * Used for testing, not used anymore.
	 * TODO delete
	 */
	private void testEstimator() {
		Random r = new Random();
		float[][] x = {};
		x = new float [2][5];
		for (int i = 0; i<2; i++) {
			for (int j = 0; j<5; j++) {
				x[i][j] = r.nextFloat();
			}
		}
		double y [] = {1, 2};
		//MixedModelEstimator mm = new MixedModelEstimator(x, 0, 0);
		//mm.givenDataArray_whenConvertToCSV_thenOutputCreated(y);
		//mm.runRCode();
	}

	/*
	 * Used for testing, not used anymore.
	 * TODO delete
	 */
	public static void main (String args []) {
		Random r = new Random();
		float[][] x = {};
		x = new float [2][5];
		for (int i = 0; i<2; i++) {
			for (int j = 0; j<5; j++) {
				x[i][j] = r.nextFloat();
			}
		}
		//MixedModelEstimator mm = new MixedModelEstimator();
		//testEstimator();
		//System.out.println(mm.outputPath);
	}
}
