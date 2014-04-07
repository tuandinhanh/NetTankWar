// PlayerServerHandler.java
// written by mike slattery - mar 2007
// Based on code by
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/* Relay messages from the client
 to the other player.
 */

import java.net.*;
import java.io.*;

public class PlayerServerHandler extends Thread {
	private NetWarServer server;
	private Socket clientSock;
	private BufferedReader in;
	private PrintWriter out;

	private int playerID; // this player id is assigned by Server

	public PlayerServerHandler(int pid, Socket s, NetWarServer serv) {
		playerID = pid;
		clientSock = s;
		server = serv;
		System.out.println("Player connected");
		try {
			in = new BufferedReader(new InputStreamReader(
					clientSock.getInputStream()));
			out = new PrintWriter(clientSock.getOutputStream(), true); // autoflush
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/*
	 * start processing client-side input
	 */
	public void run() {
		processPlayerInput();

		try { // close socket from player
			clientSock.close();
			System.out.println("Handler: Player " + playerID
					+ " connection closed\n");
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	/*
	 * Stop when the input stream closes (is null) or "disconnect" is sent.
	 * Otherwise send the message to the other player.
	 */
	private void processPlayerInput() {
		String line;
		boolean done = false;
		try {
			while (!done) {
				if ((line = in.readLine()) == null)
					done = true;
				else {
					System.out.println("Player " + playerID + " got msg: "
							+ line);
					if (line.trim().equals("disconnect"))
						done = true;
					else
						server.tellOther(playerID, line); // pass on message
				}
			}
		} catch (IOException e) {
			System.out.println("Player " + playerID + " closed the connection");
		}
	}

	// called by handler and top-level server
	synchronized public void sendMessage(String msg) {
		System.out.println("Handler: Msg to player " + playerID + " " + msg);
		try {
			out.println(msg);
		} catch (Exception e) {
			System.out.println("Handler for player " + playerID + "\n" + e);
		}
	}

}
