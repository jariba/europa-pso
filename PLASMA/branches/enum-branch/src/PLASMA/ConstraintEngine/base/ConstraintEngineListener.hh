#ifndef _H_ConstraintEngineListener
#define _H_ConstraintEngineListener

#include "ConstraintEngineDefs.hh"
#include "DomainListener.hh"

/**
 * @file ConstraintEngineListener.hh
 * @author Conor McGann
 * @brief Abstract interfaces for publishing events from Constraint Engine.
 */
namespace EUROPA{

  /**
   * @class ConstraintEngineListener
   * @brief The abstract interface for events published form Constraint Engine.
   */
  class ConstraintEngineListener{
  public:
    virtual ~ConstraintEngineListener();

    virtual void notifyPropagationCommenced(){
    }

    virtual void notifyPropagationCompleted(){
    }

    virtual void notifyPropagationPreempted(){
    }

    virtual void notifyAdded(const ConstraintId& constraint){
    }

    virtual void notifyActivated(const ConstraintId& constraint){
    }

    virtual void notifyDeactivated(const ConstrainedVariableId& var){
    }

    virtual void notifyActivated(const ConstrainedVariableId& var){
    }

    virtual void notifyDeactivated(const ConstraintId& constraint){
    }

    virtual void notifyRemoved(const ConstraintId& constraint){
    }

    virtual void notifyExecuted(const ConstraintId& constraint){
    }

    virtual void notifyAdded(const ConstrainedVariableId& variable){
    }

    virtual void notifyRemoved(const ConstrainedVariableId& variable){
    }

    virtual void notifyChanged(const ConstrainedVariableId& variable, const DomainListener::ChangeType& changeType){
    }

    virtual void notifyViolationAdded(const ConstraintId& constraint){
    }

    virtual void notifyViolationRemoved(const ConstraintId& constraint){
    }


    const ConstraintEngineListenerId& getId() const;

    // for PSConstraintEngineListener, where we don't have access to plan database
    // at construction time:
    virtual void setConstraintEngine(const ConstraintEngineId& constraintEngine);


  protected:
    ConstraintEngineListener(const ConstraintEngineId& constraintEngine);
    ConstraintEngineListenerId m_id;
    ConstraintEngineId m_constraintEngine;

    // for PSConstraintEngineListener, where we don't have access to constraint engine
     // at construction time
     ConstraintEngineListener();
  };
}
#endif
