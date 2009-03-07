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


/** Runs given command until it succeeds. */
public final class RunRetry {
	/** A job that is being run. */
	public interface Job {
		/**
		 * Called with each iteration.
		 * \param first whether it is the first call or not.
		 * \return whether operation succeeded.
		 * \throw InterruptedException if it wants it can. ;)
		 */
		public boolean run(boolean first) throws InterruptedException;
	};


	/**
	 * Executes given \a job until it succeeds.  If it does not
	 * succeed waits \a startDelay seconds and repeats operation.  If
	 * operation still fails doubles the time to wait and repeats
	 * everything.  The sleeping time is doubled until it reaches \a
	 * maxDelay.
	 *
	 * \param job        job to run.
	 * \param startDelay starting delay in seconds.
	 * \param maxDelay   maximal delay in seconds.
	 * \throw InterruptedException if thread was interrupted while
	 *                             sleeping or job throws that exception.
	 */
	public static void run(Job job, long startDelay, long maxDelay)
		throws InterruptedException {
		long delay = startDelay;
		for (boolean ok = job.run(true); !ok; ok = job.run(false)) {
			System.out.println("Sleeping " + delay + " before retry.");
			Thread.sleep(delay * 1000);
			delay *= 2;
			if (delay > maxDelay) delay = maxDelay;
		}
	}


	/**
	 * Executes given \a job until it succeeds.  If it does not
	 * succeed waits \a startDelay seconds and repeats operation.  If
	 * operation still fails doubles the time to wait and repeats
	 * everything.  The sleeping time is doubled until it reaches 256
	 * times \a startDelay.
	 *
	 * \param job        job to run.
	 * \param startDelay starting delay in seconds.
	 * \throw InterruptedException if thread was interrupted while
	 *                             sleeping or job throws that exception.
	 */
	public static void run(Job job, long startDelay)
		throws InterruptedException {
		run(job, startDelay, startDelay * 256);
	}


	/**
	 * Executes given \a job until it succeeds.  If it does not
	 * succeed waits 4 seconds and repeats operation.  If operation
	 * still fails doubles the time to wait and repeats everything.
	 * The sleeping time is doubled until it reaches 1024.
	 *
	 * \param job        job to run.
	 * \throw InterruptedException if thread was interrupted while
	 *                             sleeping or job throws that exception.
	 */
	public static void run(Job job) throws InterruptedException {
		run(job, 4, 1024);
	}
};
