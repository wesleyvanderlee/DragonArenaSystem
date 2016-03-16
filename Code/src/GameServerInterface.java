import java.rmi.Remote;

public interface GameServerInterface extends Remote{
	public void play(String name) throws java.rmi.RemoteException;
	
	public int add(int a, int b) throws java.rmi.RemoteException; 
	
}
