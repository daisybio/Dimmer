package dk.sdu.imada.jlumina.search.primitives;

import org.apache.commons.lang3.StringUtils;

/**
 * @author diogo
 * Class used for store the statistical parameters computed for each CpG 
 * 
 * 
 * TODO 1 gets and setters
 * 		2 use only the arrays
 * 		3 change to private all attributes
 */
public class CPGResult {

	public float pvalueList[];
	
	public float tvalueList[];
	
	public float coefficientList[];
	
	public float p0List [];
	
	public float pvalue;
	
	public float tvalue;
	
	public float meanMethylationDifference;
	
	// . 
	public CPGResult(int n) {
		
		this.pvalueList = new float[n];
		
		this.tvalueList = new float[n];
		
		this.coefficientList = new float[n];
		
		this.p0List = new float[n];
		
		// pseudo count
		for (int i = 0; i < p0List.length; i++) this.p0List[i] = 1;
	}
	
	public float getMeanMethylationDifference() {
		return meanMethylationDifference;
	}
	
	public void setMeanMethylationDifference(float meanMethylationDifference) {
		this.meanMethylationDifference = meanMethylationDifference;
	}
	
	public String getPvalues() {
		String buff = "";
		
		for (float d : pvalueList) {
			buff+=d+";";
		}
		buff = StringUtils.chomp(buff, ";");
		
		return buff;
	}
	
	public String getTvalues() {
		String buff = "";
		
		for (float d : tvalueList) {
			buff+=d+";";
		}
		buff = StringUtils.chomp(buff, ";");
		
		return buff;
	}
	
	public String getCoefficients() {
		String buff = "";
		
		for (float d : coefficientList) {
			buff+=d+";";
		}
		buff = StringUtils.chomp(buff, ";");
		
		return buff;
	}
}
