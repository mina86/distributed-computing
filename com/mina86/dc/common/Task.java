package com.mina86.dc.common;

import java.io.Serializable;
import java.io.PrintStream;


/** Task to perform by clients. */
public interface Task extends Serializable {
	/**
	 * Pauses the task.  If another thread is executing run() the
	 * method shall return after teh current iteration is done.
	 * \see unpause(), run()
	 */
	public void pause  ();

	/**
	 * Unpauses the task.  If the task was paused previously it needs
	 * to be unpaused before calling run() since run() will return
	 * immidiatelly.  In most cases it is best to unpause() priori to
	 * each run().
	 * \see pause(), run()
	 */
	public void unpause();

	/**
	 * Runs the task.  The method will return if either the task is
	 * completed (and then it returns \c true) or when task is paused
	 * (and then it returns \c false).
	 *
	 * While the task is being run registered lsiteners will be called
	 * after each iteration is finished so that they can monitor the
	 * progress of the task.
	 *
	 * \see pause(), unpause()
	 */
	public boolean run();


	/** Returns task's size. */
	public int size();

	/** Returns how long the task has been calculated in miliseconds. */
	public long time();


	/** Listener of task's progress. */
	public interface ProgressListener {
		/**
		 * Called from run() after each iteration.  It is ment to be
		 * used to monitor the progress of the task (and for instance
		 * display a progress bar).
		 *
		 * \a iterations specify how many iterations were done and \a
		 * end how many are needed to finish the task.  Be aware that
		 * it is not guaranteed that each iteration takes the same
		 * amount of time.  Also \a end <b>may be zero</b> which means
		 * that there is no way to say in advance how many tierations
		 * are required.
		 *
		 * \param task       task that is being calculated.
		 * \param iterations number of finished iterations.
		 * \param end        number of needed iterations <b>or zero</b>.
		 */
		void onProgress(Task task, long iterations, long end);
	};

	/**
	 * Adds a progress listener.  Does nothing if it was already added.
	 * \param listener listener to add.
	 */
	public void addProgressListener   (ProgressListener listener);
	/**
	 * Removes a progress listener.  Does nothing if it was not added.
	 * \param listener listener to remove.
	 */
	public void removeProgressListener(ProgressListener listener);


	/** Returns whether \a verifyResult() method is implemented. */
	public boolean isVerifiable();
	/**
	 * Verifies the result.  The intention is that if there is a fast
	 * algorithm for verifying the result it should be implemented in
	 * this method.
	 * \throw UnsupportedOperationException if verification is not implemented.
	 */
	public boolean verifyResult() throws UnsupportedOperationException;
}
