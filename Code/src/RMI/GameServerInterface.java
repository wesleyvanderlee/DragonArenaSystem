package RMI;
import java.rmi.Remote;

public interface GameServerInterface extends Remote{
	public boolean addClient(String clientID) throws java.rmi.RemoteException;
	public String getID() throws java.rmi.RemoteException;
//	public void initDragon(ArrayList<Coordinate> coords) throws java.rmi.RemoteException;
	public void initDragon(int x, int y) throws java.rmi.RemoteException;
	public void initMe(GameServerInterface gs) throws Exception;
	public Message onMessageReceived(Message msg) throws Exception;
//	public BattleField getBattlefield() throws java.rmi.RemoteException;
}
