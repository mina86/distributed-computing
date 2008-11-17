package com.mina86.util;

import java.util.Hashtable;
import java.util.Vector;


/**
 * A class for parsing command line arguments.  The usage is as
 * follows:
 *
 * <pre>
 * GetOptions getopts = new GetOptions();
 * GetOptions.StringHandler fooArg = new GetOptionsStringHandler("default");
 * getopts.addOption("foo", fooArg, GetOptions.TakesOption.REQ);
 * getopts.addAlias("f", "foo");
 * getopts.addOption("bar", fooArg, GetOptions.TakesOption.NO, "bar");
 * try { getopts.parseArgs(args); }
 * catch (GetOptions.Exception e) { ... errors ... }
 * String foo = fooArg.value;
 * </pre>
 *
 * You can of course write your own handlers which is especially
 * useful when dealing with some class methods -- you can then use
 * anonymouse classes, like so:
 *
 * <pre>
 * getopts.addOption("baz", new GetOptions.Handler() {
 *     public void handleOption(String name, String arg) {
 *         classField = arg;
 *     }
 * }, GetOptions.TakesOption.NO, "qux");
 * </pre>
 */
public class GetOptions {

	/**
	 * Base class for exception thrown by parseArgs() method as well
	 * as Handler's methods.
	 */
	public abstract static class Exception extends java.lang.Exception {
		/**
		 * Constructs the exception.
		 * \param optName    option name that caused problem.
		 * \param theMessage error message.
		 */
		protected Exception(String optName, String theMessage) {
			name = optName.equals("-") ? "positional argument" : optName;
			message = theMessage;
		}

		/** Option name that caused problem. */
		private String name;
		/** Returns option name that caused problem. */
		public final String getOptionName() { return name; }

		/** Error message. */
		private String message;
		/** Returns error message. */
		public String getMessage() { return message; }

		/** Returns error message prefixed with option name. */
		public final String getFullMessage() {
			return name + ": " + getMessage();
		}
	}

	/**
	 * Thrown by parseArgs() method if an option which does not exist
	 * was given.
	 */
	public final static class NoSuchOption extends Exception {
		/**
		 * Constructs the exception.
		 * \param optName    option name that caused problem.
		 */
		public NoSuchOption(String optName) {
			super(optName, "no such option");
		}
	}

	/** Thrown when an option that does not take argument was given one. */
	public final static class NoArgExpected extends Exception {
		/**
		 * Constructs the exception.
		 * \param optName    option name that caused problem.
		 */
		public NoArgExpected(String optName) {
			super(optName, "no argument expected");
		}
	}

	/** Thrown when an option that takes argument was not given one. */
	public final static class ArgExpected extends Exception {
		/**
		 * Constructs the exception.
		 * \param optName    option name that caused problem.
		 */
		public ArgExpected(String optName) {
			super(optName, "argument expected");
		}
	}

	/** Thrown when an invalid value was given as argument for an option. */
	public final static class InvalidValue extends Exception {
		/**
		 * Constructs the exception.
		 * \param optName    option name that caused problem.
		 * \param theValue   the value given as an argument for an option.
		 * \param theMessage error message.
		 */
		public InvalidValue(String optName, String theValue, String message) {
			super(optName, message);
			value = theValue;
		}

		/** Returns error message. */
		public String getRawMessage() { return super.getMessage(); }

		/** Returns error message prefixed with value causing problems. */
		public String getMessage() { return getValue() + ": " + getRawMessage();}

		/** Returns value given as an argument for an option. */
		public String getValue() { return value; }

		/** Value given as an argument for an option. */
		private String value;
	}

	/** Thrown when an option was given too many times. */
	public final static class TooManyArguments extends Exception {
		/**
		 * Constructs the exception.
		 * \param optName    option name that caused problem.
		 */
		public TooManyArguments(String optName) {
			super(optName, "too many arguments");
		}
	}




	/** Specifies whether given option takes arguments. */
	public enum TakesArg {
		NO,   /**< Option does not take argument. */
		OPT,  /**< Option takes optional argument. */
		REQ   /**< Option requires an argument. */
	};


	/**
	 * An interface for classes handling options encountered while
	 * processing command line arguments.
	 */
	public interface Handler {
		/**
		 * Called by parseArgs() when an option named \a name is
		 * encountered.
		 *
		 * If argument has been given \a argument will hold it's
		 * value.  Otherwise, if option was configured with a default
		 * argument \a argument will hold the default value.
		 * Otherwise \a argumetn will be \c null.  There is no way for
		 * handler to tell whether the argument has been given
		 * explicitly or was a default argument.
		 *
		 * \param name     name of the option.
		 * \param argument value of an argument.
		 * \throw Exception in case of any errors.
		 */
		public void handleOption(String name, String argument) throws Exception;
	}

	/** A handler which toggles its boolean value each time it is called. */
	public final static class FlagHandler implements Handler {
		/** The value of the flag. */
		public boolean value = false;

		/** Default constructor. */
		public FlagHandler() { this(false); }
		/** Constructors handler.  \param theValue initial value. */
		public FlagHandler(boolean theValue) { value = theValue; }

		public void handleOption(String name, String argument) throws Exception {
			if (argument != null) throw new NoArgExpected(name);
			value = !value;
		}
	}

	/** A handler which stores its argument. */
	public final static class StringHandler implements Handler {
		/** The value. */
		public String value;

		/** Default constructor. */
		public StringHandler() { this(null); }
		/** Constructors handler.  \param theValue initial value. */
		public StringHandler(String theValue) { value = theValue; }

		public void handleOption(String name, String argument) throws Exception {
			value = argument;
		}
	}

	/** A handler which stores a vector of arguments.  */
	public final static class VectorHandler implements Handler {
		/** The maximal number of arguments to accept. */
		private int max;
		/** The vector of values. */
		public Vector<String> vector = new Vector<String>();

		/** Default constructor. */
		public VectorHandler() { this(Integer.MAX_VALUE); }
		/** Constructors handler.  \param theMax maximal number of arguments. */
		public VectorHandler(int theMax) { max = theMax; }

		public void handleOption(String name, String argument) throws Exception {
			if (argument == null) throw new ArgExpected(name);
			if (vector.size() == max) throw new TooManyArguments(name);
			vector.add(argument);
		}

		/**
		 * Returns value at index \a n or \a theDefault.
		 * \param n index in a vector.
		 * \param theDefault default value if vector is shorter then \a n.
		 */
		public String get(int n, String theDefault) {
			return n >= vector.size() ? theDefault : vector.get(n);
		}
	}

	/** A handler which stores its argument parsing it as an integer. */
	public final static class IntegerHandler implements Handler {
		/** Minimal possible value. */
		private int min;
		/** Maximal possible value. */
		private int max;
		/** The value. */
		public int value;

		/** Default constructor. */
		public IntegerHandler() {
			this(0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		}
		/** Constructors handler.  \param theValue initial value. */
		public IntegerHandler(int theValue) {
			this(theValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
		}
		/**
		 * Constructors handler.
		 * \param theValue initial value.
		 * \param theMax   maximal possible value.
		 */
		public IntegerHandler(int theValue, int theMax) {
			this(theValue, Integer.MIN_VALUE, theMax);
		}
		/**
		 * Constructors handler.
		 * \param theValue initial value.
		 * \param theMin   minimal possible value.
		 * \param theMax   maximal possible value.
		 */
		public IntegerHandler(int theValue, int theMin, int theMax) {
			value = theValue;
			min = theMin;
			max = theMax;
		}

		public void handleOption(String name, String argument) throws Exception {
			if (argument == null) throw new ArgExpected(name);
			int val = 0;
			boolean err = false;

			try { val = Integer.parseInt(argument); }
			catch (NumberFormatException e) { err = true; }
			if (err || val < min || val > max) {
				throw new InvalidValue(name, argument, "integer from <" + min +
				                       ", " + max + "> expected");
			}

			value = val;
		}
	}




	/** Describes a single option. */
	private final static class Option {
		/** Handler handling the option. */
		private Handler handler;
		/** Whether option takes argument. */
		private TakesArg takesArg;
		/** Default argument. */
		private String defaultArgument;

		/**
		 * Constructs object.
		 * \param theHandler      handler handling the option.
		 * \param theTakesOption  whether option takes argument.
		 * \param theDefault      default argument.
		 */
		public Option(Handler theHandler, TakesArg theTakesArg,
		              String theDefault) {
			handler = theHandler;
			takesArg = theTakesArg;
			defaultArgument = theDefault;
		}

		/**
		 * Constructs object.
		 * \param theHandler      handler handling the option.
		 * \param theTakesOption  whether option takes argument.
		 */
		public Option(Handler theHandler, TakesArg theTakesArg) {
			handler = theHandler;
			takesArg = theTakesArg;
			defaultArgument = null;
		}

		/** Returns whether option takes arguments. */
		public TakesArg takesArguments() {
			return takesArg;
		}

		/**
		 * Handles an option with no argument.
		 * \param name option name.
		 * \throw Exception if handler decides to throw an exception.
		 */
		public void handleOption(String name) throws Exception {
			handleOption(name, defaultArgument);
		}

		/**
		 * Handles an option with an argument.
		 * \param name     option name.
		 * \param argument the argument.
		 * \throw Exception if handler decides to throw an exception.
		 */
		public void handleOption(String name, String argument) throws Exception {
			handler.handleOption(name, argument);
		}
	}


	/** A map of option descriptions. */
	private Hashtable<String, Option> options = new Hashtable<String, Option>();


	/**
	 * Adds an option named \a name.  If \a name is one character long
	 * it is considered a short option (ie. it may start with single
	 * dash) otherwise it is a long option (and must start with two
	 * dashes).
	 *
	 * \param name      option name.
	 * \param handler   option handler.
	 * \param arg       whether option takes arguments.
	 */
	public final void addOption(String name, Handler handler, TakesArg arg) {
		addOption(name, handler, arg, null);
	}

	/**
	 * Adds a short option named \a name.  A short option may start
	 * with single dash.
	 *
	 * \param name      option name.
	 * \param handler   option handler.
	 * \param arg       whether option takes arguments.
	 */
	public final void addOption(char name, Handler handler, TakesArg arg) {
		addOption("" +name, handler, arg, null);
	}

	/**
	 * Adds an option named \a name.  If \a name is one character long
	 * it is considered a short option (ie. it may start with single
	 * dash) otherwise it is a long option (and must start with two
	 * dashes).
	 *
	 * \param name      option name.
	 * \param handler   option handler.
	 * \param arg       whether option takes arguments.
	 * \param def       default argument.
	 */
	public void addOption(String name, Handler handler, TakesArg arg,
	                      String def) {
		options.put(name, new Option(handler, arg, def));
	}

	/**
	 * Adds a short option named \a name.  A short option may start
	 * with single dash.
	 *
	 * \param name      option name.
	 * \param handler   option handler.
	 * \param arg       whether option takes arguments.
	 * \param def       default argument.
	 */
	public final void addOption(char name, Handler handler, TakesArg arg,
	                            String def) {
		addOption("" + name, handler, arg, def);
	}


	/**
	 * Adds an alias for an option.  In other words copies entry of
	 * one option under another name.
	 * \param newName the name of an alias.
	 * \param oldName name of exiting option.
	 * \throw NoSuchOption if option named \a oldName does not exist.
	 */
	public final void addAlias(char newName, char oldName) throws NoSuchOption {
		addAlias("" + newName, "" + oldName);
	}

	/**
	 * Adds an alias for an option.  In other words copies entry of
	 * one option under another name.
	 * \param newName the name of an alias.
	 * \param oldName name of exiting option.
	 * \throw NoSuchOption if option named \a oldName does not exist.
	 */
	public final void addAlias(String newName, char oldName)
		throws NoSuchOption {
		addAlias(newName, "" + oldName);
	}

	/**
	 * Adds an alias for an option.  In other words copies entry of
	 * one option under another name.
	 * \param newName the name of an alias.
	 * \param oldName name of exiting option.
	 * \throw NoSuchOption if option named \a oldName does not exist.
	 */
	public final void addAlias(char newName, String oldName)
		throws NoSuchOption {
		addAlias("" + newName, oldName);
	}

	/**
	 * Adds an alias for an option.  In other words copies entry of
	 * one option under another name.
	 * \param newName the name of an alias.
	 * \param oldName name of exiting option.
	 * \throw NoSuchOption if option named \a oldName does not exist.
	 */
	public void addAlias(String newName, String oldName) throws NoSuchOption {
		Option opt = options.get(oldName);
		if (opt == null) {
			throw new NoSuchOption(oldName);
		}
		options.put(newName, opt);
	}



	/**
	 * Parses all arguments.
	 * \param args the arguments.
	 */
	public final void parseArguments(String args[]) throws Exception {
		parseArguments(args, 0, args.length);
	}

	/**
	 * Parses arguments starting from \a start.
	 * \param args  the arguments.
	 * \param start index to start at.
	 */
	public final void parseArguments(String args[], int start) throws Exception {
		parseArguments(args, start, args.length);
	}

	/**
	 * Parses arguments starting from \a start to (excluding) \a end.
	 * \param args  the arguments.
	 * \param start index to start at.
	 * \param end   index to end   at.
	 */
	public void parseArguments(String args[], int start, int end)
		throws Exception {
		boolean dashdash = false;

		for (; start < end; ++start) {
			String arg = args[start];
			if (dashdash) {
				handleOption("-", arg);
			} else if (arg.equals("-")) {
				handleOption("-", "-");
			} else if (arg.equals("--")) {
				dashdash = true;
			} else if (arg.startsWith("--")) {
				int index = arg.indexOf('=');
				if (index == -1) {
					handleOption(arg.substring(2));
				} else {
					handleOption(arg.substring(2, index),
					             arg.substring(index + 1));
				}
			} else if (arg.startsWith("-")) {
				start = handleShortArgument(args, start, end);
			} else {
				handleOption("-", arg);
			}
		}
	}


	/**
	 * Handles a single option with an argument.
	 * \param name option name.
	 * \param arg  argument.
	 * \throw NoSuchOption  if option does not exist.
	 * \throw NoArgExpected if option does not take argument.
	 * \throw Exception     if handler decides to throw an exception.
	 */
	private void handleOption(String name, String arg) throws Exception {
		Option opt = options.get(name);
		if (opt == null) {
			throw new NoSuchOption(name);
		} else if (opt.takesArguments() == TakesArg.NO) {
			throw new NoArgExpected(name);
		} else {
			opt.handleOption(name, arg);
		}
	}

	/**
	 * Handles a single option with no argument.
	 * \param name option name.
	 * \throw NoSuchOption  if option does not exist.
	 * \throw ArgExpected   if option requires an argument.
	 * \throw Exception     if handler decides to throw an exception.
	 */
	private void handleOption(String name) throws Exception {
		Option opt = options.get(name);
		if (opt == null) {
			throw new NoSuchOption(name);
		} else if (opt.takesArguments() == TakesArg.REQ) {
			throw new ArgExpected(name);
		} else {
			opt.handleOption(name);
		}
	}


	/**
	 * Handles a single or set of short options.
	 * \param args the arguments.
	 * \param pos  position we are at.
	 * \param end  index to end at.
	 * \returns index of an argument "consumed" by the method.
	 * \throw NoSuchOption  if encountered an option does not exist.
	 * \throw ArgExpected   if encountered an option that requires an
	 *                      argument but was not given one.
	 * \throw Exception     if one of the handlers decided to throw an
	 *                      exception.
	 */
	private int handleShortArgument(String args[], int pos, int end)
		throws Exception {
		String arg = args[pos];
		int len = arg.length();

		for (int i = 1; i < len; ++i) {
			String name = arg.substring(i, i+1);
			Option opt = options.get(name);

			if (opt == null) {
				throw new NoSuchOption(name);
			} else if (i + 1 != len && opt.takesArguments() != TakesArg.NO) {
				handleOption(name, arg.substring(i+1));
				return pos;
			} else if (opt.takesArguments() != TakesArg.REQ) {
				handleOption(name);
			} else if (pos + 1 == end) {
				throw new ArgExpected(name);
			} else {
				handleOption(name, args[pos+1]);
				return pos + 1;
			}
		}

		return pos;
	}
}
