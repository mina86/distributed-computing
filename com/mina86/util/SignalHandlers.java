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
