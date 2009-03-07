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

package com.mina86.dc.tasks;

import java.util.Iterator;
import java.io.PrintStream;


/**
 * An implementation of nondeterministic BogoSort algorithm.
 *
 * BogoSort is a sorting algorithm that works in the following way:
 * (1) if the sequence is sorted then algorithm ends; (2) otherwise
 * generate next permutation of the sequence and go to step (1).
 * A nondeterministic version generates a random permutation.
 *
 * BogoSort algorithm has complexity of <code>O(n!)</code> which is
 * insane as far as sorting algorithm are concerned but it is perfect
 * for demonstrating distributed computing infrastructure.
 */
final public class BogoSort<T extends Comparable>
	extends AbstractVerifiableTask
	implements Iterable<T> {
	/** Version UID used for serialization. */
	static final long serialVersionUID = 0xbb94ec5699152e46L;

	/** Elements to sort. */
	private T data[];

	/**
	 * Initializes task.
	 * \param theData elements to sort.
	 */
	public BogoSort(T theData[]) {
		super(0, theData.length);
		data = (T[])new Comparable[theData.length]; /* Java sux */
		for (int i = 0; i < data.length; ++i) {
			data[i] = theData[i];
		}
	}

	/**
	 * Initializes task.
	 * \param it    elements to sort.
	 * \param count number of elements to sort.
	 */
	public BogoSort(Iterator<T> it, int count) {
		super(0, count);
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
