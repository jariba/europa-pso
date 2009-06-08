package org.ops.ui.filemanager.model;

import java.io.File;

import psengine.PSEngine;

/**
 * File model gets AST from a file. To do so, it creates a brand new copy of
 * engine, and deletes it when it is done. Loading of models into an engine for
 * running is done in SolverModel.
 * 
 * @author Tatiana Kichkaylo
 */
public abstract class FileModel {
	/** Property name in the engine config: list of search paths for includes */
	private static final String INCLUDE_PATH = "nddl.includePath";

	/** Make this file purely abstract */
	private FileModel() {
	}

	/** AST parser does not actually load data into the database */
	public static AstNode getAstTree(String fname) {
		PSEngine engine = PSEngine.makeInstance();
		engine.start();
		String oldPath = engine.getConfig().getProperty(INCLUDE_PATH);
		AstNode root = new AstNode();
		try {
			File file = new File(fname);
			if (!file.exists()) {
				System.err.println("Cannot open non-existing file " + file);
				return null;
			}
			fname = file.getAbsolutePath();
			String newPath = file.getParent();
			if (oldPath != null)
				newPath = newPath + ":" + oldPath;
			engine.getConfig().setProperty(INCLUDE_PATH, newPath);
			String astString = engine.executeScript("nddl-ast", fname, true);
			// System.out.println(astString);
			int offset = root.readTreeFrom(astString, 0);
			// root.print(System.out, "");
			assert (offset == astString.length());

			return root;
		} catch (Exception e) {
			System.err.println("Cannot parse NDDL file? " + e);
			root = null;
		}
		engine.shutdown();
		engine.delete();
		return root;
	}
}
