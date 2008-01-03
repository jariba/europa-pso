#include "NddlResource.hh"
#include "Debug.hh"
#include "IntervalDomain.hh"
#include "Constraint.hh"
#include "Variable.hh"
#include "ConstrainedVariable.hh"
#include "Constraints.hh"
#include "SAVH_Transaction.hh"
#include "SAVH_Instant.hh"
#include "SAVH_Profile.hh"

namespace NDDL {

  NddlUnaryToken::NddlUnaryToken(const PlanDatabaseId& planDatabase, const LabelStr& predicateName, const bool& rejectable, const bool& isFact, const bool& close)
    : EUROPA::SAVH::UnaryToken(planDatabase, predicateName, rejectable, isFact, IntervalIntDomain(), IntervalIntDomain(), IntervalIntDomain(1, PLUS_INFINITY), 
			       EUROPA::Token::noObject(), false, false) {
    commonInit(close);
  }

  NddlUnaryToken::NddlUnaryToken(const TokenId& master, const LabelStr& predicateName, const LabelStr& relation, const bool& close)
    : EUROPA::SAVH::UnaryToken(master, relation, predicateName, IntervalIntDomain(), IntervalIntDomain(), IntervalIntDomain(1, PLUS_INFINITY),
			       EUROPA::Token::noObject(), false, false) {
    commonInit(close);
  }

  void NddlUnaryToken::handleDefaults(const bool&) {}
  
  void NddlUnaryToken::commonInit(const bool& autoClose) {
    state = getState();
    object = getObject();
    start = getStart();
    end = getEnd();
    duration = getDuration();
    if(autoClose)
      close();
  }

  NddlUnary::NddlUnary(const PlanDatabaseId& planDatabase,
			     const LabelStr& type,
			     const LabelStr& name,
			     bool open)
    : EUROPA::SAVH::Reusable(planDatabase, type, name, open) {}

  NddlUnary::NddlUnary(const ObjectId& parent,
			     const LabelStr& type,
			     const LabelStr& name,
			     bool open)
    : EUROPA::SAVH::Reusable(parent, type, name, open) {}


  void NddlUnary::close() {
    static const unsigned int CMAX = 0;
    check_error_variable(static const unsigned int ARG_COUNT = 1);
    static const std::string PARAM_CONSUMPTION_MAX("consumptionMax");

    check_error(m_variables.size() >= ARG_COUNT);
    check_error(m_variables[CMAX]->getName().toString().find(PARAM_CONSUMPTION_MAX) != std::string::npos);
    
    check_error(m_variables[CMAX]->derivedDomain().isSingleton());
    
    init(1, 1, //capacity lb, capacity ub
         0, 1,//lower limit, upper limit
         PLUS_INFINITY, PLUS_INFINITY, //max inst production, max inst consumption
         m_variables[CMAX]->derivedDomain().getSingletonValue(), m_variables[CMAX]->derivedDomain().getSingletonValue(), //max production, max consumption
         //"ReusableFVDetector", "FlowProfile");
         "ReusableFVDetector", "IncrementalFlowProfile");
    EUROPA::SAVH::Resource::close();
  }

  void NddlUnary::handleDefaults(bool autoClose) {
    if(consumptionMax.isNoId()) {
      consumptionMax = addVariable(IntervalDomain("float"), "consumptionMax");
    }
    if(autoClose)
      close();
  }

  void NddlUnary::constructor(float c_max) {
    consumptionMax = addVariable(IntervalDomain(c_max, c_max, "float"), "consumptionMax");
  }

  void NddlUnary::constructor() {
    consumptionMax = addVariable(IntervalDomain(PLUS_INFINITY, PLUS_INFINITY, "float"), "consumptionMax");
  }

  NddlUnary::use::use(const PlanDatabaseId& planDatabase,
                      const LabelStr& predicateName,
                      bool rejectable,
                      bool isFact,
                      bool close)
    : EUROPA::SAVH::ReusableToken(planDatabase, predicateName, IntervalIntDomain(), IntervalIntDomain(), IntervalIntDomain(1, PLUS_INFINITY),
                                  IntervalDomain(1.0), Token::noObject(), false) {
    handleDefaults(close);
  }

  NddlUnary::use::use(const TokenId& master,
				const LabelStr& predicateName,
				const LabelStr& relation,
				bool close)
    : EUROPA::SAVH::ReusableToken(master, relation, predicateName, IntervalIntDomain(), IntervalIntDomain(), IntervalIntDomain(1, PLUS_INFINITY),
                                  IntervalDomain(1.0), Token::noObject(), false) {
    handleDefaults(close);
  }

  void NddlUnary::use::close() {
    EUROPA::SAVH::ReusableToken::close();
  }

  void NddlUnary::use::handleDefaults(bool autoClose) {
    state = getState();
    object = getObject();
    start = getStart();
    end = getEnd();
    duration = getDuration();

    if(autoClose)
      close();
  }

  /*===============*/

  NddlReusable::NddlReusable(const PlanDatabaseId& planDatabase,
			     const LabelStr& type,
			     const LabelStr& name,
			     bool open)
    : EUROPA::SAVH::Reusable(planDatabase, type, name, open) {}

  NddlReusable::NddlReusable(const ObjectId& parent,
			     const LabelStr& type,
			     const LabelStr& name,
			     bool open)
    : EUROPA::SAVH::Reusable(parent, type, name, open) {}

  
  bool isValidProfile(ConstrainedVariableId profileNameVar)
  {
      if (!profileNameVar->derivedDomain().isSingleton())
          return false;
      
      std::string profileName = LabelStr(profileNameVar->derivedDomain().getSingletonValue()).toString();
      
      if (profileName == "FlowProfile" ||
          profileName == "IncrementalFlowProfile" ||
          profileName == "TimetableProfile")
            return true;
      
      return false;      
  }

  void NddlReusable::close() {
    static const unsigned int C = 0;
    static const unsigned int LLMIN = 1;
    static const unsigned int CRMAX = 2;
    static const unsigned int CMAX = 3;
    check_error_variable(static const unsigned int ARG_COUNT = 4);
    static const std::string PARAM_CAPACITY("capacity");
    static const std::string PARAM_LEVEL_LIMIT_MIN("levelLimitMin");
    static const std::string PARAM_CONSUMPTION_RATE_MAX("consumptionRateMax");
    static const std::string PARAM_CONSUMPTION_MAX("consumptionMax");

    check_error(m_variables.size() >= ARG_COUNT);
    check_error(m_variables[C]->getName().toString().find(PARAM_CAPACITY) != std::string::npos);
    check_error(m_variables[LLMIN]->getName().toString().find(PARAM_LEVEL_LIMIT_MIN) != std::string::npos);
    check_error(m_variables[CRMAX]->getName().toString().find(PARAM_CONSUMPTION_RATE_MAX) != std::string::npos);
    check_error(m_variables[CMAX]->getName().toString().find(PARAM_CONSUMPTION_MAX) != std::string::npos);
    
    check_error(m_variables[C]->derivedDomain().isSingleton());
    check_error(m_variables[LLMIN]->derivedDomain().isSingleton());
    check_error(m_variables[CRMAX]->derivedDomain().isSingleton());
    check_error(m_variables[CMAX]->derivedDomain().isSingleton());
    
    static const std::string PARAM_PROFILE_TYPE("profileType"); 
    std::string fullName = getName().toString()+"."+PARAM_PROFILE_TYPE;
    ConstrainedVariableId profileNameVar = getVariable(fullName);
    LabelStr profileName("IncrementalFlowProfile");
    if (!profileNameVar.isNoId()) {
        debugMsg("NddlReusable","Using Profile : " << profileNameVar->toString());
        check_error(isValidProfile(profileNameVar),"Invalid resource profile type:"+profileNameVar->toString());
        profileName = LabelStr(profileNameVar->derivedDomain().getSingletonValue());
    }
    debugMsg("NddlReusable","Using Profile : " << profileName.toString())
            
    init(m_variables[C]->derivedDomain().getSingletonValue(), m_variables[C]->derivedDomain().getSingletonValue(), 
	 m_variables[LLMIN]->derivedDomain().getSingletonValue(), m_variables[C]->derivedDomain().getSingletonValue(),
	 m_variables[CRMAX]->derivedDomain().getSingletonValue(), m_variables[CRMAX]->derivedDomain().getSingletonValue(),
	 m_variables[CMAX]->derivedDomain().getSingletonValue(), m_variables[CMAX]->derivedDomain().getSingletonValue(),
	 "ReusableFVDetector", 
	 profileName);
    
    EUROPA::SAVH::Resource::close();
  }

  void NddlReusable::handleDefaults(bool autoClose) {
    if(capacity.isNoId()) {
      capacity = addVariable(IntervalDomain("float"), "capacity");
    }
    if(levelLimitMin.isNoId()) {
      levelLimitMin = addVariable(IntervalDomain("float"), "levelLimitMin");
    }
    if(consumptionRateMax.isNoId()) {
      consumptionRateMax = addVariable(IntervalDomain("float"), "consumptionRateMax");
    }
    if(consumptionMax.isNoId()) {
      consumptionMax = addVariable(IntervalDomain("float"), "consumptionMax");
    }
    if(autoClose)
      close();
  }

  void NddlReusable::constructor(float c, float ll_min) {
    capacity = addVariable(IntervalDomain(c, c, "float"), "capacity");
    levelLimitMin = addVariable(IntervalDomain(ll_min, ll_min, "float"), "levelLimitMin");
    consumptionRateMax = addVariable(IntervalDomain(PLUS_INFINITY, PLUS_INFINITY, "float"), "consumptionRateMax");
    consumptionMax = addVariable(IntervalDomain(PLUS_INFINITY, PLUS_INFINITY, "float"), "consumptionMax");
  }
  void NddlReusable::constructor(float c, float ll_min, float cr_max) {
    capacity = addVariable(IntervalDomain(c, c, "float"), "capacity");
    levelLimitMin = addVariable(IntervalDomain(ll_min, ll_min, "float"), "levelLimitMin");
    consumptionRateMax = addVariable(IntervalDomain(cr_max, cr_max, "float"), "consumptionRateMax");
    consumptionMax = addVariable(IntervalDomain(PLUS_INFINITY, PLUS_INFINITY, "float"), "consumptionMax");
  }
  void NddlReusable::constructor(float c, float ll_min, float c_max, float cr_max) {
    capacity = addVariable(IntervalDomain(c, c, "float"), "capacity");
    levelLimitMin = addVariable(IntervalDomain(ll_min, ll_min, "float"), "levelLimitMin");
    consumptionRateMax = addVariable(IntervalDomain(cr_max, cr_max, "float"), "consumptionRateMax");
    consumptionMax = addVariable(IntervalDomain(c_max, c_max, "float"), "consumptionMax");
  }

  void NddlReusable::constructor() {
    capacity = addVariable(IntervalDomain(PLUS_INFINITY, PLUS_INFINITY, "float"), "capacity");
    levelLimitMin = addVariable(IntervalDomain(MINUS_INFINITY, MINUS_INFINITY, "float"), "levelLimitMin");
    consumptionRateMax = addVariable(IntervalDomain(PLUS_INFINITY, PLUS_INFINITY, "float"), "consumptionRateMax");
    consumptionMax = addVariable(IntervalDomain(PLUS_INFINITY, PLUS_INFINITY, "float"), "consumptionMax");
  }

  NddlReusable::uses::uses(const PlanDatabaseId& planDatabase,
				const LabelStr& predicateName,
				bool rejectable,
				bool isFact,
				bool close)
    : EUROPA::SAVH::ReusableToken(planDatabase, predicateName, IntervalIntDomain(), IntervalIntDomain(), IntervalIntDomain(1, PLUS_INFINITY),
				  Token::noObject(), false) {
    handleDefaults(close);
  }

  NddlReusable::uses::uses(const TokenId& master,
				const LabelStr& predicateName,
				const LabelStr& relation,
				bool close)
    : EUROPA::SAVH::ReusableToken(master, relation, predicateName, IntervalIntDomain(), IntervalIntDomain(), IntervalIntDomain(1, PLUS_INFINITY),
				  Token::noObject(), false) {
    handleDefaults(close);
  }

  void NddlReusable::uses::close() {
    EUROPA::SAVH::ReusableToken::close();
  }

  void NddlReusable::uses::handleDefaults(bool autoClose) {
    state = getState();
    object = getObject();
    start = getStart();
    end = getEnd();
    duration = getDuration();
    quantity = getQuantity();

    if(autoClose)
      close();
  }

  NddlReservoir::NddlReservoir(const PlanDatabaseId& planDatabase,
			       const LabelStr& type,
			       const LabelStr& name,
			       bool open) 
    : EUROPA::SAVH::Reservoir(planDatabase, type, name, open) {}

  NddlReservoir::NddlReservoir(const ObjectId parent,
			       const LabelStr& type,
			       const LabelStr& name,
			       bool open) 
    : EUROPA::SAVH::Reservoir(parent, type, name, open) {}

  void NddlReservoir::close() {
    static const int IC = 0;
    static const int LLMIN = 1;
    static const int LLMAX = 2;
    static const int PRMAX = 3;
    static const int PMAX = 4;
    static const int CRMAX = 5;
    static const int CMAX = 6;
    check_error_variable(static const unsigned int ARG_COUNT = 7);
    static const std::string PARAM_INITIAL_CAPACITY("initialCapacity");
    static const std::string PARAM_LEVEL_LIMIT_MIN("levelLimitMin");
    static const std::string PARAM_LEVEL_LIMIT_MAX("levelLimitMax");
    static const std::string PARAM_PRODUCTION_RATE_MAX("productionRateMax");
    static const std::string PARAM_PRODUCTION_MAX("productionMax");
    static const std::string PARAM_CONSUMPTION_RATE_MAX("consumptionRateMax");
    static const std::string PARAM_CONSUMPTION_MAX("consumptionMax");

    // Ensure the binding of variable names is as expected

    check_error(m_variables.size() >= ARG_COUNT);
    check_error(m_variables[IC]->getName().toString().find(PARAM_INITIAL_CAPACITY)  != std::string::npos);
    check_error(m_variables[LLMIN]->getName().toString().find(PARAM_LEVEL_LIMIT_MIN)  != std::string::npos);
    check_error(m_variables[LLMAX]->getName().toString().find(PARAM_LEVEL_LIMIT_MAX)  != std::string::npos);
    check_error(m_variables[PRMAX]->getName().toString().find(PARAM_PRODUCTION_RATE_MAX)  != std::string::npos);
    check_error(m_variables[PMAX]->getName().toString().find(PARAM_PRODUCTION_MAX)  != std::string::npos);
    check_error(m_variables[CRMAX]->getName().toString().find(PARAM_CONSUMPTION_RATE_MAX)  != std::string::npos);
    check_error(m_variables[CMAX]->getName().toString().find(PARAM_CONSUMPTION_MAX)  != std::string::npos);
    

    // Ensure all values have been set to singletons already
    check_error(m_variables[IC]->derivedDomain().isSingleton());
    check_error(m_variables[LLMIN]->derivedDomain().isSingleton());
    check_error(m_variables[LLMAX]->derivedDomain().isSingleton());
    check_error(m_variables[PRMAX]->derivedDomain().isSingleton());
    check_error(m_variables[PMAX]->derivedDomain().isSingleton());
    check_error(m_variables[CRMAX]->derivedDomain().isSingleton());
    check_error(m_variables[CMAX]->derivedDomain().isSingleton());

    init(m_variables[IC]->derivedDomain().getSingletonValue(), m_variables[IC]->derivedDomain().getSingletonValue(),
	 m_variables[LLMIN]->derivedDomain().getSingletonValue(), m_variables[LLMAX]->derivedDomain().getSingletonValue(),
	 m_variables[PRMAX]->derivedDomain().getSingletonValue(), (m_variables[CRMAX]->derivedDomain().getSingletonValue()),
	 m_variables[PMAX]->derivedDomain().getSingletonValue(), (m_variables[CMAX]->derivedDomain().getSingletonValue()),
	 "TimetableFVDetector", "IncrementalFlowProfile");
    EUROPA::SAVH::Resource::close();
  }

  void NddlReservoir::handleDefaults(bool autoClose) {
    if(initialCapacity.isNoId()){
      initialCapacity = addVariable(IntervalDomain("float"), "initialCapacity");
    }
    if(levelLimitMin.isNoId()){
      levelLimitMin = addVariable(IntervalDomain("float"), "levelLimitMin");
    }
    if(levelLimitMax.isNoId()){
      levelLimitMax = addVariable(IntervalDomain("float"), "levelLimitMax");
    }
    if(productionRateMax.isNoId()){
      productionRateMax = addVariable(IntervalDomain("float"), "productionRateMax");
    }
    if(productionMax.isNoId()){
      productionMax = addVariable(IntervalDomain("float"), "productionMax");
    }
    if(consumptionRateMax.isNoId()){
      consumptionRateMax = addVariable(IntervalDomain("float"), "consumptionRateMax");
    }
    if(consumptionMax.isNoId()){
      consumptionMax = addVariable(IntervalDomain("float"), "consumptionMax");
    }
    if (autoClose)
      close();
  }

  void NddlReservoir::constructor(float ic, float ll_min, float ll_max) {
    initialCapacity = addVariable(IntervalDomain(ic, ic, "float"), "initialCapacity");
    levelLimitMin = addVariable(IntervalDomain(ll_min, ll_min, "float"), "levelLimitMin");
    levelLimitMax = addVariable(IntervalDomain(ll_max, ll_max, "float"), "levelLimitMax");
    productionRateMax = addVariable(IntervalDomain(+inf, +inf, "float"), "productionRateMax");
    productionMax = addVariable(IntervalDomain(+inf, +inf, "float"), "productionMax");
    consumptionRateMax = addVariable(IntervalDomain(+inf, +inf, "float"), "consumptionRateMax");
    consumptionMax = addVariable(IntervalDomain(+inf, +inf, "float"), "consumptionMax");
  }

  void NddlReservoir::constructor(float ic, float ll_min, float ll_max, float p_max, float c_max) {
    initialCapacity = addVariable(IntervalDomain(ic, ic, "float"), "initialCapacity");
    levelLimitMin = addVariable(IntervalDomain(ll_min, ll_min, "float"), "levelLimitMin");
    levelLimitMax = addVariable(IntervalDomain(ll_max, ll_max, "float"), "levelLimitMax");
    productionRateMax = addVariable(IntervalDomain(p_max, p_max, "float"), "productionRateMax");
    productionMax = addVariable(IntervalDomain(p_max, p_max, "float"), "productionMax");
    consumptionRateMax = addVariable(IntervalDomain(+inf, +inf, "float"), "consumptionRateMax");
    consumptionMax = addVariable(IntervalDomain(c_max, c_max, "float"), "consumptionMax");
  }

  void NddlReservoir::constructor(float ic, float ll_min, float ll_max, float pr_max, float p_max, float cr_max, float c_max) {
    initialCapacity = addVariable(IntervalDomain(ic, ic, "float"), "initialCapacity");
    levelLimitMin = addVariable(IntervalDomain(ll_min, ll_min, "float"), "levelLimitMin");
    levelLimitMax = addVariable(IntervalDomain(ll_max, ll_max, "float"), "levelLimitMax");
    productionRateMax = addVariable(IntervalDomain(pr_max, pr_max, "float"), "productionRateMax");
    productionMax = addVariable(IntervalDomain(p_max, p_max, "float"), "productionMax");
    consumptionRateMax = addVariable(IntervalDomain(cr_max, cr_max, "float"), "consumptionRateMax");
    consumptionMax = addVariable(IntervalDomain(c_max, c_max, "float"), "consumptionMax");
  }


  void NddlReservoir::constructor() {
    initialCapacity = addVariable(IntervalDomain(0, 0, "float"), "initialCapacity");
    levelLimitMin = addVariable(IntervalDomain(-inf, -inf, "float"), "levelLimitMin");
    levelLimitMax = addVariable(IntervalDomain(+inf, +inf, "float"), "levelLimitMax");
    productionRateMax = addVariable(IntervalDomain(+inf, +inf, "float"), "productionRateMax");
    productionMax = addVariable(IntervalDomain(+inf, +inf, "float"), "productionMax");
    consumptionRateMax = addVariable(IntervalDomain(+inf, +inf, "float"), "consumptionRateMax");
    consumptionMax = addVariable(IntervalDomain(+inf, +inf, "float"), "consumptionMax");
  }


  NddlReservoir::produce::produce(const PlanDatabaseId& planDatabase,
				    const LabelStr& predicateName,
				    bool rejectable,
				    bool isFact,
				    bool close)
    : EUROPA::SAVH::ProducerToken(planDatabase, predicateName, rejectable, isFact, IntervalIntDomain(), Token::noObject(), false) {
    handleDefaults(close);
  }

  NddlReservoir::produce::produce(const TokenId& master,
			       const LabelStr& predicateName,
			       const LabelStr& relation,
			       bool close)
    : EUROPA::SAVH::ProducerToken(master, relation, predicateName, IntervalIntDomain(), Token::noObject(), false) {
    handleDefaults(close);
  }

  void NddlReservoir::produce::close() {
    EUROPA::SAVH::ProducerToken::close();
  }

  void NddlReservoir::produce::handleDefaults(bool autoClose) {
    state = getState();
    object = getObject();
    start = getStart();
    end = getEnd();
    duration = getDuration();
    time = getTime();
    quantity = getQuantity();

    if (autoClose)
      close();
  }

  NddlReservoir::consume::consume(const PlanDatabaseId& planDatabase,
				    const LabelStr& predicateName,
				    bool rejectable,
				    bool isFact,
				    bool close)
    : EUROPA::SAVH::ConsumerToken(planDatabase, predicateName, rejectable, isFact, IntervalIntDomain(), Token::noObject(), false) {
    handleDefaults(close);
  }

  NddlReservoir::consume::consume(const TokenId& master,
			       const LabelStr& predicateName,
			       const LabelStr& relation,
			       bool close)
    : EUROPA::SAVH::ConsumerToken(master, relation, predicateName, IntervalIntDomain(), Token::noObject(), false) {
    handleDefaults(close);
  }

  void NddlReservoir::consume::close() {
    EUROPA::SAVH::ConsumerToken::close();
  }

  void NddlReservoir::consume::handleDefaults(bool autoClose) {
    state = getState();
    object = getObject();
    start = getStart();
    end = getEnd();
    duration = getDuration();
    time = getTime();
    quantity = getQuantity();

    if (autoClose)
      close();
  }

  /******************************************************************************
   *  Implementation for NddlReservoir 
   ******************************************************************************/
  NddlResource::NddlResource(const PlanDatabaseId& planDatabase,
			     const LabelStr& type,
			     const LabelStr& name,
			     bool open)
    : EUROPA::Resource(planDatabase, type, name, open){}

  NddlResource::NddlResource(const ObjectId parent,
			     const LabelStr& type,
			     const LabelStr& name,
			     bool open)
    : EUROPA::Resource(parent, type, name, open){}

  void NddlResource::close() {
    /* Constants that indicate NDDL model bindings for Resource specified properties. */
    static const int IC = 0;
    static const int LLMIN = 1;
    static const int LLMAX = 2;
    static const int PRMAX = 3;
    static const int PMAX = 4;
    static const int CRMAX = 5;
    static const int CMAX = 6;
    check_error_variable(static const unsigned int ARG_COUNT = 7);
    static const std::string PARAM_INITIAL_CAPACITY("initialCapacity");
    static const std::string PARAM_LEVEL_LIMIT_MIN("levelLimitMin");
    static const std::string PARAM_LEVEL_LIMIT_MAX("levelLimitMax");
    static const std::string PARAM_PRODUCTION_RATE_MAX("productionRateMax");
    static const std::string PARAM_PRODUCTION_MAX("productionMax");
    static const std::string PARAM_CONSUMPTION_RATE_MAX("consumptionRateMax");
    static const std::string PARAM_CONSUMPTION_MAX("consumptionMax");

    // Ensure the binding of variable names is as expected

    check_error(m_variables.size() >= ARG_COUNT);
    check_error(m_variables[IC]->getName().toString().find(PARAM_INITIAL_CAPACITY)  != std::string::npos);
    check_error(m_variables[LLMIN]->getName().toString().find(PARAM_LEVEL_LIMIT_MIN)  != std::string::npos);
    check_error(m_variables[LLMAX]->getName().toString().find(PARAM_LEVEL_LIMIT_MAX)  != std::string::npos);
    check_error(m_variables[PRMAX]->getName().toString().find(PARAM_PRODUCTION_RATE_MAX)  != std::string::npos);
    check_error(m_variables[PMAX]->getName().toString().find(PARAM_PRODUCTION_MAX)  != std::string::npos);
    check_error(m_variables[CRMAX]->getName().toString().find(PARAM_CONSUMPTION_RATE_MAX)  != std::string::npos);
    check_error(m_variables[CMAX]->getName().toString().find(PARAM_CONSUMPTION_MAX)  != std::string::npos);
    

    // Ensure all values have been set to singletons already
    check_error(m_variables[IC]->derivedDomain().isSingleton());
    check_error(m_variables[LLMIN]->derivedDomain().isSingleton());
    check_error(m_variables[LLMAX]->derivedDomain().isSingleton());
    check_error(m_variables[PRMAX]->derivedDomain().isSingleton());
    check_error(m_variables[PMAX]->derivedDomain().isSingleton());
    check_error(m_variables[CRMAX]->derivedDomain().isSingleton());
    check_error(m_variables[CMAX]->derivedDomain().isSingleton());

    //move to internal vars.
    init(m_variables[IC]->derivedDomain().getSingletonValue(),
	 m_variables[LLMIN]->derivedDomain().getSingletonValue(),
	 m_variables[LLMAX]->derivedDomain().getSingletonValue(),
	 m_variables[PRMAX]->derivedDomain().getSingletonValue(),
	 m_variables[PMAX]->derivedDomain().getSingletonValue(),
	 m_variables[CRMAX]->derivedDomain().getSingletonValue(),
	 m_variables[CMAX]->derivedDomain().getSingletonValue());

    // Close the parent
    Resource::close();
  }

  void NddlResource::handleDefaults(bool autoClose) {
    if(initialCapacity.isNoId()){
      initialCapacity = addVariable(IntervalDomain("float"), "initialCapacity");
    }
    if(levelLimitMin.isNoId()){
      levelLimitMin = addVariable(IntervalDomain("float"), "levelLimitMin");
    }
    if(levelLimitMax.isNoId()){
      levelLimitMax = addVariable(IntervalDomain("float"), "levelLimitMax");
    }
    if(productionRateMax.isNoId()){
      productionRateMax = addVariable(IntervalDomain("float"), "productionRateMax");
    }
    if(productionMax.isNoId()){
      productionMax = addVariable(IntervalDomain("float"), "productionMax");
    }
    if(consumptionRateMax.isNoId()){
      consumptionRateMax = addVariable(IntervalDomain("float"), "consumptionRateMax");
    }
    if(consumptionMax.isNoId()){
      consumptionMax = addVariable(IntervalDomain("float"), "consumptionMax");
    }
    if (autoClose)
      close();
  }
  
  void NddlResource::constructor(float ic, float ll_min, float ll_max) {
    initialCapacity = addVariable(IntervalDomain(ic, ic, "float"), "initialCapacity");
    levelLimitMin = addVariable(IntervalDomain(ll_min, ll_min, "float"), "levelLimitMin");
    levelLimitMax = addVariable(IntervalDomain(ll_max, ll_max, "float"), "levelLimitMax");
    productionRateMax = addVariable(IntervalDomain(+inf, +inf, "float"), "productionRateMax");
    productionMax = addVariable(IntervalDomain(+inf, +inf, "float"), "productionMax");
    consumptionRateMax = addVariable(IntervalDomain(-inf, -inf, "float"), "consumptionRateMax");
    consumptionMax = addVariable(IntervalDomain(-inf, -inf, "float"), "consumptionMax");
  }
  
  // Plasma.nddl:20 Resource
  void NddlResource::constructor(float ic, float ll_min, float ll_max, float p_max, float c_max) {
    initialCapacity = addVariable(IntervalDomain(ic, ic, "float"), "initialCapacity");
    levelLimitMin = addVariable(IntervalDomain(ll_min, ll_min, "float"), "levelLimitMin");
    levelLimitMax = addVariable(IntervalDomain(ll_max, ll_max, "float"), "levelLimitMax");
    productionRateMax = addVariable(IntervalDomain(p_max, p_max, "float"), "productionRateMax");
    productionMax = addVariable(IntervalDomain(p_max, p_max, "float"), "productionMax");
    consumptionRateMax = addVariable(IntervalDomain(-16, -16, "float"), "consumptionRateMax");
    consumptionMax = addVariable(IntervalDomain(c_max, c_max, "float"), "consumptionMax");
  }
  
  // Plasma.nddl:20 Resource
  void NddlResource::constructor(float ic, float ll_min, float ll_max, float pr_max, float p_max, float cr_max, float c_max) {
    initialCapacity = addVariable(IntervalDomain(ic, ic, "float"), "initialCapacity");
    levelLimitMin = addVariable(IntervalDomain(ll_min, ll_min, "float"), "levelLimitMin");
    levelLimitMax = addVariable(IntervalDomain(ll_max, ll_max, "float"), "levelLimitMax");
    productionRateMax = addVariable(IntervalDomain(pr_max, pr_max, "float"), "productionRateMax");
    productionMax = addVariable(IntervalDomain(p_max, p_max, "float"), "productionMax");
    consumptionRateMax = addVariable(IntervalDomain(cr_max, cr_max, "float"), "consumptionRateMax");
    consumptionMax = addVariable(IntervalDomain(c_max, c_max, "float"), "consumptionMax");
  }
  
  // Plasma.nddl:20 Resource
  void NddlResource::constructor() {
    initialCapacity = addVariable(IntervalDomain(0, 0, "float"), "initialCapacity");
    levelLimitMin = addVariable(IntervalDomain(-inf, -inf, "float"), "levelLimitMin");
    levelLimitMax = addVariable(IntervalDomain(+inf, +inf, "float"), "levelLimitMax");
    productionRateMax = addVariable(IntervalDomain(+inf, +inf, "float"), "productionRateMax");
    productionMax = addVariable(IntervalDomain(+inf, +inf, "float"), "productionMax");
    consumptionRateMax = addVariable(IntervalDomain(-inf, -inf, "float"), "consumptionRateMax");
    consumptionMax = addVariable(IntervalDomain(-inf, -inf, "float"), "consumptionMax");
  }

  /*
   * Implementation for NddlResource::change
   */

  NddlResource::change::change(const PlanDatabaseId& planDatabase,
			       const LabelStr& predicateName,
			       bool rejectable,
			       bool isFact,
			       bool close)
    : Transaction(planDatabase, predicateName, rejectable, isFact, IntervalIntDomain(), Token::noObject(), false) {
    handleDefaults(close);
  }

  NddlResource::change::change(const TokenId& master,
			       const LabelStr& predicateName,
			       const LabelStr& relation,
			       bool close)
    : Transaction(master, relation, predicateName, IntervalIntDomain(), Token::noObject(), false) {
    handleDefaults(close);
  }

  void NddlResource::change::close() {
    EUROPA::Transaction::close();
  }

  void NddlResource::change::handleDefaults(bool autoClose) {
    state = getState();
    object = getObject();
    start = getStart();
    end = getEnd();
    duration = getDuration();
    time = getTime();
    quantity = m_usage;

    if (autoClose)
      close();
  }

} // namespace NDDL
