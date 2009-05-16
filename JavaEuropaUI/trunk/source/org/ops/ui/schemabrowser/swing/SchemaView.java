package org.ops.ui.schemabrowser.swing;

import javax.swing.JScrollPane;
import javax.swing.JTree;

import org.ops.ui.filemanager.model.FileModel;
import org.ops.ui.filemanager.model.FileModelListener;
import org.ops.ui.main.swing.EuropaInternalFrame;
import org.ops.ui.schemabrowser.model.SchemaSource;

import psengine.PSEngine;

public class SchemaView extends EuropaInternalFrame implements
		FileModelListener {

	private JTree tree;
	private SchemaTreeModel treeModel;

	public SchemaView(PSEngine engine, FileModel fileModel) {
		super("Schema browser");

		// Data
		this.treeModel = new SchemaTreeModel(new SchemaSource(engine));
		fileModel.addListener(this);

		// Widgets
		this.tree = new JTree(treeModel);
		this.add(new JScrollPane(tree));
		this.tree.setRootVisible(false);
	}

	public void databaseReloaded() {
		treeModel.reloadFromSchema();
	}
}
