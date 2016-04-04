package core;

import RMI.Message;

public interface IMessageReceivedHandler {
	public void onMessageReceived(Message message);
}
