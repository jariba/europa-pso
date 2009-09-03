#include "ExampleStateResourceCustomCode.hh"

using namespace std;

// Put any C++ project-specific custom code here
StateProfile::StateProfile(const PlanDatabaseId db, const FVDetectorId flawDetector, const double initCapacityLb, const double initCapacityUb)
: TimetableProfile(db, flawDetector, initCapacityLb, initCapacityUb) {}


void StateProfile::handleTransactionStart(bool isConsumer, const double & lb, const double & ub)
{
	// if the transaction just started, add producer to upper bounds and
	// consumer to the lower bounds
	if(isConsumer) {
		m_lowerLevelMin -= ub;
		m_lowerLevelMax -= lb;
		bound(m_lowerLevelMax);
		bound(m_lowerLevelMin);
	}
	else {
		m_upperLevelMin += lb;
		m_upperLevelMax += ub;
		bound(m_upperLevelMax);
		bound(m_upperLevelMin);
	}
}

void StateProfile::handleTransactionEnd(bool isConsumer, const double & lb, const double & ub)
{
	// if the transaction just ended, add producer to lower bounds and
	// consumer to the upper bounds
	if(isConsumer) {
		m_upperLevelMax -= lb;
		m_upperLevelMin -= ub;
		bound(m_upperLevelMax);
		bound(m_upperLevelMin);
	}
	else {
		m_lowerLevelMin += lb;
		m_lowerLevelMax += ub;
		bound(m_lowerLevelMax);
		bound(m_lowerLevelMin);
	}
}

// TODO:  We could split this into two functions so that for each case, we
// only have to check one thing
void StateProfile::bound(double& val)
{
	// JLB: max saturation limit for computation is STATE_COND_TRUE
	//      min saturation limit is zero
	static const double STATE_COND_TRUE = 10.0;
	if(val > STATE_COND_TRUE)
		val -= STATE_COND_TRUE;
	else if (val <= -STATE_COND_TRUE)
		val += STATE_COND_TRUE;
}

