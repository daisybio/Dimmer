package dk.sdu.imada.jlumina.core.util;

import java.util.HashMap;

import dk.sdu.imada.jlumina.core.io.ReadControlProbe;
import dk.sdu.imada.jlumina.core.io.ReadManifest;
import dk.sdu.imada.jlumina.core.primitives.MSet;
import dk.sdu.imada.jlumina.core.primitives.RGSet;
import dk.sdu.imada.jlumina.core.primitives.USet;
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
	Normalization normalization;
	
	AbstractQualityControl qualityControl;

	USet refUSet; 
	MSet refMSet;

	int numThreads;

	char[] gender;

	public RawDataLoader(RGSet rgSet, ReadManifest manifest, ReadControlProbe readControlProbe, USet uSet, MSet mSet,
			CellCompositionCorrection cellCompositionCorrection, USet refUSet, MSet refMSet,
			Normalization normalization, int numThreads, char[] gender) {

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

	public void loadData() {

		this.done = false;
		setMsg("Processing raw data...");
		int p = 0;
		setProgress(p++);
		System.out.println("Reading IDAT files");
		this.rgSet.loadIDATs();
		
		setMsg("Loading CpG probe info...");
		setProgress(p++);
		int numSamples = this.rgSet.getSampleIDs().size();
		this.manifest.loadManifest();
		
		ReadControlProbe controlProbe = this.readControlProbe;
		controlProbe.loadManifest();
		
		/*System.out.println("Running detection-p and removing low quality CpG loci");
		AbstractQualityControl qualityControl = this.getQualityControl();
		ArrayList<String> removedCpGs = qualityControl.removeBadCpGs(0.01f);
		System.out.println(removedCpGs.size() + " CpGs were removed:");*/
						
		//testing function normalization
		/*FunctionNormalization fn = new FunctionNormalization();
		fn.setControlData(controlProbe);
		fn.setManifest(manifest);
		fn.performNormalization(rgSet, manifest, null, 1);*/
		
		
		/*System.out.println("Performing backgorund correction");
		BackgroundCorrection bc = new BackgroundCorrection(controlProbe,0.05);
		bc.performNormalization(rgSet, controlProbe, null, numSamples);*/
		
		
		setMsg("Setting U and M probes");
		setProgress(p++);
		this.uSet.setManifest(manifest);
		this.uSet.setRgSet(rgSet);

		this.mSet.setManifest(manifest);
		this.mSet.setRgSet(rgSet);

		try {
			this.uSet.loadData();
			this.mSet.loadData();
		}catch(OutOfMemoryError e) {
			this.setOveflow(true);
			System.out.println("Memory ram problem. "
					+ "Increase your java heap space with the parameters -Xmx and Xms");
		}
		rgSet = null; System.gc();

		if (cellCompositionCorrection!=null) {

			setMsg("Estimating cell composition");
			setProgress(p++);
			
			//refMSet.loadData("/Users/diogo/Desktop/flow.sorted.blood.data/M.csv");
			//refUSet.loadData("/Users/diogo/Desktop/flow.sorted.blood.data/U.csv");
			
			String mf = "resources/M.csv";

			if (getClass().getClassLoader().getResourceAsStream(mf)==null) {
				mf = "M.csv";
			}
			String uf = "resources/U.csv";
			if (getClass().getClassLoader().getResourceAsStream(uf)==null) {
				uf = "U.csv";
			}

			setMsg("Loading cell composition reference data");
			setProgress(p++);
			try {
				refMSet.loadData(mf);
				refUSet.loadData(uf);
				//refUSet.loadData((getClass().getResource("U.csv").toString()));

				setMsg("Merging user and ref datasets");
				setProgress(p++);
				MSet combinedMset = new MSet();
				combinedMset.setData(MatrixUtil.combineDataSets(refMSet, mSet, manifest));
				USet combinedUset = new USet();
				combinedUset.setData(MatrixUtil.combineDataSets(refUSet, uSet, manifest));
				refUSet=null; refMSet=null; System.gc();

				CheckGender checkGender = new CheckGender(combinedUset, combinedMset, manifest, -2);

				char[] newGender = null;

				if (this.gender == null) {
					setMsg("Running gender detection");
					newGender = checkGender.getGender();
					for (int i = 0 ; i < 60; i++) {
						newGender[i] = 'M';
					}

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
				setProgress(p++);
				normalization.performNormalization(combinedMset, manifest, newGender, numThreads);
				normalization.performNormalization(combinedUset, manifest, newGender, numThreads);

				setMsg("Calculating cell composition ....");
				setProgress(p++);
				HashMap<String, float[]> beta = MatrixUtil.getBeta(combinedUset.getData(),  combinedMset.getData(), manifest, 0.0f);
				combinedMset=null; combinedUset=null; System.gc();
				cellCompositionCorrection.setManifest(manifest);
				cellCompositionCorrection.estimateCellComposition(beta, numSamples);
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
				
				setMsg("Running normalization in your data, this gonna take a while ....");
				setProgress(p++);
				
				System.out.println("Normalizing user data, this can take a  while...");

				if (this.gender == null) {
					CheckGender checkGender = new CheckGender(uSet, mSet, manifest, -2);
					this.gender = checkGender.getGender();
				}

				normalization.performNormalization(uSet, manifest, this.gender, numThreads);
				normalization.performNormalization(mSet, manifest, this.gender, numThreads);

				normalization = null; System.gc();
				this.done=true;
				setProgress(p++);

			}catch(OutOfMemoryError e) {
				this.setOveflow(true);
				System.out.println("Memory ram problem. Increase your java heap space with the parameters -Xmx and Xms");
			}
		}
	}
}	

