package com.mina86;

import java.io.PrintStream;
import com.mina86.dc.client.Client;
import com.mina86.dc.server.Server;
import com.mina86.util.SignalHandlers;


/**
 * Starter of com.mina86.dc.client.Client and
 * com.mina86.dc.server.Server classes.  It contains some common code
 * base which otherwise would have to be included in both client and
 * server but mostly it allows easier execution of each application as
 * you type <tt>java com.mina86.DC server</tt> instead of <tt>java
 * com.mina86.dc.server.Server</tt>.
 */
public class DC {
	/** Interface each runable application must implement. */
	public interface Application
		extends com.mina86.util.SignalHandlers.Listener {
		/**
		 * Called when given application is run.  The first argumnt is
		 * always name given when launching the application.
		 * \param args list of positional arguments.
		 */
		public void run(String args[]);
	}


	/** A default service name for the server. */
	public static final String defaultServiceName = "mina86-dc-server";
	/** A default RMI registry URL for client. */
	public static final String defaultRegistryURL = "";


	/**
	 * Executed when program stats.  It checks the first argument and
	 * launches aproprit application.  Before doing so though it
	 * registers the application as an listener of
	 * com.mina86.util.SignalHandlers so it is notified when
	 * terminating signal is delivered.
	 *
	 * \param args list of arguments.
	 */
	public static void main(String args[]) {
		if (args.length == 0) {
			usage(System.out, 0);
		}

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		Application app = null;

		if (args[0].equals("server")) {
			app = new Server();
		} else if (args[0].equals("client")) {
			app = new Client();
		} else {
			usage(System.err, 1);
		}

		System.out.println("Starting " + args[0] + ".");
		SignalHandlers.get().addListener(app);
		app.run(args);
	}


	/** An usage screen. */
	private static String usageLines[] =  {
		"usage: java com.mina86.dc <application> [ <options> ]",
		"<application>:  server  -- start DC server",
		"                client  -- start DC client"
	};

	/**
	 * Prings usage information.
	 * \param o stream to print to.
	 * \param exitStatus status to exit with.
	 */
	private static void usage(PrintStream o, int exitStatus) {
		for (String line : usageLines) {
			o.println(line);
		}
		System.exit(exitStatus);
	}
}
