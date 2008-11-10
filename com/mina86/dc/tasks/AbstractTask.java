package com.mina86.dc.tasks;

import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;
import com.mina86.dc.common.Task;


public abstract class AbstractTask implements Task {
	static final long serialVersionUID = 0xb8b8c87befd452e2L;

	transient private boolean running = true;
	protected long iterations = 0, end;

	protected AbstractTask(long theEnd) {
		this.end = theEnd;
	}


	protected abstract boolean nextIteration();

	final public void pause  () {
		running = false;
	}

	final public void unpause() {
		running = true;
	}

	final public boolean run() {
		boolean haveMoreWork = true;
		while (haveMoreWork && running) {
			haveMoreWork = nextIteration();
			progressNotify();
		}
		return !haveMoreWork;
	}



	transient private List<ProgressListener> listeners =
		new LinkedList<ProgressListener>();

	final public void addProgressListener(ProgressListener listener) {
		ListIterator<ProgressListener> it = listeners.listIterator(0);
		while (it.hasNext()) {
			if (it.next() == listener) return;
		}
		it.add(listener);
	}

	final public void removeProgressListener(ProgressListener listener) {
		ListIterator<ProgressListener> it = listeners.listIterator(0);
		while (it.hasNext()) {
			if (it.next() == listener) {
				it.remove();
				return;
			}
		}
	}

	final protected void progressNotify() {
		for (ProgressListener listener : listeners) {
			listener.onProgress(this, iterations, end);
		}
	};



	public boolean isVerifiable() {
		return false;
	}

	public boolean verifyResult() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
};
