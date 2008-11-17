package com.mina86.util;

public final class RunRetry {
	public interface Job {
		public boolean run(boolean first) throws InterruptedException;
	};


	public static void run(Job job, long startDelay, long maxDelay)
		throws InterruptedException {
		long delay = startDelay;
		for (boolean ok = job.run(true); !ok; ok = job.run(false)) {
			System.out.println("Sleeping " + delay + " before retry.");
			Thread.sleep(delay * 1000);
			delay *= 2;
			if (delay > maxDelay) delay = maxDelay;
		}
	}


	public static void run(Job job, long startDelay)
		throws InterruptedException {
		run(job, startDelay, startDelay * 256);
	}


	public static void run(Job job) throws InterruptedException {
		run(job, 4, 1024);
	}
};
