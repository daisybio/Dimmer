package dk.sdu.imada.jlumina.core.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import dk.sdu.imada.console.Util;
import dk.sdu.imada.jlumina.core.io.ReadControlProbe;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.CpG;
import dk.sdu.imada.jlumina.core.primitives.RGSet;
import weka.core.matrix.Maths;

public class QualityControlImpl extends AbstractQualityControl{
	HashMap<String,float[]> pvalues;

	public QualityControlImpl(RGSet rgSet, ReadManifest manifest, ReadControlProbe controlProbe) {
		super(rgSet, manifest, controlProbe);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * Besure that the object manifest has their bad CpGs removed
	 */
	
	/**
	 * @param cutoff: p-value cutoff
	 * @return list of removed CpG names
	 */
	@Override
	public ArrayList<String> removeBadCpGs(float cutoff) {
		int sampleNo = rgSet.getNumberSamples();
		pvalues = detectP(rgSet,manifest,sampleNo);
		ArrayList<String> removed = pValueRemoval(pvalues,cutoff,sampleNo,0.05f);
		removeCpGsFast(removed,manifest);
		return removed;
	}
	
	/**
	 * 
	 * @param rgset
	 * @param manifest
	 * @param sampleNo
	 * @return hashmap containing p-values for every cpg (intensity explained by background distribution)
	 */
	
	public HashMap<String,float[]> detectP(RGSet rgset, ReadManifest manifest, int sampleNo){
		 int[] controlIdx = controlProbe.getControlAddress("NEGATIVE",controlProbe,0);
		 HashMap<Integer,float[]> rBg = getBackground(rgset.getRedSet(),controlIdx);
		 HashMap<Integer,float[]> gBg = getBackground(rgset.getGreenSet(),controlIdx);
		 
		 if(rBg == null || gBg == null){
			 System.out.println(Util.warningLog("Probe filtering won't be performed!"));
			 return new HashMap<String,float[]>();
		 }
		 float[] medianRed = findMedian(rBg,controlIdx, sampleNo);
		 float[] medianGreen = findMedian(gBg,controlIdx, sampleNo);
		 float[] redMAD = MedianAbsoluteDerivation(rBg,controlIdx,medianRed);
		 float[] greenMAD = MedianAbsoluteDerivation(gBg,controlIdx,medianGreen);
		 HashMap<String, float[]> detP = calculateP(rgset, manifest,medianRed,medianGreen,redMAD, greenMAD,sampleNo);
		 return detP;
	 }
	
	/**
	 * 
	 * @param color : either the red or green set
	 * @param addresses : NEGATIVE control adresses
	 * @return	a HashMap linking from the control adresses to the measured values (sampleNo float values per entry)
	 */
	 public HashMap<Integer,float[]> getBackground(HashMap<Integer,float[]> color, int[] addresses){
		HashMap<Integer,float[]> newSet = new HashMap<Integer,float[]>();	
		 for(int i = 0;i<addresses.length;i++){
				int key = addresses[i];
				float[] address = color.get(addresses[i]);
				if(address == null){
					System.out.println(Util.warningLog("Missing control probe address: " + key));
					return null;
				}
				newSet.put(key, address);
			}
		 return newSet;
	 }
	 /**
	  * 
	  * @param color : the background distribution
	  * @param addresses : the control adresses 
	  * @param sampleNo : number of total samples 
	  * @return a array containing the medians of the background distribution for every sample
	  */
	 public float[] findMedian(HashMap<Integer,float[]> color, int[] addresses, int sampleNo){
			int noAddresses = addresses.length;
			int mid = addresses.length/2;
			float[] results = new float[sampleNo];
			for(int i = 0;i<sampleNo;i++){
				float[] sort = new float[noAddresses];
				for(int j=0;j<noAddresses;j++){
				    float[] address = color.get(addresses[j]);
				    sort[j]=address[i];
				}
				Arrays.sort(sort);
				results[i] = sort[mid];
			}

			return results;
	 }
	 
	 /**
	  * calulates the median absolute deviation from the median with constant scale factor
	  * k = 1.4826 (for normally distributed data)
	  * @param color : the background distributions
	  * @param addresses : the control adresses
	  * @param median : the background medians produced by findMedian()
	  * @return the median * 1.4826 of the absolute deviation from the median (for every sample)
	  */


	 public float[] MedianAbsoluteDerivation(HashMap<Integer,float[]> color,int[] addresses, float[] median){
			double k = 1.4826;
		    int noAddresses = addresses.length;
			int mid = addresses.length/2;
			float[] results = new float[median.length];
			for(int i = 0;i<median.length;i++){
				float[] sort = new float[noAddresses];
				for(int j=0;j<noAddresses;j++){
				    float[] address = color.get(addresses[j]);
				    sort[j]=address[i]-median[i];
				    if(sort[j]<0){
				       float a = Math.abs(sort[j]);
				       sort[j]=a;
				    }
				}
				Arrays.sort(sort);
				results[i] = (float) (sort[mid]*k);
			}
			return results;
	 }
	 
	 /**
	  * 
	  * @param rgset
	  * @param manifest
	  * @param rMu red set background medians
	  * @param gMu green set background medians
	  * @param rSd red set median deviation
	  * @param gSd green set median deviation
	  * @param sampleNo
	  * @return p values for every cpg and sample (intensity explained by background distribution)
	  */

	 public HashMap<String,float[]> calculateP(RGSet rgset, ReadManifest manifest,float[] rMu, float[] gMu,float[] rSd, float[] gSd ,int sampleNo){
		HashMap<String, float[]> detP = new HashMap<String,float[]>();
		CpG[] list = manifest.getCpgList();
		for(int i=0;i<list.length;i++){
			String name = list[i].getCpgName();
			int addressA = 0;
			int addressB = 0;
			if(list[i].getInifniumType().equals("II")){
				float[]  result = new float[sampleNo];
				addressA = list[i].getAddressA();
				float[] adAr = rgset.getRedSet().get(addressA);
				float[] adAg = rgset.getGreenSet().get(addressA);
				for(int j=0;j<sampleNo;j++){
					float intensity = adAr[j]+adAg[j];
					float value = (intensity-(rMu[j]+gMu[j]))/(rSd[j]+gSd[j]);
					double pn = 1-Maths.pnorm(value);
					result[j]=(float) pn;		
				}
				detP.put(name, result);
				
			}else if(list[i].getInifniumType().equals("I")&&list[i].getColorChannel().equals("Red")){
				float[]  result = new float[sampleNo];
				addressA = list[i].getAddressA();
				addressB = list[i].getAddressB();
				float[] adAr = rgset.getRedSet().get(addressA);
				float[] adBr = rgset.getRedSet().get(addressB);
				for(int j=0;j<sampleNo;j++){
					float intensity = adAr[j]+adBr[j];
					float value = (intensity-(rMu[j]*2))/(rSd[j]*2);
					double pn = 1-Maths.pnorm(value);
					result[j]=(float)pn;
				}
				detP.put(name, result);
				
			}else if(list[i].getInifniumType().equals("I")&&list[i].getColorChannel().equals("Grn")){
				float[]  result = new float[sampleNo];
				addressA = list[i].getAddressA();
				addressB = list[i].getAddressB();
				float[] adAg = rgset.getGreenSet().get(addressA);
				float[] adBg = rgset.getGreenSet().get(addressB);
				for(int j=0;j<sampleNo;j++){
					float intensity = adAg[j]+adBg[j];
					float value = (intensity-(gMu[j]*2))/(gSd[j]*2);
					double pn = 1-Maths.pnorm(value);
					result[j]=(float)pn;
					}
				detP.put(name, result);
			}
		}
		 return detP;		
	 }
	 
	 /**
	  * 
	  * @param pvalues : result of detectP
	  * @param cutoff : p-value cutoff
	  * @param sampleNo : number of samples 
	  * @param accept : maximum fraction of samples below cutoff per CpG
	  * @return list of CpG keys, that should be removed
	  */
	 
	 public ArrayList<String> pValueRemoval(HashMap<String,float[]> pvalues,float cutoff,int sampleNo, float accept){
		 ArrayList<String> removed = new ArrayList<String>();
		 for (Entry<String, float[]> values: pvalues.entrySet()) {
			      float[] vals = values.getValue();
			      String key = values.getKey();
			      int na = 0;
			      	for(int i=0;i<vals.length;i++){
			    	  if(vals[i]>cutoff){
			    		 na++; 
			    	  }
			      	}
			      

			      float ac = (float) na/ (float)vals.length;
			      if(!(ac<accept)){
			    	 removed.add(key);
			      }
			}
		 return removed;
		 
	 }

	 
	 public void removeCpGs(ArrayList<String> removed,ReadManifest manifest){
		 CpG[] list = manifest.getCpgList();
		 ArrayList<CpG> newList = new ArrayList<CpG>();
		 for(int i=0;i<list.length;i++){
			 int r =0;
			 for(int j=0;j<removed.size();j++){
				 if(list[i].getCpgName().equals(removed.get(j))){
					r=1;
				 } 
			 }
			 if(r==0){
				 newList.add(list[i]);
			 }	 
			 
		 }
		 CpG[] cpgl = new CpG[newList.size()];
		 cpgl =  newList.toArray(cpgl);
		 manifest.setCpGList(cpgl);
		 
	 }
	 
	 /**
	  * faster implementation of removeCpGs
	  * @param removed
	  * @param manifest
	  */
	 
	 public void removeCpGsFast(ArrayList<String> removed,ReadManifest manifest){
		 CpG[] list = manifest.getCpgList();
		 ArrayList<CpG> newList = new ArrayList<CpG>();
		 Set removedSet = new HashSet(removed);
		 for(int i=0;i<list.length;i++){
			 if(!removedSet.contains(list[i].getCpgName())){
				 newList.add(list[i]);
			 }	 
			 
		 }
		 CpG[] cpgl = new CpG[newList.size()];
		 cpgl =  newList.toArray(cpgl);
		 manifest.setCpGList(cpgl);
	 }
}
