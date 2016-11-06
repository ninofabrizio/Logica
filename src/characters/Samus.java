package characters;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import map.KnownArea;
import map.Zone;

public class Samus {

	private int i, j, health, score, actionsTaken, ammo;
	private KnownArea knownArea;
	
	// UP == 1 | DOWN == 2 | LEFT == 3 | RIGHT == 4
	private int direction;
	
	public Samus(int i, int j, KnownArea knownArea) {
		this.i = i;
		this.j = j;
		this.knownArea = knownArea;
		health = 100;
		direction = 1;
		ammo = 5;
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
	
	public void setAmmo(int a) {
		ammo = a;
	}
	
	public void setScore(int s) {
		score = s;
	}
	
	public void setActionsTaken() {
		actionsTaken++;
	}
	
	public void setHealth(int h) {
		health = h;
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
	
	public int getAmmo() {
		return ammo;
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

	// Returns random damage done
	public int tookAShot() {
		return ThreadLocalRandom.current().nextInt(20, 50 + 1);
	}
}