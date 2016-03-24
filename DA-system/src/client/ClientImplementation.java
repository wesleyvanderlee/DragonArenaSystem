package client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import core.Message;
import server.Configuration;
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
	
	public void sendMessage(String request) throws AccessException, RemoteException, NotBoundException 
	{
		Message message = new Message(request);
		server = (ServerInterface) registry.lookup(Configuration.SERVER_ID);
		server.receiveMessage(message);
	}
	
}
