package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class AmmoPanel extends JPanel {
	
	private int ammoCount;
	
	public AmmoPanel(int ammoLeft) {
		ammoCount = ammoLeft;
		setOpaque(true);
		setBackground(Color.BLACK);
	}

	public void setAmmoCount(int ammo) {
		ammoCount = ammo;
	}
	
	public void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		Image im = null;
		
		int xPos;
		int i;
		
		for(i = 0, xPos = 0; i < ammoCount; i++, xPos += 100) {
				
			try {
				im = ImageIO.read(new File("img/ammo.png"));
			} catch (IOException e) {
				System.out.println(e.getMessage());
				System.exit(1);
			}
			g.drawImage(im, xPos, 0, null);
		}
	}
}