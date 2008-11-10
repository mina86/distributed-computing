package com.mina86.dc.tasks;

import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;


public abstract class AbstractVerifiableTask extends AbstractTask {
	static final long serialVersionUID = 0x001fc848a0b19359L;


	protected AbstractVerifiableTask(long theEnd) {
		super(theEnd);
	}


	final protected boolean nextIteration() {
		if (quickVerifyResult()) {
			return false;
		}
		generateNextState();
		++iterations;
		return true;
	}


	final public boolean isVerifiable() {
		return true;
	}

	public boolean verifyResult() {
		return quickVerifyResult();
	}


	protected abstract boolean quickVerifyResult();
	protected abstract void generateNextState();
};
