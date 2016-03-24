package core;

public interface IStatusListener {
	/**
	 * The internal status of the given Socket has changed.
	 *
	 * Use socket.getInternalStatus( ) to find out what the new status is.
	 *
	 * @arg socket  The Socket which' status changed.
	 */
	public void statusChanged( Socket socket );
}
