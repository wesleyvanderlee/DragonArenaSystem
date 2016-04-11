package units;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import RMI.GameServerInterface;
import RMI.Message;
import RMI.MessageRequest;
import core.IMessageReceivedHandler;
import game.GameState;

/**
 * Base class for all players whom can participate in the DAS game. All
 * properties of the units (hitpoints, attackpoints) are initialized in this
 * class.
 * 
 * @author Pieter Anemaet, Boaz Pat-El
 */
public abstract class SimpleUnit implements Serializable, IMessageReceivedHandler {

	private static final long serialVersionUID = 1L;

	// Position of the unit
	protected int x, y;

	// Health
	private int maxHitPoints;
	protected int hitPoints;

	// Attack points
	protected int attackPoints;

	// Identifier of the unit
	private int unitID;

	// If this is set to false, the unit will return its run()-method and
	// disconnect from the server
	protected boolean running;

	/*
	 * The thread that is used to make the unit run in a separate thread. We
	 * need to remember this thread to make sure that Java exits cleanly. (See
	 * stopRunnerThread())
	 */

	// Map messages from their ids
	private Map<Integer, Message> messageList;
	// Is used for mapping an unique id to a message sent by this unit
	private int localMessageCounter = 0;

	public enum Direction {
		up, right, down, left
	};

	public enum UnitType {
		player, dragon, undefined,
	};

	protected Thread runnerThread;
	GameServerInterface gameServer;
	int SERVER_REGISTRY_PORT;
	Registry serverRegister;

	/**
	 * Create a new unit and specify the number of hitpoints. Units hitpoints
	 * are initialized to the maxHitPoints.
	 * 
	 * @param maxHealth
	 *            is the maximum health of this specific unit.
	 * @throws IOException
	 */
	public SimpleUnit(int x, int y, int maxHealth, int attackPoints, String serverID, String SERVER_REGISTRY_HOST,
			int SERVER_REGISTRY_PORT) throws IOException {
		this.x = x;
		this.y = y;
		// Initialize the max health and health
		hitPoints = maxHitPoints = maxHealth;

		// Initialize the attack points
		this.attackPoints = attackPoints;
		messageList = new HashMap<Integer, Message>();
		try {
			this.SERVER_REGISTRY_PORT = SERVER_REGISTRY_PORT;
			this.serverRegister = LocateRegistry.getRegistry(SERVER_REGISTRY_HOST, SERVER_REGISTRY_PORT);
			this.gameServer = (GameServerInterface) serverRegister.lookup(serverID);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public SimpleUnit() throws IOException {

	}

	/**
	 * Adjust the hitpoints to a certain level. Useful for healing or dying
	 * purposes.
	 * 
	 * @param modifier
	 *            is to be added to the hitpoint count.
	 */
	public synchronized void adjustHitPoints(int modifier) {

		if (hitPoints <= 0) {
			removeUnit(x, y);
			return;
		}
		hitPoints += modifier;
		if (hitPoints > maxHitPoints) {
			hitPoints = maxHitPoints;
		}
		if (hitPoints <= 0) {
			removeUnit(x, y);
		}
	}

	/**
	 * @return the maximum number of hitpoints.
	 */
	public int getMaxHitPoints() {
		return maxHitPoints;
	}

	/**
	 * @return the unique unit identifier.
	 */
	public int getUnitID() {
		return unitID;
	}

	/**
	 * Set the position of the unit.
	 * 
	 * @param x
	 *            is the new x coordinate
	 * @param y
	 *            is the new y coordinate
	 */
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the x position
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y position
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return the current number of hitpoints.
	 */
	public int getHitPoints() {
		return hitPoints;
	}

	/**
	 * @return the attack points
	 */
	public int getAttackPoints() {
		return attackPoints;
	}

	/**
	 * Tries to make the unit spawn at a certain location on the battlefield
	 * 
	 * @param x
	 *            x-coordinate of the spawn location
	 * @param y
	 *            y-coordinate of the spawn location
	 * @return true iff the unit could spawn at the location on the battlefield
	 */
	protected boolean spawn(int x, int y, UnitType type) {
		/*
		 * Create a new message, notifying the board the unit has actually
		 * spawned at the designated position.
		 */
		int id = localMessageCounter++;
		Message spawnMessage = new Message(), result;;
		spawnMessage.put("request", MessageRequest.spawnUnit);
		spawnMessage.put("x", x);
		spawnMessage.put("y", y);
		spawnMessage.put("type", type);
		spawnMessage.put("unit", this);
		spawnMessage.put("id", id);
		sendServerMessage(spawnMessage);

		// Wait for the reply
		while (!messageList.containsKey(id)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return false;
		}

		result = messageList.get(id);
		messageList.put(id, null);
		return true;
	}

	protected SimpleUnit getUnit(int x, int y) {
		Message getMessage = new Message(), result;
		int id = localMessageCounter++;
		getMessage.put("request", MessageRequest.getUnit);
		getMessage.put("x", x);
		getMessage.put("y", y);
		getMessage.put("id", id);
		// Send the getUnit message
		sendServerMessage(getMessage);

		// Wait for the reply
		while (!messageList.containsKey(id)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return null;
		}

		result = messageList.get(id);
		messageList.put(id, null);
		return (SimpleUnit) result.get("unit");
	}

	private void sendServerMessage(Message message) {
		Message reply = new Message();
		message.put("serverRequest", MessageRequest.toBattleField);
		try {
			reply = gameServer.onMessageReceived(message);

			if (reply != null) {
				messageList.put((Integer) message.get("id"), reply);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void moveUnit(int x, int y, UnitType type) {
		Message moveMessage = new Message(), result = null;
		int id = localMessageCounter++;
		moveMessage.put("request", MessageRequest.moveUnit);
		moveMessage.put("x", x);
		moveMessage.put("y", y);
		moveMessage.put("type", type);
		moveMessage.put("id", id);
		moveMessage.put("unit", this);
		// Send the getUnit message
		sendServerMessage(moveMessage);

		// Wait for the reply
		while (!messageList.containsKey(id)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return;
		}

		result = messageList.get(id);
		// Remove the result from the messageList
		setPosition(x, y);
		messageList.put(id, null);
	}

	public void healDamage(int x, int y, int healed) {
		/*
		 * Create a new message, notifying the board that a unit has been
		 * healed.
		 */
		int id;
		Message healMessage, result;
		synchronized (this) {
			id = localMessageCounter++;

			healMessage = new Message();
			healMessage.put("request", MessageRequest.healDamage);
			healMessage.put("x", x);
			healMessage.put("y", y);
			healMessage.put("healed", healed);
			healMessage.put("id", id);
		}
		// Send a spawn message
		sendServerMessage(healMessage);

		// Wait for the reply
		while (!messageList.containsKey(id)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return;
		}

		result = messageList.get(id);
		messageList.put(id, null);
	}

	protected void removeUnit(int x, int y) {
		Message removeMessage = new Message(), result;;
		int id = localMessageCounter++;
		removeMessage.put("request", MessageRequest.removeUnit);
		removeMessage.put("x", x);
		removeMessage.put("y", y);
		removeMessage.put("id", id);
		// Send the removeUnit message
		sendServerMessage(removeMessage);

		// Wait for the reply
		while (!messageList.containsKey(id)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return;
		}

		result = messageList.get(id);
		messageList.put(id, null);
	}

	/**
	 * Returns whether the indicated square contains a player, a dragon or
	 * nothing.
	 * 
	 * @param x:
	 *            x coordinate
	 * @param y:
	 *            y coordinate
	 * @return UnitType: the indicated square contains a player, a dragon or
	 *         nothing.
	 */
	protected UnitType getType(int x, int y) {
		Message getTypeMessage = new Message(), result = null;
		int id = localMessageCounter++;
		getTypeMessage.put("request", MessageRequest.getType);
		getTypeMessage.put("x", x);
		getTypeMessage.put("y", y);
		getTypeMessage.put("id", id);
		sendServerMessage(getTypeMessage);

		// Wait for the reply
		while (!messageList.containsKey(id)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return UnitType.undefined;
		}

		result = messageList.get(id);
		if (result == null) // Could happen if the game window had closed
			return UnitType.undefined;

		if (result.get("type") == null)
			return UnitType.undefined;

		messageList.put(id, null);
		return (UnitType) result.get("type");

	}

	public void dealDamage(int x, int y, int damage) {
		/*
		 * Create a new message, notifying the board that a unit has been dealt
		 * damage.
		 */
		int id;
		Message damageMessage, result;
		synchronized (this) {
			id = localMessageCounter++;

			damageMessage = new Message();
			damageMessage.put("request", MessageRequest.dealDamage);
			damageMessage.put("x", x);
			damageMessage.put("y", y);
			damageMessage.put("damage", damage);
			damageMessage.put("id", id);
		}
		sendServerMessage(damageMessage);

		// Wait for the reply
		while (!messageList.containsKey(id)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}

			// Quit if the game window has closed
			if (!GameState.getRunningState())
				return;
		}

		result = messageList.get(id);
		messageList.put(id, null);
	}

	// Disconnects the unit from the battlefield by exiting its run-state
	public void disconnect() {
		running = false;
	}

	/**
	 * Stop the running thread. This has to be called explicitly to make sure
	 * the program terminates cleanly.
	 */
	public void stopRunnerThread() {
		try {
			runnerThread.join();
		} catch (InterruptedException ex) {
			assert (false) : "Unit stopRunnerThread was interrupted";
		}

	}
}
