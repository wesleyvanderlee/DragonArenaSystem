package game;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import RMI.GameServerInterface;
import RMI.Message;
import RMI.MessageRequest;
import units.SimpleDragon;
import units.SimplePlayer;
import units.SimpleUnit;
import units.SimpleUnit.UnitType;

public class SimpleBattleField extends UnicastRemoteObject implements SimpleBattleFieldInterface, Serializable{
	public SimpleUnit[][] map;
	public final static int MAP_WIDTH = 25;
	public final static int MAP_HEIGHT = 25;	
	private ArrayList<SimpleUnit> units;
	boolean accept = false;
	public static SimpleBattleField battlefield;
	private int lastUnitID = 0;
	GameServerInterface gameServer;
	int SERVER_REGISTRY_PORT;
	Registry serverRegister;
	
	public SimpleBattleField(String serverID, String SERVER_REGISTRY_HOST, int SERVER_REGISTRY_PORT)throws IOException {
		synchronized (this) {
			this.map = new SimpleUnit[MAP_WIDTH][MAP_HEIGHT];
			units = new ArrayList<SimpleUnit>();
			try {
				this.SERVER_REGISTRY_PORT = SERVER_REGISTRY_PORT;
				this.serverRegister = LocateRegistry.getRegistry(SERVER_REGISTRY_HOST, SERVER_REGISTRY_PORT);
				this.gameServer = (GameServerInterface) serverRegister.lookup(serverID);
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
	}
	
	public SimpleBattleField getBattleField() throws Exception 
	{
		return this;
	}
	public ArrayList<SimpleUnit> getUnits() throws Exception 
	{
		return units;
	}
	
	public boolean spawnUnit(SimpleUnit unit, int x, int y) {
		synchronized (this) {
			if (map[x][y] != null)
				return false;

			map[x][y] = unit;
			unit.setPosition(x, y);
		}
		units.add(unit);
		return true;
	}

	public boolean putUnit(SimpleUnit unit, int x, int y) {
		//unit.setPosition(x, y);
		map[x][y] = unit;
		return true;
	}

	public SimpleUnit getUnit(int x, int y) {
		assert x >= 0 && x < map.length;
		assert y >= 0 && x < map[0].length;
		return map[x][y];
	}
	
	public boolean moveUnit(SimpleUnit unit, int newX, int newY) {
		if(unit == null)
			return false;
		
		int prevX = unit.getX();
		int prevY = unit.getY();

		if (unit.getHitPoints() <= 0)
		{
			return false;
		}
		if (newX >= 0 && newX < MAP_WIDTH)
		{
			if (newY >= 0 && newY < MAP_HEIGHT)
			{
				if (map[newX][newY] == null) 
				{
					if (putUnit(unit, newX, newY))
					{
						map[prevX][prevY] = null;
						return true;
					}
				}
			}
		}
		return false;
	}

	public void removeUnit(int x, int y) {
		SimpleUnit unitToRemove = this.getUnit(x, y);

		if (unitToRemove == null)
			return; // There was no unit here to remove
		map[x][y] = null;
		unitToRemove.disconnect();
		units.remove(unitToRemove);
	}
	
	public void onMessageReceived(Message msg) {
		if((int)msg.get("unitID")>20)
			System.out.println("battle "+msg);
		MessageRequest request = (MessageRequest)msg.get("request");
		SimpleUnit unit;
		switch(request)
		{
			case spawnUnit:
				this.spawnUnit((SimpleUnit)msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y"));
				break;
			case putUnit:
				this.putUnit((SimpleUnit)msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y"));
				break;
			case getUnit:
			{
				Message reply = new Message();
				reply.put("unitID", msg.get("unitID"));
				reply.put("unit", msg.get("unit"));
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				reply.put("id", msg.get("id"));
				// Get the unit at the specific location
				reply.put("unit", getUnit(x, y));
				sendServerMessage(reply);
				break;
			}
			case getType:
			{
				Message reply = new Message();
				reply.put("unitID", msg.get("unitID"));
				reply.put("unit", msg.get("unit"));
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				reply.put("id", msg.get("id"));
				if (getUnit(x, y) instanceof SimplePlayer)
				{
					reply.put("type", UnitType.player);
				}
				else if (getUnit(x, y) instanceof SimpleDragon)
				{
					reply.put("type", UnitType.dragon);
				}
				else 
				{
					reply.put("type", UnitType.undefined);
				}
				sendServerMessage(reply);
				break;
			}
			case dealDamage:
			{
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				unit = this.getUnit(x, y);

				//TODO unit is null
				if (unit != null)
				{
					unit.adjustHitPoints( -(Integer)msg.get("damage") );
				}
				break;
			}
			case healDamage:
			{
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				unit = this.getUnit(x, y);
				if (unit != null)
					unit.adjustHitPoints( (Integer)msg.get("healed") );
				break;
			}
			case moveUnit:
			{
				Message reply = new Message();
				reply.put("unitID", msg.get("unitID"));
				reply.put("unit", msg.get("unit"));
				this.moveUnit((SimpleUnit) msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y"));
				reply.put("id", msg.get("id"));
				sendServerMessage(reply);
				break;
			}
			case removeUnit:
			{
				this.removeUnit((Integer)msg.get("x"), (Integer)msg.get("y"));
				break;
			}
		}
	}
	
	private void sendServerMessage(Message message) {
		message.put("serverRequest", MessageRequest.toUnit);
		try {
			gameServer.onMessageReceived(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int[] getRandomLocation()
	{
		/* Try picking a random spot */
		int x, y, attempt = 0;
		do {
			x = (int) (Math.random() * MAP_WIDTH);
			y = (int) (Math.random() * MAP_HEIGHT);
			attempt++;
		} while (getUnit(x, y) != null && attempt < 10);

		int[] values={x,y};
		return values;
	}
	
	public String battleFieldString()throws Exception
	{
		String string="";
		int count= 1;
		int newCount=0;
		string += "  1 |2 |3 |4 |5 |6 |7 |8 |9 |10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|\n";
		for(int r = 0; r < map.length; r++)
		{
			string += count;
			for(int c = 0; c < map.length; c++)
			{
				string += "|";
				if(map[r][c] == null)
				{
					string += "  ";
				}
				else if(map[r][c] instanceof SimplePlayer)
				{
					newCount++;
					string += "P ";
				}
				else if(map[r][c]instanceof SimpleDragon)
				{	
					string += "D ";
				}
			}
			count++;
			string += "|\n";
			string += "  ----------------------------------------------------------------------------\n";
			
		}
		string += newCount +"\n";
		string += units.size() +"\n";
		return string;
		
	}

	public SimpleUnit[][] getMap() {
		return map;
	}

	public void setMap(SimpleUnit[][] map) {
		this.map = map;
	}

	public void setUnits(ArrayList<SimpleUnit> units) {
		this.units = units;
	}

	/**
	 * Returns a new unique unit ID.
	 * @return int: a new unique unit ID.
	 */
	public synchronized int getNewUnitID() {
		return ++lastUnitID;
	}

}
