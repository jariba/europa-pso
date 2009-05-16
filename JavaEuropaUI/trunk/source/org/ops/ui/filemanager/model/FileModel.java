package org.ops.ui.filemanager.model;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import psengine.PSEngine;

/**
 * List of files to be loaded into Europa. Gets a PSEngine as an argument and
 * reloads data on change.
 * 
 * @author Tatiana Kichkaylo
 */
public class FileModel implements Iterable<File> {
	/** List of files loaded into Europa */
	private ArrayList<File> files = new ArrayList<File>();

	/** Europa engine */
	private PSEngine engine;

	/** Model listeners */
	private ArrayList<FileModelListener> listeners = new ArrayList<FileModelListener>();

	public FileModel(PSEngine engine) {
		this.engine = engine;
	}

	public FileModel(PSEngine engine, Collection<File> files) {
		this.engine = engine;
		addFiles(files);
	}

	public FileModel(PSEngine engine, File[] files) {
		this.engine = engine;
		addFiles(files);
	}

	public void addFile(File file) {
		if (!files.contains(file)) {
			files.add(file);
			reload();
		}
	}

	public void addFiles(Collection<File> files) {
		boolean change = false;
		for (File f : files)
			if (!this.files.contains(f))
				change = this.files.add(f) || change;
		if (change)
			reload();
	}

	public void addFiles(File[] files) {
		boolean change = false;
		for (File f : files)
			if (!this.files.contains(f))
				change = this.files.add(f) || change;
		if (change)
			reload();
	}

	public void removeFile(File file) {
		if (files.remove(file))
			reload();
	}

	public void removeFiles(Collection<File> files) {
		if (this.files.removeAll(files))
			reload();
	}

	public void removeFiles(File[] files) {
		boolean change = false;
		for (File f : files)
			change = this.files.remove(f) || change;
		if (change)
			reload();
	}

	/**
	 * Reload the Europa database from the given files. Assume all files are
	 * NDDL for now
	 */
	public void reload() {
		System.out.println("Reloading " + files);
		this.engine.shutdown();
		this.engine.start();
		for (File f : files)
			try {
				// Call plain nddl, not AST, so that it loads
				this.engine.executeScript("nddl", new FileReader(f));
			} catch (Exception e) {
				System.err.println("Cannot load NDDL file? " + e);
			}
		for (FileModelListener lnr : this.listeners)
			lnr.databaseReloaded();
	}

	@Override
	public String toString() {
		return files.toString();
	}

	public File[] toArray() {
		File[] res = new File[files.size()];
		files.toArray(res);
		return res;
	}

	public void addListener(FileModelListener lnr) {
		this.listeners.add(lnr);
	}

	public void removeListener(FileModelListener lnr) {
		this.listeners.remove(lnr);
	}

	public Iterator<File> iterator() {
		return files.iterator();
	}

	/** AST parser does not actually load data into the database */
	public AstNode getAstTree(String fname) {
		try {
			String astString = this.engine.executeScript("nddl-ast", fname,
					true);
			// System.out.println(astString);
			AstNode root = new AstNode();
			int offset = root.readTreeFrom(astString, 0);
			// root.print(System.out, "");
			assert (offset == astString.length());
			return root;
		} catch (Exception e) {
			System.err.println("Cannot parse NDDL file? " + e);
			return null;
		}
	}
}
