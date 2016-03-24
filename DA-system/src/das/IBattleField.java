package das;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.LocatorEx.Snapshot;

import core.Message;
import units.Unit.UnitType;

public interface IBattleField extends Remote 
{

	public void sendMessage(Message msg) throws RemoteException;

	public void receiveMessage(Message msg) throws RemoteException;

	public void setMyAddress(String myAddress) throws RemoteException;

	public HashMap<String, String> getHelpers() throws RemoteException;

	public void putHelper(String key, String value) throws RemoteException;

	public int[] getPosition(String id) throws RemoteException;

	public int getMapHeight() throws RemoteException;

	public int getMapWidth() throws RemoteException;

	public UnitType getType(int x, int y) throws RemoteException;

	public String getRandomHelper() throws RemoteException;

	public void ping() throws RemoteException;
	
	public void setBackupAddress(String address) throws RemoteException;
	
	public boolean updateBackup(Snapshot snapshot) throws RemoteException;
	
	public boolean promoteBackupToMain() throws RemoteException;
	
	public void saveMetrics() throws RemoteException;

}