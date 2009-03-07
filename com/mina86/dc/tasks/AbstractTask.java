package com.mina86.dc.tasks;

import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;
import java.io.ObjectInputStream;
import java.io.IOException;
import com.mina86.dc.common.Task;


/**
 * Abstract class implementing \link com.mina86.dc.common.Task
 * Task\endlink intrface.
 *
 * Classes extending this class must implement nextIteration() method
 * where a single iteration of the algorithm should be performed.
 * A single iteration should not take too much time.
 */
public abstract class AbstractTask implements Task {
	/** Version UID used for serialization. */
	static final long serialVersionUID = 0xe5acea7a6b2c8a31L;

	/** Whether the task is running or has been paused. */
	transient private boolean running = true;
	/** Number of iterations finished. */
	protected long iterations = 0;
	/** Number of iterations needed to do (or zero if unknown). */
	protected long end = 0;

	/** Task's size. */
	protected int taskSize;
	/** Task's processing time in nanoseconds. */
	private long processingTime = 0;


	/** Returns task's size. */
	public int size() {
		return taskSize;
	}

	/** Returns how long the task has been calculated in miliseconds.  */
	public long time() {
		/* processingTime is in nanoseconds so we need to divide it by milion */
		return processingTime / 1000000;
	}


	/**
	 * Constructs object.
	 * \param theEnd number of interations needed to finish task (or zero).
	 * \param size   task's size.
	 */
	protected AbstractTask(long theEnd, int size) {
		end = theEnd;
		taskSize = size;
	}

	/**
	 * Reads serialized object.  This method is implemented so that it
	 * can initialize fields that are not being serialized.
	 * \param in stream to read object from.
	 */
	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		running = true;
		listeners = new LinkedList<ProgressListener>();
	}


	/**
	 * Performs next iteration and returns whether there are more
	 * iterations to do or not.  This is the method that classes
	 * extending this class must implement and where real work is
	 * being done.
	 *
	 * The method should increment iterations field.
	 *
	 * \return whether there are more iterations to do.
	 */
	protected abstract boolean nextIteration();

	final public void pause  () {
		running = false;
	}

	final public void unpause() {
		running = true;
	}

	/**
	 * Runs a calcultion.  It calls nextIteration() in a loop untill
	 * it returns \c true (meaning task is finished) or task is
	 * paused.  Every time nextIteration() finishes progressNotify()
	 * is called to notify all listeners about our progress.
	 *
	 * \return whether tash is completed.
	 */
	final public boolean run() {
		boolean hasMoreWork = true;
		long startTime = System.nanoTime();
		while (hasMoreWork && running) {
			hasMoreWork = nextIteration();
			progressNotify();
		}
		processingTime += System.nanoTime() - startTime;
		return !hasMoreWork;
	}


	/** List of listeners. */
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


	/** Notifies all listeners about our progress. */
	final protected void progressNotify() {
		for (ProgressListener listener : listeners) {
			listener.onProgress(this, iterations, end);
		}
	};



	/* Returns \c false. */
	public boolean isVerifiable() {
		return false;
	}

	/** Throws UnsupportedOperationException. */
	public boolean verifyResult() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
};
