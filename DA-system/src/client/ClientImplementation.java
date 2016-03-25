package client;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import server.ServerInterface;

public class ClientImplementation implements ClientInterface
{
	private String clientID;
	
	private static ServerInterface server;
	private static Registry registry;
	private static ClientImplementation clientImplementation;
	
	protected ClientImplementation() throws RemoteException 
	{
		super();
		
	}	
}
