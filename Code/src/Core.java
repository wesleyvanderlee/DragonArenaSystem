import RMI.Configuration;
import RMI.GameClient;
import RMI.GameServer;

public class Core {

	public static void main(String[] args) throws Exception {

	    
		for(int i=0; i< 5; i++){
			GameServer gameServerImplmenetation = new GameServer(Configuration.REMOTE_IDS[i], Configuration.REMOTE_HOSTS[i],
				Configuration.REMOTE_PORTS[i], Configuration.CALLBACK_PORT);
		}
		
		for(int i= 0; i<20; i++){
			int ind = i %5;
			GameClient gc = new GameClient(Configuration.REMOTE_IDS[ind], Configuration.REMOTE_HOSTS[ind], Configuration.REMOTE_PORTS[ind]);
			gc.run();
		}
		
		

	}

}
