#include "EqualityConstraintPropagator.hh"
#include "Constraint.hh"
#include "Constraints.hh"
#include "IntervalIntDomain.hh"
#include "BoolDomain.hh"
#include "ConstrainedVariable.hh"
#include "Domain.hh"

namespace Prototype {

  EqualityConstraintPropagator::EqualityConstraintPropagator(const LabelStr& name, const ConstraintEngineId& constraintEngine)
    : Propagator(name, constraintEngine), m_fullReprop(false), m_active(false){}

  EqualityConstraintPropagator::~EqualityConstraintPropagator(){}

  void EqualityConstraintPropagator::execute() {
    check_error(!m_active);
    m_active = true;
    if(m_fullReprop){
      m_eqClassCollection.getGraphKeys(m_eqClassAgenda);
      m_fullReprop = false;
    }

    // Now process the agenda
    for(std::set<int>::iterator it = m_eqClassAgenda.begin(); it != m_eqClassAgenda.end(); ++it){
      const std::set<ConstrainedVariableId>& eqClassScope = m_eqClassCollection.getGraphVariables(*it);
      equate(eqClassScope);
    }

    m_eqClassAgenda.clear();
    m_active = false;
  }

  bool EqualityConstraintPropagator::updateRequired() const {
    return (!m_eqClassAgenda.empty() || m_fullReprop);
  }

  void EqualityConstraintPropagator::handleConstraintAdded(const ConstraintId& constraint){
    check_error(!m_active);
    const ConstrainedVariableId& x = constraint->getScope()[0];
    const ConstrainedVariableId& y = constraint->getScope()[1];

    // Remove old equivalence classes for these variables from the agenda since they are abut to be merged.
    m_eqClassAgenda.erase(m_eqClassCollection.getGraphKey(x));
    m_eqClassAgenda.erase(m_eqClassCollection.getGraphKey(y));

    // Now add the merged equivalence class back to the agenda
    m_eqClassCollection.addConnection(x,y);
    m_eqClassAgenda.insert(m_eqClassCollection.getGraphKey(x));
  }

  void EqualityConstraintPropagator::handleConstraintRemoved(const ConstraintId& constraint){
    check_error(!m_active);
    const ConstrainedVariableId& x = constraint->getScope()[0];
    const ConstrainedVariableId& y = constraint->getScope()[1];
    m_eqClassCollection.removeConnection(x, y);
    m_fullReprop = true;
  }

  void EqualityConstraintPropagator::handleConstraintActivated(const ConstraintId& constraint){
    handleConstraintAdded(constraint);
  }

  void EqualityConstraintPropagator::handleConstraintDeactivated(const ConstraintId& constraint){
    handleConstraintRemoved(constraint);
  }

  void EqualityConstraintPropagator::handleNotification(const ConstrainedVariableId& variable, 
							int argIndex, 
							const ConstraintId& constraint, 
							const DomainListener::ChangeType& changeType){
    check_error(Id<EqualConstraint>::convertable(constraint));

    if(!m_fullReprop && !m_active){
      int eqClassKey = m_eqClassCollection.getGraphKey(variable);
      check_error(m_eqClassCollection.getGraphVariables(eqClassKey).size() > 0);
      check_error(m_eqClassCollection.getGraphVariables(eqClassKey).find(variable) != m_eqClassCollection.getGraphVariables(eqClassKey).end());
      m_eqClassAgenda.insert(eqClassKey);
    }
  }

  void processScope(const std::set<ConstrainedVariableId>& scope){
    AbstractDomain& domain(EqualConstraint::getCurrentDomain(* (scope.begin())));

    if(domain.isOpen())
      return;

    // Set up the initial values to match others against
    bool isFinite = domain.isFinite();
    int domainType = domain.getType();

    // Iterate over, restricting domain as we go
    for(std::set<ConstrainedVariableId>::const_iterator it = scope.begin(); it != scope.end(); ++it){
      AbstractDomain& currentDomain = EqualConstraint::getCurrentDomain(*it);
      check_error(currentDomain.getType() == domainType);

      if(currentDomain.isOpen())
	return;

      if((currentDomain.isFinite() != isFinite) || 
	 domain.intersect(currentDomain) && domain.isEmpty()){
	currentDomain.empty();
	return;
      }
    }

    // If we get to here, we have computed the new domain for all variables in the scope and we know that no
    // domain has been emptied (this could be optimized by recording the last change to dommain)
    for(std::set<ConstrainedVariableId>::const_iterator it = scope.begin(); it != scope.end(); ++it){
      AbstractDomain& currentDomain = EqualConstraint::getCurrentDomain(*it);
      currentDomain.intersect(domain);
    }
  }

  void EqualityConstraintPropagator::equate(const std::set<ConstrainedVariableId>& scope){
    check_error(!scope.empty());
    processScope(scope);
  }
}
