package org.ops.ui.schemabrowser.model;

import java.util.ArrayList;

/** Node of the schema tree. 
 * 
 * @author Tatiana Kichkaylo
 */
public class SchemaNode {
	/** Seed for conversion to array */
	private final static SchemaNode[] seedArray = {};

	/** Label to appear on this node */
	private String text;
	
	/** Child nodes */
	private ArrayList<SchemaNode> children = new ArrayList<SchemaNode>();

	public SchemaNode(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
	public void add(SchemaNode child) {
		children.add(child);
	}
	
	public ArrayList<SchemaNode> getChildren() {
		return children;
	}
	
	public SchemaNode[] getArray() {
		return children.toArray(seedArray);
	}
	
	@Override
	public String toString() {
		return text;
	}

	/** Remove all children */
	public void clear() {
		children.clear();
	}
}
