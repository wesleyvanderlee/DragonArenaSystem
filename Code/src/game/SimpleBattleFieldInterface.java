package game;

import java.rmi.Remote;

public interface SimpleBattleFieldInterface extends Remote {
	public SimpleBattleField getBattleField() throws Exception;
	public String battleFieldString()throws Exception;
	public int[] getRandomLocation()throws Exception;
}
