#ifndef _H_PSConstraintEngine
#define _H_PSConstraintEngine

#include "Engine.hh"
#include "PSUtils.hh"

namespace EUROPA {

  enum PSVarType {INTEGER,DOUBLE,BOOLEAN,STRING,OBJECT};

  class PSVariable;
  class PSVarValue;
  class PSObject;
  
  class PSConstraintEngine : public EngineComponent
  {
    public:
      virtual ~PSConstraintEngine() {}
    	
	  virtual PSVariable* getVariableByKey(PSEntityKey id) = 0;
	  virtual PSVariable* getVariableByName(const std::string& name) = 0;
	  
  	  virtual bool getAllowViolations() const = 0;
  	  virtual void setAllowViolations(bool v) = 0;

  	  virtual double getViolation() const = 0;
  	  virtual std::string getViolationExpl() const = 0;    
  };
  
  class PSVariable : public PSEntity
  {
    public:
    	PSVariable(const EntityId& id) : PSEntity(id) {}	
    	virtual ~PSVariable() {}

    	virtual const std::string& getEntityType() const = 0;

    	virtual PSVarType getType() = 0; // Data Type 

    	virtual bool isEnumerated() = 0;
    	virtual bool isInterval() = 0;

    	virtual bool isNull() = 0;      // iif CurrentDomain is empty and the variable hasn't been specified    
    	virtual bool isSingleton() = 0;

    	virtual PSVarValue getSingletonValue() = 0;    // Call to get value if isSingleton()==true 

    	virtual PSList<PSVarValue> getValues() = 0;  // if isSingleton()==false && isEnumerated() == true

    	virtual double getLowerBound() = 0;  // if isSingleton()==false && isInterval() == true
    	virtual double getUpperBound() = 0;  // if isSingleton()==false && isInterval() == true

    	virtual void specifyValue(PSVarValue& v) = 0;
    	virtual void reset() = 0;

    	virtual double getViolation() const = 0;
    	virtual std::string getViolationExpl() const = 0;

    	virtual PSEntity* getParent() = 0;

    	virtual std::string toString() = 0;
  };

  class PSVarValue
  {
    public:
	  PSVarValue(const double val, const PSVarType type);
	  PSVarType getType() const;

	  PSEntity*           asObject() const;
	  int                 asInt() const;
	  double              asDouble() const;
	  bool                asBoolean() const;
	  const std::string&  asString() const; 

	  std::string toString() const;

	  static PSVarValue getInstance(std::string val) {return PSVarValue((double)LabelStr(val), STRING);}
	  static PSVarValue getInstance(int val) {return PSVarValue((double)val, INTEGER);}
	  static PSVarValue getInstance(double val) {return PSVarValue((double)val, DOUBLE);}
	  static PSVarValue getInstance(bool val) {return PSVarValue((double)val, BOOLEAN);}
	  static PSVarValue getObjectInstance(double obj) {return PSVarValue(obj, OBJECT);} // cast an EntityId to double to call this

    private:
	  double m_val;
	  PSVarType m_type;
  };                

}

#endif

