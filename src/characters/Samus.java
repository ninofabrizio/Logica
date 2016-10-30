package characters;

import java.util.ArrayList;

import map.KnownArea;
import map.Zone;

public class Samus {

	private int i, j;
	private KnownArea knownArea;
	private int health;
	private int score;
	private int actionsTaken;
	
	// UP == 1 | DOWN == 2 | LEFT == 3 | RIGHT == 4
	private int direction;
	
	private int ammoLeft;
	
	public Samus(int i, int j, KnownArea knownArea) {
		this.i = i;
		this.j = j;
		this.knownArea = knownArea;
		health = 100;
		direction = 1;
		ammoLeft = 5;
		score = 0;
		actionsTaken = 0;
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
	
	public void setScore(int s) {
		score += s;
	}
	
	public void setActionsTaken(int a) {
		actionsTaken += a;
	}
	
	public void setHealth(int h) {
		health += h;
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
	
	public int getScore() {
		return score;
	}
	
	public int getActionsTaken() {
		return actionsTaken;
	}
	
	public int getHealth() {
		return health;
	}

	// Method to call prolog file and check what Samus feels, updating knownArea
	// TODO Idea: creates an ArrayList of the neighbor zones AND where she is, saying what she feels
	public void feelNeighbors() {
		// TODO NOT FINISHED
		
		// The size must be 5 (4 neighbors, and where she stands), add first the zone where she is
		ArrayList<Zone> neighborZones = new ArrayList<Zone>();
		
		// VERIFY HERE WHAT PROLOG SENT BACK TO SEE IF IT'S POSSIBLE, THEN CALL METHOD TO UPDATE THE MAP
		// Be sure that the positions [i,j] sent are valid to our matrix indexes
		//knownArea.updateMap(neighborZones);
	}
}