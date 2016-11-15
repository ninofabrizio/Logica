package map;

import characters.Enemy;
import characters.Samus;

public class Zone {

	private int i, j;
	private char type;
	private Samus samus;
	private Enemy enemy;
	private boolean visited = false, stepSounds = false, breeze = false, flash = false,
					damageEnemyDoubt = false, holeDoubt = false, teleportEnemyDoubt = false;
	
	// For AStar usage
	private int g = -1; // G
    private int f = -1; // G + H
	private int h;
	private Zone parent;
    
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
	
	public void setEnemy(Enemy e) {
		enemy = e;
	}
	
	public void setVisited() {
		visited = true;
	}
	
	public void setStepSounds(boolean condition) {
		stepSounds = condition;
	}

	public void setBreeze(boolean condition) {
		breeze = condition;
	}

	public void setFlash(boolean condition) {
		flash = condition;
	}
	
	public void setDamageEnemyDoubt(boolean condition) {
		damageEnemyDoubt = condition;
	}

	public void setHoleDoubt(boolean condition) {
		holeDoubt = condition;
	}

	public void setTeleportEnemyDoubt(boolean condition) {
		teleportEnemyDoubt = condition;
	}
	
	public void setG(int g) {
		this.g = g;
	}
	
	public void setF(int f) {
		this.f = f;
	}
	
	public void setH(int h) {
		this.h = h;
	}
	
	public void setParent(Zone parent) {
		this.parent = parent;
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
	
	public Enemy getEnemy() {
		return enemy;
	}
	
	public int getG() {
		return g;
	}
	
	public int getF() {
		return f;
	}
	
	public int getH() {
		return h;
	}
	
	public Zone getParent() {
		return parent;
	}
	
	public boolean isVisited() {
		return visited;
	}
	
	public boolean isStepSounds() {
		return stepSounds;
	}

	public boolean isBreeze() {
		return breeze;
	}

	public boolean isFlash() {
		return flash;
	}
	
	public boolean isDamageEnemyDoubt() {
		return damageEnemyDoubt;
	}

	public boolean isHoleDoubt() {
		return holeDoubt;
	}

	public boolean isTeleportEnemyDoubt() {
		return teleportEnemyDoubt;
	}
}