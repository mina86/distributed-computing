package com.mina86.util;

import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;
import sun.misc.Signal;
import sun.misc.SignalHandler;


final public class SignalHandlers {
	private static SignalHandlers signalHandlers = null;

	public static SignalHandlers get() {
		if (signalHandlers == null) {
			signalHandlers = new SignalHandlers();
		}
		return signalHandlers;
	}


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


	public interface Listener {
		public void handleSignal();
	};

	private List<Listener> listeners = new LinkedList<Listener>();

	public void addListener(Listener listener) {
		ListIterator<Listener> it = listeners.listIterator(0);
		while (it.hasNext()) {
			if (it.next() == listener) return;
		}
		it.add(listener);
	}

	public void removeListener(Listener listener) {
		ListIterator<Listener> it = listeners.listIterator(0);
		while (it.hasNext()) {
			if (it.next() == listener) {
				it.remove();
				return;
			}
		}
	}

	protected void notifyListeners() {
		for (Listener listener : listeners) {
			listener.handleSignal();
		}
	};
}
