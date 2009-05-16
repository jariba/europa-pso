package org.ops.ui.editor.swt;

import java.util.HashSet;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.ops.ui.filemanager.model.AstNode;
import org.ops.ui.filemanager.model.AstNodeTypes;
import org.ops.ui.filemanager.model.FileModel;
import org.ops.ui.main.swt.EuropaPlugin;

public class NddlContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	private HashSet<Integer> leafTypes = new HashSet<Integer>();

	private NddlOutlinePage outlinePage;

	public NddlContentProvider(NddlOutlinePage nddlOutlinePage) {
		this.outlinePage = nddlOutlinePage;

		leafTypes.add(AstNodeTypes.ENUM_KEYWORD);
		leafTypes.add(AstNodeTypes.DCOLON); // operator definition
		leafTypes.add(AstNodeTypes.VARIABLE);
		leafTypes.add(AstNodeTypes.CONSTRUCTOR);
		leafTypes.add(AstNodeTypes.PREDICATE_KEYWORD);
		leafTypes.add(AstNodeTypes.GOAL_KEYWORD);
		leafTypes.add(AstNodeTypes.CONSTRAINT_INSTANTIATION);
		leafTypes.add(AstNodeTypes.ACTIVATE_KEYWORD);
		leafTypes.add(AstNodeTypes.FACT_KEYWORD);
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}

	public AstNode getParent(Object child) {
		if (child instanceof AstNode) {
			System.out.println("getParent for AstNode " + child);
		}
		return null;
	}

	public Object[] getChildren(Object parent) {
		if (!(parent instanceof AstNode))
			return new Object[0];
		AstNode p = (AstNode) parent;

		// Leaf-level nodes. Label provider may still dig deeper
		if (leafTypes.contains(p.getType()))
			return new Object[0];

		// File name filtering happens in filter on the outline
//		if (!filename.equals(p.getFileName()))
//			return new Object[0];

		// Forward class mention is a leaf, but not a definition
		if (p.getType() == AstNodeTypes.CLASS_KEYWORD) {
			if (p.getChildren().get(1).getType() == AstNodeTypes.SEMICOLON)
				return new Object[0];
			// Roll until hit {, which starts all members
			for (int i = 2; i < p.getChildren().size(); i++) 
				if (p.getChildren().get(i).getType() == AstNodeTypes.LBRACE) {
					// Skip one level, go straight to children in {}
					p = p.getChildren().get(i);
					break;
			}
		}

		// Filtering by file happens outside
//		ArrayList<AstNode> children = new ArrayList<AstNode>();
//		for (AstNode child : p.getChildren()) {
//			if (filename.equals(child.getFileName()))
//				children.add(child);
//		}
//		return children.toArray();
		return p.getChildren().toArray();
	}

	public boolean hasChildren(Object parent) {
		if (!(parent instanceof AstNode))
			return false;
		return getChildren(parent).length > 0;
	}

	public void reload(String fileName) {
		FileModel fmodel = EuropaPlugin.getDefault().getFileModel();
		AstNode root = fmodel.getAstTree(fileName);
		outlinePage.update(root);		
	}
}