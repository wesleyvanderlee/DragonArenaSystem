import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameClient {
	public static void main ( String args[] ) throws Exception
    {
		Registry reg = LocateRegistry.getRegistry(Configuration.REMOTE_HOST, Configuration.REMOTE_PORT);
		GameServerInterface rmiInterface= (GameServerInterface) reg.lookup(Configuration.REMOTE_ID);
		

		rmiInterface.play("Wesley");
		
		int a = 7, b= 13;
		int sum = rmiInterface.add(a, b);
		System.out.println("The sum of 7 and 13 equals: " + sum);
    }
}
