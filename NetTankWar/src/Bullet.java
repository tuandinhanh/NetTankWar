import java.awt.Color;
import java.awt.Graphics;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

class Bullet implements Ball {
	double locX, locY, dx, dy;
	int tank; // index of tank that fired this bullet
	boolean alive = false;
	int ttl; // time to live
	public static final int LIFETIME = 70;
	public static final double SPEED = 8.0;
	public static final int radius = 7;

	public Bullet(int t) {
		tank = t;
	}

	void update() {
		int i;
		// Check if this bullet is worn out
		ttl--;
		if (ttl < 0)
			alive = false;
		if (!alive)
			return;
		// If not worn out, update position
		locX += SPEED * dx;
		locY += SPEED * dy;
		// check for collisions with rocks
		i = WarPanel.hitAnItem(this, WarPanel.rocks);
		if (i >= 0) {
			alive = false;
			// Ask the game to deactivate this rock
			WarPanel.removeRock(i);
			try {
			    Clip tankExplosionSound = AudioSystem.getClip();
			    AudioInputStream inputStream = AudioSystem.getAudioInputStream(this.getClass().getClassLoader().getResourceAsStream("rockHit.wav"));
			    tankExplosionSound.open(inputStream);
			    tankExplosionSound.start();
			    tankExplosionSound.drain();
			} catch(Exception e) {
				System.out.println(e);
			}
		}
		// check for collisions with tanks (other than
		// our tank)
		i = WarPanel.hitAnItem(this, WarPanel.tanks);
		if ((i >= 0) && (i != tank)) {
			alive = false;
			// Tell game a tank was hit
			WarPanel.tankHit(i);
		}
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

	void setLocation(double x, double y) {
		locX = x;
		locY = y;
	}

	void setDirection(double angle) {
		dx = Math.cos(angle);
		dy = Math.sin(angle);
	}

	void paint(Graphics g) {
		if (alive) {
			g.setColor(Color.black);
			g.fillOval((int) (locX - radius), (int) (locY - radius),
					2 * radius, 2 * radius);
		}
	}

	public boolean isAlive() {
		return alive;
	}

	void reset() {
		ttl = LIFETIME;
		alive = true;
	}
}