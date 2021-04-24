package dk.sdu.imada.jlumina.search.algorithms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import dk.sdu.imada.jlumina.search.primitives.DMR;

public class tests {
	
	public static void main(String[] args){
		
		System.out.println("Starting test...");
		
		//init array lists
		
		ArrayList<String> names_list = new ArrayList<>();
		ArrayList<String> chrs_list = new ArrayList<>();
		ArrayList<Integer> positions_list = new ArrayList<>();
		ArrayList<Integer> binary_list = new ArrayList<>();
		
		//read test file
		try{
			
			BufferedReader br = new BufferedReader(new FileReader(new File("C:/Users/kerst/Desktop/Dimmer/test_server.txt")));
			
			String line = br.readLine();
			while((line = br.readLine())!=null){
				
				String[] split = line.split("\t");
				names_list.add(split[0]);
				positions_list.add(Integer.parseInt(split[1]));
				chrs_list.add(split[2]);
				binary_list.add(Integer.parseInt(split[3]));
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		//parse Lists to arrays
		String[] names = names_list.toArray(new String[0]);
		String[] chrs = chrs_list.toArray(new String[0]);
		
		int[] positions = new int[positions_list.size()];
		int[] binary = new int[binary_list.size()];
		
		for(int i = 0; i < positions.length; i++){
			positions[i]= positions_list.get(i);
			binary[i] = binary_list.get(i);
		}
		
		//call methods
		int k = 2;
		int w = 4;
		int l = 1000;
		//DMRAlgorithm dmrAlgorithm = new DMRAlgorithm(k, w, l, 1, positions, chrs, main);
		//ArrayList<DMR> dmrs = dmrAlgorithm.islandSearch(binary);
		
		//DMRTester tester = new DMRTester(k,w,l,binary,positions,chrs);
		//tester.runTests(dmrs);
		//System.out.println(dmrs.size());
	
		
		
		
	}
}
	
	

