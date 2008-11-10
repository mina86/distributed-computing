package com.mina86.dc.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
	public Task getTask() throws RemoteException;
	public void sendResult(Task t) throws RemoteException;
};
