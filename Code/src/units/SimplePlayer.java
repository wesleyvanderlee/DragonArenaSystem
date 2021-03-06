package units;

import java.io.IOException;
import java.io.Serializable;

import RMI.Message;
import game.BattleField;
import game.GameState;
import units.SimpleUnit.UnitType;

/**
 * A Player is, as the name implies, a playing 
 * character. It can move in the four wind directions,
 * has a hitpoint range between 10 and 20 
 * and an attack range between 1 and 10.
 * 
 * Every player runs in its own thread, simulating
 * individual behaviour, not unlike a distributed
 * server setup.
 *   
 * @author Pieter Anemaet, Boaz Pat-El
 */
@SuppressWarnings("serial")
public class SimplePlayer extends SimpleUnit implements Runnable, Serializable
{
	/* Reaction speed of the player
	 * This is the time needed for the player to take its next turn.
	 * Measured in half a seconds x GAME_SPEED.
	 */
	protected int timeBetweenTurns;
	public static final int MIN_TIME_BETWEEN_TURNS = 2;
	public static final int MAX_TIME_BETWEEN_TURNS = 7;
	public static final int MIN_HITPOINTS = 20;
	public static final int MAX_HITPOINTS = 10;
	public static final int MIN_ATTACKPOINTS = 1;
	public static final int MAX_ATTACKPOINTS = 10;

	static final UnitType type = UnitType.player;
	/**
	 * Create a player, initialize both 
	 * the hit and the attackpoints. 
	 * @throws IOException 
	 */
	public SimplePlayer(int x, int y, String serverID, String SERVER_REGISTRY_HOST, int SERVER_REGISTRY_PORT) throws IOException 
	{
		/* Initialize the hitpoints and attackpoints */
		super(x,y,(int)(Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS), (int)(Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS), serverID, SERVER_REGISTRY_HOST, SERVER_REGISTRY_PORT);

		/* Create a random delay */
		timeBetweenTurns = (int)(Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;


		if (!spawn(x, y, type))
			return; // We could not spawn on the battlefield
	}
	


	/**
	 * Roleplay the player. Make the player act once in a while,
	 * only stopping when the player is actually dead or the 
	 * program has halted.
	 * 
	 * It checks a random direction, if an entity is located there.
	 * If there is a player, it will try to heal that player if the
	 * 50% health rule applies. If there is a dragon, it will attack
	 * and if there is nothing, it will move in that direction. 
	 */
	@SuppressWarnings("static-access")
	public void run() {
		Direction direction;
		UnitType adjacentUnitType = UnitType.undefined;
		int targetX = 0, targetY = 0;
		this.running = true;

		while(GameState.getRunningState() && this.running) {
			try {			
				/* Sleep while the player is considering its next move */
				Thread.currentThread().sleep((int)(timeBetweenTurns * 5000 * GameState.GAME_SPEED));

				/* Stop if the player runs out of hitpoints */
				if (getHitPoints() <= 0)
				{
					System.out.println("die");
					Thread.interrupted();
					break;
				}

				// Randomly choose one of the four wind directions to move to if there are no units present
				direction = Direction.values()[ (int)(Direction.values().length * Math.random()) ];
				adjacentUnitType = UnitType.undefined;
				switch (direction) {
					case up:
						if (this.getY() <= 0)
							// The player was at the edge of the map, so he can't move north and there are no units there
							continue;
						
						targetX = this.getX();
						targetY = this.getY() - 1;
						break;
					case down:
						if (this.getY() >= BattleField.MAP_HEIGHT - 1)
							// The player was at the edge of the map, so he can't move south and there are no units there
							continue;

						targetX = this.getX();
						targetY = this.getY() + 1;
						break;
					case left:
						if (this.getX() <= 0)
							// The player was at the edge of the map, so he can't move west and there are no units there
							continue;

						targetX = this.getX() - 1;
						targetY = this.getY();
						break;
					case right:
						if (this.getX() >= BattleField.MAP_WIDTH - 1)
							// The player was at the edge of the map, so he can't move east and there are no units there
							continue;

						targetX = this.getX() + 1;
						targetY = this.getY();
						break;
				}
				// Get what unit lies in the target square
				adjacentUnitType = this.getType(targetX, targetY);
				
				switch (adjacentUnitType) 
				{
					case undefined:
						// There is no unit in the square. Move the player to this square
						this.moveUnit(targetX, targetY,type);
						break;
					case player:
						// There is a player in the square, attempt a healing
						this.healDamage(targetX, targetY, getAttackPoints());
						break;
					case dragon:
						// There is a dragon in the square, attempt a dragon slaying
						this.dealDamage(targetX, targetY, getAttackPoints());
						break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void onMessageReceived(Message message) {
		// TODO Auto-generated method stub
		
	}
	public UnitType getType() {
		return type;
	}
}
