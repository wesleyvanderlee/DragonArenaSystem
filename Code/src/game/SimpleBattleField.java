package game;

import java.awt.Color;
import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import javax.swing.text.html.StyleSheet;

import RMI.Message;
import RMI.MessageRequest;
import units.SimpleDragon;
import units.SimplePlayer;
import units.SimpleUnit;
import units.Unit.UnitType;

public class SimpleBattleField extends UnicastRemoteObject implements SimpleBattleFieldInterface{
	private SimpleUnit[][] map;
	public final static int MAP_WIDTH = 25;
	public final static int MAP_HEIGHT = 25;
	private ArrayList<SimpleUnit> units;
	boolean accept = false;
	public SimpleBattleField()throws IOException {
		synchronized (this) {
			this.map = new SimpleUnit[MAP_WIDTH][MAP_HEIGHT];
			units = new ArrayList<SimpleUnit>();
		}
	}
	
	public SimpleBattleField getBattleField() throws Exception 
	{
		return this;
	}
	
	private boolean spawnUnit(SimpleUnit unit, int x, int y) {
		synchronized (this) {
			if (map[x][y] != null)
				return false;

			map[x][y] = unit;
			unit.setPosition(x, y);
		}
		units.add(unit);
		return true;
	}

	private synchronized boolean putUnit(SimpleUnit unit, int x, int y) {
		if (map[x][y] != null)
			return false;
		map[x][y] = unit;
		unit.setPosition(x, y);
		return true;
	}

	public SimpleUnit getUnit(int x, int y) {
		assert x >= 0 && x < map.length;
		assert y >= 0 && x < map[0].length;
		return map[x][y];
	}

	private synchronized boolean moveUnit(SimpleUnit unit, int newX, int newY) {
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
		SimpleUnit unitToRemove = this.getUnit(x, y);
		if (unitToRemove == null)
			return; // There was no unit here to remove
		map[x][y] = null;
		unitToRemove.disconnect();
		units.remove(unitToRemove);
	}
		
	public synchronized void shutdown() {
		// Remove all units from the battlefield and make them disconnect from the server
		for (SimpleUnit unit : units) {
			unit.disconnect();
			unit.stopRunnerThread();
		}
	}

	public Message onMessageReceived(Message msg) {
		Message reply = null;
		String origin = (String)msg.get("origin");
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
				reply = new Message();
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				reply.put("id", msg.get("id"));
				// Get the unit at the specific location
				reply.put("unit", getUnit(x, y));
				break;
			}
			case getType:
			{
				reply = new Message();
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
				break;
			}
			case dealDamage:
			{
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				unit = this.getUnit(x, y);
				if (unit != null)
					unit.adjustHitPoints( -(Integer)msg.get("damage") );
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				break;
			}
			case healDamage:
			{
				int x = (Integer)msg.get("x");
				int y = (Integer)msg.get("y");
				unit = this.getUnit(x, y);
				if (unit != null)
					unit.adjustHitPoints( (Integer)msg.get("healed") );
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				break;
			}
			case moveUnit:
				reply = new Message();
				this.moveUnit((SimpleUnit)msg.get("unit"), (Integer)msg.get("x"), (Integer)msg.get("y"));
				/* Copy the id of the message so that the unit knows 
				 * what message the battlefield responded to. 
				 */
				reply.put("id", msg.get("id"));
				break;
			case removeUnit:
				this.removeUnit((Integer)msg.get("x"), (Integer)msg.get("y"));
				break;
		}
		return reply;
	}
	
	public int[] getRandomLocation()
	{
		/* Try picking a random spot */
		int x, y, attempt = 0;
		do {
			x = (int) (Math.random() * BattleField.MAP_WIDTH);
			y = (int) (Math.random() * BattleField.MAP_HEIGHT);
			attempt++;
		} while (getUnit(x, y) != null && attempt < 10);

		int[] values={x,y};
		return values;
	}
	
	public String battleFieldString()throws Exception
	{
		String string="";
		int count= 0;
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
					
					string += "P ";
				}
				else if(map[r][c] instanceof SimpleDragon)
				{	
					string += "D ";
				}
				
			}
			count++;
			string += "|\n";
			string += "  ----------------------------------------------------------------------------\n";
		}
		return string;
		
	}
}
