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

#include "ConstraintLibrary.hh"
#include "DbClient.hh"
#include "EnumeratedDomain.hh"
#include "EnumeratedTypeFactory.hh"
#include "Object.hh" 
#include "ObjectFactory.hh"
#include "TokenVariable.hh"
#include "TypeFactory.hh"
#include "Schema.hh"
#include "Utils.hh"

#include "NddlResource.hh"
#include "NddlRules.hh" 
#include "NddlUtils.hh"

// Hack!! the macro in NddlRules.hh only works with code-generation
// so this is redefined here. this is brittle though
// TODO: change code-generation code to work with this macro instead
namespace NDDL {
#define relation(relationname, origin, originvar, target, targetvar) {\
 std::vector<ConstrainedVariableId> vars;\
 vars.push_back(getSlave(LabelStr(origin))->get##originvar());\
 vars.push_back(getSlave(LabelStr(target))->get##targetvar());\
 rule_constraint(relationname,vars);\
  } 
}  


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
      
      // If it can't create objects, generate default super call
      if (!canCreateObjects) 
            superCallExpr = new ExprConstructorSuperCall(Schema::instance()->getParent(className),std::vector<Expr*>());                 	
      
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
  	  m_systemClasses.insert("Resource"); 
  	  m_systemClasses.insert("Resource.change");
  	  m_systemClasses.insert("Reusable");
  	  m_systemTokens.insert("Reusable.uses");
  	  // TODO: expose Reservoir and Unary
  	  
  	  // TODO: this should be done only once after the schema is initialized, not for every TransactionPlayer
  	  createDefaultObjectFactory("Object", true);
      REGISTER_OBJECT_FACTORY(TimelineObjectFactory, Timeline);	    	    	  

      REGISTER_OBJECT_FACTORY(ResourceObjectFactory, Resource);	    	    	  
      REGISTER_OBJECT_FACTORY(ResourceObjectFactory, Resource:float:float:float);	    	    	  
      REGISTER_OBJECT_FACTORY(ResourceObjectFactory, Resource:float:float:float:float:float);	    	    	  
      REGISTER_OBJECT_FACTORY(ResourceObjectFactory, Resource:float:float:float:float:float:float:float);	    	    	  
      new ResourceChangeTokenFactory("Resource.change");

      REGISTER_OBJECT_FACTORY(ReusableObjectFactory, Reusable);	    	    	  
      REGISTER_OBJECT_FACTORY(ReusableObjectFactory, Reusable:float:float);	    	    	  
      REGISTER_OBJECT_FACTORY(ReusableObjectFactory, Reusable:float:float:float);	    	    	  
      REGISTER_OBJECT_FACTORY(ReusableObjectFactory, Reusable:float:float:float:float);	    	    	  
      new ReusableUsesTokenFactory("Reusable.uses");
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
    debugMsg("XMLInterpreter:XML",dbgout.str());
  }
  
  void InterpretedDbClientTransactionPlayer::playDefineClass(const TiXmlElement& element) 
  {
    const char* className = element.Attribute("name");

    const char* parentClassName = element.Attribute("extends");
    parentClassName = (parentClassName == NULL ? "Object" : parentClassName);

    // TODO: should do nothing here for native classes, need to make Plasma.nddl consistent with this
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
                          
    bool definedConstructor = false;                          
    for(const TiXmlElement* child = element.FirstChildElement(); child; child = child->NextSiblingElement() ) {    	
    	const char * tagname = child->Value();
    	
	    if (strcmp(tagname, "var") == 0) 
	    	defineClassMember(schema,className,child);
	    else if (strcmp(tagname, "constructor") == 0) { 
            if (m_systemClasses.find(className) == m_systemClasses.end()) {
	    	    defineConstructor(schema,className,child);
	    	    definedConstructor = true;
            }
            else {	
            	// TODO: should always be displayed as WARNING!
    	        debugMsg("XMLInterpreter:XML","Skipping constructor declaration for System class : " << className);
	        }	  
	    }  	    	
	    else if (strcmp(tagname, "predicate") == 0) 
	    	declarePredicate(schema,className,child);	    	
	    else if (strcmp(tagname, "enum") == 0) 
	    	defineEnum(schema,className,child);	  
	    else
	        check_runtime_error(ALWAYS_FAILS,std::string("Unexpected element ")+tagname+" while defining class "+className);  	
    }

    // Register a default factory with no arguments if one is not provided explicitly
    if (!definedConstructor &&
        m_systemClasses.find(className) == m_systemClasses.end()) { 
        	createDefaultObjectFactory(className, false);
        	dbgout << "    generated default constructor" << std::endl;
    }
    
    dbgout << "}" << std::endl;    
    debugMsg("XMLInterpreter:XML",dbgout.str());     
  }

  Expr* InterpretedDbClientTransactionPlayer::valueToExpr(const TiXmlElement* element, bool isRule)
  {  	
    check_runtime_error(element != NULL,"Unexpected NULL element, expected value or id element");
    
    if (strcmp(element->Value(),"value") == 0 ||
        strcmp(element->Value(),"symbol") == 0 ||
        strcmp(element->Value(),"interval") == 0 ||
      	strcmp(element->Value(),"set") == 0) {
     	return new ExprConstant(m_client,element->Attribute("type"),xmlAsAbstractDomain(*element));
    }
    else if (strcmp(element->Value(),"id") == 0) {
      	const char* varName = element->Attribute("name");
      	if (isRule)
      	    return new ExprRuleVariableRef(varName);
      	else
      	    return new ExprVariableRef(varName);
    }
    else
        check_runtime_error(ALWAYS_FAILS,std::string("Unexpected xml element:") + element->Value() + ", expected constant(value,symbol,interval) or id element");
        
    return NULL;        
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
            for(const TiXmlElement* argChild = child->FirstChildElement(); argChild; argChild = argChild->NextSiblingElement() ) 
            	argExprs.push_back(valueToExpr(argChild,false));

      	  	superCallExpr = new ExprConstructorSuperCall(Schema::instance()->getParent(className),argExprs);
      	  }
      	  else if (strcmp(child->Value(),"assign") == 0) {
      	  	const TiXmlElement* rhsChild = child->FirstChildElement();
      	  	const char* lhs = child->Attribute("name");

      	  	Expr* rhs=NULL;
      	  	const char* rhsType = rhsChild->Value();      	  	
      	  	if (strcmp(rhsType,"new") == 0) {
	          	 const char* objectType = rhsChild->Attribute("type");      	  		
      	  		
	      	  	std::vector<Expr*> argExprs;     	  	
	            for(const TiXmlElement* argChild = rhsChild->FirstChildElement(); argChild; argChild = argChild->NextSiblingElement() ) 
	          	    argExprs.push_back(valueToExpr(argChild,false));

      	  		rhs = new ExprNewObject(
      	  		    m_client,
      	  		    objectType,
      	  		    lhs,
      	  		    argExprs
      	  		);
      	  	}
      	  	else 
      	  	    rhs = valueToExpr(rhsChild,false);

      	  	constructorBody.push_back(new ExprConstructorAssignment(lhs,rhs));
      	  }
          else
            check_runtime_error(ALWAYS_FAILS,std::string("Unexpected xml element:") + child->Value());
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

      std::vector<LabelStr> parameterNames;
      std::vector<LabelStr> parameterTypes;
      std::vector<LabelStr> assignVars;
      std::vector<Expr*> assignValues;
      std::vector<ExprConstraint*> constraints;
      
      dbgout << ident << "predicate " <<  predName << "(";
      for(const TiXmlElement* predArg = element->FirstChildElement(); predArg; predArg = predArg->NextSiblingElement() ) {
      	if (strcmp(predArg->Value(),"var") == 0) {
	      const char* type = safeStr(predArg->Attribute("type"));	
	      const char* name = safeStr(predArg->Attribute("name"));	
	      dbgout << type << " " << name << ",";
          schema->addMember(predName.c_str(), type, name);
          parameterNames.push_back(name);
          parameterTypes.push_back(type);
      	}
      	else if (strcmp(predArg->Value(),"assign") == 0) {
	      const char* type = safeStr(predArg->Attribute("type"));	
	      const char* name = safeStr(predArg->Attribute("name"));
	      bool inherited = (predArg->Attribute("inherited") != NULL ? true : false);	
	      dbgout << type << " " << name << ",";
	      if (!inherited)
              schema->addMember(predName.c_str(), type, name);
          assignVars.push_back(name);
          assignValues.push_back(valueToExpr(predArg->FirstChildElement()));                 
      	}
      	else if (strcmp(predArg->Value(),"invoke") == 0) {
      		dbgout << "constraint " << predArg->Attribute("name");
      		std::vector<Expr*> constraintArgs;
            for(const TiXmlElement* arg = predArg->FirstChildElement(); arg; arg = arg->NextSiblingElement() ) 
            	constraintArgs.push_back(valueToExpr(arg));
            	
      		constraints.push_back(new ExprConstraint(predArg->Attribute("name"),constraintArgs));
      	}             
        else
            check_runtime_error(ALWAYS_FAILS,std::string("Unexpected xml element:") + predArg->Value()+ " in predicate "+predName);
      }	
      dbgout << ")" << std::endl;

      if (m_systemTokens.find(predName) != m_systemTokens.end()) {
          // TODO: should always be displayed as WARNING!
	      debugMsg("XMLInterpreter:XML","Skipping factory registration for System token : " << predName); 
	      return;    
      }    
         
      // The TokenFactory's constructor automatically registers the factory
      new InterpretedTokenFactory(
          predName,
          parameterNames,
          parameterTypes,
          assignVars,
          assignValues,
          constraints
      );
  }
    
  void InterpretedDbClientTransactionPlayer::defineEnum(Id<Schema>& schema, const char* className,  const TiXmlElement* element)
  {	
  	  // Enum is scoped within the class but in the generated code it doesn't make a difference
  	  playDefineEnumeration(*element);
  }

  bool isClass(LabelStr className)
  {  	  
  	  return Schema::instance()->isObjectType(className);
  }
    
  LabelStr getObjectVarClass(LabelStr className,LabelStr var)
  {
      const SchemaId& schema = Schema::instance();
      check_runtime_error(schema->hasMember(className,var),className.toString()+" has no member called "+var.toString());
      return schema->getMemberType(className,var);
  }

  LabelStr getTokenVarClass(LabelStr className,LabelStr predName,LabelStr var)
  {
      if (strcmp(var.c_str(),"object") == 0) // is it the object variable? 
      	return className;
      else { // look through the parameters to the token
          const SchemaId& schema = Schema::instance();
          if (schema->hasMember(predName,var))
              return schema->getMemberType(predName,var); 	
      } 
      
      // if everything else fails, see if it's an object member	
      return getObjectVarClass(className,var);
  }

  LabelStr checkPredicateType(LabelStr type)
  {
  	check_runtime_error(Schema::instance()->isPredicate(type),type.toString()+" is not a Type");
  	return type;
  }
  
  /*
   * figures out the type of a predicate given an instance
   * 
   */
  LabelStr predicateInstanceToType(const char* className,
                                   const char* predicateName, 
                                   const char* predicateInstance,
                                   std::map<std::string,std::string>& localVars)
  {
  	// see ModelAccessor.getSlaveObjectType() in NDDL compiler  	
  	LabelStr str(predicateInstance);
  	
  	unsigned int tokenCnt = str.countElements(".");
  	
  	if (tokenCnt == 1) {
  		std::string retval = std::string(className)+"."+predicateInstance;
  		return checkPredicateType(LabelStr(retval));
  	}
  	else if (tokenCnt == 2) {
  		LabelStr prefix(str.getElement(0,"."));
  		LabelStr suffix(str.getElement(1,"."));
  		
  		if (prefix.toString() == "object") {
      		std::string retval = std::string(className)+"."+suffix.toString();
  		    return checkPredicateType(LabelStr(retval.c_str()));
  		}
  		else if (isClass(prefix)) {
  		     return checkPredicateType(LabelStr(predicateInstance));
  		}
  		else if (localVars.find(prefix.toString()) != localVars.end()) {
  			std::string clazz = localVars[prefix.toString()];
  			return checkPredicateType(clazz+"."+suffix.toString());
  		}
  		else {
     		LabelStr clazz = getTokenVarClass(className,predicateName,prefix);
      		std::string retval = clazz.toString()+"."+suffix.toString();
  		    return checkPredicateType(LabelStr(retval.c_str()));
  		}
  	}
  	else {
  	    LabelStr var = str.getElement(0,".");
  		LabelStr clazz;
  		if (localVars.find(var.toString()) != localVars.end()) 
  		    clazz = localVars[var.toString()];
  		else 
  		    clazz = getTokenVarClass(className,predicateName,var);
  		    
  		for (unsigned int i=1;i<tokenCnt-1;i++) {
  			LabelStr var = str.getElement(i,".");
  			clazz = getObjectVarClass(clazz,var);
  		}
  		
  		LabelStr predicate = str.getElement(tokenCnt-1,".");
  		std::string retval = clazz.toString() + "." + predicate.toString();  	    
  		return checkPredicateType(LabelStr(retval));  		
  	}  	  	
  }
  
  bool isGuarded(const LabelStr& varName,const TiXmlElement* node)
  {
  	if (node->Parent() == NULL)
  	    return false;
  	    
    if(strcmp(node->Value(),"id") == 0) {
      const char* name = node->Attribute("name");

      const TiXmlElement* parent = (const TiXmlElement*)node->Parent();
      if (strcmp(name,varName.c_str())==0 &&
          (
              strcmp(parent->Value(),"if")==0 || 
              strcmp(parent->Value(),"equals")==0 ||
              strcmp(parent->Value(),"nequals")==0
          )
      )
        return true; /* We are done */
    }

    for(const TiXmlElement* child = node->FirstChildElement(); child; child = child->NextSiblingElement() ) {
      if(isGuarded(varName, child))
        return true;
    }

    return false;
  }
  
  void InterpretedDbClientTransactionPlayer::buildRuleBody(
                                          const char* className,
                                          const std::string& predName,
                                          const TiXmlElement* element, 
                                          std::vector<RuleExpr*>& ruleBody,
                                          std::map<std::string,std::string>& localVars)
  {
      int slave_cnt = 0;      
      
      for(const TiXmlElement* child = element->FirstChildElement(); child; child = child->NextSiblingElement() ) {
      	if (strcmp(child->Value(),"invoke") == 0) {

      		std::vector<Expr*> constraintArgs;
            for(const TiXmlElement* arg = child->FirstChildElement(); arg; arg = arg->NextSiblingElement() ) 
            	constraintArgs.push_back(valueToExpr(arg));
            	
      		ruleBody.push_back(new ExprConstraint(child->Attribute("name"),constraintArgs));
      	}
      	else if (strcmp(child->Value(),"subgoal") == 0) {
      		const char* predicateInstance = NULL;
      		const char* name = NULL;
            for(const TiXmlElement* arg = child->FirstChildElement(); arg; arg = arg->NextSiblingElement() ) {
            	if (strcmp(arg->Value(),"predicateinstance") == 0) { 
            		predicateInstance = arg->Attribute("type");
            		name = arg->Attribute("name");
            	}            		
             	else 
      	            check_runtime_error(ALWAYS_FAILS,std::string("Unknown subgoal element:") + arg->Value());
            }
            check_runtime_error(predicateInstance != NULL,"predicate instance in a subgoal cannot be null");

            const char* predicateType = predicateInstanceToType(className, predName.c_str(), predicateInstance,localVars).c_str();    
            if (name == NULL) {
            	std::ostringstream tmpname;
            	tmpname << "slave" << (slave_cnt++);            
                name = LabelStr(tmpname.str()).c_str();
            }                 
      		ruleBody.push_back(new ExprSubgoal(name,predicateType,predicateInstance,child->Attribute("relation")));
      	}
      	else if (strcmp(child->Value(),"var") == 0) {
      		LabelStr name(child->Attribute("name"));
      		LabelStr type(child->Attribute("type"));
      		localVars[name.toString()]=type.toString();

            Expr* domainRestriction=NULL;        		
      		if (child->FirstChildElement() != NULL)
      	        domainRestriction=valueToExpr(child->FirstChildElement());
      	              		
      		ruleBody.push_back(new ExprLocalVar(name,type, isGuarded(name,element), domainRestriction));
      	}
      	else if (strcmp(child->Value(),"if") == 0) {
      		const TiXmlElement* opElement = child->FirstChildElement();
      		const TiXmlElement* opArg = opElement->FirstChildElement();
      		
      		Expr *lhs=NULL,*rhs=NULL;
      		std::string op = "equals";      		
      		      		
      		if (opArg != NULL) {
      		    lhs = valueToExpr(opArg);
      		    rhs = valueToExpr(opArg->NextSiblingElement());
      		    op = opElement->Value();
      		}
      		else {
      		    lhs = valueToExpr(opElement);
      		}
      		
      		std::vector<RuleExpr*> ifBody;
      		buildRuleBody(className,predName,opElement->NextSiblingElement(),ifBody,localVars);
      		ruleBody.push_back(new ExprIf(op.c_str(),lhs,rhs,ifBody));
      	}
      	else if (strcmp(child->Value(),"loop") == 0) {
      		const char* varName = child->Attribute("name");
      		const char* varType = child->Attribute("type");
      		const char* varValue = child->Attribute("value");
      		localVars[varName]=varType;
      		std::vector<RuleExpr*> loopBody;
      		buildRuleBody(className,predName,child->FirstChildElement(),loopBody,localVars);
      		ruleBody.push_back(new ExprLoop(varName,varType,varValue,loopBody));
      	}
      	else 
      	   check_runtime_error(ALWAYS_FAILS,std::string("Unknown Compatibility element:") + child->Value());
      }      
  }
  
  void InterpretedDbClientTransactionPlayer::playDefineCompat(const TiXmlElement& element)
  {
  	  const char* className = element.Attribute("class");
      std::string predName = std::string(className) + "." + element.Attribute("name");	
      std::string source = std::string(element.Attribute("filename")) + 
          " line " + element.Attribute("line") +
          " column " + element.Attribute("column");	
      
      std::vector<RuleExpr*> ruleBody;
      std::map<std::string,std::string> localVars; // name-type map for local vars
      buildRuleBody(className,predName,element.FirstChildElement(),ruleBody,localVars);      

      // The RuleFactory's constructor automatically registers the factory
      new InterpretedRuleFactory(predName,source,ruleBody);
  }
  
  void InterpretedDbClientTransactionPlayer::playDefineEnumeration(const TiXmlElement &element)
  {
      const char* enumName = element.Attribute("name");
      const TiXmlElement* setElement = element.FirstChildElement();
      check_error(strcmp(setElement->Value(),"set") == 0, "Expected value set as part of Enum definition");
      
      Id<Schema> schema = Schema::instance();
      schema->addEnum(enumName);
      
      std::list<double> values;
      const TiXmlElement* enumValue;
      for(enumValue = setElement->FirstChildElement(); enumValue; enumValue = enumValue->NextSiblingElement() ) {
      	double newValue=0;
      	
      	if (strcmp(enumValue->Value(),"symbol") == 0) {
      		LabelStr symbolValue(enumValue->Attribute("value"));
      		newValue = symbolValue;
      	}
      	else {
      	    check_runtime_error(ALWAYS_FAILS,std::string("Don't know how to deal with enum values of type ") + enumValue->Value());
      	}
      	
        schema->addValue(enumName, newValue);
        values.push_back(newValue);
      }	      
      
      new EnumeratedTypeFactory(
        enumName,
        enumName,
        EnumeratedDomain(values,false,enumName)
    );             
      
  }

  void InterpretedDbClientTransactionPlayer::playDefineType(const TiXmlElement& element)
  {
      const char* name = element.Attribute("name");

      const AbstractDomain* restrictedDomain = NULL;
      if (element.FirstChildElement() != NULL)
          restrictedDomain =  xmlAsAbstractDomain(*(element.FirstChildElement()),"");
      
      const AbstractDomain& domain = (
          restrictedDomain != NULL 
              ? *restrictedDomain 
              : TypeFactory::baseDomain(element.Attribute("basetype"))
      );
      
      
      if (domain.isEnumerated()) 
          new EnumeratedTypeFactory(name,name,domain);
      else
          new IntervalTypeFactory(name,domain);         
              
      std::string domainString = domain.toString();        
      debugMsg("XMLInterpreter:typedef", "Created type factory " << name 
                  << " with base domain " << domainString);
                  
      if (restrictedDomain != NULL)
          delete restrictedDomain;   
          
      // TODO: this is what the code generator does for every typedef, it doesn't seem right for interval types though    
      Schema::instance()->addEnum(name);          
  }  
  
    /*
     * DataRef
     */   
    DataRef DataRef::null;
    
    DataRef::DataRef()
        : m_value(ConstrainedVariableId::noId())
    {
    }
    
    DataRef::DataRef(const ConstrainedVariableId& v)
        : m_value(v)
    {
    }
    
    DataRef::~DataRef()
    {
    }
  	    
    ConstrainedVariableId& DataRef::getValue() { return m_value; }
    
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

  	void EvalContext::addVar(const char* name,const ConstrainedVariableId& v)
  	{
  		m_variables[name] = v;
  		debugMsg("XMLInterpreter:EvalContext","Added var:" << name << " to EvalContext");
  	} 

  	ConstrainedVariableId EvalContext::getVar(const char* name)
  	{
  		std::map<std::string,ConstrainedVariableId>::iterator it = 
  		    m_variables.find(name);
  		    
        if( it != m_variables.end() ) 
            return it->second;
  	    else if (m_parent != NULL)
  	    	return m_parent->getVar(name);
  	    else 
  	        return ConstrainedVariableId::noId();
  	}

  	void EvalContext::addToken(const char* name,const TokenId& t)
  	{
  		m_tokens[name] = t;
  	} 

  	TokenId EvalContext::getToken(const char* name)
  	{
  		std::map<std::string,TokenId>::iterator it = 
  		    m_tokens.find(name);
  		    
        if( it != m_tokens.end() ) 
            return it->second;
  	    else if (m_parent != NULL)
  	    	return m_parent->getToken(name);
  	    else 
  	        return TokenId::noId();
  	}

    std::string EvalContext::toString() const
    {
    	std::ostringstream os;
    	
    	if (m_parent == NULL)
    	    os << "EvalContext {" << std::endl;    
    	else
    	    os << m_parent->toString();
    	    
  		std::map<std::string,ConstrainedVariableId>::const_iterator varIt = m_variables.begin();    	
    	os << "    vars {";    
    	for (;varIt != m_variables.end();++varIt)
    	    os << varIt->first << " " << varIt->second->toString() << ",";
    	os << "    }" << std::endl;
    	
  		std::map<std::string,TokenId>::const_iterator tokenIt = m_tokens.begin();    	
    	os << "    tokens {";    
    	for (;tokenIt != m_tokens.end();++tokenIt)
    	    os << tokenIt->first << " " << tokenIt->second->getPredicateName().toString() << ",";
    	os << "    }"  << std::endl;
    	
    	if (m_parent == NULL)
    	    os << "}" << std::endl;
    	    
    	return os.str();        	    
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
  		ObjectId object = context.getVar("this")->derivedDomain().getSingletonValue();

  		std::vector<const AbstractDomain*> arguments;

        evalArgs(context,arguments);  		
  		ObjectFactory::evalConstructorBody(object,m_superClassName,arguments);
  		
  		return DataRef::null;
  	}      	 

    void ExprConstructorSuperCall::evalArgs(EvalContext& context, std::vector<const AbstractDomain*>& arguments) const
    {
  		for (unsigned int i=0; i < m_argExprs.size(); i++) {
			DataRef arg = m_argExprs[i]->eval(context);
			arguments.push_back(&(arg.getValue()->derivedDomain()));
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
  		ObjectId object = context.getVar("this")->derivedDomain().getSingletonValue();
     	check_error(object->getVariable(m_lhs) == ConstrainedVariableId::noId());
     	ConstrainedVariableId rhsValue = m_rhs->eval(context).getValue(); 
     	const AbstractDomain& domain = rhsValue->derivedDomain();
     	object->addVariable(domain,m_lhs);
     	debugMsg("XMLInterpreter:InterpretedObject","Initialized variable:" << object->getName().toString() << "." << m_lhs << " to " << rhsValue->derivedDomain().toString() << " in constructor");
  		
  		return DataRef::null;
  	} 

    /*
     * ExprConstant
     */   
    ExprConstant::ExprConstant(DbClientId& dbClient, const char* type, const AbstractDomain* domain)
        : m_dbClient(dbClient)
        , m_type(type)
        , m_domain(domain)
    {
    }
    
  	ExprConstant::~ExprConstant()
  	{
  	}

  	DataRef ExprConstant::eval(EvalContext& context) const
  	{
  		// need to create a new variable every time this is evaluated, since propagation
  		// will affect the variable, should provide immutable variables so that a single var
  		// can be created for a constant
  		// TODO: Who destroys this variable?, need to give handle to RuleInstance
		ConstrainedVariableId var = m_dbClient->createVariable(
		    m_type.c_str(),
		    *m_domain,
		    "TMP_VAR",
		    true, // isTmp
		    false // cannot be specified
		);    	
		
  		return DataRef(var);
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
  		// TODO: do this only once
  		std::vector<std::string> vars;
  		tokenize(m_varName.toString(),vars,".");
  		std::string varName=vars[0];
  		
     	ConstrainedVariableId rhs = context.getVar(vars[0].c_str());
     	
   	    if (rhs.isNoId()) 
	        check_runtime_error(ALWAYS_FAILS,std::string("Couldn't find variable ")+varName+" in Evaluation Context");
     	
     	for (unsigned int idx = 1;idx<vars.size();idx++) {
     		check_error(isClass(rhs->baseDomain().getTypeName()), std::string("Can't apply dot operator to:")+rhs->baseDomain().getTypeName().toString());
     		check_runtime_error(rhs->derivedDomain().isSingleton(),varName+" must be singleton to be able to get to "+vars[idx]);
     		ObjectId object = rhs->derivedDomain().getSingletonValue();
     		rhs = object->getVariable(object->getName().toString()+"."+vars[idx]);
     		varName += "." + vars[idx];
         	if (rhs.isNoId()) 
     	        check_runtime_error(ALWAYS_FAILS,std::string("Couldn't find variable ")+vars[idx]+
     	            " in object \""+object->getName().toString()+"\" of type "+object->getType().toString());
     	}
     	    
     	return DataRef(rhs);
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
			arguments.push_back(&(arg.getValue()->derivedDomain()));
  		}
  		
  		// when this is the rhs of an assignment in a constructor, an object var must be created to specify 
  		// the enclosing object (for which the constructor is being executed) as the parent.
  		// TODO: The way it is now, when using code generation objects are created :
  		// - directly in C++ through new (in the generated code) if the stmt is inside a NDDL constructor
  		// - through the factories if the stmt is in the initial state
  		// This is a problem, everybody should go through the factory
  		// faking it for now, but this is a hack

  		// This assumes for now this is only called inside a constructor, not as part of an initial-state   		
  		ObjectId thisObject = context.getVar("this")->derivedDomain().getSingletonValue();
  		LabelStr name(std::string(thisObject->getName().toString() + "." + m_objectName.toString()));  		
     	ObjectId newObject = m_dbClient->createObject(
     	    m_objectType.c_str(), 
     	    name.c_str(), 
     	    arguments
     	);
     	newObject->setParent(thisObject);
     	 
	    return DataRef(newObject->getThis());
  	}
  	
  	
    /*
     * ExprRuleVariableRef
     */   
    ExprRuleVariableRef::ExprRuleVariableRef(const char* varName)
        : m_varName(varName)
    {
  		std::vector<std::string> vars;    	
  		tokenize(m_varName,vars,".");
  		
  		if (vars.size() > 1) {
  		    m_parentName = vars[0];
  		    m_varName = m_varName.substr(m_parentName.length()+1);
  		    debugMsg("XMLInterpreter:InterpretedRule","Split " << varName << " into " << m_parentName << " and " << m_varName);
  		}
  		else {
  			m_parentName = "";
  		    debugMsg("XMLInterpreter:InterpretedRule","Didn't split " << varName);
  		} 
    }
  	
  	ExprRuleVariableRef::~ExprRuleVariableRef()
  	{
  	}

    DataRef ExprRuleVariableRef::doEval(RuleInstanceEvalContext& context) const 	    
  	{

     	ConstrainedVariableId var; 
     	
     	if (m_parentName == "") {
     	    var = context.getVar(m_varName.c_str());     	    
            check_runtime_error(!var.isNoId(),std::string("Couldn't find variable ")+m_varName+" in Evaluation Context");
     	}  
     	else {
    		TokenId tok = context.getToken(m_parentName.c_str());
   		    if (!tok.isNoId())  
   	            var = context.getRuleInstance()->varfromtok(tok,m_varName);         			
   	        else {    
     		    var = context.getVar(m_parentName.c_str());
     		    if (!var.isNoId()) 
     			    var = context.getRuleInstance()->varFromObject(m_parentName,m_varName,false);
     			else
   	                check_runtime_error(ALWAYS_FAILS,std::string("Couldn't find variable ")+m_parentName+" in Evaluation Context");
     		}
     	}   	    
     	
     	return DataRef(var);
  	}  
  	
  	ExprConstraint::ExprConstraint(const char* name,const std::vector<Expr*> args)
  	    : m_name(name)
  	    , m_args(args)
  	{
  	}

  	ExprConstraint::~ExprConstraint()
  	{
  	}

    std::string varsToString(const std::vector<ConstrainedVariableId>& vars)
    {
  		std::ostringstream os;
  		for (unsigned int i=0; i < vars.size(); i++) {
  			if (i>0) os << ",";
			os << vars[i]->toString();
  		}
  		
  		return os.str();
    }
    
  	DataRef ExprConstraint::doEval(RuleInstanceEvalContext& context) const
  	{
  		std::vector<ConstrainedVariableId> vars;
  		for (unsigned int i=0; i < m_args.size(); i++) {
			DataRef arg = m_args[i]->eval(context);
			vars.push_back(arg.getValue());
  		}

  		context.getRuleInstance()->createConstraint(m_name,vars);
  		debugMsg("XMLInterpreter:InterpretedRule","Evaluated Constraint : " << m_name.toString() << " - " << varsToString(vars));
  		return DataRef::null;
  	}  
  	    
  
  	ExprSubgoal::ExprSubgoal(const char* name,
  	                         const char* predicateType, 
  	                         const char* predicateInstance, 
  	                         const char* relation)
  	    : m_name(name)
  	    , m_predicateType(predicateType)
  	    , m_predicateInstance(predicateInstance)
  	    , m_relation(relation)
    {
    }  	      	    
  	
  	ExprSubgoal::~ExprSubgoal()
  	{
  	}

  	// see ModelAccessor.isConstrained in Nddl compiler 
    bool isConstrained(const LabelStr& predicateInstance)
    {
    	unsigned int tokenCnt = predicateInstance.countElements(".");
    	
    	// If the predicate is not qualified that means it belongs to the object in scope
    	if (tokenCnt == 1)
    	    return true;
    	    
    	// If the prefix is a class, it means it can be any object instance, so it must not be constrained    
        LabelStr prefix(predicateInstance.getElement(0,"."));
        if (!isClass(prefix))
            return true;
    	
    	return false;
    }    

  	DataRef ExprSubgoal::doEval(RuleInstanceEvalContext& context) const  
  	{
  		debugMsg("XMLInterpreter:InterpretedRule","Creating subgoal " << m_predicateType.toString() << ":" << m_name.toString());
  		
  		bool constrained = isConstrained(m_predicateInstance);
  		ConstrainedVariableId owner;
  		if (constrained) {
         	unsigned int tokenCnt = m_predicateInstance.countElements(".");
  			if (tokenCnt == 1) 
  			    owner = context.getVar("object");
  			else      
  			    owner = context.getVar(m_predicateInstance.getElement(0,".").c_str());  
  		}
  		  		
  		TokenId slave = context.getRuleInstance()->createSubgoal(
  		    m_name,
  		    m_predicateType,
  		    m_predicateInstance,
  		    m_relation,
  		    constrained,
  		    owner
  		);
  		
  		context.addToken(m_name.c_str(),slave);
  		debugMsg("XMLInterpreter:InterpretedRule","Created  subgoal " << m_predicateType.toString() << ":" << m_name.toString());
  		return DataRef::null;
  	}  
  	 
  	ExprLocalVar::ExprLocalVar(const LabelStr& name,const LabelStr& type, bool guarded, Expr* domainRestriction)
  	    : m_name(name)
  	    , m_type(type)
  	    , m_guarded(guarded)
  	    , m_domainRestriction(domainRestriction)
  		, m_baseDomain(TypeFactory::baseDomain(type.c_str())) 		
  	{
  	}
  	
  	ExprLocalVar::~ExprLocalVar()
  	{
  	}

  	DataRef ExprLocalVar::doEval(RuleInstanceEvalContext& context) const
  	{
  		ConstrainedVariableId localVar;
  		if (isClass(m_type))
  		  localVar = context.getRuleInstance()->addObjectVariable(
  		      m_type,
  		      ObjectDomain(m_type.c_str()),
  		      m_guarded, // can't be specified
  		      m_name
  		  );
  		else
  		  localVar = context.getRuleInstance()->addLocalVariable(
  		      m_baseDomain,
  		      m_guarded, // can't be specified
  		      m_name
  		  );
  		
  		if (m_domainRestriction != NULL)  
  		    localVar->restrictBaseDomain(m_domainRestriction->eval(context).getValue()->derivedDomain());
  		  
  		context.addVar(m_name.c_str(),localVar);
  		debugMsg("XMLInterpreter:InterpretedRule","Added RuleInstance local var:" << localVar->toString());
  		return DataRef::null;
  	}  
  	  
  	ExprIf::ExprIf(const char* op, Expr* lhs,Expr* rhs,const std::vector<RuleExpr*>& ifBody)
  	    : m_op(op)
  	    , m_lhs(lhs)
  	    , m_rhs(rhs)
  	    , m_ifBody(ifBody)
  	{
  	}
  	
  	ExprIf::~ExprIf()
  	{
  	}

  	DataRef ExprIf::doEval(RuleInstanceEvalContext& context) const
  	{
  		bool isOpEquals = (m_op == "equals");
  		
  		DataRef lhs = m_lhs->eval(context);
  		
  		// TODO: this assumes that the variable is always on the lhs and the value on the rhs
  		// is this enforced by the parser?
       
        if (m_rhs != NULL) {
  		    DataRef rhs = m_rhs->eval(context);
            context.getRuleInstance()->addChildRule(
                new InterpretedRuleInstance(
                    context.getRuleInstance()->getId(), 
                    lhs.getValue(), 
                    rhs.getValue()->derivedDomain(), 
                    isOpEquals,
                    m_ifBody
                )
            );
  		    debugMsg("XMLInterpreter:InterpretedRule","Evaluated IF " << m_op << " " << lhs.getValue()->toString() << " " << rhs.getValue()->toString());
        }
        else {
            context.getRuleInstance()->addChildRule(
                new InterpretedRuleInstance(
                    context.getRuleInstance()->getId(), 
                    makeScope(lhs.getValue()), 
                    isOpEquals,
                    m_ifBody
                )
            );
  		    debugMsg("XMLInterpreter:InterpretedRule","Evaluated IF " << m_op << " " << lhs.getValue()->toString());
        }
        
  		return DataRef::null;  		
  	}  

  	ExprLoop::ExprLoop(const char* varName, const char* varType, const char* varValue,const std::vector<RuleExpr*>& loopBody)
  	    : m_varName(varName)
  	    , m_varType(varType)
  	    , m_varValue(varValue)
  	    , m_loopBody(loopBody)
  	{
  	}
  	
  	ExprLoop::~ExprLoop()
  	{
  	}

  	DataRef ExprLoop::doEval(RuleInstanceEvalContext& context) const
  	{
        context.getRuleInstance()->executeLoop(context,m_varName,m_varType,m_varValue,m_loopBody);
  		debugMsg("XMLInterpreter:InterpretedRule","Evaluated LOOP " << m_varName.toString() << "," << m_varValue.toString());
  		return DataRef::null;  		
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
	
	class ObjectFactoryEvalContext : public EvalContext
	{
      public:
          ObjectFactoryEvalContext(const PlanDatabaseId& planDb,
                                   const std::vector<std::string>& argNames,
	                               const std::vector<std::string>& argTypes,
	                               const std::vector<const AbstractDomain*>& args)
	          : EvalContext(NULL) // TODO: should pass in eval context from outside to have access to globals                        
          {
		      // Add arguments to eval context		
		      for (unsigned int i=0;i<argNames.size();i++) {
				  ConstrainedVariableId arg = planDb->getClient()->createVariable(
				      argTypes[i].c_str(),
			          *(args[i]),
					  argNames[i].c_str(), 
					  true
				   );
				   m_tmpVars.push_back(arg);
				   addVar(argNames[i].c_str(),arg);
				}
          }
          
          virtual ~ObjectFactoryEvalContext()
          {
              for (unsigned int i=0;i<m_tmpVars.size();i++) {
              	  // TODO: must release temprary vars, causing crash?
                  //m_tmpVars[i].release();
              }
          }
      
      protected:
          std::vector<ConstrainedVariableId> m_tmpVars;        
	};
	
	ObjectId InterpretedObjectFactory::createInstance(
	                        const PlanDatabaseId& planDb,
	                        const LabelStr& objectType, 
	                        const LabelStr& objectName,
	                        const std::vector<const AbstractDomain*>& arguments) const 
	{
	    check_runtime_error(checkArgs(arguments));
        
	    ObjectFactoryEvalContext evalContext(
	        planDb,
	        m_constructorArgNames,
	        m_constructorArgTypes,
	        arguments
	    );	  			    
		m_evalContext = &evalContext;
		
	    ObjectId instance = makeNewObject(planDb, objectType, objectName,arguments);
		evalContext.addVar("this",instance->getThis());
	    evalConstructorBody(instance,arguments);
	    instance->close();
		
		m_evalContext = NULL;

	    return instance;
	} 

    bool InterpretedObjectFactory::checkArgs(const std::vector<const AbstractDomain*>& arguments) const
    {
    	// TODO: implement this. is this even necessary?, parser should take care of it
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
    	  debugMsg("XMLInterpreter:InterpretedObject","Created Object:" << objectName.toString() << " type:" << objectType.toString());
	      return (new Object(planDb, objectType, objectName,true))->getId();
	  }
	  else {
	        check_error(m_superCallExpr != NULL, std::string("Failed to find factory for object ") + objectName.toString() + " of type "+objectType.toString());
	        // TODO: argumentsForSuper are eval'd twice, once here and once when the constructor body is eval'd
	        //  figure out how to do it only once
	    	std::vector<const AbstractDomain*> argumentsForSuper;
	    	
	    	bool needsContext = (m_evalContext == NULL);
	    	if (needsContext) {
	    	    m_evalContext = new ObjectFactoryEvalContext(
	                planDb,
	                m_constructorArgNames,
	                m_constructorArgTypes,
	                arguments
	            );
	        }	  			    	    	    
     	    m_superCallExpr->evalArgs(*m_evalContext,argumentsForSuper);
     	    if (needsContext) {
     	    	delete m_evalContext;
     	    	m_evalContext = NULL;
     	    }
     	    
	        ObjectId retval = ObjectFactory::makeNewObject(planDb, m_superCallExpr->getSuperClassName(), objectType, objectName,argumentsForSuper);
  		    return retval;
	  }
	}
	
	void InterpretedObjectFactory::evalConstructorBody(
	                                           ObjectId& instance, 
	                                           const std::vector<const AbstractDomain*>& arguments) const
	{	    
		EvalContext& evalContext = *m_evalContext;
		if (m_superCallExpr != NULL)
		    m_superCallExpr->eval(evalContext);
		
		for (unsigned int i=0; i < m_constructorBody.size(); i++) 
			m_constructorBody[i]->eval(evalContext);

	    // Initialize any variables that were not explicitly initialized
	    const Schema::NameValueVector& members = Schema::instance()->getMembers(m_className);
	    for (unsigned int i=0; i < members.size(); i++) {
	    	std::string varName = instance->getName().toString() + "." + members[i].second.toString();
	        if (instance->getVariable(varName) == ConstrainedVariableId::noId()) {
	    	    const AbstractDomain& baseDomain = TypeFactory::baseDomain(members[i].first.c_str()); 
	            instance->addVariable(
	                baseDomain,
	                members[i].second.c_str()
	            );
	            debugMsg("XMLInterpreter:InterpretedObject","Used default initializer for " << m_className.toString() << "." << members[i].second.toString()); 
	        } 
	    }
	}	
	
    /*
     * InterpretedToken
     */     	     
    InterpretedToken::InterpretedToken(const PlanDatabaseId& planDatabase, 
                     const LabelStr& predicateName, 
                     const std::vector<LabelStr>& parameterNames,
                     const std::vector<LabelStr>& parameterTypes, 
                     const std::vector<LabelStr>& assignVars,
                     const std::vector<Expr*>& assignValues,
                     const std::vector<ExprConstraint*>& constraints,
                     const bool& rejectable, 
                     const bool& isFact,
                     const bool& close) 
                     
        : IntervalToken(planDatabase, 
                        predicateName,
                        rejectable,
                        isFact,
                        IntervalIntDomain(),                  // start
                        IntervalIntDomain(),                  // end
                        IntervalIntDomain(1, PLUS_INFINITY),  // duration
                        Token::noObject(),                    // Object Name
                        false) 
    {
    	commonInit(parameterNames, parameterTypes, assignVars, assignValues, constraints, close);
    	debugMsg("XMLInterpreter:InterpretedToken","Created token(" << getKey() << ") of type:" << predicateName.toString() << " objectVar=" << getVariable("object")->toString());
    }
  	                     
    InterpretedToken::InterpretedToken(const TokenId& master, 
                     const LabelStr& predicateName, 
                     const LabelStr& relation, 
                     const std::vector<LabelStr>& parameterNames,
                     const std::vector<LabelStr>& parameterTypes, 
                     const std::vector<LabelStr>& assignVars,
                     const std::vector<Expr*>& assignValues,
                     const std::vector<ExprConstraint*>& constraints,
                     const bool& close)
        : IntervalToken(master, 
                        relation,
                        predicateName,
                        IntervalIntDomain(),                 // start
                        IntervalIntDomain(),                 // end
                        IntervalIntDomain(1, PLUS_INFINITY), // duration
                        Token::noObject(),                   // Object Name
                        false) 
    {
    	commonInit(parameterNames, parameterTypes, assignVars, assignValues, constraints, close);
    	debugMsg("XMLInterpreter:InterpretedToken","Created slave token(" << getKey() << ") of type:" << predicateName.toString() << " objectVar=" << getVariable("object")->toString());
    }
        
    InterpretedToken::~InterpretedToken()
    {
    }

// slight modification of the version in NddlRules.hh that can be used with a variable name
// instead of a literal  
// TODO: make code generator use this instead
#define token_constraint1(name, vars)\
{\
  ConstraintId c0 = ConstraintLibrary::createConstraint(\
                            name,\
							m_planDatabase->getConstraintEngine(),\
							vars);\
  m_standardConstraints.insert(c0);\
}
  
    void InterpretedToken::commonInit(
                     const std::vector<LabelStr>& parameterNames,
                     const std::vector<LabelStr>& parameterTypes, 
                     const std::vector<LabelStr>& assignVars,
                     const std::vector<Expr*>& assignValues,
                     const std::vector<ExprConstraint*>& constraints,
                     const bool& autoClose)
    {
        //debugMsg("XMLInterpreter","Token " << getName().toString() << " has " << parameterNames.size() << " parameters"); 
	    for (unsigned int i=0; i < parameterNames.size(); i++) {
	        check_runtime_error(getVariable(parameterNames[i]) == ConstrainedVariableId::noId(), "Token parameter "+parameterNames[i].toString()+ " already exists!"); 
	    	
	    	// This is a hack needed because TokenVariable is parametrized by the domain arg to addParameter 
	        ConstrainedVariableId parameter;
	        
	        // same as completeObjectParam in NddlRules.hh
	        if (isClass(parameterTypes[i])) {
    	        parameter = addParameter(
	                ObjectDomain(parameterTypes[i].c_str()),
	                parameterNames[i]
	            );
                getPlanDatabase()->makeObjectVariableFromType(parameterTypes[i], parameter);
	        }
	        else {
    	        parameter = addParameter(
	                TypeFactory::baseDomain(parameterTypes[i].c_str()),
	                parameterNames[i]
	            );
	        }
                	        
            debugMsg("XMLInterpreter:InterpretedToken","Token " << getName().toString() << " added Parameter " 
                                               << parameter->toString() << " " << parameterNames[i].toString());
	    }
	    
        if (autoClose)
            close();    	
	    
   	    TokenEvalContext context(NULL,getId()); // TODO: give access to class or global context?
   	     
	    // Take care of initializations that were part of the predicate declaration
	    for (unsigned int i=0; i < assignVars.size(); i++) 
	        getVariable(assignVars[i])->restrictBaseDomain(assignValues[i]->eval(context).getValue()->baseDomain());

        // Post parameter constraints
	    for (unsigned int i=0; i < constraints.size(); i++) {
	    	const std::vector<Expr*>& args = constraints[i]->getArgs();
	  		std::vector<ConstrainedVariableId> constraintArgs;
	  		for (unsigned int j=0; j < args.size(); j++) {
				DataRef arg = args[j]->eval(context);
				constraintArgs.push_back(arg.getValue());
	  		}
    	    token_constraint1(constraints[i]->getName(),constraintArgs);
	    }    	
    }
      
    /*
     * InterpretedTokenFactory
     */     	     
    InterpretedTokenFactory::InterpretedTokenFactory(
                                       const LabelStr& predicateName,
                                       const std::vector<LabelStr>& parameterNames,
                                       const std::vector<LabelStr>& parameterTypes, 
                                       const std::vector<LabelStr>& assignVars,
                                       const std::vector<Expr*>& assignValues,
                                       const std::vector<ExprConstraint*>& constraints)
	    : ConcreteTokenFactory(predicateName) 
	    , m_parameterNames(parameterNames)
	    , m_parameterTypes(parameterTypes)
	    , m_assignVars(assignVars)
	    , m_assignValues(assignValues)
	    , m_constraints(constraints)
	{ 
	} 
	
	TokenId InterpretedTokenFactory::createInstance(const PlanDatabaseId& planDb, const LabelStr& name, bool rejectable, bool isFact) const 
	{ 
	    TokenId token = (new InterpretedToken(
	        planDb, 
	        name, 
	        m_parameterNames, 
	        m_parameterTypes,
	        m_assignVars,
	        m_assignValues,
	        m_constraints, 
	        rejectable,
	        isFact, 
	        true))->getId(); 
	    return token; 
	} 
	
	TokenId InterpretedTokenFactory::createInstance(const TokenId& master, const LabelStr& name, const LabelStr& relation) const 
	{ 
	    TokenId token = (new InterpretedToken(
	        master, 
	        name, 
	        relation, 
	        m_parameterNames, 
	        m_parameterTypes, 
	        m_assignVars,
	        m_assignValues,
	        m_constraints, 
	        true))->getId(); 
	    return token; 
    } 
	
	
    /*
     * RuleInstanceEvalContext
     * Puts RuleInstance variables like duration, start, end, in context
     */     	     
  	 RuleInstanceEvalContext::RuleInstanceEvalContext(EvalContext* parent, const InterpretedRuleInstanceId& ruleInstance) 
  	     : EvalContext(parent) 
  	     , m_ruleInstance(ruleInstance)
  	 {
  	 }
     
     RuleInstanceEvalContext::~RuleInstanceEvalContext()
     {
     }   	
  	    
  	 ConstrainedVariableId RuleInstanceEvalContext::getVar(const char* name)
  	 {
  	 	ConstrainedVariableId var = m_ruleInstance->getVariable(LabelStr(name));
  	 	
  	 	if (!var.isNoId()) {
  	 		debugMsg("XMLInterpreter:EvalContext:RuleInstance","Found var in rule instance:" << name);
  	 	    return var;
  	 	}  	 	    
  	 	else {
  	 		debugMsg("XMLInterpreter:EvalContext:RuleInstance","Didn't find var in rule instance:" << name);
  	 	    return EvalContext::getVar(name);
  	 	}
  	 }  
  	 
  	 std::string RuleInstanceEvalContext::toString() const 
  	 {
  	 	std::ostringstream os;
  	 	
  	 	os << EvalContext::toString();
  	 	
  	 	os << "Token variables {";
        const std::vector<ConstrainedVariableId>& vars = m_ruleInstance->getToken()->getVariables();
        for(std::vector<ConstrainedVariableId>::const_iterator it = vars.begin(); it != vars.end(); ++it){
            ConstrainedVariableId var = *it;
            os << var->getName().toString() << "," ;
        }        
        os << "}" << std::endl;
  	 	
  	 	return os.str();
  	 }	    
	 
    /*
     * RuleInstanceEvalContext
     * Puts Token variables like duration, start, end, in context
     */     	     
  	 TokenEvalContext::TokenEvalContext(EvalContext* parent, const TokenId& token) 
  	     : EvalContext(parent) 
  	     , m_token(token)
  	 {
  	 }
     
     TokenEvalContext::~TokenEvalContext()
     {
     }   	
  	    
  	 ConstrainedVariableId TokenEvalContext::getVar(const char* name)
  	 {
  	 	ConstrainedVariableId var = m_token->getVariable(LabelStr(name));
  	 	
  	 	if (!var.isNoId()) {
  	 		debugMsg("XMLInterpreter:EvalContext:Token","Found var in token :" << name);
  	 	    return var;
  	 	}  	 	    
  	 	else
  	 	    return EvalContext::getVar(name);
  	 }  	    
	 
    /*
     * InterpretedRuleInstance
     */     	     
    InterpretedRuleInstance::InterpretedRuleInstance(const RuleId& rule, 
                                                     const TokenId& token, 
                                                     const PlanDatabaseId& planDb,
                                                     const std::vector<RuleExpr*>& body)
        : RuleInstance(rule, token, planDb)
        , m_body(body)
    {
    }

    InterpretedRuleInstance::InterpretedRuleInstance(
                                const RuleInstanceId& parent, 
                                const ConstrainedVariableId& var, 
                                const AbstractDomain& domain, 
                                const bool positive,
                                const std::vector<RuleExpr*>& body)
        : RuleInstance(parent,var,domain,positive)
        , m_body(body)
    {
    }    
    
    InterpretedRuleInstance::InterpretedRuleInstance(
                                const RuleInstanceId& parent, 
                                const std::vector<ConstrainedVariableId>& vars, 
                                const bool positive,
                                const std::vector<RuleExpr*>& body)
        : RuleInstance(parent,vars,positive)
        , m_body(body)
    {
    }        

    InterpretedRuleInstance::~InterpretedRuleInstance()
    {
    }

    void InterpretedRuleInstance::handleExecute()
    {
        // TODO: should pass in eval context from outside
	    RuleInstanceEvalContext evalContext(NULL,getId());
	    
    	debugMsg("XMLInterpreter:InterpretedRule","Executing interpreted rule:" << getRule()->getName().toString() << " token:" << m_token->toString());
		for (unsigned int i=0; i < m_body.size(); i++) 
			m_body[i]->eval(evalContext);
    	debugMsg("XMLInterpreter:InterpretedRule","Executed  interpreted rule:" << getRule()->getName().toString() << " token:" << m_token->toString());
    }
    
    void InterpretedRuleInstance::createConstraint(const LabelStr& name, std::vector<ConstrainedVariableId>& vars)
    {
        addConstraint(name,vars);    	
    }

    TokenId InterpretedRuleInstance::createSubgoal(
                                        const LabelStr& name,
                                        const LabelStr& predicateType, 
                                        const LabelStr& predicateInstance, 
                                        const LabelStr& relation,
                                        bool isConstrained,
                                        ConstrainedVariableId& owner)
    {
  		TokenId slave = TokenFactory::createInstance(m_token,predicateType,relation);
  		addSlave(slave,name);  		

  		// For qualified names like "object.helloWorld" must add constraint to the object variable on the slave token
  		// See RuleWriter.allocateSlave in Nddl compiler 
  		if (isConstrained) {
  			std::vector<ConstrainedVariableId> vars;
  			    
         	unsigned int tokenCnt = predicateInstance.countElements(".");
  			if (tokenCnt <= 2) {
  			    vars.push_back(owner);
  			}
  			else {  // equivalent of constrainObject() in NddlRules.hh
  				// TODO: this can be done more efficiently
  				int cnt = predicateInstance.countElements(".");
  				std::string ownerName(predicateInstance.getElement(0,".").toString());
  				std::string prefix(predicateInstance.getElement(1,".").toString());
  				std::string tokenName(predicateInstance.getElement(cnt-1,".").toString());
  				std::string asString = predicateInstance.toString();
  				std::string suffix = asString.substr(
  				    ownerName.size()+prefix.size()+2,
  				    asString.size()-(prefix.size()+tokenName.size()+2)
  				);
  				debugMsg("XMLInterpreter:InterpretedRule","Subgoal slave object constraint. prefix=" << prefix << " suffix=" << suffix << " tokenName=" << tokenName);
                vars.push_back(varFromObject(owner,prefix,suffix));
  			}
  			
            vars.push_back(slave->getObject());
            addConstraint(LabelStr("eq"),vars);             
  		}
  		else 
  	        debugMsg("XMLInterpreter:InterpretedRule",predicateInstance.toString() << " NotConstrained");
  		
  		const char* relationName = relation.c_str();
  		
  		if (strcmp(relationName,"meets") == 0) {
  			meets("this",name);
  		} 
  		else if (strcmp(relationName,"met_by") == 0) {
  			met_by("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"contains") == 0) {
  			contains("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"contained_by") == 0) {
  			contained_by("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"before") == 0) {
  			before("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"after") == 0) {
  			after("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"starts") == 0) {
  			starts("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"ends") == 0) {
  			ends("this",name);
  		}   	  		   
        else if (strcmp(relationName, "paralleled_by") == 0) {
            relation(precedes, "this", Start, name, Start);
            relation(precedes, "this", End, name, End);
        }
        else if (strcmp(relationName, "parallels") == 0) {
            relation(precedes, "this", Start, name, Start);
            relation(precedes, "this", End, name, End);
        }
  		else if (strcmp(relationName,"ends_after") == 0) {
  			ends_after("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"ends_before") == 0) {
  			ends_before("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"ends_after_start") == 0) {
  			ends_after_start("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"starts_before_end") == 0) {
  			starts_before_end("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"starts_during") == 0) {
  			starts_during("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"contains_start") == 0) {
  			contains_start("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"ends_during") == 0) {
  			ends_during("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"contains_end") == 0) {
  			contains_end("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"starts_after") == 0) {
  			starts_after("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"starts_before") == 0) {
  			starts_before("this",name);
  		}   	  		   
  		else if (strcmp(relationName,"equals") == 0) {
  			equals("this",name);
  		}  
  		else {
  			check_runtime_error(strcmp(relationName,"any") == 0,std::string("Unrecognized relation:")+relationName);
  		} 	  		   
  		
  		debugMsg("XMLInterpreter:InterpretedRule","Created relation " << relationName << " " << predicateInstance.c_str());
  		return slave; 			
    }
    
    ConstrainedVariableId InterpretedRuleInstance::addLocalVariable( 
                       const AbstractDomain& baseDomain,
				       bool canBeSpecified,
				       const LabelStr& name)                   
	{
		return addVariable(baseDomain,canBeSpecified,name);
    }
    
    ConstrainedVariableId InterpretedRuleInstance::addObjectVariable( 
                       const LabelStr& type,
                       const ObjectDomain& baseDomain,
				       bool canBeSpecified,
				       const LabelStr& name)                   
	{
		ConstrainedVariableId localVariable = addVariable(baseDomain,canBeSpecified,name);
		getPlanDatabase()->makeObjectVariableFromType(type,localVariable,canBeSpecified);
		
		return localVariable;
	}    

    void InterpretedRuleInstance::executeLoop(EvalContext& evalContext,
                                              const LabelStr& loopVarName,
                                              const LabelStr& loopVarType,
                                              const LabelStr& valueSet,
                                              const std::vector<RuleExpr*>& loopBody)	
    {
      // Create a local domain based on the objects included in the valueSet
      ConstrainedVariableId setVar = evalContext.getVar(valueSet.c_str());
      check_error(!setVar.isNoId(),"Loop var can't be NULL");
      const AbstractDomain& loopVarDomain = setVar->lastDomain();
      debugMsg("XMLInterpreter:InterpretedRule","loop var domain:" << loopVarDomain.toString());
      const ObjectDomain& loopObjectSet =
          dynamic_cast<const ObjectDomain&>(loopVarDomain); 
        
      if (!loopObjectSet.isEmpty()){
          // Post a locking constraint on the set
          {
              std::vector<ConstrainedVariableId> loop_vars;
              loop_vars.push_back(setVar);
              loop_vars.push_back(ruleVariable(loopObjectSet));
              rule_constraint(Lock, loop_vars);
          }
           
          std::list<double> loopObjectSet_values;
          loopObjectSet.getValues(loopObjectSet_values);
        
          // Translate into a set ordered by key to ensure reliable ordering across runs
          ObjectSet loopObjectSet_valuesByKey;          
          for(std::list<double>::iterator it=loopObjectSet_values.begin();
              it!=loopObjectSet_values.end(); ++it) {
              ObjectId t = *it;
              loopObjectSet_valuesByKey.insert(t);
          }
        
          // execute loop body
          
          for(ObjectSet::const_iterator it=loopObjectSet_valuesByKey.begin()
              ;it!=loopObjectSet_valuesByKey.end(); ++it) {
             ObjectId loop_var = *it;
             check_error(loop_var.isValid());
          
             // Allocate a local variable for this singleton object
             // see loopVar(Allocation, a);
             {
                 ObjectDomain loopVarDomain(loopVarType.c_str());
                 loopVarDomain.insert(loop_var);
                 loopVarDomain.close();
                 // This will automatically put it in the evalContext, since all RuleInstance vars are reachable there
                 addVariable(loopVarDomain, false, loopVarName);
             }

     	     for (unsigned int i=0; i < loopBody.size(); i++) 
		         loopBody[i]->eval(evalContext);
                    
             clearLoopVar(loopVarName);
          }
       }
    }
	

    /*
     * InterpretedRuleFactory
     */     	     
    InterpretedRuleFactory::InterpretedRuleFactory(const LabelStr& predicate, 
                                                   const LabelStr& source, 
                                                   const std::vector<RuleExpr*>& body)
      : Rule(predicate,source)
      , m_body(body)
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
        InterpretedRuleInstance *foo = new InterpretedRuleInstance(m_id, token, planDb, m_body);
        foo->setRulesEngine(rulesEngine);
        return foo->getId();
    }	
    
    
    // Factories for exported C++ classes
  	TimelineObjectFactory::TimelineObjectFactory(const LabelStr& signature) 
  	    : NativeObjectFactory("Timeline",signature) 
  	{
  	} 
    
    TimelineObjectFactory::~TimelineObjectFactory() 
  	{
  	} 

   	ObjectId TimelineObjectFactory::makeNewObject( 
	                        const PlanDatabaseId& planDb, 
	                        const LabelStr& objectType, 
	                        const LabelStr& objectName, 
	                        const std::vector<const AbstractDomain*>& arguments) const 
	{ 
	    ObjectId instance =  (new Timeline(planDb, objectType, objectName,true))->getId();
	    debugMsg("XMLInterpreter:NativeObjectFactory","Created Native " << m_className.toString() << ":" << objectName.toString() << " type:" << objectType.toString()); 
	    
	    return instance; 
	}

  	ResourceObjectFactory::ResourceObjectFactory(const LabelStr& signature) 
  	        : NativeObjectFactory("Resource",signature) 
  	{
  	}
  	 
  	ResourceObjectFactory::~ResourceObjectFactory() 
  	{
  	} 
  	
    ObjectId ResourceObjectFactory::makeNewObject( 
                        const PlanDatabaseId& planDb, 
                        const LabelStr& objectType, 
                        const LabelStr& objectName, 
                        const std::vector<const AbstractDomain*>& arguments) const 
	{ 
	   	Id<NDDL::NddlResource>  instance = (new NDDL::NddlResource(planDb, objectType, objectName,true))->getId();
    	
	   	std::vector<float> argValues;
	   	for (unsigned int i=0;i<arguments.size();i++)
	   	    argValues.push_back((float)(arguments[i]->getSingletonValue()));
	   	    
	   	if (argValues.size() == 0) 
    		instance->constructor();
    	else if (argValues.size() == 3)
    		instance->constructor(argValues[0],argValues[1],argValues[2]);
    	else if (argValues.size() == 5)
    		instance->constructor(argValues[0],argValues[1],argValues[2],argValues[3],argValues[4]);
    	else if (argValues.size() == 7)
    		instance->constructor(argValues[0],argValues[1],argValues[2],argValues[3],argValues[4],argValues[5],argValues[6]);
    	else {
    		std::ostringstream os;
    		os << "Unexpected number of args in Resource constructor:" << argValues.size();
    	    check_runtime_error(ALWAYS_FAILS,os.str());
    	}	
	    		
    	instance->handleDefaults(false /*don't close the object yet*/); 	    	
	   	debugMsg("XMLInterpreter:NativeObjectFactory","Created Native " << m_className.toString() << ":" << objectName.toString() << " type:" << objectType.toString()); 

    	return instance;	
    }   
    
	TokenId ResourceChangeTokenFactory::createInstance(const PlanDatabaseId& planDb, const LabelStr& name, bool rejectable, bool isFact) const
	{
	   	debugMsg("XMLInterpreter:NativeObjectFactory","Created Native Resource.change"); 
		return new NDDL::NddlResource::change(planDb,name,rejectable,isFact,true);
	}
	
	TokenId ResourceChangeTokenFactory::createInstance(const TokenId& master, const LabelStr& name, const LabelStr& relation) const
	{
	   	debugMsg("XMLInterpreter:NativeObjectFactory","Created Native Resource.change"); 
		return (new NDDL::NddlResource::change(master,name,relation,true))->getId();
	}     
		  
  	ReusableObjectFactory::ReusableObjectFactory(const LabelStr& signature) 
  	        : NativeObjectFactory("Reusable",signature) 
  	{
  	}
  	 
  	ReusableObjectFactory::~ReusableObjectFactory() 
  	{
  	} 
  	
    ObjectId ReusableObjectFactory::makeNewObject( 
                        const PlanDatabaseId& planDb, 
                        const LabelStr& objectType, 
                        const LabelStr& objectName, 
                        const std::vector<const AbstractDomain*>& arguments) const 
	{ 
	   	Id<NDDL::NddlReusable>  instance = (new NDDL::NddlReusable(planDb, objectType, objectName,true))->getId();
    	
	   	std::vector<float> argValues;
	   	for (unsigned int i=0;i<arguments.size();i++)
	   	    argValues.push_back((float)(arguments[i]->getSingletonValue()));
	   	    
	   	if (argValues.size() == 0) 
    		instance->constructor();
    	else if (argValues.size() == 2)
    		instance->constructor(argValues[0],argValues[1]);
    	else if (argValues.size() == 3)
    		instance->constructor(argValues[0],argValues[1],argValues[2]);
    	else if (argValues.size() == 4)
    		instance->constructor(argValues[0],argValues[1],argValues[2],argValues[3]);
    	else {
    		std::ostringstream os;
    		os << "Unexpected number of args in Reusable constructor:" << argValues.size();
    	    check_runtime_error(ALWAYS_FAILS,os.str());
    	}	
	    		
    	instance->handleDefaults(false /*don't close the object yet*/); 	    	
	   	debugMsg("XMLInterpreter:NativeObjectFactory","Created Native " << m_className.toString() << ":" << objectName.toString() << " type:" << objectType.toString()); 

    	return instance;	
    }   
    
	TokenId ReusableUsesTokenFactory::createInstance(const PlanDatabaseId& planDb, const LabelStr& name, bool rejectable, bool isFact) const
	{
	   	debugMsg("XMLInterpreter:NativeObjectFactory","Created Native Reusable.uses"); 
		return new NDDL::NddlReusable::uses(planDb,name,rejectable,isFact,true);
	}
	
	TokenId ReusableUsesTokenFactory::createInstance(const TokenId& master, const LabelStr& name, const LabelStr& relation) const
	{
	   	debugMsg("XMLInterpreter:NativeObjectFactory","Created Native Reusable.uses"); 
		return (new NDDL::NddlReusable::uses(master,name,relation,true))->getId();
	}        
}

/*
 * Here is what can be used as a main to run an interpreted version of NDDL-XML through PSEngine
 * It's also possible to run NDDL (what is support it of it so far) fully interpreted
 * through the java version of PSEngine, see NDDLHelloWorld example in PlanWorks/PSUI/test  

#include "Nddl.hh" 
#include "SolverAssembly.hh" 
#include "PSEngine.hh" 
#include "Debug.hh"

using namespace EUROPA;

bool runPSEngineTest(const char* plannerConfig, const char* txSource, int startHorizon, int endHorizon, int maxSteps);
void printFlaws(int it, PSList<std::string>& flaws);

int main(int argc, const char ** argv)
{
  if (argc != 3) {
    std::cerr << "Must provide initial transactions file and planner config file ." << std::endl;
    return -1;
  }

  const char* txSource = argv[1];
  const char* plannerConfig = argv[2];
  int startHorizon = 0;
  int endHorizon   = 100;
  int maxSteps     = 1000;
  
  if (!executeWithEngineTest(plannerConfig,txSource,startHorizon,endHorizon,maxSteps)) 
      return -1;
      
  return 0;
}
 
bool executeWithPSEngine(const char* plannerConfig, const char* txSource, int startHorizon, int endHorizon, int maxSteps)
{
    try {
	  PSEngine engine;
	
	  engine.start();
	  engine.executeTxns(txSource,true,true);
	
	  PSSolver* solver = engine.createSolver(plannerConfig);
	  solver->configure(startHorizon,endHorizon);
	  int i;
      for (i = 0; 
           !solver->isExhausted() &&
           !solver->isTimedOut() &&
           i<maxSteps; i = solver->getStepCount()) {
		  solver->step();
		  PSList<std::string> flaws;
		  if (solver->isConstraintConsistent()) {
	          flaws = solver->getFlaws();
        	  printFlaws(i,flaws);
			  if (flaws.size() == 0)
			      break;
		  }
		  else
			debugMsg("Main","Iteration " << i << " Solver is not constraint consistent");
	  }
	  
	  if (solver->isExhausted()) {
	      debugMsg("Main","Solver was exhausted after " << i << " steps");	  
	  }
	  else if (solver->isTimedOut()) {
	      debugMsg("Main","Solver timed out after " << i << " steps");
	  }
	  else {    
	      debugMsg("Main","Solver finished after " << i << " steps");
	  }	      
	      
	  delete solver;	
	  engine.shutdown();

	  return true;
	}
	catch (Error& e) {
		std::cerr << "PSEngine failed:" << e.getMsg() << std::endl;
		return false;
	}	
}

void printFlaws(int it, PSList<std::string>& flaws)
{
	debugMsg("XMLInterpreter","Iteration:" << it << " " << flaws.size() << " flaws");
	
	for (int i=0; i<flaws.size(); i++) {
		debugMsg("XMLInterpreter","    " << (i+1) << " - " << flaws.get(i));
	}
}

*/
  

