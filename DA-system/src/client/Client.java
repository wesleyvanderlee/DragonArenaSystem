package client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import server.Configuration;
import server.ServerInterface;

public class Client
{
	
	private static ServerInterface server;
	private static Registry registry;
	private static ClientImplementation clientImplementation;
	
	public static void main(String[] args) throws NotBoundException, UnknownHostException, IOException 
	{
		System.setProperty("java.security.policy", "security.policy");
	    System.setSecurityManager(new SecurityManager());
	    
	    registry = LocateRegistry.getRegistry();
	    
	    server = (ServerInterface) registry.lookup(Configuration.SERVER_ID);
	    
	    clientImplementation = new ClientImplementation();
	    
	    ClientInterface clientStub = (ClientInterface) UnicastRemoteObject.exportObject(clientImplementation, Configuration.CLIENT_CALLBACK);
	    
	    server.spawn();
	}
}
