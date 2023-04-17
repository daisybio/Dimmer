package dk.sdu.imada.jlumina.search.statistics;

import java.io.*;
import java.util.Properties;
import java.util.Random;

import au.com.bytecode.opencsv.CSVReader;
import dk.sdu.imada.console.Config;

/*
 * Performs the mixed Model, y is the array with methylation levels for each patient
 * and x is a matrix of labels (patients X labels).
 */
public class MixedModelEstimator extends StatisticalEstimator{
	public float[] pvalues;
	float coefficients[];
	double x[][];
	Config config;

	String sample_index_file;
	String mm_pvalues_file;
	String mixedModelCode;
	String formula;
	String annotation_file;
	String beta_matrix_file;
	int threadNumber;
	int numThreads;

	public void setX(float[][] x) {
		this.x = toDouble(x);
	}
	
	public double[][] getX() {
		return x;
	}

	public void setPvalues(float[] pvalues) {
		this.pvalues = pvalues;
	}
	
	/*
	 * Creates an Instance of the MixedModelEstimator.
	 * @param dataLines is used to save all information for the mixed Model in one Table.
	 * @param ThreadNumber is used to manage saving and reading information on different threads.
	 * @param x matrix of labels (samples X labels)
	 * @param config a Configuration file, with the properties for the mixed Model
	 * @param sample_index_file Path to the folder, which should contain the csv file for the mixed Model
	 * @param mm_pvalues_file Path to the output folder, which contains the results of the mixed Model
	 * @param formula is used in the mixed Model.
	 */
	public MixedModelEstimator(float x[][], int target, int threadNumber, Config config) {
		this.x = toDouble(x);
		this.config = config;

		this.sample_index_file = config.getOutputDirectory() + "mm_tmp_in_" + threadNumber + ".csv";
		this.mm_pvalues_file = config.getOutputDirectory() + "mm_pvalues" + threadNumber + ".csv";
		
		this.mixedModelCode = config.get("MixedModelCode");
		this.formula = ("beta_value ~ " + config.get("Formula")).replaceAll("\\s+","");
		this.annotation_file = config.getAnnotationPath();
		this.beta_matrix_file = config.getOutputDirectory() + "beta_matrix.csv";
		this.numThreads = config.getThreads();
	}

	/*
	 * Saves order of samples to temporary file
	 * @param indexes order of samples
	 */
	private void prepareData(int[] indexes) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(this.sample_index_file));

		for (int i = 0; i < indexes.length; i++){
			bw.write(Integer.toString(indexes[i]));
			bw.newLine();
		}
		bw.flush();
		bw.close();

	}
	
	/*
	 * remove Files for the next iteration
	 * Stops if the File couldn't get deleted
	 */
	public void removeFiles() {
		try {
			File file = new File(sample_index_file);
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

			File file1 = new File(mm_pvalues_file);
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
	 * Creates sample-index input file for Mixed Model R script and then calls this script
	 * @param y beta-values of CpG
	 * @param indexes order of samples
	 */
	@Override
	public void setSignificance(double[] y, int[] indexes) {
		try {
			// save sample order to file
			prepareData(indexes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// start mixed model script
		runRCode();

		// read output p values from mixed model
		try {
			BufferedReader br = new BufferedReader(new FileReader(mm_pvalues_file));

			int k = 0;
			String line;
			while ((line = br.readLine()) != null) {
				float mm_pval = Float.parseFloat(line);
				if (Double.isNaN(this.pvalue)) {
					mm_pval = 1.f;
				}
				this.pvalues[k] = mm_pval;
				k++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("File \"" + mm_pvalues_file + "\" not found.");
		} catch (IOException e) {
			System.out.println("Error " + e);
		}

		// remove temporary files
		removeFiles();
	}

	/*
	 * Starts the Rscript to calculate the mixed model and prints possible outputs from the script.
	 * In case of an Error its stops and writes an error-message.
	 */
	public void runRCode() {
		try {
			Process p = Runtime.getRuntime().exec(
					"Rscript " + mixedModelCode +
							" " + beta_matrix_file +
							" " + sample_index_file +
							" " + mm_pvalues_file +
							" " + formula +
							" " + annotation_file +
					        " " + numThreads);
			
			BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
		    while ((line = is.readLine()) != null)
		      System.out.println(line);
		    
			int exitCode = p.waitFor();
			switch (exitCode) {
			case 0:
				break;
			case 2:
				System.out.println("Not enough parameters for the R Script.");
				System.exit(-1);
			case 3:
				System.out.println("Not a valid formula.");
				System.exit(-1);
			case 4:
				System.out.println("The beta matrix file does not exist.");
				System.exit(-1);
			case 5:
				System.out.println("The annotation file does not exist.");
				System.exit(-1);
			case 6:
				System.out.println("The sample order file does not exist.");
				System.exit(-1);
			default:
			System.out.println("Something in the mixed model script went wrong. ExitCode: " + exitCode);
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
}
