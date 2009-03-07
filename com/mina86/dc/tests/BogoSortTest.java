package com.mina86.dc.tests;

import java.util.Vector;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import com.mina86.dc.common.Task;
import com.mina86.dc.tasks.BogoSort;
import com.mina86.util.SignalHandlers;
import com.mina86.dc.client.TaskLoader;


/** A simple test program for a BogoSort task. */
final public class BogoSortTest
	implements Task.ProgressListener, SignalHandlers.Listener {


	/**
	 * Runs the test.
	 * \param args program arguments (ignored)
	 */
	public static void main(String [] args) {
		(new BogoSortTest()).run();
	}


	/** Runs the test. */
	private void run() {
		System.out.print("Bogo Sort testing program\n");
		BogoSort<String> bogoSortTask = null;

		/* Try to load saved task */
		if (TaskLoader.savedTaskExists()) {
			System.out.print("Loading saved task... ");
			try {
				bogoSortTask = (BogoSort<String>)TaskLoader.loadTask();
				System.out.print("done.\nLoaded:\n");
				for (String line : bogoSortTask) {
					System.out.print(">> ");
					System.out.println(line);
				}
			}
			catch (Exception e) {
				System.out.print("failed: " + e.toString() + "\n");
				bogoSortTask = null;
			}
		}


		/* Saved task not loaded, read task from stdin */
		if (bogoSortTask == null) {
			System.out.print("Please enter lines to sort and finish with an EOF character:\n");

			/* Read data */
			Vector<String> vector = new Vector<String>();
			BufferedReader reader =
				new BufferedReader(new InputStreamReader(System.in));

			try {
				String line;
				while ((line = reader.readLine()) != null) {
					vector.addElement(line);
				}
			}
			catch (IOException e) {
				/* ignore */
			}

			bogoSortTask =
				new BogoSort<String>(vector.iterator(), vector.size());
		}


		/* Initialise sorting */
		System.out.print("\nSorting...  ");
		task = bogoSortTask;
		onProgress(task, 0, 0);
		task.addProgressListener(this);
		task.unpause();


		/* Install signal handler */
		SignalHandlers.get().addListener(this);


		/* Run */
		boolean done = task.run();


		/* Print output */
		if (done) {
			TaskLoader.deleteTask();
			System.out.print("\bdone.\nSorted:\n");
			for (String line : bogoSortTask) {
				System.out.print(">> ");
				System.out.println(line);
			}
		} else {
			System.out.print("\nInterrupted.\nSaving task... ");
			try {
				TaskLoader.saveTask(task);
				System.out.print("done.\n");
			}
			catch (Exception e) {
				System.out.print("failed: " + e.toString() + "\n");
			}
		}
	}



	/** Characters used in animation. */
	private static char animation[] = { '.', 'o', 'O', '0', 'O', 'o' };
	/** Index of character used in animation. */
	private int animationPos = -1;
	/** Last time animation character was changed. */
	private long lastTick = 0;
	/** Last time task was saved. */
	private long lastSave = 0;

	/**
	 * Called each time task finishes single cycle.  This method saves
	 * task on disk every five seconds and updates animation four
	 * times per second.
	 * \param task       task being calculated.
	 * \param iterations how many iterations there wer (ignored).
	 * \param end        how many iterations are needed (ignored).
	 */
	public void onProgress(Task task, long iterations, long end) {
		long tick = System.currentTimeMillis();
		if (tick - lastSave >= 5000) {
			try { TaskLoader.saveTask(task); }
			catch (Exception e) { /* ignore */ }
			lastSave = tick;
		}
		if (tick - lastTick >= 250) {
			lastTick = tick;
			System.out.print('\b');
			animationPos = (animationPos + 1) % animation.length;
			System.out.print(animation[animationPos]);
		}
	}


	/** The task being performed. */
	private Task task;

	public void handleSignal() {
		task.pause();
	}
}
