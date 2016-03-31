package units;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import RMI.MessageRequest;
import core.IMessageReceivedHandler;
import core.LocalSocket;
import core.Message;
import core.Socket;
import core.SynchronizedSocket;
import core.exception.AlreadyAssignedIDException;
import core.exception.IDNotAssignedException;
import game.BattleField;
import game.GameState;

/**
 * Base class for all players whom can 
 * participate in the DAS game. All properties
 * of the units (hitpoints, attackpoints) are
 * initialized in this class.
 *  
 * @author Pieter Anemaet, Boaz Pat-El
 */
public abstract class SimpleUnit implements Serializable, IMessageReceivedHandler {


	/**
	 * 
	 */
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
	
	// If this is set to false, the unit will return its run()-method and disconnect from the server
	protected boolean running;

	/* The thread that is used to make the unit run in a separate thread.
	 * We need to remember this thread to make sure that Java exits cleanly.
	 * (See stopRunnerThread())
	 */
	protected Thread runnerThread;

	public enum Direction {
		up, right, down, left
	};
	
	public enum UnitType {
		player, dragon, undefined,
	};

	/**
	 * Create a new unit and specify the 
	 * number of hitpoints. Units hitpoints
	 * are initialized to the maxHitPoints. 
	 * 
	 * @param maxHealth is the maximum health of 
	 * this specific unit.
	 * @throws IOException 
	 */
	public SimpleUnit(int maxHealth, int attackPoints) throws IOException {

		// Initialize the max health and health
		hitPoints = maxHitPoints = maxHealth;

		// Initialize the attack points
		this.attackPoints = attackPoints;
	}

	/**
	 * Adjust the hitpoints to a certain level. 
	 * Useful for healing or dying purposes.
	 * 
	 * @param modifier is to be added to the
	 * hitpoint count.
	 */
	public synchronized void adjustHitPoints(int modifier) {
		if (hitPoints <= 0)
			return;

		hitPoints += modifier;

		if (hitPoints > maxHitPoints)
			hitPoints = maxHitPoints;

//		if (hitPoints <= 0)
//			removeUnit(x, y);
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
	 * @param x is the new x coordinate
	 * @param y is the new y coordinate
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
	
	
	// Disconnects the unit from the battlefield by exiting its run-state
	public void disconnect() {
		running = false;
	}

	/**
	 * Stop the running thread. This has to be called explicitly to make sure the program 
	 * terminates cleanly.
	 */
	public void stopRunnerThread() {
		try {
			runnerThread.join();
		} catch (InterruptedException ex) {
			assert(false) : "Unit stopRunnerThread was interrupted";
		}
		
	}
}
