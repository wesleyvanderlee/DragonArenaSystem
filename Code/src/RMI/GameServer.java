package RMI;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
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

	
//	public static void main ( String args[] ) throws Exception
//    {
//		GameServer gameServerImplmenetation = new GameServer();
//		Registry registry = LocateRegistry.createRegistry(Configuration.REMOTE_PORT);
//		registry.bind(Configuration.REMOTE_ID, gameServerImplmenetation);
//    }
}
