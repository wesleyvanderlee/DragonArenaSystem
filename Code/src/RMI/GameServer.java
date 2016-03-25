package RMI;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import game.BattleField;
import presentation.BattleFieldViewer;
import units.Dragon;
import units.Player;

public class GameServer extends UnicastRemoteObject implements GameServerInterface{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String ID;
	String HOST;
	int PORT;
	int CALLBACK_PORT;
	Registry clientRegistry; 
	Registry severRegistry; 
	BattleField battlefield; 
	ArrayList<GameServerInterface> gameServers;
	
	public GameServer(String ID, String HOST, int PORT, int CALLBACK_PORT) throws IOException {
		super();
		this.ID = ID;
		this.HOST = HOST;
		this.PORT = PORT;
		this.CALLBACK_PORT = CALLBACK_PORT;
		this.register();
		this.makeBattleField();
	}
	
	private void makeBattleField() throws IOException
	{
		if(ID.charAt(ID.length()-1) == '1')
		{
			battlefield = BattleField.getBattleField();
			makeDragons();
		}
		else
		{
			String firstGameSeverID =ID.substring(0, ID.length()-1) + 1;
			GameServerInterface firstGameSever;
			try 
			{
				firstGameSever = (GameServerInterface) severRegistry.lookup(firstGameSeverID);
				setBattlefield(firstGameSever.getBattlefield());
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	
	
	private void makeDragons()
	{
		/* All the dragons connect */
		for(int i = 0; i < Configuration.DRAGON_COUNT; i++) 
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
			if (battlefield.getUnit(x, y) != null)
			{
				break;
			}
			
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
						e.printStackTrace();
					}
				}
			}).start();
		}
	}
	
	private void register()
	{
		System.setProperty("java.security.policy", "security.policy");
	    System.setSecurityManager(new SecurityManager());
		try 
		{
			if(ID.charAt(ID.length()-1) == '1')
			{
				severRegistry = LocateRegistry.createRegistry(Configuration.SERVER_PORT);
				severRegistry.bind(this.ID, this);
			}
			else
			{
				severRegistry = LocateRegistry.getRegistry(Configuration.SERVER_PORT);
				severRegistry.bind(this.ID, this);
			}
			clientRegistry = LocateRegistry.createRegistry(this.PORT);
			clientRegistry.bind(this.ID, this);
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void play() throws RemoteException {
		System.out.println("Play Dragon Arena System");
		
	}
	
	public void addClient()
	{
		/* Initialize a client */
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
			System.out.println("No room on the battlefield");
		}

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
					e.printStackTrace();
				}
			}
		}).start();
	}
	public void setBattlefield(BattleField battlefield)
	{
		this.battlefield = battlefield;
	}
	
	public BattleField getBattlefield()
	{
		return this.battlefield;
	}
	
	public void onMessageReceived(Message msg) throws RemoteException {
		Message reply = null;
		String origin = (String)msg.get("origin");
		MessageRequest request = (MessageRequest)msg.get("request");
		
		switch(request)
		{
			case play:
				play();
				break;
			case addClient:
				addClient();
				break;
			case updateBattlefield:
				battlefield = (BattleField) msg.get("battlefiled");
				break;
			case getBattlefield:
				battlefield = (BattleField) msg.get("battlefiled");
				break;
		default:
			System.out.println("No message type found");
			break;
		}
	}
	
	private void sendSeverMessage(String ID, MessageRequest request) throws Exception {
		Message message = new Message();
		message.put("request", request);
		message.put("id", ID);
		GameServerInterface otherSever = (GameServerInterface) severRegistry.lookup(ID);
		otherSever.onMessageReceived(message);
	}
}
