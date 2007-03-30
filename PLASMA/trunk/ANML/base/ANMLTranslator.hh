#ifndef ANMLTRANSLATOR_H_
#define ANMLTRANSLATOR_H_

#include <map>
#include <string>
#include <vector>

namespace ANML
{

class Action;
class ANMLContext;
class ANMLElement;
class ConstraintDef;
class Expr;
class Fluent;
class LHSExpr;
class LHSPlannerConfig;
class Proposition;
class ObjType;
class TemporalQualifier;
class Type;
class Variable;

class ANMLTranslator
{
  public:
      ANMLTranslator();
      virtual ~ANMLTranslator();
      
      virtual void pushContext(ANMLContext* context);
      virtual void popContext();      

      virtual ANMLContext& getContext() { return *m_context; }
      
      virtual LHSPlannerConfig* getPlannerConfig() { return m_plannerConfig; }
            
      virtual void toNDDL(std::ostream& os) const;
      
	  virtual std::string toString() const;      
      
  protected:
      ANMLContext* m_context;
      LHSPlannerConfig* m_plannerConfig;
      
      ANMLContext* createGlobalContext();          
};

class ANMLContext
{
  public:
	ANMLContext(const ANMLContext* parent=NULL);
	virtual ~ANMLContext();

    virtual std::string getContextDesc() const { return "Global Context"; }

    virtual const ANMLContext* getParentContext() { return m_parentContext; }
    virtual void setParentContext(const ANMLContext* parent) { m_parentContext = parent; }
    	
    virtual void addElement(ANMLElement* element);
    virtual const std::vector<ANMLElement*>& getElements() const { return m_elements; }
    
	virtual void     addType(Type* type);
	virtual ObjType* addObjType(const std::string& name,const std::string& parentName);

	virtual Type*    getType(const std::string& name,bool mustExist=false) const;
	virtual ObjType* getObjType(const std::string& name) const;
	
	virtual void addAction(Action* a);
	virtual Action* getAction(const std::string& name,bool mustExist=false) const;
	
	// Variables, Functions and Predicates are included here, 
	// a function is just a var with args, a predicate is just a function on the boolean domain
	virtual void addVariable(Variable* v);
	virtual Variable* getVariable(const std::string& name,bool mustExist=false) const;

    virtual void addConstraint(ConstraintDef* c);
    virtual ConstraintDef* getConstraint(const std::string& name,const std::vector<const Type*>& argTypes,bool mustExist=false) const;
    	
    virtual void toNDDL(std::ostream& os) const;
    
	virtual std::string toString() const;
		
  protected:
    const ANMLContext*        m_parentContext;
    std::vector<ANMLElement*> m_elements;              

    // maps to quickly get to elements by name
    std::map<std::string,Type*>      m_types;    
    std::map<std::string,Action*>    m_actions;
    std::map<std::string,Variable*>  m_variables;
    
    // map to get to constraints by name and signature
    std::map<std::string,ConstraintDef*>  m_constraints;        
};

class ConstraintDef 
{
  public:
    ConstraintDef(const std::string& name, const std::vector<const Type*>& argTypes) : m_name(name), m_argTypes(argTypes) {}   

    const std::string& getName() const { return m_name; }
    const std::vector<const Type*>& getArgTypes() const { return m_argTypes; }
    
  protected:
	std::string m_name;
	std::vector<const Type*> m_argTypes;
};

class ANMLElement
{
  public:
    ANMLElement(const std::string& type) : m_type(type) {}
    ANMLElement(const std::string& type,const std::string& name) : m_type(type), m_name(name) {}
    virtual ~ANMLElement() {}    

    virtual const std::string& getType() { return m_type; }
    virtual const std::string& getName() const { return m_name; }
    virtual void setName(const std::string& n) { m_name = n; }
    virtual void toNDDL(std::ostream& os) const;
    virtual std::string toString() const;
    
  protected:    
    std::string m_type;
    std::string m_name;	  
};

class ANMLElementList : public ANMLElement
{
  public:
    ANMLElementList() : ANMLElement("Element list") {}
    
    void add(ANMLElement* e) { m_elements.push_back(e); }
    
    virtual void toNDDL(std::ostream& os) const
    {
    	for (unsigned int i=0;i<m_elements.size();i++)
    	    m_elements[i]->toNDDL(os);
    }
    
  protected:
    std::vector<ANMLElement*> m_elements;  	
};

class ObjType;

class Type : public ANMLElement
{
  public:
	Type(const std::string& name);
	virtual ~Type();
	    
	virtual bool isPrimitive() const { return true; }
	
	virtual bool canBeResourceType() const { return false; }
    virtual bool isResourceType() const { return false; }	
	virtual void becomeResourceType();
	
	virtual bool isAssignableFrom(const Type& rhs) const { return getName() == rhs.getName(); }
	    
    virtual void toNDDL(std::ostream& os) const { os << m_name; }
    
    static Type* VOID;    
    static Type* BOOL;    
    static Type* INT;    
    static Type* FLOAT;    
    static Type* STRING;    
    static ObjType* OBJECT;    
};

class TypeAlias : public Type
{
  public:
	TypeAlias(const std::string& name,const Type& t);
	virtual ~TypeAlias();
	    
	virtual bool isPrimitive() const { return m_wrappedType.isPrimitive(); }
	    
    virtual void toNDDL(std::ostream& os) const;    

  protected:    
    const Type& m_wrappedType;
};

class Range : public Type
{
  public:
	Range(const std::string& name,const Type& dataType,const std::string& lb,const std::string& ub);
	virtual ~Range();
	    
	virtual bool canBeResourceType() const;
    virtual bool isResourceType() const { return m_isResourceType; }	
	virtual void becomeResourceType();
	
	virtual bool isPrimitive() const { return !m_isResourceType; }
	    
    virtual void toNDDL(std::ostream& os) const;
    
  protected:
    bool m_isResourceType;
    const Type& m_dataType;
    std::string m_lb;
    std::string m_ub;
};

class Enumeration : public Type
{
  public:
	Enumeration(const std::string& name,const Type& dataType,const std::vector<Expr*>& values);
	virtual ~Enumeration();
	    
	virtual bool isPrimitive() const { return false; }
	    
    virtual void toNDDL(std::ostream& os) const;
    
  protected:
    const Type& m_dataType;
    std::vector<Expr*> m_values;
};
	
class Arg
{
  public:
    Arg(const std::string& name,const Type& type) : m_name(name), m_dataType(type) {}    
    virtual ~Arg() {}

    const std::string& getName() const { return m_name; }    
    const Type& getType() const { return m_dataType; }

  protected:
    std::string m_name;    
    const Type& m_dataType;
};	
	
class Variable 
{
  public:
    Variable(const Type& type, const std::string& name);
    Variable(const Type& type, const std::string& name, const std::vector<Arg*>& args);
    
    virtual ~Variable();
    
    const std::string& getName() const { return m_name; }

    const Type& getDataType() const { return m_dataType; }
    const std::vector<Arg*>& getArgs() const { return m_args; }
    
    virtual void toNDDL(std::ostream& os) const;
    
  protected:
    std::string m_name;
	const Type& m_dataType;
	std::vector<Arg*> m_args;
};
    
class VarInit
{
  public:
      VarInit(const Variable* v, const std::string& value)
          : m_var(v)
          , m_value(value)
      {
      }
      
      const std::string& getName() const { return m_var->getName(); }
      const std::string& getValue() const { return m_value; }
      
  protected:
      const Variable* m_var;
      std::string m_value;	
};
    
class VarDeclaration : public ANMLElement
{
  public:
    VarDeclaration(const Type& type, const std::vector<VarInit*>& init);
    virtual ~VarDeclaration();

    const Type& getDataType() const { return m_dataType; }
    const std::vector<VarInit*>& getInit() const { return m_init; }
    
    virtual void toNDDL(std::ostream& os) const;
      
  protected:
	const Type& m_dataType;
	std::vector<VarInit*> m_init;
};

// TODO: figure out whether this should be integrated with VarDeclaration
class FreeVarDeclaration : public ANMLElement
{
  public:
    FreeVarDeclaration(const std::vector<Variable*>& vars);
    virtual ~FreeVarDeclaration();
    
    virtual void toNDDL(std::ostream& os) const;
      
  protected:
	std::vector<Variable*> m_vars;    
};


class ObjType : public Type, public ANMLContext
{
  public:
    ObjType(const std::string& name,ObjType* parentObjType);
    virtual ~ObjType();
    
    virtual std::string getContextDesc() const { return m_name; }

    virtual ObjType* getParentObjType() const { return m_parentObjType; }
        
	virtual bool isPrimitive() const { return false; }

    virtual void toNDDL(std::ostream& os) const;
    
  protected:
    ObjType* m_parentObjType;   	
};

class VectorType : public ObjType
{
  public:
    VectorType(const std::string& name);
    
    virtual void toNDDL(std::ostream& os) const;  
};

class Action : public ANMLElement, public ANMLContext
{
  public:
    Action(ObjType& objType,const std::string& name,const std::vector<Variable*>& params);
    virtual ~Action();

    virtual std::string getContextDesc() const { return m_objType.getName() + "::" + m_name; }
    
    const std::vector<Variable*> getParams() const { return m_params; }
    
    void setBody(const std::vector<ANMLElement*>& body);
  
    virtual void toNDDL(std::ostream& os) const;
        
  protected:
    ObjType& m_objType;
    std::vector<Variable*> m_params;
    std::vector<ANMLElement*> m_body;
};

class ActionDuration : public ANMLElement
{
  public:
    ActionDuration(const std::vector<Expr*>& values);
    virtual ~ActionDuration();
    
    virtual void toNDDL(std::ostream& os) const;
    
  protected:  
    std::vector<Expr*> m_values;    	
};

// TODO: Condition and effect must be same class, or have a common parent that incorporates functionality
class Condition : public ANMLElement
{
  public:
    Condition(const std::vector<Proposition*>& propositions);
    virtual ~Condition();
    
    virtual void toNDDL(std::ostream& os) const;
    
  protected:  
    std::vector<Proposition*> m_propositions;    	
};

class Effect : public ANMLElement
{
  public:
    Effect(const std::vector<Proposition*>& propositions);
    virtual ~Effect();
    
    virtual void toNDDL(std::ostream& os) const;
    
  protected:  
    std::vector<Proposition*> m_propositions;    	
};

class Decomposition : public ANMLElement
{
  public:
    Decomposition() : ANMLElement("DECOMPOSITION") {}
    virtual ~Decomposition() {}
    
    //virtual void toNDDL(std::ostream& os) const;
    
  protected:  
    //std::vector<Proposition*> m_propositions;    	
};

class Goal : public ANMLElement
{
  public:
    Goal(const std::vector<Proposition*>& props);
    virtual ~Goal();
    
    virtual void toNDDL(std::ostream& os) const;
    
  protected:  
    std::vector<Proposition*> m_propositions;    	
};

class Fact : public ANMLElement
{
  public:
    Fact(const std::vector<Proposition*>& props);
    virtual ~Fact();
    
    virtual void toNDDL(std::ostream& os) const;
    
  protected:  
    std::vector<Proposition*> m_propositions;    	
};

class Proposition : public ANMLElement
{
  public:
    enum Context {GOAL,CONDITION,FACT,EFFECT};
    
    Proposition(TemporalQualifier* tq,Fluent* f); 
    Proposition(TemporalQualifier* tq,const std::vector<Fluent*>& fluents);
    virtual ~Proposition();
    
    void setContext(Context c) { m_context = c; }
    Context getContext() const { return m_context; }
    
    virtual void toNDDL(std::ostream& os) const;
    
  protected:  
    Context m_context;    	
    TemporalQualifier* m_temporalQualifier;
    std::vector<Fluent*> m_fluents;
    
    void commonInit();    
};

class Change : public Proposition
{
  public:
    Change(Proposition* when_condition,TemporalQualifier* tq,Fluent* f);
    virtual ~Change();
    
    void setWhenCondition(Proposition* p) { m_whenCondition = p; }
    
    virtual void toNDDL(std::ostream& os) const;
    
  protected:  
    Proposition* m_whenCondition;
};

class PropositionComponent
{
  public:
    PropositionComponent() {}
    
    void setProposition(Proposition* p) { m_parentProp = p; }
  
  protected:
    Proposition* m_parentProp;	
};

class TemporalQualifier : public PropositionComponent
{
  public:
    TemporalQualifier(const std::string& op,const std::vector<Expr*>& args);
    virtual ~TemporalQualifier();
     
    virtual void toNDDL(std::ostream& os,Proposition::Context context) const;
    virtual void toNDDL(std::ostream& os,const std::string& ident,const std::string& fluentName) const;
    
  protected:
    std::string m_operator;
    std::vector<Expr*> m_args;
    mutable std::vector<std::string> m_argValues;
};

class Fluent : public PropositionComponent
{
  public:
    Fluent() {}
    virtual ~Fluent() {}
    
    virtual void toNDDL(std::ostream& os, TemporalQualifier* tq) const = 0;
};

class Constraint : public Fluent
{
  public:
    Constraint(const std::string& name,const std::vector<ANML::Expr*>& args);
    virtual ~Constraint();
    
    virtual void toNDDL(std::ostream& os, TemporalQualifier* tq) const;
    
  protected:
    std::string m_name;    
    std::vector<ANML::Expr*> m_args;        	
};

class CompositeFluent : public Fluent
{
  public:
    CompositeFluent(const std::string& op,Fluent* lhs, Fluent* rhs) : m_op(op), m_lhs(lhs), m_rhs(rhs) {}
    virtual ~CompositeFluent() {}
    
    virtual void toNDDL(std::ostream& os, TemporalQualifier* tq) const { os << m_op; }
    
  protected:
    std::string m_op;
    Fluent* m_lhs;
    Fluent* m_rhs;  
};

class RelationalFluent : public Fluent
{
  public:
    RelationalFluent(LHSExpr* lhs,Expr* rhs);
    virtual ~RelationalFluent();
    
    virtual void toNDDL(std::ostream& os, TemporalQualifier* tq) const;
    
  protected:  
    LHSExpr* m_lhs;
    Expr* m_rhs;    	
};

class ResourceChangeFluent : public Fluent
{
  public:
    ResourceChangeFluent(const std::string& op,Expr* var,Expr* qty);
    virtual ~ResourceChangeFluent();

    virtual void toNDDL(std::ostream& os, TemporalQualifier* tq) const;

  protected:
	std::string m_op;
	Expr* m_var;
	Expr* m_qty;      
};

class TransitionChangeFluent : public Fluent
{
  public :
    TransitionChangeFluent(Expr* var, const std::vector<Expr*>& states);
    virtual ~TransitionChangeFluent();

    virtual void toNDDL(std::ostream& os, TemporalQualifier* tq) const;
    
  protected:    
    Expr* m_var;
	std::vector<Expr*> m_states;
};

class Expr 
{
  public:
    Expr() {}
    virtual ~Expr() {}
        
    virtual bool needsVar() { return true; }
    virtual std::string toString() const = 0; 
    virtual const Type& getDataType() const = 0;
    
    virtual void toNDDLasLHS(std::ostream& os,
                             Proposition::Context context,
                             const std::string& varName) const {}    
                             
    virtual void toNDDLasRHS(std::ostream& os,
                             Proposition::Context context,
                             const std::string& varName) const {}    
};

class LHSExpr : public Expr
{
  public:
    LHSExpr() {}
    virtual ~LHSExpr() {}
        
    virtual void setArgs(const std::vector<Expr*>& args) { m_args = args; }
    virtual const std::vector<Expr*>& getArgs() const { return m_args; } 
    
  protected:  
    std::vector<Expr*> m_args;    	
};

class LHSPlannerConfig : public LHSExpr
{
  public:
	LHSPlannerConfig();
	virtual ~LHSPlannerConfig();
	
    virtual bool needsVar() { return false; }
    virtual void setArgs(const std::string& predicate,const std::vector<Expr*>& args);
    virtual std::string toString() const  { return "PlannerConfig"; }
    virtual const Type& getDataType() const { return *Type::BOOL; }
    virtual void toNDDL(std::ostream& os) const;
    virtual void toNDDLasRHS(std::ostream& os,
                             Proposition::Context context,
                             const std::string& varName) const {}    
    
  protected:
    Expr* m_startHorizon;
    Expr* m_endHorizon;
    Expr* m_maxSteps;
    Expr* m_maxDepth;  	  
};

class LHSAction : public LHSExpr
{
  public:
	LHSAction(Action* a,const std::string& path) : m_action(a), m_path(path) {}
	virtual ~LHSAction() {}
	
    virtual const Type& getDataType() const { return *Type::VOID; }
    virtual std::string toString() const  { return m_path; }
    
    virtual void toNDDLasLHS(std::ostream& os,
                             Proposition::Context context,
                             const std::string& varName) const;    
        
  protected:
    Action* m_action;  	  
    std::string m_path;
};

class LHSVariable : public LHSExpr
{
  public:
	LHSVariable(Variable* v,const std::string& path) : m_var(v), m_path(path) {}
	virtual ~LHSVariable() {}
	
    virtual bool needsVar() { return false; }
    virtual const Type& getDataType() const { return m_var->getDataType(); }
    virtual std::string toString() const  { return m_path; }
    virtual void toNDDLasRHS(std::ostream& os,
                             Proposition::Context context,
                             const std::string& varName) const;    
    
  protected:
    Variable* m_var;  	  
    std::string m_path;
};

class ExprConstant : public Expr
{
  public:
    ExprConstant(const Type& dataType,const std::string& value) : m_dataType(dataType),m_value(value) {}
    virtual ~ExprConstant() {}
        
    virtual bool needsVar() { return false; }
    virtual const Type& getDataType() const { return m_dataType; }
    virtual std::string toString() const { return m_value; } 
    
    virtual void toNDDLasRHS(std::ostream& os,
                             Proposition::Context context,
                             const std::string& varName) const;    
  
  protected:
    const Type& m_dataType;
    std::string m_value;    
};

class ExprArithOp : public Expr
{
  public:
    ExprArithOp(const std::string& op, Expr* operand1, Expr* operand2) : m_operator(op), m_op1(operand1), m_op2(operand2) {}
    virtual ~ExprArithOp() {}
        
    virtual bool needsVar() { return true; }
    virtual const Type& getDataType() const { return m_op1->getDataType(); }
    virtual std::string toString() const { return m_op1->toString() + m_operator + m_op2->toString(); } 
    virtual void toNDDLasRHS(std::ostream& os,
                             Proposition::Context context,
                             const std::string& varName) const;    
  
  protected:
    std::string m_operator;
    Expr* m_op1;
    Expr* m_op2;        
};

class ExprVector : public Expr
{
  public:
    ExprVector(const std::vector<Expr*>& values);
    virtual ~ExprVector();
        
    virtual bool needsVar() { return false; }
    virtual std::string toString() const; 
    virtual const Type& getDataType() const { return *m_dataType; }
    virtual void toNDDLasRHS(std::ostream& os,
                             Proposition::Context context,
                             const std::string& varName) const;    
    
  protected:
    VectorType* m_dataType;
    std::vector<Expr*> m_values;        
};

}

#endif /*ANMLTRANSLATOR_H_*/
