package RMI;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameClient {
	GameServerInterface gameServer;
	Registry register;

	public GameClient(String ID, String HOST, int PORT) {
		try {
			this.register = LocateRegistry.getRegistry(HOST, PORT);
			this.gameServer= (GameServerInterface) register.lookup(ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() 
	{
		try {
			sendMessage();
			//this.gameServer.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage() throws RemoteException {
		Message message = new Message();
		int id = 1234;
		message.put("request", MessageRequest.play);
		message.put("id", id);
		gameServer.onMessageReceived(message);
	}
}
