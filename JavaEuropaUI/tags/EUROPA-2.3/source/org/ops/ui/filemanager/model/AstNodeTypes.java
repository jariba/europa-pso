package org.ops.ui.filemanager.model;

/**
 * Type constants. The numbers in this file should match those in 
 * /Europa/src/PLASMA/NDDL/base/NDDL3.tokens
 */
public interface AstNodeTypes {
	public static final int ERROR = 0;
	public static final int NDDL = 8;
	public static final int IDENT = 15;
	public static final int CLASS_DEF = 47; // '::' for class
	public static final int PREDICATE_DEF = 50; // '::' for predicate code
	public static final int LBRACE = 32; // '{'
	public static final int VARIABLE = 11;
	public static final int CONSTRUCTOR = 5;
	public static final int LPAREN = 40; // '('
	// mention of predicate in class
	public static final int PREDICATE_KEYWORD = 49; 
	public static final int GOAL_KEYWORD = 56;
	public static final int FACT_KEYWORD = 57;
	// instantiation of predicate
	public static final int PREDICATE_INSTANCE = 9;
	public static final int CONSTRAINT_INSTANCE = 4;	
	public static final int ENUM_KEYWORD = 31;
}
