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

public class KnownArea extends JPanel {

	// TODO Dictionary for extra types:
	// 'u' == unknown/unexplored zone
	// 'p' == possible hole
	// 'e' == possible Pirate (small enemy) or Metroid (big enemy)
	// 'r' == possible Ridley (teleport)
	// 'w' == unknown wall
	
	private static Zone exploredMap[][] = null;
	private Zone myZone;
	
	private int zoneWidth, zoneHeight;
	
	/*
	 * TODO:
	 * - Methods listed below in verifyValidPosition
	 * - Method to check and update our holeZones list for each step
	 * - Some other methods we'll still need...
	 * - Adjust wall cells that are corners when It's walls neighbors are both marked as known walls ('W'), just to paint it right
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
		
		// TODO PRINT TEST
		/*System.out.println("\nKNOWN MAP:");
		for(int i = 0; i < 14; i++) {
			for(int j = 0; j < 14; j++)
				System.out.print(exploredMap[i][j].getType() + " ");
			System.out.println();
		}*/
	}
	
	public void setMyZone(Zone zone) {
		myZone = zone;
	}
	
	public Zone getMyZone() {
		return myZone;
	}
	
	public Zone[][] getExploredMap() {
		return exploredMap;
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
					
					if(!exploredMap[i][j].isVisited()) {
						try {
							im = ImageIO.read(new File("img/possible_ground.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					else {
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
							im = ImageIO.read(new File("img/possible_metroid.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
						try {
							im = ImageIO.read(new File("img/possible_pirate.png"));
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
									if(myZone.getSamus().getHealth() > 0)
										im = ImageIO.read(new File("img/samus_up.png"));
									else
										im = ImageIO.read(new File("img/samus_up_dead.png"));
								} catch (IOException e) {
									System.out.println(e.getMessage());
									System.exit(1);
								}
								break;
								
							case 2:
								try {
									if(myZone.getSamus().getHealth() > 0)
										im = ImageIO.read(new File("img/samus_down.png"));
									else
										im = ImageIO.read(new File("img/samus_down_dead.png"));
								} catch (IOException e) {
									System.out.println(e.getMessage());
									System.exit(1);
								}
								break;
								
							case 3:
								try {
									if(myZone.getSamus().getHealth() > 0)
										im = ImageIO.read(new File("img/samus_left.png"));
									else
										im = ImageIO.read(new File("img/samus_left_dead.png"));
								} catch (IOException e) {
									System.out.println(e.getMessage());
									System.exit(1);
								}
								break;
								
							case 4:
								try {
									if(myZone.getSamus().getHealth() > 0)
										im = ImageIO.read(new File("img/samus_right.png"));
									else
										im = ImageIO.read(new File("img/samus_right_dead.png"));
								} catch (IOException e) {
									System.out.println(e.getMessage());
									System.exit(1);
								}
								break;
								
							default:
								System.err.println("WRONG SAMUS DIRECTION GIVEN");
								System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
				}
				// Cave walls
				else if(((i == 0 || i == 13) && (j >= 0 && j <= 13)) || ((j == 0 || j == 13) && (i >= 0 && i <= 13))) {
					
					if(exploredMap[i][j].getType() == 'w')
						g2d.setPaint(Color.GRAY);
					else if(exploredMap[i][j].getType() == 'W')
						g2d.setPaint(Color.RED);
					
					double xTemp = zoneWidth/2, yTemp = zoneHeight/2;

					rt = new Rectangle2D.Double(xPos, yPos, zoneWidth/2, zoneHeight/2);
					g2d.fill(rt);
					
					if(j == 0 || j == 13)
						rt = new Rectangle2D.Double(xPos, yPos+yTemp, zoneWidth/2, zoneHeight/2);
					else if (i == 0 || i == 13)
						rt = new Rectangle2D.Double(xPos+xTemp, yPos, zoneWidth/2, zoneHeight/2);
					g2d.fill(rt);
					
					if(i == 0 && j == 13)
						yPos -= zoneHeight/2;
				}
			}
		}
	}
}