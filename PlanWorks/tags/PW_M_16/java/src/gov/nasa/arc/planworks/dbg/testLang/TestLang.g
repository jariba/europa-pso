header { package gov.nasa.arc.planworks.dbg.testLang;}


class TestLangParser extends Parser;
//header { package gov.nasa.arc.planworks.dbg.testLang;}
options{ k=2; buildAST=true; exportVocab=TestLang; }


enumeration :  OBRACE^ (single_value (COMMA!)?)+ CBRACE!
            //{System.err.println("enumeration");}
            ;

range : OBRACKET^ single_value TO! single_value CBRACKET!
      ////{System.err.println("range");}
      ;

step_statement : STEP^ (single_boolean_op INTEGER | list_boolean_op list_value)
               //{System.err.println("step_statement");}
               ;

start_statement : START^ (single_boolean_op single_value | list_boolean_op list_value)
                //{System.err.println("start_statement");}
                ;

end_statement : END^ (single_boolean_op single_value | list_boolean_op list_value)
              //{System.err.println("end_statement");}
              ;

status_statement : STATUS^ (single_boolean_op single_value | (IN | OUT | INTERSECTS) list_value)
                 //{System.err.println("status_statement");}
                 ;

predicate_statement : PREDICATE^ ((EQ | NE) single_value | (IN | OUT | INTERSECTS) list_value)
                    //{System.err.println("predicate_statement");}
                    ;

value_statement : VALUE^ (single_boolean_op single_value | list_boolean_op list_value)
                //{System.err.println("value_statement");}
                ;

variable_statement : VARIABLE^ OPAREN! name_statement (COMMA!)? value_statement CPAREN!
                    //{System.err.println("variable_statement");}
                    ;

token_function : TOKENS^ OPAREN! (step_statement | start_statement | end_statement | 
                                  status_statement | predicate_statement | variable_statement)* CPAREN!
                 //{System.err.println("token_function");}
                 ;

name_statement : NAME^ (((EQ | NE) STRING) | ((IN | OUT | INTERSECTS) list_value))
								//{System.err.println("name_statement");}
								;

object_function : OBJECTS^ OPAREN! (step_statement | name_statement | variable_statement)* CPAREN!
                //{System.err.println("object_function");}
                ;

type_statement : TYPE^ ((EQ | NE) STRING | (IN | OUT | INTERSECTS) list_value);

transaction_function : TRANSACTIONS^ OPAREN! (step_statement | name_statement | 
                                             type_statement)* CPAREN!
                     //{System.err.println("transaction_function");}
                     ;

list_valued_function : token_function | object_function | transaction_function
                     //{System.err.println("list_valued_function");}
                     ;

entity_function : ENTITY^ OPAREN! INTEGER (COMMA!)? list_value CPAREN!
                //{System.err.println("entity_function");}
                ;

property_function : PROPERTY^ OPAREN! STRING (COMMA!)? (token_function | object_function | 
                                                    entity_function) CPAREN!
                  //{System.err.println("property_function");}
                  ;

count_function : COUNT^ OPAREN! list_valued_function CPAREN!
               //{System.err.println("count_function");}
               ;

single_valued_function : entity_function | property_function | count_function
                       //{System.err.println("single_valued_function");}
                       ;

list_value : range | enumeration | list_valued_function
           //{System.err.println("list_value");}
           ;

single_value : INTEGER | STRING | single_valued_function
             //{System.err.println("single_value");}
             ;

single_boolean_op : (EQ^ | LT^ | GT^ | GE^ | LE^ | NE^)
                  //{System.err.println("single_boolean_op");}
                  ;

list_boolean_op : (EQ^ | LT^ | GT^ | GE^ | LE^ | NE^ | IN^ | OUT^ | INTERSECTS^)
                //{System.err.println("list_boolean_op");}
                ;

list_boolean_statement : list_value (EQ^ | LT^ | GT^ | GE^ | LE^ | NE^ | IN^ | OUT^ | INTERSECTS^) 
                         (list_value | single_value)
                       //{System.err.println("list_boolean_statement");}
                       ;

singleton_boolean_statement : single_value ((EQ^ | LT^ | GT^ | GE^ | LE^ | NE^) single_value |
                              	            (IN^ | OUT^ | LT^ | GT^ | GE^ | LE^) list_value)
                            //{System.err.println("singleton_boolean_statement");}
                            ;

boolean_statement : singleton_boolean_statement | list_boolean_statement
                  //{System.err.println("boolean_statement");}
                  ;

test_set : TEST^ OPAREN! STRING COMMA! (test_set | boolean_statement SEMI!)+ CPAREN! SEMI!
         //{System.err.println("test_set");}
         ;

class TestLangLexer extends Lexer;
options {k=11; exportVocab=TestLang; }

WS :
   (' '
   | '\t'
   | "\r\n"
   | '\n'
   )
   {$setType(Token.SKIP);}
   ;

STRING : '\'' (ESC|~('\''|'\\'))* '\'';
ESC : '\\' ( 'n' | 't' | '\'' | '\\') ;
OPAREN : '(';
CPAREN : ')';
OBRACKET : '[';
CBRACKET : ']';
OBRACE : '{';
CBRACE : '}';
SEMI : ';';
COMMA : ',';
SQUOTE : '\'';
protected PLUS : '+';
protected MINUS : '-';
EQ : ('=' | "==");
LT : '<';
GT : '>';
GE : ">=";
LE : "<=";
NE : "!=";
TO : "..";
INTEGER : (PLUS | MINUS)? ('0'..'9')+;
TEST : "Test";
TOKENS : "Tokens";
OBJECTS : "Objects";
TRANSACTIONS : "Transactions";
ENTITY : "Entity";
PROPERTY : "Property";
COUNT : "Count";
IN : "in";
OUT : "out";
INTERSECTS : "intersects";
INF : "Inf";
STEP : "step";
START : "start";
END : "end";
DURATION : "duration";
STATUS : "status";
PREDICATE : "predicate";
OBJECT : "object";
MASTER : "master";
VARIABLE : "variable";
VALUE : "value";
NAME : "name";
TYPE : "type";

//this rule exists because ANTLR will only generate matches for characters used in other rules
DUMMY : "ghlqvwxzABCDFGHJKLMNQRSUVWXYZ1234567890!@#$%^&*(){}[]<>,./?\";:`~_-+=\\|";
