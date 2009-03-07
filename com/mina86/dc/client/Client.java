/** Implementation of distributed computing client program. */
package com.mina86.dc.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DecimalFormat;
import com.mina86.DC;
import com.mina86.dc.client.TaskLoader;
import com.mina86.dc.common.ServerInterface;
import com.mina86.dc.common.Task;
import com.mina86.util.GetOptions;
import com.mina86.util.RunRetry;
import com.mina86.util.SignalHandlers;


/** Distributed computing client class. */
public final class Client implements Task.ProgressListener, DC.Application {

	/** Whether client is running or was it interrupted and should stop. */
	private volatile boolean running = true;

	/**
	 * An entry function called from com.mina86.DC.main().
	 * \param args arguments.
	 */
	public void run(String args[]) {
		try {
			GetOptions.VectorHandler vec = new GetOptions.VectorHandler(1);
			GetOptions getopts = new GetOptions();
			GetOptions.IntegerHandler timeArg =
				new GetOptions.IntegerHandler(0, 1, Integer.MAX_VALUE);
			getopts.addOption("-", vec, GetOptions.TakesArg.REQ);
			getopts.addOption("t", timeArg, GetOptions.TakesArg.REQ);
			getopts.parseArguments(args, 1);

			serviceName = vec.get(0, DC.defaultServiceName);
			rmiURL = vec.get(1, DC.defaultRegistryURL);
			constTime = timeArg.value * 1000;
			if (constTime != 0) {
				System.out.println("Aiming at constant time " +
				                   formatTime(constTime) + ".");
			}
		}
		catch (GetOptions.Exception e) {
			System.err.println(e.getFullMessage());
			System.exit(1);
		}

		try {
			do {
				RunRetry.run(new RunRetry.Job() {
					public boolean run(boolean first) { return getTask(first); }
				});

				/* Run task */
				if (runTask()) {
					RunRetry.run(new RunRetry.Job() {
						public boolean run(boolean first) { return sendTask(first); }
					});
				} else {
					running = false;
					saveTask();
				}
			} while (running);
		}
		catch (InterruptedException e) {
			System.out.println("Interrupted.");
			if (task != null) {
				saveTask();
			}
		}

		System.out.println("Client finished.");
	}


	/** URL to the RMI registry. */
	private String rmiURL;
	/** Distributed computing server name in RMI registry. */
	private String serviceName;
	/** Distributed computing server. */
	private ServerInterface server = null;
	/** Task being calculated. */
	private Task task = null;

	/** If we are aiming at constant time then what time period otherwise 0. */
	private long constTime = 0;
	/** Task's size to request. */
	private int taskSizeToRequest = 0;


	/** Tries to look up the server. */
	private void getServer() throws RemoteException, NotBoundException {
		if (server == null) {
			/* Get registry */
			System.out.print("Getting registry... ");
			Registry registry = LocateRegistry.getRegistry(rmiURL);
			System.out.println("done.");

			/* Get server */
			System.out.print("Looking server up (" + serviceName + ")... ");
			server = (ServerInterface)registry.lookup(serviceName);
			System.out.println("done.");
		}
	}

	/**
	 * Tries to get a task.  If \a tryLoad is \c true will first try
	 * to load task from file system (if one exists).
	 * \param tryLoad whether to try loading cached task.
	 */
	private boolean getTask(boolean tryLoad) {
		/* Load saved task from file */
		if (tryLoad && TaskLoader.savedTaskExists()) {
			System.out.print("Loading saved task... ");
			try {
				task = TaskLoader.loadTask();
				System.out.print("done (n = " + task.size() + ").\n");
				return true;
			}
			catch (Exception e) {
				System.out.println("failed.\n" + e.toString());
			}
		}

		/* Download task */
		try {
			getServer();

			System.out.print("Downloading task... ");
			while ((task = server.getTask(taskSizeToRequest)) == null) {
				System.out.println("no task.");
				Thread.sleep(15);
				System.out.print("Downloading task... ");
			}
			System.out.print("done (n = " + task.size() + ").\n");

			return true;
		}
		catch (Exception e) {
			System.out.println("failed.\n" + e.toString());
			server = null;
		}
		return false;
	}


	/**
	 * Returns a nicely formatted time.
	 * \param time the time in miliseconds.
	 */
	private String formatTime(long time) {
		if (time < 60000) {
			return (new DecimalFormat("0.000")).format(time / 1000.0) + " s";
		}

		String days = "";
		if (time > 24 * 60 * 60000) {
			days = time / (24 * 60 * 6000) + " d ";
		}

		int   s = (int)  (time % 60000); time /= 60000;
		short m = (short)(time %    60); time /=    60;

		return days +
			(new DecimalFormat("##")).format(time % 24) + ":" +
			(new DecimalFormat("00")).format(m) + ":" +
			(new DecimalFormat("00.000")).format(s / 1000.0);
		/* Now compare it to sprintf(buffer, "%2d:%02d:%02d:%03d",
		 * ...) which one would use in C. */
	}

	/** Starts calculating task. */
	private boolean runTask() {
		System.out.print("Calculating...  ");
		lastTick = lastSave = 0;
		onProgress(task, 0, 0);
		task.addProgressListener(this);
		task.unpause();

		if (!running || !task.run()) {
			System.out.println("\nInterrupted.");
			return false;
		}

		long time = task.time();
		System.out.println("\bdone in " + formatTime(time) + ".");

		if (constTime != 0) {
			taskSizeToRequest = task.size();
			if (time < constTime / 2) {
				taskSizeToRequest += 1;
			} else if (time > 3 * constTime / 2) {
				taskSizeToRequest -= 1;
			}
		}

		return true;
	}


	/** Saves task on disk. */
	private void saveTask() {
		System.out.print("\nSaving task... ");
		try {
			TaskLoader.saveTask(task);
			System.out.print("done.\n");
		}
		catch (Exception e) {
			System.out.println("failed.\n" + e.toString() + "\n");
		}
	}


	/**
	 * Sends task to server.  If \a trySave is \c true and method was
	 * unable to save task it will save it on disk.
	 * \param trySave whether to save task on disk if sending fails.
	 */
	private boolean sendTask(boolean trySave) {
		/* Send result */
		try {
			getServer();
			System.out.print("Sending result... ");
			server.sendResult(task);
			System.out.print("done.\n");
			TaskLoader.deleteTask();
			task = null;
			return true;
		}
		catch (Exception e) {
			System.out.println("failed.\n" + e.toString());
			server = null;
		}

		/* Save task */
		if (trySave) {
			saveTask();
		}

		return false;
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
		}
		if (tick - lastTick >= 250) {
			lastTick = tick;
			System.out.print('\b');
			animationPos = (animationPos + 1) % animation.length;
			System.out.print(animation[animationPos]);
		}
	}


	/** Handles an unix signal.  Pauses task and unsets \a running flag. */
	public void handleSignal() {
		running = false;
		if (task != null) {
			task.pause();
		}
	}

};
