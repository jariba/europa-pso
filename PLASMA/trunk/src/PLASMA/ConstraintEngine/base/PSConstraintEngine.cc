 
#include "PSConstraintEngine.hh"
#include "ConstraintEngine.hh"
#include "ConstrainedVariable.hh"
#include "Debug.hh"


namespace EUROPA {

  const std::string& PSVariable::getEntityType() const 
  {
	static const std::string VARIABLE_STR("VARIABLE");
  	return VARIABLE_STR;
  }

  PSVarType PSVariable::getType() {
	  return m_type;
  }

  std::string PSVariable::toString() {
    std::ostringstream os;
    
    if (isNull())
        os << "NULL";
    else if (isSingleton()) 
    	os << getSingletonValue().toString();    	    
    else if (isInterval()) 
        os << "[" << getLowerBound() << "," << getUpperBound() << "]";
    else if (isEnumerated()) {
    	os << "{";
    	PSList<PSVarValue> values = getValues();
    	for (int i=0;i<values.size();i++) {
    		if (i > 0)
    		    os << ", ";
    		os << values.get(i).toString();    
    	}
    	os << "}";
    }
    else 
        os << "ERROR!";    
    
    return os.str();
  }

  std::string PSVarValue::toString() const {
  	std::ostringstream os;
  	
  	switch (m_type) {
  		case INTEGER:
            os << asInt();
  		    break;
  		case DOUBLE:
            os << asDouble();
  		    break;
  		case BOOLEAN:
            os << asBoolean();
  		    break;
  		case STRING:
            os << asString();
  		    break;
  		case OBJECT:
  		    {
  		        PSEntity* obj = asObject();
  		        debugMsg("TBS:", "About to print out object...");
  		        os << "OBJECT:" << obj->getEntityName() << "(" << obj->getKey() << ")";
  		        debugMsg("TBS:", "Done printing object...");
  		    }
  		    break;
  		
  		default:
  		    check_error(ALWAYS_FAILS, "Unknown type");    
  	}
  	  	
  	return os.str();
  }      

  //  PSVarValue pieces:
  PSVarValue::PSVarValue(const double val, const PSVarType type) : m_val(val), m_type(type) {}
  PSVarType PSVarValue::getType() const {return m_type;}

  int PSVarValue::asInt() const {check_runtime_error(m_type == INTEGER); return (int) m_val;}
  
  double PSVarValue::asDouble() const {return m_val;}

  bool PSVarValue::asBoolean() const {check_runtime_error(m_type == BOOLEAN); return (bool) m_val;}

  const std::string& PSVarValue::asString() const {
    check_runtime_error(m_type == STRING);
    debugMsg("TBS:", "asString called with " << m_val);
    return LabelStr(m_val).toString();
  }

  // TODO:  DON'T NEED THIS ANYMORE?
  PSEntity* PSVarValue::asObject() const 
  {
    check_runtime_error(m_type == OBJECT);
    /* TODO: provide hooks to return PSObject or other objects */
    //return new PSEntity(EntityId(m_val));
    PSEntityId id = PSEntityId(m_val);
    return (PSEntity *) id;
  }


}
