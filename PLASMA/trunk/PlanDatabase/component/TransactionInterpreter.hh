#ifndef _H_TransactionInterpreter
#define _H_TransactionInterpreter

#include "PlanDatabaseDefs.hh"
#include "Object.hh"
#include "ObjectFactory.hh"
#include "Timeline.hh"
#include <map>
#include <vector>


namespace EUROPA {

  class DataRef
  {
  	public :
  	    DataRef(AbstractDomain* d);
  	    DataRef(const AbstractDomain* d=NULL);
  	    virtual ~DataRef();
  	    
  	    AbstractDomain* getValue();
  	    const AbstractDomain* getConstValue() const;
  	    
  	    static DataRef null;

  	protected :
  	    union DataRefValue {
  	        AbstractDomain* domain;
  	        const AbstractDomain* constDomain;
  	    };
  	    
  	    DataRefValue m_value;
  };
  
  /*
   * Transaction Interpreter Variable
   */
  class TIVariable
  {
  	public:
  	    TIVariable() {} // TODO: this is so that EvalContext can add to the var map, fix it  	    
  	    TIVariable(const char* name,const AbstractDomain* d);
  	    virtual ~TIVariable();
  	    
  	    const char* getName() const;
  	    DataRef& getDataRef();
  	    
  	protected:
  	    const char* m_name;
  	    DataRef m_value;
  };
  
  class EvalContext 
  {
  	public:
  	    EvalContext(EvalContext* parent); 
  	    virtual ~EvalContext();
  	    
  	    virtual void addVar(const char* name,const AbstractDomain* value); 
  	    virtual TIVariable* getVar(const char* name);
  	
  	protected:
  	    EvalContext* m_parent;      	    
  	    std::map<std::string,TIVariable> m_variables;
  };
  
  class Expr
  {      	
  	public:
  	    virtual DataRef eval(EvalContext& context) const = 0;
  };
  
  // Call to super inside a constructor 
  class ExprConstructorSuperCall : public Expr
  {      	
  	public:
  	    ExprConstructorSuperCall(const LabelStr& superClassName, 
  	                             const std::vector<Expr*>& argExprs);
  	    virtual ~ExprConstructorSuperCall();
  	    
  	    virtual DataRef eval(EvalContext& context) const;
  	    
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
        const char* m_lhs;
        Expr* m_rhs;
  };
  
  class ExprConstant : public Expr
  {
  	public:
  	    ExprConstant(const AbstractDomain* d);
  	    virtual ~ExprConstant();

  	    virtual DataRef eval(EvalContext& context) const;  
  	    
  	protected:
  	    DataRef m_data;    	    
  };
  
  class ExprVariableRef : public Expr
  {
  	public:
  	    ExprVariableRef(const char* name);
  	    virtual ~ExprVariableRef();

  	    virtual DataRef eval(EvalContext& context) const;  
  	    
  	protected:
  	    const char* m_varName;    	    
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
    
  class InterpretedObjectFactory : public ConcreteObjectFactory
  {
  	public:
  	    InterpretedObjectFactory(
  	        const char* className,
  	        const LabelStr& signature, 
  	        const std::vector<std::string>& constructorArgNames,
  	        const std::vector<std::string>& constructorArgTypes,
  	        Expr* superCallExpr,
  	        const std::vector<Expr*>& constructorBody
  	    );
  	    
  	    virtual ~InterpretedObjectFactory();
  	      	    
	protected:
	    virtual ObjectId createInstance(const PlanDatabaseId& planDb,
	                            const LabelStr& objectType, 
	                            const LabelStr& objectName,
	                            const std::vector<const AbstractDomain*>& arguments) const;
	                             
	    virtual void constructor(ObjectId& instance, const std::vector<const AbstractDomain*>& arguments) const;
  
        // Any exported C++ classes must register a factory for each C++ constructor and override this method to call it 
    	virtual ObjectId makeNewObject( 
	                        const PlanDatabaseId& planDb,
	                        const LabelStr& objectType, 
	                        const LabelStr& objectName,
	                        const std::vector<const AbstractDomain*>& arguments) const;
	                        
	    bool checkArgs(const std::vector<const AbstractDomain*>& arguments) const;

        LabelStr m_className;
        std::vector<std::string> m_constructorArgNames;	                          
        std::vector<std::string> m_constructorArgTypes;	
        Expr*                    m_superCallExpr;                          
        std::vector<Expr*>       m_constructorBody;	                          
  };  
  
  // TODO: create a separate file for exported C++ classes?
  class TimelineObjectFactory : public InterpretedObjectFactory
  {
  	public:
  	    TimelineObjectFactory(const LabelStr& signature) 
  	        : InterpretedObjectFactory(
  	              "Timeline",
  	              signature,
  	              std::vector<std::string>(),
  	              std::vector<std::string>(),
  	              NULL,
  	              std::vector<Expr*>()
  	          )
  	    {
  	    }
  	    
  	    virtual ~TimelineObjectFactory() {}
  	    
  	protected:
    	virtual ObjectId makeNewObject( 
	                        const PlanDatabaseId& planDb,
	                        const LabelStr& objectType, 
	                        const LabelStr& objectName,
	                        const std::vector<const AbstractDomain*>& arguments) const
	    {
	    	std::cout << "Created Timeline: " << objectName.toString() << " type:" << objectType.toString() << std::endl;
	    	return (new Timeline(planDb, objectType, objectName,true))->getId();
	    }
  };
  
}


#endif // _H_TransactionInterpreter
