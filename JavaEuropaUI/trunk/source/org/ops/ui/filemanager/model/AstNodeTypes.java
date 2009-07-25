package org.ops.ui.filemanager.model;

/**
 * Type constants. The numbers in this file should match those in 
 * /Europa/src/PLASMA/NDDL/base/NDDL3.tokens
 */
public interface AstNodeTypes {
	public static final int NDDL = 8;
	public static final int IDENT = 12;
	public static final int ENUM_KEYWORD = 28;
	public static final int CLASS_KEYWORD = 44;
	public static final int VARIABLE = 11;
	public static final int CONSTRUCTOR = 5;
	public static final int PREDICATE_KEYWORD = 46;
	public static final int PREDICATE_INSTANCE = 9;
	public static final int CONSTRAINT_INSTANTIATION = 4;
	public static final int LBRACE = 29;
	public static final int LPAREN = 37;
	public static final int SEMICOLON = 33;
	public static final int DCOLON = 47;
	public static final int GOAL_KEYWORD = 53;
	public static final int FACT_KEYWORD = 54;
	public static final int ACTIVATE_KEYWORD = 95;
	public static final int CLOSE_KEYWORD = 99;
	public static final int EQUAL_CHAR = 39; // '='
	public static final int DOT_CHAR = 42; // '.'
	public static final int STRING = 13; // all literals, including numbers

	/*
	EXPONENT=24
	FLOAT_SUFFIX=25
	CONSTRUCTOR_INVOCATION=6
	OCTAL_ESC=20
	INCLUDE=17
	COMMENT=26
	LINE_COMMENT=27
	INT=14
	METHOD_CALL=7
	WS=16
	INT_SUFFIX=23
	FLOAT=15
	TOKEN_RELATION=10
	ESCAPE_SEQUENCE=18
	DIGIT=22
	UNICODE_ESC=19
	HEX_DIGIT=21
	'contained_by'=64
	'paralleled_by'=78
	'=='=59
	'free'=94
	'string'=51
	'parallels'=77
	'||'=102
	'this'=41
	'&&'=101
	'typedef'=32
	'ends_after'=69
	'+'=84
	'specify'=91
	'starts'=79
	'rejectable'=52
	'contains'=65
	'true'=89
	'contains_end'=66
	'else'=57
	'equals'=74
	'starts_after'=80
	'extends'=45
	'numeric'=104
	'constrain'=93
	'-inf'=87
	'equal'=73
	'<:'=103
	'int'=48
	'if'=56
	','=30
	'inf'=85
	'in'=43
	'starts_during'=83
	']'=35
	'reset'=92
	'foreach'=58
	'filter'=36
	'cancel'=98
	'new'=40
	'ends'=68
	'meets'=75
	'merge'=96
	'-inff'=88
	'constraint'=100
	'ends_after_start'=70
	'bool'=50
	')'=38
	'ends_before'=71
	'super'=55
	'any'=62
	'after'=61
	'starts_before_end'=82
	'float'=49
	'}'=31
	'contains_start'=67
	'met_by'=76
	'reject'=97
	'inff'=86
	'before'=63
	'!='=60
	'false'=90
	'ends_during'=72
	'starts_before'=81
	'['=34
*/
}
