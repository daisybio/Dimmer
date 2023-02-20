package dk.sdu.imada.jlumina.core.statistics;

import java.util.LinkedList;

import org.apache.commons.math3.stat.StatUtils;

import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.CpG;

public class LowVarianceFilter {
	
	private ReadManifest manifest;
	private float[][] beta;
	private double minVariance;
	
	
	public LowVarianceFilter(ReadManifest manifest, float[][] beta, double minVariance){
		this.manifest = manifest;
		this.beta = beta;
		this.minVariance = minVariance;
	}
	
	public void filter(){
		CpG[] cpgList = this.manifest.getCpgList();
		
		LinkedList<CpG> newCpgList = new LinkedList<>();
		LinkedList<float[]> newBeta = new LinkedList<>();
		
		for( int i = 0; i < cpgList.length; i++){
			double variance = StatUtils.variance(removeNaNs(beta[i]));
			if(variance >= this.minVariance){
				newCpgList.add(cpgList[i]);
				newBeta.add(beta[i]);
			}
		}
		
		System.out.println(cpgList.length - newCpgList.size() + " CpGs have been removed because of a variance lower than " + minVariance);
		
		this.manifest.setCpGList(newCpgList.toArray(new CpG[newCpgList.size()]));
		this.beta = newBeta.toArray(new float[newBeta.size()][]);
	}
	
	public double[] removeNaNs(float[] array){
		
		int counter = 0;
		for(float value : array){
			if(Float.isNaN(value)){
				counter++;
			}
		}
		
		double[] newArray = new double[array.length-counter];
		
		int index = 0;
		for(float value : array){
			if(!Float.isNaN(value)){
				newArray[index++] = value;
			}
		}
		
		return newArray;
	}
	
	public ReadManifest getManifest(){
		return this.manifest;
	}
	
	public float[][] getBeta(){
		return this.beta;
	}

}
