#include "UnboundVariableManager.hh"
#include "ConstrainedVariable.hh"
//#include "MatchingRule.hh"
#include "PlanDatabase.hh"
#include "ConstraintEngine.hh"
#include "Debug.hh"
#include "Constraint.hh"
#include "RuleVariableListener.hh"
#include "RuleInstance.hh"
#include "Token.hh"
#include "Utils.hh"
#include "SolverUtils.hh"
#include "ComponentFactory.hh"
#include "UnboundVariableDecisionPoint.hh"

/**
 * @file Provides implementation for UnboundVariableManager
 * @author Conor McGann
 * @date May, 2005
 */

namespace EUROPA {
  namespace SOLVERS {

    /**
     * @brief Constructor will evaluate the configuration information and construct assembly from there.
     * @see ComponentFactory
     */
    UnboundVariableManager::UnboundVariableManager(const TiXmlElement& configData)
      : FlawManager(configData) {}

    void UnboundVariableManager::handleInitialize(){

      // FILL UP VARIABLES
      const ConstrainedVariableSet& allVars = m_db->getConstraintEngine()->getVariables();
      for(ConstrainedVariableSet::const_iterator it = allVars.begin(); it != allVars.end(); ++it){
	ConstrainedVariableId var = *it;
	updateFlaw(var);
      }

      // PROCESS CONSTRAINTS TO INITIALIZE GUARDS. We are looking for RuleVariableListener constraints since they 
      // determine if a variable is guarded or not.
      const ConstraintSet& allConstraints = m_db->getConstraintEngine()->getConstraints();
      for(ConstraintSet::const_iterator it = allConstraints.begin(); it != allConstraints.end(); ++it){ 
	ConstraintId constraint = *it;
	handleConstraintAddition(constraint);
      }
    }

    DecisionPointId UnboundVariableManager::nextZeroCommitmentDecision(){
      for(ConstrainedVariableSet::const_iterator it = m_singletonFlawCandidates.begin(); 
	  it != m_singletonFlawCandidates.end(); ++it){
	ConstrainedVariableId var = *it;
	checkError(var.isValid(), var);
	checkError(var->lastDomain().isSingleton(), "Buffer management error:" << var->toString());

	if(!dynamicMatch(var)){
	  debugMsg("UnboundVariableManager:nextZeroCommitmentDecision", "Allocating for " << var->toString());
	  return allocateDecisionPoint(var);
	}
      }

      return DecisionPointId::noId();
    }

    bool UnboundVariableManager::dynamicMatch(const EntityId& entity){
      if (FlawManager::dynamicMatch(entity))
	return true;

      ConstrainedVariableId var = entity;

      // We also exclude singletons unless they are guards
      if(!isCompatGuard(var) && var->lastDomain().isSingleton())
	return true;

      // Finally, we exlude if the bounds are not finite
      return !var->lastDomain().areBoundsFinite();
    }

    bool UnboundVariableManager::inScope(const EntityId& entity){
      bool result = false;
      if(ConstrainedVariableId::convertable(entity)){
	ConstrainedVariableId var = entity;
	result =  (!var->isSpecified() && FlawManager::inScope(entity));
      }

      return result;
    }

    /**
     * We may filter based on static information only.
     */
    void UnboundVariableManager::updateFlaw(const ConstrainedVariableId& var){
      debugMsg("UnboundVariableManager:updateFlaw", var->toString());
      m_flawCandidates.erase(var);
      m_singletonFlawCandidates.erase(var);

      if(variableOfNonActiveToken(var) || !var->canBeSpecified() || var->isSpecified() || staticMatch(var)){
	debugMsg("UnboundVariableManager:updateFlaw", "Excluding: " << var->toString());
	return;
      }

      debugMsg("UnboundVariableManager:addFlaw",
	       "Adding " << var->toString() << " as a candidate flaw.");

      m_flawCandidates.insert(var);

      if(var->lastDomain().isSingleton() && isCompatGuard(var))
	m_singletonFlawCandidates.insert(var);
    }

    void UnboundVariableManager::removeFlaw(const ConstrainedVariableId& var){
      condDebugMsg(m_flawCandidates.find(var) != m_flawCandidates.end(), 
		   "UnboundVariableManager:removeFlaw", "Removing " << var->toString() << " as a flaw.");

      m_flawCandidates.erase(var);
      m_singletonFlawCandidates.erase(var);
    }

    void UnboundVariableManager::addGuard(const ConstrainedVariableId& var){
      std::map<ConstrainedVariableId, unsigned int>::iterator it = m_guardCache.find(var);
      unsigned int refCount = 1;
      // If already guarded just increment the ref count
      if(it != m_guardCache.end()){
	refCount = it->second;
	refCount++;
	it->second = refCount;
      }
      else // Insert a new pair
	m_guardCache.insert(std::pair<ConstrainedVariableId, unsigned int>(var, 1));

      debugMsg("UnboundVariableManager:addGuard", 
	       "GUARDS=" << refCount << " for " << var->getName().toString() << "(" << var->getKey() << ")");

      updateFlaw(var);
    }

    void UnboundVariableManager::removeGuard(const ConstrainedVariableId& var){
      std::map<ConstrainedVariableId, unsigned int>::iterator it = m_guardCache.find(var);
      check_error(it != m_guardCache.end(), "Cannot see how guard would not be here so force it to be.");

      unsigned int refCount = 0;
      if(it->second == 1)
	m_guardCache.erase(it);
      else {
	refCount = it->second - 1;
	it->second = refCount;
      }

      debugMsg("UnboundVariableManager:removeGuard", 
	       "GUARDS=" << refCount << " for " << var->getName().toString() << "(" << var->getKey() << ")");

      updateFlaw(var);
    }

    void UnboundVariableManager::handleConstraintAddition(const ConstraintId& constraint){
      if(constraint->getName() == RuleVariableListener::CONSTRAINT_NAME()){
	const std::vector<ConstrainedVariableId>& scope = constraint->getScope();
	for(std::vector<ConstrainedVariableId>::const_iterator it = scope.begin(); it != scope.end(); ++it){
	  ConstrainedVariableId guard = *it;
	  addGuard(guard);
	}
      }
    }

    void UnboundVariableManager::handleConstraintRemoval(const ConstraintId& constraint){
      if(constraint->getName() == RuleVariableListener::CONSTRAINT_NAME()){
	const std::vector<ConstrainedVariableId>& scope = constraint->getScope();
	for(std::vector<ConstrainedVariableId>::const_iterator it = scope.begin(); it != scope.end(); ++it){
	  ConstrainedVariableId guard = *it;
	  removeGuard(guard);
	}
      }
    }

    bool UnboundVariableManager::variableOfNonActiveToken(const ConstrainedVariableId& var){
      // If var parent is a token and the state is active, then true.
      if(TokenId::convertable(var->getParent())){
	TokenId token(var->getParent());
	return !token->isActive();
      }

      // Otherwise false
      return false;
    }


    void UnboundVariableManager::notifyRemoved(const ConstrainedVariableId& variable){
      removeFlaw(variable);
    }

    void UnboundVariableManager::notifyChanged(const ConstrainedVariableId& variable, 
					       const DomainListener::ChangeType& changeType){

      // In the event it is bound to a singleton, we remove it altogether as a flaw.
      if(changeType == DomainListener::SET_TO_SINGLETON){
	// If it is a token state variable, we test if a case for activation
	if(Token::isStateVariable(variable) && variable->getSpecifiedValue() == Token::ACTIVE){
	  TokenId token = variable->getParent();
	  const std::vector<ConstrainedVariableId>& variables = token->getVariables();
	  for(std::vector<ConstrainedVariableId>::const_iterator it = variables.begin(); it != variables.end(); ++it){
	    ConstrainedVariableId var = *it;
	    updateFlaw(var);
	  }
	}
	else
	  removeFlaw(variable);

	return;
      }

      if(changeType == DomainListener::RESET && Token::isStateVariable(variable)){
	TokenId token = variable->getParent();
	const std::vector<ConstrainedVariableId>& variables = token->getVariables();
	for(std::vector<ConstrainedVariableId>::const_iterator it = variables.begin(); it != variables.end(); ++it){
	  ConstrainedVariableId var = *it;
	  removeFlaw(var);
	}
	return;
      }

      // Now listen for all the other events of interest. We can ignore other cases of restriction since
      // the event set below is sufficient to capture all the meaningful changes without incurring
      // all the evaluation costs on every propagation.
      if(changeType == DomainListener::RESET || 
	 changeType == DomainListener::CLOSED ||
	 changeType == DomainListener::RELAXED ||
	 changeType == DomainListener::RESTRICT_TO_SINGLETON)
	updateFlaw(variable);
    }

    void UnboundVariableManager::notifyAdded(const ConstraintId& constraint){
      handleConstraintAddition(constraint);
    }

    void UnboundVariableManager::notifyRemoved(const ConstraintId& constraint){
      handleConstraintRemoval(constraint);
    }

    IteratorId UnboundVariableManager::createIterator() {
      return (new UnboundVariableManager::FlawIterator(*this))->getId();
    }

    UnboundVariableManager::FlawIterator::FlawIterator(UnboundVariableManager& manager)
      : m_visited(0), m_timestamp(manager.m_db->getConstraintEngine()->cycleCount()),
	m_manager(manager), m_it(manager.m_flawCandidates.begin()), m_end(manager.m_flawCandidates.end())  {

      // Must advance to the first available flaw in scope.
      while(!done()){
	ConstrainedVariableId var = *m_it;
	if(!m_manager.dynamicMatch(var))
	  break;
	else
	  ++m_it;
      }
    }
    
    bool UnboundVariableManager::FlawIterator::done() const { return m_it == m_end;}

    const EntityId UnboundVariableManager::FlawIterator::next() {
      check_error(m_manager.m_db->getConstraintEngine()->cycleCount() == m_timestamp,
		  "Error: potentially stale flaw iterator.");
      checkError(!done(), "Cannot be done when you call next.");
      ConstrainedVariableId flaw = *m_it;
      checkError(!m_manager.dynamicMatch(flaw), "Not advancing correctly.");
      ++m_visited;

      // Advance till we get another hit
      ++m_it;
      while(!done()){
	ConstrainedVariableId var = *m_it;
	if(!m_manager.dynamicMatch(var))
	  break;
	else
	  ++m_it;
      }

      return flaw;
    }

    bool UnboundVariableManager::isCompatGuard(const ConstrainedVariableId& var) const{
      return (m_guardCache.find(var) != m_guardCache.end());
    }

    bool UnboundVariableManager::betterThan(const EntityId& a, const EntityId& b){
      if(a.isId() && b.isId()) {
	bool aCompat = isCompatGuard(a);
	bool bCompat = isCompatGuard(b);
	if(aCompat && !bCompat)
	  return true;
	else if(!aCompat && bCompat)
	  return false;
      }

      return FlawManager::betterThan(a, b);
    }

    std::string UnboundVariableManager::toString(const EntityId& entity) const {
      checkError(ConstrainedVariableId::convertable(entity), entity->toString());
      ConstrainedVariableId var = entity;
      std::string compatStr = (isCompatGuard(var) ? " GUARD" : "");
      std::string unitStr = (var->lastDomain().isSingleton() ? " UNIT" : "");
      std::stringstream os;
      os << "VAR:   " << var->toString() << unitStr << compatStr;
      return os.str();
    }
  }
}
