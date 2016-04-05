package presentation;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JPanel;

import RMI.GameServerInterface;
import game.BattleField;
import game.GameState;
import game.SimpleBattleField;
import game.SimpleBattleFieldInterface;
import units.SimpleDragon;
import units.SimplePlayer;
import units.SimpleUnit;

/**
 * Create an viewer, which runs in a seperate thread and
 * monitors the whole battlefield. Server side viewer,
 * this version cannot be run at client side.
 * 
 * @author Pieter Anemaet, Boaz Pat-El
 */
@SuppressWarnings("serial")
public class BattleFieldViewer extends JPanel implements Runnable {

	/* Double buffered image */
	private Image doubleBufferImage;
	/* Double buffered graphics */
	private Graphics doubleBufferGraphics;
	/* Dimension of the stored image */
	private int bufferWidth;
	private int bufferHeight;
	public SimpleBattleFieldInterface battle;
	
	GameServerInterface gameServer;
	int SERVER_REGISTRY_PORT;
	String SERVER_REGISTRY_HOST;
	Registry serverRegister;
	String serverID;

	/* The thread that is used to make the battlefield run in a separate thread.
	 * We need to remember this thread to make sure that Java exits cleanly.
	 * (See stopRunnerThread())
	 */
	private Thread runnerThread;

	/**
	 * Create a battlefield viewer in 
	 * a new thread. 
	 */
	public BattleFieldViewer(String serverID, String SERVER_REGISTRY_HOST, int SERVER_REGISTRY_PORT) {
		try {
			this.serverID = serverID;
			this.SERVER_REGISTRY_HOST = SERVER_REGISTRY_HOST;
			this.SERVER_REGISTRY_PORT = SERVER_REGISTRY_PORT;
			this.serverRegister = LocateRegistry.getRegistry(SERVER_REGISTRY_HOST, SERVER_REGISTRY_PORT);
			this.gameServer = (GameServerInterface) serverRegister.lookup(serverID);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		doubleBufferGraphics = null;
		runnerThread = new Thread(this);
		runnerThread.start();
	}

	/**
	 * Initialize the double buffer. 
	 */
	private void initDB() {
		bufferWidth = getWidth();
		bufferHeight = getHeight();
		doubleBufferImage = createImage(getWidth(), getHeight());
		doubleBufferGraphics = doubleBufferImage.getGraphics();
	}

	/**
	 * Paint the battlefield overview. Use a red color
	 * for dragons and a blue one for players. 
	 */
	public void paint(Graphics g) {
		SimpleUnit u = null;
		double x = 0, y = 0;
		double xRatio = (double)this.getWidth() / (double)BattleField.MAP_WIDTH;
		double yRatio = (double)this.getHeight() / (double)BattleField.MAP_HEIGHT;
		double filler;
		SimpleBattleFieldInterface bf = (SimpleBattleFieldInterface) battle;

		/* Possibly adjust the double buffer */
		if(bufferWidth != getSize().width 
				|| bufferHeight != getSize().height 
				|| doubleBufferImage == null 
				|| doubleBufferGraphics == null)
			initDB();

		/* Fill the background */
		//doubleBufferGraphics.setColor(Color.GREEN);
		doubleBufferGraphics.clearRect(0, 0, bufferWidth, bufferHeight);
		doubleBufferGraphics.setColor(Color.BLACK);

		/* Draw the field, rectangle-wise */
		for(int i = 0; i < BattleField.MAP_WIDTH; i++, x += xRatio, y = 0)
			for(int j = 0; j < BattleField.MAP_HEIGHT; j++, y += yRatio) {
				try {
					u = bf.getUnit(i, j);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (u == null) continue; // Nothing to draw in this sector

				if (u instanceof SimpleDragon)
					doubleBufferGraphics.setColor(Color.RED);
				else if (u instanceof SimplePlayer)
					doubleBufferGraphics.setColor(Color.BLUE);

				/* Fill the unit color */
				doubleBufferGraphics.fillRect((int)x + 1, (int)y + 1, (int)xRatio - 1, (int)yRatio - 1);

				/* Draw healthbar */
				doubleBufferGraphics.setColor(Color.GREEN);
				filler = (double)yRatio * u.getHitPoints() / (double)u.getMaxHitPoints();
				doubleBufferGraphics.fillRect((int)(x + 0.75 * xRatio), (int)(y + 1 + yRatio - filler), (int)xRatio / 4, (int)(filler));

				/* Draw the identifier */
				doubleBufferGraphics.setColor(Color.WHITE);
				doubleBufferGraphics.drawString("" + u.getUnitID(), (int)x, (int)y + 15);
				doubleBufferGraphics.setColor(Color.BLACK);

				/* Draw a rectangle around the unit */
				doubleBufferGraphics.drawRect((int)x, (int)y, (int)xRatio, (int)yRatio);
			}

		/* Flip the double buffer */
		g.drawImage(doubleBufferImage, 0, 0, this);
	}

	public void run() {
		final Frame f = new Frame();
		f.addWindowListener(new WindowListener() {
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {
				// What happens if the user closes this window?
				f.setVisible(false); // The window becomes invisible
				GameState.haltProgram(); // And the game stops running
				stopRunnerThread(); // And this thread stops running
			}
		});
		f.add(this);
		f.setMinimumSize(new Dimension(200, 200));
		f.setSize(1000, 1000);
		f.setVisible(true);
		
		while(GameState.getRunningState()) {		
			/* Keep the system running on a nice speed */
			try {
				Thread.sleep((int)(1000 * GameState.GAME_SPEED));
				try {
					battle= (SimpleBattleFieldInterface) serverRegister.lookup(gameServer.getBattleField());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				invalidate();
				repaint();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Stop the running thread. This has to be called explicitly to make sure the program 
	 * terminates cleanly.
	 */
	public void stopRunnerThread() {
		try {
			runnerThread.join();
		} catch (InterruptedException ex) {
			assert(false) : "BattleFieldViewer stopRunnerThread was interrupted";
		}
		
	}
}