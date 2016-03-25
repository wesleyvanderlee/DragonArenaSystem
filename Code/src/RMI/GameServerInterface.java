package RMI;
import java.rmi.Remote;

public interface GameServerInterface extends Remote{
	public void play() throws java.rmi.RemoteException;
	public void onMessageReceived(Message msg) throws java.rmi.RemoteException;
}
