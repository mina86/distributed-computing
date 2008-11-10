package com.mina86.dc.common;

import java.io.Serializable;
import java.io.PrintStream;

public interface Task extends Serializable {
	public void pause  ();
	public void unpause();
	public boolean run();


	public interface ProgressListener {
		void onProgress(Task task, long iterations, long end);
	};

	public void addProgressListener   (ProgressListener listener);
	public void removeProgressListener(ProgressListener listener);


	public boolean isVerifiable();
	public boolean verifyResult() throws UnsupportedOperationException;
}
