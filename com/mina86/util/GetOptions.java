package com.mina86.util;

import java.util.Hashtable;
import java.util.Vector;


public class GetOptions {


	public abstract static class Exception extends java.lang.Exception {
		protected Exception(String argName, String theMessage) {
			name = argName.equals("-") ? "positional argument" : argName;
			message = theMessage;
		}
		private String name, message;
		public String getArgumentName() { return name; }
		public String getMessage() { return message; }
	}

	public final static class NoSuchOption extends Exception {
		public NoSuchOption(String argName) {
			super(argName, "no such option");
		}
	}

	public final static class NoArgExpected extends Exception {
		public NoArgExpected(String argName) {
			super(argName, "no argument expected");
		}
	}

	public final static class ArgExpected extends Exception {
		public ArgExpected(String argName) {
			super(argName, "argument expected");
		}
	}

	public final static class InvalidValue extends Exception {
		public InvalidValue(String argName, String theValue, String message) {
			super(argName, message);
			value = theValue;
		}

		public String getRawMessage() { return super.getMessage(); }
		public String getMessage() { return getValue() + ": " + getRawMessage();}
		public String getValue() { return value; }

		private String value;
	}

	public final static class TooManyArguments extends Exception {
		public TooManyArguments(String argName) {
			super(argName, "too many arguments");
		}
	}



	public enum TakesOption { NO, OPT, REQ };

	public interface Handler {
		public void handleOption(String name, String argument) throws Exception;
	}

	public final static class FlagHandler implements Handler {
		public boolean value = false;

		public FlagHandler() { this(false); }
		public FlagHandler(boolean theValue) { value = theValue; }

		public void handleOption(String name, String argument) throws Exception {
			if (argument != null) throw new NoArgExpected(name);
			value = !value;
		}
	}

	public final static class StringHandler implements Handler {
		public String value;

		public StringHandler() { this(null); }
		public StringHandler(String theValue) { value = theValue; }

		public void handleOption(String name, String argument) throws Exception {
			value = argument;
		}
	}

	public final static class VectorHandler implements Handler {
		private int max;
		public Vector<String> vector = new Vector<String>();

		public VectorHandler() { this(Integer.MAX_VALUE); }
		public VectorHandler(int theMax) { max = theMax; }

		public void handleOption(String name, String argument) throws Exception {
			if (argument == null) throw new ArgExpected(name);
			if (vector.size() == max) throw new TooManyArguments(name);
			vector.add(argument);
		}

		public String get(int n, String theDefault) {
			return n >= vector.size() ? theDefault : vector.get(n);
		}
	}

	public final static class IntegerHandler implements Handler {
		private int min, max;
		public int value;

		public IntegerHandler() {
			this(0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		}
		public IntegerHandler(int theValue) {
			this(theValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
		}
		public IntegerHandler(int theValue, int theMax) {
			this(theValue, Integer.MIN_VALUE, theMax);
		}
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




	private final static class Option {
		private Handler handler;
		private TakesOption takesOption;
		private String defaultArgument;

		Option(Handler theHandler, TakesOption theTakesOption,
		       String theDefault) {
			handler = theHandler;
			takesOption = theTakesOption;
			defaultArgument = theDefault;
		}

		Option(Handler theHandler, TakesOption theTakesOption) {
			handler = theHandler;
			takesOption = theTakesOption;
			defaultArgument = null;
		}

		public TakesOption takesOption() {
			return takesOption;
		}

		public void handleOption(String name) throws Exception {
			handleOption(name, defaultArgument);
		}

		public void handleOption(String name, String argument) throws Exception {
			handler.handleOption(name, argument);
		}
	}


	private Hashtable<String, Option> options = new Hashtable<String, Option>();


	public final void addOption(String name, Handler handler, TakesOption opt) {
		addOption(name, handler, opt, null);
	}

	public final void addOption(char name, Handler handler, TakesOption opt) {
		addOption("" +name, handler, opt, null);
	}

	public void addOption(String name, Handler handler, TakesOption opt,
	                      String def) {
		options.put(name, new Option(handler, opt, def));
	}

	public final void addOption(char name, Handler handler, TakesOption opt,
	                            String def) {
		addOption("" + name, handler, opt, def);
	}


	public final void addAlias(char newName, char oldName) throws NoSuchOption {
		addAlias("" + newName, "" + oldName);
	}

	public final void addAlias(String newName, char oldName)
		throws NoSuchOption {
		addAlias(newName, "" + oldName);
	}

	public final void addAlias(char newName, String oldName)
		throws NoSuchOption {
		addAlias("" + newName, oldName);
	}

	public void addAlias(String newName, String oldName) throws NoSuchOption {
		Option opt = options.get(oldName);
		if (opt == null) {
			throw new NoSuchOption(oldName);
		}
		options.put(newName, opt);
	}



	public final void parseArguments(String args[]) throws Exception {
		parseArguments(args, 0, args.length);
	}

	public final void parseArguments(String args[], int start) throws Exception {
		parseArguments(args, start, args.length);
	}

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


	private void handleOption(String name, String arg) throws Exception {
		Option opt = options.get(name);
		if (opt == null) {
			throw new NoSuchOption(name);
		} else if (opt.takesOption() == TakesOption.NO) {
			throw new NoArgExpected(name);
		} else {
			opt.handleOption(name, arg);
		}
	}

	private void handleOption(String name) throws Exception {
		Option opt = options.get(name);
		if (opt == null) {
			throw new NoSuchOption(name);
		} else if (opt.takesOption() == TakesOption.REQ) {
			throw new ArgExpected(name);
		} else {
			opt.handleOption(name);
		}
	}


	private int handleShortArgument(String args[], int pos, int end)
		throws Exception {
		String arg = args[pos];
		int len = arg.length();

		for (int i = 1; i < len; ++i) {
			String name = arg.substring(i, i+1);
			Option opt = options.get(name);

			if (opt == null) {
				throw new NoSuchOption(name);
			} else if (i + 1 != len && opt.takesOption() != TakesOption.NO) {
				handleOption(name, arg.substring(i+1));
				return pos;
			} else if (opt.takesOption() != TakesOption.REQ) {
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
