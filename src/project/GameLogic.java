package project;

import gui.WindowMaker;

import java.util.List;
import java.util.Map;

import map.Cave;
import map.KnownArea;
import map.Zone;

import org.jpl7.Atom;
import org.jpl7.Query;
import org.jpl7.Term;

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
			
			sendKnowledgeToProlog(13-12,1,"breeze([",".");
			getKnowledgeFromProlog("breeze");
			knownArea.repaint();
		}
		if(isSensationZone('e', 12, 1)) {

			sendKnowledgeToProlog(13-12,1,"sound([",".");
			getKnowledgeFromProlog("sound");
			knownArea.repaint();
		}
		if(isSensationZone('r', 12, 1)) {
			
			sendKnowledgeToProlog(13-12,1,"flash([",".");
			getKnowledgeFromProlog("flash");
			getKnowledgeFromProlog("doubt");
			knownArea.repaint();
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

				System.out.println("X" + " = " + solution[0].get("X").toString() + "\n");
				// System.out.println("X length = " + solution[i].get("X").listLength());
				// System.out.println(solution[i].get("X").arg(2).arg(2).arg(2).toString());

				// Extracting term, because, head is 1 value, body is another term
				Term term = solution[0].get("X");
				for (int j = 0; j < solution[0].get("X").listLength(); j++, term = term.arg(2)) {

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

				posX = 13 - posX;

				// Updating my values
				knownArea.getMyZone().getSamus().setActionsTaken();
				knownArea.getMyZone().getSamus().setScore(score);
				WindowMaker.setGameInfoText(Integer.toString(knownArea.getMyZone().getSamus().getScore()),
											Integer.toString(knownArea.getMyZone().getSamus().getActionsTaken()),
											Integer.toString(goldLeftToTake));

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
				getKnowledgeFromProlog("toVisit");
				
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
							
							if(Cave.getZones()[enemyX][enemyY].getType() == 'd')
								Cave.getZones()[enemyX][enemyY].setType('p');
							else if(Cave.getZones()[enemyX][enemyY].getType() == 'D')
								Cave.getZones()[enemyX][enemyY].setType('m');
							else if(Cave.getZones()[enemyX][enemyY].getType() == 'T')
								Cave.getZones()[enemyX][enemyY].setType('r');
							
							knownArea.getExploredMap()[enemyX][enemyY].setType('.');
							
							Cave.getZones()[enemyX][enemyY].setEnemy(null);
							knownArea.getExploredMap()[enemyX][enemyY].setEnemy(null);
							
							sendKnowledgeToProlog(13-posX,posY,"scream([",".");
						}
					}
				}
				else if(action == 'C')
					break;
				
				knownArea.repaint();
				
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
					else if(knownArea.getMyZone().getSamus().getHealth() <= 50) {
						
						List<Zone> closestPath = null;
						
						// Looking for known power-ups
						for(int i = 1, bestPathCost = 0; i < 13; i++) {
							for(int j = 1; j < 13; j++) {
								if(knownArea.getExploredMap()[i][j].getType() == 'U') {
									
									AStar star = new AStar(knownArea.getExploredMap(), knownArea.getMyZone(), knownArea.getExploredMap()[i][j]);
									
									if(closestPath == null) {
										closestPath = star.aStar();
										
										if(closestPath == null)
											continue;
										
										bestPathCost = closestPath.get(0).getF();
									}
									else {
										List<Zone> candidatePath = star.aStar();
										
										if(candidatePath == null)
											continue;
										
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
				else if(string.equals("toVisit") && knownArea.getExploredMap()[i][j].getType() == 't')
					knownArea.getExploredMap()[i][j].setType('n');
			}
		}
		
		if(string.contains("danger") || string.contains("doubt"))
			command = string.concat("([I | J], T)");
		else
			command = string.concat("([I | J])");
		
		Query q2 = new Query(command);
		
		q2.hasSolution();
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
					else if(type == 'd' && string.contains("danger")) {
						knownArea.getExploredMap()[posX][posY].setType('d');
						knownArea.getExploredMap()[posX][posY].setDamageEnemyDoubt(false);
						//if(Cave.getZones()[posX][posY].getEnemy() != null)
							knownArea.getExploredMap()[posX][posY].setEnemy(Cave.getZones()[posX][posY].getEnemy());
					}
					else if(type == 'D' && string.contains("danger")) {
						knownArea.getExploredMap()[posX][posY].setType('D');
						knownArea.getExploredMap()[posX][posY].setDamageEnemyDoubt(false);
						//if(Cave.getZones()[posX][posY].getEnemy() != null)
							knownArea.getExploredMap()[posX][posY].setEnemy(Cave.getZones()[posX][posY].getEnemy());
					}
					else if((type == 'd' || type == 'D') && string.contains("doubt"))
						knownArea.getExploredMap()[posX][posY].setDamageEnemyDoubt(true);
					else if(type == 'T' && string.contains("danger")) {
						knownArea.getExploredMap()[posX][posY].setType('T');
						knownArea.getExploredMap()[posX][posY].setTeleportEnemyDoubt(false);
						//if(Cave.getZones()[posX][posY].getEnemy() != null)
							knownArea.getExploredMap()[posX][posY].setEnemy(Cave.getZones()[posX][posY].getEnemy());
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
					else if(string.contains("toVisit"))
						if(posX > 0 && posX < 13 && posY > 0 && posY < 13)
							knownArea.getExploredMap()[posX][posY].setType('t');
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
			
			if(type.contains("\'d\'")) {
				
				String command = name.concat("X | Y], \'D\')");
				
				// This is in case she marked 'D' for an enemy she knows it's there but never visited
				// ('D' is the default in prolog in that case)
				if(!verifyFact(command, i, j)) {
					
					command = new String("retract(").concat(name);
					command = command.concat(Integer.toString(i));
					command = command.concat("|");
					command = command.concat(Integer.toString(j));
					command = command.concat("],\'D\'))");

					q2 = new Query(command);
					q2.hasSolution();
					System.out.println(q2.toString() + "\n");
				}
			}
			
			String command = name.concat("X | Y], \'");
			command = command.concat(type);
			command = command.concat("\')");
			
			if(verifyFact(command, i, j)) {
				
				command = new String("assert(").concat(name);
				command = command.concat(Integer.toString(i));
				command = command.concat("|");
				command = command.concat(Integer.toString(j));
				command = command.concat("],\'");
				command = command.concat(type);
				command = command.concat("\'))");
				
				q2 = new Query(command);
				q2.hasSolution();
				System.out.println(q2.toString() + "\n");
			}
		}
		else if(name.contains("doubt")) {
			
			if(type.equals("e")) {
				
				String command = name.concat("X | Y], \'d\')");
				
				if(!verifyFact(command, i, j)) {
				
					command = new String("retract(").concat(name);
					command = command.concat(Integer.toString(i));
					command = command.concat("|");
					command = command.concat(Integer.toString(j));
					command = command.concat("],\'d\'))");

					q2 = new Query(command);
					q2.hasSolution();
					System.out.println(q2.toString() + "\n");
				}
				
				command = name.concat("X | Y], \'D\')");
				
				if(!verifyFact(command, i, j)) {
				
					command = new String("retract(").concat(name);
					command = command.concat(Integer.toString(i));
					command = command.concat("|");
					command = command.concat(Integer.toString(j));
					command = command.concat("],\'D\'))");

					q2 = new Query(command);
					q2.hasSolution();
					System.out.println(q2.toString() + "\n");
				}
			}
			else {
				
				String command = name.concat("X | Y], \'P\')");
				
				if(!verifyFact(command, i, j)) {
				
					command = new String("retract(").concat(name);
					command = command.concat(Integer.toString(i));
					command = command.concat("|");
					command = command.concat(Integer.toString(j));
					command = command.concat("],\'");
					command = command.concat(type);
					command = command.concat("\'))");

					q2 = new Query(command);
					q2.hasSolution();
					System.out.println(q2.toString() + "\n");
				}
			}
		}
		else if(name.contains("statusChange")) {
			
			String command = null;
			
			if(name.contains("\'P\'")) {
				command = name.concat(Integer.toString(i));
				command = command.concat("|");
				command = command.concat(Integer.toString(j));
				command = command.concat("])");
			}
			else if(name.contains("\'H\'")) {
				command = name.concat(type);
				command = command.concat(")");
			}
			
			q2 = new Query(command);
			q2.hasSolution();
			System.out.println(q2.toString() + "\n");
		}
		else if(name.contains("nextDestination")) {
			
			String command = new String("assert(").concat(name);
			command = command.concat(Integer.toString(i));
			command = command.concat("|");
			command = command.concat(Integer.toString(j));
			command = command.concat("]))");
			
			q2 = new Query(command);
			q2.hasSolution();
			System.out.println(q2.toString() + "\n");
		}
		else if(name.contains("update_lastVisited")) {
			
			q2 = new Query("update_lastVisited");
			q2.hasSolution();
			System.out.println(q2.toString() + "\n");
		}
		else {
			
			String command = name.concat("X | Y])");
			
			if(verifyFact(command, i, j)) {
			
				command = new String("assert(").concat(name);
				command = command.concat(Integer.toString(i));
				command = command.concat("|");
				command = command.concat(Integer.toString(j));
				command = command.concat("]))");

				q2 = new Query(command);
				q2.hasSolution();
				System.out.println(q2.toString() + "\n");
			}
		}
	}

	private boolean verifyFact(String command, int posX, int posY) {
		
		Query q2 = new Query(command);
		System.out.println("Verify: " + q2.toString());
		
		Map<String, Term>[] solution = q2.allSolutions();

		if (solution != null) {
			
			for(int i = 0; i < solution.length; i++) {
				if(solution[i].get("X").toString().equals(Integer.toString(posX))
					&& solution[i].get("Y").toString().equals(Integer.toString(posY))) {
					System.out.println("Is in prolog!\n");
					return false;
				}
			}
		}
		
		System.out.println("Is not in prolog!\n");
		return true;
	}

	// Returns true if game has to stop
	private boolean treatMovement(int posX, int posY) {
		
		if((posX > 0 && posX < 13) && (posY > 0 && posY < 13) && isSensationZone('e', posX, posY)) {
			
			sendKnowledgeToProlog(13-posX,posY,"sound([",".");
			getKnowledgeFromProlog("sound");
		}
		if((posX > 0 && posX < 13) && (posY > 0 && posY < 13) && isSensationZone('p', posX, posY)) {
			
			sendKnowledgeToProlog(13-posX,posY,"breeze([",".");
			getKnowledgeFromProlog("breeze");
		}
		if((posX > 0 && posX < 13) && (posY > 0 && posY < 13) && isSensationZone('r', posX, posY)) {
			
			sendKnowledgeToProlog(13-posX,posY,"flash([",".");
			getKnowledgeFromProlog("flash");
		}
		
		if(knownArea.getMyZone().isDamageEnemyDoubt()) {

			sendKnowledgeToProlog(13-posX,posY,"doubt([","e");
			getKnowledgeFromProlog("doubt");
		}
		if(knownArea.getMyZone().isHoleDoubt()) {

			sendKnowledgeToProlog(13-posX,posY,"doubt([","P");
			getKnowledgeFromProlog("doubt");
		}
		if(knownArea.getMyZone().isTeleportEnemyDoubt()) {

			sendKnowledgeToProlog(13-posX,posY,"doubt([","T");
			getKnowledgeFromProlog("doubt");
		}
		
		if(Cave.getZones()[posX][posY].getType() == 'O') {
			
			sendKnowledgeToProlog(13-posX,posY,"glitter([",".");
			getKnowledgeFromProlog("glitter");
		}
		else if(Cave.getZones()[posX][posY].getType() == 'U') {
			
			if(knownArea.getExploredMap()[posX][posY].getType() != 'U') {
				sendKnowledgeToProlog(13-posX,posY,"power_up([",".");
				getKnowledgeFromProlog("power_up");
			}
		}
		else if(Cave.getZones()[posX][posY].getType() == 'P') {
			
			knownArea.getMyZone().getSamus().setScore(knownArea.getMyZone().getSamus().getScore() - 1000);
			WindowMaker.setGameInfoText(Integer.toString(knownArea.getMyZone().getSamus().getScore()),
															Integer.toString(knownArea.getMyZone().getSamus().getActionsTaken()),
															Integer.toString(goldLeftToTake));
			knownArea.getMyZone().getSamus().setHealth(0);
			WindowMaker.setLifeBarValue(knownArea.getMyZone().getSamus().getHealth());
			
			if(knownArea.getExploredMap()[posX][posY].getType() != 'P') {
				sendKnowledgeToProlog(13-posX,posY,"danger([","P");
				getKnowledgeFromProlog("danger");
			}
			
			return true;
		}
		else if(Cave.getZones()[posX][posY].getType() == 'd' || Cave.getZones()[posX][posY].getType() == 'D') {
			
			int d;
			if(Cave.getZones()[posX][posY].getType() == 'd') {
				d = 20;
				if(knownArea.getExploredMap()[posX][posY].getType() != 'd') {
					sendKnowledgeToProlog(13-posX,posY,"danger([","d");
					getKnowledgeFromProlog("danger");
				}
			}
			else {
				d = 50;
				if(knownArea.getExploredMap()[posX][posY].getType() != 'D') {
					sendKnowledgeToProlog(13-posX,posY,"danger([","D");
					getKnowledgeFromProlog("danger");
				}
			}
			
			knownArea.getMyZone().getSamus().setScore(knownArea.getMyZone().getSamus().getScore() - d);
			WindowMaker.setGameInfoText(Integer.toString(knownArea.getMyZone().getSamus().getScore()),
															Integer.toString(knownArea.getMyZone().getSamus().getActionsTaken()),
															Integer.toString(goldLeftToTake));
			knownArea.getMyZone().getSamus().setHealth(knownArea.getMyZone().getSamus().getHealth() - d);
			
			if(knownArea.getMyZone().getSamus().getHealth() <= 0) {
				
				knownArea.getMyZone().getSamus().setHealth(0);
				WindowMaker.setLifeBarValue(knownArea.getMyZone().getSamus().getHealth());
				
				knownArea.getMyZone().getSamus().setScore(knownArea.getMyZone().getSamus().getScore() - 1000);
				WindowMaker.setGameInfoText(Integer.toString(knownArea.getMyZone().getSamus().getScore()),
																Integer.toString(knownArea.getMyZone().getSamus().getActionsTaken()),
																Integer.toString(goldLeftToTake));
				return true;
			}
			
			WindowMaker.setLifeBarValue(knownArea.getMyZone().getSamus().getHealth());
			
			sendKnowledgeToProlog(13-posX,posY,"statusChange(\'H\',",Integer.toString(-d));
		}
		else if(Cave.getZones()[posX][posY].getType() == 'T') {
			
			if(knownArea.getExploredMap()[posX][posY].getType() != 'T') {
				sendKnowledgeToProlog(13-posX,posY,"danger([","T");
				getKnowledgeFromProlog("danger");
			}
			
			knownArea.getMyZone().getEnemy().generateRandomPosition(knownArea.getMyZone());
			
			knownArea.getExploredMap()[knownArea.getMyZone().getSamus().getI()]
					[knownArea.getMyZone().getSamus().getJ()].setSamus(knownArea.getMyZone().getSamus());
			knownArea.setMyZone(knownArea.getExploredMap()[knownArea.getMyZone().getSamus().getI()]
					[knownArea.getMyZone().getSamus().getJ()]);
			knownArea.getMyZone().setVisited();
			knownArea.getExploredMap()[posX][posY].setSamus(null);
			
			sendKnowledgeToProlog(13-knownArea.getMyZone().getSamus().getI(),knownArea.getMyZone().getSamus().getJ(),"statusChange(\'P\',[",".");
			sendKnowledgeToProlog(0,0,"update_lastVisited",".");
			
			// Treat new position
			if(treatMovement(knownArea.getMyZone().getI(),knownArea.getMyZone().getJ()))
				return true;
		}
		else if(Cave.getZones()[posX][posY].getType() == 'W') {
			
			if(knownArea.getExploredMap()[posX][posY].getType() != 'W') {
				sendKnowledgeToProlog(13-posX,posY,"bump([",".");
				getKnowledgeFromProlog("bump");
			}
		}
		
		return false;
	}
	
	// This method says if the area shot has an enemy
	private boolean isValidShot(int posX, int posY) {
		
		if((knownArea.getMyZone().getSamus().getDirection() == 1 && Cave.getZones()[posX-1][posY].getEnemy() != null)
			|| (knownArea.getMyZone().getSamus().getDirection() == 2 && Cave.getZones()[posX+1][posY].getEnemy() != null)
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