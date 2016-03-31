package RMI;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import game.BattleField;
import game.SimpleBattleField;
import units.Coordinate;
import units.Dragon;
import units.Unit;

public class GameServer extends UnicastRemoteObject implements GameServerInterface, Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String ID;
	String HOST;
	int SERVER_REGISTRY_PORT;
	Registry severRegistry;
	SimpleBattleField battlefield;
	ArrayList<GameServerInterface> gameServers;
	Thread runnerThread;
	ArrayList<String> gameClients;
	int initTime;
	GameServerInterface oldestGameServer;
	private ReentrantLock lock = new ReentrantLock();

	public GameServer(String serverID, String SERVER_HOST, int SERVER_REGISTRY_PORT) throws IOException {
		super();
		this.gameClients = new ArrayList<String>();
		this.ID = serverID;
		this.HOST = SERVER_HOST;
		this.SERVER_REGISTRY_PORT = SERVER_REGISTRY_PORT;
		this.register();
		this.battlefield = new SimpleBattleField();
		oldestGameServer = this;
		lock.lock();
		try {
			sync();

		} finally {
			lock.unlock();
		}

		this.runnerThread = new Thread(this);
		this.runnerThread.start();

	}

	public String getID() {
		return this.ID;
	}

	public synchronized void sync() {
		if (!this.ID.equals(Configuration.SERVER_IDS[0]))
			return;
		int covered = 0;
		while (covered < Configuration.SERVER_IDS.length) {
			for (int i = 0; i < Configuration.SERVER_IDS.length; i++) {
				if (covered >= Configuration.SERVER_IDS.length)
					break;
				try {
					Registry otherRegistry = LocateRegistry.getRegistry(Configuration.SERVER_HOSTS[i],
							Configuration.SERVER_REGISTRY_PORTS[i]);
					GameServerInterface otherServer = (GameServerInterface) otherRegistry
							.lookup(Configuration.SERVER_IDS[i]);
					otherServer.initMe(this);
					covered++;
				} catch (Exception e) {
				}
			}
		}
	}

	public void initMe(GameServerInterface gs) throws RemoteException {
		this.oldestGameServer = gs;
		this.makeDragons();
	}

	private void makeDragons() {
		try {
			if (oldestGameServer.getID().equals(this.getID())) {
//				System.out.println("Dragons created by " + this.getID());
				/* All the dragons connect */
				ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
				for (int i = 0; i < Configuration.DRAGON_COUNT; i++) {
					/* Try picking a random spot */
					int x, y, attempt = 0;
					do {
						x = (int) (Math.random() * BattleField.MAP_WIDTH);
						y = (int) (Math.random() * BattleField.MAP_HEIGHT);
						attempt++;
					} while (battlefield.getUnit(x, y) != null && attempt < 10);

					// If we didn't find an empty spot, we won't add a new
					// dragon
					if (battlefield.getUnit(x, y) != null) {
						break;
					}

					final int finalX = x;
					final int finalY = y;
					Message message = new Message();
					message.put("request", MessageRequest.addDragon);
					message.put("ID", this.ID);
					message.put("x", finalX);
					message.put("y", finalY);
					serverBroadCast(message);
					/*
					 * Create the new dragon in a separate thread, making sure
					 * it does not block the system.
					 */

				}
				// Communicate dragon coordinates with all other servers

				
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}
	
	public void serverBroadCast(Message message){
		for (int i = 0; i < Configuration.SERVER_IDS.length; i++) {
			try {
				Registry otherRegistry = LocateRegistry.getRegistry(Configuration.SERVER_HOSTS[i],
						Configuration.SERVER_REGISTRY_PORTS[i]);
				GameServerInterface otherServer = (GameServerInterface) otherRegistry
						.lookup(Configuration.SERVER_IDS[i]);
				otherServer.onMessageReceived(message);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void initDragon(int x, int y) {
//		System.out.println(this.getID());
//		for (Coordinate coord : coords) {
		String id = this.getID();
		Thread t = new Thread(new Runnable() {
				public void run() {
					try {
//						System.out.println(id + " added a dragon at: (" + x + "," + y + ")");
						new Dragon(x, y);
//						System.out.println("--"+id);
						
					} catch (IOException e) {
						System.err.println("wtf");
						e.printStackTrace();
					}
				}
			});
		t.start();

//		}
		// new Thread(new Runnable()
		// {
		// public void run()
		// {
		// new BattleFieldViewer();
		// }
		// }).start();
	}

	private void register() {
		try {
			severRegistry = LocateRegistry.createRegistry(SERVER_REGISTRY_PORT);
		} catch (Exception e) {
			try {
				severRegistry = LocateRegistry.getRegistry(SERVER_REGISTRY_PORT);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				severRegistry.bind(this.ID, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public boolean addClient(String clientID) {
		this.gameClients.add(clientID);
		return true;
	}

	public void onMessageReceived(Message msg) throws Exception {
		MessageRequest request = (MessageRequest) msg.get("request");
		switch (request) {
		case updateBattlefield:
			updatebfmap(msg.get("battlefieldMap"));
			break;
		case addDragon:
			initDragon((Integer)msg.get("x"),(Integer)msg.get("y"));
			break;
		case getBattlefield:
//			sendBattlefield(origin, MessageRequest.updateBattlefield);
			break;
		default:
			System.out.println("No message type found");
			break;
		}
	}

	private void updatebfmap(Object map) {
		Unit[][] m;
		try {
			m = (Unit[][]) map;
			System.out.println(battlefield == null);
			// battlefield.setMap(m);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendSeverMessage(String ID, MessageRequest request) throws Exception {
		Message message = new Message();
		message.put("request", request);
		message.put("ID", this.ID);
		GameServerInterface otherSever = (GameServerInterface) severRegistry.lookup(ID);
		otherSever.onMessageReceived(message);
	}



	@Override
	public void run() {

	}

	@Override
	public String toString() {
		return this.ID;
	}

}
