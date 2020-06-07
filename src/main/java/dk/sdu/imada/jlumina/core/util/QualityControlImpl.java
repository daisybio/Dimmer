package dk.sdu.imada.jlumina.core.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

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
	@Override
	public ArrayList<String> removeBadCpGs(float cutoff) {
		int sampleNo = rgSet.getNumberSamples();
		pvalues = detectP(rgSet,manifest,sampleNo);
		ArrayList<String> removed = pValueRemoval(pvalues,cutoff,sampleNo,0.05f);
		removeCpGs(removed,manifest);
		return removed;
	}
	
	
	public HashMap<String,float[]> detectP(RGSet rgset, ReadManifest manifest, int sampleNo){
		 int[] controlIdx = controlProbe.getControlAddress("NEGATIVE",controlProbe,0);
		 HashMap<Integer,float[]> rBg = getBackground(rgset.getRedSet(),controlIdx);
		 HashMap<Integer,float[]> gBg = getBackground(rgset.getGreenSet(),controlIdx);
		 float[] medianRed = findMedian(rBg,controlIdx, sampleNo);
		 float[] medianGreen = findMedian(gBg,controlIdx, sampleNo);
		 float[] redMAD = MedianAbsoluteDerivation(rBg,controlIdx,medianRed);
		 float[] greenMAD = MedianAbsoluteDerivation(gBg,controlIdx,medianGreen);
		 HashMap<String, float[]> detP = calculateP(rgset, manifest,medianRed,medianGreen,redMAD, greenMAD,sampleNo);
		 return detP;
	 }

	 public HashMap<Integer,float[]> getBackground(HashMap<Integer,float[]> color, int[] addresses){
		HashMap<Integer,float[]> newSet = new HashMap<Integer,float[]>();	
		 for(int i = 0;i<addresses.length;i++){
				int key = addresses[i];
				float[] address = color.get(addresses[i]);
				newSet.put(key, address);
			}
		 return newSet;
	 }

	 public float[] findMedian(HashMap<Integer,float[]> color,int[] addresses, int sampleNo){
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
			      if(ac<accept){
			      pvalues.put(key, vals);
			      }else{
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
}
