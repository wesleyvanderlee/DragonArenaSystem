package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import client.ClientInterface;
import das.BattleField;
import das.GameState;
import presentation.BattleFieldViewer;
import units.Dragon;
import units.Player;

public class ServerImplementation implements ServerInterface
{
	public static final int MIN_PLAYER_COUNT = 2;
	public static final int MAX_PLAYER_COUNT = 2;
	public static final int DRAGON_COUNT = 5;
	public static final int TIME_BETWEEN_PLAYER_LOGIN = 5000; // In milliseconds
	
	public static BattleField battlefield; 
	public static int playerCount;
	public static int timeOut = 500;
	
	List<ClientInterface> clients;
	
	protected ServerImplementation() throws IOException 
	{
		super();
		clients = new ArrayList<ClientInterface>();
		battlefield = BattleField.getBattleField();
	}

	public void spawn()
	{
		/* Once again, pick a random spot */
		int x, y, attempt = 0;
		do 
		{
			x = (int)(Math.random() * BattleField.MAP_WIDTH);
			y = (int)(Math.random() * BattleField.MAP_HEIGHT);
			attempt++;
		} 
		while (battlefield.getUnit(x, y) != null && attempt < 10);

		// If we didn't find an empty spot, we won't add a new player
		if (battlefield.getUnit(x, y) != null) 
		{
			System.out.println("No room on battle field");
		};

		final int finalX = x;
		final int finalY = y;

		/* Create the new player in a separate
		 * thread, making sure it does not 
		 * block the system.
		 */
		new Thread(new Runnable()
		{
			public void run() 
			{
				try
				{
					new Player(finalX, finalY);
				} 
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start(); 
		
		/* Spawn a new battlefield viewer */
		new Thread(new Runnable() 
		{
			public void run() 
			{
				new BattleFieldViewer();
			}
		}).start();
	}
	
	public void startServer() throws IOException, InterruptedException
	{
		battlefield = BattleField.getBattleField();

		/* All the dragons connect */
		for(int i = 0; i < DRAGON_COUNT; i++) 
		{
			/* Try picking a random spot */
			int x, y, attempt = 0;
			do 
			{
				x = (int)(Math.random() * BattleField.MAP_WIDTH);
				y = (int)(Math.random() * BattleField.MAP_HEIGHT);
				attempt++;
			}
			while (battlefield.getUnit(x, y) != null && attempt < 10);

			// If we didn't find an empty spot, we won't add a new dragon
			if (battlefield.getUnit(x, y) != null) break;
			
			final int finalX = x;
			final int finalY = y;

			/* Create the new dragon in a separate
			 * thread, making sure it does not 
			 * block the system.
			 */
			new Thread(new Runnable() 
			{
				public void run()
				{
					try 
					{
						new Dragon(finalX, finalY);
					}
					catch (IOException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();

		}
		
		/* Add a random player every (5 seconds x GAME_SPEED) so long as the
		 * maximum number of players to enter the battlefield has not been exceeded. 
		 */
		while(GameState.getRunningState()) 
		{
			Thread.sleep((int)(5000 * GameState.GAME_SPEED));
		}
		
		/* Make sure both the battlefield and
		 * the socket monitor close down.
		 */
		BattleField.getBattleField().shutdown();
		System.exit(0); // Stop all running processes
	}
}
