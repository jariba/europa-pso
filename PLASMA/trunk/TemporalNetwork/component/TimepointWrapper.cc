#include "TimepointWrapper.hh"
#include "TemporalPropagator.hh"

namespace Prototype {

  TimepointWrapper::TimepointWrapper(const TemporalPropagatorId& prop, const TempVarId& tempVar, const TimepointId& point) 
    :m_propagator(prop), m_tempVar(tempVar), m_timepoint(point), m_id(this) {}

  TimepointWrapper::~TimepointWrapper() {
    m_propagator->notifyDeleted(m_tempVar, m_timepoint); 
    m_id.remove();
  }

}
