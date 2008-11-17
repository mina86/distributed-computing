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


public final class Client
	implements Task.ProgressListener, DC.Application {


	private volatile boolean running = true;

	public void run(String args[]) {
		try {
			GetOptions.VectorHandler vec = new GetOptions.VectorHandler(1);
			GetOptions getopts = new GetOptions();
			getopts.addOption("-", vec, GetOptions.TakesOption.REQ);
			getopts.parseArguments(args, 1);

			serviceName = vec.get(0, DC.defaultServiceName);
			rmiURL = vec.get(1, DC.defaultRegistryURL);
		}
		catch (GetOptions.Exception e) {
			System.err.println(e.getArgumentName() + ": " + e.getMessage());
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


	private String rmiURL, serviceName;
	private ServerInterface server = null;
	private Task task = null;


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



	private static char animation[] = { '.', 'o', 'O', '0', 'O', 'o' };
	private int animationPos = -1;
	private long lastTick = 0, lastSave = 0;

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


	public void handleSignal() {
		running = false;
		if (task != null) {
			task.pause();
		}
	}

};
