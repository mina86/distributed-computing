package com.mina86.dc.tasks;

import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;


/**
 * Abstract class implementing \link com.mina86.dc.common.Task
 * Task\endlink intrface.  This class requires that \a verifyResult()
 * is implemented.
 *
 * Classes extending this class must implement generateNextState() and
 * quickVerifyResult() methods.  They can also overwrite
 * verifyResult() if required.
 *
 * This class works by the assumption that a single iteration consits
 * of testing whether state we are in is a valid answer.  If it is
 * then algorithm complets.  Otherwise a new state is generated and wo
 * go back to the begining.
 */
public abstract class AbstractVerifiableTask extends AbstractTask {
	/** Version UID used for serialization. */
	static final long serialVersionUID = 0xd9b74322276c8487L;


	/**
	 * Constructs object.
	 * \param theEnd number of interations needed to finish task (or zero).
	 * \param size   task's size.
	 */
	protected AbstractVerifiableTask(long theEnd, int size) {
		super(theEnd, size);
	}


	/**
	 * Performs a single iteration of the algorithm.  First calls
	 * quickVerifyResult() to verify if our current state is valid
	 * answer.  If it is returns \c false otehrwise generates next
	 * state by calling generateNextState() and returns \c true.
	 *
	 * \return whether there are more iterations to do.
	 */
	final protected boolean nextIteration() {
		if (quickVerifyResult()) {
			return false;
		}
		generateNextState();
		++iterations;
		return true;
	}


	/** Returns \c true. */
	final public boolean isVerifiable() {
		return true;
	}

	/** Calls quickVerifyResult(). */
	public boolean verifyResult() {
		return quickVerifyResult();
	}


	/** Verifies the result. */
	protected abstract boolean quickVerifyResult();
	/** Generates new state (result). */
	protected abstract void generateNextState();
};
