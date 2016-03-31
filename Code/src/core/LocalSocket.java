package core;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import core.Message;
import core.Socket;
import core.exception.IDNotAssignedException;

@SuppressWarnings("serial")
public class LocalSocket extends Socket {

	/* ID of the current socket */
	private String url;
	/* Hashmap where ids are stored */
	private static Map<String, Socket> localMap = new HashMap<String, Socket>();

	/**
	 * Create a new local socket.
	 */
	public LocalSocket() throws IOException 
	{
		
	}

	/**
	 * Register the socket with the hashmap.
	 */
	@Override
	public void register(String ID) {
		// Use a new 'protocol' for LocalSockets
		url = "localsocket://"+ID;
		localMap.put(url, this);
	}

	/**
	 * Notify handlers that a message has arrived.
	 */
	public void receiveMessage(Message message) 
	{
		notifyReceivedHandlers(message, null);
	}

	/**
	 * Unregister the socket 
	 */
	@Override
	public void unRegister() 
	{
		localMap.remove(url);
	}

	/**
	 * @return this URL.
	 */
	public String getURL() 
	{
		return url;
	}

	/**
	 * Send a message to the specified URL.
	 * 
	 * @param message is the message to send.
	 * @param URL is the target address to send it to.
	 */
	@Override
	public void sendMessage(Message message, String URL ) 
	{
		if (localMap.containsKey(URL))
		{
			
			message.put("origin", getURL());
			localMap.get(URL).receiveMessage(message);
		}
		else
			throw new IDNotAssignedException();
	}
}