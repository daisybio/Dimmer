package dk.sdu.imada.console;

import dk.sdu.imada.gui.controllers.StartGUI;

public class Start {
	public static void main(String[] args){
		
		//calls GUI start if no args
		if(args.length==0){
			StartGUI.main(args);
		}
		// else calls the console interface
		else{
			ConfigReader configReader= new ConfigReader();
			Config config = configReader.read(args);
			ConsoleMainController mainController = new ConsoleMainController(config);
			mainController.start();
		}
	}
}
