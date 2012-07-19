#include "SAVH_TimetableFVDetector.hh"
#include "SAVH_Instant.hh"
#include "SAVH_FVDetector.hh"

namespace EUROPA {
  namespace SAVH {
    class TimetableFVDetectorLocalStatic {
    public:
      TimetableFVDetectorLocalStatic() {
	static bool sl_registerFactory = false;
	check_error(!sl_registerFactory, "Should only be called once.");
	if(!sl_registerFactory) {
	  REGISTER_FVDETECTOR(EUROPA::SAVH::TimetableFVDetector, TimetableFVDetector);
	  sl_registerFactory = true;
	}
      }
    };

    TimetableFVDetectorLocalStatic s_localStatic;

    TimetableFVDetector::TimetableFVDetector(const ResourceId res) : FVDetector(res) {
      m_lowerLimit = res->getLowerLimit();
      m_upperLimit = res->getUpperLimit();
      m_maxInstConsumption = res->getMaxInstConsumption();
      m_maxInstProduction = res->getMaxInstProduction();
      m_maxCumulativeConsumption = res->getMaxConsumption();
      m_maxCumulativeProduction = res->getMaxProduction();
      debugMsg("TimetableFVDetector:TimetableFVDetector", "Created FVDetector for " << res->toString());
      debugMsg("TimetableFVDetector:TimetableFVDetector", "Got values: lower limit(" << m_lowerLimit << ") upper limit(" << 
	       m_upperLimit << ") max instantaneous consumption(" << m_maxInstConsumption << ") max instantaneous production(" <<
	       m_maxInstProduction << ") max consumption(" << m_maxCumulativeConsumption << ") max production(" << 
	       m_maxCumulativeProduction << ")");
    }

    bool TimetableFVDetector::detect(const InstantId inst) {
      debugMsg("TimetableFVDetector:detect", "Detecting flaws and violations at time " << inst->getTime());
      condDebugMsg(inst->getMinCumulativeConsumption() > m_maxCumulativeConsumption, "TimetableFVDetector:detect", 
		   "Cumulative consumption violation. Limit: " << m_maxCumulativeConsumption << " Minimum cumulative consumption: " << inst->getMinCumulativeConsumption());
      condDebugMsg(inst->getMinCumulativeProduction() > m_maxCumulativeProduction, "TimetableFVDetector:detect", 
		   "Cumulative production violation. Limit: " << m_maxCumulativeProduction << " Minimum cumulative production: " << inst->getMinCumulativeProduction());
      condDebugMsg(inst->getMinInstantConsumption() > m_maxInstConsumption, "TimetableFVDetector:detect", 
		   "Instantaneous consumption violation. Limit: " << m_maxInstConsumption << " Minimum instantaneous consumption: " << inst->getMinInstantConsumption());
      condDebugMsg(inst->getMinInstantProduction() > m_maxInstProduction, "TimetableFVDetector:detect", 
		   "Instantaneous production violation. Limit: " << m_maxInstProduction << " Minimum instantaneous production: " << inst->getMinInstantProduction());
      condDebugMsg(inst->getUpperLevel() + (m_maxCumulativeProduction - inst->getMinCumulativeProduction()) < m_lowerLimit,
		   "TimetableFVDetector:detect", "Lower limit violation.  Limit: " << m_lowerLimit << " Upper level: " << inst->getUpperLevel() <<
		   " Maximum remaining production: " << (m_maxCumulativeProduction - inst->getMinCumulativeProduction()));
      condDebugMsg(inst->getLowerLevel() - (m_maxCumulativeConsumption - inst->getMinCumulativeConsumption()) > m_upperLimit,
		   "TimetableFVDetector:detect", "Upper limit violation.  Limit: " << m_upperLimit << " Lower level: " << inst->getLowerLevel() <<
		   " Maxumum remaining consumption: " << (m_maxCumulativeConsumption - inst->getMinCumulativeConsumption()));
      inst->setViolated(false);
      inst->setFlawed(false);
      //detect violations
      //if the min consumption up to now is more than the max allowable
      if(inst->getMinCumulativeConsumption() > m_maxCumulativeConsumption ||
	 //if the min production up to now is more than the max allowable
	 inst->getMinCumulativeProduction() > m_maxCumulativeProduction ||
	 //if the min consumption at this instant is more than the max allowable
	 inst->getMinInstantConsumption() > m_maxInstConsumption ||
	 //if the min production at this instant is more than the max allowable
	 inst->getMinInstantProduction() > m_maxInstProduction ||
	 //if the upper level plus the max amount of possible production is still below the lower limit
	 inst->getUpperLevel() + (m_maxCumulativeProduction - inst->getMinCumulativeProduction()) < m_lowerLimit ||
	 //if the lower level minus the max amount of possible consumption is still above the upper limit
	 inst->getLowerLevel() - (m_maxCumulativeConsumption - inst->getMinCumulativeConsumption()) > m_upperLimit) {
	inst->setViolated(true);
	notifyOfViolation(inst);
      }
      //detect flaws NOTE: don't really need to do this, since flaws in the timetable situation aren't integrated into planning
      //if(instant->getLowerMax() < m_lowerLimit) {}
      //if(instant->getUpperMax() > m_upperLimit){}
      return false;
    }
  }
}
