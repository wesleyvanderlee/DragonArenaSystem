package RMI;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import game.BattleField;
import presentation.BattleFieldViewer;
import units.Dragon;


public class GameServer extends UnicastRemoteObject implements GameServerInterface{
	String ID;
	String HOST;
	int PORT;
	int CALLBACK_PORT;
	Registry registry; 
	BattleField battlefield; 
	
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
		
		/* Spawn a new battlefield viewer */
		new Thread(new Runnable() 
		{
			public void run() 
			{
				new BattleFieldViewer();
			}
		}).start();
	}
	
	private void register(){
		try {
			registry = LocateRegistry.createRegistry(this.PORT);
			registry.bind(this.ID, this);
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void play() throws RemoteException {
		System.out.println("Play Dragon Arena System");
		
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
			case test2:
				System.out.println("test2");
				break;
		}
	}

	
//	public static void main ( String args[] ) throws Exception
//    {
//		GameServer gameServerImplmenetation = new GameServer();
//		Registry registry = LocateRegistry.createRegistry(Configuration.REMOTE_PORT);
//		registry.bind(Configuration.REMOTE_ID, gameServerImplmenetation);
//    }
}
