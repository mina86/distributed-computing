package com.mina86.dc.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.NumberFormat;
import com.mina86.DC;
import com.mina86.dc.common.ServerInterface;
import com.mina86.dc.common.Task;
import com.mina86.dc.tasks.BogoSort;
import com.mina86.util.GetOptions;
import com.mina86.util.SignalHandlers;


/** A distributed computing server. */
public final class Server implements ServerInterface, DC.Application {
	/* A default task size if client requested size 0. */
	private int defaultSize = 8;

	public Task getTask(int n)
		throws RemoteException, NegativeArraySizeException {
		if (n < 0) {
			System.out.println("Negative task size requested.");
			throw new NegativeArraySizeException("negative task size requested (" + n + ")");
		}

		if (n == 0) {
			n = defaultSize;
		} else if (n == 1) {
			n = 2;
		}

		System.out.println("Generating and sending task (n = " + n + ").");

		Long data[] = new Long[n];
		for (int i = 0; i < n; ++i) {
			data[i] = new Long((long)(Math.random()*1000000000));
		}

		return new BogoSort<Long>(data);
	}

	public void sendResult(Task t) throws RemoteException {
		System.out.print("Got result: {");
		BogoSort<Long> task = (BogoSort<Long>)t;
		NumberFormat nf = NumberFormat.getIntegerInstance();
		String sep = " ";
		for (Long item : task) {
			System.out.print(sep); sep = ", ";
			System.out.print(nf.format(item));
		}
		System.out.println(" }");
	}


	/**
	 * A helper method which displays exception's name and exits
	 * application.
	 * \param e    exception that has been caught.
	 */
	private static void catchException(Exception e) {
		catchException(e, true);
	}

	/**
	 * A helper method which displays exception's name and exits
	 * application if \a exit is \c true.
	 * \param e    exception that has been caught.
	 * \param exit whether to exit after printing exception ingo.
	 */
	private static void catchException(Exception e, boolean exit) {
		System.out.print("failed.\n");
		e.printStackTrace();
		if (exit) {
			System.exit(1);
		}
	}


	/** The local registry to bind to. */
	private Registry registry = null;
	/** The name to bind to. */
	private String serviceName = null;
	/** Whether the service was bound. */
	private boolean serviceBound = false;

	public void run(String args[]) {
		int port = 0;

		try {
			GetOptions.VectorHandler vec = new GetOptions.VectorHandler(1);
			GetOptions.IntegerHandler portArg =
				new GetOptions.IntegerHandler(Registry.REGISTRY_PORT,
				                              1024, 0xffff);
			GetOptions.IntegerHandler sizeArg =
				new GetOptions.IntegerHandler(8, 2, 1024);
			GetOptions getopts = new GetOptions();
			getopts.addOption("-", vec, GetOptions.TakesArg.REQ);
			getopts.addOption("p", portArg, GetOptions.TakesArg.REQ);
			getopts.addOption("n", sizeArg, GetOptions.TakesArg.REQ);
			getopts.addAlias("port", "p");
			getopts.parseArguments(args, 1);
			serviceName = vec.get(0, DC.defaultServiceName);
			port = portArg.value;
			defaultSize = sizeArg.value;
		}
		catch (GetOptions.Exception e) {
			System.err.println(e.getFullMessage());
			System.exit(1);
		}

		ServerInterface server = this, stub = null;

		checkInterrupt();

		System.out.print("Getting registry... ");
		try { registry = LocateRegistry.getRegistry(port); }
		catch (RemoteException e) { catchException(e); }
		System.out.print("done.\n");

		checkInterrupt();

		System.out.print("Creating server stub... ");
		try { stub=(ServerInterface)UnicastRemoteObject.exportObject(server,0); }
		catch (RemoteException e) { catchException(e); }
		System.out.print("done.\n");

		synchronized (this) {
			checkInterrupt();

			System.out.print("Binding server (" + serviceName + ")... ");
			try { registry.rebind(serviceName, stub); }
			catch (RemoteException e) { catchException(e); }
			System.out.print("done.\n");
			serviceBound = true;
		}

		System.out.print("Server running.\n");
	}


	/** Whether there was a signal. */
	private volatile boolean interrupted = false;
	/** Checks whether there was a signal and exits with \c 0 exit code. */
	private void checkInterrupt() { checkInterrupt(0); }
	/**
	 * Checks whether there was a signal and exits with given exit code.
	 * \param exitCode exit code to exit with if program was interrupted
	 */
	private void checkInterrupt(int exitCode) {
		if (interrupted) {
			System.out.println("Interrupted, exiting.");
			System.exit(exitCode);
		}
	}


	public void handleSignal() {
		checkInterrupt(1);
		interrupted = true;

		synchronized (this) {
			if (!serviceBound) return;
		}

		System.out.print("Unbinding server... ");
		try { registry.unbind(serviceName); }
		catch (RemoteException e) { catchException(e); }
		catch (NotBoundException e) { }
		System.out.print("done.\n");
		System.exit(0);
	}

}
