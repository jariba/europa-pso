package org.ops.ui.schemabrowser.model;

import psengine.PSEngine;
import psengine.PSSchema;
import psengine.PSStringList;

/**
 * Accessor class for EUROPA schema. Uses PSEngine to get actual data
 * 
 * @author Tatiana Kichkaylo
 */
public class SchemaSource {

	/** PSEngine to do all the work */
	private PSEngine engine;

	public SchemaSource(PSEngine engine) {
		this.engine = engine;
	}

	/** Make a node for predicates */
	public SchemaNode getPredicatesNode() {
		if (engine == null)
			return null;
		SchemaNode node = new SchemaNode("Predicates");
		PSSchema schema = engine.getPSSchema();
		PSStringList preds = schema.getAllPredicates();
		for (int i = 0; i < preds.size(); i++) {
			node.add(new SchemaNode(preds.get(i)));
		}
		return node;
	}

	public void setEngine(PSEngine engine) {
		this.engine = engine;
	}

	public boolean isInitialized() {
		return engine != null;
	}
}
