package core;

/**
 * Callback interface for clients who wish to
 * monitor a Socket.
 * 
 * Implement this interface and register your
 * object with Socket.addMessageReceivedHandler( )
 * to receive notifications when a message is
 * received.
 */
public interface IMessageReceivedHandler {
	/**
	 * Called when a message has been received by 
	 * the underlying socket.
	 * 
	 * @param message is the message received.
	 */
	public void onMessageReceived(Message message);
}
