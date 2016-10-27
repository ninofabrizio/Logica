package map;

import characters.Samus;

public class Zone {

	private char type;
	private Samus samus;

    private int i, j;
    
	public void setI(int i) {
		this.i = i;
	}
	
	public void setJ(int j) {
		this.j = j;
	}
    
	public void setType(char t) {
		type = t;
	}
	
	public void setSamus(Samus s) {
		samus = s;
	}
	
	public int getI() { 
		return i;
	}
	
	public int getJ() {
		return j;
	}
	
	public char getType() {
		return type;
	}
	
	public Samus getSamus() {
		return samus;
	}
}