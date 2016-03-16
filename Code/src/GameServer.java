import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class GameServer extends UnicastRemoteObject implements GameServerInterface{
	
	/* We have to declare a default constructor, even when we don't have any initialization 
	code for our service. This is because our default constructor can throw a 
	java.rmi.RemoteException, from its parent constructor in UnicastRemoteObject. */
	protected GameServer() throws RemoteException {
		super();
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void play(String name) throws RemoteException {
		System.out.println("Play called with: " + name);
		
	}
	
	@Override
	public int add(int a, int b) throws RemoteException {
		return a + b;		
	}
	
	public static void main ( String args[] ) throws Exception
    {
		GameServer gameServerImplmenetation = new GameServer();
		Registry registry = LocateRegistry.createRegistry(Configuration.REMOTE_PORT);
		registry.bind(Configuration.REMOTE_ID, gameServerImplmenetation);
    }
}
