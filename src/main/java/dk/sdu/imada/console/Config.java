 package dk.sdu.imada.console;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Config {
	
	private ArrayList<String> missing_fields; 
	private ArrayList<String> missing_values;
	private HashMap<String,String> malformatted_values;
	private ArrayList<String> messages;
	private HashMap<String,String> parameters;
	
	private boolean check;
	
	private String dimmer_project_path;
	private boolean load;
	
	private String data_type;
	private String model;
	private String alternative_hypothesis;
	private boolean assume_equal_variance;
	private HashSet<String> confounding_variables;
	
	private String input_type;
	private String annotation_path;
	private String output_path;
	
	private String variable;
	private int threads;
	
	private boolean background_correction;
	private boolean probe_filtering;
	private boolean cell_composition;
	private String cell_composition_path;
	private boolean cd8t;
	private boolean cd4t;
	private boolean nk;
	private boolean ncell;
	private boolean mono;
	private boolean gran;
	
	private String beta_path;
	private String array_type;
	
	private int min_reads;
	private int n_min_read_exceptions;
	private float min_variance;
	private int n_permutations_cpg;
	private boolean save_permu_plots;
	private boolean save_beta;
	
	private boolean dmr_search;
	private boolean pause;
	
	private int max_cpg_dist;
	private int n_permutations_dmr;
	private int n_random_regions;
	private int w_size;
	private int n_exceptions;
	private float p_value_cutoff;
	private float min_diff;
	private String p_value_type;
	private boolean save_search_plots;
	private boolean save_search_tables;
	private boolean save_dmr_permu_plots;
	private float mm_variance_cutoff;
	private String mm_formula;
	
	
	public Config(HashMap<String,String> parameters){
		initialize(parameters);
	}
	
	// ##################### initialization ########################################################################
	
	
	/**
	 * initilaizes the Object and checks if key-value pairs from the input HashMap are fine
	 * @param parameters : a HashMap with key-value pairs from the config file
	 */
	
	
	public void initialize(HashMap<String,String> parameters){
		
		this.load = false;
		this.check = true;
		this.missing_fields = new ArrayList<>();
		this.missing_values = new ArrayList<>();
		this.malformatted_values = new HashMap<>();
		this.messages = new ArrayList<>();
		this.parameters = parameters;
		
//		for(String key: parameters.keySet()){
//			System.out.println(key+": "+parameters.get(key));
//		}
		
		// check parameter validity, if something is wrong, this.check will be set to false and program will stop
		System.out.println("\nValid parameters:");
		
			check_project_path();
			check_output_path();
			check_threads();
			
			// variables only needed if no old project to load
			if(this.dimmer_project_path==null){
				
				check_data_type();
				check_model();
				check_variable();
				if(model!=null){
					switch (model) {
						case "Regression":
							check_regression();
							break;
						case "T-test":
							check_t_test();
							break;
						case "mixedModel":
							check_mixed_model_variance();
							check_mixed_model_formula();
							break;
					}
				}
				
				check_annotation_path();
				check_input_type();
				
				if(input_type!=null){

					switch (input_type) {
						case Variables.IDAT:
							check_background_correction();
							check_probe_filtering();
							check_cell_composition();
							break;
						case Variables.BETA:
							check_beta_path();
							check_array_type();
							break;
						case Variables.BISULFITE:
							check_min_reads();
							check_min_read_exceptions();
							check_min_variance();
							break;
					}
				}
				
				check_n_permutations_cpg();
				check_save_permu_plots();
				check_save_beta();
				
			}
			
			else{
				load = true;
			}
	
			
			check_dmr_search();
			if(dmr_search){
				check_pause();
				check_max_cpg_dist();
				check_n_permutations_dmr();
				check_n_random_regions();
				check_w_size();
				check_n_exceptions();
				check_p_value_cutoff();
				if(model == null || model.equals("T-test")) check_min_diff();
				check_p_value_type();
				check_save_search_plots();
				check_save_search_tables();
				check_save_dmr_permu_plots();
			}

			
		
		System.out.println("");
		System.out.println("Ignored parameters:");
		System.out.println(ignored_report()+"\n");
		if(!check){
			System.out.println(error_report());
			System.exit(1);
		}
		else{
			System.out.println("Configuration file is ok...");
		}

	}

	private String check_string(String parameter){
		String value = null;
		String entry = this.parameters.remove(parameter);

		if(entry == null){
			this.check = false;
			this.missing_fields.add(parameter);
		}
		else if(entry.equals("")){
			this.check = false;
			this.missing_values.add(parameter);
		}else{
			value = entry;
		}
		if(value!=null){
			System.out.println(parameter+": "+value);
		}
		return(value);
	}

	/**
	 * checks if a paramter value exists and is contained in an array of possible choices
	 * @param parameter : the paramter name
	 * @param choices : an array of value choices
	 * @return the value, if it is contained in the choices, else null is returned
	 */
	
	private String check_choices(String parameter,String[] choices){
		
		String value = null;
		String entry = this.parameters.remove(parameter);
		HashSet<String> choices_set = new HashSet<>(Arrays.asList(choices));
		
		if(entry == null){
			this.check = false;
			this.missing_fields.add(parameter);
		}
		else if(entry.equals("")){
			this.check = false;
			this.missing_values.add(parameter);
		}
		else if(!choices_set.contains(entry)){
			this.check = false;
			this.malformatted_values.put(parameter,entry);
		}
		else if(entry.length()==1){
			value = choices[Integer.parseInt(entry)-1];
		}
		else{
			value = entry;
		}
		if(value!=null){
			System.out.println(parameter+": "+value);
		}
		return value;
	}
	
	/**
	 *  checks if a parameter value exists and can be parse to boolean
	 * @param parameter : the parameter name
	 * @return "true" or "false", if can be parsed, else null
	 */
	
	private String check_boolean(String parameter){
		String value = null;
		String entry = this.parameters.remove(parameter);
		
		String[] choices = {"false","true","0","1"};
		HashSet<String> choices_set = new HashSet<>(Arrays.asList(choices));
		
		if(entry == null){
			this.check = false;
			this.missing_fields.add(parameter);
		}
		else if(entry.equals("")){
			this.check = false;
			this.missing_values.add(parameter);
		}
		else if(!choices_set.contains(entry)){
			this.check = false;
			this.malformatted_values.put(parameter,entry);
		}
		else if(entry.length()==1){
			value = choices[Integer.parseInt(entry)];
		}
		else{
			value = entry;
		}
		if(value!=null){
			System.out.println(parameter+": "+value);
		}
		return value;
	}
	
	/**
	 *  checks if a parameter value exists and can be parsed to a positive integer
	 * @param parameter : the parameter name
	 * @param non_zero : boolean if the value has to be greater than 0
	 * @return the value, if parsing was possible, else -1
	 */
	
	private int check_positive_integer(String parameter,boolean non_zero){
		
		String entry = this.parameters.remove(parameter);
		int value = -1;
		if(entry == null){
			this.check = false;
			this.missing_fields.add(parameter);
		}
		else if(entry.equals("")){
			this.check = false;
			this.missing_values.add(parameter);
		}else{
			try{
				value = Integer.parseInt(entry);
				if(non_zero){
					if(!(value>0)){
						value=-1;
						this.check = false;
						this.malformatted_values.put(parameter,entry);
					}
				}else{
					if(!(value>=0)){
						value=-1;
						this.check = false;
						this.malformatted_values.put(parameter,entry);
					}
				}

			}
			catch(Exception e){
				this.check = false;
				this.malformatted_values.put(parameter,entry);
			}
		}
		if(value!=-1){
			System.out.println(parameter+": "+value);
		}
		return value;
	}
	/**
	 * @param parameter : the parameter name
	 * @param lower : lower bound
	 * @param upper : upper bound
	 * @param inclusive : whether bounds are inclusive or not
	 * @return a float between lower and upper, if parameter can be parsed, else -1 (a negative value will always return -1, regardless of the choice of the lower bound)
	 */
	
	private float check_positive_float(String parameter, double lower, double upper, boolean inclusive){
		
		String entry = this.parameters.remove(parameter);
		float value = -1;
		if(entry == null){
			this.check = false;
			this.missing_fields.add(parameter);
		}
		else if(entry.equals("")){
			this.check = false;
			this.missing_values.add(parameter);
		}
		else{
			entry = entry.replace(',', '.');
			try{
				value = Float.parseFloat(entry);
				if(inclusive){
					if(value>upper||value<lower||value<0){
						this.check = false;
						this.malformatted_values.put(parameter,entry);
						value = -1;
					}
				}
				else{
					if(value>=upper||value<=lower||value<0){
						this.check = false;
						this.malformatted_values.put(parameter,entry);
						value = -1;
					}	
				}

			}
			catch(Exception e){
				this.check = false;
				this.malformatted_values.put(parameter,entry);
				value = -1;
			}
		}
		if(value != -1){
			System.out.println(parameter+": "+value);
		}
		return value;
	}
	
	/**
	 * checks if a parameter exists and is a valid path 
	 * @param parameter : the parameter name
	 * @param directory : if true: path needs to point to a directory, else to a file
	 * @param read : true if path has to be readable
	 * @param write : true if path has to be writable
	 * @param execute : true if path has to be executable
	 * @return the path if it is valid, else null
	 */
	
	private String check_path(String parameter,boolean directory, boolean read, boolean write, boolean execute){
		
		String entry = this.parameters.remove(parameter);
		String value = null;
		boolean path_ok = true;
		
		if(entry == null){
			this.check = false;
			this.missing_fields.add(parameter);
			path_ok = false;
		}
		else if(entry.equals("")){
			this.check = false;
			this.missing_values.add(parameter);
			path_ok = false;
		}
		else{
			String orig_entry = entry;
			entry = entry.replace("\\", "/");
			
		    File file = new File(entry);
		    //check if file exists
		    if(!file.exists()){
		    	this.check = false;
			    path_ok = false;
		    	this.messages.add("Path "+orig_entry+" does not exist");
		    }
		    else{
			    //check if file is directory of file
			    if(directory){
			    	if(!file.isDirectory()){
				    	this.check = false;
				    	this.messages.add("Path "+orig_entry+" needs to point to a directory");
				    	path_ok = false;
			    	}
			    }
			    else{
			    	if(!file.isFile()){
				    	this.check = false;
				    	this.messages.add("Path "+orig_entry+" needs to point to a file");
				    	path_ok = false;
			    	}
			    }
			    
			    //check if file is writable
			    if(write && !Files.isWritable(file.toPath())) {
				    this.check = false;
				    this.messages.add("Path "+orig_entry+" has no writing permission");
				    path_ok = false;
			    } 
			    //check if file is readable
			    if(read && !Files.isReadable(file.toPath())) {
				    this.check = false;
				    this.messages.add("Path "+orig_entry+" has no reading permission");
				    path_ok = false;
			    }
			    //check if file is executable
			    if(execute && !Files.isExecutable(file.toPath())) {
				    this.check = false;
				    this.messages.add("Path "+orig_entry+" has no execution permission");
				    path_ok = false;
			    } 
			    // checks if path had any flaws
			    if(path_ok){
			    	value = entry;
			    	System.out.println(parameter+": "+value);
			    }
		    }

		}
		
		return value;
	}
	
	private void check_data_type(){
		String parameter = "data_type";
		String[] choices = {"unpaired","paired","1","2"};
		String value = check_choices(parameter,choices);
		this.data_type = value;
	}
	
	/**
	 * Added Mixed Model option
	 */
	private void check_model(){
		String parameter = "model";
		String[] choices = {"Regression","T-test","1","2", "mixedModel", "3"};
		String value = check_choices(parameter,choices);
		this.model = value;
	}
	
	private void check_input_type(){
		String parameter = "input_type";
		String[] choices = {Variables.IDAT,Variables.BETA,Variables.BISULFITE,"1","2","3"};
		String value = check_choices(parameter,choices);
		this.input_type = value;
	}
	
	private void check_array_type(){
		String parameter = "array_type";
		String[] choices = {Variables.INFINIUM,Variables.EPIC,Variables.CUSTOM,"1","2","3"};
		String value = check_choices(parameter,choices);
		this.array_type = value;
	}
	
	private void check_regression(){
		String parameter = "confounding_variables";
		HashSet<String> value = null;
		String entry = this.parameters.remove(parameter);
		
		if(this.isPaired()){
			this.check = false;
			this.messages.add("Regression doesn't work with the paired data type. Please choose T-Test or change to unpaired");
		}
		if(entry == null){
			this.check = false;
			this.missing_fields.add(parameter);
		}
		else{
			value = new HashSet<>();
			String[] splitted = entry.split(", ");
			StringBuilder builder = new StringBuilder();
			builder.append(parameter+": ");
			boolean first = true;
			if(!entry.equals("")){
				for(String split: splitted){
					if(first){
						first = false;
					}
					else{
						builder.append(", ");
					}
					builder.append("\""+split.trim()+"\"");
					if(split.trim().equals(this.variable)){
						this.messages.add("The variable of interest can't be one ouf the confounding variables");
						this.check = false;
					}
					value.add(split.trim());
				}
			}
			System.out.println(builder.toString());
		}
		
		this.confounding_variables = value;
	}
	
	private void check_t_test(){
		String parameter = "alternative_hypothesis";
		String[] choices = {"left","right","both","1","2","3"};
		String value = check_choices(parameter,choices);
		this.alternative_hypothesis = value;
		
		parameter = "assume_equal_variance";
		value = check_boolean(parameter);
		if(value!=null){
			this.assume_equal_variance = Boolean.parseBoolean(value);
		}
	}

	private void check_mixed_model_variance(){
		String parameter = "mm_variance_cutoff";
		this.mm_variance_cutoff = check_positive_float(parameter,0, 1, true);
	}

	private void check_mixed_model_formula(){
		String parameter = "mm_formula";
		this.mm_formula = check_string(parameter);
	}
	
	private void check_variable(){
		String value = null;
		String parameter = "variable";
		String entry = this.parameters.remove(parameter);
		if(entry == null){
			this.check = false;
			this.missing_fields.add(parameter);
		}
		else if(entry.equals("")){
			this.check = false;
			this.missing_values.add(parameter);
		}
		else{
			value = entry;
			System.out.println(parameter+": "+value);
		}
		this.variable = value;
	}
	
	private void check_threads(){
		int value = 0;
		String parameter = "threads";
		value = check_positive_integer(parameter,true);
		this.threads = value;
	}
	
	private void check_background_correction(){
		String parameter = "background_correction";
		String value = check_boolean(parameter);
		if(value!=null){
			this.background_correction = Boolean.parseBoolean(value);
		}
	}
	
	private void check_probe_filtering(){
		String parameter = "probe_filtering";
		String value = check_boolean(parameter);
		if(value!=null){
			this.probe_filtering = Boolean.parseBoolean(value);
		}
	}
	
	private void check_cell_composition(){
		String parameter = "cell_composition";
		String value = check_boolean(parameter);
		if(value!=null){
			this.cell_composition = Boolean.parseBoolean(value);
			if(this.cell_composition){
				parameter = "cd8t";
				value = check_boolean(parameter);
				if(value!= null){
					this.cd8t = Boolean.parseBoolean(value);
				}
				
				parameter =	"cd4t";
				value = check_boolean(parameter);
				if(value!= null){
					this.cd4t = Boolean.parseBoolean(value);
				}
				
				parameter = "nk";
				value = check_boolean(parameter);
				if(value!= null){
					this.nk = Boolean.parseBoolean(value);
				}
				
				parameter = "ncell";
				value = check_boolean(parameter);
				if(value!= null){
					this.ncell = Boolean.parseBoolean(value);
				}
				
				parameter = "mono";
				value = check_boolean(parameter);
				if(value!= null){
					this.mono = Boolean.parseBoolean(value);
				}
				
				parameter = "gran";
				value = check_boolean(parameter);
				if(value!= null){
					this.gran = Boolean.parseBoolean(value);
				}
				
				parameter = "cell_composition_path";
				// path needs to be a directory and readable
				value = check_path(parameter,true,true,false,false);
				this.cell_composition_path = value;
				
				if(this.model!=null && !this.model.equals("Regression")){
					this.check=false;
					messages.add("Cell composition can only be selected with model type \"Regression\"");
				}
			
			}
		}
	}
	
	private void check_min_reads(){
		int value = 0;
		String parameter = "min_reads";
		value = check_positive_integer(parameter,false);
		if(value>=0){
			this.min_reads = value;	
		}
	}
	
	private void check_min_read_exceptions(){
		int value = 0;
		String parameter = "n_min_read_exceptions";
		value = check_positive_integer(parameter,false);
		if(value>=0){
			this.n_min_read_exceptions = value;	
		}
	}
	
	private void check_min_variance(){
		float value = -1;
		String parameter = "min_variance";
		value = check_positive_float(parameter,0,1,true);
		if(value!=-1){
			this.min_variance = value;
		}
	}
	
	
	
	private void check_n_permutations_cpg(){
		int value = 0;
		String parameter = "n_permutations_cpg";
		value = check_positive_integer(parameter,true);
		this.n_permutations_cpg = value;
	}
	
	private void check_save_permu_plots(){
		String parameter = "save_permu_plots";
		String value = check_boolean(parameter);
		if(value!=null){
			this.save_permu_plots = Boolean.parseBoolean(value);
		}
	}
	
	private void check_save_beta(){
		String parameter = "save_beta";
		String value = check_boolean(parameter);
		if(value!=null){
			this.save_beta = Boolean.parseBoolean(value);
		}
	}
	
	private void check_dmr_search(){
		String parameter = "dmr_search";
		String value = check_boolean(parameter);
		if(value!=null){
			this.dmr_search = Boolean.parseBoolean(value);
		}
	}
	
	private void check_pause(){
		String parameter = "pause";
		String value = check_boolean(parameter);
		if(value!=null){
			this.pause = Boolean.parseBoolean(value);
		}
	}
	
	public void check_max_cpg_dist(){
		int value = 0;
		String parameter = "max_cpg_dist";
		value = check_positive_integer(parameter,true);
		if(value>=1){
			this.max_cpg_dist = value;
		}
	}
	
	public void check_n_permutations_dmr(){
		int value = 0;
		String parameter = "n_permutations_dmr";
		value = check_positive_integer(parameter,true);
		if(value>=1){
			this.n_permutations_dmr = value;
		}
	}
	
	public void check_n_random_regions(){
		int value = 0;
		String parameter = "n_random_regions";
		value = check_positive_integer(parameter,true);
		if(value>=1){
			this.n_random_regions = value;
		}
	}
	
	public void check_w_size(){
		int value = 0;
		String parameter = "w_size";
		value = check_positive_integer(parameter,true);
		if(value>=1){
			this.w_size = value;
		}
	}
	
	public void check_n_exceptions(){
		int value = 0;
		String parameter = "n_exceptions";
		value = check_positive_integer(parameter,false);
		if(value>=0){
			this.n_exceptions = value;	
		}
	}
	
	public void check_p_value_cutoff(){
		float value = -1;
		String parameter = "p_value_cutoff";
		value = check_positive_float(parameter,0,1,true);
		if(value!=-1){
			this.p_value_cutoff = value;
		}
	}
	
	public void check_min_diff(){
		float value = -1;
		String parameter = "min_diff";
		value = check_positive_float(parameter,0,1,true);
		if(value!=-1){
			this.min_diff = value;
		}
	}
	
	public void check_p_value_type(){
		String parameter = "p_value_type";
		String[] choices = {"empirical", "original", "FWER", "FDR", "minP","1","2","3","4","5"};
		String value = check_choices(parameter,choices);
		if(value!=null){
			this.p_value_type = value;
		}
	}
	
	public void check_save_search_plots(){
		String parameter = "save_search_plots";
		String value = check_boolean(parameter);
		if(value!=null){
			this.save_search_plots = Boolean.parseBoolean(value);
		}
	}
	
	public void check_save_search_tables(){
		String parameter = "save_search_tables";
		String value = check_boolean(parameter);
		if(value!=null){
			this.save_search_tables = Boolean.parseBoolean(value);
		}
	}
	
	public void check_save_dmr_permu_plots(){
		String parameter = "save_dmr_permu_plots";
		String value = check_boolean(parameter);
		if(value!=null){
			this.save_dmr_permu_plots = Boolean.parseBoolean(value);
		}
	}
	
	private void check_annotation_path(){
		String value = null;
		String parameter = "annotation_path";
		// path needs to be a readable file
		value = check_path(parameter,false,true,false,false);
		this.annotation_path = value;
	}
	
	private void check_output_path(){
		String value = null;
		String parameter = "output_path";
		// path needs to be a directory and writable
		value = check_path(parameter,true,false,true,false);
		this.output_path = value;
	}
	
	//parameter is optional -> value doesn't have to exist
	private void check_beta_path(){
		
		String value = null;
		String parameter = "beta_path";

		value = check_path(parameter,false,true,false,false);
		
		this.parameters.remove(parameter);
		this.beta_path = value;
	}
	
	//parameter is optional -> value doesn't have to exist
	private void check_project_path(){
		String value = null;
		String parameter = "dimmer_project_path";
		// path needs to be a readable file
		if(this.parameters.get(parameter)!=null && !this.parameters.get(parameter).equals("")){
			value = check_path(parameter,false,true,false,false);
		}
		this.parameters.remove(parameter);
		this.dimmer_project_path = value;
	}
	

	
	// ################################# error report #############################
	
	public String error_report(){
		StringBuilder builder = new StringBuilder();
		builder.append("");
		
		if(check==false){
			builder.append("\nPlease check your configuration file. The following problems occured:\n");
			
			if(!this.missing_fields.isEmpty()){
				builder.append("\nMissing fields in the configuration file:\n");
				boolean first = true;
				for(String error: this.missing_fields){
					if(first){
						first = false;
					}else{
						builder.append("; ");
					}
					builder.append(error);
				}
				builder.append("\n");
			}
			
			if(!this.missing_values.isEmpty()){
				builder.append("\nMissing values for the fields:\n");
				boolean first = true;
				for(String error: this.missing_values){
					if(first){
						first = false;
					}else{
						builder.append("; ");
					}
					builder.append(error);
				}
				builder.append("\n");
			}
			
			if(!this.malformatted_values.isEmpty()){
				builder.append("\nMalformatted/unaccepted values:\n");
				for(String error: this.malformatted_values.keySet()){
					builder.append(error+": "+malformatted_values.get(error)+"\n");
				}
			}
			if(!this.messages.isEmpty()){
				builder.append("\nMessages:\n");
					for(String error: this.messages){
						builder.append(error+"\n");
					}
			}
			
		}
			
		return builder.toString();
	}
	
	public String ignored_report(){
		StringBuilder builder = new StringBuilder();
		builder.append("");
		for(String key : this.parameters.keySet()){
			builder.append(key+": "+parameters.get(key)+"\n");
		}
		return builder.toString();
	}
	
	// ############################ getter/setter ##########################
	
	public String get(String parameter){
		return this.parameters.get(parameter);
	}

	public String get_mm_formula(){
		return this.mm_formula;
	}
	
	public String getAnnotationPath(){
		return this.annotation_path;
	}
	
	public String getOutputDirectory(){
		if(this.output_path.endsWith("/")){
			return this.output_path;
		}
		return this.output_path+"/";
	}
	
	public String getVariable(){
		return this.variable;
	}
	
	
	public HashSet<String> getConfoundingVariables(){
		return this.confounding_variables;
	}
	
	public String getModel(){
		return this.model;
	}
	
	public int getThreads(){
		return this.threads;
	}
	
	public boolean getBackgroundCorrection(){
		return this.background_correction;
	}
	
	public boolean getProbeFiltering(){
		return this.probe_filtering;
	}
	
	public boolean getCellComposition(){
		return this.cell_composition;
	}
	
	public void setCellComposition(boolean cell_composition){
		this.cell_composition = cell_composition;
	}
	
	public int getNPermutationsCpG(){
		return this.n_permutations_cpg;
	}
	
	public boolean getCd8t(){
		return this.cd8t;
	}
	
	public boolean getCd4t(){
		return this.cd4t;
	}
	
	public boolean getNk(){
		return this.nk;
	}
	
	public boolean getNCell(){
		return this.ncell;
	}
	
	public boolean getGran(){
		return this.gran;
	}
	
	public boolean getMono(){
		return this.mono;
	}
	
	public String getDataType(){
		return this.data_type;
	}
	
	public boolean isTwoSided(){
		return this.alternative_hypothesis.equals("both");
	}
	
	public boolean isLeftSided(){
		return this.alternative_hypothesis.equals("left");
	}
	
	public boolean isRightSided(){
		return this.alternative_hypothesis.equals("right");
	}
	
	public boolean getAssumeEqualVariance(){
		return this.assume_equal_variance;
	}
	
	public boolean isRegression(){
		return this.model.equals("Regression");
	}
	
	public boolean isTTest(){
		return this.model.equals("T-test");
	}
	
	/**
	 * Equals isRegression and isTTEst
	 * @return true if mixedModel is selected otherwise false
	 */
	public boolean isMixedModel(){
		return this.model.equals("mixedModel");
	}
	public float getMMVarianceCutoff() {return this.mm_variance_cutoff;}
	
	public boolean isPaired(){
		return  this.data_type.equals("paired");
	}
	
	public double getPvalueCutoff(){
		return this.p_value_cutoff;
	}
	
	public boolean getSavePermuPlots(){
		return this.save_permu_plots;
	}
	
	public boolean getSaveBeta(){
		return this.save_beta;
	}
	
	public boolean getPause(){
		return this.pause;
	}
	
	public int getMaxCpgDist(){
		return this.max_cpg_dist;
	}
	
	public int getNPermutationsDmr(){
		return this.n_permutations_dmr;
	}
	
	public int getNRandomRegions(){
		return this.n_random_regions;
	}
	
	public int getWSize(){
		return this.w_size;
	}
	
	public int getNExceptions(){
		return this.n_exceptions;
	}
	
	public float getPValueCutoff(){
		return this.p_value_cutoff;
	}
	
	public float getMinDiff(){
		return this.min_diff;
	}
	
	public String getPValueType(){
		return this.p_value_type;
	}
	
	public boolean getSaveSearchPlots(){
		return this.save_search_plots;
	}
	
	public boolean getSaveSearchTables(){
		return this.save_search_tables;
	}
	
	public boolean getSaveDmrPermuPlots(){
		return this.save_dmr_permu_plots;
	}
	
	public HashMap<String,String> getMalformattedValues(){
		return this.malformatted_values;
	}
	
	public ArrayList<String> getMissingValues(){
		return this.missing_values;
	}
	
	public String getProjectPath(){
		return this.dimmer_project_path;
	}
	
	public String getArrayType(){
		return this.array_type;
	}
	
	public boolean useBetaInput(){
		return this.input_type.equals(Variables.BETA);
	}
	public boolean useIdatInput(){
		return this.input_type.equals(Variables.IDAT);
	}
	public boolean useBisulfiteInput(){
		return this.input_type.equals(Variables.BISULFITE);
	}
	
	public String getInputType(){
		return this.input_type;
	}
	
	public int getMinReads(){
		return this.min_reads;
	}
	
	public int getMinReadExceptions(){
		return this.n_min_read_exceptions;
	}
	
	public float getMinVariance(){
		return this.min_variance;
	}
	
	
	
	public boolean doDMRSearch(){
		return this.dmr_search;
	}
	
	public String getCellCompositionPath(){
		if(this.cell_composition_path.endsWith("/")){
			return this.cell_composition_path;
		}
		return this.cell_composition_path+"/";
	}
	
	public String getBetaPath(){
		return this.beta_path;
	}
	
	
	public boolean load(){
		return this.load;
	}
	
	public void put(String key, String value){
		this.parameters.put(key, value);
	}

}
