package RMI;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import game.SimpleBattleField;
import game.SimpleBattleFieldInterface;
import units.SimpleDragon;
import units.SimpleUnit;
import units.SimpleUnit.UnitType;

public class GameServer extends UnicastRemoteObject implements GameServerInterface, Runnable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String ID;
	String HOST;
	int SERVER_REGISTRY_PORT;
	Registry severRegistry;
	SimpleBattleField battlefield;
	SimpleBattleFieldInterface battlefieldinterface;
	Thread battleFieldThread;
	ArrayList<GameServerInterface> gameServers;
	ArrayList<GameClient> gameClients;
	Map<Integer, Map<Integer, Message>> unitMessages;
	int initTime;
	GameServerInterface oldestGameServer;
	boolean first = true;
	boolean ready = false;
	LinkedList<Message> messageQueue;
	LinkedList<Message> unitMessageQueue;
	MessageProcessor mp;

	public GameServer(String serverID, String SERVER_HOST, int SERVER_REGISTRY_PORT) throws IOException {
		super();
		this.gameClients = new ArrayList<GameClient>();
		this.gameServers = new ArrayList<GameServerInterface>();
		this.unitMessages = new HashMap<Integer, Map<Integer, Message>>();
		this.messageQueue = new LinkedList<Message>();
		this.ID = serverID;
		this.HOST = SERVER_HOST;
		this.SERVER_REGISTRY_PORT = SERVER_REGISTRY_PORT;
		this.register();
		this.battlefield = new SimpleBattleField(serverID, SERVER_HOST, SERVER_REGISTRY_PORT);
		this.oldestGameServer = this;
		this.sync();
		this.mp = new MessageProcessor(this);
		(new Thread(this.mp)).start();

	}

	public Message nextMessgae() {
		try {
			return this.messageQueue.removeFirst();
		} catch (Exception e) {
			return null;
		}
	}

	// public void balanceClients() {
	// System.out.println("AAAAAA");
	// int totalClients = 0, balanceNum = 0;
	// int[] numClientsNeeded = new int[gameServers.size()];
	// try {
	// for (int i = 0; i < gameServers.size(); i++) {
	// totalClients = +gameServers.get(i).getGameClients().size();
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// System.out.println("BBBBB");
	// balanceNum = totalClients / gameServers.size();
	//
	// for (int i = 0; i < gameServers.size(); i++) {
	// try {
	// numClientsNeeded[i] = balanceNum -
	// gameServers.get(i).getGameClients().size();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// System.out.println("CCCC");
	// int count = 0;
	// if (this.gameClients.size() > balanceNum) {
	// if (numClientsNeeded[count] > 0) {
	//
	// try {
	// Message message = new Message();
	// message.put("serverRequest", MessageRequest.newClient);
	// message.put("ID", this.ID);
	// message.put("client", this.gameClients.get(count));
	// System.out.println("Client "+ this.gameClients.get(count).ID + " to " +
	// this.gameServers.get(count).getID());
	// this.gameClients.get(count).updateServerInfo(this.gameServers.get(count).getID(),this.gameServers.get(count).getHOST(),this.gameServers.get(count).getSERVER_REGISTRY_PORT());
	// sendSeverMessage(this.gameServers.get(count).getID(), message);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }
	// }
	// }

	public int getRank() {
		return Integer.parseInt(ID.substring(ID.length() - 1, ID.length()));
	}

	public String getID() {
		return this.ID;
	}

	public void sync() {
		for (int i = 0; i < Configuration.SERVER_IDS.length; i++) {
			try {
				Registry otherRegistry = LocateRegistry.getRegistry(Configuration.SERVER_HOSTS[i],
						Configuration.SERVER_REGISTRY_PORTS[i]);
				GameServerInterface otherServer = (GameServerInterface) otherRegistry
						.lookup(Configuration.SERVER_IDS[i]);
				this.setOldestGameServer(otherServer.getOldestGameServer());
				if (!otherServer.getID().equals(this.ID)) {
					gameServers.add(otherServer);
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
	}

	private void initBattlefield() {
		if (gameServers.size() > 0) {
			for (int i = 0; i < gameServers.size(); i++) {
				try {
					Registry otherRegistry = LocateRegistry.getRegistry(gameServers.get(i).getHOST(),
							gameServers.get(i).getSERVER_REGISTRY_PORT());
					SimpleBattleFieldInterface bf = gameServers.get(i).getBattleField();
					updateBattlefield(bf);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void updateBattlefield(SimpleBattleFieldInterface bf) {
		try {
			this.battlefield.setMap(bf.getMap());
			this.battlefield.setUnits(bf.getUnits());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void makeDragons() {
		try {
			if (oldestGameServer.getID().equals(this.getID())) {
				/* All the dragons connect */
				for (int i = 0; i < Configuration.DRAGON_COUNT; i++) {
					/* Try picking a random spot */
					int x, y, attempt = 0;
					do {
						x = (int) (Math.random() * SimpleBattleField.MAP_WIDTH);
						y = (int) (Math.random() * SimpleBattleField.MAP_HEIGHT);
						attempt++;
					} while (battlefield.getUnit(x, y) != null && attempt < 10);

					// If we didn't find an empty spot, we won't add a new
					// dragon
					if (battlefield.getUnit(x, y) != null) {
						break;
					}

					initDragon(x, y);
					Message message = new Message();
					message.put("serverRequest", MessageRequest.updatebattlefield);
					message.put("ID", this.ID);
					message.put("type", UnitType.dragon);
					message.put("battlefield", this.battlefield);

					serverBroadCast(message);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a message to all other GameServers in a meshstyle
	 * 
	 * @param message
	 */
	public void serverBroadCast(Message message) {
		for (int i = 0; i < gameServers.size(); i++) {
			try {
				Registry otherRegistry = LocateRegistry.getRegistry(gameServers.get(i).getHOST(),
						gameServers.get(i).getSERVER_REGISTRY_PORT());
				GameServerInterface otherServer = (GameServerInterface) otherRegistry
						.lookup(gameServers.get(i).getID());
				otherServer.onMessageReceived(message);
			} catch (Exception e) {

			}
		}
	}

	public void initDragon(int x, int y) {
		String id = this.getID();
		SimpleDragon dragon;
		try {
			dragon = new SimpleDragon(x, y, ID, HOST, SERVER_REGISTRY_PORT);
			Thread t = new Thread(dragon);
			t.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets or retrieves this GameServer's serverRegistry which is a registry
	 * containing the locations of all other Clients. This method is called at
	 * initialization of the GameServer
	 */
	private void register() {
		try {
			// First try to create a new registry, might fail if the port
			// already contains a registry
			severRegistry = LocateRegistry.createRegistry(SERVER_REGISTRY_PORT);
		} catch (Exception e) {
			try {
				// If the PORT is already taken, then a server registry is set,
				// thus that registry should be retrieved.
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

	public boolean addClient(GameClient clientID) {
		this.gameClients.add(clientID);
		return true;
	}

	public void onMessageReceived(Message msg) {
		try
		{
			this.messageQueue.add(msg);
		}
		catch(NullPointerException e)
		{
			this.messageQueue = new LinkedList<Message>();
			this.messageQueue.add(msg);
		}
		if((int)msg.get("unitID")>20)
			System.out.println(messageQueue);
	}

	@Override
	public String toString() {
		return this.ID;
	}

	@Override
	public SimpleBattleFieldInterface getBattleField() {
		return (SimpleBattleFieldInterface) this.battlefield;
	}

	public GameServerInterface getOldestGameServer() {
		return oldestGameServer;
	}

	public void setOldestGameServer(GameServerInterface oldestGameServer) {
		this.oldestGameServer = oldestGameServer;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	@Override
	public void run() {
		this.makeDragons();
		this.initBattlefield();
		ready = true;
		Message message = new Message();
		message.put("serverRequest", MessageRequest.addServer);
		message.put("gameServer", this);
		serverBroadCast(message);

		System.out.println(ID + " is running");
		try {
			System.out.println(ID + " oldest: " + this.oldestGameServer.getID());

			severRegistry.rebind("BattleField", this.battlefield);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// stay-alive
	}

	public void processMessage(Message message) {
		MessageRequest serverRequest = (MessageRequest) message.get("serverRequest");
		switch (serverRequest) {
		case addDragon:
			initDragon((Integer) message.get("x"), (Integer) message.get("y"));
			break;
		case addClient:
			gameClients.add((GameClient) message.get("gameClient"));
			// balanceClients();
			break;
		case addServer:
			gameServers.add((GameServerInterface) message.get("gameServer"));
			// balanceClients();
			break;
		case updatebattlefield:
			this.battlefieldinterface = ((SimpleBattleFieldInterface) message.get("battlefield"));
			this.updateBattlefield(battlefieldinterface);
			break;
		case toBattleField:
			battlefield.onMessageReceived(message);
			Message serverMessage = new Message();
			serverMessage.put("serverRequest", MessageRequest.updatebattlefield);
			serverMessage.put("ID", this.ID);
			serverMessage.put("battlefield", this.battlefield);
			serverBroadCast(serverMessage);
			break;
		case toUnit:
			if (message.get("unitID") != null) {
				int unitID = (int) message.get("unitID");
				if (unitMessages.get(unitID) != null) {
					unitMessages.get(unitID).put((int) message.get("id"), message);
				} else {
					unitMessages.put(unitID, new HashMap<Integer, Message>());
					unitMessages.get(unitID).put((int) message.get("id"), message);
				}
			}
			break;
		default:
			// No message type found
			break;
		}
	}

	public String getHOST() {
		return HOST;
	}

	public void setHOST(String HOST) {
		this.HOST = HOST;
	}

	public int getSERVER_REGISTRY_PORT() {
		return SERVER_REGISTRY_PORT;
	}

	public void setSERVER_REGISTRY_PORT(int SERVER_REGISTRY_PORT) {
		this.SERVER_REGISTRY_PORT = SERVER_REGISTRY_PORT;
	}

	public ArrayList<GameClient> getGameClients() {
		return gameClients;
	}

	public void setGameClients(ArrayList<GameClient> gameClients) {
		this.gameClients = gameClients;
	}

	public void removeMessage(int messageID, int unitID) {
		unitMessages.get(unitID).remove(messageID);
	}

	public Map<Integer, Message> getUnitMessages(int unitID) {
		return unitMessages.get(unitID);
	}

	public void setUnitMessages(Map<Integer, Map<Integer, Message>> unitMessages) {
		this.unitMessages = unitMessages;
	}

}
