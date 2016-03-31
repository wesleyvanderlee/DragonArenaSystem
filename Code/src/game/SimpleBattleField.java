package game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import RMI.Configuration;
import units.Unit;

public class SimpleBattleField implements Runnable {
	private Unit[][] map;
	public final static int MAP_WIDTH = 25;
	public final static int MAP_HEIGHT = 25;
	private ArrayList<Unit> units;
	ServerSocket singleBattlefieldSocket;
	Socket connection = null;
	ObjectOutputStream out;
	ObjectInputStream in;
	int port;
	boolean accept = true;

	public SimpleBattleField(int port) {
		// Socket local = new LocalSocket();
		// this.battlefieldID = ID;
		synchronized (this) {
			this.map = new Unit[MAP_WIDTH][MAP_HEIGHT];
			// local.register(BattleField.battlefieldID);
			// serverSocket = new SynchronizedSocket(local);
			// serverSocket.addMessageReceivedHandler(this);
			units = new ArrayList<Unit>();
			this.port = port;

		}
	}

	public void onMessageRecieved() {

	}

	private boolean spawnUnit(Unit unit, int x, int y) {
		synchronized (this) {
			if (map[x][y] != null)
				return false;

			map[x][y] = unit;
			unit.setPosition(x, y);
		}
		units.add(unit);
		return true;
	}

	private synchronized boolean putUnit(Unit unit, int x, int y) {
		if (map[x][y] != null)
			return false;
		map[x][y] = unit;
		unit.setPosition(x, y);
		return true;
	}

	public Unit getUnit(int x, int y) {
		assert x >= 0 && x < map.length;
		assert y >= 0 && x < map[0].length;
		return map[x][y];
	}

	private synchronized boolean moveUnit(Unit unit, int newX, int newY) {
		int prevX = unit.getX();
		int prevY = unit.getY();

		if (unit.getHitPoints() <= 0)
			return false;

		if (newX >= 0 && newX < BattleField.MAP_WIDTH)
			if (newY >= 0 && newY < BattleField.MAP_HEIGHT)
				if (map[newX][newY] == null) {
					if (putUnit(unit, newX, newY)) {
						map[prevX][prevY] = null;
						return true;
					}
				}
		return false;
	}

	private synchronized void removeUnit(int x, int y) {
		Unit unitToRemove = this.getUnit(x, y);
		if (unitToRemove == null)
			return; // There was no unit here to remove
		map[x][y] = null;
		unitToRemove.disconnect();
		units.remove(unitToRemove);
	}

	public synchronized void shutdown() {
		// Remove all units from the battlefield and make them disconnect from
		// the server
		for (Unit unit : units) {
			unit.disconnect();
			unit.stopRunnerThread();
		}
	}

	@Override
	public void run() {
		try {
			
			singleBattlefieldSocket = new ServerSocket(port, 10);
			
			while (true) {
				connection = singleBattlefieldSocket.accept();
				out = new ObjectOutputStream(connection.getOutputStream());
				in = new ObjectInputStream(connection.getInputStream());
				String message = (String) in.readObject();
				System.out.println("reading dragon message: " + message);

				if (message.equals("SPAWN")) {
					System.out.println("SimpleBattleField recieved message: " + message);
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
