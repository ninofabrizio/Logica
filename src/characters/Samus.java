package characters;

import map.KnownArea;

public class Samus {

	private int i, j;
	private KnownArea knownArea;
	
	// UP == 1 | DOWN == 2 | LEFT == 3 | RIGHT == 4
	private int direction;
	
	private int ammoLeft;
	
	public Samus(int i, int j, KnownArea knownArea) {
		this.i = i;
		this.j = j;
		this.knownArea = knownArea;
		direction = 1;
		ammoLeft = 5;
	}
	
	public void setI(int i) {
		this.i = i;
	}
	
	public void setJ(int j) {
		this.j = j;
	}
	
	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	public void tookAShot() {
		ammoLeft--;
	}
	
	public int getI() {
		return i;
	}
	
	public int getJ() {
		return j;
	}
	
	public int getDirection() {
		return direction;
	}
	
	public KnownArea getKnownArea() {
		return knownArea;
	}
	
	public int getAmmoLeft() {
		return ammoLeft;
	}
}