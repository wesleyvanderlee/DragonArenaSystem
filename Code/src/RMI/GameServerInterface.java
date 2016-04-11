package RMI;
import java.rmi.Remote;

import game.SimpleBattleField;

public interface GameServerInterface extends Remote{
	public boolean addClient(String clientID) throws java.rmi.RemoteException;
	public String getID() throws java.rmi.RemoteException;
	public void initDragon(int x, int y) throws java.rmi.RemoteException;
	public Message onMessageReceived(Message msg) throws Exception;
	public String getBattleField() throws Exception;
	public GameServerInterface getOldestGameServer() throws Exception;
	public void setOldestGameServer(GameServerInterface oldestGameServer) throws Exception;
	public int getRank()throws Exception;
	public boolean isReady()throws Exception;
	public void setReady(boolean ready) throws Exception;
	public int getSERVER_REGISTRY_PORT() throws Exception;
	public void setSERVER_REGISTRY_PORT(int sERVER_REGISTRY_PORT)throws Exception;
	public String getHOST() throws Exception;
	public void setHOST(String HOST) throws Exception;
}
