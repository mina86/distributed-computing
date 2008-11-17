package com.mina86.dc.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.mina86.dc.common.Task;


final public class TaskLoader {
	static public String currentTaskName = "current-task";


	static public boolean savedTaskExists() {
		File file = new File(currentTaskName);
		return file.exists() && file.isFile();
	}


	static public Task loadTask() throws IOException, ClassNotFoundException {
		ObjectInputStream in =
			new ObjectInputStream(new FileInputStream(currentTaskName));
		Task t = (Task)in.readObject();
		in.close();
		return t;
	}


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


	static public void deleteTask() {
		(new File(currentTaskName)).delete();
	}
}
