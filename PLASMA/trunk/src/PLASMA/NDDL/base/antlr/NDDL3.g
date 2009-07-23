/* vim: set ts=8 ft=antlr3: */

grammar NDDL3;

options {
language=C;
output=AST;
ASTLabelType=pANTLR3_BASE_TREE;
}

tokens {
	CONSTRAINT_INSTANTIATION;
	CONSTRUCTOR;
	CONSTRUCTOR_INVOCATION;
	METHOD_CALL;
	NDDL;
	PREDICATE_INSTANCE;     
	TOKEN_RELATION;
	VARIABLE;
}

@lexer::includes
{
#include "NddlInterpreter.hh"

using namespace EUROPA;
}

@lexer::context {
    NddlInterpreter* parserObj;
}


nddl	
    :   nddlStatement*
	       -> ^(NDDL nddlStatement*)
    ;

nddlStatement
    :   typeDefinition
    |   enumDefinition
    |   variableDeclarations
    |   assignment
    |   constraintInstantiation
    |   classDeclaration
    |   allocationStmt
    |   rule
    |   problemStmt
    |   relation
    |   methodInvocation
    |   noopstatement
    |   constraintSignature!
    ;

enumDefinition
    : 'enum'^ IDENT enumValues
    ;
   
enumValues
    : '{'^ IDENT (','! IDENT)* '}'!
    ;
                
// MEB Language Change: domain no longer optional
typeDefinition
	:	'typedef' type baseDomain IDENT ';'
	       -> ^('typedef' IDENT type baseDomain)
	;

baseDomain  
    :   intervalBaseDomain
    |   enumeratedBaseDomain
    ;

intervalBaseDomain
    :   '['^ numericLiteral (','!)? numericLiteral ']'!
    ;

enumeratedBaseDomain
    :   '{'^ baseDomainValue (','! baseDomainValue)* '}'!
    ;
       
baseDomainValue
    : literalValue
    | qualified  
    ;
                
variableDeclarations
    :   ('filter')? type nameWithBaseDomain (',' nameWithBaseDomain)* ';'
            -> ^(VARIABLE type nameWithBaseDomain (nameWithBaseDomain)*)
    ;

nameWithBaseDomain
    :   (   variable=IDENT ('('^ value=initializer ')'! )?
        |   variable=IDENT '='^ value=initializer
        )
    ;

anyValue
    :   literalValue
    |   baseDomain
    |   qualified
    ;

allocation
    :   'new'! constructorInvocation
    ;

constructorInvocation
    :   IDENT variableArgumentList
            -> ^(CONSTRUCTOR_INVOCATION IDENT variableArgumentList)
    ;

qualified
    :   ('this' | IDENT) ('.'^ IDENT)*
    ;

assignment
    :   qualified ('in' | '=') initializer ';'
            -> ^('=' qualified initializer)
    ;
    
initializer
    :   anyValue
    |   allocation
    ;
        
classDeclaration
	:	'class' c=IDENT
		(	(('extends' x=IDENT)? classBlock)
		        -> ^('class' $c ^('extends' $x)? classBlock)
		|	';'
                -> ^('class' $c ';')
		) 
	;

classBlock
	:	'{'^ classStatement* '}'!
	;

classStatement
	:	variableDeclarations
	|	constructor
	|	predicate
	|	noopstatement
	;

constructor
	:	IDENT constructorParameterList constructorBlock
			-> ^(CONSTRUCTOR IDENT constructorParameterList constructorBlock)
	;

constructorBlock
	:	'{'^ constructorStatement* '}'!
	;

constructorStatement
	:	assignment
	|	superInvocation
	|	noopstatement
	;

constructorParameterList
	:	'('^ constructorParameters? ')'!
	;

constructorParameters
	:	constructorParameter  (','! constructorParameters)?
	;

constructorParameter
	:	type IDENT
			-> ^(VARIABLE IDENT type)
	;

predicate
	:	'predicate'^ IDENT predicateBlock 
	;

predicateBlock
	:	'{'^ predicateStatement* '}'!
	;

// Note: Allocations are not legal here.
predicateStatement
	:	variableDeclarations
	|	constraintInstantiation
	|	assignment
	;


rule
    :	IDENT '::'^ IDENT ruleBlock
	;

ruleBlock
	:	'{'^ ruleStatement* '}'!
	|	ruleStatement -> ^('{' ruleStatement)
	;

ruleStatement
	:	relation
	|	variableDeclarations
	|	constraintInstantiation
	|	flowControl
	|	noopstatement
	;

type	
    :	'int'
	|	'float'
	|	'bool'
	|	'string'
	|	IDENT
	;

relation
    :	(token=IDENT | token='this')? temporalRelation predicateArgumentList ';'
			-> ^(TOKEN_RELATION $token temporalRelation predicateArgumentList)
    ;

problemStmt
    :	('rejectable'^ | 'goal'^ | 'fact'^) predicateArgumentList ';'!
	;
        
predicateArgumentList
	:	IDENT
	|	'('^ predicateArguments? ')'!
	;

predicateArguments
	:	predicateArgument (','! predicateArgument)*
	;

predicateArgument
	:	qualified IDENT?
	        -> ^(PREDICATE_INSTANCE qualified IDENT?)
	;

constraintInstantiation
	:	IDENT variableArgumentList ';'
			-> ^(CONSTRAINT_INSTANTIATION IDENT variableArgumentList)
	;

superInvocation
	:	'super'^ variableArgumentList ';'!
	;

variableArgumentList
	:	'('^ variableArguments? ')'!
	;

variableArguments
	:	variableArgument (','! variableArgument)*
	;

variableArgument
	:	anyValue
	;

typeArgumentList
	:	'('^ typeArguments? ')'!
	;

typeArguments
	:	typeArgument (','! typeArgument)*
	;

typeArgument
	:	IDENT
	;

flowControl
	:	'if'^ guardExpression ruleBlock (options {k=1;}:'else'! ruleBlock)?
	|	'foreach'^ '('! IDENT 'in'! qualified ')'! ruleBlock
	;
	
guardExpression
	:	'('! anyValue (('=='^ | '!='^) anyValue)? ')'!
	;
          
allocationStmt
	:	allocation ';'!
	;

temporalRelation
    :   'after'
    |   'any'
    |   'before'
    |   'contained_by'
    |   'contains'
    |   'contains_end'
    |   'contains_start'
    |   'ends'
    |   'ends_after'
    |   'ends_after_start'
    |   'ends_before'
    |   'ends_during'
    |   'equal'
    |   'equals'
    |   'meets'
    |   'met_by'
    |   'parallels'
    |   'paralleled_by'
    |   'starts'
    |   'starts_after'
    |   'starts_before'
    |   'starts_before_end'
    |   'starts_during'
	;

literalValue
    :   booleanLiteral
    |   numericLiteral
    |   stringLiteral
    ;
    
booleanLiteral
    :   'true'
    |   'false' 
    ;

numericLiteral
	:	INT
	|	FLOAT
	|	('+'!)? ('inf' | 'inff')
	|	'-inf' 
    |   '-inff' 
	;

stringLiteral
    :   STRING
    ;
    
// TODO: this is ugly and very inflexible, need to provide extensible method exporting mechanism  
methodName
    :   'specify'
    |   'reset'
    |   'constrain'
    |   'free'
    |   'activate'
    |   'merge'
    |   'reject'
    |   'cancel'
    |   'close'
    ;
         
methodInvocation
    :   (qualified '.')? methodName variableArgumentList ';'
            -> ^(METHOD_CALL methodName qualified? variableArgumentList)
	;

tokenNameList
	:  '('^ (tokenNames)? ')'!
	;

tokenNames
	:	IDENT (','! IDENT)*
    ;

noopstatement
	:	';'!
	;

constraintSignature
    :   'constraint' c=IDENT args=typeArgumentList
            ('extends' x=IDENT xargs=typeArgumentList)? 
            (sb=signatureBlock | ';')
                -> ^('constraint' $c $args ^('extends' $x $xargs)? $sb?)
    ;

signatureBlock
    :   '{'^ signatureExpression? '}'!
    ;

signatureExpression
    :   signatureAtom (('&&'^ | '||'^) signatureAtom)*
    ;

signatureAtom
    :   '('^ signatureExpression ')'!
    |   IDENT '<:'^ (type | 'numeric' )
    ;

INCLUDE :	'#include' WS+ file=STRING 
                {
                        std::string fullName = std::string((const char*)($file.text->chars));
                        fullName = CTX->parserObj->getFilename(fullName);
                        if (!CTX->parserObj->queryIncludeGuard(fullName)) {
                            CTX->parserObj->addInclude(fullName);
                            // Look for the included file in include path

                            if (fullName.length() == 0) {
                                std::string path = "";
                                std::vector<std::string> parserPath = CTX->parserObj->getIncludePath();
                                for (unsigned int i=0; i<parserPath.size();i++) {
                                    path += parserPath[i] + ":";
                                }
                                checkError(false, std::string("ERROR!: couldn't find file: " + std::string((const char*)$file.text->chars)
                                                              + ", search path \"" + path + "\"").c_str());
                            }

                            // Create a new input stream and take advantage of built in stream stacking
                            // in C target runtime.

                            pANTLR3_STRING_FACTORY factory = antlr3StringFactoryNew();
                            pANTLR3_STRING fName = factory->newStr(factory,(ANTLR3_UINT8 *)fullName.c_str());
                            delete factory;
                        
                            pANTLR3_INPUT_STREAM in = antlr3AsciiFileStreamNew(fName->chars);
                            PUSHSTREAM(in);
                            CTX->parserObj->addInputStream(in);

                            // TODO: the string may be leaked here?
                        } else {
                            //std::cout << "Ignoring already included file " << fullName << std::endl;
                        }
                        
                        $channel=HIDDEN;
		}
	;

IDENT	:	 ('a'..'z'|'A'..'Z'|'_'|'$') ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'$')*
	;

STRING	:	'"' (~('\\'|'"') | ESCAPE_SEQUENCE)* '"'
	;

fragment ESCAPE_SEQUENCE
	:	'\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
	|	UNICODE_ESC
	|	OCTAL_ESC
	;

fragment OCTAL_ESC
	:	'\\' ('0'..'3') ('0'..'7') ('0'..'7')
	|	'\\' ('0'..'7') ('0'..'7')
	|	'\\' ('0'..'7')
	;

fragment UNICODE_ESC
	:	'\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
	;

fragment HEX_DIGIT
	:	('0'..'9'|'a'..'f'|'A'..'F')
	;
	
fragment DIGIT
	:	('0'..'9')
	;
	
INT	:	('+' | '-')? ('0' | '1'..'9' '0'..'9'*) INT_SUFFIX?
	;
	
fragment INT_SUFFIX
	:	('l'|'L')
	;

FLOAT	:	('+' | '-')? ('0'..'9')+ '.' ('0'..'9')* EXPONENT? FLOAT_SUFFIX?
	|	'.' ('0'..'9')+ EXPONENT? FLOAT_SUFFIX?
	|	('0'..'9')+ EXPONENT FLOAT_SUFFIX?
	|	('0'..'9')+ FLOAT_SUFFIX
	;

fragment EXPONENT
	:	('e'|'E') ('+'|'-')? ('0'..'9')+
	;
	
fragment FLOAT_SUFFIX
	:	('f'|'F'|'d'|'D')
	;
	
COMMENT	:	'/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
	;

LINE_COMMENT
	:	'//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
	;

WS	:	(' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;}
	;
