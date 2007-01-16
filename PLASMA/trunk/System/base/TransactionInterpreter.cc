/**
 * @file TransactionInterpreter.cc
 * @brief Core classes to interpret XML statements
 * @author Javier Barreiro
 * @date December, 2006
 */

#include "TransactionInterpreter.hh"
#include "tinyxml.h"
#include "Debug.hh"
#include "Error.hh"
#include "DbClient.hh"
#include "EnumeratedTypeFactory.hh"
#include "Object.hh" 
#include "ObjectFactory.hh"
#include "TypeFactory.hh"
#include "Schema.hh"

namespace EUROPA {

  /*
   * 
   * InterpretedDbClientTransactionPlayer
   * 
   */ 
       
  void createDefaultObjectFactory(const char* className, bool canCreateObjects)
  {
      std::vector<std::string> constructorArgNames;
      std::vector<std::string> constructorArgTypes;
      std::vector<Expr*> constructorBody;
      ExprConstructorSuperCall* superCallExpr = NULL;
      
      // The ObjectFactory constructor automatically registers the factory
      new InterpretedObjectFactory(
          className,
          className,
          constructorArgNames,
          constructorArgTypes,
          superCallExpr,
          constructorBody,
          canCreateObjects
      );       
  }
  
  InterpretedDbClientTransactionPlayer::InterpretedDbClientTransactionPlayer(const DbClientId & client)
      : DbClientTransactionPlayer(client) 
  {  	  
  	  m_systemClasses.insert("Object");
  	  m_systemClasses.insert("Timeline");
  	  
  	  // TODO: this should be done once after the schema is initialized
  	  // TODO: check to make sure this is done only once
  	  createDefaultObjectFactory("Object", true);
      REGISTER_OBJECT_FACTORY(TimelineObjectFactory, Timeline);	    	    	  
  }

  InterpretedDbClientTransactionPlayer::~InterpretedDbClientTransactionPlayer() 
  {
  }

  const char* safeStr(const char* str)
  {
  	return (str !=NULL ? str : "NULL");
  }

  std::ostringstream dbgout;
  const char* ident="    ";
  
  void InterpretedDbClientTransactionPlayer::playDeclareClass(const TiXmlElement& element) 
  {
    const char* className = element.Attribute("name");
    Id<Schema> schema = Schema::instance();
    schema->declareObjectType(className);
    dbgout.str("");
    dbgout << "Declared class " << className << std::endl;
    std::cout << dbgout.str();
  }
  
  void InterpretedDbClientTransactionPlayer::playDefineClass(const TiXmlElement& element) 
  {
    const char* className = element.Attribute("name");

    const char* parentClassName = element.Attribute("extends");
    parentClassName = (parentClassName == NULL ? "Object" : parentClassName);

    Id<Schema> schema = Schema::instance();
    schema->addObjectType(className,parentClassName);
    // The TypeFactory constructor automatically registers the factory
    // TODO: This should probably be done by Schema::addObjectType
    new EnumeratedTypeFactory(
        className,
        className,
        ObjectDomain(className)
    );             

    dbgout.str("");
    dbgout << "class " << className  
                       << (parentClassName != NULL ? " extends " : "") 
                        << (parentClassName != NULL ? parentClassName : "") 
                        << " {" << std::endl;
                          
    bool definedDefaultConstructor = false;                          
    for(const TiXmlElement* child = element.FirstChildElement(); child; child = child->NextSiblingElement() ) {    	
    	const char * tagname = child->Value();
    	
	    if (strcmp(tagname, "var") == 0) 
	    	defineClassMember(schema,className,child);
	    else if (strcmp(tagname, "constructor") == 0) { 
	    	int argCnt = defineConstructor(schema,className,child);
	    	if (argCnt == 0)
	    	    definedDefaultConstructor = true;	
	    }	    	    	
	    else if (strcmp(tagname, "predicate") == 0) 
	    	declarePredicate(schema,className,child);	    	
	    else if (strcmp(tagname, "enum") == 0) 
	    	defineEnum(schema,className,child);	    	
    }

    // Register a default factory with no arguments if one is not provided explicitly
    if (!definedDefaultConstructor &&
        m_systemClasses.find(className) == m_systemClasses.end()) { 
        	createDefaultObjectFactory(className, false);
        	dbgout << "    generated default constructor" << std::endl;
    }
    
    dbgout << "}" << std::endl;    
    std::cout << dbgout.str() << std::endl;     
  }

  void InterpretedDbClientTransactionPlayer::defineClassMember(Id<Schema>& schema, const char* className,  const TiXmlElement* element)
  {	
	  const char* type = safeStr(element->Attribute("type"));	
	  const char* name = safeStr(element->Attribute("name"));	
	  dbgout << ident << type << " " << name << std::endl;
	  schema->addMember(className, type, name);
  }

  int InterpretedDbClientTransactionPlayer::defineConstructor(Id<Schema>& schema, const char* className,  const TiXmlElement* element)
  {	
  	  std::ostringstream signature;
  	  signature << className;

      std::vector<std::string> constructorArgNames;
      std::vector<std::string> constructorArgTypes;
      std::vector<Expr*> constructorBody;
      ExprConstructorSuperCall* superCallExpr = NULL;
        	  
      for(const TiXmlElement* child = element->FirstChildElement(); child; child = child->NextSiblingElement() ) {
      	  if (strcmp(child->Value(),"arg") == 0) {
	          const char* type = safeStr(child->Attribute("type"));	
	          const char* name = safeStr(child->Attribute("name"));	
	          constructorArgNames.push_back(name);
	          constructorArgTypes.push_back(type);
	          signature << ":" << type;
      	  }
      	  else if (strcmp(child->Value(),"super") == 0) {
      	  	std::vector<Expr*> argExprs;     	  	
            for(const TiXmlElement* argChild = child->FirstChildElement(); argChild; argChild = argChild->NextSiblingElement() ) {
          	    if (strcmp(argChild->Value(),"value") == 0) {
          	    	Expr* arg = new ExprConstant(xmlAsAbstractDomain(*argChild));
          	    	argExprs.push_back(arg);
          	    }
          	    else if (strcmp(argChild->Value(),"id") == 0) {
          	    	const char* varName = argChild->Attribute("name");
          	    	Expr* arg = new ExprVariableRef(varName);
          	    	argExprs.push_back(arg);
          	    }
            }

      	  	superCallExpr = new ExprConstructorSuperCall(Schema::instance()->getParent(className),argExprs);
      	  }
      	  else if (strcmp(child->Value(),"assign") == 0) {
      	  	const TiXmlElement* rhsChild = child->FirstChildElement();

      	  	const char* lhs = child->Attribute("name");

      	  	Expr* rhs=NULL;
            // rhs type can be "value | interval | set" (constant), "id" (parameter) or "new" (new object)  
      	  	const char* rhsType = rhsChild->Value();
      	  	if (strcmp(rhsType,"value") == 0 || 
      	  	    strcmp(rhsType,"interval") == 0 ||
      	  	    strcmp(rhsType,"set") == 0) {
      	  		rhs = new ExprConstant(xmlAsAbstractDomain(*rhsChild));
      	  	}
      	  	else if (strcmp(rhsType,"id") == 0) {
          	  	const char* varName = rhsChild->Attribute("name");
          	  	rhs = new ExprVariableRef(varName);
      	  	}
      	  	else if (strcmp(rhsType,"new") == 0) {
	          	 const char* objectType = rhsChild->Attribute("type");      	  		
      	  		
	      	  	std::vector<Expr*> argExprs;     	  	
	            for(const TiXmlElement* argChild = rhsChild->FirstChildElement(); argChild; argChild = argChild->NextSiblingElement() ) {
	          	    if (strcmp(argChild->Value(),"value") == 0) {
	          	    	Expr* arg = new ExprConstant(xmlAsAbstractDomain(*argChild));
	          	    	argExprs.push_back(arg);
	          	    }
	          	    else if (strcmp(argChild->Value(),"id") == 0) {
	          	    	const char* varName = argChild->Attribute("name");
	          	    	Expr* arg = new ExprVariableRef(varName);
	          	    	argExprs.push_back(arg);
	          	    }
	            }

      	  		rhs = new ExprNewObject(
      	  		    m_client,
      	  		    objectType,
      	  		    lhs,
      	  		    argExprs
      	  		);
      	  	}

      	  	constructorBody.push_back(new ExprConstructorAssignment(lhs,rhs));
      	  }
      }	
      dbgout << ident << "constructor (" << signature.str() << ")" << std::endl; 

		// If constructor for super class isn't called explicitly, call default one with no args
        if (superCallExpr == NULL) 
            superCallExpr = new ExprConstructorSuperCall(Schema::instance()->getParent(className),std::vector<Expr*>());                 	
            
      // The ObjectFactory constructor automatically registers the factory
      new InterpretedObjectFactory(
          className,
          signature.str(),
          constructorArgNames,
          constructorArgTypes,
          superCallExpr,
          constructorBody
      ); 
      
      return constructorArgNames.size();
  }

  void InterpretedDbClientTransactionPlayer::declarePredicate(Id<Schema>& schema, const char* className,  const TiXmlElement* element)
  {	
      std::string predName = std::string(className) + "." + element->Attribute("name");	
      schema->addPredicate(predName.c_str());

      dbgout << ident << "predicate " <<  predName << "(";
      for(const TiXmlElement* predArg = element->FirstChildElement(); predArg; predArg = predArg->NextSiblingElement() ) {
      	if (strcmp(predArg->Value(),"var") == 0) {
	      const char* type = safeStr(predArg->Attribute("type"));	
	      const char* name = safeStr(predArg->Attribute("name"));	
	      dbgout << type << " " << name << ",";
          schema->addMember(predName.c_str(), type, name);
      	}
      	else if (strcmp(predArg->Value(),"invoke") == 0) {
      		// TODO: deal with this
      		dbgout << "constraint " << predArg->Attribute("name");
      	}             
      }	
      dbgout << ")" << std::endl;
  }
    
  void InterpretedDbClientTransactionPlayer::defineEnum(Id<Schema>& schema, const char* className,  const TiXmlElement* element)
  {	
  	  // Enum is scoped within the class but in the generated code it doesn't make a difference
  	  playDefineEnumeration(*element);
  }
  
  void InterpretedDbClientTransactionPlayer::playDefineCompat(const TiXmlElement &)
  {
      // TODO: jrb
  }
  
  void InterpretedDbClientTransactionPlayer::playDefineEnumeration(const TiXmlElement &element)
  {
      // TODO: jrb
      const char* enumName = element.Attribute("name");
      const TiXmlElement* setElement = element.FirstChildElement();
      check_error(strcmp(setElement->Value(),"set") == 0, "Expected value set as part of Enum definition");
      
      Id<Schema> schema = Schema::instance();
      schema->addEnum(enumName);
      
      const TiXmlElement* enumValue;
      for(enumValue = setElement->FirstChildElement(); enumValue; enumValue = enumValue->NextSiblingElement() ) {
      	if (strcmp(enumValue->Value(),"symbol") == 0) {
      		LabelStr symbolValue(enumValue->Attribute("value"));
            schema->addValue(enumName, symbolValue);
      	}
      	else {
      	    // TODO: deal with other types
      	    check_error(ALWAYS_FAILS,std::string("Don't now how to deal with enum values of type ") + enumValue->Value());
      	}
      }	      
  }

  void InterpretedDbClientTransactionPlayer::playDefineType(const TiXmlElement &)
  {
      // TODO: jrb
  }  
  
    /*
     * DataRef
     */   
    DataRef DataRef::null;
    
    DataRef::DataRef(AbstractDomain* d)
    {
    	m_value.domain = d;
    }
    
    DataRef::DataRef(const AbstractDomain* d)
    {
    	m_value.constDomain = d;
    }
    
    DataRef::~DataRef()
    {
    	// TODO: should DataRef delete the AbstractDomain?, it'll prevent leaks in many cases
    }
  	    
    AbstractDomain* DataRef::getValue() { return m_value.domain; }
    
    const AbstractDomain* DataRef::getConstValue() const { return m_value.constDomain; }
  	        
    /*
     * TIVariable
     */   
    TIVariable::TIVariable(const char* name,const AbstractDomain* value)
        : m_name(name)
        , m_value(value)
    {
    }

    TIVariable::~TIVariable()
    {
    }

    const char* TIVariable::getName() const { return m_name; }
    DataRef& TIVariable::getDataRef() { return m_value; }

    /*
     * EvalContext
     */
    EvalContext::EvalContext(EvalContext* parent)
        : m_parent(parent)
    {
    } 
    
    EvalContext::~EvalContext()
    {
    }

  	void EvalContext::addVar(const char* name,const AbstractDomain* value)
  	{
  		TIVariable v(name,value);
  		m_variables[name] = v;
  	} 

  	TIVariable* EvalContext::getVar(const char* name)
  	{
  		std::map<std::string,TIVariable>::iterator it = 
  		    m_variables.find(name);
  		    
        if( it != m_variables.end() ) 
            return &(it->second);
  	    else if (m_parent != NULL)
  	    	return m_parent->getVar(name);
  	    else 
  	        return NULL;
  	}

    /*
     * ExprConstructorSuperCall
     */   
  	ExprConstructorSuperCall::ExprConstructorSuperCall(const LabelStr& superClassName, const std::vector<Expr*>& argExprs)
  	    : m_superClassName(superClassName)
  	    , m_argExprs(argExprs)
  	{
  	}
  	
  	ExprConstructorSuperCall::~ExprConstructorSuperCall()
  	{
  	}
  	
  	DataRef ExprConstructorSuperCall::eval(EvalContext& context) const
  	{
  		ObjectId object = context.getVar("this")->getDataRef().getValue()->getSingletonValue();

  		std::vector<const AbstractDomain*> arguments;

        evalArgs(context,arguments);  		
  		ObjectFactory::evalConstructorBody(object,m_superClassName,arguments);
  		
  		return DataRef::null;
  	}      	 

    void ExprConstructorSuperCall::evalArgs(EvalContext& context, std::vector<const AbstractDomain*>& arguments) const
    {
  		for (unsigned int i=0; i < m_argExprs.size(); i++) {
			DataRef arg = m_argExprs[i]->eval(context);
			arguments.push_back(arg.getConstValue());
  		}
    }

    /*
     * ExprConstructorAssignment
     */   
  	ExprConstructorAssignment::ExprConstructorAssignment(const char* lhs, 
  	                                                     Expr* rhs)
  	    : m_lhs(lhs)
  	    , m_rhs(rhs)
  	{
  	}
  	
  	ExprConstructorAssignment::~ExprConstructorAssignment()
  	{
  	}
  	    
  	DataRef ExprConstructorAssignment::eval(EvalContext& context) const
  	{
  		ObjectId object = context.getVar("this")->getDataRef().getValue()->getSingletonValue();
     	const AbstractDomain* domain = m_rhs->eval(context).getConstValue();
     	check_error(object->getVariable(m_lhs) == ConstrainedVariableId::noId());
     	object->addVariable(*domain,m_lhs);
     	std::cout << "Initialized variable:" << object->getName().toString() << "." << m_lhs << " in constructor" << std::endl;
  		
  		return DataRef::null;
  	} 

    /*
     * ExprConstant
     */   
    ExprConstant::ExprConstant(const AbstractDomain* d)
        : m_data(d)
    {
    }
    
  	ExprConstant::~ExprConstant()
  	{
  	}

  	DataRef ExprConstant::eval(EvalContext& context) const
  	{
  		return m_data;
  	}  
  	    

    /*
     * ExprVariableRef
     */   
    ExprVariableRef::ExprVariableRef(const char* varName)
        : m_varName(varName)
    {
    }
  	
  	ExprVariableRef::~ExprVariableRef()
  	{
  	}

  	DataRef ExprVariableRef::eval(EvalContext& context) const
  	{
     	TIVariable* rhs = context.getVar(m_varName);
     	return rhs->getDataRef();
  	}  

    /*
     * ExprNewObject
     */   
    ExprNewObject::ExprNewObject(const DbClientId& dbClient,
	                             const LabelStr& objectType, 
	                             const LabelStr& objectName,
	                             const std::vector<Expr*>& argExprs)
	    : m_dbClient(dbClient)
	    , m_objectType(objectType)
	    , m_objectName(objectName)
	    , m_argExprs(argExprs)
	{
	}
	    
    ExprNewObject::~ExprNewObject()
    {
    }

  	DataRef ExprNewObject::eval(EvalContext& context) const
  	{  
  		std::vector<const AbstractDomain*> arguments;
  		for (unsigned int i=0; i < m_argExprs.size(); i++) {
			DataRef arg = m_argExprs[i]->eval(context);
			arguments.push_back(arg.getConstValue());
  		}
  		
  		// TODO: when this is the rhs of an assignment in a constructor, this must create an object specifying 
  		// the enclosing object (for which the constructor is being executed) as the parent.
  		// The way it is now, objects are created :
  		// - directly in C++ through new (in the generated code) if the stmt is inside a NDDL constructor
  		// - through the factories if the stmtm is in the initial state
  		// This is a problem, everybody should go through the factory
  		// faking it for now, but this is a hack

  		// This assumes for now this is only called inside a constructor  		
  		ObjectId thisObject = context.getVar("this")->getDataRef().getValue()->getSingletonValue();
  		LabelStr name(std::string(thisObject->getName().toString() + "." + m_objectName.toString()));  		
     	ObjectId newObject = m_dbClient->createObject(
     	    m_objectType.c_str(), 
     	    name.c_str(), 
     	    arguments
     	);
     	newObject->setParent(thisObject);
     	 
	    // TODO: must not leak ObjectDomain                
	    return DataRef(new ObjectDomain(newObject,m_objectType.c_str()));
  	}
  	     
    /*
     * InterpretedObjectFactory
     */     	     
	InterpretedObjectFactory::InterpretedObjectFactory(
	                               const char* className,
	                               const LabelStr& signature, 
	                               const std::vector<std::string>& constructorArgNames,
	                               const std::vector<std::string>& constructorArgTypes,
	                               ExprConstructorSuperCall* superCallExpr,
	                               const std::vector<Expr*>& constructorBody,
	                               bool canMakeNewObject)
	    : ConcreteObjectFactory(signature) 
	    , m_className(className)
	    , m_constructorArgNames(constructorArgNames)
	    , m_constructorArgTypes(constructorArgTypes)
	    , m_superCallExpr(superCallExpr)
	    , m_constructorBody(constructorBody)
	    , m_canMakeNewObject(canMakeNewObject)
	{
	}
	
	InterpretedObjectFactory::~InterpretedObjectFactory()
	{
		if (m_superCallExpr != NULL)
    		delete m_superCallExpr;
		
		for (unsigned int i=0; i < m_constructorBody.size(); i++) 
		    delete m_constructorBody[i];
	}
	
	ObjectId InterpretedObjectFactory::createInstance(
	                        const PlanDatabaseId& planDb,
	                        const LabelStr& objectType, 
	                        const LabelStr& objectName,
	                        const std::vector<const AbstractDomain*>& arguments) const 
	{
	    check_error(checkArgs(arguments));
	  
	    ObjectId instance = makeNewObject(planDb, objectType, objectName,arguments);
	    evalConstructorBody(instance,arguments);
	    instance->close();
	  
	    return instance;
	} 

    bool InterpretedObjectFactory::checkArgs(const std::vector<const AbstractDomain*>& arguments) const
    {
    	// TODO: implement this
    	return true;
    }
    	
    /*
     * If a class has a native C++ ancestor the ancestor instance must be instanciated instead
     * - Every exported C++ class (like Timeline) must extend Object
     * - Every exported C++ class must register an InterpretedObjectFactory for each one of its constructors 
     *   that overrides this method and calls the appropriate constructor
     * 
	 */  
	ObjectId InterpretedObjectFactory::makeNewObject( 
	                        const PlanDatabaseId& planDb,
	                        const LabelStr& objectType, 
	                        const LabelStr& objectName,
	                        const std::vector<const AbstractDomain*>& arguments) const
	{
	  
      // go up the hierarchy and give the parents a chance to create the object, this allows native classes to be exported
      // TODO: some effort can be saved by keeping track of whether a class has a native ancestor different from Object.
      // If it doesn't, the object can be created right away and this traversal up the hierarchy can be skipped
	  if (m_canMakeNewObject) {
    	  std::cout << "Created Object:" << objectName.toString() << " type:" << objectType.toString() << std::endl;
	      return (new Object(planDb, objectType, objectName,true))->getId();
	  }
	  else {
	        check_error(m_superCallExpr != NULL, "Failed to find factory that can makeNewObject");
	        // TODO: argumentsForSuper are eval'd twice, once here and once when the constructor body is eval'd
	        //  figure out how to do it only once
	    	std::vector<const AbstractDomain*> argumentsForSuper;
     	    EvalContext evalContext(NULL);
     	    m_superCallExpr->evalArgs(evalContext,argumentsForSuper);
	        return ObjectFactory::makeNewObject(planDb, m_superCallExpr->getSuperClassName(), objectType, objectName,argumentsForSuper);
	  }
	}
	
	void InterpretedObjectFactory::evalConstructorBody(ObjectId& instance, 
	                                           const std::vector<const AbstractDomain*>& arguments) const
	{
        // TODO: need to pass in eval context from outside
	    EvalContext evalContext(NULL);
	    
        // Add new object and arguments to eval context
		ObjectDomain thisDomain(instance,m_className.c_str());		
		evalContext.addVar("this",&thisDomain);
		
		for (unsigned int i=0;i<m_constructorArgNames.size();i++) 
			evalContext.addVar(m_constructorArgNames[i].c_str(),arguments[i]);
		
		if (m_superCallExpr != NULL)
		    m_superCallExpr->eval(evalContext);
		
		for (unsigned int i=0; i < m_constructorBody.size(); i++) 
			m_constructorBody[i]->eval(evalContext);
		
		const std::vector<ConstrainedVariableId>& vars = instance->getVariables();
		std::cout << "    Vars for " << m_className.toString() << " " << instance->getName().toString() << " are:";
		for (unsigned j=0; j < vars.size(); j++) {
			std::cout << vars[j]->getName().toString() << ",";
		}
		std::cout << std::endl;
			
	    // Initialize any variables that were not explicitly initialized
	    const Schema::NameValueVector& members = Schema::instance()->getMembers(m_className);
	    for (unsigned int i=0; i < members.size(); i++) {
	    	std::string varName = instance->getName().toString() + "." + members[i].second.toString();
	        if (instance->getVariable(varName) == ConstrainedVariableId::noId()) {
	    	    AbstractDomain* baseDomain = TypeFactory::baseDomain(members[i].first.c_str()).copy(); 
	            instance->addVariable(
	                *baseDomain,
	                members[i].second.c_str()
	            );
	            delete baseDomain;
	            std::cout << "Used default initializer for " << m_className.toString() << "." << members[i].second.toString() << std::endl; 
	        } 
	    }
	}	
	
    /*
     * InterpretedToken
     */     	     
    InterpretedToken::InterpretedToken(const PlanDatabaseId& planDatabase, 
                     const LabelStr& predicateName, 
                     const bool& rejectable, 
                     const bool& close)
        : IntervalToken(planDatabase, 
                        predicateName,
                        rejectable,
                        IntervalIntDomain(),                  // start
                        IntervalIntDomain(),                  // end
                        IntervalIntDomain(1, PLUS_INFINITY),  // duration
                        Token::noObject(),                    // Object Name
                        false) 
    {
    }
  	                     
    InterpretedToken::InterpretedToken(const TokenId& master, 
                     const LabelStr& predicateName, 
                     const LabelStr& relation, 
                     const bool& close)
        : IntervalToken(master, 
                        relation,
                        predicateName,
                        IntervalIntDomain(),                 // start
                        IntervalIntDomain(),                 // end
                        IntervalIntDomain(1, PLUS_INFINITY), // duration
                        EUROPA::Token::noObject(),           // Object Name
                        false) 
    {
    }
        
    InterpretedToken::~InterpretedToken()
    {
    }
  
    /*
     * InterpretedRuleInstance
     */     	     
    InterpretedRuleInstance::InterpretedRuleInstance(const RuleId& rule, const TokenId& token, const PlanDatabaseId& planDb)
        : RuleInstance(rule, token, planDb)
    {
    }

    InterpretedRuleInstance::~InterpretedRuleInstance()
    {
    }

    void InterpretedRuleInstance::handleExecute()
    {
    	// TODO: Implement this
    }

    /*
     * InterpretedRuleFactory
     */     	     
    InterpretedRuleFactory::InterpretedRuleFactory(const LabelStr& predicate, const LabelStr& source) 
      : Rule(predicate,source)
    {
    }
        
    InterpretedRuleFactory::~InterpretedRuleFactory()
    {
    }
        
    RuleInstanceId InterpretedRuleFactory::createInstance(
                                  const TokenId& token, 
                                  const PlanDatabaseId& planDb, 
                                  const RulesEngineId &rulesEngine) const
    {
        InterpretedRuleInstance *foo = new InterpretedRuleInstance(m_id, token, planDb);
        foo->setRulesEngine(rulesEngine);
        return foo->getId();
    }	
}

