import RMI.Configuration;
import RMI.GameClient;

public class StartGameClient {
	public static void main(String[] args) throws Exception {
		
		for(int k = 1; k<50; k++){
			int i = 0;
			GameClient gc = new GameClient(k, Configuration.SERVER_IDS[i], Configuration.SERVER_HOSTS[i],Configuration.SERVER_REGISTRY_PORTS[i]);
			Thread t  = new Thread(gc);
			t.start();
		}
	}
}
