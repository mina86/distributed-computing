package com.mina86.dc.common;

import java.rmi.Remote;
import java.rmi.RemoteException;


/** An interface of remote distributed computing server. */
public interface ServerInterface extends Remote {
	/**
	 * Returns a task to perform.  It may return \c null if there are
	 * no pending task.  In such situation client should try again
	 * after a small delay.
	 */
	public Task getTask() throws RemoteException;

	/**
	 * Sends a result to the server.
	 * \param t task to send.
	 */
	public void sendResult(Task t) throws RemoteException;
};
