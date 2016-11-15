package map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import characters.Enemy;
import characters.Samus;

public class Cave extends JPanel {
	
	// TODO Dictionary for extra types:
	// 'o' == gold taken
	// 'h' == health taken
	// 'p' == pirate (small enemy) killed
	// 'm' == metroid (big enemy) killed
	// 'r' == ridley (teleport) killed
	
	// Mind that our "real" map is 12x12, the extra rooms represent the walls
	private static Zone caveMap[][] = new Zone[14][14];
	private Zone samusZone;
	
	//private static ArrayList<Double> wolfZones = new ArrayList<Double>();
	//public static Map<Character, Double> caveMapsCosts = new HashMap<Character, Double>();
	
	private int zoneWidth, zoneHeight;
	
	public Cave(int w, int h) {
		
		zoneWidth = w;
		zoneHeight = h;
	}
	
	public static Zone[][] getZones() {	

		return caveMap;
	}

	public Zone getSamusZone() {
		
		return samusZone;
	}
	
	public void setSamusZone(Zone zone) {
		
		samusZone = zone;
	}
	
	public void loading(BufferedReader br) throws IOException {
		
		String s;
		char line[] = null;
		
		for(int i = 0; i < 14; i++) {
			
			if(i > 0 && i <= 12) {
				s = br.readLine();
				line = s.toCharArray();
			}
			
			for(int j = 0; j < 14; j++) {
				
				caveMap[i][j] = new Zone();
				if(i != 0 && i != 13 && j != 0 && j != 13)
					caveMap[i][j].setType(line[j-1]);
				else
					caveMap[i][j].setType('W');
				caveMap[i][j].setI(i);
				caveMap[i][j].setJ(j);
				
				// Our position [1,1] in the matrix
				if(i == 12 && j == 1) {
					
					caveMap[i][j].setSamus(new Samus(i, j, new KnownArea(zoneWidth, zoneHeight)));
					samusZone = caveMap[i][j];
					samusZone.getSamus().getKnownArea().getExploredMap()[i][j].setSamus(samusZone.getSamus());
					samusZone.getSamus().getKnownArea().setMyZone(samusZone.getSamus().getKnownArea().getExploredMap()[i][j]);
					samusZone.getSamus().getKnownArea().getMyZone().setVisited();
					samusZone.setType('.');
				}
				else {
					caveMap[i][j].setSamus(null);
					
					if(caveMap[i][j].getType() == 'd' || caveMap[i][j].getType() == 'D'
						|| caveMap[i][j].getType() == 'T')
						caveMap[i][j].setEnemy(new Enemy(i,j));
					else
						caveMap[i][j].setEnemy(null);
				}
			}
		}
		
		// TODO PRINT TEST
		/*System.out.println("\nCAVE MAP:");
		for(int i = 0; i < 14; i++) {
			for(int j = 0; j < 14; j++)
				System.out.print(caveMap[i][j].getType() + " ");
			System.out.println();
		}*/
	}

	public void generateRandomMap() {
		
		for(int i = 0; i < 14; i++) {
			for(int j = 0; j < 14; j++) {
				
				caveMap[i][j] = new Zone();
				if(i == 0 || i == 13 || j == 0 || j == 13)
					caveMap[i][j].setType('W');
				else
					caveMap[i][j].setType('.');
				caveMap[i][j].setI(i);
				caveMap[i][j].setJ(j);
				
				// Our position [1,1] in the matrix
				if(i == 12 && j == 1) {
					
					caveMap[i][j].setSamus(new Samus(i, j, new KnownArea(zoneWidth, zoneHeight)));
					samusZone = caveMap[i][j];
					samusZone.getSamus().getKnownArea().getExploredMap()[i][j].setSamus(samusZone.getSamus());
					samusZone.getSamus().getKnownArea().setMyZone(samusZone.getSamus().getKnownArea().getExploredMap()[i][j]);
					samusZone.getSamus().getKnownArea().getMyZone().setVisited();
				}
				else
					caveMap[i][j].setSamus(null);
			}
		}
		
		generateRandomPosition(2, 'd');
		generateRandomPosition(2, 'D');
		generateRandomPosition(4, 'T');
		generateRandomPosition(8, 'P');
		generateRandomPosition(3, 'U');
		generateRandomPosition(3, 'O');
		
		for(int i = 0; i < 14; i++) {
			for(int j = 0; j < 14; j++) {
				if(caveMap[i][j].getType() == 'd' || caveMap[i][j].getType() == 'D'
					|| caveMap[i][j].getType() == 'T')
					caveMap[i][j].setEnemy(new Enemy(i,j));
				else
					caveMap[i][j].setEnemy(null);
			}
		}
		
		// TODO PRINT TEST
		/*System.out.println("\nCAVE MAP:");
		for(int i = 0; i < 14; i++) {
			for(int j = 0; j < 14; j++)
				System.out.print(caveMap[i][j].getType() + " ");
			System.out.println();
		}*/
	}
	
	private void generateRandomPosition(int num, char type) {
		
		for(int k = 1; k <= num; k++) {
			
			int i = 12, j = 1;
			while((i == 12 && j == 1) || caveMap[i][j].getType() != '.' || sameNeighbors(type, i, j)) {
				i = ThreadLocalRandom.current().nextInt(1, 12 + 1);
				j = ThreadLocalRandom.current().nextInt(1, 12 + 1);
			}
			
			caveMap[i][j].setType(type);
		}
	}

	private boolean sameNeighbors(char type, int i, int j) {
		
		//if(type == 'd' || type == 'D' || type == 'T' || type == 'P') {
			// Checking closest neighbors
			if(caveMap[i+1][j].getType() == type || caveMap[i-1][j].getType() == type
					|| caveMap[i][j+1].getType() == type || caveMap[i][j-1].getType() == type
					|| caveMap[i+1][j+1].getType() == type || caveMap[i+1][j-1].getType() == type
					|| caveMap[i-1][j-1].getType() == type || caveMap[i-1][j+1].getType() == type)
				return true;
			
			// Checking farthest neighbors
			if((i - 2 >= 0) && caveMap[i-2][j].getType() == type)
				return true;
			if((i + 2 <= 13) && caveMap[i+2][j].getType() == type)
				return true;
			if((j - 2 >= 0) && caveMap[i][j-2].getType() == type)
				return true;
			if((j + 2 <= 13) && caveMap[i][j+2].getType() == type)
				return true;
		//}
			
		// To avoid mixed "feelings" from neighbors
		/*if(type == 'd' || type == 'D' || type == 'T' || type == 'P')
			if(caveMap[i+1][j+1].getType() == 'd' || caveMap[i+1][j-1].getType() == 'd'
				|| caveMap[i-1][j-1].getType() == 'd' || caveMap[i-1][j+1].getType() == 'd'
				|| caveMap[i+1][j+1].getType() == 'D' || caveMap[i+1][j-1].getType() == 'D'
				|| caveMap[i-1][j-1].getType() == 'D' || caveMap[i-1][j+1].getType() == 'D'
				|| caveMap[i+1][j+1].getType() == 'T' || caveMap[i+1][j-1].getType() == 'T'
				|| caveMap[i-1][j-1].getType() == 'T' || caveMap[i-1][j+1].getType() == 'T'
				|| caveMap[i+1][j+1].getType() == 'P' || caveMap[i+1][j-1].getType() == 'P'
				|| caveMap[i-1][j-1].getType() == 'P' || caveMap[i-1][j+1].getType() == 'P')
					return true;*/
			
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
					try {
						im = ImageIO.read(new File("img/ground.png"));
					} catch (IOException e) {
						System.out.println(e.getMessage());
						System.exit(1);
					}
					g.drawImage(im, (int)xPos, (int)yPos, null);
					
					if(caveMap[i][j].getType() == 'P') {
						
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
					else if(caveMap[i][j].getType() == 'O' || caveMap[i][j].getType() == 'o') {
						
						try {
							im = ImageIO.read(new File("img/gold.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					else if(caveMap[i][j].getType() == 'U' || caveMap[i][j].getType() == 'h') {
						
						try {
							im = ImageIO.read(new File("img/energy.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					else if(caveMap[i][j].getType() == 'd' || caveMap[i][j].getType() == 'p') {
						
						try {
							im = ImageIO.read(new File("img/pirate.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					else if(caveMap[i][j].getType() == 'D' || caveMap[i][j].getType() == 'm') {
						
						try {
							im = ImageIO.read(new File("img/metroid.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					else if(caveMap[i][j].getType() == 'T' || caveMap[i][j].getType() == 'r') {
						
						try {
							im = ImageIO.read(new File("img/ridley.png"));
						} catch (IOException e) {
							System.out.println(e.getMessage());
							System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
					
					if(i == samusZone.getI() && j == samusZone.getJ()) {
						//System.out.println(samusZone.getSamus().getDirection());
						switch(samusZone.getSamus().getDirection()) {
						
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
								System.err.println("WRONG SAMUS DIRECTION GIVEN");
								System.exit(1);
						}
						g.drawImage(im, (int)xPos, (int)yPos, null);
					}
				}
				// Cave walls
				else if((i == 0 && j == 0) || (i == 13 && j == 13)) {
					
					rt = new Rectangle2D.Double(xPos, yPos, zoneWidth/2, zoneHeight/2);
					g2d.setPaint(Color.RED);
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