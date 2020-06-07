package dk.sdu.imada.jlumina.search.algorithms;

import java.util.ArrayList;

import dk.sdu.imada.jlumina.search.primitives.DMR;

public class DMRTester {

	private int w;
	private int k;
	private int l;
	private int[] binaryArray;
	private int[] positions;
	private String[] chrs;
	private ArrayList<DMR> errorDMRs;
	private int count;
	
	
	public DMRTester(int k, int w, int l, int[] binaryArray, int[] positions, String[] chrs){
		this.w = w;
		this.k = k;
		this.l = l;
		this.binaryArray = binaryArray;
		this.positions = positions;
		this.chrs = chrs;
		count = 0;
	}
	
	//checks every dmr
	
	public void runTests(ArrayList<DMR> dmrs){
		
		for(DMR dmr: dmrs){
			
			//check if all cpgs are on the same chromosom
			boolean chrError = chrError(dmr);
	
			//check if distances are correct
			boolean distanceError = distanceError(dmr);
			
			//checks if exceptions are correct
			boolean exceptionError = exceptionError(dmr);
			
			if(chrError||distanceError||exceptionError){
				count++;
				System.out.println("Error: \t"+count);
				System.out.print("Types:\t");
				if(chrError){
					System.out.print("chrError\t");
				}
				if(distanceError){
					System.out.print("distanceError\t");
				}
				if(exceptionError){
					System.out.print("exceptionError\t");
				}
				System.out.println("");
				System.out.println("DMR:");
				System.out.println(dmr.log());
			}
			
		}
	}
	
	/**
	 * checks if in the window are enough 1 in the binary array
	 * 
	 * @param dmr
	 * @return
	 */
	
	public boolean exceptionError(DMR dmr){
		
		int beginPosition = dmr.beginPosition;
		int totalCpgs = dmr.totalCpgs;
		
		boolean exceptionError = false;
		int sum = 0;
		int i = beginPosition;
		for(; i < beginPosition+w+1;i++){
			sum += binaryArray[i];
		}
		
		if(sum < w+1-l){
			exceptionError = true;
		}
		
		while(i<beginPosition+totalCpgs){
			sum+=binaryArray[i];
			if(i-w-1>=0){
				sum-=binaryArray[i-w-1];
			}
			if(sum< w+1-l){
				exceptionError = true;
			}
			i++;
			
		}
		
		
		return exceptionError;
	}
	
	
	/**
	 * 
	 * @param dmr
	 * @return true if there are different chromosoms in one dmr
	 */
	
	public boolean chrError(DMR dmr){
		
		int beginPosition = dmr.beginPosition;
		int totalCpgs = dmr.totalCpgs;
		
		boolean chrError= false;
		
		String chr = chrs[beginPosition];
		for(int i = beginPosition; i < beginPosition+totalCpgs; i++){
			if(!chrs[i].equals(chr)){
				chrError = true;
			}
		}
		
		return chrError;
		
	}
	
	
	/**
	 * 
	 * @param dmr
	 * @return true if the distance between two consecutive cpgs in a dmr is wrong
	 */
	
	public boolean distanceError(DMR dmr){
		
		int beginPosition = dmr.beginPosition;
		int totalCpgs = dmr.totalCpgs;
		
		boolean distanceError = false;
		for(int i = beginPosition+1; i < beginPosition + totalCpgs; i++){
			if(positions[i]-positions[i-1]>1000){
				distanceError = true;
			}
		}
		return distanceError;
	}
	

	
}
