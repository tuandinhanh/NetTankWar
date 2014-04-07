interface Ball {
	// Description of common features for Rock, Tank, and Bullet
	// Useful for WarPanel.hitAnItem()
	double getX();

	double getY();

	double getRadius();

	boolean isAlive();
}