package org.ops.ui.editor.swt;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.ops.ui.filemanager.model.AstNode;
import org.ops.ui.filemanager.model.AstNodeTypes;
import org.ops.ui.main.swt.EuropaPlugin;

/**
 * Label/icon provider for the NDDL outline
 * 
 * @author Tatiana Kichkaylo
 */
public class NddlLabelProvider extends LabelProvider {
	private final String path = "icons/";

	private final String[] names = { "class.png", "class_forward.png",
			"enum.png", "var.png", "pred.png", "cons.png", "fact.png",
			"constr.png", "goal.png" };

	private Image[] images = null;

	@SuppressWarnings("null")
	@Override
	public String getText(Object object) {
		AstNode node = (AstNode) object;
		AstNode child = null;
		if (!node.getChildren().isEmpty())
			child = node.getSafe(0);
		AstNode second = null;

		switch (node.getType()) {
		case AstNodeTypes.ENUM_KEYWORD:
		case AstNodeTypes.CLASS_KEYWORD:
			assert (child != null) : "No name?";
			assert (child.getType() == AstNodeTypes.IDENT);
			return child.getText();
		case AstNodeTypes.VARIABLE:
			assert (child != null) : "No variable name?";
			// First child gives type
			assert (child.getType() == AstNodeTypes.IDENT);
			// The second child may be an identifier, or =. In the latter case
			// the variable name is the first child of = (the 2nd is the 
			// expression)
			second = node.getSafe(1);
			if (second.getType() == AstNodeTypes.EQUAL_CHAR)
				second = second.getSafe(0);
			assert (second.getType() == 0 || second.getType() == AstNodeTypes.IDENT);
			return child.getText() + " " + second.getText();
		case AstNodeTypes.DCOLON:
			second = node.getSafe(1);
			assert (child.getType() == AstNodeTypes.IDENT);
			assert (second.getType() == AstNodeTypes.IDENT);
			return child.getText() + "::" + second.getText();
		case AstNodeTypes.CONSTRUCTOR:
		case AstNodeTypes.PREDICATE_KEYWORD:
		case AstNodeTypes.CONSTRAINT_INSTANTIATION:
			return child.getText() + buildFunctionArgs(node, 1);
			// node.getChildren().get(1));
		case AstNodeTypes.CLOSE_KEYWORD:
			return "close";
		case AstNodeTypes.ACTIVATE_KEYWORD:
			return child.getText() + ".activate()";
		case AstNodeTypes.GOAL_KEYWORD:
			// return "goal" + buildFunctionArgs(node, 0);
		case AstNodeTypes.FACT_KEYWORD:
			// the only child is (, it's only child is PREDICATE_INSTANCE,
			// which has two children: a . and a token name
			assert (child.getType() == AstNodeTypes.LPAREN);
			second = child.getSafe(0);
			assert (second.getType() == AstNodeTypes.PREDICATE_INSTANCE);
			assert (second.getChildren().size() == 2);
			return argText(second.getSafe(0)) + " " + second.getSafe(1).getText();
		default:
			return node.getType() + " " + node.toString();
		}
	}

	/**
	 * Child is the name of the function. It may or may not have a sibling (
	 * with parameters. Build a pretty string for function signature
	 */
	private String buildFunctionArgs(AstNode parent, int index) {
		StringBuffer b = new StringBuffer();
		b.append("(");
		AstNode child = parent.getChildren().get(index);
		if (child.getType() == AstNodeTypes.LPAREN) {
			parent = child;
			index = 0;
			child = parent.getSafe(index);
			if (child != null) {
				b.append(argText(child));
				child = parent.getSafe(++index);
			}
			while (child != null) {
				b.append(", ").append(argText(child));
				child = parent.getSafe(++index);
			}
		}
		b.append(")");
		return b.toString();
	}

	private String argText(AstNode node) {
		if (node.getType() == AstNodeTypes.DOT_CHAR) {
			// The first child is token name, the second is the field
			return node.getSafe(0).getText() + "." + node.getSafe(1).getText();
		}
		if (node.getType() == AstNodeTypes.VARIABLE)
			return getText(node);
		assert (node.getType() == AstNodeTypes.STRING) : "Unexpected arg node type "
				+ node.getType();
		return node.getText();
	}

	private void loadImages() {
		images = new Image[names.length];
		for (int i = 0; i < names.length; i++) {
			try {
				ImageDescriptor descr = EuropaPlugin.getImageDescriptor(path
						+ names[i]);
				images[i] = descr.createImage();
			} catch (Exception e) {
				throw new RuntimeException("Cannot load icon " + names[i]);
			}
		}
	}

	@Override
	public Image getImage(Object object) {
		if (images == null)
			loadImages();

		AstNode node = (AstNode) object;
		AstNode child = node.getSafe(0);
		AstNode second = node.getSafe(1);

		switch (node.getType()) {
		case AstNodeTypes.CLASS_KEYWORD:
			assert (child != null) : "Class has not name?";
			assert (child.getType() == AstNodeTypes.IDENT);
			assert (second != null);
			if (second.getType() == AstNodeTypes.SEMICOLON)
				return images[1]; // "class_forward"
			return images[0];
		case AstNodeTypes.ENUM_KEYWORD:
			return images[2];
		case AstNodeTypes.VARIABLE:
			return images[3];
		case AstNodeTypes.PREDICATE_KEYWORD:
			return images[4];
		case AstNodeTypes.CONSTRUCTOR:
			return images[5];
		case AstNodeTypes.FACT_KEYWORD:
			return images[6];
		case AstNodeTypes.CONSTRAINT_INSTANTIATION:
			return images[7];
		case AstNodeTypes.ACTIVATE_KEYWORD:
		case AstNodeTypes.CLOSE_KEYWORD:
		case AstNodeTypes.GOAL_KEYWORD:
			return images[8];
		}
		return null;
	}

	@Override
	public void dispose() {
		if (images != null)
			for (Image im : images)
				im.dispose();
		super.dispose();
	}
}
