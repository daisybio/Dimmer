package dk.sdu.imada.console;

import java.util.ArrayList;
import java.util.TreeMap;

import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.search.primitives.DMR;
import dk.sdu.imada.jlumina.search.primitives.DMRDescription;
import dk.sdu.imada.jlumina.search.primitives.DMRPermutationSummary;

public class ConsoleMainController {
	
	private Config config;
	
	private boolean infinium;
	private boolean epic;
	
	private float[][] beta;
	private float[][] cellComposition;
	private float[][] phenotype;
	
	private float[] originalPvalues;
	private float[] methylationDifference;
	
	// how often a given p-value is <= to the permuted p-values for one CpG
	private float[] empiricalPvalues;
	// how often a given p-value is <= to all permuted p-values
	private float[] fdrPvalues;
	// similar to fwer p-value
	private float[] stepDownMinPvalues;
	// how the p-values are <= to the best permuted p-value
	private float[] fwerPvalues;
	
	private float[] searchPvalues;
	
	private ArrayList<DMR> dmrs;
	private ArrayList<DMRDescription> dmrDescriptions;
	
	ReadManifest manifest;
	
	private ConsoleInputController inputController;
	private ConsolePermutationController permutationController;
	private DMRConfigScanner dmrConfigScanner;
	private ConsoleDMRFinderController dmrFinderController;
	private ConsoleReadDimmerProject dimmerProject;
	
	private TreeMap<Integer,DMRPermutationSummary> dmrPermutationMap;
	ArrayList<Float> permutedScores;


	
	public ConsoleMainController(Config config){
		this.config = config;
	}
	
	//all start methods in here do not start a new thread but run in sequence
	public void start(){
		
		if(config.load()){
			System.out.println("Loading existing dimmer project "+config.getProjectPath());
			dimmerProject = new ConsoleReadDimmerProject(this,config.getProjectPath());
			dimmerProject.loadPermutationfile();
		}
		else{
			this.inputController = new ConsoleInputController(this.config,this);
			inputController.start();
			
			this.permutationController = new ConsolePermutationController(this.config,this);
			permutationController.start();
		}
	
		if(config.doDMRSearch()){
			
			if(config.getPause()){
				this.dmrConfigScanner = new DMRConfigScanner(this.config,this);
				dmrConfigScanner.start();
			}
			
			this.dmrFinderController = new ConsoleDMRFinderController(this.config,this);
			dmrFinderController.start();
		}
		
		System.out.println("DiMmer finished");
		
	}
	
	//////////// getter/setter /////////////////////////
	
	public boolean isEpic() {
		return epic;
	}

	public boolean isInfinium() {
		return infinium;
	}

	public void setEpic(boolean epic) {
		this.epic = epic;
	}

	public void setInfinium(boolean infinium) {
		this.infinium = infinium;
	}
	
	public void setBeta(float[][] beta) {
		this.beta = beta;
	}
	
	public float[][] getBeta(){
		return this.beta;
	}
	
	public void setManifest(ReadManifest manifest){
		this.manifest = manifest;
	}
	
	public ReadManifest getManifest(){
		return this.manifest;
	}
	
	public void setCellComposition(float[][] cc){
		this.cellComposition = cc;
	}
	
	public float[][] getCellComposition(){
		return this.cellComposition;
	}
	
	public ConsoleInputController getInputController(){
		return this.inputController;
	}
	
	public void setPhenotype(float[][] phenotype){
		this.phenotype = phenotype;
	}
	
	public void setOriginalPvalues(float[] originalPvalues) {
		this.originalPvalues = originalPvalues;
	}
	
	public float[] getOriginalPvalues(){
		return this.originalPvalues;
	}
	
	
	public void setMethylationDifference(float[] methylationDifference) {
		this.methylationDifference = methylationDifference;
	}

	
	public float[] getMethylationDifference(){
		return this.methylationDifference;
	}
	
	public Config getConfig(){
		return this.config;
	}
	

	public void setEmpiricalPvalues(float[] empiricalPvalues){
		this.empiricalPvalues = empiricalPvalues;
	}
	
	public void setFwerPvalues(float[] fwerPvalues){
		this.fwerPvalues = fwerPvalues;
	}
	
	public void setFdrPvalues(float[] fdrPvalues){
		this.fdrPvalues = fdrPvalues;
	}
	
	public void setStepDownMinPvalues(float[] stepDownMinPvalues){
		this.stepDownMinPvalues = stepDownMinPvalues;
	}
	
	public float[] getEmpiricalPvalues(){
		return this.empiricalPvalues;
	}
	
	public float[] getFwerPvalues(){
		return this.fwerPvalues;
	}
	
	public float[] getFdrPvalues(){
		return this.fdrPvalues;
	}
	
	public float[] getStepDownMinPvalues(){
		return this.stepDownMinPvalues;
	}
	
	public ConsolePermutationController getPermutationController(){
		return this.permutationController;
	}
	
	public void setDMRs(ArrayList<DMR> dmrs) {
		this.dmrs = dmrs;
	}

	public ArrayList<DMR> getDMRs() {
		return dmrs;
	}
	
	public void setDmrDescriptions(ArrayList<DMRDescription> dmrDescriptions) {
		this.dmrDescriptions = dmrDescriptions;
	}

	public ArrayList<DMRDescription> getDmrDescriptions() {
		return dmrDescriptions;
	}
	
	public void setDMRPermutationMap(TreeMap<Integer, DMRPermutationSummary> dmrPermutationMap) {
		this.dmrPermutationMap = dmrPermutationMap;
	}

	public TreeMap<Integer, DMRPermutationSummary> getDMRPermutationMap() {
		return dmrPermutationMap;
	}
	
	public void setPermutedScores(ArrayList<Float> permutedScores) {
		this.permutedScores = permutedScores;
	}

	public ArrayList<Float> getPermutedScores() {
		return permutedScores;
	}

	
	public void setSearchPvalues(){
		String p_value_type = config.getPValueType();
		switch(p_value_type){
			case "empirical":
				this.searchPvalues = this.empiricalPvalues;
				break;
			case "original":
				this.searchPvalues = this.originalPvalues;
				break;
			case "FWER":
				this.searchPvalues = this.fwerPvalues;
				break;
			case "FDR":
				this.searchPvalues = this.fdrPvalues;
				break;
			case "minP":
				this.searchPvalues = this.stepDownMinPvalues;
				break;
			default:
				System.out.println(p_value_type + " is no accepted p-value type. Empirical p-values are used.");
				this.searchPvalues = this.empiricalPvalues;
				return;
		}
		System.out.println("Using " + p_value_type + " p-values for the DMR search.");
	}
	
	public void setSearchPvalues(float[] searchPvalues) {
		this.searchPvalues = searchPvalues;
	}

	public float[] getSearchPvalues() {
		if(searchPvalues==null){
			setSearchPvalues();
		}
		return searchPvalues;
	}
	
	//for project loading

	public boolean loadManifest(String[][] data) {

		this.manifest = new ReadManifest();
		return(this.manifest.loadManifest(data));
		
	}

	

}
