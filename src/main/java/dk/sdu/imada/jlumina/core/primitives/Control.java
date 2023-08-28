package dk.sdu.imada.jlumina.core.primitives;


public class Control {
	int address;
	String type;
	String color;
	String extendedType;
	
	public Control(int address, String type, String color, String extendedType ){
		this.address = address;
		this.type = type;
		this.color = color;
		this.extendedType = extendedType;
	}
	
	public int getAddress(){
		return address;
	}
	
	public String getType(){
		return type;
	}
	

	public String getColor() {
		return color;
	}
	
	public String getExtendedType() {
		return extendedType;
	}
	
}
