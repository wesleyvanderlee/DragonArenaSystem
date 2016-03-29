package RMI;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import game.BattleField;
import presentation.BattleFieldViewer;
import units.*;

public class GameServer extends UnicastRemoteObject implements GameServerInterface, Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String ID;
	String HOST;
	int PORT;
	int CALLBACK_PORT;
	int SERVER_REGISTRY_PORT;
	Registry severRegistry;
	BattleField battlefield;
	ArrayList<GameServerInterface> gameServers;
	Thread runnerThread;
	ArrayList<String> gameClients;

	public GameServer(String ID, String HOST, int PORT, int SERVER_REGISTRY_PORT, int CALLBACK_PORT)
			throws IOException {
		super();
		this.gameClients = new ArrayList<String>();
		this.ID = ID;
		this.HOST = HOST;
		this.PORT = PORT;
		this.CALLBACK_PORT = CALLBACK_PORT;
		this.SERVER_REGISTRY_PORT = SERVER_REGISTRY_PORT;
		this.register();
		// this.makeBattleField();

		this.runnerThread = new Thread(this);
		this.runnerThread.start();
	}

	private void makeBattleField() throws IOException {
		if (ID.charAt(ID.length() - 1) == '1') {
			battlefield = BattleField.getBattleField();
			makeDragons();
		} else {
			String firstGameSeverID = ID.substring(0, ID.length() - 1) + 1;
			GameServerInterface firstGameSever;
			try {
				firstGameSever = (GameServerInterface) severRegistry.lookup(firstGameSeverID);
				sendSeverMessage(firstGameSeverID, MessageRequest.getBattlefield);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void makeDragons() {
		/* All the dragons connect */
		for (int i = 0; i < Configuration.DRAGON_COUNT; i++) {
			/* Try picking a random spot */
			int x, y, attempt = 0;
			do {
				x = (int) (Math.random() * BattleField.MAP_WIDTH);
				y = (int) (Math.random() * BattleField.MAP_HEIGHT);
				attempt++;
			} while (battlefield.getUnit(x, y) != null && attempt < 10);

			// If we didn't find an empty spot, we won't add a new dragon
			if (battlefield.getUnit(x, y) != null) {
				break;
			}

			final int finalX = x;
			final int finalY = y;

			/*
			 * Create the new dragon in a separate thread, making sure it does
			 * not block the system.
			 */
			new Thread(new Runnable() {
				public void run() {
					try {
						new Dragon(finalX, finalY);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}

	private void register() {
		System.setProperty("java.security.policy", "security.policy");
		System.setSecurityManager(new SecurityManager());
		try {
			System.out.println("A");
			severRegistry = LocateRegistry.createRegistry(SERVER_REGISTRY_PORT);
		} catch (Exception e)
		{
			try {
				System.out.println("B");
				severRegistry = LocateRegistry.getRegistry(SERVER_REGISTRY_PORT);
			} catch (RemoteException e1) {
				System.out.println("C");
				e1.printStackTrace();
			}
		} finally{
			try {
				System.out.println("D");
				severRegistry.bind(this.ID, this);
			} catch (Exception e) {
				System.out.println("E");
				e.printStackTrace();
			} 
		}

	}

	@Override
	public void play() throws RemoteException {
		System.out.println("Play Dragon Arena System");

	}

	public boolean addClient(String clientID) {
		this.gameClients.add(clientID);
		return true;
	}

	public void addClient(int origin) {

		// System.out.println("Server " + this.ID + " talked with " + origin);
		String clientId = origin + "";
		this.gameClients.add(clientId);

		/* Initialize a client */
		// int x, y, attempt = 0;
		// do
		// {
		// x = (int)(Math.random() * BattleField.MAP_WIDTH);
		// y = (int)(Math.random() * BattleField.MAP_HEIGHT);
		// attempt++;
		// }
		// while (battlefield.getUnit(x, y) != null && attempt < 10);
		//
		// // If we didn't find an empty spot, we won't add a new player
		// if (battlefield.getUnit(x, y) != null)
		// {
		// System.out.println("No room on the battlefield");
		// }
		//
		// final int finalX = x;
		// final int finalY = y;
		//
		// /* Create the new player in a separate
		// * thread, making sure it does not
		// * block the system.
		// */
		// new Thread(new Runnable()
		// {
		// public void run()
		// {
		// try
		// {
		// new Player(finalX, finalY);
		// }
		// catch (IOException e)
		// {
		// e.printStackTrace();
		// }
		// }
		// }).start();
	}

	public void setBattlefield(BattleField battlefield) {
		this.battlefield = battlefield;
	}

	public BattleField getBattlefield() {
		return this.battlefield;
	}

	public void onMessageReceived(Message msg) throws Exception {
		Message reply = null;
		int origin = Integer.parseInt(msg.get("ID") + "");
		MessageRequest request = (MessageRequest) msg.get("request");
		switch (request) {
		case play:
			play();
			break;
		case addClient:
			addClient(origin);
			break;
		case updateBattlefield:
			updatebfmap(msg.get("battlefieldMap"));
			break;
		case getBattlefield:
			System.out.println(ID);
			sendBattlefield(origin, MessageRequest.updateBattlefield);
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
		message.put("id", this.ID);
		GameServerInterface otherSever = (GameServerInterface) severRegistry.lookup(ID);
		otherSever.onMessageReceived(message);
	}

	private void sendBattlefield(int ID, MessageRequest request) throws Exception {
		Message message = new Message();
		message.put("request", request);
		message.put("id", this.ID);
		message.put("battlefieldMap", this.battlefield.getMap());
		GameServerInterface otherSever = (GameServerInterface) severRegistry.lookup(ID + "");
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
