package project;

import gui.WindowMaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import map.Cave;
import map.KnownArea;
import map.Zone;

import org.jpl7.Atom;
import org.jpl7.Query;
import org.jpl7.Term;

import characters.Enemy;

public class GameLogic extends Thread {

	private int goldLeftToTake = 3, waitTime;
	
	private KnownArea knownArea;
	
	public GameLogic(KnownArea knownArea, int time) {
		this.knownArea = knownArea;
		waitTime = time;
	}

	public void run() {

		List<Zone> aStarPath = null;
		
		Query q1 = new Query("consult", new Term[] { new Atom("logic.pl") });
		q1.hasSolution();
		
		if(isSensationZone('p', 12, 1)) {
			//showDoubt('p', 12, 1);
			
			// TODO INFORM SENSATION TO PROLOG
			sendKnowledgeToProlog(13-12,1,"breeze([",".");
			getKnowledgeFromProlog("breeze");
			knownArea.repaint();
			
			try {
				sleep(waitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(isSensationZone('e', 12, 1)) {
			//showDoubt('e', 12, 1);
			
			// TODO INFORM SENSATION TO PROLOG
			sendKnowledgeToProlog(13-12,1,"sound([",".");
			getKnowledgeFromProlog("sound");
			knownArea.repaint();
			
			try {
				sleep(waitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(isSensationZone('r', 12, 1)) {
			//showDoubt('r', 12, 1);
			
			// TODO INFORM SENSATION TO PROLOG
			sendKnowledgeToProlog(13-12,1,"flash([",".");
			getKnowledgeFromProlog("flash");
			getKnowledgeFromProlog("doubt");
			knownArea.repaint();
			
			try {
				sleep(waitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		while(true) {

			Query q2;
			
			if(aStarPath == null)
				q2 = new Query("action(X)");
			else
				q2 = new Query("aStarAction(X)");

			Map<String, Term>[] solution = q2.allSolutions();

			// These I use to get the values of interest
			Character action = null;
			int posX = 0, posY = 0, direction = 0, health = 0, ammo = 0, score = 0;

			if (solution != null) {

				// Showing the requisition
				System.out.println(q2.toString());

				// For every solution returned by prolog
				for (int i = 0; i < solution.length; i++) {

					System.out.println("X" + " = " + solution[i].get("X").toString() + "\n");
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
				
				getKnowledgeFromProlog("danger");
				getKnowledgeFromProlog("sound");
				getKnowledgeFromProlog("flash");
				getKnowledgeFromProlog("breeze");
				getKnowledgeFromProlog("doubt");
				getKnowledgeFromProlog("bump");
				getKnowledgeFromProlog("glitter");
				getKnowledgeFromProlog("power_up");
				getKnowledgeFromProlog("visited");
				
				knownArea.repaint();
				
				if (action == 'G') {
					knownArea.getMyZone().setType('.');
					
					if(Cave.getZones()[posX][posY].getType() == 'O') {
						Cave.getZones()[posX][posY].setType('o');
						goldLeftToTake--;
					}
					else if(Cave.getZones()[posX][posY].getType() == 'U')
						Cave.getZones()[posX][posY].setType('h');
				}
				else if(action == 'M') {
					if(treatMovement(posX, posY)) {
						knownArea.repaint();
						break;
					}
				}
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
							
							//updateDoubts(posX, posY, doubt);
							
							// TODO INFORM PROLOG SCREAM SENSATION
							// PROLOG SHOULD UPDATE DANGERS AND/OR DOUBTS
							sendKnowledgeToProlog(13-posX,posY,"scream([",".");
						}
					}
				}
				else if(action == 'C')
					break;
				
				knownArea.repaint();
				
				
				Query q3 = new Query("toVisit([X | Y])");
				solution = q3.allSolutions();
				
				for (int i = 0; i < solution.length; i++)
					System.out.println("\nTo Visit:\nI: " + solution[i].get("X").toString() + "\nJ: " + solution[i].get("Y").toString() + "\n");
				
				
				
				// Dealing with AStar situation
				if(aStarPath == null) {
					if(goldLeftToTake == 0) {
						AStar star = new AStar(knownArea.getExploredMap(), knownArea.getMyZone(), knownArea.getExploredMap()[12][1]);
						aStarPath = star.aStar();
						
						if(aStarPath != null && aStarPath.size() > 1) {
							aStarPath.remove(aStarPath.size()-1); // Getting rid of position she's in
							Zone nextDestination = aStarPath.remove(aStarPath.size()-1);
							sendKnowledgeToProlog(13-nextDestination.getI(),nextDestination.getJ(),"nextDestination([",".");
						}
						else
							aStarPath = null;
					}
					else if(knownArea.getMyZone().getSamus().getHealth() <= 80) {
						
						List<Zone> closestPath = null;
						
						// Looking for known power-ups
						for(int i = 1, bestPathCost = 0; i < 13; i++) {
							for(int j = 1; j < 13; j++) {
								if(knownArea.getExploredMap()[i][j].getType() == 'U') {
									
									AStar star = new AStar(knownArea.getExploredMap(), knownArea.getMyZone(), knownArea.getExploredMap()[i][j]);
									
									if(closestPath == null) {
										closestPath = star.aStar();
										bestPathCost = closestPath.get(0).getF();
									}
									else {
										List<Zone> candidatePath = star.aStar();
										
										if(candidatePath.get(0).getF() < bestPathCost) {
											closestPath = candidatePath;
											bestPathCost = candidatePath.get(0).getF();
										}
									}
									
									for(int k = 1; k < 13; k++) {
										for(int l = 1; l < 13; l++) {
											knownArea.getExploredMap()[k][l].setF(-1);
											knownArea.getExploredMap()[k][l].setG(-1);
											knownArea.getExploredMap()[k][l].setParent(null);
										}
									}
								}
							}
						}
						
						aStarPath = closestPath;
						
						if(aStarPath != null && aStarPath.size() > 1) {
							aStarPath.remove(aStarPath.size()-1); // Getting rid of position she's in
							Zone nextDestination = aStarPath.remove(aStarPath.size()-1);
							sendKnowledgeToProlog(13-nextDestination.getI(),nextDestination.getJ(),"nextDestination([",".");
						}
						else
							aStarPath = null;
					}
				}
				else if(!aStarPath.isEmpty()) {
					if(action == 'M') {
						Zone nextDestination = aStarPath.remove(aStarPath.size()-1);
						sendKnowledgeToProlog(13-nextDestination.getI(),nextDestination.getJ(),"nextDestination([",".");
					}
				}
				else if(action == 'M') {
					aStarPath = null;
					
					// In case AStar is called again
					for(int i = 1; i < 13; i++) {
						for(int j = 1; j < 13; j++) {
							knownArea.getExploredMap()[i][j].setF(-1);
							knownArea.getExploredMap()[i][j].setG(-1);
							knownArea.getExploredMap()[i][j].setParent(null);
						}
					}
				}
			}
			else {
				System.err.println("JAVA -> PROLOG COMMUNICATION PROBLEM");
				System.exit(1);
			}

			try {
				sleep(waitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void getKnowledgeFromProlog(String string) {
		
		int posX, posY;
		char type;
		
		String command = null;
		
		for(int i = 0; i < 14; i++) {
			for(int j = 0; j < 14; j++) {
				if(string.equals("danger")){
					if(knownArea.getExploredMap()[i][j].getEnemy() != null) {
						knownArea.getExploredMap()[i][j].setEnemy(null);
					
						if(knownArea.getExploredMap()[i][j].isVisited())
							knownArea.getExploredMap()[i][j].setType('.');
						else
							knownArea.getExploredMap()[i][j].setType('n');
					}
				}
				else if(string.equals("doubt")){
					knownArea.getExploredMap()[i][j].setDamageEnemyDoubt(false);
					knownArea.getExploredMap()[i][j].setHoleDoubt(false);
					knownArea.getExploredMap()[i][j].setTeleportEnemyDoubt(false);
				}
				else if(string.equals("sound") && knownArea.getExploredMap()[i][j].isStepSounds())
					knownArea.getExploredMap()[i][j].setStepSounds(false);
				else if(string.equals("flash")  && knownArea.getExploredMap()[i][j].isFlash())
					knownArea.getExploredMap()[i][j].setFlash(false);
				else if(string.equals("breeze") && knownArea.getExploredMap()[i][j].isBreeze())
					knownArea.getExploredMap()[i][j].setBreeze(false);
				else if(string.equals("bump") && knownArea.getExploredMap()[i][j].getType() == 'W')
					knownArea.getExploredMap()[i][j].setType('w');
				else if(string.equals("glitter") && knownArea.getExploredMap()[i][j].getType() == 'O')
					knownArea.getExploredMap()[i][j].setType('.');
				else if(string.equals("power_up") && knownArea.getExploredMap()[i][j].getType() == 'U')
					knownArea.getExploredMap()[i][j].setType('.');
			}
		}
		
		if(string.contains("danger") || string.contains("doubt"))
			command = string.concat("([I | J], T)");
		else
			command = string.concat("([I | J])");
		
		Query q2 = new Query(command);
		
		Map<String, Term>[] solution = q2.allSolutions();
		
		if (solution != null) {
			
			if(solution.length == 0) {
				System.out.println(q2.toString());
				System.out.println("EMPTY SOLUTION\n");
			}
			
			for(int i = 0; i < solution.length; i++) {
				
				System.out.println(q2.toString());
				System.out.println("I" + " = " + solution[i].get("I").toString());
				System.out.println("J" + " = " + solution[i].get("J").toString());
				
				posX = Integer.parseInt(solution[i].get("I").toString());
				posY = Integer.parseInt(solution[i].get("J").toString());
				
				posX = 13 - posX;
				
				if(string.contains("danger") || string.contains("doubt")) {
					
					System.out.println("T" + " = " + solution[i].get("T").toString() + "\n");
					
					if(!solution[i].get("T").toString().contains("d"))
						type = solution[i].get("T").toString().charAt(1);
					else
						type = solution[i].get("T").toString().charAt(0);
						
					if(type == 'P' && string.contains("danger")) {
						knownArea.getExploredMap()[posX][posY].setHoleDoubt(false);
						knownArea.getExploredMap()[posX][posY].setType('P');
					}
					else if(type == 'P' && string.contains("doubt"))
						knownArea.getExploredMap()[posX][posY].setHoleDoubt(true);
					else if(type == 'd') {
						knownArea.getExploredMap()[posX][posY].setType('d');
						knownArea.getExploredMap()[posX][posY].setDamageEnemyDoubt(false);
						if(Cave.getZones()[posX][posY].getEnemy() != null)
							knownArea.getExploredMap()[posX][posY].setEnemy(Cave.getZones()[posX][posY].getEnemy());
						else // TODO THIS SHOULDN'T BE HERE!
							knownArea.getExploredMap()[posX][posY].setEnemy(new Enemy(posX, posY));
					}
					else if(type == 'D') {
						knownArea.getExploredMap()[posX][posY].setType('D');
						knownArea.getExploredMap()[posX][posY].setDamageEnemyDoubt(false);
						if(Cave.getZones()[posX][posY].getEnemy() != null)
							knownArea.getExploredMap()[posX][posY].setEnemy(Cave.getZones()[posX][posY].getEnemy());
						else // TODO THIS SHOULDN'T BE HERE!
							knownArea.getExploredMap()[posX][posY].setEnemy(new Enemy(posX, posY));
					}
					else if((type == 'd' || type == 'D') && string.contains("doubt"))
						knownArea.getExploredMap()[posX][posY].setDamageEnemyDoubt(true);
					else if(type == 'T') {
						knownArea.getExploredMap()[posX][posY].setType('T');
						knownArea.getExploredMap()[posX][posY].setTeleportEnemyDoubt(false);
						if(Cave.getZones()[posX][posY].getEnemy() != null)
							knownArea.getExploredMap()[posX][posY].setEnemy(Cave.getZones()[posX][posY].getEnemy());
						else // TODO THIS SHOULDN'T BE HERE!
							knownArea.getExploredMap()[posX][posY].setEnemy(new Enemy(posX, posY));
					}
					else if(type == 'T' && string.contains("doubt"))
						knownArea.getExploredMap()[posX][posY].setTeleportEnemyDoubt(true);
				}
				else {
					
					System.out.println();
					
					if(string.contains("sound"))
						knownArea.getExploredMap()[posX][posY].setStepSounds(true);
					else if(string.contains("flash"))
						knownArea.getExploredMap()[posX][posY].setFlash(true);
					else if(string.contains("breeze"))
						knownArea.getExploredMap()[posX][posY].setBreeze(true);
					else if(string.contains("bump"))
						knownArea.getExploredMap()[posX][posY].setType('W');
					else if(string.contains("glitter"))
						knownArea.getExploredMap()[posX][posY].setType('O');
					else if(string.contains("power_up"))
						knownArea.getExploredMap()[posX][posY].setType('U');
					else if(string.contains("visited")) {
						if(posX > 0 && posX < 13 && posY > 0 && posY < 13) {
							knownArea.getExploredMap()[posX][posY].setVisited();
							if(knownArea.getExploredMap()[posX][posY].getType() == 'n')
								knownArea.getExploredMap()[posX][posY].setType('.');
						}
					}
				}
			}
		}
		else {
			System.err.println("JAVA -> PROLOG COMMUNICATION PROBLEM");
			System.exit(1);
		}
	}

	// Here we create facts in our prolog file
	private void sendKnowledgeToProlog(int i, int j, String name, String type) {
		
		Query q2 = null;
		
		if(name.contains("danger")) {
			
			String command = new String("retract(").concat(name);
			command = command.concat(Integer.toString(i));
			command = command.concat("|");
			command = command.concat(Integer.toString(j));
			command = command.concat("],\'");
			command = command.concat(type);
			command = command.concat("\'))");
			
			q2 = new Query(command);
			Map<String, Term>[] solution = q2.allSolutions();
			System.out.println(q2.toString() + "\nSolutions: " + solution.length + "\n");
			
			command = new String("assert(").concat(name);
			command = command.concat(Integer.toString(i));
			command = command.concat("|");
			command = command.concat(Integer.toString(j));
			command = command.concat("],\'");
			command = command.concat(type);
			command = command.concat("\'))");
			
			q2 = new Query(command);
		}
		else if(name.contains("doubt")) {
			
			if(type.equals("e")) {
				String command = new String("retract(").concat(name);
				command = command.concat(Integer.toString(i));
				command = command.concat("|");
				command = command.concat(Integer.toString(j));
				command = command.concat("],\'d\'))");
			
				q2 = new Query(command);
				Map<String, Term>[] solution = q2.allSolutions();
				System.out.println(q2.toString() + "\nSolutions: " + solution.length + "\n");
				
				command = new String("retract(").concat(name);
				command = command.concat(Integer.toString(i));
				command = command.concat("|");
				command = command.concat(Integer.toString(j));
				command = command.concat("],\'D\'))");
			
				q2 = new Query(command);
			}
			else {
				String command = new String("retract(").concat(name);
				command = command.concat(Integer.toString(i));
				command = command.concat("|");
				command = command.concat(Integer.toString(j));
				command = command.concat("],\'");
				command = command.concat(type);
				command = command.concat("\'))");
			
				q2 = new Query(command);
			}
		}
		else if(name.contains("samus")) {
			
			q2 = new Query("retract(samus(_,_,_,_,_))");
			Map<String, Term>[] solution = q2.allSolutions();
			System.out.println(q2.toString() + "\nSolutions: " + solution.length + "\n");
			
			String command = new String("assert(").concat(name);
			command = command.concat(Integer.toString(13-knownArea.getMyZone().getSamus().getI()));
			command = command.concat("|");
			command = command.concat(Integer.toString(knownArea.getMyZone().getSamus().getJ()));
			command = command.concat("],");
			command = command.concat(Integer.toString(knownArea.getMyZone().getSamus().getDirection()));
			command = command.concat(",");
			command = command.concat(Integer.toString(knownArea.getMyZone().getSamus().getHealth()));
			command = command.concat(",");
			command = command.concat(Integer.toString(knownArea.getMyZone().getSamus().getAmmo()));
			command = command.concat(",");
			command = command.concat(Integer.toString(knownArea.getMyZone().getSamus().getScore()));
			command = command.concat("))");
			
			q2 = new Query(command);
		}
		else if(name.contains("nextDestination")) {
			
			String command = new String("assert(").concat(name);
			command = command.concat(Integer.toString(i));
			command = command.concat("|");
			command = command.concat(Integer.toString(j));
			command = command.concat("]))");
			
			q2 = new Query(command);
		}
		else {
			
			String command = new String("retract(").concat(name);
			command = command.concat(Integer.toString(i));
			command = command.concat("|");
			command = command.concat(Integer.toString(j));
			command = command.concat("]))");
			
			q2 = new Query(command);
			Map<String, Term>[] solution = q2.allSolutions();
			System.out.println(q2.toString() + "\nSolution: " + solution.length + "\n");
			
			command = new String("assert(").concat(name);
			command = command.concat(Integer.toString(i));
			command = command.concat("|");
			command = command.concat(Integer.toString(j));
			command = command.concat("]))");
		
			q2 = new Query(command);
		}
		
		if(q2.allSolutions() == null) {
			System.err.println("JAVA -> PROLOG COMMUNICATION PROBLEM");
			System.exit(1);
		}
		
		System.out.println(q2.toString() + "\n");
	}

	// Returns true if game has to stop
	private boolean treatMovement(int posX, int posY) {
		
		if((posX > 0 && posX < 13) && (posY > 0 && posY < 13) && isSensationZone('e', posX, posY)) {
			
			// TODO PUT DAMAGE MONSTER SENSATION IN PROLOG, IF NOT ALREADY THERE
			sendKnowledgeToProlog(13-posX,posY,"sound([",".");
			getKnowledgeFromProlog("sound");
			
			//showDoubt('e', posX, posY);
		}
		if((posX > 0 && posX < 13) && (posY > 0 && posY < 13) && isSensationZone('p', posX, posY)) {
			
			// TODO PUT HOLE SENSATION IN PROLOG, IF NOT ALREADY THERE
			sendKnowledgeToProlog(13-posX,posY,"breeze([",".");
			getKnowledgeFromProlog("breeze");
			
			//showDoubt('p', posX, posY);
		}
		if((posX > 0 && posX < 13) && (posY > 0 && posY < 13) && isSensationZone('r', posX, posY)) {
			
			// TODO PUT TELEPORT SENSATION IN PROLOG, IF NOT ALREADY THERE
			sendKnowledgeToProlog(13-posX,posY,"flash([",".");
			getKnowledgeFromProlog("flash");
			
			//showDoubt('r', posX, posY);
		}
		
		if(knownArea.getMyZone().isDamageEnemyDoubt()) {

			sendKnowledgeToProlog(13-posX,posY,"doubt([","e");
			getKnowledgeFromProlog("doubt");
			
			// TODO INFORM PROLOG THIS SENSATION IS GONE AND TELL WHAT'S THERE
	
			/*if(Cave.getZones()[posX][posY].getType() != 'd' && 
					Cave.getZones()[posX][posY].getType() != 'D'  && countDoubts(posX, posY, 'e') == 1) {
				
				int lastDoubtX = getDoubtXPos(posX,posY,'e');
				int lastDoubtY = getDoubtYPos(posX,posY,'e');
				
				if(Cave.getZones()[lastDoubtX][lastDoubtY].getType() == 'd')
					knownArea.getExploredMap()[lastDoubtX][lastDoubtY].setType('d');
				else if(Cave.getZones()[lastDoubtX][lastDoubtY].getType() == 'D')
					knownArea.getExploredMap()[lastDoubtX][lastDoubtY].setType('D');
				knownArea.getExploredMap()[lastDoubtX][lastDoubtY].setEnemy(Cave.getZones()[lastDoubtX][lastDoubtY].getEnemy());
				
				// TODO INFORM PROLOG THAT DANGER IS FOUND (LAST DOUBT IS DANGER)
				
			}*/
		}
		if(knownArea.getMyZone().isHoleDoubt()) {

			sendKnowledgeToProlog(13-posX,posY,"doubt([","P");
			getKnowledgeFromProlog("doubt");
			
			// TODO INFORM PROLOG THIS SENSATION IS GONE
	
			/*if(Cave.getZones()[posX][posY].getType() != 'P' && countDoubts(posX, posY, 'p') == 1) {
				
				int lastDoubtX = getDoubtXPos(posX,posY,'p');
				int lastDoubtY = getDoubtYPos(posX,posY,'p');
				
				knownArea.getExploredMap()[lastDoubtX][lastDoubtY].setType('P');
				
				// TODO INFORM PROLOG THAT DANGER IS FOUND (LAST DOUBT IS DANGER)
				
			}*/
		}
		if(knownArea.getMyZone().isTeleportEnemyDoubt()) {

			sendKnowledgeToProlog(13-posX,posY,"doubt([","T");
			getKnowledgeFromProlog("doubt");
			
			// TODO INFORM PROLOG THIS SENSATION IS GONE
	
			/*if(Cave.getZones()[posX][posY].getType() != 'T' && countDoubts(posX, posY, 'r') == 1) {
				
				int lastDoubtX = getDoubtXPos(posX,posY,'r');
				int lastDoubtY = getDoubtYPos(posX,posY,'r');
				
				knownArea.getExploredMap()[lastDoubtX][lastDoubtY].setType('T');
				knownArea.getExploredMap()[lastDoubtX][lastDoubtY].setEnemy(Cave.getZones()[lastDoubtX][lastDoubtY].getEnemy());
				
				// TODO INFORM PROLOG THAT DANGER IS FOUND (LAST DOUBT IS DANGER)
				
			}*/
		}
		
		if(Cave.getZones()[posX][posY].getType() == 'O') {
			
			// TODO PUT GOLD SENSATION IN PROLOG
			sendKnowledgeToProlog(13-posX,posY,"glitter([",".");
			getKnowledgeFromProlog("glitter");
		}
		else if(Cave.getZones()[posX][posY].getType() == 'U') {
			
			// TODO PUT EXTRA HEALTH FACT IN PROLOG, IF NOT ALREADY THERE
			if(knownArea.getExploredMap()[posX][posY].getType() != 'U') {
				sendKnowledgeToProlog(13-posX,posY,"power_up([",".");
				getKnowledgeFromProlog("power_up");
			}
		}
		else if(Cave.getZones()[posX][posY].getType() == 'P') {
			
			knownArea.getMyZone().getSamus().setScore(knownArea.getMyZone().getSamus().getScore() - 1000);
			WindowMaker.setGameInfoText(Integer.toString(knownArea.getMyZone().getSamus().getScore()),
															Integer.toString(knownArea.getMyZone().getSamus().getActionsTaken()));
			knownArea.getMyZone().getSamus().setHealth(0);
			WindowMaker.setLifeBarValue(knownArea.getMyZone().getSamus().getHealth());
			
			if(knownArea.getExploredMap()[posX][posY].getType() != 'P') {
				sendKnowledgeToProlog(13-posX,posY,"danger([","P");
				getKnowledgeFromProlog("danger");
			}
			
			//knownArea.getMyZone().setVisited();
			//knownArea.getMyZone().setHoleDoubt(false);
			//updateDoubts(posX, posY, 'p');
			
			return true;
		}
		else if(Cave.getZones()[posX][posY].getType() == 'd' || Cave.getZones()[posX][posY].getType() == 'D') {
			
			char e;
			int d;
			if(Cave.getZones()[posX][posY].getType() == 'd') {
				e = 'd';
				d = 20;
				if(knownArea.getExploredMap()[posX][posY].getType() != 'd') {
					sendKnowledgeToProlog(13-posX,posY,"danger([","d");
					getKnowledgeFromProlog("danger");
				}
			}
			else {
				e = 'D';
				d = 50;
				if(knownArea.getExploredMap()[posX][posY].getType() != 'D') {
					sendKnowledgeToProlog(13-posX,posY,"danger([","D");
					getKnowledgeFromProlog("danger");
				}
			}
			
			/*if(knownArea.getMyZone().getType() != e) {
				knownArea.getMyZone().setType(e);
				knownArea.getMyZone().setEnemy(Cave.getZones()[posX][posY].getEnemy());
			}*/
			
			knownArea.getMyZone().getSamus().setScore(knownArea.getMyZone().getSamus().getScore() - d);
			WindowMaker.setGameInfoText(Integer.toString(knownArea.getMyZone().getSamus().getScore()),
															Integer.toString(knownArea.getMyZone().getSamus().getActionsTaken()));
			knownArea.getMyZone().getSamus().setHealth(knownArea.getMyZone().getSamus().getHealth() - d);
			
			//knownArea.getMyZone().setVisited();
			//knownArea.getMyZone().setDamageEnemyDoubt(false);
			//updateDoubts(posX, posY, 'e');
			
			if(knownArea.getMyZone().getSamus().getHealth() <= 0) {
				
				knownArea.getMyZone().getSamus().setHealth(0);
				WindowMaker.setLifeBarValue(knownArea.getMyZone().getSamus().getHealth());
				
				knownArea.getMyZone().getSamus().setScore(knownArea.getMyZone().getSamus().getScore() - 1000);
				WindowMaker.setGameInfoText(Integer.toString(knownArea.getMyZone().getSamus().getScore()),
																Integer.toString(knownArea.getMyZone().getSamus().getActionsTaken()));
				return true;
			}
			
			WindowMaker.setLifeBarValue(knownArea.getMyZone().getSamus().getHealth());
			
			// TODO UPDATE CHAR INFO INTO PROLOG
			// INFORM PROLOG ABOUT DANGER, IF IT'S NOT A FACT ALREADY
			// INFORM PROLOG THERE'S NO DOUBTS ANYMORE, IF THERE WERE ANY ABOUT THIS DANGER
			sendKnowledgeToProlog(13-posX,posY,"samus([",".");
		}
		else if(Cave.getZones()[posX][posY].getType() == 'T') {
			
			if(knownArea.getExploredMap()[posX][posY].getType() != 'T') {
				sendKnowledgeToProlog(13-posX,posY,"danger([","T");
				getKnowledgeFromProlog("danger");
			}
			sendKnowledgeToProlog(13-knownArea.getMyZone().getSamus().getI(),knownArea.getMyZone().getSamus().getJ(),"samus([",".");
			
			//knownArea.getMyZone().setTeleportEnemyDoubt(false);
			//updateDoubts(posX, posY, 'r');
			
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
			
			// TODO INFORM PROLOG ABOUT WALL SENSATION, IF IT'S NOT A FACT ALREADY
			// TELL PROLOG TO GET BACK TO LAST POSITION
			if(knownArea.getExploredMap()[posX][posY].getType() != 'W') {
				sendKnowledgeToProlog(13-posX,posY,"bump([",".");
				getKnowledgeFromProlog("bump");
			}
		}
		
		return false;
	}

	// This method put doubts into the known area matrix
	private void showDoubt(char c, int posX, int posY) {

		if((c == 'e' && (knownArea.getExploredMap()[posX + 1][posY].getType() != 'd' && knownArea.getExploredMap()[posX - 1][posY].getType() != 'd'
						&& knownArea.getExploredMap()[posX][posY + 1].getType() != 'd' && knownArea.getExploredMap()[posX][posY - 1].getType() != 'd'
						&& knownArea.getExploredMap()[posX + 1][posY].getType() != 'D' && knownArea.getExploredMap()[posX - 1][posY].getType() != 'D'
						&& knownArea.getExploredMap()[posX][posY + 1].getType() != 'D' && knownArea.getExploredMap()[posX][posY - 1].getType() != 'D'))
			|| (c == 'p' && (knownArea.getExploredMap()[posX + 1][posY].getType() != 'P' && knownArea.getExploredMap()[posX - 1][posY].getType() != 'P'
							&& knownArea.getExploredMap()[posX][posY + 1].getType() != 'P' && knownArea.getExploredMap()[posX][posY - 1].getType() != 'P'))
			|| (c == 'r' && (knownArea.getExploredMap()[posX + 1][posY].getType() != 'T' && knownArea.getExploredMap()[posX - 1][posY].getType() != 'T'
							&& knownArea.getExploredMap()[posX][posY + 1].getType() != 'T' && knownArea.getExploredMap()[posX][posY - 1].getType() != 'T'))){
			if (/*posX+1 < 13 && */knownArea.getExploredMap()[posX + 1][posY].isVisited() == false) {
				if(c == 'e')
					knownArea.getExploredMap()[posX + 1][posY].setDamageEnemyDoubt(true);
				else if(c == 'p')
					knownArea.getExploredMap()[posX + 1][posY].setHoleDoubt(true);
				else
					knownArea.getExploredMap()[posX + 1][posY].setTeleportEnemyDoubt(true);
			}
			if (/*posX-1 > 0 && */knownArea.getExploredMap()[posX - 1][posY].isVisited() == false) {
				if(c == 'e')
					knownArea.getExploredMap()[posX - 1][posY].setDamageEnemyDoubt(true);
				else if(c == 'p')
					knownArea.getExploredMap()[posX - 1][posY].setHoleDoubt(true);
				else
					knownArea.getExploredMap()[posX - 1][posY].setTeleportEnemyDoubt(true);
			}
			if (/*posY-1 > 0 && */knownArea.getExploredMap()[posX][posY - 1].isVisited() == false) {
				if(c == 'e')
					knownArea.getExploredMap()[posX][posY - 1].setDamageEnemyDoubt(true);
				else if(c == 'p')
					knownArea.getExploredMap()[posX][posY - 1].setHoleDoubt(true);
				else
					knownArea.getExploredMap()[posX][posY - 1].setTeleportEnemyDoubt(true);
			}
			if (/*posY+1 < 13 && */knownArea.getExploredMap()[posX][posY + 1].isVisited() == false) {
				if(c == 'e')
					knownArea.getExploredMap()[posX][posY + 1].setDamageEnemyDoubt(true);
				else if(c == 'p')
					knownArea.getExploredMap()[posX][posY + 1].setHoleDoubt(true);
				else
					knownArea.getExploredMap()[posX][posY + 1].setTeleportEnemyDoubt(true);
			}
		}
	}

	// This method clears doubts from the known area matrix
	private void updateDoubts(int i, int j, char type) {

		if(type == 'e') {
			if (knownArea.getExploredMap()[i + 1][j + 1].isDamageEnemyDoubt())
				knownArea.getExploredMap()[i + 1][j + 1].setDamageEnemyDoubt(false);
			if (knownArea.getExploredMap()[i - 1][j + 1].isDamageEnemyDoubt())
				knownArea.getExploredMap()[i - 1][j + 1].setDamageEnemyDoubt(false);
			if (knownArea.getExploredMap()[i - 1][j - 1].isDamageEnemyDoubt())
				knownArea.getExploredMap()[i - 1][j - 1].setDamageEnemyDoubt(false);
			if (knownArea.getExploredMap()[i + 1][j - 1].isDamageEnemyDoubt())
				knownArea.getExploredMap()[i + 1][j - 1].setDamageEnemyDoubt(false);

			if ((i - 2 >= 0) && knownArea.getExploredMap()[i - 2][j].isDamageEnemyDoubt())
				knownArea.getExploredMap()[i - 2][j].setDamageEnemyDoubt(false);
			if ((i + 2 <= 13) && knownArea.getExploredMap()[i + 2][j].isDamageEnemyDoubt())
				knownArea.getExploredMap()[i + 2][j].setDamageEnemyDoubt(false);
			if ((j - 2 >= 0) && knownArea.getExploredMap()[i][j - 2].isDamageEnemyDoubt())
				knownArea.getExploredMap()[i][j - 2].setDamageEnemyDoubt(false);
			if ((j + 2 <= 13) && knownArea.getExploredMap()[i][j + 2].isDamageEnemyDoubt())
				knownArea.getExploredMap()[i][j + 2].setDamageEnemyDoubt(false);
		}
		else if(type == 'p') {
			if (knownArea.getExploredMap()[i + 1][j + 1].isHoleDoubt())
				knownArea.getExploredMap()[i + 1][j + 1].setHoleDoubt(false);
			if (knownArea.getExploredMap()[i - 1][j + 1].isHoleDoubt())
				knownArea.getExploredMap()[i - 1][j + 1].setHoleDoubt(false);
			if (knownArea.getExploredMap()[i - 1][j - 1].isHoleDoubt())
				knownArea.getExploredMap()[i - 1][j - 1].setHoleDoubt(false);
			if (knownArea.getExploredMap()[i + 1][j - 1].isHoleDoubt())
				knownArea.getExploredMap()[i + 1][j - 1].setHoleDoubt(false);

			if ((i - 2 >= 0) && knownArea.getExploredMap()[i - 2][j].isHoleDoubt())
				knownArea.getExploredMap()[i - 2][j].setHoleDoubt(false);
			if ((i + 2 <= 13) && knownArea.getExploredMap()[i + 2][j].isHoleDoubt())
				knownArea.getExploredMap()[i + 2][j].setHoleDoubt(false);
			if ((j - 2 >= 0) && knownArea.getExploredMap()[i][j - 2].isHoleDoubt())
				knownArea.getExploredMap()[i][j - 2].setHoleDoubt(false);
			if ((j + 2 <= 13) && knownArea.getExploredMap()[i][j + 2].isHoleDoubt())
				knownArea.getExploredMap()[i][j + 2].setHoleDoubt(false);
		}
		else {
			if (knownArea.getExploredMap()[i + 1][j + 1].isTeleportEnemyDoubt())
				knownArea.getExploredMap()[i + 1][j + 1].setTeleportEnemyDoubt(false);
			if (knownArea.getExploredMap()[i - 1][j + 1].isTeleportEnemyDoubt())
				knownArea.getExploredMap()[i - 1][j + 1].setTeleportEnemyDoubt(false);
			if (knownArea.getExploredMap()[i - 1][j - 1].isTeleportEnemyDoubt())
				knownArea.getExploredMap()[i - 1][j - 1].setTeleportEnemyDoubt(false);
			if (knownArea.getExploredMap()[i + 1][j - 1].isTeleportEnemyDoubt())
				knownArea.getExploredMap()[i + 1][j - 1].setTeleportEnemyDoubt(false);

			if ((i - 2 >= 0) && knownArea.getExploredMap()[i - 2][j].isTeleportEnemyDoubt())
				knownArea.getExploredMap()[i - 2][j].setTeleportEnemyDoubt(false);
			if ((i + 2 <= 13) && knownArea.getExploredMap()[i + 2][j].isTeleportEnemyDoubt())
				knownArea.getExploredMap()[i + 2][j].setTeleportEnemyDoubt(false);
			if ((j - 2 >= 0) && knownArea.getExploredMap()[i][j - 2].isTeleportEnemyDoubt())
				knownArea.getExploredMap()[i][j - 2].setTeleportEnemyDoubt(false);
			if ((j + 2 <= 13) && knownArea.getExploredMap()[i][j + 2].isTeleportEnemyDoubt())
				knownArea.getExploredMap()[i][j + 2].setTeleportEnemyDoubt(false);
		}
	}
	
	// This method checks how many doubts remain in a neighborhood
	private int countDoubts(int i, int j, char type) {
		
		int count = 0;
		
		if(type == 'e') {
			if (knownArea.getExploredMap()[i + 1][j + 1].isDamageEnemyDoubt())
				count++;
			if (knownArea.getExploredMap()[i - 1][j + 1].isDamageEnemyDoubt())
				count++;
			if (knownArea.getExploredMap()[i - 1][j - 1].isDamageEnemyDoubt())
				count++;
			if (knownArea.getExploredMap()[i + 1][j - 1].isDamageEnemyDoubt())
				count++;

			if ((i - 2 >= 0) && knownArea.getExploredMap()[i - 2][j].isDamageEnemyDoubt())
				count++;
			if ((i + 2 <= 13) && knownArea.getExploredMap()[i + 2][j].isDamageEnemyDoubt())
				count++;
			if ((j - 2 >= 0) && knownArea.getExploredMap()[i][j - 2].isDamageEnemyDoubt())
				count++;
			if ((j + 2 <= 13) && knownArea.getExploredMap()[i][j + 2].isDamageEnemyDoubt())
				count++;
		}
		else if(type == 'p') {
			if (knownArea.getExploredMap()[i + 1][j + 1].isHoleDoubt())
				count++;
			if (knownArea.getExploredMap()[i - 1][j + 1].isHoleDoubt())
				count++;
			if (knownArea.getExploredMap()[i - 1][j - 1].isHoleDoubt())
				count++;
			if (knownArea.getExploredMap()[i + 1][j - 1].isHoleDoubt())
				count++;

			if ((i - 2 >= 0) && knownArea.getExploredMap()[i - 2][j].isHoleDoubt())
				count++;
			if ((i + 2 <= 13) && knownArea.getExploredMap()[i + 2][j].isHoleDoubt())
				count++;
			if ((j - 2 >= 0) && knownArea.getExploredMap()[i][j - 2].isHoleDoubt())
				count++;
			if ((j + 2 <= 13) && knownArea.getExploredMap()[i][j + 2].isHoleDoubt())
				count++;
		}
		else {
			if (knownArea.getExploredMap()[i + 1][j + 1].isTeleportEnemyDoubt())
				count++;
			if (knownArea.getExploredMap()[i - 1][j + 1].isTeleportEnemyDoubt())
				count++;
			if (knownArea.getExploredMap()[i - 1][j - 1].isTeleportEnemyDoubt())
				count++;
			if (knownArea.getExploredMap()[i + 1][j - 1].isTeleportEnemyDoubt())
				count++;

			if ((i - 2 >= 0) && knownArea.getExploredMap()[i - 2][j].isTeleportEnemyDoubt())
				count++;
			if ((i + 2 <= 13) && knownArea.getExploredMap()[i + 2][j].isTeleportEnemyDoubt())
				count++;
			if ((j - 2 >= 0) && knownArea.getExploredMap()[i][j - 2].isTeleportEnemyDoubt())
				count++;
			if ((j + 2 <= 13) && knownArea.getExploredMap()[i][j + 2].isTeleportEnemyDoubt())
				count++;
		}
		
		return count;
	}
	
	private int getDoubtXPos(int i, int j, char type) {
		
		if(type == 'e') {
			if(knownArea.getExploredMap()[i+1][j+1].isDamageEnemyDoubt() || knownArea.getExploredMap()[i+1][j-1].isDamageEnemyDoubt())
				return i+1;
			if(knownArea.getExploredMap()[i-1][j+1].isDamageEnemyDoubt() || knownArea.getExploredMap()[i-1][j-1].isDamageEnemyDoubt())
				return i-1;
			if((i - 2 >= 0) && knownArea.getExploredMap()[i-2][j].isDamageEnemyDoubt())
				return i-2;
			if((i + 2 <= 13) && knownArea.getExploredMap()[i+2][j].isDamageEnemyDoubt())
				return i+2;
			if(((j - 2 >= 0) && knownArea.getExploredMap()[i][j-2].isDamageEnemyDoubt()) || ((j + 2 <= 13) && knownArea.getExploredMap()[i][j+2].isDamageEnemyDoubt()))
				return i;
		}
		else if(type == 'p') {
			if(knownArea.getExploredMap()[i+1][j+1].isHoleDoubt() || knownArea.getExploredMap()[i+1][j-1].isHoleDoubt())
				return i+1;
			if(knownArea.getExploredMap()[i-1][j+1].isHoleDoubt() || knownArea.getExploredMap()[i-1][j-1].isHoleDoubt())
				return i-1;
			if((i - 2 >= 0) && knownArea.getExploredMap()[i-2][j].isHoleDoubt())
				return i-2;
			if((i + 2 <= 13) && knownArea.getExploredMap()[i+2][j].isHoleDoubt())
				return i+2;
			if(((j - 2 >= 0) && knownArea.getExploredMap()[i][j-2].isHoleDoubt()) || ((j + 2 <= 13) && knownArea.getExploredMap()[i][j+2].isHoleDoubt()))
				return i;
		}
		else {
			if(knownArea.getExploredMap()[i+1][j+1].isTeleportEnemyDoubt() || knownArea.getExploredMap()[i+1][j-1].isTeleportEnemyDoubt())
				return i+1;
			if(knownArea.getExploredMap()[i-1][j+1].isTeleportEnemyDoubt() || knownArea.getExploredMap()[i-1][j-1].isTeleportEnemyDoubt())
				return i-1;
			if((i - 2 >= 0) && knownArea.getExploredMap()[i-2][j].isTeleportEnemyDoubt())
				return i-2;
			if((i + 2 <= 13) && knownArea.getExploredMap()[i+2][j].isTeleportEnemyDoubt())
				return i+2;
			if(((j - 2 >= 0) && knownArea.getExploredMap()[i][j-2].isTeleportEnemyDoubt()) || ((j + 2 <= 13) && knownArea.getExploredMap()[i][j+2].isTeleportEnemyDoubt()))
				return i;
		}
		// If this is returned, something's wrong
		return -1;
	}
	
	private int getDoubtYPos(int i, int j, char type) {

		if(type == 'e') {
			if(knownArea.getExploredMap()[i+1][j+1].isDamageEnemyDoubt() || knownArea.getExploredMap()[i-1][j+1].isDamageEnemyDoubt())
				return j+1;
			if(knownArea.getExploredMap()[i+1][j-1].isDamageEnemyDoubt() || knownArea.getExploredMap()[i-1][j-1].isDamageEnemyDoubt())
				return j-1;
			if(((i - 2 >= 0) && knownArea.getExploredMap()[i-2][j].isDamageEnemyDoubt()) || ((i + 2 <= 13) && knownArea.getExploredMap()[i+2][j].isDamageEnemyDoubt()))
				return j;
			if((j - 2 >= 0) && knownArea.getExploredMap()[i][j-2].isDamageEnemyDoubt())
				return j-2;
			if((j + 2 <= 13) && knownArea.getExploredMap()[i][j+2].isDamageEnemyDoubt())
				return j+2;
		}
		else if(type == 'p') {
			if(knownArea.getExploredMap()[i+1][j+1].isHoleDoubt() || knownArea.getExploredMap()[i-1][j+1].isHoleDoubt())
				return j+1;
			if(knownArea.getExploredMap()[i+1][j-1].isHoleDoubt() || knownArea.getExploredMap()[i-1][j-1].isHoleDoubt())
				return j-1;
			if(((i - 2 >= 0) && knownArea.getExploredMap()[i-2][j].isHoleDoubt()) || ((i + 2 <= 13) && knownArea.getExploredMap()[i+2][j].isHoleDoubt()))
				return j;
			if((j - 2 >= 0) && knownArea.getExploredMap()[i][j-2].isHoleDoubt())
				return j-2;
			if((j + 2 <= 13) && knownArea.getExploredMap()[i][j+2].isHoleDoubt())
				return j+2;
		}
		else {
			if(knownArea.getExploredMap()[i+1][j+1].isTeleportEnemyDoubt() || knownArea.getExploredMap()[i-1][j+1].isTeleportEnemyDoubt())
				return j+1;
			if(knownArea.getExploredMap()[i+1][j-1].isTeleportEnemyDoubt() || knownArea.getExploredMap()[i-1][j-1].isTeleportEnemyDoubt())
				return j-1;
			if(((i - 2 >= 0) && knownArea.getExploredMap()[i-2][j].isTeleportEnemyDoubt()) || ((i + 2 <= 13) && knownArea.getExploredMap()[i+2][j].isTeleportEnemyDoubt()))
				return j;
			if((j - 2 >= 0) && knownArea.getExploredMap()[i][j-2].isTeleportEnemyDoubt())
				return j-2;
			if((j + 2 <= 13) && knownArea.getExploredMap()[i][j+2].isTeleportEnemyDoubt())
				return j+2;
		}
		// If this is returned, something's wrong
		return -1;
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