
import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

class Rock implements Ball {
	// Obstacles scattered about the play field.
	// Create a random generator to use for rock sizes and
	// placement
	public static Random rockGen = new Random();

	int locX, locY, diameter, radius; // int properties easier to
	boolean alive = true; // send over network

	public Rock(int x, int y, int minD, int maxD) {
		locX = x;
		locY = y;
		diameter = minD + rockGen.nextInt(maxD - minD + 1);
		radius = diameter / 2;
	}

	public Rock(int x, int y, int d) {
		locX = x;
		locY = y;
		diameter = d;
		radius = diameter / 2;
	}

	void demolish() {
		// "Turn off" this rock - won't paint or be hit
		alive = false;
	}

	public boolean isAlive() {
		return alive;
	}

	public double getX() {
		return (double) locX;
	}

	public double getY() {
		return (double) locY;
	}

	public double getRadius() {
		return (double) radius;
	}

	public int getIntX() {
		return locX;
	}

	public int getIntY() {
		return locY;
	}

	public int getDiameter() {
		return diameter;
	}

	void paint(Graphics g) {
		if (alive) {
			g.setColor(Color.gray);
			g.fillOval(locX - radius, locY - radius, diameter, diameter);
		}
	}
}