#ifndef _H_Interpreter
#define _H_Interpreter

#include "ConstrainedVariable.hh"
#include "IntervalToken.hh"
#include "Object.hh"
#include "ObjectFactory.hh"
#include "PlanDatabaseDefs.hh"
#include "Rule.hh"
#include "RuleInstance.hh"
#include "RulesEngineDefs.hh"
#include "Timeline.hh"
#include "TokenFactory.hh"
#include "Debug.hh"
#include <map>
#include <vector>


namespace EUROPA {

  class Expr;
  class RuleExpr;

  class DataRef
  {
  	public :
  	    DataRef();
  	    DataRef(const ConstrainedVariableId& v);
  	    virtual ~DataRef();

  	    const ConstrainedVariableId& getValue();

  	    static DataRef null;

  	protected :
  	    ConstrainedVariableId m_value;
  };

  class EvalContext
  {
  	public:
  	    EvalContext(EvalContext* parent);
  	    virtual ~EvalContext();

  	    virtual void addVar(const char* name,const ConstrainedVariableId& v);
  	    virtual ConstrainedVariableId getVar(const char* name);

  	    virtual void addToken(const char* name,const TokenId& t);
  	    virtual TokenId getToken(const char* name);

  	    virtual void* getElement(const char* name) const { return NULL; }

        virtual std::string toString() const;

  	protected:
  	    EvalContext* m_parent;
  	    std::map<std::string,ConstrainedVariableId> m_variables;
  	    std::map<std::string,TokenId> m_tokens;
  };

  class Expr
  {
  	public:
        virtual DataRef eval(EvalContext& context) const = 0;
        virtual ~Expr(){}

        virtual std::string toString() const { return "Expr"; }
  };

  class ExprList : public Expr
  {
    public:
        ExprList();
        virtual ~ExprList();

        virtual DataRef eval(EvalContext& context) const;
        void addChild(Expr* child);

        virtual std::string toString() const;

    protected:
        std::vector<Expr*> m_children;
  };

  class ExprNoop : public Expr
  {
    public:
        ExprNoop(const std::string& str);
        virtual ~ExprNoop();

        virtual DataRef eval(EvalContext& context) const;

        virtual std::string toString() const { return "ExprNoop:"+m_str; }

    protected:
        std::string m_str;
  };

  // Call to super inside a constructor
  class ExprConstructorSuperCall : public Expr
  {
  	public:
  	    ExprConstructorSuperCall(const LabelStr& superClassName,
  	                             const std::vector<Expr*>& argExprs);
  	    virtual ~ExprConstructorSuperCall();

  	    virtual DataRef eval(EvalContext& context) const;

  	    const LabelStr& getSuperClassName() const { return m_superClassName; }

  	    void evalArgs(EvalContext& context, std::vector<const AbstractDomain*>& arguments) const;

  	protected:
  	    LabelStr m_superClassName;
        std::vector<Expr*> m_argExprs;
  };

  // Assignment inside a constructor
  // TODO: make lhs an Expr as well to make this a generic assignment
  class ExprConstructorAssignment : public Expr
  {
  	public:
  	    ExprConstructorAssignment(const char* lhs,
  	                              Expr* rhs);
  	    virtual ~ExprConstructorAssignment();

  	    virtual DataRef eval(EvalContext& context) const;

    protected:
        LabelStr m_lhs;
        Expr* m_rhs;
  };

  class ExprConstant : public Expr
  {
  	public:
  	    ExprConstant(DbClientId& dbClient, const char* type, const AbstractDomain* d);
  	    virtual ~ExprConstant();

  	    virtual DataRef eval(EvalContext& context) const;

  	protected:
  	    DbClientId m_dbClient;
  	    LabelStr m_type;
  	    const AbstractDomain* m_domain;
  };

  class ExprVarRef : public Expr
  {
  	public:
  	    ExprVarRef(const char* name);
  	    virtual ~ExprVarRef();

  	    virtual DataRef eval(EvalContext& context) const;
  	    virtual std::string toString() const;

  	protected:
  	    LabelStr m_varName;
  };

  class ExprNewObject : public Expr
  {
  	public:
  	    ExprNewObject(const DbClientId& dbClient,
	                  const LabelStr& objectType,
	                  const LabelStr& objectName,
	                  const std::vector<Expr*>& argExprs);

	    virtual ~ExprNewObject();

  	    virtual DataRef eval(EvalContext& context) const;

  	protected:
        DbClientId            m_dbClient;
	    LabelStr              m_objectType;
	    LabelStr              m_objectName;
	    std::vector<Expr*>    m_argExprs;
  };

  class InterpretedObjectFactory : public ObjectFactory
  {
  	public:
  	    InterpretedObjectFactory(
  	        const char* className,
  	        const LabelStr& signature,
  	        const std::vector<std::string>& constructorArgNames,
  	        const std::vector<std::string>& constructorArgTypes,
  	        ExprConstructorSuperCall* superCallExpr,
  	        const std::vector<Expr*>& constructorBody,
  	        bool canMakeNewObject = false
  	    );

  	    virtual ~InterpretedObjectFactory();

	protected:
	    // createInstance = makeNewObject + evalConstructorBody
	    virtual ObjectId createInstance(
	                            const PlanDatabaseId& planDb,
	                            const LabelStr& objectType,
	                            const LabelStr& objectName,
	                            const std::vector<const AbstractDomain*>& arguments) const;

        // Any exported C++ classes must register a factory for each C++ constructor
        // and override this method to call the C++ constructor
    	virtual ObjectId makeNewObject(
	                        const PlanDatabaseId& planDb,
	                        const LabelStr& objectType,
	                        const LabelStr& objectName,
	                        const std::vector<const AbstractDomain*>& arguments) const;

	    virtual void evalConstructorBody(
	                       ObjectId& instance,
	                       const std::vector<const AbstractDomain*>& arguments) const;

	    bool checkArgs(const std::vector<const AbstractDomain*>& arguments) const;

        LabelStr                  m_className;
        std::vector<std::string>  m_constructorArgNames;
        std::vector<std::string>  m_constructorArgTypes;
        ExprConstructorSuperCall* m_superCallExpr;
        std::vector<Expr*>        m_constructorBody;
        bool                      m_canMakeNewObject;
    mutable EvalContext*      m_evalContext;
  };

  class ObjectEvalContext : public EvalContext
  {
    public:
        ObjectEvalContext(EvalContext* parent, const ObjectId& objInstance);
        virtual ~ObjectEvalContext();

        virtual ConstrainedVariableId getVar(const char* name);

    protected:
        ObjectId m_obj;
  };

  class ExprConstraint;

  // InterpretedToken is the interpreted version of NddlToken
  class InterpretedToken : public IntervalToken
  {
  	public:
  	    // Same Constructor signatures as NddlToken, see if both are needed
  	    InterpretedToken(const PlanDatabaseId& planDatabase,
  	                     const LabelStr& predicateName,
                         const std::vector<LabelStr>& parameterNames,
                         const std::vector<LabelStr>& parameterTypes,
                         const std::vector<Expr*>& parameterValues,
	                     const std::vector<LabelStr>& assignVars,
                         const std::vector<Expr*>& assignValues,
                         const std::vector<ExprConstraint*>& constraints,
                         const bool& rejectable = false,
                         const bool& isFact = false,
  	                     const bool& close = false);

        InterpretedToken(const TokenId& master,
                         const LabelStr& predicateName,
                         const LabelStr& relation,
                         const std::vector<LabelStr>& parameterNames,
                         const std::vector<LabelStr>& parameterTypes,
                         const std::vector<Expr*>& parameterValues,
                         const std::vector<LabelStr>& assignVars,
                         const std::vector<Expr*>& assignValues,
                         const std::vector<ExprConstraint*>& constraints,
                         const bool& close = false);


  	    virtual ~InterpretedToken();

    protected:
        void commonInit(const std::vector<LabelStr>& parameterNames,
                        const std::vector<LabelStr>& parameterTypes,
                        const std::vector<Expr*>& parameterValues,
			const std::vector<LabelStr>& assignVars,
			const std::vector<Expr*>& assignValues,
			const std::vector<ExprConstraint*>& constraints,
			const bool& autoClose);

        friend class InterpretedTokenFactory;
  };

  class TokenEvalContext : public EvalContext
  {
  	public:
  	    TokenEvalContext(EvalContext* parent, const TokenId& tok);
  	    virtual ~TokenEvalContext();

  	    virtual ConstrainedVariableId getVar(const char* name);

  	    virtual bool isClass(const LabelStr& className) const;

  	protected:
  	    TokenId m_token;
  };

  class InterpretedTokenFactory: public TokenFactory
  {
    public:
	  InterpretedTokenFactory(const LabelStr& predicateName,
	                          TokenFactoryId parentFactory,
	                          const std::vector<LabelStr>& parameterNames,
                              const std::vector<LabelStr>& parameterTypes,
                              const std::vector<Expr*>& parameterValues,
	                          const std::vector<LabelStr>& assignVars,
                              const std::vector<Expr*>& assignValues,
                              const std::vector<ExprConstraint*>& constraints);

	  virtual TokenId createInstance(const PlanDatabaseId& planDb, const LabelStr& name, bool rejectable, bool isFact) const;
	  virtual TokenId createInstance(const TokenId& master, const LabelStr& name, const LabelStr& relation) const;

    protected:
      TokenFactoryId m_parentFactory;
      std::vector<LabelStr> m_parameterNames;
      std::vector<LabelStr> m_parameterTypes;
      std::vector<Expr*> m_parameterValues;
      std::vector<LabelStr> m_assignVars;
      std::vector<Expr*> m_assignValues;
      std::vector<ExprConstraint*> m_constraints;
  };

  class RuleExpr;

  class InterpretedRuleInstance : public RuleInstance
  {
  	public:
  	    InterpretedRuleInstance(const RuleId& rule,
  	                            const TokenId& token,
  	                            const PlanDatabaseId& planDb,
                                const std::vector<RuleExpr*>& body);

        InterpretedRuleInstance(const RuleInstanceId& parent,
                                const ConstrainedVariableId& var,
                                const AbstractDomain& domain,
                                const bool positive,
                                const std::vector<RuleExpr*>& body);

        InterpretedRuleInstance(const RuleInstanceId& parent,
                                const std::vector<ConstrainedVariableId>& vars,
                                const bool positive,
                                const std::vector<RuleExpr*>& body);

  	    virtual ~InterpretedRuleInstance();

        void createConstraint(const LabelStr& name, std::vector<ConstrainedVariableId>& vars);

        TokenId createSubgoal(
                   const LabelStr& name,
                   const LabelStr& predicateType,
                   const LabelStr& predicateInstance,
                   const LabelStr& relation,
                   bool isConstrained,
                   ConstrainedVariableId& owner);

        ConstrainedVariableId addLocalVariable(
                       const AbstractDomain& baseDomain,
				       bool canBeSpecified,
				       const LabelStr& name);

        ConstrainedVariableId addObjectVariable(
                       const LabelStr& type,
                       const ObjectDomain& baseDomain,
				       bool canBeSpecified,
				       const LabelStr& name);

        void executeLoop(EvalContext& evalContext,
                         const LabelStr& loopVarName,
                         const LabelStr& loopVarType,
                         const LabelStr& valueSet,
                         const std::vector<RuleExpr*>& loopBody);


    protected:
        std::vector<RuleExpr*> m_body;

        virtual void handleExecute();

        friend class ExprIf;
        friend class ExprRuleVarRef;
  };

  typedef Id<InterpretedRuleInstance> InterpretedRuleInstanceId;
  class RuleInstanceEvalContext : public EvalContext
  {
  	public:
  	    RuleInstanceEvalContext(EvalContext* parent, const InterpretedRuleInstanceId& ruleInstance);
  	    virtual ~RuleInstanceEvalContext();

  	    virtual ConstrainedVariableId getVar(const char* name);
  	    virtual InterpretedRuleInstanceId& getRuleInstance() { return m_ruleInstance; }

  	    virtual TokenId getToken(const char* name);

        virtual bool isClass(const LabelStr& className) const;

        virtual std::string toString() const;

  	protected:
  	    InterpretedRuleInstanceId m_ruleInstance;
  };

  class InterpretedRuleFactory : public Rule
  {
    public:
        InterpretedRuleFactory(const LabelStr& predicate, const LabelStr& source, const std::vector<RuleExpr*>& ruleBody);
        virtual ~InterpretedRuleFactory();

        virtual RuleInstanceId createInstance(const TokenId& token,
                                              const PlanDatabaseId& planDb,
                                              const RulesEngineId &rulesEngine) const;

    protected:
        std::vector<RuleExpr*> m_body;
  };

  /*
   * Expr that appears in the body of an interpreted rule instance
   *
   */

  class RuleExpr  : public Expr
  {
  	public:
  	    virtual DataRef eval(EvalContext& context) const
  	    {
  	    	RuleInstanceEvalContext* ec = (RuleInstanceEvalContext*)&context;
  	    	return doEval(*ec);
  	    }

  	    virtual DataRef doEval(RuleInstanceEvalContext& context) const = 0;
    virtual ~RuleExpr(){}
  };

  class ExprRuleVarRef : public RuleExpr
  {
  	public:
  	    ExprRuleVarRef(const char* name);
  	    virtual ~ExprRuleVarRef();

  	    virtual DataRef doEval(RuleInstanceEvalContext& context) const;

  	protected:
  	    std::string m_parentName;
  	    std::string m_varName;
  };

  class ExprConstraint : public RuleExpr
  {
  	public:
  	    ExprConstraint(const char* name,const std::vector<Expr*> args);
  	    virtual ~ExprConstraint();

  	    virtual DataRef doEval(RuleInstanceEvalContext& context) const;

  	    const LabelStr getName() const { return m_name; }
  	    const std::vector<Expr*>& getArgs() const { return m_args; }

  	protected:
  	    LabelStr m_name;
  	    std::vector<Expr*> m_args;
  };

  class ExprSubgoal : public RuleExpr
  {
  	public:
  	    ExprSubgoal(const char* name,
  	                const char* predicateType,
  	                const char* predicateInstance,
  	                const char* relation);
  	    virtual ~ExprSubgoal();

  	    virtual DataRef doEval(RuleInstanceEvalContext& context) const;

  	protected:
  	    LabelStr m_name;
  	    LabelStr m_predicateType;
  	    LabelStr m_predicateInstance;
  	    LabelStr m_relation;

  	  bool isConstrained(RuleInstanceEvalContext& context, const LabelStr& predicateInstance) const;
  };

  class ExprLocalVar : public RuleExpr
  {
  	public:
  	    ExprLocalVar(const LabelStr& name,
  	                 const LabelStr& type,
  	                 bool guarded,
  	                 Expr* domainRestriction,
  	                 const AbstractDomain& baseDomain);
  	    virtual ~ExprLocalVar();

  	    virtual DataRef doEval(RuleInstanceEvalContext& context) const;

  	protected:
  	    LabelStr m_name;
  	    LabelStr m_type;
  	    bool m_guarded;
  	    Expr* m_domainRestriction;
  	    const AbstractDomain& m_baseDomain;
  };

  class ExprIf : public RuleExpr
  {
  	public:
  	    ExprIf(const char* op, Expr* lhs,Expr* rhs,const std::vector<RuleExpr*>& ifBody);
  	    virtual ~ExprIf();

  	    virtual DataRef doEval(RuleInstanceEvalContext& context) const;

    protected:
        const std::string m_op;
        Expr* m_lhs;
        Expr* m_rhs;
        const std::vector<RuleExpr*> m_ifBody;
  };


  class ExprLoop : public RuleExpr
  {
  	public:
  	    ExprLoop(const char* varName, const char* varType, const char* varValue,const std::vector<RuleExpr*>& loopBody);
  	    virtual ~ExprLoop();

  	    virtual DataRef doEval(RuleInstanceEvalContext& context) const;

    protected:
        LabelStr m_varName;
        LabelStr m_varType;
        LabelStr m_varValue;
        const std::vector<RuleExpr*> m_loopBody;
  };

  // TODO: create a separate file for exported C++ classes?
  class NativeObjectFactory : public InterpretedObjectFactory
  {
  	public:
  	    NativeObjectFactory(const char* className, const LabelStr& signature)
  	        : InterpretedObjectFactory(
  	              className,                  // className
  	              signature,                  // signature
  	              std::vector<std::string>(), // ConstructorArgNames
  	              std::vector<std::string>(), // constructorArgTypes
  	              NULL,                       // SuperCallExpr
  	              std::vector<Expr*>(),       // constructorBody
  	              true                        // canCreateObjects
  	          )
  	    {
  	    }

  	    virtual ~NativeObjectFactory() {}

  	protected:
    	virtual ObjectId makeNewObject(
	                        const PlanDatabaseId& planDb,
	                        const LabelStr& objectType,
	                        const LabelStr& objectName,
	                        const std::vector<const AbstractDomain*>& arguments) const = 0;
  };

  class NativeTokenFactory: public TokenFactory
  {
    public:
	  NativeTokenFactory(const LabelStr& predicateName) : TokenFactory(predicateName) {}

	  virtual TokenId createInstance(const PlanDatabaseId& planDb, const LabelStr& name, bool rejectable, bool isFact) const = 0;
	  virtual TokenId createInstance(const TokenId& master, const LabelStr& name, const LabelStr& relation) const = 0;
  };

  class TimelineObjectFactory : public NativeObjectFactory
  {
  	public:
  	    TimelineObjectFactory(const LabelStr& signature);
  	    virtual ~TimelineObjectFactory();

  	protected:
    	virtual ObjectId makeNewObject(
	                        const PlanDatabaseId& planDb,
	                        const LabelStr& objectType,
	                        const LabelStr& objectName,
	                        const std::vector<const AbstractDomain*>& arguments) const;
  };
}

#endif // _H_Interpreter
