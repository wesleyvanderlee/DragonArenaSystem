package RMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameClient implements Runnable{
	GameServerInterface gameServer;
	int SERVER_REGISTRY_PORT;
	Registry serverRegister;
	int ID;
	Thread runnerThread;
	

	public GameClient(int identifier ,String ServerID, int SERVER_REGISTRY_PORT, String HOST, int PORT) {
		try {
			this.SERVER_REGISTRY_PORT = SERVER_REGISTRY_PORT;
			this.serverRegister = LocateRegistry.getRegistry(SERVER_REGISTRY_PORT);
			this.gameServer = (GameServerInterface) serverRegister.lookup(ServerID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.ID = identifier;
		this.runnerThread = new Thread(this);
		this.runnerThread.start();
	}

	public void run() 
	{
		
		try {
//			sendMessage(MessageRequest.addClient);
			String s = this.ID + "";
			System.out.println("Gameserver acknowledge: " + gameServer.addClient(s));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(MessageRequest request) throws Exception {
		Message message = new Message();
		message.put("request", request);
		message.put("ID", this.ID);
		gameServer.onMessageReceived(message);
	}
	
	public void onMessageReceived(Message msg) throws Exception {
		Message reply = null;
		String origin = msg.get("id") + "";
		MessageRequest request = (MessageRequest)msg.get("request");
		switch(request)
		{
		case play:
			break;
		case connect:
//			clientRegister.bind(this.ID+"", this);
			System.out.println("At client: connect recieved");
			break;
		default:
			System.out.println("No message type found");
			break;
		}
	}
	
	
}
