package dk.sdu.imada.console;

import java.util.ArrayList;
import java.util.Scanner;

public class DMRConfigScanner {
	
	private ConsoleMainController mainController;
	private Config config;
	private boolean saved;
	
	private Scanner scanner;

	
	public DMRConfigScanner(Config config, ConsoleMainController mainController){
		this.mainController = mainController;
		this.config = config;
		this.scanner = new Scanner(System.in);
		saved = true;
	}
	
	public void start(){
		System.out.println("\nProgramm halted for parameter refinement...");
		if(config.getSavePermuPlots()){
			System.out.println("All already processed results can be found in "+config.getOutputDirectory());
		}
		else{
			System.out.println("No plots were saved. Type \"s\", if you want to save them now.");
			saved = false;
		}
		System.out.println("To quit the program, type \"q\".");
		System.out.print(parametersToString());
		System.out.println("Do you want to refine any of them? (y/n)");

		if(scanYN()){
			System.out.println("To change a parameter value, type in the new key-value pair in the format \"key: value\" (same as in the configuration file).");
			System.out.println("Once you made all your changes, type \"c\" to continue.\n"
					+ "To check the current parameter settings, type \"check\".\n"
					+ "To quit the program, type \"q\".");
			scanParameters();
		}
	}
	
	public String parametersToString(){
		StringBuilder builder = new StringBuilder();
		builder.append("Your current parameters are:\n");
		builder.append("max_cpg_dist: " + config.getMaxCpgDist()+"\n");
		builder.append("n_permutations_dmr: " + config.getNPermutationsDmr()+"\n");
		builder.append("n_random_regions: " + config.getNRandomRegions()+"\n");
		builder.append("w_size: " + config.getWSize()+"\n");
		builder.append("n_exceptions: " + config.getNExceptions()+"\n");
		builder.append("p_value_cutoff: " + config.getPValueCutoff()+"\n");
		builder.append("p_value_type: " + config.getPValueType()+"\n");
		builder.append("save_search_plots: " + config.getSaveSearchPlots()+"\n");
		builder.append("save_search_tables: " + config.getSaveSearchTables()+"\n");
		return builder.toString();
	}
	
	public boolean scanYN(){
		boolean finished = false;
		boolean result = false;
		
		while(!finished){
			
			String input = scanner.nextLine();
			input = input.trim();
			
			if(input.equals("y")){
				result = true;
				finished = true;
			}
			else if(input.equals("n")){
				result = false;
				finished = true;
			}
			else if(input.equals("s")){
				if(saved==false){
					this.mainController.getPermutationController().saveAll();
					saved=true;
					System.out.println("Ready for new console input.");
				}
				else{
					System.out.println("The results are already saved.");
				}
			}
			else if(input.equals("q")){
				System.out.println("Terminating DiMmer...");
				System.exit(0);
			}
			else{
				System.out.println("\""+input+"\" is no valid input, please try again. Your options are (y/n).");
			}

		}

		return result;
	}
	
	public void scanParameters(){
		boolean finished = false;
		
		while(!finished){
			
			String input = scanner.nextLine();
			input = input.trim();
			
			if(input.equals("c")){
				finished = true;
			}
			else if(input.equals("check")){
				System.out.print(parametersToString());
			}
			else if(input.equals("s")){
				if(saved==false){
					this.mainController.getPermutationController().saveAll();
					saved=true;
					System.out.println("Ready for new console input.");
				}
				else{
					System.out.println("The results are already saved.");
				}
			}
			else if(input.equals("q")){
				System.out.println("Terminating DiMmer...");
				System.exit(0);
			}
			else{
				matchField(input);
			}
		}
	}
	
	public void matchField(String input){
		String[] input_pair = input.split(":", 2);
		String field = input_pair[0].trim();
		String value;
		
		if(input_pair.length==2){
			value = input_pair[1].trim();
		}
		else{
			System.out.println("\""+input+"\" is no correctly formatted key-value pair. Please try again with a different input.");
			return;
		}
		
		switch(field){
			case "max_cpg_dist":
				changeMaxCpgDist(value);
				break;
			case "n_permutations_dmr":
				changeNPermutationsDmr(value);
				break;
			case "n_random_regions":
				changeNRandomRegions(value);
				break;
			case "w_size":
				changeWSize(value);
				break;
			case "n_exceptions":
				changeNExceptions(value);
				break;
			case "p_value_cutoff":
				changePValueCutoff(value);
				break;
			case "p_value_type":
				changePValueType(value);
				break;
			case "save_search_plots":
				changeSaveSearchPlots(value);
				break;
			case "save_search_tables":
				changeSaveSearchTables(value);
				break;
			default:
				System.out.println("\""+field+"\" is no valid parameter or command name. Please try again with a different input.");
				return;
		}
		report(field,value);
	}
	
	public void report(String field, String value){
		if(config.getMissingValues().contains(field)){
			config.getMissingValues().remove(field);
			System.out.println("Input \""+field+"\" has no value. Please try again with a different input.");
		}
		else if(config.getMalformattedValues().contains(field)){
			config.getMalformattedValues().remove(field);
			System.out.println("\""+value+"\" is no accepted value for the field \""+field+"\". Please try again with a different input.");
		}
		else{
			System.out.println("Change was succesful.");
		}
	}
	
	public void changeMaxCpgDist(String value){
		String field = "max_cpg_dist";
		config.put(field, value);
		config.check_max_cpg_dist();
	}
	
	public void changeNPermutationsDmr(String value){
		String field = "n_permutations_dmr";
		config.put(field, value);
		config.check_n_permutations_dmr();
	}
	
	public void changeNRandomRegions(String value){
		String field = "n_random_regions";
		config.put(field, value);
		config.check_n_random_regions();
	}
	
	public void changeWSize(String value){
		String field = "w_size";
		config.put(field, value);
		config.check_w_size();
	}
	
	public void changeNExceptions(String value){
		String field = "n_exceptions";
		config.put(field,value);
		config.check_n_exceptions();
	}
	
	public void changePValueCutoff(String value){
		String field = "p_value_cutoff";
		config.put(field, value);
		config.check_p_value_cutoff();
	}
	
	public void changePValueType(String value){
		String field = "p_value_type";
		config.put(field, value);
		config.check_p_value_type();
	}
	
	public void changeSaveSearchPlots(String value){
		String field = "save_search_plots";
		config.put(field, value);
		config.check_save_search_plots();
	}
	
	public void changeSaveSearchTables(String value){
		String field = "save_search_tables";
		config.put(field, value);
		config.check_save_search_tables();
	}

}
