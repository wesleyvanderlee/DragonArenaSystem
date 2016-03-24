package server;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import core.Message;

public interface ServerInterface extends Remote
{
	void startServer() throws RemoteException, IOException, InterruptedException;

	void spawn() throws RemoteException;
}