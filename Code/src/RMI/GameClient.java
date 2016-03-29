package RMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameClient implements Runnable{
	GameServerInterface gameServer;
	int SERVER_REGISTRY_PORT;
	Registry serverRegister;
	int ID;
	Thread runnerThread;
	

	public GameClient(int clientID ,String serverID, String SERVER_REGISTRY_HOST, int SERVER_REGISTRY_PORT) {
		try {
			this.SERVER_REGISTRY_PORT = SERVER_REGISTRY_PORT;
			this.serverRegister = LocateRegistry.getRegistry(SERVER_REGISTRY_HOST, SERVER_REGISTRY_PORT);
			this.gameServer = (GameServerInterface) serverRegister.lookup(serverID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.ID = clientID;
		this.runnerThread = new Thread(this);
		this.runnerThread.start();
	}

	public void run() 
	{
		try {
			String s =  this.ID + "";
//			System.out.println("Gameserver acknowledge: " + gameServer.addClient(s));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
