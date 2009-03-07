/*
 * Copyright 2008-2009 by Michal Nazarewicz (mina86/AT/mina86.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 */

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
