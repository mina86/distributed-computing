package com.mina86.dc.tests;

import java.util.Vector;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import com.mina86.dc.common.Task;
import com.mina86.dc.tasks.BogoSort;
import com.mina86.util.SignalHandlers;


final public class BogoSortTest
	implements Task.ProgressListener, SignalHandlers.Listener {

	public static void main(String [] args) {
		(new BogoSortTest()).run();
	}


	private void run() {
		System.out.print("Bogo sort testing program\nPlease enter lines to sort and finish with an EOF character:\n");


		/* Read data */
		Vector<String> vector = new Vector<String>();
		BufferedReader reader =
			new BufferedReader(new InputStreamReader(System.in));

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				vector.addElement(line);
			}
		}
		catch (IOException e) {
			/* ignore */
		}


		/* Initialise sorting */
		System.out.print("\nSorting...  ");
		BogoSort<String> bogoSortTask =
			new BogoSort<String>(vector.iterator(), vector.size());
		task = bogoSortTask;
		onProgress(task, -1, 0);
		task.addProgressListener(this);
		task.unpause();


		/* Install signal handler */
		SignalHandlers.get().addListener(this);


		/* Run */
		boolean done = task.run();


		/* Print output */
		if (done) {
			System.out.print("\bdone.\nSorted:\n");
			for (String line : bogoSortTask) System.out.println(line);
		} else {
			System.out.print("\nInterrupted.\n");
		}
	}



	private static char animation[] = { '.', 'o', 'O', '0', 'O', 'o' };
	int animationPos = -1;
	long lastTick = 0;

	public void onProgress(Task task, long iterations, long end) {
		long tick = (new Date()).getTime();
		if (tick - lastTick >= 250) {
			lastTick = tick;
			System.out.print('\b');
			animationPos = (animationPos + 1) % animation.length;
			System.out.print(animation[animationPos]);
		}
	}



	Task task;

	public void handleSignal() {
		task.pause();
	}
}
