package RMI;
import java.rmi.Remote;
import java.util.ArrayList;

import game.SimpleBattleField;
import game.SimpleBattleFieldInterface;

public interface GameServerInterface extends Remote{
	public boolean addClient(GameClient clientID) throws java.rmi.RemoteException;
	public String getID() throws java.rmi.RemoteException;
	public void initDragon(int x, int y) throws java.rmi.RemoteException;
	public void onMessageReceived(Message msg) throws Exception;
	public SimpleBattleFieldInterface getBattleField() throws Exception;
	public GameServerInterface getOldestGameServer() throws Exception;
	public void setOldestGameServer(GameServerInterface oldestGameServer) throws Exception;
	public int getRank()throws Exception;
	public boolean isReady()throws Exception;
	public void setReady(boolean ready) throws Exception;
	public int getSERVER_REGISTRY_PORT() throws Exception;
	public void setSERVER_REGISTRY_PORT(int sERVER_REGISTRY_PORT)throws Exception;
	public String getHOST() throws Exception;
	public void setHOST(String HOST) throws Exception;
	public ArrayList<GameClient> getGameClients()throws Exception;
	public void setGameClients(ArrayList<GameClient> gameClients)throws Exception;
}
