import java.awt.*;
import javax.swing.*;

/*
 * Implement a simple 2-player
 * tank battle game.
 *
 * written by mike slattery - mar 2007
 */

public class NetTankWar {

	public static void main(String[] args) {
		JFrame f = new JFrame("TankWar");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		WarPanel panel = new WarPanel();
		f.add(panel, BorderLayout.CENTER);
		f.pack();
		f.setVisible(true);
	}
}
