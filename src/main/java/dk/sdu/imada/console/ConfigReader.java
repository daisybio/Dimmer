package dk.sdu.imada.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ConfigReader {
	
	/**
	 * reads in a Dimmer config file, key-value pairs need to be seperated by ":"
	 * @param path : path to the config file
	 * @return a Config object
	 */
	
	public Config read(String path){
		
		path = path.replace('\\', '/');
		HashMap<String,String> parameters = new HashMap<>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader (new File(path)));
			String line;
			System.out.println("Reading configuration file: "+path);
			while((line = br.readLine())!=null){
				line = line.trim();
				if(!line.startsWith("#") && !line.equals("")){
					String[] split_line = line.split(":", 2);
					if(split_line.length<2){
						parameters.put(split_line[0].trim(),"");
					}
					else{
						parameters.put(split_line[0].trim(), split_line[1].trim());
					}

				}
			}
			br.close();
		} 
		catch (IOException e) {
			System.out.println("The configuration file " + path +" does not exist or has no reading permission!");
			System.exit(0);
		}
		
		return new Config(parameters);
	}
}
