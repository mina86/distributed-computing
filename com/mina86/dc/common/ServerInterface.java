/*
 * Copyright 2008-2009 by Michal Nazarewicz (mina86/AT/mina86.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package com.mina86.dc.common;

import java.rmi.Remote;
import java.rmi.RemoteException;


/** An interface of remote distributed computing server. */
public interface ServerInterface extends Remote {
	/**
	 * Returns a task to perform.  It may return \c null if there are
	 * no pending task.  In such situation client should try again
	 * after a small delay.
	 *
	 * \a n specifies a desired problem size.  If it is zero a server
	 * default value will be used.  If there is no task of size \a
	 * n server should return task which size is nearest \a n.  This
	 * parameter may be used by clients to maintain a task-processing
	 * time near some given value.  The way task's size is calculated
	 * is task dependent.
	 *
	 * \param n desired task's size or zero meaning server default.
	 * \throw NegativeArraySizeException if n is negative.
	 */
	public Task getTask(int n)
		throws RemoteException, NegativeArraySizeException;


	/**
	 * Sends a result to the server.
	 * \param t task to send.
	 */
	public void sendResult(Task t) throws RemoteException;
};
