#include "RuleVariableListener.hh"
#include "RuleInstance.hh"
#include "DomainListener.hh"
#include "ConstrainedVariable.hh"
#include "ConstraintType.hh"
#include "LabelStr.hh"
#include "Rule.hh"

namespace EUROPA {

  RuleVariableListener::RuleVariableListener(const LabelStr& name,
					     const LabelStr& propagatorName,
					     const ConstraintEngineId& constraintEngine,
					     const std::vector<ConstrainedVariableId>& scope)
    : Constraint(name, propagatorName, constraintEngine, scope){}


  RuleVariableListener::RuleVariableListener(const ConstraintEngineId& constraintEngine,
					     const RuleInstanceId& ruleInstance,
					     const std::vector<ConstrainedVariableId>& scope)
    : Constraint(CONSTRAINT_NAME(), PROPAGATOR_NAME(), constraintEngine, scope),
      m_ruleInstance(ruleInstance){
    check_error(! m_ruleInstance->isExecuted(),
		"A Rule Instance should never be already executed when we construct the constraint!");

    // Add rule variable listener as a dependent of the rule instance to receive discard notifications
    m_ruleInstance->addDependent(this);
  }

  /**
   * @see Mergemento::merge
   */
  void RuleVariableListener::setSource(const ConstraintId& sourceConstraint){
    check_error(sourceConstraint.isValid());

    checkError(sourceConstraint->getName() == getName(),
	       "Supposed to be sourced from constraint of same type." << sourceConstraint->toString());

    m_sourceConstraint = sourceConstraint;
  }

  /**
   * @brief Handle all behaviour immediately on set or reset operations
   * so that rule execution is not subject to the vagaries of propagtion timing
   * @return true
   */
  bool RuleVariableListener::canIgnore(const ConstrainedVariableId& variable,
				       int argIndex,
				       const DomainListener::ChangeType& changeType){
    checkError(getRuleInstance().isValid(), getKey() << " has lost its rule instance:" << getRuleInstance());

    if(getRuleInstance().isNoId())
      return true;

    debugMsg("RuleVariableListener:canIgnore", "Checking canIgnore for guard listener for rule " <<
	     getRuleInstance()->getRule()->getName() << " from source " << getRuleInstance()->getRule()->getName());
    // If a Reset has occurred, and the rule has been fired, we may have to do something right now
//     if(getRuleInstance()->isExecuted() &&
//        (changeType == DomainListener::RESET || changeType == DomainListener::RELAXED) &&
//        !getRuleInstance()->test(getScope())){
//       getRuleInstance()->undo();
//       return true;
//     }

    return false;
  }

  const RuleInstanceId& RuleVariableListener::getRuleInstance() {
    if(m_ruleInstance.isNoId()){
      checkError(m_sourceConstraint.isValid(), "Must be able to get this from a source constraint.");

      // Now obtain the rule instance from the source
      RuleVariableListener* source = (RuleVariableListener*) m_sourceConstraint;
      m_ruleInstance = source->getRuleInstance();

      checkError(m_ruleInstance.isNoId() || m_ruleInstance.isValid(), m_sourceConstraint->toString());

      // It is possible that this constraint is being migrated even though the master has been terminated. If that were the case then the rule
      // instance may have been cleared. So we have to check for that.
      if(!m_ruleInstance.isNoId())
	m_ruleInstance->addDependent(this);
    }
  
    return m_ruleInstance;
  }

  /**
   * Evalautes if it should execute the rule, and does so if appropriate
   */
  void RuleVariableListener::handleExecute() {
    // Only apply when all guards are singeltons
//     for(unsigned int i = 0; i < getScope().size(); i++)
//       if(!getScope()[i]->lastDomain().isSingleton())
// 	return;

//     // Fire if appropriate
//     if(!getRuleInstance()->isExecuted() &&  getRuleInstance()->test(getScope()))
//       getRuleInstance()->prepareExecute();
//     else if(getRuleInstance()->isExecuted() && 
    getRuleInstance()->prepare();
  }

  void RuleVariableListener::notifyDiscarded(const Entity*){
    m_ruleInstance = RuleInstanceId::noId();
    if(isActive())
      deactivate();
  }

  void RuleVariableListener::handleDiscard(){
    if(!Entity::isPurging() && m_ruleInstance.isId())
      m_ruleInstance->removeDependent(this);

    Constraint::handleDiscard();
  }

  /**
   * @brief If the base class test passes, then we need to see if there is any more information contained in the rule that
   * has not been applied. This will be the case if the rule has not fired yet and the test indicates it could.
   */
  bool RuleVariableListener::testIsRedundant(const ConstrainedVariableId& var) const{
    return Constraint::testIsRedundant(var) && (m_ruleInstance.isNoId() || m_ruleInstance->isExecuted() || !m_ruleInstance->test(getScope()));
  }
}
