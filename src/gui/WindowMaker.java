package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

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
		
		DEFAULT_WIDTH = (w * 15);
		DEFAULT_HEIGHT = (h * 15);
		
		cave = new Cave(w, h);
		
		getScreenDimensions();
		
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
	                	
	                	// TODO HERE WE CALL THE LOGIC CALLING OBJECT METHOD
	                	
	                	startWalk.setEnabled(false);
	                	
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
	    lifeBar.setString("Health = 100");
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
		gameInfoText.setText("GENERAL GAME INFO:\n-SCORE = 0\n-NUMBER OF ACTIONS TAKEN = 0");
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
		gameInfoText.setText("GENERAL GAME INFO:\n-SCORE = " + score + "\n-NUMBER OF ACTIONS TAKEN = " + action);
	}
	
	public static void setAmmoPanelValue(int ammo) {
		ammoPanel.setAmmoCount(ammo);
		ammoPanel.repaint();
	}
}