import java.io.IOException;

import Log.Logger;
import RMI.Configuration;
import RMI.GameClient;
import RMI.GameServer;

public class Core {

	public static void main(String[] args) throws Exception {

		Logger.init();

		for (int i = 0; i < 5; i++) {
			final int index = i;
			new Thread(new Runnable() {
				public void run() {
					try {
						new GameServer(Configuration.SERVER_IDS[index], Configuration.SERVER_HOSTS[index],
								Configuration.SERVER_REGISTRY_PORTS[index]);
					} catch (IOException e) {
						e.printStackTrace();

					}
				}
			}).start();
			Logger.log("New server at " + Configuration.SERVER_HOSTS[i] + ":" + Configuration.SERVER_REGISTRY_PORTS[i]);
		}
		for (int i = 0; i < 20; i++) {
			final int clientID = i;
			final int index = i % 5;
			new Thread(new Runnable() {
				public void run() {
					try {
						new GameClient(clientID, Configuration.SERVER_IDS[index], Configuration.SERVER_HOSTS[index],
								Configuration.SERVER_REGISTRY_PORTS[index]);
					} catch (Exception e) {
						e.printStackTrace();

					}
				}
			}).start();
		}
		Logger.log(Logger.Level.SEVR, "Finish main");

	}

}
