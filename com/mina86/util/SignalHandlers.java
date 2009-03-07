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

package com.mina86.util;

import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;
import sun.misc.Signal;
import sun.misc.SignalHandler;


/**
 * A wrapper for sun.misc.SignalHandler.  Registers itself on some
 * common "interrupt" signals and notifies listeners when singal was
 * delivered.  The signals class registers itself are: \c XCPU, \c
 * XFSZ, \c INT, \c TERM and \c HUP.
 */
final public class SignalHandlers {
	/** An instance of the object. */
	private static SignalHandlers signalHandlers = null;

	/** Returns an instance of the object.  Method is \em not thread safe. */
	public static SignalHandlers get() {
		if (signalHandlers == null) {
			signalHandlers = new SignalHandlers();
		}
		return signalHandlers;
	}


	/** A constructor which registers itself on some signals. */
	private SignalHandlers() {
		String signals[] = { "XCPU", "XFSZ", "INT", "TERM", "HUP" };
		for (String name : signals) {
			try {
				final String n = name;
				new SignalHandler() {
					private SignalHandler old =
						Signal.handle(new Signal(n), this);
					public void handle(Signal sig) {
						notifyListeners();
						if (old != SIG_DFL && old != SIG_IGN) old.handle(sig);
					}
				};
			}
			catch (Throwable e) { /* ignore */ }
		}
	}


	/** A listener. */
	public interface Listener {
		/** Called when signal is delivered. */
		public void handleSignal();
	};

	/** A list of listeners. */
	private List<Listener> listeners = new LinkedList<Listener>();

	/**
	 * Adds a listener to the list.  If it already exists in the list
	 * it is not added for the second time.
	 * \param listener listener to add.
	 */
	public void addListener(Listener listener) {
		ListIterator<Listener> it = listeners.listIterator(0);
		while (it.hasNext()) {
			if (it.next() == listener) return;
		}
		it.add(listener);
	}

	/**
	 * Removes a listener from the list.  If it is not there no error
	 * is signaled (simply nothing happens).
	 * \param listener listener to remove.
	 */
	public void removeListener(Listener listener) {
		ListIterator<Listener> it = listeners.listIterator(0);
		while (it.hasNext()) {
			if (it.next() == listener) {
				it.remove();
				return;
			}
		}
	}

	/** Notifies all listeners that signal was delivered. */
	protected void notifyListeners() {
		for (Listener listener : listeners) {
			listener.handleSignal();
		}
	};
}
