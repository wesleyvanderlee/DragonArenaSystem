import RMI.Configuration;
import RMI.GameClient;

public class StartGameClient {
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 1; i++) {
			int j = 0;
			GameClient gc = new GameClient(0, Configuration.SERVER_IDS[j], Configuration.SERVER_HOSTS[j],
					Configuration.SERVER_REGISTRY_PORTS[j]);
			Thread t = new Thread(gc);
			t.start();
		}
	}
}
