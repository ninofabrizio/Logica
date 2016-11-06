package project;

import gui.WindowMaker;

import java.util.Map;

import map.Cave;
import map.KnownArea;

import org.jpl7.Atom;
import org.jpl7.Query;
import org.jpl7.Term;

public class GameLogic extends Thread {

	private int goldToTake = 3, waitTime;
	
	private KnownArea knownArea;
	
	public GameLogic(KnownArea knownArea, int time) {
		this.knownArea = knownArea;
		waitTime = time;
	}

	public void run() {

		Query q1 = new Query("consult", new Term[] { new Atom("logic.pl") });
		q1.hasSolution();
		
		if(isSensationZone('p', 12, 1)) {
			showDoubt('p', 12, 1);
			
			// TODO INFORM SENSATION TO PROLOG
			
		}
		if(isSensationZone('e', 12, 1)) {
			showDoubt('e', 12, 1);
			
			// TODO INFORM SENSATION TO PROLOG
			
		}
		if(isSensationZone('r', 12, 1)) {
			showDoubt('r', 12, 1);
			
			// TODO INFORM SENSATION TO PROLOG
			
		}

		while(true) {

			Query q2 = new Query("action(X)");

			Map<String, Term>[] solution = q2.allSolutions();

			// These I use to get the values of interest
			Character action = null;
			int posX = 0, posY = 0, direction = 0, health = 0, ammo = 0, score = 0;

			if (solution != null) {

				// Showing the requisition
				System.out.println(q2.toString());

				// For every solution returned by prolog
				for (int i = 0; i < solution.length; i++) {

					System.out.println("X" + i + " = " + solution[i].get("X").toString() + "\n");
					// System.out.println("X length = " + solution[i].get("X").listLength());
					// System.out.println(solution[i].get("X").arg(2).arg(2).arg(2).toString());

					// Extracting term, because, head is 1 value, body is another term
					Term term = solution[i].get("X");
					for (int j = 0; j < solution[i].get("X").listLength(); j++, term = term.arg(2)) {

						// Here enters the actions (letter)
						if (j == 0 && term.arg(1).toString().contains("'")) {
							// System.out.println(term.arg(1).toString().charAt(1));
							action = term.arg(1).toString().charAt(1);
						}
						// By being second iteration, I know that first argument is a list of [i,j] position
						else if (j == 1 /* && action == 'M' */) {
							// System.out.println(term.arg(1).arg(1).toString());
							posX = Integer.parseInt(term.arg(1).arg(1).toString());
							// System.out.println(term.arg(1).arg(2).toString());
							posY = Integer.parseInt(term.arg(1).arg(2).toString());
						}
						// And here the rest (numbers)
						else {
							// System.out.println(term.arg(1).toString());
							if (j == 2)
								direction = Integer.parseInt(term.arg(1).toString());
							else if (j == 3)
								health = Integer.parseInt(term.arg(1).toString());
							else if (j == 4)
								ammo = Integer.parseInt(term.arg(1).toString());
							else if (j == 5)
								score = Integer.parseInt(term.arg(1).toString());
						}
					}
				}

				posX = 13 - posX;
				// posY = 13 - posY;

				// System.out.println(action);
				// System.out.println(direction);
				// System.out.println(health);
				// System.out.println(ammo);
				// System.out.println(score);

				// Updating my values
				knownArea.getMyZone().getSamus().setActionsTaken();
				knownArea.getMyZone().getSamus().setScore(score);
				WindowMaker.setGameInfoText(Integer.toString(knownArea.getMyZone().getSamus().getScore()),
											Integer.toString(knownArea.getMyZone().getSamus().getActionsTaken()));

				if(Cave.getZones()[posX][posY].getType() != 'W' && action == 'M') {
					knownArea.getMyZone().getSamus().setI(posX);
					knownArea.getMyZone().getSamus().setJ(posY);
					
					knownArea.getExploredMap()[posX][posY].setSamus(knownArea.getMyZone().getSamus());
					knownArea.getMyZone().setSamus(null);
					knownArea.setMyZone(knownArea.getExploredMap()[posX][posY]);
					knownArea.getMyZone().setVisited();
				}
				
				knownArea.getMyZone().getSamus().setHealth(health);
				WindowMaker.setLifeBarValue(knownArea.getMyZone().getSamus().getHealth());
					
				knownArea.getMyZone().getSamus().setDirection(direction);
					
				knownArea.getMyZone().getSamus().setAmmo(ammo);
				WindowMaker.setAmmoPanelValue(knownArea.getMyZone().getSamus().getAmmo());
					
				if (action == 'G') {
					knownArea.getMyZone().setType('.');
					
					if(Cave.getZones()[posX][posY].getType() == 'O') {
						Cave.getZones()[posX][posY].setType('o');
						goldToTake--;
					}
					else if(Cave.getZones()[posX][posY].getType() == 'U')
						Cave.getZones()[posX][posY].setType('h');
				}
				else if(action == 'M')
					if(treatMovement(posX, posY))
						break;
				else if (action == 'S') {
					
					if(isValidShot(posX, posY)) {
						
						int enemyX = getEnemyXPos(posX, posY);
						int enemyY = getEnemyYPos(posX, posY);
						
						if(damageEnemy(enemyX, enemyY, knownArea.getMyZone().getSamus().tookAShot())) {
							
							char doubt = 0;
							
							if(Cave.getZones()[enemyX][enemyY].getType() == 'd') {
								Cave.getZones()[enemyX][enemyY].setType('p');
								doubt = 'e';
							}
							else if(Cave.getZones()[enemyX][enemyY].getType() == 'D') {
								Cave.getZones()[enemyX][enemyY].setType('m');
								doubt = 'e';
							}
							else if(Cave.getZones()[enemyX][enemyY].getType() == 'T') {
								Cave.getZones()[enemyX][enemyY].setType('r');
								doubt = 'r';
							}
							
							knownArea.getExploredMap()[enemyX][enemyY].setType('.');
							
							Cave.getZones()[enemyX][enemyY].setEnemy(null);
							knownArea.getExploredMap()[enemyX][enemyY].setEnemy(null);
							
							updateDoubts(posX, posY, doubt);
							
							// TODO INFORM PROLOG SCREAM SENSATION
							// PROLOG SHOULD UPDATE DANGERS AND/OR DOUBTS
							
						}
					}
				}

				knownArea.repaint();
				
				if(goldToTake == 0) {
					
					// TODO CALL METHOD TO SEARCH FOR EXIT (ASTAR), WITH IT'S OWN LOOP UNTIL GO UP ACTION
					
					break;
				}
			}

			try {
				sleep(waitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// Returns true if game has to stop
	private boolean treatMovement(int posX, int posY) {

		if(Cave.getZones()[posX][posY].getType() == 'O') {
			
			// TODO PUT GOLD SENSATION IN PROLOG, IF NOT ALREADY THERE
			
			knownArea.getMyZone().setType('O');
		}
		else if(Cave.getZones()[posX][posY].getType() == 'H') {
			
			// TODO PUT EXTRA HEALTH FACT IN PROLOG, IF NOT ALREADY THERE
			
			knownArea.getMyZone().setType('H');
		}
		else if(isSensationZone('e', posX, posY)) {
			
			// TODO PUT DAMAGE MONSTER SENSATION IN PROLOG, IF NOT ALREADY THERE
			
			showDoubt('e', posX, posY);
		}
		else if(isSensationZone('p', posX, posY)) {
			
			// TODO PUT HOLE SENSATION IN PROLOG, IF NOT ALREADY THERE
			
			showDoubt('p', posX, posY);
		}
		else if(isSensationZone('r', posX, posY)) {
			
			// TODO PUT TELEPORT SENSATION IN PROLOG, IF NOT ALREADY THERE
			
			showDoubt('r', posX, posY);
		}
		else if(Cave.getZones()[posX][posY].getType() == 'P') {
			
			knownArea.getMyZone().getSamus().setScore(knownArea.getMyZone().getSamus().getScore() - 1000);
			WindowMaker.setGameInfoText(Integer.toString(knownArea.getMyZone().getSamus().getScore()),
															Integer.toString(knownArea.getMyZone().getSamus().getActionsTaken()));
			knownArea.getMyZone().getSamus().setHealth(0);
			WindowMaker.setLifeBarValue(knownArea.getMyZone().getSamus().getHealth());
			
			if(knownArea.getMyZone().getType() != 'P')
				knownArea.getMyZone().setType('P');
			
			updateDoubts(posX, posY, 'p');
			
			knownArea.repaint();
			return true;
		}
		else if(Cave.getZones()[posX][posY].getType() == 'd' || Cave.getZones()[posX][posY].getType() == 'D') {
			
			char e;
			int d;
			if(Cave.getZones()[posX][posY].getType() == 'd') {
				e = 'd';
				d = 20;
			}
			else {
				e = 'D';
				d = 50;
			}
			
			if(knownArea.getMyZone().getType() != e) {
				knownArea.getMyZone().setType(e);
				knownArea.getMyZone().setEnemy(Cave.getZones()[posX][posY].getEnemy());
			}
			
			knownArea.getMyZone().getSamus().setScore(knownArea.getMyZone().getSamus().getScore() - d);
			WindowMaker.setGameInfoText(Integer.toString(knownArea.getMyZone().getSamus().getScore()),
															Integer.toString(knownArea.getMyZone().getSamus().getActionsTaken()));
			knownArea.getMyZone().getSamus().setHealth(knownArea.getMyZone().getSamus().getHealth() - d);
			
			updateDoubts(posX, posY, 'e');
			
			if(knownArea.getMyZone().getSamus().getHealth() <= 0) {
				
				knownArea.getMyZone().getSamus().setHealth(0);
				WindowMaker.setLifeBarValue(knownArea.getMyZone().getSamus().getHealth());
				
				knownArea.getMyZone().getSamus().setScore(knownArea.getMyZone().getSamus().getScore() - 1000);
				WindowMaker.setGameInfoText(Integer.toString(knownArea.getMyZone().getSamus().getScore()),
																Integer.toString(knownArea.getMyZone().getSamus().getActionsTaken()));
				knownArea.repaint();
				return true;
			}
			
			WindowMaker.setLifeBarValue(knownArea.getMyZone().getSamus().getHealth());
			
			// TODO UPDATE CHAR INFO INTO PROLOG
			// INFORM PROLOG ABOUT DANGER, IF IT'S NOT A FACT ALREADY
			// INFORM PROLOG THERE'S NO DOUBTS ANYMORE, IF THERE WERE ANY ABOUT THIS DANGER
			
			if(knownArea.getMyZone().getSamus().getHealth() <= 80){
				
				// TODO CALL METHOD TO SEARCH FOR HEALTH (ASTAR), WITH IT'S OWN LOOP UNTIL THE HEALTH IS REACHED
				
			}
		}
		else if(Cave.getZones()[posX][posY].getType() == 'T') {
			
			if(knownArea.getMyZone().getType() != 'T') {
				knownArea.getMyZone().setType('T');
				knownArea.getMyZone().setEnemy(Cave.getZones()[posX][posY].getEnemy());
			}
			
			updateDoubts(posX, posY, 'r');
			
			knownArea.getMyZone().getEnemy().generateRandomPosition(knownArea.getMyZone());
			
			knownArea.getExploredMap()[knownArea.getMyZone().getSamus().getI()]
					[knownArea.getMyZone().getSamus().getJ()].setSamus(knownArea.getMyZone().getSamus());
			knownArea.setMyZone(knownArea.getExploredMap()[knownArea.getMyZone().getSamus().getI()]
					[knownArea.getMyZone().getSamus().getJ()]);
			knownArea.getMyZone().setVisited();
			knownArea.getExploredMap()[posX][posY].setSamus(null);
			
			// TODO UPDATE CHAR INFO INTO PROLOG
			// INFORM PROLOG ABOUT DANGER, IF IT'S NOT A FACT ALREADY
			// INFORM PROLOG THERE'S NO DOUBTS ANYMORE, IF THERE WERE ANY ABOUT THIS DANGER
			
			// Treat new position
			if(treatMovement(knownArea.getMyZone().getI(),knownArea.getMyZone().getJ()))
				return true;
		}
		else if(Cave.getZones()[posX][posY].getType() == 'W') {
			
			if(knownArea.getMyZone().getType() != 'W')
				knownArea.getMyZone().setType('W');
			
			// TODO INFORM PROLOG ABOUT WALL SENSATION, IF IT'S NOT A FACT ALREADY
			// TELL PROLOG TO GET BACK TO LAST POSITION
		}
		
		knownArea.repaint();
		return false;
	}

	// This method put doubts into the known area matrix
	private void showDoubt(char c, int posX, int posY) {

		if(knownArea.getExploredMap()[posX+1][posY].getType() == 'u')
			knownArea.getExploredMap()[posX+1][posY].setType(c);
		if(knownArea.getExploredMap()[posX-1][posY].getType() == 'u')
			knownArea.getExploredMap()[posX-1][posY].setType(c);
		if(knownArea.getExploredMap()[posX][posY-1].getType() == 'u')
			knownArea.getExploredMap()[posX][posY-1].setType(c);
		if(knownArea.getExploredMap()[posX][posY+1].getType() == 'u')
			knownArea.getExploredMap()[posX][posY+1].setType(c);
	}

	// This method clears doubts from the known area matrix
	private void updateDoubts(int i, int j, char type) {
		
		if(knownArea.getExploredMap()[i+1][j+1].getType() == type)
			knownArea.getExploredMap()[i+1][j+1].setType('u');
		if(knownArea.getExploredMap()[i-1][j+1].getType() == type)
			knownArea.getExploredMap()[i-1][j+1].setType('u');
		if(knownArea.getExploredMap()[i-1][j-1].getType() == type)
			knownArea.getExploredMap()[i-1][j-1].setType('u');
		if(knownArea.getExploredMap()[i+1][j-1].getType() == type)
			knownArea.getExploredMap()[i+1][j-1].setType('u');
		
		if((i - 2 >= 0) && knownArea.getExploredMap()[i-2][j].getType() == type)
			knownArea.getExploredMap()[i-2][j].setType('u');
		if((i + 2 <= 13) && knownArea.getExploredMap()[i+2][j].getType() == type)
			knownArea.getExploredMap()[i+2][j].setType('u');
		if((j - 2 >= 0) && knownArea.getExploredMap()[i][j-2].getType() == type)
			knownArea.getExploredMap()[i][j-2].setType('u');
		if((j + 2 <= 13) && knownArea.getExploredMap()[i][j+2].getType() == type)
			knownArea.getExploredMap()[i][j+2].setType('u');
	}
	
	// This method says if the area shot has an enemy
	private boolean isValidShot(int posX, int posY) {
		
		if((knownArea.getMyZone().getSamus().getDirection() == 1 && Cave.getZones()[posX+1][posY].getEnemy() != null)
			|| (knownArea.getMyZone().getSamus().getDirection() == 2 && Cave.getZones()[posX-1][posY].getEnemy() != null)
			|| (knownArea.getMyZone().getSamus().getDirection() == 3 && Cave.getZones()[posX][posY-1].getEnemy() != null)
			|| (knownArea.getMyZone().getSamus().getDirection() == 4 && Cave.getZones()[posX][posY+1].getEnemy() != null))
			return true;
		
		return false;
	}
	
	private int getEnemyXPos(int posX, int posY) {
		
		if(knownArea.getMyZone().getSamus().getDirection() == 3 || knownArea.getMyZone().getSamus().getDirection() == 4)
			return posX;
		else if(knownArea.getMyZone().getSamus().getDirection() == 1)
			return posX-1;
		else
			return posX+1;
	}
	
	private int getEnemyYPos(int posX, int posY) {
		
		if(knownArea.getMyZone().getSamus().getDirection() == 1 || knownArea.getMyZone().getSamus().getDirection() == 2)
			return posY;
		else if(knownArea.getMyZone().getSamus().getDirection() == 3)
			return posY-1;
		else
			return posY+1;
	}
	
	// Returns true if enemy dies
	private boolean damageEnemy(int posX, int posY, int damage) {
			
		Cave.getZones()[posX][posY].getEnemy().setHealth(Cave.getZones()[posX][posY].getEnemy().getHealth()-damage);
		if(Cave.getZones()[posX][posY].getEnemy().getHealth() <= 0)
			return true;
			
		return false;
	}
	
	private boolean isSensationZone(char c, int posX, int posY) {
		
		if(c == 'e') {
			if(Cave.getZones()[posX+1][posY].getType() == 'd' || Cave.getZones()[posX-1][posY].getType() == 'd'
				|| Cave.getZones()[posX][posY-1].getType() == 'd' || Cave.getZones()[posX][posY+1].getType() == 'd'
				|| Cave.getZones()[posX+1][posY].getType() == 'D' || Cave.getZones()[posX-1][posY].getType() == 'D'
				|| Cave.getZones()[posX][posY-1].getType() == 'D' || Cave.getZones()[posX][posY+1].getType() == 'D')
				return true;
		}
		else if(c == 'p') {
			if(Cave.getZones()[posX+1][posY].getType() == 'P' || Cave.getZones()[posX-1][posY].getType() == 'P'
				|| Cave.getZones()[posX][posY-1].getType() == 'P' || Cave.getZones()[posX][posY+1].getType() == 'P')
				return true;
		}
		else if(c == 'r') {
			if(Cave.getZones()[posX+1][posY].getType() == 'T' || Cave.getZones()[posX-1][posY].getType() == 'T'
				|| Cave.getZones()[posX][posY-1].getType() == 'T' || Cave.getZones()[posX][posY+1].getType() == 'T')
				return true;
		}
		
		return false;
	}
}