/** Implementation of distributed computing client program. */
package com.mina86.dc.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
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
			getopts.addOption("-", vec, GetOptions.TakesArg.REQ);
			getopts.parseArguments(args, 1);

			serviceName = vec.get(0, DC.defaultServiceName);
			rmiURL = vec.get(1, DC.defaultRegistryURL);
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
				System.out.print("done.\n");
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
			while ((task = server.getTask()) == null) {
				System.out.println("no task.");
				Thread.sleep(15);
				System.out.print("Downloading task... ");
			}
			System.out.print("done.\n");

			return true;
		}
		catch (Exception e) {
			System.out.println("failed.\n" + e.toString());
			server = null;
		}
		return false;
	}


	/** Starts calculating task. */
	private boolean runTask() {
		System.out.print("Calculating...  ");
		lastTick = lastSave = 0;
		onProgress(task, 0, 0);
		task.addProgressListener(this);
		task.unpause();

		boolean done = running && task.run();

		System.out.println(done ? "\bdone." : "\nInterrupted.");
		return done;
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
	private lastSave = 0;


	/**
	 * Called each time task finishes single cycle.  This method saves
	 * task on disk every five seconds and updates animation four
	 * times per second.
	 * \param task       task being calculated.
	 * \param iterations how many iterations there wer (ignored).
	 * \param end        how many iterations are needed (ignored).
	 */
	public void onProgress(Task task, long iterations, long end) {
		long tick = (new Date()).getTime();
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


	/** Handles an unix signal.  Pauses task and unsets \a running flag. */
	public void handleSignal() {
		running = false;
		if (task != null) {
			task.pause();
		}
	}

};
