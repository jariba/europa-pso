
header "post_include_hpp" {
#include "Debug.hh"
#include "antlr/ASTFactory.hpp"
#include "ANMLTranslator.hh"

}

// run antlr.Tool on this file to generate a tree parser
options {
	language="Cpp";
}

{
	ANML2NDDL::ANML2NDDL(ANML::ANMLTranslator translator)
		: antlr::TreeParser()
		, m_translator(translator)
    {
	}

	ANML2NDDL::ANML2NDDL() 
		: antlr::TreeParser()
		, m_translator()
    {
	}
  /**
   * Custom traceIn for Antlr which uses debugMsg.
   */
  void ANML2NDDL::traceIn(const char* rname, antlr::RefAST t) {
		int indentsize = LONG_RULE_SIZE-strlen(rname)+traceDepth+2;
    char indent[indentsize+1];
		for(int i=0; i < indentsize; ++i)
			indent[i] = ' ';
		indent[LONG_RULE_SIZE-strlen(rname)+1] = '|';
		indent[indentsize] = '\0';
    ++traceDepth;

		debugMsg((std::string("ANML2NDDL:traceIn:")+rname).c_str(), indent << "> " << rname << 
						 ((inputState->guessing > 0)? "; [guessing]" : ";") <<
						 " LA==" << ((t == antlr::nullAST)? "null" : t->getText()));
  }

  /**
   * Custom traceOut for Antlr which uses debugMsg.
   */
  void ANML2NDDL::traceOut(const char* rname, antlr::RefAST t) {
    --traceDepth;
		int indentsize = LONG_RULE_SIZE-strlen(rname)+traceDepth+1;
    char indent[indentsize+1];
		for(int i=0; i < indentsize; ++i)
			indent[i] = ' ';
		indent[LONG_RULE_SIZE-strlen(rname)] = '|';
		indent[indentsize] = '\0';

		debugMsg((std::string("ANML2NDDL:traceOut:")+rname).c_str(),
		         indent << "< " << rname << ((inputState->guessing > 0)? "; [guessing]" : ";") <<
						 " LA==" << ((t == antlr::nullAST)? "null" : t->getText()));
  }
}

class ANML2NDDL extends TreeParser;

options {
  importVocab = ANML;
	noConstructors = true;
}

{
	#define LONG_RULE_SIZE 26
	public:
		ANML2NDDL(ANML::ANMLTranslator translator);
		ANML2NDDL();
		
		ANML::ANMLTranslator& getTranslator() { return m_translator; }
		
	protected:
		void traceIn(const char* rname, antlr::RefAST t);
		void traceOut(const char* rname, antlr::RefAST t);

        ANML::ANMLTranslator m_translator;
}

anml
{
	ANML::ANMLElement* stmt;
}
  : #(ANML
	    {
           debugMsg("ANML2NDDL:anml", "Parsing...");
        }
      (stmt=anml_stmt { m_translator.getContext().addElement(stmt); debugMsg("ANML2NDDL:anml", "Added Element " << stmt->getType() << " " << stmt->getName());})*
	    {
	       debugMsg("ANML2NDDL:anml", "Done parsing");
	    }
    )
  ;

anml_stmt returns [ANML::ANMLElement* element]
    : element=declaration
    | element=problem_stmt
    | element=transition_constraint
    | element=action_def            // <- implicit global object
;

declaration returns [ANML::ANMLElement* element]
    : element=objtype_decl
    | element=vartype_decl
    | element=var_obj_declaration
    | (#(FUNCTION IDENT LPAREN)) => element=predicate_declaration
    | element=function_declaration
;

problem_stmt returns [ANML::ANMLElement* element]
    : element=fact
    | element=goal
;
   
vartype_decl returns [ANML::ANMLElement* element]
{
	std::string name;
	ANML::Type* t;
}
    : #(VARTYPE name=user_defined_type_name t=var_type[name])
{
	element = t;
}    
;

var_obj_declaration! returns [ANML::ANMLElement* element]
{
	ANML::Type* type;
	std::vector<ANML::VarInit*> varInit;
}    
    : #(VARIABLE type=var_type varInit=var_init_list[type])
{
    element = new ANML::VarDeclaration(*type,varInit);	
}    
;

var_init_list[ANML::Type* type] returns [std::vector<ANML::VarInit*> varInit;]
{
	ANML::VarInit* vi;
}
	: (vi=var_init[type] { varInit.push_back(vi); })+
;

var_type[const std::string& name=""] returns [ANML::Type* t]
{
	std::vector<std::string> values;
	std::vector<ANML::Expr*> rangeValues;
	ANML::ANMLContext& context = m_translator.getContext();
	bool newType=false;
    std::vector<ANML::Variable*> vb;	
}
    : (
    BOOL { t = context.getType("bool"); }
    | s:IDENTIFIER { t = context.getType(s->getText(),true); }
    | #(INT { t = context.getType("int"); } 
         (  rangeValues=range { t = new ANML::Range(name,*t,rangeValues[0]->toString(),rangeValues[1]->toString()); newType=true; } 
           |values=enum_body  { t = new ANML::Enumeration(name,*t,values); newType=true; }
         )?
      )
	| #(FLOAT { t = context.getType("float"); }
	     (  rangeValues=range  { t = new ANML::Range(name,*t,rangeValues[0]->toString(),rangeValues[1]->toString()); newType=true; }
	       |values=enum_body   { t = new ANML::Enumeration(name,*t,values); newType=true; }
	     )?
	  )
    | #(STRING { t = context.getType("string"); }
         (values=enum_body { t = new ANML::Enumeration(name,*t,values); newType=true;}
         )?
      )
    | #(VECTOR vb=vector_body)   
       { 
           ANML::ObjType* vectorType = new ANML::ObjType(name,(ANML::ObjType*)ANML::Type::OBJECT); newType=true; 
           for (unsigned int i=0; i<vb.size(); i++) {
               vectorType->addVariable(vb[i]);
               std::vector<ANML::VarInit*> init;
               init.push_back(new ANML::VarInit(vb[i],""));
               vectorType->addElement(new ANML::VarDeclaration(vb[i]->getDataType(),init));
           }
              
           t = vectorType;    
       }
    )
{
	if (newType) {
		context.addType(t);
	}
	else {
		if (name != "") {
			t = new ANML::TypeAlias(name,*t);
			context.addType(t);
		}
	}
}    
;

enum_body returns [std::vector<std::string> values]
{
	ANML::Expr* e;
	// TODO: validate values (flag  duplicates, type checking)
}
    : #(LCURLY (e=constant { values.push_back(e->toString()); })+)
;

vector_body returns [std::vector<ANML::Variable*> attrs;]
    : attrs=parameters
;

range returns [std::vector<ANML::Expr*> values]
{
	ANML::Expr *lb,*ub;
}
    : #(LBRACK lb=signed_literal ub=signed_literal)
{
	// TODO: validate range, do type checking
	values.push_back(lb);
	values.push_back(ub);	
}    
;

var_init[ANML::Type* type] returns [ANML::VarInit* vi]
{
	std::string name;
	ANML::Expr* value=NULL;
}
    : #(EQUAL name=var_name (value=constant)?)
	| name=var_name
{
	if (value!=NULL && !type->isAssignableFrom(value->getDataType()))
	       check_runtime_error(ALWAYS_FAIL,"Can't initialize " + type->getName() + " with " + value->getDataType().getName());
	    
	ANML::Variable* v = new ANML::Variable(*type,name);
    m_translator.getContext().addVariable(v);
    // TODO : pass along expr?
	vi = new ANML::VarInit(v,(value !=NULL ? value->toString() : ""));
}	
;

parameters returns [std::vector<ANML::Variable*> params;]
{
	ANML::Variable* p;
}
    : #(LPAREN (p=parameter_decl { params.push_back(p); })*)
;

parameter_decl! returns [ANML::Variable* v]
{
	ANML::Type* type;
}
    : #(PARAMETER type=var_type name:var_name)
    {
    	v = new ANML::Variable(*type,name->getText());
    }
;

objtype_decl! returns [ANML::ANMLElement* element]
{
	ANML::ObjType* newType;
	ANML::ANMLElement* childElement;
}
    : #(OBJTYPE #(objtypeName:IDENTIFIER parent:(IDENTIFIER | OBJECT)?)
      {
   		std::string parentType("object");
   		if (parent.get() != NULL)
   		    parentType = parent->getText();
       	newType = m_translator.getContext().addObjType(objtypeName->getText(),parentType);
       	m_translator.pushContext(newType);
      }
        #(LCURLY (childElement=objtype_body_stmt { newType->addElement(childElement); } )*))
      {
      	element = newType;
        m_translator.popContext();      	
      }
;

objtype_body_stmt returns [ANML::ANMLElement* element=NULL]
    : element=declaration
    | element=action_def
    | element=transition_constraint
;

function_declaration returns [ANML::ANMLElement* element]
    : #(FUNCTION var_type (function_signature)+)
;

function_signature 
    : function_symbol parameters
;

predicate_declaration returns [ANML::ANMLElement* element]
    : #(FUNCTION (function_signature)+)
;

fact returns [ANML::ANMLElement* element]
{
	ANML::Proposition* p;
    std::vector<ANML::Proposition*> propositions;	
}    
    : #(FACT 
       (p=effect_proposition { propositions.push_back(p); } 
        | propositions=effect_proposition_list))
{
    element = new ANML::Fact(propositions);	
}    
;

goal returns [ANML::Goal* element]
{
	ANML::Proposition* p;
    std::vector<ANML::Proposition*> propositions;	
}    
    : #(GOAL 
       (p=condition_proposition { propositions.push_back(p); } 
        | propositions=condition_proposition_list))
{
    element = new ANML::Goal(propositions);	
}    
;

effect_proposition returns [ANML::Proposition* p]
    : p=proposition
;

effect_proposition_list returns [std::vector<ANML::Proposition*> l]
{
    ANML::Proposition* p;
}
    : #(LCURLY (p=effect_proposition { l.push_back(p); })+)
;

condition_proposition returns [ANML::Proposition* p]
    : p=proposition
;

condition_proposition_list returns [std::vector<ANML::Proposition*> l]
{
    ANML::Proposition* p;
}
    : #(LCURLY (p=condition_proposition { l.push_back(p); })+)
;

// TODO : FROM and FOR branches to be implemented later
proposition! returns [ANML::Proposition* p;]
    : p=qualif_fluent
    | #(WHEN { check_runtime_error(false, "WHEN not supported yet"); } 
        #(LCURLY condition_proposition) #(LCURLY effect_proposition))
    | #(FROM { check_runtime_error(false, "FROM not supported yet"); } 
        time_pt qualif_fluent_list)
    | #(FOR { check_runtime_error(false, "FOR not supported yet"); } 
        object_name #(LCURLY proposition))    
;

qualif_fluent returns [ANML::Proposition* p;]
{
	ANML::TemporalQualifier* tq;
	std::vector<ANML::Fluent*> fluents;
}
    : #(FLUENT tq=temporal_qualif fluents=fluent_list)
{
	p = new ANML::Proposition(tq,fluents);
}    
;

qualif_fluent_list 
    : #(LCURLY (qualif_fluent)+)
;

fluent_list returns [std::vector<ANML::Fluent*> fluents]
{
	ANML::Fluent* f=NULL;
} 
    : #(LCURLY (f=fluent { check_error(f!=NULL,"Can't have NULL fluents"); fluents.push_back(f); })+)
;

fluent returns [ANML::Fluent* f]
{
	ANML::Fluent *lhs,*rhs;
}
    : #(LPAREN f=fluent)
	| #(OR  lhs=fluent rhs=fluent)  { f = new ANML::CompositeFluent("and",lhs,rhs); }
	| #(AND lhs=fluent rhs=fluent)  { f = new ANML::CompositeFluent("or",lhs,rhs); }
	| f=relational_fluent 
	| f=constraint
;

free_vars_decl returns [ANML::ANMLElement* element]
{
	std::vector<ANML::Variable*> vars;
}
    : #(VARIABLES  vars=var_list)
{
	element = new ANML::FreeVarDeclaration(vars);
}
;

var_list returns [std::vector<ANML::Variable*> vars]
{
	ANML::Type* vt;
	std::string vn;
}
    : #(LPAREN 
           (vt=var_type vn=var_name 
               {
                   ANML::Variable* v = new ANML::Variable(*vt,vn);
                   m_translator.getContext().addVariable(v);
               }
           )+
       )    
;

// NOTE: if the rhs is not present, that means we're stating a predicate to be true
relational_fluent returns [ANML::RelationalFluent* f]
{
	ANML::LHSExpr* lhs=NULL;
	ANML::Expr*    rhs=NULL;
}
    : 
    (
        #(EQUAL lhs=lhs_expr rhs=expr)
    	| lhs=lhs_expr 
    )
{
	// TODO: do type checking for lhs and rhs	
	//if (rhs!=NULL && !lhs->getDataType().isAssignableFrom(rhs->getDataType()))
	//    check_runtime_error(ALWAYS_FAIL,type->getName() + " and " + value->getDataType().getName() + " are incompatible types");	
	//else if (!lhs->getDataType() != ANML::Type::BOOL) // TODO: if rhs is absent, lhs must be a predicate 
	//    check_runtime_error(ALWAYS_FAIL,"Only predicates can be stated without a right hand side expr. the following is not a predicate:" + lhs->toString());
	
	f = new ANML::RelationalFluent(lhs,rhs);
}	
;

// NOTE: removed start(fluent), end(fluent) from the grammar, it has to be taken care of by either functions or dot notation
lhs_expr returns [ANML::LHSExpr* p;]
{
	std::string name;
	std::vector<ANML::Expr*> args;
}
    : #(FUNCTION 
          name=function_symbol // This can be an action, a function or a predicate
          {
          	ANML::Variable* v;
          	ANML::Action* a;
          	
            if ((v=m_translator.getContext().getVariable(name)) != NULL)
                p = new ANML::LHSVariable(v,name);
          	else if ((a=m_translator.getContext().getAction(name)) != NULL)
              	p = new ANML::LHSAction(a,name);
            else
                check_runtime_error(false,std::string("No function, action or predicate called ") + name + " in scope");
          }
          args=arguments
          {
          	if (name == "PlannerConfig" || name == "PlanningHorizon") {
          		ANML::LHSPlannerConfig* p1 = m_translator.getPlannerConfig();
          		p1->setArgs(name,args);
          		p = p1;
          	}
          	else {          	
             	p->setArgs(args);
          	}
          }
      ) 
    | p=qualified_var_name[m_translator.getContext(),""] 
;

// TODO: we should allow for full-blown expressions (logical and numerical) at some point
expr returns [ANML::Expr* e]
{
	std::vector<ANML::Expr*> vectorValues;
} 
    : e=constant 
	| vectorValues=arguments { e = new ANML::ExprVector(vectorValues); }
    | e=lhs_expr
;

// TODO: For effects and facts:
// - Temporal qualifiers IN, BEFORE, AFTER and CONTAINS are not allowed. 
temporal_qualif returns [ANML::TemporalQualifier* tq;]
{
	std::string op;
	ANML::Expr* arg;
	std::vector<ANML::Expr*> args;
}
    : (
      #(AT arg=time_pt) { op="at"; args.push_back(arg); } 
      
    | #(OVER args=interval)  { op="over"; } 
    
    | #(IN args=interval (arg=numeric_expr { args.push_back(arg); })?) { op="in"; }
    
    | #(AFTER arg=time_pt { args.push_back(arg); }
           (arg=numeric_expr { args.push_back(arg); })?) { op="after"; }
           
    | #(BEFORE arg=time_pt 
          (arg=numeric_expr { args.push_back(arg); })?) { op="before"; }
    
    | #(CONTAINS args=interval) { op="contains"; }
    )
{
	tq = new ANML::TemporalQualifier(op,args);
}    
;

// all(action) is shorthand for the interval [action.start,action.end]
interval returns [std::vector<ANML::Expr*> bounds;]
{
	ANML::Expr* b;
	std::string label;
}
    : #(ALL (label=action_label_arg { label += "."; })?
           {           	
               bounds.push_back(new ANML::ExprConstant(*ANML::Type::INT,label+"start"));
               bounds.push_back(new ANML::ExprConstant(*ANML::Type::INT,label+"end"));
           } 
       )
    | #(LBRACK 
           b=time_pt { bounds.push_back(b); }
           b=time_pt { bounds.push_back(b); }
       )
;

action_label_arg returns [std::string s]
    : #(LPAREN s=action_instance_label)
;

time_pt returns [ANML::Expr* expr]
    : expr=numeric_expr
;

numeric_expr returns [ANML::Expr* expr]
{
	ANML::Expr  *op1,*op2;
}
    :     (#(PLUS numeric_expr numeric_expr)) => #(PLUS op1=numeric_expr op2=numeric_expr)
		        { expr = new ANML::ExprArithOp("+",op1,op2); }
		| (#(MINUS numeric_expr numeric_expr)) => #(MINUS op1=numeric_expr op2=numeric_expr) 
		        { expr = new ANML::ExprArithOp("-",op1,op2); }
		| #(MULT op1=numeric_expr op2=numeric_expr)
		        { expr = new ANML::ExprArithOp("*",op1,op2); }
		| #(DIV op1=numeric_expr op2=numeric_expr)
		        { expr = new ANML::ExprArithOp("/",op1,op2); }
		| #(LPAREN expr=numeric_expr)
		| #(PLUS expr=numeric_expr) 
		| #(MINUS expr=numeric_expr) { /* TODO: apply minus!!*/ }
		| expr=numeric_literal 
		| expr=lhs_expr
;

arguments returns [std::vector<ANML::Expr*> args;]
    : #(LPAREN (args=arg_list)?)
;

arg_list returns [std::vector<ANML::Expr*> args;]
{
	ANML::Expr* arg;
}
    : (arg=expr { args.push_back(arg); })+
;

action_def returns [ANML::ANMLElement* element]
{
	std::vector<ANML::ANMLElement*> body;
	std::vector<ANML::Variable*> params;
    ANML::Action* a;
}
    : #(ACTION name:action_symbol params=parameters 
        {
            a = new ANML::Action((ANML::ObjType&)(m_translator.getContext()),name->getText(),params);
            m_translator.getContext().addAction(a); // TODO: add at the end so that errors are handled gracefully
            m_translator.pushContext(a);
            
            for (unsigned int i=0; i<params.size();i++)
                m_translator.getContext().addVariable(params[i]);
        }    
        body=action_body
      )
    {
      a->setBody(body);	
      element = a;
      m_translator.popContext();
    }
;

action_body returns [std::vector<ANML::ANMLElement*> body] 
{
	ANML::ANMLElement* stmt;
}
    : #(LCURLY (stmt=action_body_stmt { body.push_back(stmt); })*)
;

action_body_stmt returns [ANML::ANMLElement* element]
    : element=duration_stmt 
    | element=free_vars_decl
    | element=condition_stmt
    | element=effect_stmt 
    | element=change_stmt 
    | element=decomp_stmt 
    // TODO: constraints?
;

// TODO: semantic layer to enforce that only one duration statement is allowed    
duration_stmt returns [ANML::ANMLElement* element]
{
    std::vector<ANML::Expr*> values;
    ANML::Expr* v;
}    
    : #(DURATION (v=numeric_expr { values.push_back(v); } | values=range))
{
    element = new ANML::ActionDuration(values);	
}    
;

condition_stmt returns [ANML::ANMLElement* element]
{
    std::vector<ANML::Proposition*> propositions;
    ANML::Proposition* p;
}    
    : #(CONDITION (
         p=condition_proposition { propositions.push_back(p); }
         | propositions=condition_proposition_list))
{
    element = new ANML::Condition(propositions);	
}    
;
    
effect_stmt returns [ANML::ANMLElement* element]
{
    std::vector<ANML::Proposition*> propositions;
    ANML::Proposition* p;
}    
    : #(EFFECT (
         p=effect_proposition { propositions.push_back(p); }
         | propositions=effect_proposition_list))
{
    element = new ANML::Effect(propositions);	
}    
;

change_stmt returns [ANML::ANMLElement* element]
    : #(CHANGE (change_proposition | change_proposition_list))
{
	element = new ANML::Change();
}    
;

change_proposition_list
    : #(LCURLY (change_proposition)+)
; 

change_proposition
    : #(FLUENT temporal_qualif change_fluent)
    | #(WHEN condition_proposition change_proposition)
;

change_fluent 
    : #(AND change_fluent change_fluent) 
    | #(LPAREN change_fluent)
    | resource_change
    | transition_change
;

resource_change 
    : #(CONSUMES var_name (numeric_expr)?)
    | #(PRODUCES var_name (numeric_expr)?)
    | #(USES var_name (numeric_expr)?)
;

transition_change
    : #(EQUAL qualified_var_name[m_translator.getContext(),""] directed_expr_list)
;

directed_expr_list
    : #(ARROW directed_expr_list directed_expr_list)
		| expr
;

decomp_stmt returns [ANML::ANMLElement* element]
    : #(DECOMPOSITION (decomp_step | #(LCURLY (decomp_step)+)))
{
	element = new ANML::Decomposition();
}    
;

decomp_step
    : #(ACTIONS temporal_qualif action_set)
		| constraint
;

action_set
    : #(ORDERED action_set_element_list)
    | #(UNORDERED action_set_element_list)
    | #(DISJUNCTION action_set_element_list)
;

action_set_element_list
    : #(LPAREN (action_set_element)+)
;

action_set_element
    : #(ACTION qualified_action_symbol arguments (action_instance_label)?)
    | action_set
;

qualified_action_symbol
    : (#(IDENTIFIER DOT))=> #(IDENTIFIER (#(DOT action_symbol))?)
		| action_symbol
;

constraint returns [ANML::Constraint* element]
{
	std::string name;
	std::vector<ANML::Expr*> args;
}
    : #(CONSTRAINT name=constraint_symbol args=arguments)
{
	std::vector<const ANML::Type*> argTypes;
	for (unsigned int i=0;i<args.size();i++)
	    argTypes.push_back(&(args[i]->getDataType()));// TODO: eliminate this de-reference
	
	// Make sure that the constraint exists    
	m_translator.getContext().getConstraint(name,argTypes,true);
	element = new ANML::Constraint(name,args);
}    
;

transition_constraint returns [ANML::ANMLElement* element]
    : #(TRANSITION var_name trans_pair_list)
;

trans_pair_list
    : #(LCURLY (trans_pair)+)
;

trans_pair 
    : #(ARROW constant (constant | #(LCURLY (constant)+)))
;

unsigned_constant returns [ANML::Expr* e]
    : e=numeric_literal 
    | e=string_literal 
    | e=bool_literal
;

constant returns [ANML::Expr* e]
    : e=signed_literal 
    | e=string_literal 
    | e=bool_literal
;

signed_literal returns [ANML::Expr* e]
    : e=numeric_literal         
	| #(MINUS e=numeric_literal)  { /* TODO!!: append - to numeric literal */  }
;

// TODO: What about floats??
numeric_literal returns [ANML::Expr* e]
    : t1:NUMERIC_LIT  { e = new ANML::ExprConstant(*ANML::Type::INT,t1->getText()); }
    | t2:INF          { e = new ANML::ExprConstant(*ANML::Type::BOOL,t2->getText()); }
;

bool_literal returns [ANML::Expr* e]
    : t1:TRUE  { e = new ANML::ExprConstant(*ANML::Type::BOOL,t1->getText()); }
    | t2:FALSE { e = new ANML::ExprConstant(*ANML::Type::BOOL,t2->getText()); }
;

string_literal returns [ANML::Expr* e]
    : t:STRING_LIT { e = new ANML::ExprConstant(*ANML::Type::STRING,t->getText()); }
;

action_symbol         returns [std::string s]  : i:IDENTIFIER { s = i->getText(); };
action_instance_label returns [std::string s]  : i:IDENTIFIER { s = i->getText(); };
constraint_symbol     returns [std::string s]  : i:IDENTIFIER { s = i->getText(); };
function_symbol       returns [std::string s]  : i:IDENTIFIER { s = i->getText(); };

object_name           returns [std::string s]  : i:IDENTIFIER { s = i->getText(); };

var_name returns [std::string s]               
    : t1:IDENTIFIER { s = t1->getText(); } 
    | t2:START      { s = t2->getText(); } 
    | t3:END        { s = t3->getText(); }
;

// TODO: validate var_names
qualified_var_name [ANML::ANMLContext& context,const std::string& path] returns [ANML::LHSExpr* expr] 
{
	std::string s;
	std::string newPath;
	ANML::ANMLContext* newContext=NULL;
}    
    : #(DOT 
           s=var_name 
           { 
           	   newPath = (path=="" ? s : path+"."+s); 
               ANML::Variable* v;
               if ((v=context.getVariable(s)) != NULL)
                  newContext = m_translator.getContext().getObjType(v->getDataType().getName());
               else  	
                  check_runtime_error(false,"Variable " + s + " has not been defined in " + context.getContextDesc());  	
           }
           expr=qualified_var_name[*newContext,newPath]
         ) 
      | s=var_name
        {
            ANML::Variable* v;
          	ANML::Action* a;
          	
          	newPath = (path=="" ? s : path+"."+s); 
            if ((v=context.getVariable(s)) != NULL)
                expr = new ANML::LHSVariable(v,newPath);
          	else if ((a=context.getAction(s)) != NULL)
              	expr = new ANML::LHSAction(a,newPath);        	
            else  	
                check_runtime_error(false,s + " has not been defined in " + context.getContextDesc());  	
        }
;


user_defined_type_name returns [std::string s] : i:IDENTIFIER { s = i->getText(); };
