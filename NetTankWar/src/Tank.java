import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.util.Scanner;

class Tank implements Ball {

	double locX, locY, radius, angle;
	int self; // index of this tank in WarPanel.tanks
	public boolean turnL, turnR, forth, back, fire;
	boolean prevtL, prevtR, prevfo;
	Color color;
	Image image;

	public static final double twoPi = Math.PI * 2.0;
	public static final double turnRate = Math.PI / 8;
	public static final double speed = 4.0;
	public static final int RELOAD = 8; // delay between bullets
	int count; // timer for reloading

	public static final int MAXBULLETS = 7; // max simultaneous shots
	Bullet bullets[] = new Bullet[MAXBULLETS];

	AffineTransform saveAT; // place to hold current affine transform

	public Tank(double x, double y, double a, int index, Image im) {
		locX = x;
		locY = y;
		angle = a;
		self = index;
		image = im;
		radius = 22;
		// create bullets for this tank
		for (int i = 0; i < bullets.length; i++)
			bullets[i] = new Bullet(self);
	}

	public double getX() {
		return locX;
	}

	public double getY() {
		return locY;
	}

	public double getRadius() {
		return radius;
	}

	public boolean isAlive() {
		return true;
	}

	void update(Boolean local) {
		if (turnL)
			turnLeft(turnRate);
		if (turnR)
			turnRight(turnRate);
		if (forth) {
			moveForward();
			// Check for rocks
			if (WarPanel.hitAnItem(this, WarPanel.rocks) >= 0)
				backUp();
		}
		if (local) {
			if (turnL != prevtL) {
				WarPanel.send("turnL " + turnL + " " + locX + " " + locY + " " + angle);
				prevtL = turnL;
			}
			if (turnR != prevtR) {
				WarPanel.send("turnR " + turnR + " " + locX + " " + locY + " " + angle);
				prevtR = turnR;
			}
			if (forth != prevfo) {
				WarPanel.send("forth " + forth + " " + locX + " " + locY + " " + angle);
				prevfo = forth;
			}
		}
		if (fire) {
			WarPanel.send("fire");
			fireBullet();
		}
		// Update all of our bullets
		for (Bullet b : bullets)
			b.update();
	}

	public void processMove(String s) {
		// Update movement parameters based on s
		Scanner sc = new Scanner(s);
		// Get the flag change
		String command = sc.next();
		boolean value = sc.nextBoolean();
		if (command.equals("turnL"))
			turnL = value;
		else if (command.equals("turnR"))
			turnR = value;
		else if (command.equals("forth"))
			forth = value;
		else
			System.out.println("Unexpected move: " + command);
		// then unpack position update
		locX = sc.nextDouble();
		locY = sc.nextDouble();
		angle = sc.nextDouble();
	}

	void paint(Graphics g) {
		// Use the affine transform feature in Graphics2D
		// to easily rotate the tank's image.
		Graphics2D g2 = (Graphics2D) g;
		saveAT = g2.getTransform();
		g2.translate(locX, locY);
		g2.rotate(angle);
		g2.drawImage(image, (int) (-radius), (int) (-radius), null);
		// Reset the transform (this is important)
		g2.setTransform(saveAT);
		// Then draw bullets
		for (Bullet b : bullets)
			b.paint(g2);

	}

	void fireBullet() {
		// If it has been long enough since the last shot...
		count--;
		if (count > 0)
			return;
		// ...and if all the bullets aren't currently in use...
		int slot = getAvailableBullet();
		if (slot < 0)
			return;
		// ...then launch a new bullet
		bullets[slot].setLocation(locX, locY);
		bullets[slot].setDirection(angle);
		bullets[slot].reset();
		// Reset the timer
		count = RELOAD;
	}

	int getAvailableBullet() {
		for (int i = 0; i < bullets.length; i++)
			if (!bullets[i].isAlive())
				return i;
		return -1;
	}

	void turnRight(double a) {
		angle += a;
		if (angle > twoPi)
			angle -= twoPi;
	}

	void turnLeft(double a) {
		angle -= a;
		if (angle < 0.0)
			angle += twoPi;
	}

	void moveForward() {
		locX += speed * Math.cos(angle);
		locY += speed * Math.sin(angle);
	}

	void backUp() {
		locX -= speed * Math.cos(angle);
		locY -= speed * Math.sin(angle);
	}

}
