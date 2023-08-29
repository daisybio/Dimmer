package dk.sdu.imada.jlumina.search.statistics;

import java.io.*;
import java.nio.file.StandardCopyOption;


import dk.sdu.imada.console.Config;
import dk.sdu.imada.console.Variables;
import org.apache.commons.io.IOUtils;

/*
 * Performs the Timeseries Model (repeated measures ANOVA or Friedman Test as specified) and is based on the code of the
 * MixedModelEstimator class, y is the array with methylation levels for each patient and x is a matrix of labels
 * (patients X labels).
 */
public class TimeSeriesEstimator extends StatisticalEstimator{
    private final int threadValue;
    private final boolean removeTemporaryFiles;
    public float[] pvalues;
    int target;
    double[][] x;
    Config config;

    String method;
    String sample_index_file;
    String ts_pvalues_file;
    String formula;
    String annotation_file;
    String beta_path;
    int numThreads;
    float ts_variance_cutoff;
    String rscript;
    int permutationValue;

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
        this.sample_index_file = config.getOutputDirectory() + "ts-tmp-in_thread" + this.threadValue + "_permutation" + this.permutationValue + ".csv";
        this.ts_pvalues_file = config.getOutputDirectory() + "ts-pvalues_thread" + this.threadValue + "_permutation" + this.permutationValue + ".csv";
    }

    /*
     * Creates an Instance of the TimeSeriesEstimator.
     * @param x matrix of labels (samples X labels)
     * @param target index of the target coefficient (based on annotation file) of current Dimmer run; needed to extract correct pvalues from model
     * @param threadValue is used to manage saving and reading information on different threads.
     * @param beta_path file path to beta matrix is only passed on
     * @param config a Configuration file, with the properties for the time series Model
     * @param permutation boolean that determines if this TimeSeriesEstimator is used for permutations or for the
     *      original p-value calculation. Determines if variance cutoff is used in calculations
     */
    public TimeSeriesEstimator(float x[][], int target, int threadValue, String beta_path, Config config, boolean permutation, boolean removeTemporaryFiles) throws IOException {
        this.x = toDouble(x);
        this.config = config;
        this.target = target;
        this.beta_path = beta_path;
        this.threadValue = threadValue;

        this.sample_index_file = config.getOutputDirectory() + "ts-tmp-in_thread" + threadValue + ".csv";
        this.ts_pvalues_file = config.getOutputDirectory() + "ts-pvalues_thread" + threadValue + ".csv";

        this.method = config.getModel();
        if(config.isRM_ANOVA()) {
            this.ts_variance_cutoff = config.geRMAVarianceCutoff();
            this.formula = ("beta_value ~ " + config.get_rma_formula()).replaceAll("\\s+", "");
        }else if(config.isFriedmanTest()){
            this.ts_variance_cutoff = config.getFTVarianceCutoff();
            this.formula = ("beta_value ~ " + config.get_ft_formula()).replaceAll("\\s+", "");
        }
        // Only use a variance cutoff for the permutations not for the original P-value calculation.
        if(!permutation){
            this.ts_variance_cutoff = (float)0.0;
        }
        this.annotation_file = config.getAnnotationPath();
        this.numThreads = config.getThreads();

        this.rscript = this.getRFile(Variables.MODEL_SCRIPT);
        this.removeTemporaryFiles = removeTemporaryFiles;
    }

    /*
     * Saves order of samples to file
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
            File file = new File(this.sample_index_file);
            if (file.isFile()) {
                if (!file.delete()) {
                    System.out.println("Couldn't delete temporary sample index file");
                    System.exit(1);
                }
            } else {
                System.out.println("sample index file isn't File");
                System.exit(1);
            }

            File file1 = new File(this.ts_pvalues_file);
            if (file1.isFile()) {
                if (!file1.delete()) {
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
     * Creates sample-index input file for TimeSeries Model R script and then calls this script
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
        // start time series model script
        runRCode();

        // read output p values from time series model
        try {
            BufferedReader br = new BufferedReader(new FileReader(ts_pvalues_file));

            int k = 0;
            String line;
            while ((line = br.readLine()) != null) {
                float ts_pval = Float.parseFloat(line);
                if (Double.isNaN(this.pvalue)) {
                    ts_pval = 1.f;
                }
                this.pvalues[k] = ts_pval;
                k++;
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("File \"" + ts_pvalues_file + "\" not found.");
        } catch (IOException e) {
            System.out.println("Error " + e);
        }

        // remove temporary files
        if(this.removeTemporaryFiles){
            removeFiles();
        }else{
            System.out.println("Keeping temporary files");
        }
    }

    /*
     * Starts the Rscript to calculate the time series model and prints possible outputs from the script.
     * In case of an Error its stops and writes an error-message.
     */
    public void runRCode() {
        try {
            Process p = Runtime.getRuntime().exec(
                    "Rscript " + this.rscript +
                            " " + this.beta_path +
                            " " + this.sample_index_file +
                            " " + this.ts_pvalues_file +
                            " " + this.annotation_file +
                            " " + this.formula +
                            " " + this.target +
                            " " + this.ts_variance_cutoff +
                            " " + this.method +
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
                    System.out.println("The sample order file does not exist.");
                    System.exit(-1);
                case 6:
                    System.out.println("The annotation file does not exist.");
                    System.exit(-1);
                case 7:
                    System.out.println("The method is no valid model (neither \"mixedModel\", \"friedmanT\" nor \"rmANOVA\"), it is: " + this.getMethod());
                    System.exit(-1);
                default:
                    System.out.println("Something in the mixed model script went wrong. ExitCode: " + exitCode);
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

    public String getMethod(){
        return this.method;
    }

    public String getRFile(String fileName) throws IOException {

        InputStream stream = getClass().getResourceAsStream(fileName);

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