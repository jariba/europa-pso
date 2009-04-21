/* vim: set ts=8 ft=antlr3: */
tree grammar NDDL3Tree;

options {
	language=C;
	tokenVocab=NDDL3;
	ASTLabelType=pANTLR3_BASE_TREE;
}

@context {
    NddlSymbolTable* SymbolTable;
}

@includes
{
#include "Debug.hh"
#include "BoolDomain.hh"
#include "StringDomain.hh"
#include "NddlInterpreter.hh"

using namespace EUROPA;

}

@members {

static const char* c_str(pANTLR3_UINT8 chars)
{
    // TODO: what's the best way to do this?
    return (const char*)chars;
}

// TODO: make sure we also get ANTLR errors, see apifuncs below
static void reportSemanticError(pNDDL3Tree treeWalker, const std::string& msg)
{
    // get location. see displayRecognitionError() in antlr3baserecognizer.c
    treeWalker->SymbolTable->addError(msg);
    // TODO: make sure cleanup is done correctly
    throw msg;
}

static std::string getAutoLabel(const char* prefix)
{
    static int idx = 0;
    std::ostringstream os;
    os << prefix << "_" << idx++;
    
    return os.str();   
}

static DataRef evalExpr(pNDDL3Tree treeWalker,Expr* expr)
{
    return expr->eval(*(treeWalker->SymbolTable));
}

}

@apifuncs
{
    // TODO: Install custom error message display that gathers them in CTX->SymbolTable
    //RECOGNIZER->displayRecognitionError = reportAntlrError;

    // Add a function with the following signature:
    // void reportAntlrError(pANTLR3_BASE_RECOGNIZER recognizer, pANTLR3_UINT8 *tokenNames);    
    // use the meat from displayRecognitionError() in antlr3baserecognizer.c
}

nddl :               
	^(NDDL
		( {
		     // debugMsg("NddlInterpreter:nddl","Line:" << LEXER->getLine(LEXER)); 
		  }
		  (	child=enumDefinition
		  |	child=typeDefinition
		  |	child=variableDeclarations
		  |	child=assignment
		  |	child=constraintInstantiation
		  |	child=classDeclaration
		  |	child=allocation[NULL]
		  |	child=rule
		  |	child=problemStmt
		  |	child=relation
		  |	child=methodInvocation
		  |	child=constraintSignature
		  ) 
		  {
		      if (child != NULL) { 
		          debugMsg("NddlInterpreter:nddl","Evaluating:" << child->toString());
		          evalExpr(CTX,child);
		          // TODO!!: systematically deal with memory mgmt for all Exprs.
		          delete child;
		          child = NULL; 
		          
		          CTX->SymbolTable->getPlanDatabase()->getConstraintEngine()->propagate();
		      }
		  }
		)*  
		
	)
	;

enumDefinition returns [Expr* result]
@init {
    std::vector<std::string> values;
}
        :       ^('enum' name=IDENT enumValues[values])
                {
                    const char* enumName = c_str($name.text->chars);
                    CTX->SymbolTable->addEnumValues(enumName,values);
                    result = new ExprEnumdef(enumName,values);
                }
        ;

enumValues[std::vector<std::string>& values]
        :       ^('{' (v=IDENT {values.push_back(c_str($v.text->chars));})+ )
        ;
                  
typeDefinition returns [Expr* result]
	:	^('typedef'
			name=IDENT
			dataType=type
			{
    		    if (dataType == NULL) {		        
                         result = NULL;              
                         reportSemanticError(CTX,
                            "Incorrect typedef. Unknown data type for : " + std::string(c_str($name.text->chars)));
                }            
			}
			domain=baseDomain[dataType]
		)
		{
		    if (dataType != NULL) {		        
		        const char* newName = c_str($name.text->chars);
		        result = new ExprTypedef(dataType->getId(),newName,domain);
		     }
		}
	;

type returns [const DataType* result] 
        : (     name='int'
        |       name='float'
        |       name='bool'
        |       name='string'
        |       name=IDENT
        )
        {
            const DataTypeId& dt = CTX->SymbolTable->getVarType(c_str($name.text->chars));
            result = (DataType*)dt;
        }
        ;
        
baseDomain[const DataType* baseType] returns [AbstractDomain* result]
        : (      child=valueSet
          |      child=numericInterval
          )
{
    // TODO: type checking. ensure inline domain is consistent with base
    DataRef data=evalExpr(CTX,child);    
    result = (AbstractDomain*)&(data.getValue()->lastDomain()); 
    // TODO: delete child;?
}        
        ;       

variableDeclarations returns [ExprList* result]
        :       ^(VARIABLE dataType=type 
                           {
                               if (dataType != NULL)
                                   result = new ExprList();
                               else { 
                                   result = NULL;
                                   reportSemanticError(CTX,
                                       "Incorrect variable declaration. Unknown data type");
                               }
                           }
                           (child=variableInitialization[dataType]
                           {
                               result->addChild(child);
                           }
                           )+
                 )
        ;
        
variableInitialization[const DataType* dataType] returns [Expr* result]
@init {
   const char* varName; 
}
        : (      name=IDENT { varName = c_str($name.text->chars); }
          |       ^('=' name=IDENT  { varName = c_str($name.text->chars); } initExpr=initializer[varName])
          )
          {
              // TODO: type check initExpr;
              result = new ExprVarDeclaration(
                   varName,
                   dataType->getId(),
                   initExpr,
                   true // canBeSpecified
              ); 
          }
        ;       

initializer[const char* varName] returns [Expr* result]
        : (     child=anyValue 
          |     child=allocation[varName]
          |     child=qualified
          )
          {
              result = child;
          }
        ;

anyValue returns [Expr* result]
        : (      child=value
          |      child=valueSet
          |      child=numericInterval
          )
          { 
              result = child;
          }
        ;

value returns [Expr* result]
@init {
    result = NULL;
}
        : (       child=booleanLiteral
          |       child=stringLiteral
          |       child=numericLiteral 
          |       ^(i=IDENT type?) // TODO: what is this?, a variable ref?, an object ref?
                   {
                       const char* ident = c_str($i.text->chars);
                       if (CTX->SymbolTable->isEnumValue(ident))
                           result = CTX->SymbolTable->makeEnumRef(ident);
                       else  
                           result = new ExprVarRef(ident); 
                   }
          )
          { 
              if (result == NULL)
                  result = new ExprConstant(
                      CTX->SymbolTable->getPlanDatabase()->getClient(),
                      child->getTypeName().c_str(),
                      child); 
          }
        ;

valueSet returns [Expr* result]
@init {
    std::list<double> values;
    DataTypeId elementType;
}
        :       ^('{'
                        (element=value
                         {
                             DataRef elemValue = evalExpr(CTX,element);
                             const AbstractDomain& ev = elemValue.getValue()->lastDomain(); 
                             // TODO: delete element;
                             elementType = ev.getDataType();
                             double v = ev.getSingletonValue();
                             values.push_back(v);
                             // TODO: make sure data types for all values are consistent
                         }
                        )*
                 )
                 {
                   AbstractDomain* newDomain = new EnumeratedDomain(elementType,values); 
                   result = new ExprConstant(
                       CTX->SymbolTable->getPlanDatabase()->getClient(),
                       elementType->getName().c_str(),
                       newDomain                       
                   );
                 }
        ;

booleanLiteral returns [AbstractDomain* result]
        :       'true'  { result = new BoolDomain(true); }            
        |       'false' { result = new BoolDomain(false); }
        ;

stringLiteral returns [AbstractDomain* result]
        :    str = STRING 
             { 
                 // remove quotes
                 std::string s(c_str($str.text->chars));
                 s = s.substr(1,s.size()-2);
                 
                 LabelStr value(s); 
                 result = new StringDomain((double)value,StringDT::instance());
             }
        ; 

numericLiteral returns [AbstractDomain* result]
        :       floating=floatLiteral  { result = CTX->SymbolTable->makeNumericDomainFromLiteral("float",c_str($floating.text->chars)); }
        |       integer=intLiteral  { result = CTX->SymbolTable->makeNumericDomainFromLiteral("int",c_str($integer.text->chars)); }
        ;

floatLiteral 
        : FLOAT | 'inff' | '-inff'
        ;        

intLiteral 
        : INT | 'inf' | '-inf'
        ;        
        
numericInterval returns [Expr* result]
        :       ^('['   
                        lower=numericLiteral
                        upper=numericLiteral
                )
                {      
                    double lb = lower->getSingletonValue();
                    double ub = upper->getSingletonValue();
                    AbstractDomain* baseDomain;
                    
                    if (lower->getTypeName().toString()=="float" || 
                        upper->getTypeName().toString()=="float") 
                        baseDomain = new IntervalDomain(lb,ub);
                    else 
                        baseDomain = new IntervalIntDomain((int)lb,(int)ub);
                                  
                    result = new ExprConstant(
                        CTX->SymbolTable->getPlanDatabase()->getClient(),
                        lower->getTypeName().c_str(),
                        baseDomain
                    );
                    
                    delete lower;
                    delete upper;
                }
        ;

allocation[const char* name] returns [Expr* result]
@init {
    std::vector<Expr*> args;
}
        :       ^(CONSTRUCTOR_INVOCATION
                        objType=IDENT 
                        variableArgumentList[args]?
                )
                {
                    std::string objName = (name != NULL ? name : getAutoLabel("__Object"));
                    result = new ExprNewObject(
                        CTX->SymbolTable->getPlanDatabase()->getClient(),
                        c_str($objType.text->chars), // objectType
                        objName.c_str(),
                        args
                    );
                }
        ;

variableArgumentList[std::vector<Expr*>& result]
        :       '('
        |       ^('('
                        (arg=initializer[NULL] {result.push_back(arg);})*
                )
        ;

identifier 
        : IDENT
        | 'this'
        ;

constraintInstantiation returns [ExprConstraint* result]
@init {
    std::vector<Expr*> args;
}
        :       
              ^(CONSTRAINT_INSTANTIATION
                        name=IDENT
                        variableArgumentList[args]
                )
                {
                    result = new ExprConstraint(c_str($name.text->chars),args);
                }
                
        ;

classDeclaration returns [Expr* result]
@init {
const char* newClass = NULL;
const char* parentClass = "Object";
ObjectType* objType = NULL;
}
	:	^('class'
                   className=IDENT { newClass = c_str($className.text->chars); }
                   
		   (^('extends' superClass=IDENT { parentClass = c_str($superClass.text->chars); }))?
		   
                   {
                       objType = new ObjectType(newClass,parentClass);
                       // TODO: do this more cleanly. Needed to deal with self-reference inside class definition
                       CTX->SymbolTable->getPlanDatabase()->getSchema()->declareObjectType(newClass);
                       CTX->SymbolTable->setCurrentObjectType(objType);                       
                   }
                   
                   (
		       classBlock[objType] { result = new ExprObjectTypeDefinition(objType->getId()); } 
		       | ';' 
		         { 
		             result = new ExprObjectTypeDeclaration(objType->getName());
		             delete objType; 
		         }
	           )
		)
		{
		    CTX->SymbolTable->setCurrentObjectType(NULL);                       		    
		}
  ;

classBlock[ObjectType* objType]
	:	'{'
	        | ^('{'
			componentTypeEntry[objType]*
		)
	;

componentTypeEntry[ObjectType* objType]
	:	classVariable[objType]
	|	constructor[objType]
	|	predicate[objType]
	;

classVariable[ObjectType* objType]
        :       ^(VARIABLE 
                  dataType=type 
                  (name=IDENT
                  {
                      // TODO: modify addMember to take DataTypeId instead
                      objType->addMember(dataType->getName().c_str(),c_str($name.text->chars)); 
                  }
                  )+
                 )
        ;
        
constructor[ObjectType* objType]
@init {
    std::vector<std::string> argNames;
    std::vector<std::string> argTypes;
    std::vector<Expr*> body;
}
	:	^(CONSTRUCTOR
			name=IDENT
			^('(' constructorArgument[argNames,argTypes]*)
			^('{' superCallExpr=constructorSuper[objType]?
			      (child=assignment {body.push_back(child);})*)
		)
		{
		    std::ostringstream signature;
		    signature << objType->getName().c_str();
		    
		    for (unsigned int i=0;i<argTypes.size();i++)
		        signature << ":" << argTypes[i];
		        
                    objType->addObjectFactory(
                        (new InterpretedObjectFactory(
                            objType->getId(),
                            signature.str(),
                            argNames,
                            argTypes,
                            superCallExpr,
                            body)
                        )->getId()
                    );
		}
	;

constructorArgument[std::vector<std::string>& argNames,std::vector<std::string>& argTypes]
	:	^(VARIABLE
			argName=IDENT
			argType=type
		)
		{
		    argNames.push_back(std::string(c_str($argName.text->chars)));
		    argTypes.push_back(argType->getName().toString());
		}
	;

constructorSuper[ObjectType* objType] returns [ExprConstructorSuperCall* result]
@init {
    std::vector<Expr*> args;
}
	:	^('super'
			variableArgumentList[args]
		)
		{
		    result = new ExprConstructorSuperCall(objType->getParent(),args);  
		}
	;
  
assignment returns [ExprAssignment* result]
	:	^('='
			lhs=qualified
			rhs=initializer[lhs->toString().c_str()]
		)
		{
		    result = new ExprAssignment(lhs,rhs);
		}
	;

predicate[ObjectType* objType]
@init {
    InterpretedTokenFactory* tokenFactory;
    std::string predName;
}
	:	^('predicate'
			pred=IDENT 
			{ 
			    predName = objType->getName().toString() + "." + c_str($pred.text->chars);   
			    tokenFactory = new InterpretedTokenFactory(predName,objType->getId()); 
			}
			predicateStatements[tokenFactory]
		)
		{
                    objType->addTokenFactory(tokenFactory->getId());
		}
	;

// TODO: allow assignments to inherited parameters
predicateStatements[InterpretedTokenFactory* tokenFactory]
	:	^('{'
		      (
		        ( child=predicateParameter[tokenFactory] 
                        | child=predicateParameterAssignment 
			| child=standardConstraint 
			)
			{
			    tokenFactory->addBodyExpr(child);
			}
		      )*
		)
	;
	
// Note: Allocations are not legal here.        
predicateParameter[InterpretedTokenFactory* tokenFactory] returns [Expr* result]
        :
        child=variableDeclarations 
        { 
            const std::vector<Expr*>& vars=child->getChildren();
            for (unsigned int i=0;i<vars.size();i++) {
                ExprVarDeclaration* vd = dynamic_cast<ExprVarDeclaration*>(vars[i]);
                tokenFactory->addArg(vd->getType()->getName(),vd->getName());
            }
            result = child;            
        }
        ;       

predicateParameterAssignment returns [Expr* result]
        :
        child=assignment 
        { 
            result = child; 
        }
        ;       

standardConstraint returns [Expr* result]
        :
        child = constraintInstantiation 
        { 
            result = child; 
        }
        ;	

rule returns [Expr* result]
@init {
    std::vector<Expr*> ruleBody;
}
 	:	^('::'
			className=IDENT
			predicateName=IDENT
			ruleBlock[ruleBody]
		)
		{
		    std::string predName = std::string(c_str($className.text->chars)) + "." + std::string(c_str($predicateName.text->chars));
		    std::string source=""; // TODO: get this from the antlr parser
		    result = new ExprRuleTypeDefinition((new InterpretedRuleFactory(predName,source,ruleBody))->getId());
		}
	;

ruleBlock[std::vector<Expr*>& ruleBody]
	:	^('{'
			(child=ruleStatement { ruleBody.push_back(child); })*
		)
	;

ruleStatement returns [Expr* result]
	: (	child=constraintInstantiation
	  |	child=assignment
	  |	child=variableDeclarations
	  |	child=ifStatement
	  |	child=loopStatement
	  |     child=relation
	  )
	  {
	      result = child;
	  }
	;


ifStatement returns [Expr* result]
@init {
std::vector<Expr*> ifBody;
std::vector<Expr*> elseBody;
}
	:	^('if'
			guard=guardExpression
			ruleBlock[ifBody]
			ruleBlock[elseBody]?
		)
		{
		    result = new ExprIf(guard,ifBody,elseBody);
		}
  ;

// TODO: perform systematic cleanup of lhs, rhs and constant exprs throughout
guardLhs returns [Expr* result]
        :    (child=anyValue | child=qualified)
             {
                 result = child;
             } 
        ;
        
guardExpression returns [ExprIfGuard* result]
@init
{
    const char* relopStr = "==";
}
        : ( ^(relop=guardRelop {relopStr=c_str($relop.text->chars);} lhs=guardLhs rhs=anyValue )
          | lhs=anyValue
          )
          {
              result = new ExprIfGuard(relopStr,lhs,rhs);
          }
        ;

guardRelop 
    : '==' | '!='
;

loopStatement returns [Expr* result]
@init {
std::vector<Expr*> loopBody;
}
	:	^('foreach'
			name=IDENT
			val=qualified 
			ruleBlock[loopBody]
		)
		{
		    result = new ExprLoop(c_str($name.text->chars),val->toString().c_str(),loopBody); // TODO : modify ExprLoop to pass val Expr instead
		}
	;

problemStmt returns [Expr* result] 
@init {
    std::vector<PredicateInstanceRef*> tokens;    
}

        :       ^(t=problemStmtType predicateInstanceList[tokens])
                {
                    result = new ExprProblemStmt(c_str($t.text->chars),tokens);
                }   
        ;
        
problemStmtType
        :       'goal' 
        |       'rejectable'
        |       'fact'
        ;
        
relation returns [Expr* result]
@init {
    const char* relationType=NULL;
    PredicateInstanceRef* source=NULL;
    std::vector<PredicateInstanceRef*> targets;    
}
	:	^(TOKEN_RELATION
			(i=IDENT { source = new PredicateInstanceRef(NULL,c_str($i.text->chars)); })?
			tr=temporalRelation { relationType = c_str($tr.text->chars); } 
			predicateInstanceList[targets]
		)
		{		   
		    result = new ExprRelation(relationType,source,targets);
		}
	;

predicateInstanceList[std::vector<PredicateInstanceRef*>& instances]
	:	^('('
			(child=predicateInstance { instances.push_back(child); })*
		)
		|	i=IDENT 
		        { instances.push_back(new PredicateInstanceRef(NULL,c_str($i.text->chars))); } // TODO: check predicate type and pass it along
	;

predicateInstance returns [PredicateInstanceRef* pi]
@init {
    const char* name = NULL;
}
	:	^(PREDICATE_INSTANCE qt=qualifiedToken (i=IDENT { name = c_str($i.text->chars); })?)
	        {
	            pi = new PredicateInstanceRef(qt->toString().c_str(),name);
	            delete qt;
	        }
	;
	
qualified returns [Expr* result]
@init {
    std::string varName;
}
        :  (      name=identifier { varName=c_str($name.text->chars); }                 
           |       ^('.' prefix=qualified 
                         { 
                             // TODO: Hack!, this is brittle
                             varName = prefix->toString(); 
                             delete prefix; 
                         } 
                         (q=IDENT { varName += std::string(".") + c_str($q.text->chars); })+)
           )
           {
               // TODO!!: do type checking at each "."
              if (CTX->SymbolTable->isEnumValue(varName.c_str()))
                  result = CTX->SymbolTable->makeEnumRef(varName.c_str());
               else  
                   result = new ExprVarRef(varName.c_str());
           }
        ;
        
qualifiedToken returns [Expr* result]
        :       e=qualified
                {
                    // TODO: type checking !
                    result = e;
                }
        ;
        
temporalRelation
        :       'after'
        |       'any'
        |       'before'
        |       'contained_by'
        |       'contains'
        |       'contains_end'
        |       'contains_start'
        |       'ends'
        |       'ends_after'
        |       'ends_after_start'
        |       'ends_before'
        |       'ends_during'
        |       'equal'
        |       'equals'
        |       'meets'
        |       'met_by'
        |       'parallels'
        |       'paralleled_by'
        |       'starts'
        |       'starts_after'
        |       'starts_before'
        |       'starts_before_end'
        |       'starts_during'
        ;
 
// TODO: this is ugly, need to provide extensible method exporting mechanism  
methodInvocation returns [Expr* result]
	:
	(	child=variableMethod
	|       child=objectMethod
        |       child=tokenMethod
        )
        {
            result = child;
        }
	;

variableMethod returns [Expr* result]
@init {
    std::vector<Expr*> args;
}
        :       ^(METHOD_CALL op=variableOp v=qualified? variableArgumentList[args]?)
                {
                    result = new ExprVariableMethod(c_str($op.text->chars),v,args);
                }
        ;
   
variableOp
        :       'specify'
        |       'reset'
        |       'close'
        ;
                
objectMethod returns [Expr* result]
@init {
    std::vector<Expr*> args;
}
        :       ^(METHOD_CALL op=objectOp obj=qualified? variableArgumentList[args]?)
                {
                    result = new ExprObjectMethod(c_str($op.text->chars),obj,args);
                }
        ;
   
objectOp
        :       'constrain'
        |       'free'
        ;
                
tokenMethod returns [Expr* result]    
@init {
    std::vector<Expr*> args;
}
        :       ^(METHOD_CALL op=tokenOp tok=IDENT variableArgumentList[args]?)
                {
                    result = new ExprTokenMethod(c_str($op.text->chars),c_str($tok.text->chars),args); 
                }
        ;

tokenOp
        :       'activate'
        |       'merge'
        |       'reject'
        |       'cancel'
        ;        
        
// This is here only for backwards compatibility, must be dropped eventually        
constraintSignature returns [Expr* result]
        :       ^('constraint' name=IDENT typeArgumentList constraintExtendsClause? signatureBlock?)
        {
            debugMsg("NddlInterpreter","Ignored constraint declaration for " << c_str($name.text->chars) 
                                        << ". Constraint Signatures are ignored in nddl3.");
            result = NULL;
        }
        ;
        
constraintExtendsClause
        :       ^('extends' IDENT typeArgumentList)
        ;        
        
typeArgumentList
        :       ^('(' IDENT*)
        ;

signatureBlock
        :       ^('{' signatureExpression*)
        ;

signatureExpression
        :       ^(('&&' | '||') signatureAtom signatureAtom)
        ;

signatureAtom
        :       ^('<:' IDENT  (type | 'numeric'))
        |       ^('(' signatureExpression)
        ;
