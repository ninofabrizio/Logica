package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import org.jpl7.Atom;
import org.jpl7.Query;
import org.jpl7.Term;

import map.Cave;
import map.KnownArea;

public class WindowMaker extends JFrame {

	private int DEFAULT_WIDTH;
	private int DEFAULT_HEIGHT;
	
	private int screenWidth;
	private int screenHeight;
	
	private JPanel mainPanel;
	private JPanel mapsPanel;
	private JPanel infoPanel;
	
	private static JProgressBar lifeBar;
	private static JTextArea gameInfoText;
	private static AmmoPanel ammoPanel;
	
	private Cave cave;
	private KnownArea knownArea;
	
	public WindowMaker(int w, int h) {
		
		cave = new Cave(w, h);
		
		getScreenDimensions();
		
		DEFAULT_WIDTH = screenWidth;//(w * 15);
		DEFAULT_HEIGHT = screenHeight;//(h * 15);
	    setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mapsPanel = new JPanel();
		mapsPanel.setLayout(new GridLayout(0,2));
		infoPanel = new JPanel();
		infoPanel.setLayout(new GridLayout(0,2));
		mainPanel.add(mapsPanel, BorderLayout.CENTER);
		mainPanel.add(infoPanel, BorderLayout.SOUTH);
		getContentPane().add(mainPanel);
		
		setMenu();
	}
	
	public void getScreenDimensions() {
		
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		screenWidth = screenSize.width;
		screenHeight = screenSize.height;
	}
	
	public void setMenu() {
		
		JFrame optionFrame = new JFrame();
		JPanel optionPanel = new JPanel();
		int oFrameWidth = 300;
		int oFrameHeight = 100;
		int xPos = (screenWidth - oFrameWidth)/2;
		int yPos = (screenHeight - oFrameHeight)/2;	
		
		optionFrame.setBounds(xPos, yPos, oFrameWidth,oFrameHeight);
		optionFrame.getContentPane().add(optionPanel);
		
		optionPanel.setLayout(new BorderLayout());
		JButton defaultMap = new JButton("LOAD DEFAULT MAP");
		optionPanel.add(defaultMap, BorderLayout.NORTH);
		JButton randomMap = new JButton("GENERATE RANDOM MAP");
		optionPanel.add(randomMap, BorderLayout.SOUTH);
		
		optionFrame.setResizable(true);
		optionFrame.setTitle("Choose an option");
		optionFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		optionFrame.setVisible(true);
		
		randomMap.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) { 
				cave.generateRandomMap();
				optionFrame.setVisible(false);
				setMap();
			}
		});
		
		defaultMap.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) { 
				loadDefaultMap();
				optionFrame.setVisible(false);
				setMap();
			}
		});
	}

	private void loadDefaultMap() {
		
		File file;
		FileReader fr = null;
		BufferedReader br = null;
		
		file = new File("IA_2016.2_mapa.txt");
		
		if (file.canRead() && file.exists()) {
			try {

				fr = new FileReader(file);
				br = new BufferedReader(fr);
				cave.loading(br);
				br.close();
			} 
			catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		else {
			System.exit(1);
		}
	}
	
	private void setMap() {
		
		JPanel samusInfoPanel = new JPanel();
		samusInfoPanel.setLayout(new BorderLayout());
		
		knownArea = cave.getSamusZone().getSamus().getKnownArea();
		
		// First, the button to start the logic
		JButton startWalk = new JButton("START EXPLORING");
		startWalk.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) { 
				
	    		new Thread() {
	                @Override
	                public void run() {
	                	
	                	startWalk.setEnabled(false);
	                	
	                	Query q1 = new Query("consult", new Term[] { new Atom("logic.pl") });
                		q1.hasSolution();
	                	
	                	// TODO This loop is just a test to see how prolog behaves and how to extract values from the returned list
	                	while(true) {
	                		
	                		// TODO HERE WE CALL THE LOGIC CALLING OBJECT METHOD
	                		
	            			Query q2 = new Query("action(X)");
	            		
	            			Map<String, Term>[] solution = q2.allSolutions();
	            		
	            			// These I use to get the values of interest
	            			Character action = null;
	            			int posX = 0, posY = 0, direction = 0, health = 0, ammo = 0, score = 0;
	            			
	            			if (solution != null) {
	            				//System.out.println(q2.toString());
	            				
	            				// For every solution returned by prolog
	            				for (int i = 0; i < solution.length; i++) {
	            					
	            					System.out.println("\nX" + i + " = " + solution[i].get("X").toString());
	            					//System.out.println("X length = " + solution[i].get("X").listLength());
	            					//System.out.println(solution[i].get("X").arg(2).arg(2).arg(2).toString());
	            					
	            					// Extracting term, because, head is 1 value, body is another term
	            					Term term = solution[i].get("X");
	            					for (int j = 0; j < solution[i].get("X").listLength(); j++, term = term.arg(2)) {
	            						
	            						// Here enters the actions (letter)
	            						if(j == 0 && term.arg(1).toString().contains("'")) {
	            							//System.out.println(term.arg(1).toString().charAt(1));
	            							action = term.arg(1).toString().charAt(1);
	            						}
	            						// By being second iteration, I know that first argument is a list of [i,j] position
	            						else if(j == 1 && action == 'M') {
	            							//System.out.println(term.arg(1).arg(1).toString());
	            							posX = Integer.parseInt(term.arg(1).arg(1).toString());
	            							//System.out.println(term.arg(1).arg(2).toString());
	            							posY = Integer.parseInt(term.arg(1).arg(2).toString());
	            						}
	            						
	            						// And here the rest (numbers)
	            						else {
	            							//System.out.println(term.arg(1).toString());
	            							if(j == 2)
	            								direction = Integer.parseInt(term.arg(1).toString());
	            							else if(j == 3)
	            								health = Integer.parseInt(term.arg(1).toString());
	            							else if(j == 4)
	            								ammo = Integer.parseInt(term.arg(1).toString());
	            							else if(j == 5)
	            								score = Integer.parseInt(term.arg(1).toString());
	            						}
	            					}
	            				}
	            				
	            				posX = 13 - posX;
	            				//posY = 13 - posY;
	            				
	            				//System.out.println(action);
	            				//System.out.println(direction);
	            				//System.out.println(health);
	            				//System.out.println(ammo);
	            				//System.out.println(score);
	            				
	            				// Updating my values, by the action taken
	            				if(action == 'M') {
    								cave.getSamusZone().getSamus().setActionsTaken(1);
    	            				cave.getSamusZone().getSamus().setScore(-1);
    	            				setGameInfoText(Integer.toString(cave.getSamusZone().getSamus().getScore()), Integer.toString(cave.getSamusZone().getSamus().getActionsTaken()));
    	            				
    	            				cave.getSamusZone().getSamus().setI(posX);
    	            				cave.getSamusZone().getSamus().setJ(posY);
    	            				
    	            				Cave.getZones()[posX][posY].setSamus(cave.getSamusZone().getSamus());
    	            				cave.getSamusZone().setSamus(null);
    	            				cave.setSamusZone(Cave.getZones()[posX][posY]);
    							}
	            				else if(action == 'D') {
    								cave.getSamusZone().getSamus().setActionsTaken(1);
    	            				cave.getSamusZone().getSamus().setScore(-1);
    	            				setGameInfoText(Integer.toString(cave.getSamusZone().getSamus().getScore()), Integer.toString(cave.getSamusZone().getSamus().getActionsTaken()));
    	            				
    	            				cave.getSamusZone().getSamus().setDirection(direction);
    							}
	            				
	            				cave.repaint();
	            			}
	            		
	            			try {
	            				sleep(600);
                			} catch (InterruptedException e) {
                				e.printStackTrace();
                			}
	                	}
	                	
	                	// TODO Just testing my thread here
	                	/*for(int i = 0; i <= 5; i++) {
	                		cave.getZones()[i+1][0].setType('D');
	                		cave.repaint();
	                		setLifeBarValue(i);
	                		setGameInfoText(Integer.toString(i), Integer.toString(i+1));
	                		setAmmoPanelValue(i);
	                		try {
	                			sleep(1000);
	                		} catch (InterruptedException e) {
	                			e.printStackTrace();
	                		}
	                	}
	                	startWalk.setEnabled(true);*/
	                }
	    		}.start();
			}
		});
		infoPanel.add(startWalk);
		
		// Second, the life bar status
	    lifeBar = new JProgressBar();
	    lifeBar.setStringPainted(true);
	    setLifeBarValue(100);
	    lifeBar.setMinimum(0);
	    lifeBar.setMaximum(100);
	    lifeBar.setValue(100);
	    lifeBar.setForeground(Color.DARK_GRAY);
	    lifeBar.setBackground(Color.BLACK);
	    infoPanel.add(lifeBar);
	    
	    // Third, the general info
		gameInfoText = new JTextArea();
		gameInfoText.setLineWrap(true);
		gameInfoText.setWrapStyleWord(true);
		gameInfoText.setEditable(false);
		setGameInfoText(Integer.toString(0), Integer.toString(0));
		infoPanel.add(gameInfoText);
		
		// Forth, the remaining ammo
		ammoPanel = new AmmoPanel(cave.getSamusZone().getSamus().getAmmoLeft());
		infoPanel.add(ammoPanel);
		
		mapsPanel.add(cave);
		mapsPanel.add(knownArea);
		
		int xPos = (screenWidth - DEFAULT_WIDTH)/2;
		int yPos = (screenHeight - DEFAULT_HEIGHT)/2;
		
		setBounds(xPos, yPos, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setResizable(true);
		setTitle("Not a Metroid® Game");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public static void setLifeBarValue(int newValue) {
		lifeBar.setValue(newValue);
		lifeBar.setString("Health = " + newValue);
	}
	
	public static void setGameInfoText(String score, String action) {
		gameInfoText.setText("GENERAL INFO:\n-SCORE = " + score + "\n-NUMBER OF ACTIONS TAKEN = " + action);
	}
	
	public static void setAmmoPanelValue(int ammo) {
		ammoPanel.setAmmoCount(ammo);
		ammoPanel.repaint();
	}
}