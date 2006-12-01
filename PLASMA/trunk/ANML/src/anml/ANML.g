grammar ANML;
options {k=3; backtrack=true; memoize=true;}
@header {
    //package anml;
}

/*options {
    //buildAST = true;	// uses CommonAST by default
    k=3;
    //defaultErrorHandler=false;
}
@header {
    package anml;
}
*/

anml_program 
	: (anml_stmt)*	      
;

anml_stmt
    : declaration
    | problem_stmt
;
  
declaration 
    : objtype_decl
    | func_declaration
    | (
       vartype_decl
       | pred_declaration
       | var_declaration
       | obj_declaration
      ) 
      SEMI_COLON
;

problem_stmt
    : fact
    | goal
;
   
vartype_decl 
    : VARTYPE user_defined_type_name COLON var_type
;

var_declaration 
    : var_type var_init (COMMA var_init)*
;

var_type 
    : BOOL
    | (INT | FLOAT) (range)?
    | STRING
    | ENUM LCURLY constant (COMMA constant)* RCURLY
    | VECTOR typed_arg_list 
    | user_defined_type_name
;

// TODO: should initialization allow for expressions instead of constants?
var_init 
    : var_name (EQUAL constant)?
;

typed_arg_list 
    : LPAREN (arg_declaration (COMMA arg_declaration)*)? RPAREN
;

arg_declaration 
    : var_type var_name
;

objtype_decl 
    : OBJTYPE user_defined_type_name (COLON obj_type)?
      (LCURLY obj_body RCURLY)?
;

// TODO: Any declaration is allowed within an object type, will have to think about implications on scoping
// Are all nested objects visible from outside, or just variables and objects?
// TODO: added constraint, where should constraints be allowed?
obj_body 
    : (declaration | constraint | action_def)*
;

// TODO: var_declaration and obj_declaration can probably be merged
obj_declaration 
    : obj_type IDENTIFIER (COMMA IDENTIFIER)*
;

obj_type 
    : OBJECT 
    | user_defined_type_name
;

func_declaration 
    : FUNCTION var_type function (COMMA function)*
;

// TODO: where are the semantics of the function specified?
function 
    : function_symbol typed_arg_list
;

pred_declaration 
    : PREDICATE predicate (COMMA predicate)*
;

predicate 
    : predicate_symbol typed_arg_list
;

proposition 
    : cntxt_proposition  
    | FOR object_name LCURLY cntxt_proposition RCURLY
;

cntxt_proposition 
    : qualif_fluent
    | FROM time_pt LCURLY qualif_fluent_list RCURLY
;

qualif_fluent_list 
    : qualif_fluent (SEMI_COLON qualif_fluent)*
;

qualif_fluent 
    : temporal_qualif LCURLY fluent_list RCURLY
;

fluent_list 
    : fluent (SEMI_COLON fluent)*
;

temporal_qualif 
    : AT time_pt
    | OVER interval
    | IN interval (DUR numeric_term)?
    | AFTER time_pt (DUR numeric_term)?
    | BEFORE time_pt (DUR numeric_term)?
    | CONTAINS interval
;

// all(fluent) is shorthand for the interval [start(fluent),end(fluent)]
interval 
    : ALL (LPAREN fluent RPAREN)?
    | LBRACK time_pt time_pt RBRACK
;

time_pt 
    : numeric_term
;

numeric_term 
    : numeric_add_term
;

numeric_add_term
    : numeric_mult_term ((PLUS | MINUS) numeric_mult_term)*
;
 
numeric_mult_term
    : numeric_simple_term ((MULT | DIV) numeric_simple_term)*
;

numeric_simple_term 
    : numeric_atomic_term    
    | START (LPAREN fluent RPAREN)?
    | END (LPAREN fluent RPAREN)?
;

// TODO: added object dot notation here as it seemed to be missing
numeric_atomic_term 
    : NUMERIC_LITERAL 
    | var_name (DOT var_name)*
;

fluent 
    : logical_or_fluent
;

logical_or_fluent
    : logical_and_fluent (OR logical_and_fluent)*
;

logical_and_fluent
    : simple_fluent (AND simple_fluent)*
;

simple_fluent
    : atomic_fluent 
    | LPAREN fluent RPAREN
    | NOT fluent
;

// TODO: Decompositions use conditions which in turn are composed of fluents
// that's the resource for the dot notation and the optional naming in the rule below
// are we sure this is what we mean? It would seem (to me, I guess) that decompositions should only refer to other actions
// whereas conditions should not refer to actions
// It would seem that decompositions need an element in the grammar different from condition
// TODO: what about relational operators other than EQUAL?
atomic_fluent 
    : proposition_symbol term_list
    | (object_name DOT)? predicate_symbol term_list (COLON var_name)?
    | term EQUAL term
;

// TODO: shouldn't terms be able to take advantage of the full power of numeric_terms and fluents?
// TODO: typically, numeric terms and fluents are unified and expressions and type checking makes sure semantics are correct
//  we should probably do that for ANML
term_list 
    : LPAREN (term (COMMA term)*)? RPAREN
;

// TODO: added object notation
term 
    : constant
    | var_name (DOT var_name)*
    | function_symbol term_list
;

fact 
    : FACT proposition
    | FACT LCURLY proposition (COMMA proposition)* RCURLY
;

goal 
    : GOAL (proposition  | (LCURLY proposition (SEMI_COLON proposition)* RCURLY))
;

transition 
    : TRANSITION var_name LCURLY trans_pair (SEMI_COLON trans_pair)* RCURLY
;

trans_pair 
    : constant ARROW (constant  | LCURLY constant (COMMA constant)* RCURLY)
;

action_def 
    : ACTION action_symbol typed_arg_list LCURLY action_body RCURLY 
;

// TODO: there are many things that can go in an action and remembering the right order may be hard, allow more flexibility?
action_body 
    : (duration_stmt SEMI_COLON)? 
      (condition_stmt SEMI_COLON)*
      (change_stmt SEMI_COLON)*
      (effect_stmt SEMI_COLON)*
      (decomp_stmt SEMI_COLON)* 
      (constraint SEMI_COLON)* 
;

duration_stmt 
    : DURATION numeric_term
;

condition_stmt 
    : CONDITION (condition | (LCURLY condition (COMMA condition)* RCURLY)) 
;

// TODO: fix precedence for AND/OR
condition 
    : logical_or_condition
;

logical_or_condition
    : logical_and_condition (OR logical_and_condition)*
;

logical_and_condition
    : simple_condition (AND simple_condition)*
;

// TODO: added cond_fluent, otherwise conditions that don't start with a temporal_qualif must start with a "{"
// which is weird, specially if the condition is already inside brackets (like in a "when")
simple_condition
    : temporal_qualif LCURLY cond_fluent RCURLY
    | cond_fluent
    | LPAREN condition RPAREN
;
     
cond_fluent 
    : logical_or_cond_fluent 
;

logical_or_cond_fluent
    : logical_and_cond_fluent (OR logical_and_cond_fluent)*
;

logical_and_cond_fluent
    : simple_cond_fluent (AND simple_cond_fluent)*
;

simple_cond_fluent
    : atomic_fluent 
    | LPAREN cond_fluent RPAREN
    | ORDERED LPAREN cond_fluent (COMMA cond_fluent)* RPAREN
    | UNORDERED LPAREN cond_fluent (COMMA cond_fluent)* RPAREN
;
    
effect_stmt : EFFECT (effect | (LCURLY effect (COMMA effect)* RCURLY))
;

effect 
    : logical_and_effect
;

logical_and_effect
    : simple_effect (AND simple_effect)*
;

// TODO: this part of the BNF was messed up, this is my interpretation
// do we only want to allow atomic_fluents here?
simple_effect
    : (temporal_qualif | WHEN LCURLY condition RCURLY) LCURLY effect_fluent RCURLY
    | LPAREN effect RPAREN
;

effect_fluent 
    : atomic_fluent 
;

change_stmt 
    : CHANGE (change | (LCURLY change (COMMA change)* RCURLY))
;

change 
    : temporal_qualif LCURLY change_expression RCURLY
    | LPAREN change RPAREN AND change
    | WHEN LCURLY condition RCURLY LCURLY change RCURLY
;

// TODO: clean up this rule, second branch doesn't look clean
change_expression 
    : atomic_change (AND atomic_change)?
    | WHEN LCURLY cond_fluent RCURLY LCURLY change_expression RCURLY
;

atomic_change  
    : resource_change
    | transition
;

// TODO: this should probably be constraints like any others
resource_change 
    : (CONSUMES | PRODUCES | USES)  LPAREN var_name (COMMA numeric_term)? RPAREN
;

// TODO: added constraints. where should constraints be allowed?
decomp_stmt 
    : DECOMPOSITION (condition | LCURLY condition (COMMA condition)* (constraint)* RCURLY)
;

// TODO: define grammar to support infix notation for constraints
constraint 
    : constraint_symbol term_list
;

// TODO: allow expressions?
range 
    : LBRACK NUMERIC_LITERAL NUMERIC_LITERAL RBRACK
;

constant 
    : NUMERIC_LITERAL | STRING_LITERAL
;

action_symbol           : IDENTIFIER;
constraint_symbol       : IDENTIFIER;
function_symbol         : IDENTIFIER;
predicate_symbol        : IDENTIFIER;
proposition_symbol      : IDENTIFIER;

object_name             : IDENTIFIER;
var_name                : IDENTIFIER;
user_defined_type_name  : IDENTIFIER;



/*options { 
    k=5;
    //caseSensitive=false; 
    testLiterals=false;   // don't automatically test for literals
}*/
//tokens {
    ACTION        : 'action';
	AFTER         : 'after';
	ALL           : 'all';
	AT            : 'at';
	BEFORE        : 'before';
    BOOL          : 'bool';
    CHANGE        : 'change';
    CONTAINS      : 'contains';
    CONDITION     : 'condition';
    CONSUMES      : 'consumes';
    DECOMPOSITION : 'decomposition';
    DUR           : 'dur';
    DURATION      : 'duration';
    EFFECT        : 'effect';
    END           : 'end';
    ENUM          : 'enum';
    FACT          : 'fact';
    GOAL          : 'goal';
    IN            : 'in';
    INT           : 'int';
    FLOAT         : 'float';
    FOR           : 'for';
    FROM          : 'from';
    FUNCTION      : 'function';
    NOT           : 'not';
    OVER          : 'over';
    OBJECT        : 'object';
    OBJTYPE       : 'objtype';
    ORDERED       : 'ordered';
    UNORDERED     : 'unordered';
    PREDICATE     : 'predicate';
    PRODUCES      : 'produces';
    START         : 'start';
    STRING        : 'string';
    TRANSITION    : 'transition';
    USES          : 'uses';
    VARTYPE       : 'vartype';
    VECTOR        : 'vector';
    WHEN          : 'when';
//}

IDENTIFIER 
	/*options {testLiterals=true;}*/
	:	('a'..'z'|'A'..'Z'|'_'|'$') ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'$')*
;

COMMA           :   ','     ;
COLON			:	':'		;
SEMI_COLON		:	';'		;
EQUAL	        :   '='     ;
LPAREN			:	'('		;
RPAREN			:	')'		;
LBRACK			:	'['		;
RBRACK			:	']'		;
LCURLY			:	'{'		;
RCURLY			:	'}'		;
PLUS            :   '+'     ;
MINUS           :   '-'     ;
MULT            :   '*'     ;
DIV             :   '/'     ;
AND             :   '&'     ;
OR              :   '|'     ;
DOT             :   '.'     ;

ARROW : '-' '>' ;

// a numeric literal
NUMERIC_LITERAL
	@init {boolean isDecimal=false; Token t=null;}
    :   '+inf'
    |   '-inf'
    |   '.' { /*_ttype = DOT;*/ }
            (	('0'..'9')+ (EXPONENT)? (f1=FLOAT_SUFFIX {t=f1;})?
                {
				if (t != null && t.getText().toUpperCase().indexOf('F')>=0) {
                	//_ttype = NUM_FLOAT;
				}
				else {
                	//_ttype = NUM_DOUBLE; // assume double
				}
				}
            )?

	|	(	'0' {isDecimal = true;} // special case for just '0'
			(	('x'|'X')
				(											// hex
					// the 'e'|'E' and float suffix stuff look
					// like hex digits, hence the (...)+ doesn't
					// know when to stop: ambig.  ANTLR resolves
					// it correctly by matching immediately.  It
					// is therefor ok to hush warning.
					/*options {
						warnWhenFollowAmbig=false;
					}*/
					HEX_DIGIT
				)+
			|	('0'..'7')+									// octal
			)?
		|	('1'..'9') ('0'..'9')*  {isDecimal=true;}		// non_zero decimal
		)
		(	('l'|'L') { /*_ttype = NUM_LONG;*/ }

		// only check to see if it's a float if looks like decimal so far
		|	{isDecimal}?
            (   '.' ('0'..'9')* (EXPONENT)? (f2=FLOAT_SUFFIX {t=f2;})?
            |   EXPONENT (f3=FLOAT_SUFFIX {t=f3;})?
            |   f4=FLOAT_SUFFIX {t=f4;}
            )
            {
			if (t != null && t.getText().toUpperCase() .indexOf('F') >= 0) {
                //_ttype = NUM_FLOAT;
			}
            else {
	           	//_ttype = NUM_DOUBLE; // assume double
			}
			}
        )?
	;


// a couple protected methods to assist in matching floating point numbers
fragment
EXPONENT
	:	('e'|'E') ('+'|'-')? ('0'..'9')+
	;


fragment
FLOAT_SUFFIX
	:	'f'|'F'|'d'|'D'
	;


// hexadecimal digit (again, note it's protected!)
fragment
HEX_DIGIT
	:	('0'..'9'|'A'..'F'|'a'..'f')
	;

// string literals
STRING_LITERAL
    :  '"' ( EscapeSequence | ~('\\'|'"') )* '"'
    ;

fragment
EscapeSequence
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    ;

comment : (COMMENT | LINE_COMMENT)
;

COMMENT
    :   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

LINE_COMMENT
    : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    ;
