import RMI.Configuration;
import RMI.GameClient;

public class StartGameClient {
	public static void main(String[] args) throws Exception {
		
		for(int k = 0; k<1; k++){
			int i = 0;
			GameClient gc = new GameClient(i, Configuration.SERVER_IDS[i], Configuration.SERVER_HOSTS[i],Configuration.SERVER_REGISTRY_PORTS[i]);
			Thread t  = new Thread(gc);
			t.start();
		}
	}
}
