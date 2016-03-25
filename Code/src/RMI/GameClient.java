package RMI;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameClient {
	GameServerInterface gameServer;
	Registry register;
	int id =1234;

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
			sendMessage(MessageRequest.addClient);
			//this.gameServer.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(MessageRequest request) throws RemoteException {
		Message message = new Message();
		message.put("request", request);
		message.put("id", this.id);
		gameServer.onMessageReceived(message);
	}
}
