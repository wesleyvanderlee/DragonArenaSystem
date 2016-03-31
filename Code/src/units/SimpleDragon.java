package units;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import core.Message;

/**
 * A dragon is a non-playing character, which can't move, has a hitpoint range
 * between 50 and 100 and an attack range between 5 and 20.
 * 
 * Every dragon runs in its own thread, simulating individual behaviour, not
 * unlike a distributed server setup.
 * 
 * @author Pieter Anemaet, Boaz Pat-El
 */
@SuppressWarnings("serial")
public class SimpleDragon extends SimpleUnit implements Runnable, Serializable {
	/*
	 * Reaction speed of the dragon This is the time needed for the dragon to
	 * take its next turn. Measured in half a seconds x GAME_SPEED.
	 */
	protected int timeBetweenTurns;
	public static final int MIN_TIME_BETWEEN_TURNS = 2;
	public static final int MAX_TIME_BETWEEN_TURNS = 7;
	// The minimum and maximum amount of hitpoints that a particular dragon
	// starts with
	public static final int MIN_HITPOINTS = 50;
	public static final int MAX_HITPOINTS = 100;
	// The minimum and maximum amount of hitpoints that a particular dragon has
	public static final int MIN_ATTACKPOINTS = 5;
	public static final int MAX_ATTACKPOINTS = 20;
	public boolean connected = true;

	Socket requestSocket;
	ObjectOutputStream out;
	ObjectInputStream in;
	int port;

	/**
	 * Spawn a new dragon, initialize the reaction speed
	 * 
	 * @throws IOException
	 *
	 */
	public SimpleDragon(int x, int y, int port) throws IOException {
		/*
		 * Spawn the dragon with a random number of hitpoints between 50..100
		 * and 5..20 attackpoints.
		 */
		super((int) (Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS),
				(int) (Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS));
		this.port = port;
		/* Create a random delay */
		timeBetweenTurns = (int) (Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS))
				+ MIN_TIME_BETWEEN_TURNS;

		// if (!spawn(x, y))
		// {
		// return; // We could not spawn on the battlefield
		// }
		/* Awaken the dragon */

		// sendMessageToBattleField("SPAWN" + x+ "," + y);

		runnerThread = new Thread(this);
		runnerThread.start();
	}

	public void sendMessageToBattleField(String msg) {
		try {
			out.writeObject(msg);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onMessageReceived(Message message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		try {
			requestSocket = new Socket("localhost", port);

			while (true) {
				in = new ObjectInputStream(requestSocket.getInputStream());
				out = new ObjectOutputStream(requestSocket.getOutputStream());
				out.writeObject("SPAWN");
				out.flush();

			}
			// sendMessageToBattleField("SPAWN" + 1+ "," + 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
