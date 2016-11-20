package project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import map.Zone;

public class AStar {

	private Zone exploredMap[][];
	private Zone start;
	private Zone goal;
	
	public AStar(Zone map[][], Zone start, Zone goal) {
		exploredMap = map;
		this.start = start;
		this.goal = goal;
		
		start.setParent(null);
	}
	
	// Our Manhattan heuristic
	private int estimateDistance(Zone z1, Zone z2) {
	    return Math.abs(z1.getI() - z2.getI()) + Math.abs(z1.getJ() - z2.getJ());
	}
	
	// Calculates the cost of the zone, according to turnRight and Move actions needed
	private int getCost(Zone neighbor, Zone current) {
		
		int cost = 1;
		int direction = 0;
		
		if(current.getParent() == null)
			direction = current.getSamus().getDirection();
		else {
			if(current.getParent().getI() - 1 == current.getI() && current.getParent().getJ() == current.getJ())
				direction = 1;
			else if(current.getParent().getI() + 1 == current.getI() && current.getParent().getJ() == current.getJ())
				direction = 2;
			else if(current.getParent().getJ() - 1 == current.getJ() && current.getParent().getI() == current.getI())
				direction = 3;
			else if(current.getParent().getJ() + 1 == current.getJ() && current.getParent().getI() == current.getI())
				direction = 4;
		}
		
		if(direction == 0) {
			System.err.println("\nAStar error, non-existing direction in cost calculation");
			System.exit(1);
		}
		
		if(current.getI() == neighbor.getI() + 1 && current.getJ() == neighbor.getJ()) {
			while(direction != 1) {
				cost++;
				if(direction == 4)
					direction = 2;
				else if(direction == 2)
					direction = 3;
				else if(direction == 3)
					direction = 1;
			}
		}
		else if(current.getI() == neighbor.getI() - 1 && current.getJ() == neighbor.getJ()) {
			while(direction != 2) {
				cost++;
				if(direction == 4)
					direction = 2;
				else if(direction == 1)
					direction = 4;
				else if(direction == 3)
					direction = 1;
			}
		}
		else if(current.getJ() == neighbor.getJ() + 1 && current.getI() == neighbor.getI()) {
			while(direction != 3) {
				cost++;
				if(direction == 4)
					direction = 2;
				else if(direction == 1)
					direction = 4;
				else if(direction == 2)
					direction = 3;
			}
		}
		else if(current.getJ() == neighbor.getJ() - 1 && current.getI() == neighbor.getI()) {
			while(direction != 4) {
				cost++;
				if(direction == 3)
					direction = 1;
				else if(direction == 1)
					direction = 4;
				else if(direction == 2)
					direction = 3;
			}
		}
		
		return cost;
	}
    
	// Add the neighbors that are permitted
    public ArrayList<Zone>neighbors( Zone x ) {
    	
    	ArrayList<Zone>neighbors = new ArrayList<Zone>();
    
    	if (goal.equals(exploredMap[x.getI()][x.getJ() + 1]) || (x.getJ() + 1 < 13 && exploredMap[x.getI()][x.getJ() + 1].isVisited()
    			&& exploredMap[x.getI()][x.getJ() + 1].getEnemy() == null))
    		neighbors.add(exploredMap[x.getI()][x.getJ() + 1]);
    	
    	if (goal.equals(exploredMap[x.getI()][x.getJ() - 1]) || (x.getJ() - 1 > 0 && exploredMap[x.getI()][x.getJ() - 1].isVisited()
    			&& exploredMap[x.getI()][x.getJ() - 1].getEnemy() == null))
    		neighbors.add(exploredMap[x.getI()][x.getJ() - 1]);
    	
    	if (goal.equals(exploredMap[x.getI() + 1][x.getJ()]) || (x.getI() + 1 < 13 && exploredMap[x.getI() + 1][x.getJ()].isVisited()
    			&& exploredMap[x.getI() + 1][x.getJ()].getEnemy() == null))
    		neighbors.add(exploredMap[x.getI()  + 1][x.getJ()]);
    	
    	if (goal.equals(exploredMap[x.getI() - 1][x.getJ()]) || (x.getI() - 1 > 0 && exploredMap[x.getI() - 1][x.getJ()].isVisited()
    			&& exploredMap[x.getI() - 1][x.getJ()].getEnemy() == null))
    		neighbors.add(exploredMap[x.getI() - 1][x.getJ()]);
    	  	
    	return neighbors;
    }
    
    // Returns the list containing the minimum path between start point and goal
    public List<Zone> aStar() {
    	
    	Set<Zone> open = new HashSet<Zone>();
    	Set<Zone> closed = new HashSet<Zone>();
    	
    	// Neighbors of the current zone
    	List<Zone> neighbors = new ArrayList<Zone>();

    	start.setG(0);
    	start.setH(estimateDistance(start, goal));
    	start.setF(start.getH());

    	open.add(start);

    	while(true) {
    	        
    		Zone current = null;

    		// If this happens, it means there's no way to reach the goal.
    		if(open.size() == 0)
    			return null;
    	    
    	    for(Zone node : open)
    	    	if (current == null || node.getF() < current.getF())
    	    		current = node;
    	    
    	    if (current == goal)
    	    	break;

    	    open.remove(current);
    	    closed.add(current);

    	    neighbors = neighbors(current);
    	    
    	    for (Zone neighbor : neighbors) {

    	        int nextG = current.getG() + getCost(neighbor, current);

    	        if (nextG < neighbor.getG()) {
    	               
    	        	open.remove(neighbor);
    	            closed.remove(neighbor);
    	        }

    	        if (!open.contains(neighbor) && !closed.contains(neighbor)) {
    	        	
    	        	neighbor.setG(nextG);
    	            neighbor.setH(estimateDistance(neighbor, goal));
    	            neighbor.setF(neighbor.getG() + neighbor.getH());
    	            neighbor.setParent(current);
    	            open.add(neighbor);
    	        }    
    	    }
    	}
    	
    	// Setting the list for the actual path (head is goal, tail is start)
    	List<Zone> nodes = new ArrayList<Zone>();
    	Zone current = goal;
    	    
    	while (current.getParent() != null) {
    		
    		nodes.add(current);
    	    current = current.getParent();  	    
    	}
    	
    	nodes.add(start);

    	return nodes;
    }
}