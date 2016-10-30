package map;

import gui.WindowMaker;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import characters.Samus;

public class KnownArea extends JPanel {

	// TODO Dictionary for extra types:
	// 'u' == unknown/unexplored zone
	// 'p' == possible hole
	// 'e' == possible Pirate (small enemy)
	// 'E' == possible Metroid (big enemy)
	// 'r' == possible Ridley (teleport)
	// 'w' == unknown wall
	private static Zone exploredMap[][] = null;
	private Zone myZone;
	private Zone exitZone;
	
	private int zoneWidth, zoneHeight;
	
	// All of these hold the zones of facts that she KNOWS FOR SURE (they may not be needed in the end...)
	private ArrayList<Zone> energyZones = new ArrayList<Zone>();
	private ArrayList<Zone> enemyZones = new ArrayList<Zone>();
	private ArrayList<Zone> holeZones = new ArrayList<Zone>();
	private ArrayList<Zone> wallZones = new ArrayList<Zone>();
	
	/*
	 * TODO:
	 * - Methods listed below in verifyValidPosition
	 * - Method to check and update our holeZones list for each step
	 * - Some other methods we'll still need...
	 */
	
	public KnownArea(int zoneWidth, int zoneHeight) {
		
		this.zoneWidth = zoneWidth;
		this.zoneHeight = zoneHeight;
		
		exploredMap = new Zone[14][14];
		
		for(int i = 0; i < 14; i++) {
			for(int j = 0; j < 14; j++) {
				exploredMap[i][j] = new Zone();
				exploredMap[i][j].setI(i);
				exploredMap[i][j].setJ(j);
				if(i != 0 && i != 13 && j != 0 && j != 13)
					exploredMap[i][j].setType('u');
				else
					exploredMap[i][j].setType('w');
			}
		}
	}
	
	public void setMyZone(Zone zone) {
		myZone = zone;
	}
	
	public void updateMap(ArrayList<Zone> neighborZones) {
		
		// Beginning of the game
		if(exploredMap == null) {
			//setMyZone(exploredMap[neighborZones.get(i).getI()][neighborZones.get(i).getJ()]);
			exitZone = exploredMap[neighborZones.get(0).getI()][neighborZones.get(0).getJ()];
		}
		
		for(int i = 0; i < neighborZones.size(); i++) {
			
			if(i == 0) {
				exploredMap[neighborZones.get(i).getI()][neighborZones.get(i).getJ()].setExplored(true);
				
				if(verifyValidPosition(neighborZones.get(i).getI(), neighborZones.get(i).getJ())) {
					
					exploredMap[neighborZones.get(i).getI()][neighborZones.get(i).getJ()].setSamus(myZone.getSamus());
					myZone.setSamus(null);
					myZone = exploredMap[neighborZones.get(i).getI()][neighborZones.get(i).getJ()];
					myZone.getSamus().setI(neighborZones.get(i).getI());
					myZone.getSamus().setJ(neighborZones.get(i).getJ());
					
					// TODO VERIFY IF ALL GOLDS WHERE TAKEN, AND ACTIVATE ASTAR TO GET TO THE EXIT
					// OR
					// TODO GAME OVER, STOP EXECUTION IF LANDED IN A HOLE
					
				}
			}
			else if(exploredMap[neighborZones.get(i).getI()][neighborZones.get(i).getJ()].getType() == 'u')
				exploredMap[neighborZones.get(i).getI()][neighborZones.get(i).getJ()].setType(neighborZones.get(i).getType());
		}
		
		repaint();
	}
	
	// Here we compare where she stands with the map in Cave
	// Returns TRUE to validate new position changing, FALSE if not
	// TODO This method may TOTALLY change...
	private boolean verifyValidPosition(int i, int j) {
		
		if(Cave.getZones()[i][j].getType() == 'O') {
			myZone.getSamus().setScore(1000);
			exploredMap[i][j].setType('.');
			Cave.getZones()[i][j].setType('.');
			
			// TODO CALL THE METHOD TO TAKE THE GOLD
			
			WindowMaker.setGameInfoText(Integer.toString(myZone.getSamus().getScore()), Integer.toString(myZone.getSamus().getActionsTaken()));
			
			return true;
		}
		else if(Cave.getZones()[i][j].getType() == 'P') {
			myZone.getSamus().setScore(-1000);
			myZone.getSamus().setHealth(myZone.getSamus().getHealth() - myZone.getSamus().getHealth());
			exploredMap[i][j].setType('P');
			WindowMaker.setGameInfoText(Integer.toString(myZone.getSamus().getScore()), Integer.toString(myZone.getSamus().getActionsTaken()));
			WindowMaker.setLifeBarValue(myZone.getSamus().getHealth());
			
			return true;
		}
		else if(Cave.getZones()[i][j].getType() == 'U') {
			//myZone.getSamus().setScore(-1);
			if(myZone.getSamus().getHealth() <= 80) {
				Cave.getZones()[i][j].setType('.');
				exploredMap[i][j].setType('.');
				myZone.getSamus().setHealth(20);
				WindowMaker.setLifeBarValue(myZone.getSamus().getHealth());
			}
			else {
				exploredMap[i][j].setType('U');
				energyZones.add(exploredMap[i][j]);
			}
			
			return true;
		}
		else if(Cave.getZones()[i][j].getType() == 'd' || Cave.getZones()[i][j].getType() == 'D') {
			
			if(Cave.getZones()[i][j].getType() == 'd') {
				myZone.getSamus().setScore(-20);
				myZone.getSamus().setHealth(-20);
				exploredMap[i][j].setType('d');
				WindowMaker.setLifeBarValue(myZone.getSamus().getHealth());
			}
			else {
				myZone.getSamus().setScore(-50);
				myZone.getSamus().setHealth(-50);
				exploredMap[i][j].setType('D');
				WindowMaker.setLifeBarValue(myZone.getSamus().getHealth());
			}
			
			if(!enemyZones.contains(exploredMap[i][j]))
				enemyZones.add(exploredMap[i][j]);
			
			return false;
		}
		else if(Cave.getZones()[i][j].getType() == 'T') {
			
			// TODO RANDOM NEW POSITION METHOD CALLING HERE
			
			exploredMap[i][j].setType('T');
			
			if(!enemyZones.contains(exploredMap[i][j]))
				enemyZones.add(exploredMap[i][j]);
			
			return false;
		}
		else if(Cave.getZones()[i][j].getType() == 'W') {
			
			// TODO RANDOM NEW POSITION METHOD CALLING HERE
			
			exploredMap[i][j].setType('T');
			
			if(!enemyZones.contains(exploredMap[i][j]))
				enemyZones.add(exploredMap[i][j]);
			
			return false;
		}
		else if(Cave.getZones()[i][j].getType() == '.') {
			exploredMap[i][j].setType('.');
			
			return true;
		}
		
		System.err.println("INVALID VALUE INSIDE CAVE MATRIX READ BY KNOWN AREA");
		System.exit(0);
		return false;
	}

	public void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		Rectangle2D rt;
		Image im = null;
		
		double xPos, yPos;
		int i, j;
		
		for(i = 0, yPos = 0.0; i < 14; i++, yPos += zoneHeight) {
			for(j = 0, xPos = 0.0; j < 14; j++, xPos += zoneWidth) {
				
				// Fixing my initial column position, because of the walls
				if(j == 1)
					xPos -= zoneWidth/2;
				
				// Cave content "between" the walls
				if(i != 0 && i != 13 && j != 0 && j != 13) {
					
					if(exploredMap[i][j].getType() == 'u') {
						try {
							im = ImageIO.read(new File("img/possible_ground.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					else if(exploredMap[i][j].getType() == '.') {
						try {
							im = ImageIO.read(new File("img/ground.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					
					if(exploredMap[i][j].getType() == 'P') {
						
						rt = new Rectangle2D.Double(xPos, yPos, zoneWidth, zoneHeight);
						g2d.setPaint(Color.BLACK);
						g2d.fill(rt);
					}
					else if(i == 12 && j == 1) {
						
						try {
							im = ImageIO.read(new File("img/exit.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					else if(exploredMap[i][j].getType() == 'p') {
						
						rt = new Rectangle2D.Double(xPos, yPos, zoneWidth, zoneHeight);
						g2d.setPaint(Color.BLUE);
						g2d.fill(rt);
					}
					else if(exploredMap[i][j].getType() == 'U') {
						
						try {
							im = ImageIO.read(new File("img/energy.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					else if(exploredMap[i][j].getType() == 'd') {
						
						try {
							im = ImageIO.read(new File("img/pirate.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					else if(exploredMap[i][j].getType() == 'D') {
						
						try {
							im = ImageIO.read(new File("img/metroid.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					else if(exploredMap[i][j].getType() == 'T') {
						
						try {
							im = ImageIO.read(new File("img/ridley.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					else if(exploredMap[i][j].getType() == 'e') {
						
						try {
							im = ImageIO.read(new File("img/possible_pirate.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					else if(exploredMap[i][j].getType() == 'E') {
						
						try {
							im = ImageIO.read(new File("img/possible_metroid.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					else if(exploredMap[i][j].getType() == 'r') {
						
						try {
							im = ImageIO.read(new File("img/possible_ridley.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					
					if(i == myZone.getI() && j == myZone.getJ()) {
						//System.out.println(i + " : " + j);
						switch(myZone.getSamus().getDirection()) {
						
							case 1:
								try {
									im = ImageIO.read(new File("img/samus_up.png"));
								} catch (IOException e) {
									System.out.println(e.getMessage());
									System.exit(1);
								}
								break;
								
							case 2:
								try {
									im = ImageIO.read(new File("img/samus_down.png"));
								} catch (IOException e) {
									System.out.println(e.getMessage());
									System.exit(1);
								}
								break;
								
							case 3:
								try {
									im = ImageIO.read(new File("img/samus_left.png"));
								} catch (IOException e) {
									System.out.println(e.getMessage());
									System.exit(1);
								}
								break;
								
							case 4:
								try {
									im = ImageIO.read(new File("img/samus_right.png"));
								} catch (IOException e) {
									System.out.println(e.getMessage());
									System.exit(1);
								}
								break;
								
							default:
								System.err.println("WRONG SAMUS POSITION GIVEN");
								System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
				}
				// Cave walls
				else if((i == 0 && j == 0) || (i == 13 && j == 13)) {
					
					if(exploredMap[i][j].getType() == 'w')
						g2d.setPaint(Color.GRAY);
					else if(exploredMap[i][j].getType() == 'W')
						g2d.setPaint(Color.RED);
					
					rt = new Rectangle2D.Double(xPos, yPos, zoneWidth/2, zoneHeight/2);
					g2d.fill(rt);
					
					double xTemp = zoneWidth/2, yTemp = zoneHeight/2;
					
					for(int k = 0; k < 25; k++, xTemp += zoneWidth/2, yTemp += zoneHeight/2) {
						if(i == 0 && j == 0) {
							rt = new Rectangle2D.Double(xPos+xTemp, yPos, zoneWidth/2, zoneHeight/2);
							g2d.fill(rt);
							rt = new Rectangle2D.Double(xPos, yPos+yTemp, zoneWidth/2, zoneHeight/2);
							g2d.fill(rt);
						}
						else {
							rt = new Rectangle2D.Double(xPos-xTemp, yPos, zoneWidth/2, zoneHeight/2);
							g2d.fill(rt);
							rt = new Rectangle2D.Double(xPos, yPos-yTemp, zoneWidth/2, zoneHeight/2);
							g2d.fill(rt);
						}
					}
					
					yPos -= zoneHeight/2;
					
					// Jumping unnecessary iterations
					if(i == 0 && j == 0)
						break;
				}
			}
		}
	}
}