#ifndef _H_CeLogger
#define _H_CeLogger

#include "ConstraintEngineDefs.hh"
#include "ConstraintEngineListener.hh"
#include <iostream>

namespace Prototype {
  class CeLogger : public ConstraintEngineListener {
  public:
    CeLogger(std::ostream& os, const ConstraintEngineId& ce);
    void notifyPropagationCommenced();
    void notifyPropagationCompleted();
    void notifyPropagationPreempted();
    void notifyAdded(const ConstraintId& constraint);
    void notifyRemoved(const ConstraintId& constraint);
    void notifyExecuted(const ConstraintId& constraint);
    void notifyAdded(const ConstrainedVariableId& variable);
    void notifyRemoved(const ConstrainedVariableId& variable);
    void notifyChanged(const ConstrainedVariableId& variable,
                       const DomainListener::ChangeType& changeType);
  private:
    std::ostream& m_os;
  };
}
#endif
