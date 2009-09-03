#ifndef _H_ExampleStateResourceCustomCode
#define _H_ExampleStateResourceCustomCode

#include "ResourceDefs.hh"
#include "TimetableProfile.hh"

using namespace EUROPA;

/* Implementation of a resource extension for handling a state resource (with only on/off states).  For
 * some large X (10 for the purposes of illustration, but should be larger in practice)
 * - When the state is turned ON, X units are added
 * - When the state is turned OFF, X units are used up
 * - When something required the state to be ON, 1 unit is used up
 *
 * By duplicating the behavior of TimetableProfile but adding the 'bound' method that basically implements
 * modulo arithmetic, we get the desired behavior by limiting the profile to always be in (-X,X].  As long
 * as there are never more than X activities requiring the state to be ON at a time, the level will:
 * - always be in [0,X] when the state in ON
 * - always be in (-X,0) if the state is OFF and there is any activity requiring it to be ON
 *   (this is how flaws/violations are recognized!)
 *
 * Note:  Paul Morris came up with the following formula that is equivalent to our 'bound' function:
 *        x = (x-1)mod(X) + 1
 *
 * WARNING:  For this to work properly, the X used here MUST match the X used in the nddl tokens
 *           (ie turning ON must produce X, and turning off must consume X)
 */


class StateProfile : public TimetableProfile {
public:
	StateProfile(const PlanDatabaseId db, const FVDetectorId flawDetector,
			const double initCapacityLb = 0, const double initCapacityUb = 0);

protected:

	// Slight variants on what is done in TimetableProfile:
	void handleTransactionStart(bool isConsumer, const double & lb, const double & ub);
	void handleTransactionEnd(bool isConsumer, const double & lb, const double & ub);

private:
	// Do the modulo arithmetic so this keeps bounds in (-STATE_COND_TRUE,STATE_COND_TRUE]
	void bound(double& val);
};

#endif
