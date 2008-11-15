
#include <sstream>
#include "Debug.hh"
#include "PSEngine.hh"

using namespace EUROPA;


std::string toString(const PSList<std::string>& violations)
{
    std::ostringstream os;
    for (int i=0;i<violations.size();i++)
        os <<  (const std::string&)violations.get(i) << std::endl;

    return os.str();
}

void setStart(PSEngine& psengine,
              PSVariable* s1,PSVariable* s2,PSVariable* s3,
              int v1, int v2, int v3)
{
    PSVarValue vv1 = PSVarValue::getInstance(v1);
    PSVarValue vv2 = PSVarValue::getInstance(v2);
    PSVarValue vv3 = PSVarValue::getInstance(v3);

    s1->specifyValue(vv1);
    s2->specifyValue(vv2);
    s2->specifyValue(vv3);
    std::ostringstream os;
    os << "Set start to {"<<v1<<","<<v2<<","<<v3<<"}";
    debugMsg("testViolations",os.str());
    debugMsg("testViolations",psengine.getViolation());
    debugMsg("testViolations",toString(psengine.getViolationExpl()));
}

void testViolations(PSEngine& psengine)
{
	PSObject* act_obj[3];
	PSToken* act[3];
    PSVariable* s[3];
	for (int i=0;i<3;i++) {
	    act_obj[i] = psengine.getObjectsByType("Activity").get(i);
	    act[i] = act_obj[i]->getTokens().get(0);
	    s[i] = act[i]->getParameter("start");
	}

    debugMsg("testViolations",psengine.getViolation());
    debugMsg("testViolations",toString(psengine.getViolationExpl()));

	// Cause Violation
	setStart(psengine,s[0],s[1],s[2],5,8,16);
    setStart(psengine,s[0],s[1],s[2],5,11,20);
    setStart(psengine,s[0],s[1],s[2],5,11,3);

	// Remove Violation
    setStart(psengine,s[0],s[1],s[2],5,10,16);
}

