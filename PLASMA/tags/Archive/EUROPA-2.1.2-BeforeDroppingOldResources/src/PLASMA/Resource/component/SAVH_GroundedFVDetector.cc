#include "SAVH_GroundedFVDetector.hh"

#include <cmath>

namespace EUROPA {
namespace SAVH {

GroundedFVDetector::GroundedFVDetector(const ResourceId res) : GenericFVDetector(res) {
}

// NOTE: This is the same as ClosedWorldFVDetector, but we don't subclass that as we're expecting a
// reorganization where the violation and flaw detection pieces can be configured separately
ResourceProblem::Type GroundedFVDetector::getResourceLevelViolation(const InstantId inst) const
{
	if (inst->getUpperLevel() < m_lowerLimit)
	{
		debugMsg("GroundedFVDetector:detect",
				"Lower limit violation.  Limit: " << m_lowerLimit << " Upper level: " << inst->getUpperLevel());
		return ResourceProblem::LevelTooLow;
	}
	if (inst->getLowerLevel() > m_upperLimit)
	{
    	debugMsg("GroundedFVDetector:detect",
    			"Upper limit violation.  Limit: " << m_upperLimit << " Lower level: " << inst->getLowerLevel());
    	return ResourceProblem::LevelTooHigh;
	}
	return ResourceProblem::NoProblem;
}

// Here we handle the LowerLevelMax and UpperLevelMin which, in GroundedProfile are hacked to represent
// the grounded min/max instead of the traditional meaning implied by their names.
void GroundedFVDetector::handleResourceLevelFlaws(const InstantId inst)
{
	// LowerLevelMax is really GroundedMin
	if(inst->getLowerLevelMax() < m_lowerLimit)
	{
		inst->setFlawed(true);
		inst->setLower(true);
		inst->setLowerMagnitude(fabs(m_lowerLimit - inst->getLowerLevelMax()));
		debugMsg("GroundedFVDetector:detect", "Lower limit flaw.");
	}

	// UpperLevelMin is really GroundedMax
	if(inst->getUpperLevelMin() > m_upperLimit)
	{
		inst->setFlawed(true);
		inst->setUpper(true);
		inst->setUpperMagnitude(fabs(m_upperLimit - inst->getUpperLevelMin()));
		debugMsg("GroundedFVDetector:detect", "Upper limit flaw.");
	}
}

}
}
