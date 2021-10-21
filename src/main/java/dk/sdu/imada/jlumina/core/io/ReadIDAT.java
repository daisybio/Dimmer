package dk.sdu.imada.jlumina.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.io.ByteStreams;

public class ReadIDAT {

	byte[] bytes;
	int bytePosition;

	// .
	int fileSize;
	long version;
	int numFields;
	long fields [][];
	int nSNPsRead;


	// quants table in R
	int IlluminaID[];
	int Mean[];
	int SD[];
	int NBeads[];

	/*
	 * Based on readIDAT_NONENC.R
	 * THE VARIABLES BELOW REPRESENTS THE RES VARIABLE IN R
	 */

	//
	int MidBlock[];
	int redGreen;
	String barCode;
	String chipType;
	String runInfo[][];

	//unknows
	String mostlyNull;
	String mostlyA;
	String Unknown1;
	String Unknown2;
	String Unknown3;
	String Unknown4;
	String Unknown5;
	String Unknown6;

	public ReadIDAT() {
	}
	
	/**
	 * tesing push pull...
	 * @return
	 */
	public String getBarCode() {
		return barCode;
	}
	public int getBytePosition() {
		return bytePosition;
	}
	public byte[] getBytes() {
		return bytes;
	}
	public String getChipType() {
		return chipType;
	}
	public long[][] getFields() {
		return fields;
	}
	public int getFileSize() {
		return fileSize;
	}
	public int[] getIlluminaID() {
		return IlluminaID;
	}

	public int[] getMean() {
		return Mean;
	}

	public int[] getMidBlock() {
		return MidBlock;
	}

	public String getMostlyA() {
		return mostlyA;
	}

	public String getMostlyNull() {
		return mostlyNull;
	}

	public int[] getNBeads() {
		return NBeads;
	}

	public int getnSNPsRead() {
		return nSNPsRead;
	}

	public int getNumFields() {
		return numFields;
	}

	public int getRedGreen() {
		return redGreen;
	}

	public String[][] getRunInfo() {
		return runInfo;
	}

	public int[] getSD() {
		return SD;
	}

	public String getUnknown1() {
		return Unknown1;
	}

	public String getUnknown2() {
		return Unknown2;
	}

	public String getUnknown3() {
		return Unknown3;
	}

	public String getUnknown4() {
		return Unknown4;
	}

	public String getUnknown5() {
		return Unknown5;
	}

	public String getUnknown6() {
		return Unknown6;
	}

	public long getVersion() {
		return version;
	}

	private int readUnsignedByte() {
		Byte v = bytes[bytePosition++];
		return v & 0xFF;
	}

	private int readInt() {
		byte [] array = ArrayUtils.subarray(bytes, bytePosition, bytePosition+4);
		bytePosition+=4;
		ByteBuffer wrapper = ByteBuffer.wrap(array);
		wrapper.order(ByteOrder.LITTLE_ENDIAN);
		return wrapper.getInt();
	}

	private long readLong() {
		byte [] array = ArrayUtils.subarray(bytes, bytePosition, bytePosition+8);
		bytePosition+=8;
		ByteBuffer wrapper = ByteBuffer.wrap(array);
		wrapper.order(ByteOrder.LITTLE_ENDIAN);
		return wrapper.getLong();
	}

	private int readUnsignedShort() {
		byte [] array = ArrayUtils.subarray(bytes, bytePosition, bytePosition+2);
		bytePosition+=2;
		ByteBuffer wrapper = ByteBuffer.wrap(array);
		wrapper.order(ByteOrder.LITTLE_ENDIAN);
		
		return wrapper.getShort() & 0xffff;
	}

	@SuppressWarnings("unused")
	private String readString() {
		int m = readUnsignedByte();
		byte n = (byte) (m % 128);
		byte shift = 0;

		while((m % 128) == 1) {
			m = readUnsignedByte();
			shift+=7;

			byte product = 1;
			for (int i = 0 ; i < shift; i++) {
				product*=2;
			}
			byte k = (byte) ((m % 128) * product);
			n+=k;
		}

		int bytePositionAux = bytePosition;
		this.bytePosition+=n;
		return new String(ArrayUtils.subarray(bytes, bytePositionAux, bytePosition));
	}

	/**
	 * Read a single Idat file 
	 * @param fileName file name.
	 */
	public void readNonEncryptedIDAT(String fileName) {
		try {
			bytes = ByteStreams.toByteArray(new FileInputStream(new File(fileName)));
			fileSize = bytes.length;
			String fileType = new String(ArrayUtils.subarray(bytes, 0, 4));

			if (fileType.equals("IDAT")) {

				bytePosition = 4;
				version = readLong();

				if (version < 3) {
					System.err.println("Cannot handle IDAT file with version: " + bytes[4]);
				} else {

					numFields = readInt();
					System.out.println(fileName + ", num Fields: " + numFields);

					// rows: fieldCode, byteOffset, bytes
					fields = new long[3][numFields];


					for (int i = 0 ; i < numFields; i++) {
						fields[0][i] = readUnsignedShort();
						fields[1][i] = readLong();
					}

					long minByteOffset = NumberUtils.min((fields[1]));
					long SNPsReadByteOffset = fields[1][0];

					if (minByteOffset != SNPsReadByteOffset) {
						System.err.println("Problem found. exiting... ");
						System.exit(-1);
					}

					reset(minByteOffset);
					
					nSNPsRead = readInt();
					System.out.println(fileName + ", nSNPsRead: " + nSNPsRead);

					IlluminaID = new int[nSNPsRead];
					SD = new int[nSNPsRead];
					Mean = new int[nSNPsRead];
					NBeads = new int[nSNPsRead];
					
					reset(fields[1][1]);
					for (int i = 0; i < nSNPsRead; i++) {
						IlluminaID[i] = readInt();
					}

					reset(fields[1][2]);
					for (int i = 0; i < nSNPsRead; i++) {
						SD[i] = readUnsignedShort();
					}

					reset(fields[1][3]);
					for (int i = 0; i < nSNPsRead; i++) {
						Mean[i] = readUnsignedShort();
					}

					reset(fields[1][4]);
					for (int i = 0; i < nSNPsRead; i++) {
						NBeads[i] = readUnsignedByte();
					}
					
					
					// So far we don't need this fields from the IDAT files for the analysis 
					// So if in the future is necessary just uncoment the piece of code below
					/*reset(fields[1][5]);
					int midBlockEntries = readInt();
					MidBlock = new int[midBlockEntries];
					for (int i = 0; i < midBlockEntries; i++) {
						MidBlock[i] = readInt();
					}

					reset(fields[1][7]);
					redGreen = readInt();
					
					reset(fields[1][8]);
					mostlyNull = readString();
					
					reset(fields[1][9]);
					barCode = readString();
					
					reset(fields[1][10]);
					chipType = readString();
					
					reset(fields[1][11]);
					mostlyA = readString();
					
					reset(fields[1][12]);
					Unknown1 = readString();
					
					reset(fields[1][13]);
					Unknown2 = readString();
					
					reset(fields[1][14]);
					Unknown3 = readString();
					
					reset(fields[1][15]);
					Unknown4 = readString();
					
					reset(fields[1][16]);
					Unknown5 = readString();
					
					reset(fields[1][17]);
					Unknown6 = readString();

					int nRunInfoBlocks = readInt();
					// "RunTime", "BlockType", "BlockPars", "BlockCode", "CodeVersion"
					runInfo= new String[nRunInfoBlocks][5];

					reset(fields[1][6]);
					for (int i = 0; i < nRunInfoBlocks; i++) {
						runInfo[i][0] = readString();
						runInfo[i][1] = readString();
						runInfo[i][2] = readString();
						runInfo[i][3] = readString();
						runInfo[i][5] = readString();
					}*/
				}

			}else {
				System.err.println("IDAT file not acessible");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int readnSNPs(String fileName){	
		try {
			bytes = ByteStreams.toByteArray(new FileInputStream(new File(fileName)));
			fileSize = bytes.length;
			String fileType = new String(ArrayUtils.subarray(bytes, 0, 4));

			if (fileType.equals("IDAT")) {

				bytePosition = 4;
				version = readLong();

				if (version < 3) {
					System.err.println("Cannot handle IDAT file with version: " + bytes[4]);
				} else {

					numFields = readInt();

					// rows: fieldCode, byteOffset, bytes
					fields = new long[3][numFields];


					for (int i = 0 ; i < numFields; i++) {
						fields[0][i] = readUnsignedShort();
						fields[1][i] = readLong();
					}

					long minByteOffset = NumberUtils.min((fields[1]));
					long SNPsReadByteOffset = fields[1][0];

					if (minByteOffset != SNPsReadByteOffset) {
						System.err.println("Problem found. exiting... ");
						System.exit(-1);
					}

					reset(minByteOffset);
					
					return readInt();
				}
			}	
			else {
				System.err.println("IDAT file not acessible");
			}
		}	
		catch(IOException e){
			e.printStackTrace();
		}
		return 0;
		
	}

	/**
	 * @param offset position in the binary field which start a field (mean, sd, etc)
	 * @param fileName
	 */
	private void reset(long offset) {
		bytePosition = (int) offset;
	}
	
	/// teting the class ...
	public static void main(String args[]) {
		ReadIDAT readIDAT = new ReadIDAT();
		readIDAT.readNonEncryptedIDAT("/Users/diogo/Desktop/data/9969489068_R01C01_Grn.idat");
	}
}
