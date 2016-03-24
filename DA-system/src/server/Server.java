package server;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server
{

	public static void main(String[] args) throws AlreadyBoundException, IOException, InterruptedException 
	{
		System.setProperty("java.security.policy", "security.policy");
	    System.setSecurityManager(new SecurityManager());

	    ServerImplementation serverImplementation = new ServerImplementation();

	    ServerInterface serverStub = (ServerInterface) UnicastRemoteObject.exportObject(serverImplementation, Configuration.SERVER_CALLBACK);
	
	    Registry reg = LocateRegistry.createRegistry(Configuration.REGISTRY_PORT);
	    reg.rebind(Configuration.SERVER_ID, serverStub);
	    System.out.println("Server is ready");
	    serverStub.startServer();
	}
}