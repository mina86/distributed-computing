package com.mina86.dc.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.mina86.dc.common.Task;


/** Defines methods for accessing task from file system. */
final public class TaskLoader {
	/** File name to save task under. */
	static public String currentTaskName = "current-task";


	/** Checks whether a saved task exists. */
	static public boolean savedTaskExists() {
		File file = new File(currentTaskName);
		return file.exists() && file.isFile();
	}


	/** Loads a saved task. */
	static public Task loadTask() throws IOException, ClassNotFoundException {
		ObjectInputStream in =
			new ObjectInputStream(new FileInputStream(currentTaskName));
		Task t = (Task)in.readObject();
		in.close();
		return t;
	}


	/**
	 * Saves given task.
	 * \param t task to save.
	 */
	static public void saveTask(Task t) throws IOException {
		File dir = new File(System.getProperty("user.dir"));
		File tmp = File.createTempFile("task", ".tmp", dir);

		ObjectOutputStream out =
			new ObjectOutputStream(new FileOutputStream(tmp.getPath()));
		out.writeObject(t);
		out.close();

		File file = new File(currentTaskName);
		if (!tmp.renameTo(file) && file.delete()) {
			tmp.renameTo(file);
		}
	}


	/** Deletes saved task (if any). */
	static public void deleteTask() {
		(new File(currentTaskName)).delete();
	}
}
