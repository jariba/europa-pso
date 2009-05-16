package org.ops.ui.schemabrowser.swing;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.ops.ui.schemabrowser.model.SchemaNode;
import org.ops.ui.schemabrowser.model.SchemaSource;

/**
 * Convert schema nodes into Swing tree model
 * 
 * @author Tatiana Kichkaylo
 */
public class SchemaTreeModel extends DefaultTreeModel {
	private SchemaSource model;

	public SchemaTreeModel(SchemaSource model) {
		super(new DefaultMutableTreeNode("Root"));
		this.model = model;
		reloadFromSchema();
	}

	public void reloadFromSchema() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.getRoot();
		root.removeAllChildren();

		root.add(convertNode(model.getPredicatesNode()));
		this.nodeStructureChanged(root);
	}

	private MutableTreeNode convertNode(SchemaNode snode) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(snode
				.getText());
		for (SchemaNode child : snode.getChildren())
			node.add(convertNode(child));
		return node;
	}
}
