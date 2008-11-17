package com.mina86;

import java.io.PrintStream;
import com.mina86.dc.client.Client;
import com.mina86.dc.server.Server;
import com.mina86.util.SignalHandlers;


public class DC {

	public interface Application
		extends com.mina86.util.SignalHandlers.Listener {
		public void run(String args[]);
	}


	public static final String defaultServiceName = "mina86-dc-server";
	public static final String defaultRegistryURL = "";


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


	private static String usageLines[] =  {
		"usage: java com.mina86.dc <application> [ <options> ]",
		"<application>:  server  -- start DC server",
		"                client  -- start DC client"
	};

	private static void usage(PrintStream o, int exitStatus) {
		for (String line : usageLines) {
			o.println(line);
		}
		System.exit(exitStatus);
	}
}
