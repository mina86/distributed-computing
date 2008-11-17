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
