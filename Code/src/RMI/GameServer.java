package RMI;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class GameServer extends UnicastRemoteObject implements GameServerInterface{
	String ID;
	String HOST;
	int PORT;
	int CALLBACK_PORT;
	
	public GameServer(String ID, String HOST, int PORT, int CALLBACK_PORT) throws RemoteException {
		super();
		this.ID = ID;
		this.HOST = HOST;
		this.PORT = PORT;
		this.CALLBACK_PORT = CALLBACK_PORT;
		this.register();
	}
	
	private void register(){
		try {
			Registry registry = LocateRegistry.createRegistry(this.PORT);
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
