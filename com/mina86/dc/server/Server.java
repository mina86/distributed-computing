package com.mina86.dc.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import com.mina86.DC;
import com.mina86.dc.common.ServerInterface;
import com.mina86.dc.common.Task;
import com.mina86.dc.tasks.BogoSort;
import com.mina86.util.GetOptions;
import com.mina86.util.SignalHandlers;


public final class Server
	implements ServerInterface, DC.Application {

	static String data[] = {
		"foo", "bar", "baz", "qux", "quux", "fred", "barney",
		"42", "9", "10"
	};

	public Task getTask() throws RemoteException {
		System.out.println("Sending task.");
		return new BogoSort<String>(data);
	}

	public void sendResult(Task t) throws RemoteException {
		System.out.print("Got result: { ");
		BogoSort<String> task = (BogoSort<String>)t;
		for (String item : task) {
			System.out.print(item);
			System.out.print(" ");
		}
		System.out.println("}");
	}


	public static void catchException(Exception e) {
		catchException(e, true);
	}
	public static void catchException(Exception e, boolean exit) {
		System.out.print("failed.\n");
		e.printStackTrace();
		if (exit) {
			System.exit(1);
		}
	}


	private Registry registry = null;
	private String serviceName = null;
	private boolean serviceBound = false;

	public void run(String args[]) {
		int port = 0;

		try {
			GetOptions.VectorHandler vec = new GetOptions.VectorHandler(1);
			GetOptions.IntegerHandler portArg =
				new GetOptions.IntegerHandler(Registry.REGISTRY_PORT,
				                              1024, 0xffff);
			GetOptions getopts = new GetOptions();
			getopts.addOption("-", vec, GetOptions.TakesOption.REQ);
			getopts.addOption("p", portArg, GetOptions.TakesOption.REQ);
			getopts.addAlias("port", "p");
			getopts.parseArguments(args, 1);
			serviceName = vec.get(0, DC.defaultServiceName);
			port = portArg.value;
		}
		catch (GetOptions.Exception e) {
			System.err.println(e.getArgumentName() + ": " + e.getMessage());
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


	private volatile boolean interrupted = false;
	private void checkInterrupt() { checkInterrupt(0); }
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
