#include "ResourceConstraint.hh"
#include "Utils.hh"

namespace Prototype
{
  
  ResourceConstraint::ResourceConstraint(const LabelStr& name,
					 const LabelStr& propagatorName,
					 const ConstraintEngineId& constraintEngine,
					 const std::vector<ConstrainedVariableId>& variables)
    : Constraint(name, propagatorName, constraintEngine, variables){
    check_error(variables.size() == (unsigned int) ARG_COUNT);
    //@todo add type checking of each variable in the constraint
  }

  AbstractDomain&  ResourceConstraint::getCurrentDomain(const ConstrainedVariableId& var){
    return Constraint::getCurrentDomain(var);
  }

  void ResourceConstraint::handleExecute()
  {
  }

  bool ResourceConstraint::canIgnore(const ConstrainedVariableId& variable, 
				     int argIndex, 
				     const DomainListener::ChangeType& changeType){
    // if it is a restriction, but not a singleton, then we can ignore it.
    // if(changeType != DomainListener::RESET && changeType != DomainListener::RELAXED)
    //   return !getCurrentDomain(variable).isSingleton();
    return false;
  }

}//namespace prototype
