package map;

import javax.swing.JPanel;

public class KnownArea extends JPanel {

	private static Zone exploredMap[][] = new Zone[12][12];
	
	private int zoneWidth, zoneHeight;
	
	public KnownArea(int zoneWidth, int zoneHeight) {
		
		this.zoneWidth = zoneWidth;
		this.zoneHeight = zoneHeight;
	}
}