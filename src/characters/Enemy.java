package characters;

import java.util.concurrent.ThreadLocalRandom;

import map.Zone;

public class Enemy {

	private int i, j, health;
	
	public Enemy(int i, int j) {
		
		this.i = i;
		this.j = j;
		health = 100;
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
	
	public int getHealth() {
		return health;
	}
	
	// Only for Ridley (teleport enemy)
	public void generateRandomPosition(Zone samusZone) {
		
		int i = samusZone.getI(), j = samusZone.getJ();
		while(i == samusZone.getI() && j == samusZone.getJ()) {
			i = ThreadLocalRandom.current().nextInt(1, 12 + 1);
			j = ThreadLocalRandom.current().nextInt(1, 12 + 1);
		}
		
		samusZone.getSamus().setI(i);
		samusZone.getSamus().setJ(j);
	}
}