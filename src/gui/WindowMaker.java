package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import map.Cave;
import map.KnownArea;

public class WindowMaker extends JFrame {

	private int DEFAULT_WIDTH;
	private int DEFAULT_HEIGHT;
	
	int screenWidth;
	int screenHeight;
	
	private JFrame optionFrame;
	private JPanel optionPanel;
	private JPanel infoPanel;
	private JButton randomMap;
	private JButton defaultMap;
	private JButton startWalk;
	
	private Cave cave;
	private KnownArea knownArea;
	
	public WindowMaker(int w, int h) {
		
		DEFAULT_WIDTH = (w * 12) + (w/2) + 200;
		DEFAULT_HEIGHT = (h * 12) + (2*h);
		
		cave = new Cave(w, h);
		knownArea = new KnownArea(w, h);
		
		getScreenDimensions();
		
		infoPanel = new JPanel();
		infoPanel.setLayout(new BorderLayout());
		infoPanel.setBounds((w * 12), 0, 200, (h * 12) - (2 * h));
		
		setMenu();
	}
	
	public void getScreenDimensions() {
		
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		screenWidth = screenSize.width;
		screenHeight = screenSize.height;
	}
	
	public void setMenu() {
		
		optionFrame = new JFrame();
		optionPanel = new JPanel();
		int oFrameWidth = 300;
		int oFrameHeight = 100;
		int xPos = (screenWidth - oFrameWidth)/2;
		int yPos = (screenHeight - oFrameHeight)/2;	
		
		optionFrame.setBounds(xPos, yPos, oFrameWidth,oFrameHeight);
		optionFrame.getContentPane().add(optionPanel);
		
		optionPanel.setLayout(new BorderLayout());
		defaultMap = new JButton("LOAD DEFAULT MAP");
		optionPanel.add(defaultMap, BorderLayout.NORTH);
		randomMap = new JButton("GENERATE RANDOM MAP");
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
		
		file = new File("mapa.txt");
		
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
		
		int xPos = (screenWidth - DEFAULT_WIDTH)/2;
		int yPos = (screenHeight - DEFAULT_HEIGHT)/2;
		
		setBounds(xPos, yPos, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setResizable(true);
		setTitle("Samus' Space Adventure");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		startWalk = new JButton("START EXPLORING");
		infoPanel.add(startWalk, BorderLayout.PAGE_END);
		
		startWalk.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) { 
				
	    		new Thread() {
	                @Override
	                public void run() {
	                	// HERE WE CALL THE LOGIC CALLING METHOD
	                	startWalk.setEnabled(false);
	                }
	    		}.start();
			}
		});
		
		/*JTextArea ta = new JTextArea();
		ta.setBounds(50, 0, 300, 300);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setEditable(false);
		ta.setText("Here we will put all the info:\n-Candies with quantity\n-Total time");
		infoPanel.add(ta);*/
		
		getContentPane().add(infoPanel);
		getContentPane().add(cave);
		//getContentPane().add(cave.getSamusZone().getSamus().getKnownArea());
		
		setVisible(true);
	}
}