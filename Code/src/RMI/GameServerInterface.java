package RMI;
import java.rmi.Remote;

public interface GameServerInterface extends Remote{
	public void play() throws java.rmi.RemoteException;
	
}
