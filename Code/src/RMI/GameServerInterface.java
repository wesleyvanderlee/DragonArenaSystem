package RMI;
import java.rmi.Remote;

import game.BattleField;

public interface GameServerInterface extends Remote{
	public void play() throws java.rmi.RemoteException;
	public void onMessageReceived(Message msg) throws java.rmi.RemoteException;
	public BattleField getBattlefield() throws java.rmi.RemoteException;
}
