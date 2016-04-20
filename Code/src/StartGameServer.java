import RMI.Configuration;
import RMI.GameServer;

public class StartGameServer {
	public static void main(String[] args) throws Exception {
		int i = 1;
		GameServer gs = new GameServer(Configuration.SERVER_IDS[i], Configuration.SERVER_HOSTS[i],Configuration.SERVER_REGISTRY_PORTS[i]);
		Thread t  = new Thread(gs);
		t.start();
	}
}
