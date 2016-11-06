package map;

import characters.Enemy;
import characters.Samus;

public class Zone {

	private char type;
	private Samus samus;
	private Enemy enemy;
	private boolean visited = false;
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
	
	public void setEnemy(Enemy e) {
		enemy = e;
	}
	
	public void setVisited() {
		visited = true;
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
}