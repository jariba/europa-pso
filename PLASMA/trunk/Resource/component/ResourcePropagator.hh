#ifndef _H_ResourcePropagator
#define _H_ResourcePropagator

#include "../ConstraintEngine/Propagator.hh"
#include "ResourceDefs.hh"
#include <set>

namespace Prototype {

  class ResourcePropagator: public Propagator
  {
  public:
    ResourcePropagator(const LabelStr& name, const ConstraintEngineId& constraintEngine);
    void execute();
    bool updateRequired() const;
  protected:
    void handleConstraintAdded(const ConstraintId& constraint) {}
    void handleConstraintRemoved(const ConstraintId& constraint) {}
    void handleConstraintActivated(const ConstraintId& constrain) {}
    void handleConstraintDeactivated(const ConstraintId& constraint) {}
    void handleNotification(const ConstrainedVariableId& variable, 
    			    int argIndex, 
    			    const ConstraintId& constraint, 
    			    const DomainListener::ChangeType& changeType);

  private:
    
    void handleObjectChange(const ConstrainedVariableId& variable);
    void handleQuantityChange(const ConstrainedVariableId& variable, 
    			    int argIndex, 
    			    const ConstraintId& constraint, 
    			    const DomainListener::ChangeType& changeType);
    void handleTimeChange(const ConstrainedVariableId& variable, 
    			    int argIndex, 
    			    const ConstraintId& constraint, 
    			    const DomainListener::ChangeType& changeType);
    void handleResourcePropagation(const ResourceId& r, const ConstrainedVariableId& variable);

    std::set<ResourceId> m_resources;
    ConstrainedVariableId m_forempty;


  };
}
#endif
