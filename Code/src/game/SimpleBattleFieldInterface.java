package game;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.ArrayList;

import RMI.Message;
import units.SimpleUnit;
import units.SimpleUnit.UnitType;

public interface SimpleBattleFieldInterface extends Remote, Serializable {
	public SimpleBattleField getBattleField() throws Exception;
	public String battleFieldString()throws Exception;
	public int[] getRandomLocation()throws Exception;
	public ArrayList<SimpleUnit>  getUnits()throws Exception;
	public SimpleUnit getUnit(int i, int j)throws Exception;
	public SimpleUnit[][] getMap()throws Exception;
	public void setMap(SimpleUnit[][] map) throws Exception;
	public void setUnits(ArrayList<SimpleUnit> units) throws Exception;
	public int getNewUnitID() throws Exception;
	public boolean moveUnit(SimpleUnit unit, int newX, int newY)throws Exception;
	public boolean spawnUnit(SimpleUnit unit, int x, int y)throws Exception;
	public void removeUnit(int x, int y) throws Exception;
	public boolean putUnit(SimpleUnit unit, int x, int y)throws Exception;
	public void onMessageReceived(Message message)throws Exception;
}
