package RMI;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import game.BattleField;
import game.SimpleBattleFieldInterface;
import presentation.BattleFieldViewer;
import units.SimpleDragon;
import units.SimplePlayer;

public class GameClient implements Runnable {
	GameServerInterface gameServer;
	int SERVER_REGISTRY_PORT;
	String SERVER_REGISTRY_HOST;
	Registry serverRegister;
	String serverID;
	int ID;
	transient Thread runnerThread;
	SimpleBattleFieldInterface battlefield;
	boolean first = true;
	public GameClient(int clientID, String serverID, String SERVER_REGISTRY_HOST, int SERVER_REGISTRY_PORT) {

		try {
			this.serverID = serverID;
			this.SERVER_REGISTRY_HOST = SERVER_REGISTRY_HOST;
			this.SERVER_REGISTRY_PORT = SERVER_REGISTRY_PORT;
			this.serverRegister = LocateRegistry.getRegistry(SERVER_REGISTRY_HOST, SERVER_REGISTRY_PORT);
			this.gameServer = (GameServerInterface) serverRegister.lookup(serverID);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		this.ID = clientID;
		this.runnerThread = new Thread(this);
		this.runnerThread.start();
	}

	private void sendServerMessage(Message message) {
		message.put("serverRequest", MessageRequest.toBattleField);
		try {
			gameServer.onMessageReceived(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initPlayer() {
		SimplePlayer player;
		try {
			int[] values = new int[2];
			values = (int[]) battlefield.getRandomLocation();
			System.out.println("AAAAAAAAAA");
			player = new SimplePlayer(values[0], values[1], serverID, SERVER_REGISTRY_HOST, SERVER_REGISTRY_PORT);
			Thread t = new Thread(player);
			t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			battlefield = (SimpleBattleFieldInterface) serverRegister.lookup(gameServer.getBattleField());
		} catch (AccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NotBoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(first)
		{
			initPlayer();
			new Thread(new Runnable() 
			{
				public void run() 
				{
					new BattleFieldViewer(serverID, SERVER_REGISTRY_HOST, SERVER_REGISTRY_PORT);
				}
			}).start();
			first= false;
		}
		
		while (true) {
			try {
				
				//System.out.println((String) battlefield.battleFieldString());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
