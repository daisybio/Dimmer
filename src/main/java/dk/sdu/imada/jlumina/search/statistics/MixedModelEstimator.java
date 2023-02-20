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


public class MixedModelEstimator extends StatisticalEstimator{

	/* (non-Javadoc)
	 * @see dk.sdu.imada.statistics.StatisticalEstimator#compute(float[], float[][])
	 * Perform the linear regression, y is the array with methylation levels for each patient
	 * and x is a matrix of labels (patients X labels)
	 */
	float pvalues[];
	float coefficients[];
	double x[][];
	int target;
	Config config;
	
	String inputPath;
	String outputPath;
	String mixedModelCode;
	String formula;
	int runCounter;
	
	List<String[]> dataLines = new ArrayList<>();
	
	public void setX(float[][] x) {
		this.x = toDouble(x);
	}
	
	public double[][] getX() {
		return x;
	}
	
	public MixedModelEstimator(float x[][], int target, int runCounter, Config config) {
		this.x = toDouble(x);
		this.target = target;
		this.config = config;
		
		Properties prob = loadConfig();
		String [] splitInput = prob.getProperty("MixedModelInput").split(".csv");
		this.inputPath = splitInput[0] + runCounter + ".csv";
		String splitOutput [] = prob.getProperty("MixedModelOutput").split(".csv");
		this.outputPath = splitOutput[0] + runCounter + ".csv";
		
		this.mixedModelCode = prob.getProperty("MixedModelCode");
		this.formula = prob.getProperty("Formula");
		
	}
	
	private void prepareData(double[] y) {
		dataLines.clear();
		String [] var = new String[config.getConfoundingVariables().size()+2];
		String [] v;
		v = config.getConfoundingVariables().toString().split(", " );
		for (int j = 0; j<v.length;j++) {
			var[j] = v[j].replace("[", "").replace("]", "");
		}
		var[var.length-2] = config.getVariable();
		var[var.length-1] = "pvalue";
		
		dataLines.add(var);
		
		for (int i = 0; i<this.x.length; i++) {
			String[] tmp = {};
			tmp = new String [this.x[i].length + 1];
			for (int j = 0; j<this.x[i].length;j++) {
				tmp [j] = Double.toString(this.x[i][j]);
			}
			tmp[tmp.length-1] = Double.toString(y[i]);
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
	
	@Override
	public void setSignificance(double[] y) {
		prepareData(y);
		givenDataArray_whenConvertToCSV_thenOutputCreated(y);
		runRCode();
		
		float [] parameters = {};
		float [] standardErrror = {};
		
		try { //
			CSVReader csvReader = new CSVReader(new FileReader(outputPath));
			String [] line = csvReader.readNext();
			
			parameters = new float [line.length/2];
			standardErrror = new float [line.length/2];
			//standardErrror [standardErrror.length] = Float.parseFloat(line[line.length-1]);

			while (line != null) {
				for (int i = 0; i < line.length/2; i++) {
					 parameters [i] = Float.parseFloat(line[i]);
					 standardErrror [i] = Float.parseFloat(line[line.length-i-1]);
				}
				line = csvReader.readNext();
			}
			csvReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File \"" + outputPath + "\" not Found");
		} catch (IOException e) {
			System.out.println("Fehler " + e);
		}
		
		removeFiles(); 
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
			this.pvalue = pvalues[target];
			
			if (Double.isNaN(this.pvalue)) {
				this.pvalue = 1.f;
			}
		} catch(SingularMatrixException e){
			this.pvalue = 1.f;
		}
	}
	
	public String convertToCSV(String[] data) {
	    return Stream.of(data)
	      .map(this::escapeSpecialCharacters)
	      .collect(Collectors.joining(","));
	}
	
	public String escapeSpecialCharacters(String data) {
	    String escapedData = data.replaceAll("\\R", " ");
	    if (data.contains("\"") || data.contains(",")) {
	        data = data.replace("\"", "");
	        escapedData = "\"" + data + "\"";
	    }
	    return escapedData;
	}
	
	/* This Method extends the PhenoMatrix with the result vector at the first position.
	 * Writes the resulting Matrix in a csv File for further use.
	 * @param y the result vector for the pheno matrix
	 */
	public void givenDataArray_whenConvertToCSV_thenOutputCreated(double [] y) {
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
	
	/*Rscript C:/Users/msant/Desktop/Uni/Bachelorarbeit/dimmer/Dimmer-2.1/src/main/java/dk/sdu/imada/mixed_model
/mixed_model.R C:/Users/msant/Desktop/Uni/Bachelorarbeit/dimmer/Dimmer-2.1/CSV_FILE_NAME.csv C:/Users/msant/
Desktop/Uni/Bachelorarbeit/dimmer/Dimmer-2.1/src/main/java/dk/sdu/imada/mixed_model/results.csv "Reaction ~ 
Days + (Days|Subject)"*/
	public void runRCode() {
		try {
			//System.out.println(mixedModelCode+"\n"+inputPath+"\n"+outputPath+"\n"+formula);
			Process p = Runtime.getRuntime().exec(
					"Rscript " + mixedModelCode + " " + inputPath + " " + outputPath + " " + formula);
			
			BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
		    while ((line = is.readLine()) != null)
		      System.out.println(line);
		    
			int exitCode = p.waitFor();
			switch (exitCode) {
			case 0:
				//System.out.println("Mixed Model finished");
				break;
			default:
				System.out.println(exitCode);
				System.out.println("WHY?");
				System.exit(-1);
			}
		} catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        } catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
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

	
	public static void main (String args []) {
		Random r = new Random();
		float[][] x = {};
		x = new float [2][5];
		for (int i = 0; i<2; i++) {
			for (int j = 0; j<5; j++) {
				x[i][j] = r.nextFloat();
			}
		}
		//MixedModelEstimator mm = new MixedModelEstimator(x, 0, 0);
		//testEstimator();
		//System.out.println(mm.outputPath);
	}
}
