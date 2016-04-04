package RMI;

import java.io.IOException;
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
	Thread runnerThread;
	SimpleBattleFieldInterface battlefield;

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
			int[] values ;
			values = battlefield.getRandomLocation();
			player = new SimplePlayer(values[0], values[1], serverID, SERVER_REGISTRY_HOST, SERVER_REGISTRY_PORT);
			Thread t = new Thread(player);
			t.start();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			battlefield = (SimpleBattleFieldInterface) serverRegister.lookup(gameServer.getBattleField());
			System.out.println((String) battlefield.battleFieldString());
			initPlayer();
		} catch (Exception e) 
		{
			// e.printStackTrace();
		}
	}
}
