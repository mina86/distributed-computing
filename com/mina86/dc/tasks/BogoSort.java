package com.mina86.dc.tasks;

import java.util.Iterator;
import java.io.PrintStream;


final public class BogoSort<T extends Comparable>
	extends AbstractVerifiableTask
	implements Iterable<T> {
	static final long serialVersionUID = 0xcacb0e95c8449adfL;

	private T data[];

	public BogoSort(T theData[]) {
		super(0);
		data = (T[])new Comparable[theData.length]; /* Java sux */
		for (int i = 0; i < data.length; ++i) {
			data[i] = theData[i];
		}
	}

	public BogoSort(Iterator<T> it, int count) {
		super(0);
		data = (T[])new Comparable[count]; /* Java sux */
		for (int i = 0; it.hasNext(); ++i) {
			data[i] = it.next();
		}
	}



	protected boolean quickVerifyResult() {
		for (int i = 1, count = data.length; i < count; ++i) {
			if (data[i-1].compareTo(data[i]) > 0) return false;
		}
		return true;
	}

	protected void generateNextState() {
		int count = data.length;
		while (count != 0) {
			int pos = (int)(Math.random() * count);
			--count;
			T tmp = data[count];
			data[count] = data[pos];
			data[pos] = tmp;
		}
	}



	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private int pos = 0;
			public boolean hasNext() { return pos < data.length; }
			public T next() { return data[pos++]; }
			public void remove() throws UnsupportedOperationException {
				throw new UnsupportedOperationException();
			}
		};
	}
};
