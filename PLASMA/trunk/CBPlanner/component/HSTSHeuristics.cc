#include "HSTSHeuristics.hh"
#include <sstream>

namespace Prototype {

  // todo: check bounds on priority, if inserting, make sure it wasn't
  // there

  HSTSHeuristics::TokenType::TokenType(const LabelStr& predicateName, 
				       const std::vector<std::pair<LabelStr, LabelStr> >& domainSpecs) 
    : m_predicateName(predicateName), m_domainSpecs(domainSpecs) { }

  HSTSHeuristics::TokenType::~TokenType() { check_error(m_id.isValid()); m_id.release(); }

  const LabelStr& HSTSHeuristics::TokenType::getPredicate() const {
    return m_predicateName;
  }

  const std::vector<std::pair<LabelStr,LabelStr> >& HSTSHeuristics::TokenType::getDomainSpecs() const {
    return m_domainSpecs;
  }

  const LabelStr HSTSHeuristics::TokenType::getIndexKey(const TokenTypeId& tt) {
    std::stringstream key;
    check_error(LabelStr::isString(tt->getPredicate()));
    key << tt->getPredicate().getKey(); 
    std::vector<std::pair<LabelStr,LabelStr> > ds = tt->getDomainSpecs();
    for (unsigned int i=0; i < ds.size(); ++i) {
      key << DELIMITER;
      key << ds[i].first << "|" << ds[i].second;
    }
    key << std::endl;
    return key.str();
  }

  void HSTSHeuristics::TokenType::split(const std::string& str, const char& delim, std::vector<std::string>& strings) {
    // Skip delimiters at beginning.
    std::string::size_type lastPos = str.find_first_not_of(delim, 0);
    // Find first "non-delimiter".
    std::string::size_type pos = str.find_first_of(delim, lastPos);

    while (std::string::npos != pos || std::string::npos != lastPos) {
      // Found a token, add it to the vector.
      strings.push_back(str.substr(lastPos, pos - lastPos));
      // Skip delim
      lastPos = str.find_first_not_of(delim, pos);
      // Find next "non-delimiter"
      pos = str.find_first_of(delim, lastPos);
    }
  }

  // todo: get base domain?  Alternatively, could cache it once it is
  // computed for the first time on a variable instance of the same type
  // todo: if domain is too large, create generator.
  const TokenTypeId HSTSHeuristics::TokenType::getTokenType(const LabelStr& indexKey) {
    std::vector<std::string> strings;
    split(indexKey.toString(),DELIMITER,strings);
    check_error(strings.size() >= 1);
    std::vector<std::pair<LabelStr,LabelStr> > domainSpec;
    for (unsigned int i = 1; i < strings.size();  i++) {
      std::vector<std::string> domstr;
      split(strings[i],'|',domstr);
      check_error(domstr.size() == 2);
      domainSpec.push_back(std::make_pair<LabelStr,LabelStr>(domstr[0],domstr[1]));
    }
    return (new TokenType(LabelStr(strings[0]),domainSpec))->getId();
  }

  bool HSTSHeuristics::TokenType::matches(const TokenTypeId& tt) {
    LabelStr myIndexKey HSTSHeuristics::TokenType::getIndexKey(*this);
    LabelStr indexKey HSTSHeuristics::TokenType::getIndexKey(tt);
    return (myIndexKey.getKey() == indexKey.getKey());
  }

  bool HSTSHeuristics::TokenType::conflicts(const TokenTypeId& tt) {
    return !matches(tt);
  }

  HSTSHeuristics::TokenEntry::TokenEntry() {}

  // todo: create generators from names
  HSTSHeuristics::TokenEntry::TokenEntry(const Priority p, 
					 const std::vector<TokenDecisionPointState>& states, 
					 const std::vector<CandidateOrder>& orders, 
					 const std::vector<LabelStr>& generatorNames) 
    : m_priority(p), m_states(states), m_orders(orders) { 
    // create generators from names
  }

  HSTSHeuristics::TokenEntry::~TokenEntry() {}

  void HSTSHeuristics::TokenEntry::setPriority(const Priority p) { 
    check_error(MIN_PRIORITY <= p);
    check_error(MAX_PRIORITY >= p);
    m_priority = p; 
  }

  const Priority HSTSHeuristics::TokenEntry::getPriority() { return m_priority; }

  const std::vector<TokenDecisionPoint::State>& HSTSHeuristics::TokenEntry::getStates() {
    return m_states;
  }

  const std::vector<CandidateOrder>& HSTSHeuristics::TokenEntry::getOrders() {
    return m_orders;
  }
  
  const std::vector<GeneratorId>& HSTSHeuristics::TokenEntry::getGenerators() {
    return m_generators;
  }

  HSTSHeuristics::VariableEntry::VariableEntry() {}

  // construct the generator according to the generator name
  HSTSHeuristics::VariableEntry::VariableEntry(const std::list<double>& domain,
					       const Priority p, 
					       const DomainOrder order,
					       const LabelStr& generatorName) 
    : m_domain(domain), m_priority(p) {
    check_error(MIN_PRIORITY <= p);
    check_error(MAX_PRIORITY >= p);
    switch (order) {
    case VGENERATOR:
      // create generator from name
      break;
    case ASCENDING:
      break;
    case DESCENDING:
      m_domain.reverse();
      break;
    default:
      check_error(ALWAYS_FAILS);
    }
  }

  HSTSHeuristics::VariableEntry::VariableEntry(const std::list<double>& domain,
					       const Priority p, 
					       const DomainOrder order,
					       const std::list<double>& enumeration)
    : m_domain(domain), m_priority(p) {
    check_error(MIN_PRIORITY <= p);
    check_error(MAX_PRIORITY >= p);
    switch (order) {
    case VGENERATOR:
      // create generator from name
      break;
    case ASCENDING:
      break;
    case DESCENDING:
      m_domain.reverse();
      break;
    default:
      check_error(ALWAYS_FAILS);
    }
  }

  HSTSHeuristics::VariableEntry::~VariableEntry() {}

  Priority HSTSHeuristics::VariableEntry::getPriority() { return m_priority; }
  
  const std::list<double>& HSTSHeuristics::VariableEntry::getDomain() { return m_domain; }
  
  const Generator& HSTSHeuristics::VariableEntry::getGenerator() { return m_generator; }

  HSTSHeuristics::HSTSHeuristics() {
    m_defaultPriorityPreference = PriorityPref::LOW;
    m_defaultTokenPriority = 0.0;
    m_defaultVariablePriority = 0.0;
    m_defaultDomainOrder = DomainOrder::ASCENDING;
  }

  HSTSHeuristics::~HSTSHeuristics() {
    m_defaultCompatibilityPriority.clear();
    m_defaultTokenStates.clear();
    m_defaultCandidateOrders.clear();
    std::map<double,TokenEntryId>::iterator it = m_tokenHeuristics.begin();
    for (; it != m_tokenHeuristics.end(); ++it) {
      delete (TokenEntry*)((*it).second);
    }
    m_tokenHeuristics.clear();
    std::map<double,VariableEntryId>::iterator it2 = m_variableHeuristics.begin();
    for (; it != m_variableHeuristics.end(); ++it) {
      delete (VariableEntry*)((*it).second);
    }
    m_variableHeuristics.clear();
  }

  void HSTSHeuristics::setDefaultPriorityPreference(const PriorityPref pp) {
    m_defaultPriorityPreference = pp;
  }

  void HSTSHeuristics::setDefaultPriorityForTokenDPsWithParent(const Priority p, const TokenTypeId& tt) {
    check_error(MIN_PRIORITY <= p);
    check_error(MAX_PRIORITY >= p);
    m_defaultCompatibilityPriority.insert(std::make_pair<LabelStr,Priority>(tt.getIndexKey(), p));
  }

  void HSTSHeuristics::setDefaultPriorityForTokenDPs(const Priority p) {
    check_error(MIN_PRIORITY <= p);
    check_error(MAX_PRIORITY >= p);
    m_defaultTokenPriority = p;
  }

  void HSTSHeuristics::setDefaultPriorityForConstrainedVariableDPs(const Priority p) {
    check_error(MIN_PRIORITY <= p);
    check_error(MAX_PRIORITY >= p);
    m_defaultVariablePriority = p;
  }

  void HSTSHeuristics::setDefaultPreferenceForTokenDPs(const std::vector<TokenDecisionPointState>& states, 
						       const std::vector<CandidateOrder>& orders) {
    check_error(states.size() == orders.size());
    m_defaultTokenStates = states;
    m_defaultCandidateOrders = orders;
  }

  void HSTSHeuristics::setDefaultPreferenceForConstrainedVariableDPs(const DomainOrder order) {
    m_defaultDomainOrder = order;
  }

  void HSTSHeuristics::setHeuristicsForConstrainedVariableDP(const Priority p, 
							     const LabelStr variableName, 
							     const TokenTypeId& tt, 
							     const DomainOrder order,
							     const LabelStr& generatorName, 
							     const std::list<double>& enumeration) {
    LabelStr key;
    if (tt.isNoId())
      key = HSTSHeuristics::getIndexKey(variableName,tt);
    else
      key = variableName;
    // get varId from variableName and tt
    // get baseDomain
    // if (generatorName == NO_STRING) {
    //  VariableEntry entry(baseDomain.getValues(), p, order, enumeration);
    // }
    // else  {
    //   VariableEntry entry(baseDomain.getValues(), p, order, generatorName);
    // }
    m_variableHeuristics.insert(std::make_pair<LabelStr,VariableEntry>(key, entry));
  }

  void setHeuristicsForTokenDP(const Priority p,
			       const TokenTypeId& tt, 
			       const Relationship rel, 
			       const TokenTypeId& mastertt, 
			       const Origin o, 
			       const std::vector<TokenDecisionPoint::State>& states, 
			       const std::vector<CandidateOrder>& orders, 
			       const std::vector<LabelStr>& generatorNames) {
    check_error(states.size() == orders.size());
    LabelStr key = HSTSHeuristics::getIndexKey(o, tt, mastertt, rel);
    TokenEntry entry(p, states, orders, generatorNames);
    m_tokenHeuristics.insert(std::make_pair<LabelStr, TokenEntry>(key, entry));
  }

  void HSTSHeuristics::addSuccTokenGenerator(const GeneratorId& generator) {
    check_error(generator.isValid());
    check_error(m_generatorsByName.find(generator->getName().getKey()) == m_generatorsByName.end());
    m_succTokenGenerators.push_back(generator);
    m_generatorsByName.insert(std::pair<double,GeneratorId>(generator->getName().getKey(), generator));
  }

  void HSTSHeuristics::addVariableGenerator(const GeneratorId& generator) {
    check_error(generator.isValid());
    check_error(m_generatorsByName.find(generator->getName().getKey()) == m_generatorsByName.end());
    m_variableGenerators.push_back(generator);
    m_generatorsByName.insert(std::pair<double,GeneratorId>(generator->getName().getKey(), generator));
  }

  const GeneratorId& HSTSHeuristics::getGeneratorByName(const LabelStr& name) const {
    std::map<double,GeneratorId>::iterator pos = m_generatorsByName.find(name.getKey());
    if (pos == m_generatorsByName.end())
      return GeneratorId::noId();
    else
      return (pos->second);
  }

  const TokenTypeId HSTSHeuristics::TokenType::createTokenType(const TokenId& token) {
    if (token.isNoId()) return TokenType::noId();
    std::vector<std::pair<LabelStr,LabelStr> > domains;
    for (int i = 0; i < variables.size(); ++i) {
      if (variables[i]->lastDomain().isSingleton()) {
	double val = variables[i]->lastDomain().getSingletonValue();
	LabelStr name = variables[i]->getName();
	if (variables[i]->lastDomain().isNumeric()) {
	  domains.push_back(std::make_pair<LabelStr,LabelStr>(name,val)); 
	} 
	else {
	  if (LabelStr::isString(val))
	    domains.push_back(std::make_pair<LabelStr,LabelStr>(name,(val).toString()));
	  else {
	    EntityId entity(valueAsDouble);
	    domains.push_back(std::make_pair<LabelStr,LabelStr>(name,entity->getName().toString()));
	  }
	}
      }
    }
    return (new TokenType(tok->getName(), domains))->getId();
  }

  const Priority HSTSHeuristics::getPriorityForTokenDP(const TokenDecisionPointId& tokDec) {
    TokenId tok = tokDec->getToken();
    TokenTypeId tt = HSTSHeuristics::TokenType::createTokenType(tok);
    TokenTypeId mastertt = HSTSHeuristics::TokenType::createTokenType(tok->getMaster());
    Relationship rel = tok->getRelationship(); // once it is implemented on tokens.
    Origin orig;
    if (mastertt.isNoId())
      origin = Origin::INITIAL;
    else
      origin = Origin::SUBGOAL;
    return getInternalPriorityForTokenDP(tt, rel, mastertt, orig)
  }

  const Priority HSTSHeuristics::getPriorityForObjectDP(const ObjectDecisionPointId& objDec) {
    return getPriorityForTokenDP(objDec->getToken());
  }

  const Priority HSTSHeuristics::getPriorityForConstrainedVariableDP(const ConstrainedVariableDecisionPointId& varDec) {
    const ConstrainedVariableId& var = varDec->getVariable();
    const EntityId& parent = var->getParent();
    Priority p;
    if (parent.isNoId() || !TokenId::convertable(parent)) 
      return m_defaultVariablePriority;
    TokenTypeId tt = HSTSHeuristics::TokenType::createTokenType(parent);
    return getInternalPriorityForConstrainedVariableDP(var->getName(), tt);
  }

  const Priority HSTSHeuristics::getInternalPriorityForConstrainedVariableDP(const LabelStr variableName, 
									     const TokenTypeId& tt) {
    LabelStr key = HSTSHeuristics::getIndexKey(variableName,tt);
    std::map<double, VariableEntryId>::iterator pos = m_variableHeuristics.find(key);
    if (pos != m_variableHeuristics.end())
      return (*pos)->getPriority();
    return m_defaultVariablePriority;
  }

  const Priority HSTSHeuristics::getInternalPriorityForTokenDP(const TokenTypeId& tt, 
							       const Relationship rel, 
							       const TokenTypeId& mastertt, 
							       const Origin o) {
    check_error(tt.isValid());
    check_error(mastertt.isValid());
    LabelStr key = HSTSHeuristics::getIndexKey(o, tt, mastertt, rel);
    std::map<double, TokenEntryId>::iterator pos = m_tokenHeuristics.find(key);
    if (pos != m_tokenHeuristics.end())
      return (*pos)->getPriority();
    LabelStr key2 = HSTSHeuristics::TokenType::getIndexKey(mastertt);
    std::map<double, Priority>::iterator pos2 =  m_defaultCompatibilityPriority.find(key2);
    if (pos2 != m_defaultCompatibilityPriority.end())
      return pos2->second;
    return m_defaultTokenPriority;
  }

  const LabelStr HSTSHeuristics::getIndexKey(const LabelStr& variableName, 
					     const TokenTypeId& tt) {
    check_error(tt.isValid());
    std::stringstream key;
    key << variableName << DELIMITER << HSTSHeuristics::TokenType::getIndexKey(tt);
    key << std::endl;
    return key.str();
  }

  const LabelStr HSTSHeuristics::getIndexKey(const TokenTypeId& tt, 
					     const Relationship rel, 
					     const TokenTypeId& mastertt, 
					     const Origin o) {
    check_error(tt.isValid());
    check_error(mastertt.isValid() || mastertt.isNoId());
    std::stringstream key;
    if (o != Origin::FREE)
      key << o << DELIMITER;
    key << HSTSHeuristics::TokenType::getIndexKey(tt);
    if (!mastertt.isNoId()) {
      key << DELIMITER << HSTSHeuristics::TokenType::getIndexKey(mastertt);
      key << DELIMITER << rel;
    }
    key << std::endl;
    return key.str();
  }


  /*
  void HSTSHeuristics::setPriorityForConstrainedVariableDP(const Priority p, 
							   const LabelStr variableName, 
							   const TokenTypeId& tt) {
    check_error(MIN_PRIORITY <= p);
    check_error(MAX_PRIORITY >= p);

    LabelStr key = HSTSHeuristics::getIndexKey(variableName,tt);
    VariableEntry entry;
    entry.setPriority(p);
    m_variableHeuristics.insert(std::make_pair<LabelStr,VariableEntry>(key, entry));
  }

  void HSTSHeuristics::setPriorityForTokenDP(const Priority p, const TokenTypeId& tt, 
					     const Relationship rel, 
					     const TokenTypeId& mastertt, const Origin o) {
    LabelStr key = HSTSHeuristics::getIndexKey(o, tt, mastertt, rel);
    TokenEntry entry;
    entry.setPriority(p);
    m_tokenHeuristics.insert(std::make_pair<LabelStr, TokenEntry>(key, entry));
  }

  // todo: generators?
  void setPreferenceForTokenValueChoice(const std::vector<TokenDecisionPoint::State>& states, 
					const std::vector<CandidateOrder>& orders, 
					const std::vector<LabelStr>& generatorNames,
					const TokenTypeId& tt, Relationship rel, 
					const TokenTypeId& mastertt, const Origin o) {
    LabelStr key = HSTSHeuristics::getIndexKey(o, tt, mastertt, rel);
    TokenEntry entry;
    entry.setStates(states);
    entry.setOrders(orders);
    // generators
  }

  void setPreferenceForVariableValueChoice(const DomainOrder order, 
					   const LabelStr& variableName, 
					   const TokenTypeId& tt, 
					   const LabelStr& generatorName, 
					   const std::list<double>& enumeration) {
    LabelStr key = HSTSHeuristics::getIndexKey(variableName,tt);
    // get variable base domain
    VariableEntry entry(baseDomain, order, generatorName); 

    // convert domain order and enumeration into actual enumeration
    // insert resulting enumeration
  }

  */
}
