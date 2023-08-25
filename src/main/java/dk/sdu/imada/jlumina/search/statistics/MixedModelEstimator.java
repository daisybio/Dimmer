package dk.sdu.imada.jlumina.search.statistics;

import dk.sdu.imada.console.Config;
import dk.sdu.imada.console.Variables;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.StandardCopyOption;
import java.sql.SQLOutput;
import java.util.Random;

/*
 * Performs the mixed Model, y is the array with methylation levels for each patient
 * and x is a matrix of labels (patients X labels).
 */
public class MixedModelEstimator extends StatisticalEstimator{
	public float[] pvalues;
	int target;
	double[][] x;
	Config config;

	String sample_index_file;
	String mm_pvalues_file;
	String formula;
	String annotation_file;
	String beta_path;
	int numThreads;
	int permutationValue;
	int threadValue;
	float mm_variance_cutoff;
	String rscript;

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
	* This function is needed to give the temporary files of the Rscript unique names based on the
	* thread and permutation they belong to.
	* It should only be used for empirical pvalue calculation, not for the original pvalues.
	* */
	public void setPermutationValue(int permutation){
		this.permutationValue = permutation;
		this.sample_index_file = config.getOutputDirectory() + "mm-tmp-in_thread" + this.threadValue + "_permutation" + this.permutationValue + ".csv";
		this.mm_pvalues_file = config.getOutputDirectory() + "mm-pvalues_thread" + this.threadValue + "_permutation" + this.permutationValue + ".csv";
	}
	
	/*
	 * Creates an Instance of the MixedModelEstimator.
	 * @param threadValue is used to manage saving and reading information on different threads.
	 * @param permutationNumber is used to manage saving and reading information from different permutations
	 * @param x matrix of labels (samples X labels)
	 * @param config a Configuration file, with the properties for the mixed Model
	 * @param target index of the target coefficient (based on annotation file) of current Dimmer run; needed to extract correct pvalues from model
	 */
	public MixedModelEstimator(float x[][], int target, int threadValue, String beta_path, Config config) throws IOException {
		this.x = toDouble(x);
		this.config = config;
		this.target = target;
		this.beta_path = beta_path;
		this.threadValue = threadValue;

		this.sample_index_file = config.getOutputDirectory() + "mm-tmp-in_thread" + this.threadValue + ".csv";
		this.mm_pvalues_file = config.getOutputDirectory() + "mm-pvalues_thread" + this.threadValue + ".csv";

		this.formula = ("beta_value ~ " + config.get_mm_formula()).replaceAll("\\s+","");
		this.annotation_file = config.getAnnotationPath();
		this.numThreads = config.getThreads();
		this.mm_variance_cutoff = config.getMMVarianceCutoff();

		this.rscript = this.getRFile(Variables.MIXED_MODEL_SCRIPT);
	}

	/*
	 * Saves order of samples to file; also create temporary file that holds results of Rscript (pvalues)
	 * @param indexes order of samples
	 */
	private void prepareData(int[] indexes) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(this.sample_index_file));

        for (int index : indexes) {
            bw.write(Integer.toString(index));
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
					System.out.println("Couldn't delete temporary sample index file");
					System.exit(1);
				}
			} else {
				System.out.println("sample index file isn't File");
				System.exit(1);
			}

			File file1 = new File(mm_pvalues_file);
			if (file1.isFile()) {
				if(file1.delete()) {
					//System.out.println("OutputFile got deleted");
				} else {
					System.out.println("Couldn't delete temporary pvalue file");
					System.exit(1);
				}
			}else {
				System.out.println("pvalue file isn't File");
				System.exit(1);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Creates sample-index input file for Mixed Model R script and then calls this script
	 * @param y beta-values of CpG; in case of the Mixed Model, this array is filled only with 0s, as the beta-values
	 * will be extracted from the beta-matrix directly in the R script
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
			System.out.println("Rscript " + this.rscript +
					" " + this.beta_path +
					" " + this.sample_index_file +
					" " + this.mm_pvalues_file +
					" " + this.formula +
					" " + this.annotation_file +
					" " + this.mm_variance_cutoff +
					" " + this.numThreads);

			Process p = Runtime.getRuntime().exec(
					"Rscript " + this.rscript +
							" " + this.beta_path +
							" " + this.sample_index_file +
							" " + this.mm_pvalues_file +
							" " + this.formula +
							" " + this.annotation_file +
							" " + this.mm_variance_cutoff +
					        " " + this.numThreads);
			
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

	public String getRFile(String fileName) throws IOException {

		InputStream stream = getClass().getResourceAsStream("/mixed_model.R");

		File tempFile = File.createTempFile("dimmer_R",".R");
		tempFile.deleteOnExit();

		java.nio.file.Files.copy(
				stream,
				tempFile.toPath(),
				StandardCopyOption.REPLACE_EXISTING);
		IOUtils.closeQuietly(stream);

		return(tempFile.toString());
	}
}
