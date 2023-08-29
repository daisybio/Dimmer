package dk.sdu.imada.console;

public class Variables {
	
	//files
	
	// manifest files
	public static final String EPIC_MANIFEST = "epic_manifest.csv";
	public static final String RES_EPIC_MANIFEST = "resources/epic_manifest.csv";
	
	public static final String INFINIUM_MANIFEST = "manifest_summary.csv";
	public static final String RES_INFINIUM_MANIFEST = "resources/manifest_summary.csv";
	
	
	//Control probes
	public static final String CONTROL_450K = "control_450k.csv";
	public static final String RES_CONTROL_450K = "resources/control_450k.csv";
	
	public static final String CONTROL_EPIC = "control_EPIC.csv";
	public static final String RES_CONTROL_EPIC = "resources/control_EPIC.csv";
	
	//Sample annotation columns
	public static final String SENTRIX_ID = "Sentrix_ID";
	public static final String SENTRIX_POS = "Sentrix_Position";
	public static final String GROUP_ID = "Group_ID";
	public static final String PAIR_ID = "Pair_ID";
	public static final String GENDER_ID = "Gender_ID";
	
	public static final String BISULFITE_SAMPLE = "sample";
	
	//array types
	public static final String EPIC = "epic";
	public static final String INFINIUM = "450k";
	public static final String CUSTOM = "custom";
	
	//input types
	public static final String IDAT = "idat";
	public static final String BETA = "beta";
	public static final String BISULFITE = "bisulfite";

	public static final String MIXED_MODEL_SCRIPT = "/mixed_model.R";
	public static final String TIME_SERIES_SCRIPT = "/timeseries.R";

}
