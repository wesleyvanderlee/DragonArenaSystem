package game;

import java.rmi.Remote;
import java.util.ArrayList;

import units.SimpleUnit;

public interface SimpleBattleFieldInterface extends Remote {
	public SimpleBattleField getBattleField() throws Exception;
	public String battleFieldString()throws Exception;
	public int[] getRandomLocation()throws Exception;
	public ArrayList<SimpleUnit>  getUnits()throws Exception;
	public SimpleUnit getUnit(int i, int j)throws Exception;
	public SimpleUnit[][] getMap()throws Exception;
	public void setMap(SimpleUnit[][] map) throws Exception;
	public void setUnits(ArrayList<SimpleUnit> units) throws Exception;
}
