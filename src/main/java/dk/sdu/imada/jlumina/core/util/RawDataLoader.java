package dk.sdu.imada.jlumina.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import dk.sdu.imada.console.Util;
import dk.sdu.imada.jlumina.core.io.ReadControlProbe;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.MSet;
import dk.sdu.imada.jlumina.core.primitives.RGSet;
import dk.sdu.imada.jlumina.core.primitives.USet;
import dk.sdu.imada.jlumina.core.statistics.BackgroundCorrection;
import dk.sdu.imada.jlumina.core.statistics.CellCompositionCorrection;
import dk.sdu.imada.jlumina.core.statistics.CheckGender;
import dk.sdu.imada.jlumina.core.statistics.Normalization;

public class RawDataLoader extends DataProgress {

	RGSet rgSet;
	ReadManifest manifest;
	ReadControlProbe readControlProbe;
	USet uSet;
	MSet mSet;
	
	CellCompositionCorrection cellCompositionCorrection;
	CCFileCheck ccFileCheck;
	
	Normalization normalization;
	
	AbstractQualityControl qualityControl;

	USet refUSet; 
	MSet refMSet;

	int numThreads;

	char[] gender;
	
	boolean performBackgroundCorrection = false;
	boolean performProbeFiltering = false;

	public RawDataLoader(RGSet rgSet, ReadManifest manifest, ReadControlProbe readControlProbe, USet uSet, MSet mSet,
			CellCompositionCorrection cellCompositionCorrection, USet refUSet, MSet refMSet,
			Normalization normalization, int numThreads, char[] gender, CCFileCheck ccFileCheck) {

		super();

		this.rgSet = rgSet;
		this.manifest = manifest;
		this.readControlProbe = readControlProbe;
		this.uSet = uSet;
		this.mSet = mSet;
		this.cellCompositionCorrection = cellCompositionCorrection;

		this.refMSet = refMSet;
		this.refUSet = refUSet;

		this.normalization = normalization;
		this.numThreads = numThreads;

		this.gender = gender;
		
		this.ccFileCheck = ccFileCheck;
	}
	
	public AbstractQualityControl getQualityControl() {
		return qualityControl;
	}
	
	public void setQualityControl(AbstractQualityControl qualityControl) {
		this.qualityControl = qualityControl;
	}
	

	public RGSet getRgSet() {
		return rgSet;
	}

	public ReadManifest getManifest() {
		return manifest;
	}

	public USet getuSet() {
		return uSet;
	}

	public MSet getmSet() {
		return mSet;
	}

	public MSet getRefMSet() {
		return refMSet;
	}

	public USet getRefUSet() {
		return refUSet;
	}

	public CellCompositionCorrection getCellCompositionCorrection() {
		return cellCompositionCorrection;
	}

	public Normalization getNormalization() {
		return normalization;
	}

	public void setNormalization(Normalization normalization) {
		this.normalization = normalization;
	}

	public void setRefMSet(MSet refMSet) {
		this.refMSet = refMSet;
	}

	public void setRefUSet(USet refUSet) {
		this.refUSet = refUSet;
	}

	public void setCellCompositionCorrection(CellCompositionCorrection cellCompositionCorrection) {
		this.cellCompositionCorrection = cellCompositionCorrection;
	}

	public void setRgSet(RGSet rgSet) {
		this.rgSet = rgSet;
	}

	public void setManifest(ReadManifest manifest) {
		this.manifest = manifest;
	}

	public void setuSet(USet uSet) {
		this.uSet = uSet;
	}

	public void setmSet(MSet mSet) {
		this.mSet = mSet;
	}
	
	public void setPerformBackgroundCorrection(boolean perform){
		performBackgroundCorrection = perform;
	}
	
	public void setPerformProbeFiltering(boolean perform){
		performProbeFiltering = perform;
	}

	public void loadData() throws OutOfMemoryError{
		
		this.warnings = new ArrayList<>();

		this.done = false;
		setMsg("Processing raw data...");
		int p = 0;
		setProgress(p++);
		
		System.out.println("Reading IDAT files");
		this.rgSet.loadIDATsMap();
		
		if(rgSet.hasWarnings()){
			System.out.println(Util.warningLog(rgSet.getWarnings()));
			this.warnings.addAll(rgSet.getWarnings());
		}
		
		
		setMsg("Loading CpG probe info...");
		System.out.println("Loading CpG probe info...");
		setProgress(p++);
		
		int numSamples = this.rgSet.getSampleIDs().size();
		this.manifest.loadManifest();
		
		ReadControlProbe controlProbe = this.readControlProbe;
		controlProbe.loadManifest();
		
		if(performProbeFiltering){
			System.out.println("Running detection-p and removing low quality CpG loci");
			AbstractQualityControl qualityControl = this.getQualityControl();
			ArrayList<String> removedCpGs = qualityControl.removeBadCpGs(0.01f);
			System.out.println(removedCpGs.size() + " low quality CpGs were removed");
		}

						
		//testing function normalization
		/*FunctionNormalization fn = new FunctionNormalization();
		fn.setControlData(controlProbe);
		fn.setManifest(manifest);
		fn.performNormalization(rgSet, manifest, null, 1);*/
		
		if(performBackgroundCorrection){
			System.out.println("Performing background correction");
			BackgroundCorrection bc = new BackgroundCorrection(controlProbe,0.05);
			bc.performNormalization(rgSet, controlProbe, null, numSamples);
		}

		
		
		setMsg("Setting U and M probes");
		System.out.println("Setting U and M probes");
		setProgress(p++);
		
		this.uSet.setManifest(manifest);
		this.uSet.setRgSet(rgSet);

		this.mSet.setManifest(manifest);
		this.mSet.setRgSet(rgSet);

		try {
			this.uSet.loadData();
			this.mSet.loadData();
			
			//check for missing sites
			HashSet<Integer> badCpGIndices = uSet.getBadIndices();
			badCpGIndices.addAll(mSet.getBadIndices());
			if(badCpGIndices.size()!=0){
				
				System.out.println(Util.warningLog(badCpGIndices.size()+" were ignored because of missing data"));
				this.warnings.add(badCpGIndices.size()+" were ignored because of missing data");
				manifest.removeByIndex(badCpGIndices);
			}

		}catch(OutOfMemoryError e) {
			this.setOveflow(true);
			System.out.println("Memory ram problem. "
					+ "Increase your java heap space with the parameters -Xmx and Xms");
		}
		rgSet = null; System.gc();


		if (cellCompositionCorrection!=null){

			setMsg("Estimating cell composition");
			System.out.println("Estimating cell composition");
			setProgress(p++);
			
			
			String mf = ccFileCheck.getMPath();
			String uf = ccFileCheck.getUPath();

			setMsg("Loading cell composition reference data");
			System.out.println("Loading cell composition reference data");
			setProgress(p++);
			
			try {

				refMSet.loadData(mf);
				refUSet.loadData(uf);

				setMsg("Merging user and ref datasets");
				System.out.println("Merging user and ref datasets");
				setProgress(p++);
				
				MSet combinedMset = new MSet();
				combinedMset.setData(MatrixUtil.combineDataSets(refMSet, mSet, manifest));

				USet combinedUset = new USet();
				combinedUset.setData(MatrixUtil.combineDataSets(refUSet, uSet, manifest));
				refUSet=null; refMSet=null; System.gc();

				CheckGender checkGender = new CheckGender(uSet, mSet, manifest, -2);

				char[] newGender = null;

				if (this.gender == null) {
					setMsg("Running gender detection");
					System.out.println("Running gender detection");
					newGender = checkGender.getGender();
					char[] auxGender = new char[60 + newGender.length];
					int index = 0;
					for (int i = 0 ; i < 60; i++) {
						auxGender[index++] = 'M';
					}
					for (char c : newGender) {
						auxGender[index++] = c;
					}
					newGender = auxGender;

				}else {

					newGender = new char[60 + this.gender.length];
					int index = 0;
					for (int i = 0 ; i < 60; i++) {
						newGender[index++] = 'M';
					}
					for (char c : this.gender) {
						newGender[index++] = c;
					}
				}

				setMsg("Running normalization, this gonna take a while ....");
				System.out.println("Running normalization, this gonna take a while ....");
				setProgress(p++);
				normalization.performNormalization(combinedMset, manifest, newGender, numThreads);
				normalization.performNormalization(combinedUset, manifest, newGender, numThreads);

				setMsg("Calculating cell composition ....");
				System.out.println("Calculating cell composition ....");
				setProgress(p++);
				
				HashMap<String, float[]> beta = MatrixUtil.getBeta(combinedUset.getData(),  combinedMset.getData(), manifest, 0.0f);
				combinedMset=null; combinedUset=null; System.gc();
				cellCompositionCorrection.setManifest(manifest);
				cellCompositionCorrection.estimateCellComposition(beta, numSamples);
				cellCompositionCorrection.write("C:/Users/kerst/Desktop/Dimmer/cc.tsv");
				beta=null; System.gc();
			}catch(OutOfMemoryError e) {
				this.setOveflow(true);
				System.out.println("Memory ram problem. Increase your java heap space with the parameters -Xmx and Xms");
			}
		}


		// replace this normalization below to a Illummina normalization
		// however after applying the normalization in the RGSet, we need to set
		// the uSet and mSet object like in the line 140.
		// however be aware of the order this line 140, should be after you call your Illummina normalization method.
		// Also pay attention to the line 154, because I erase the RGSET object to save memory: once we get our USet and MSet we don't need it anymore
		//
		if (normalization!=null)  {
			
			try {
				
				setMsg("Running normalization in your data, this is gonna take a while ....");
				setProgress(p++);
				
				System.out.println("Normalizing user data, this can take a  while...");

				if (this.gender == null) {
					CheckGender checkGender = new CheckGender(uSet, mSet, manifest, -2);
					this.gender = checkGender.getGender();
				}
				normalization.performNormalization(uSet, manifest, this.gender, numThreads);
				normalization.performNormalization(mSet, manifest, this.gender, numThreads);

				normalization = null; System.gc();
				setProgress(p++);
				this.setDone(true);

			}catch(OutOfMemoryError e) {
				this.setOveflow(true);
				System.out.println("Memory ram problem. Increase your java heap space with the parameters -Xmx and Xms");
			}
		}
	}
}	

