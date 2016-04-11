import java.io.IOException;

import RMI.Configuration;
import RMI.GameClient;
import RMI.GameServer;

public class StartGameClient {
	public static void main(String[] args) throws Exception {
		int i = 1;
		GameClient gc = new GameClient(4, Configuration.SERVER_IDS[i], Configuration.SERVER_HOSTS[i],Configuration.SERVER_REGISTRY_PORTS[i]);
		Thread t  = new Thread(gc);
		t.start();
	}
}
