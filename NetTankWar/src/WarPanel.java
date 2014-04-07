import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.net.*;
import java.io.*;

/*
 * Implement a simple 2-player
 * tank battle game.
 *
 * written by mike slattery - mar 2007
 */

public class WarPanel extends JPanel implements Runnable {

	Thread anim = null; // animation thread

	public static ArrayList<Rock> rocks; // obstacles on the field
	public static ArrayList<Tank> tanks;
	public static final int RED = 0;
	public static final int BLUE = 1;

	int playerID = -1; // my subscript in tank array

	Image offscreen = null;
	Graphics offgr;

	Image redtank;
	Image bluetank;

	public static final int PWIDTH = 800;
	public static final int PHEIGHT = 600;

	static boolean roundOver = true;
	static int loser;

	static boolean ready = false;

	Font font = new Font("Monospaced", Font.BOLD, 30);

	private Socket sock;
	private static PrintWriter out;

	private static final int PORT = 1234; // server details
	private static final String HOST = "localhost";

	public WarPanel() {
		super();
		setPreferredSize(new Dimension(PWIDTH, PHEIGHT));
		redtank = new ImageIcon(this.getClass().getClassLoader()
				.getResource("redtank.png")).getImage();
		bluetank = new ImageIcon(this.getClass().getClassLoader()
				.getResource("bluetank.png")).getImage();
		addKeyListener(new KeyL());
		addMouseListener(new MseL());

		makeContact();
	}

	// contact the NetWarServer
	private void makeContact() {
		try {
			sock = new Socket(HOST, PORT);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true); // autoflush

			new NetWarWatcher(this, in).start(); // start watching for server
													// msgs

		} catch (Exception e) {
			System.out.println("Cannot contact the NetTankWar Server");
			System.exit(0);
		}
	}

	// send a message to the other player (via server)
	public static void send(String msg) {
		out.println(msg);
	}

	void resetRound() {
		// Build semi-random rock field
		rocks = new ArrayList<Rock>();
		int edge = (PWIDTH + PHEIGHT) / 20;
		int halfW = PWIDTH / 2;
		int halfH = PHEIGHT / 2;
		placeRocks(40, edge, halfH, halfW, edge, 0.2);
		placeRocks(40, halfW, PHEIGHT - edge, PWIDTH - edge, halfH, 0.2);
		placeRocks(10, halfW, 0, halfW, PHEIGHT, 0.1);

		// Place tanks
		tanks = new ArrayList<Tank>();
		tanks.add(new Tank(PWIDTH - edge, PHEIGHT - edge, Math.PI, RED, redtank));
		tanks.add(new Tank(edge, edge, 0.0, BLUE, bluetank));

		roundOver = false;
		ready = true;
	}

	public void addNotify() {
		super.addNotify();

		offscreen = createImage(PWIDTH, PHEIGHT);
		offgr = offscreen.getGraphics();

		anim = new Thread(this);
		anim.start();
	}

	public void setPlayerID(int id) {
		playerID = id;
	}

	public int getPlayerID() {
		return playerID;
	}

	public void setRocks(String config) {
		// Read rock string from other player
		stringToRocks(config);
		// Place tanks
		int edge = (PWIDTH + PHEIGHT) / 20;
		tanks = new ArrayList<Tank>();
		tanks.add(new Tank(PWIDTH - edge, PHEIGHT - edge, Math.PI, RED, redtank));
		tanks.add(new Tank(edge, edge, 0.0, BLUE, bluetank));

		roundOver = false;
		ready = true;
	}

	public void sendRocks() {
		// Lay out rocks and send to other player
		resetRound();
		out.println("rocks " + rocksToString());
	}

	void placeRocks(int n, int x1, int y1, int x2, int y2, double aspect) {
		// place n rocks randomly located within distance r of
		// the line from (x1,y1) to (x2,y2) where r is the length of
		// this line times aspect.
		int x, y;
		double s, tx, ty;

		double len = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		double r = len * aspect;
		for (int i = 0; i < n; i++) {
			s = Rock.rockGen.nextDouble();
			tx = r * (Rock.rockGen.nextDouble() - 0.5);
			ty = r * (Rock.rockGen.nextDouble() - 0.5);
			x = (int) (x1 + s * (x2 - x1) + tx);
			y = (int) (y1 + s * (y2 - y1) + ty);
			rocks.add(new Rock(x, y, (int) (r / 2), (int) r));
		}

	}

	public static void removeRock(int index) {
		// Deactivate the index-th rock in rocks
		rocks.get(index).demolish();
	}

	public static String rocksToString() {
		// Return a string describing the rocks: x1 y1 d1;x2 y2 d2; ...
		StringBuilder sb = new StringBuilder();
		for (Rock r : rocks)
			sb.append(r.getIntX() + " " + r.getIntY() + " " + r.getDiameter()
					+ ";");
		return new String(sb);
	}

	public static void stringToRocks(String s) {
		// read the rocks string and fill in the ArrayList
		int x, y, d;

		rocks = new ArrayList<Rock>();
		String specs[] = s.split(";");
		for (String sp : specs) {
			Scanner sc = new Scanner(sp);
			x = sc.nextInt();
			y = sc.nextInt();
			d = sc.nextInt();
			sc.close();
			rocks.add(new Rock(x, y, d));
		}
	}

	public void processMove(String s) {
		// Send command to tank not controlled by
		// this player
		tanks.get(1 - playerID).processMove(s);
	}

	public static int hitAnItem(Ball b, ArrayList<? extends Ball> c) {
		// Check if b has run into any element of c. If so
		// return the index of the first item that was hit,
		// or -1 if nothing hit.
		// Use generics so the same code can check for collisions
		// with rocks and tanks.
		for (Ball r : c) {
			if (!r.isAlive())
				continue;
			double dx = b.getX() - r.getX();
			double dy = b.getY() - r.getY();
			double bound = b.getRadius() + r.getRadius();
			if ((dx * dx + dy * dy) < (bound * bound))
				return c.indexOf(r);
		}
		return -1;
	}

	public static void tankHit(int k) {
		// If a tank is hit, the round is over
		if (!roundOver) {
			roundOver = true;
			loser = k;
		}
	}

	public void paintComponent(Graphics g) {
		g.drawImage(offscreen, 0, 0, null);
	}

	public void frameRender(Graphics g) {

		// Draw a background
		g.setColor(Color.yellow);
		g.fillRect(0, 0, PWIDTH, PHEIGHT);

		if (!ready) { // Just display the message and return
			g.setColor(Color.black);
			g.setFont(font);
			g.drawString("Waiting for setup...", 200, 250);
			return;
		}

		// Draw rocks
		for (Rock r : rocks)
			r.paint(g);

		// Draw tanks (and their bullets)
		for (Tank t : tanks)
			t.paint(g);

		if (roundOver) {
			g.setColor(Color.black);
			g.setFont(font);
			g.drawString("Round Over: " + (loser == RED ? "Blue" : "Red")
					+ " tank wins!", 150, 200);
			g.drawString("Click mouse to start next round", 150, 250);
		}
	}

	public void run() {

		while (anim != null) {
			if (!roundOver) { // Freeze the action between rounds
				tanks.get(playerID).update(true);
				tanks.get(1 - playerID).update(false);
			}
			frameRender(offgr);
			repaint();
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
			}
		}
	}

	public void stop() {
		anim = null; // stop animation thread
	}

	class KeyL extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			int c = e.getKeyCode();
			switch (c) {
			case KeyEvent.VK_LEFT:
				tanks.get(playerID).turnL = true;
				break;
			case KeyEvent.VK_RIGHT:
				tanks.get(playerID).turnR = true;
				break;
			case KeyEvent.VK_UP:
				tanks.get(playerID).forth = true;
				break;
			case KeyEvent.VK_SPACE:
				tanks.get(playerID).fire = true;
				break;
			}
		}

		public void keyReleased(KeyEvent e) {
			int c = e.getKeyCode();
			switch (c) {
			case KeyEvent.VK_LEFT:
				tanks.get(playerID).turnL = false;
				break;
			case KeyEvent.VK_RIGHT:
				tanks.get(playerID).turnR = false;
				break;
			case KeyEvent.VK_UP:
				tanks.get(playerID).forth = false;
				break;
			case KeyEvent.VK_SPACE:
				tanks.get(playerID).fire = false;
				break;
			}
		}
	}

	class MseL extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			requestFocus();
		}
	}

}