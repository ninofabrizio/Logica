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