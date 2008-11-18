
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

    psengine.setAutoPropagation(false);
    s1->specifyValue(vv1);
    s2->specifyValue(vv2);
    s3->specifyValue(vv3);
    psengine.setAutoPropagation(false);
    std::ostringstream os;
    os << "Set start to {"<<v1<<","<<v2<<","<<v3<<"}";
    debugMsg("testViolations",os.str());
    debugMsg("testViolations",psengine.getViolation());
    debugMsg("testViolations",toString(psengine.getViolationExpl()));
}

void setStart(PSEngine& psengine,
              PSToken* toks[],
              int v1,int v2,int v3)
{
    int svals[3] = {v1,v2,v3};
    int durations[3];

    for (int i=0;i<3;i++) {
        PSVariable* dv = toks[i]->getParameter("duration");
        //debugMsg("testViolations", "duration" <<i << ":"<<dv->toLongString());
        durations[i] = dv->getSingletonValue().asInt();
    }

    std::ostringstream os;
    os << "Set start to {";
    psengine.setAutoPropagation(false);
    for (int i=0;i<3;i++) {
        PSVariable* sv = toks[i]->getParameter("start");
        PSVariable* ev = toks[i]->getParameter("end");
        PSVarValue svl = PSVarValue::getInstance(svals[i]);
        PSVarValue evl = PSVarValue::getInstance(svals[i]+durations[i]);
        sv->specifyValue(svl);
        ev->specifyValue(evl);
        if (i>0)
            os << ",";
        os << svals[i];
    }
    os << "}";
    psengine.setAutoPropagation(true);
    debugMsg("testViolations",os.str());
    debugMsg("testViolations",psengine.getViolation());
    debugMsg("testViolations",toString(psengine.getViolationExpl()));
}


void testViolations(PSEngine& psengine)
{
	PSObject* act_obj[3];
	PSToken* act[3];
    PSVariable* s[3];
    PSList<PSObject*> acts = psengine.getObjectsByType("Activity");
	for (int i=0;i<3;i++) {
	    act_obj[i] = acts.get(i);
	    act[i] = act_obj[i]->getTokens().get(0);
	    s[i] = act[i]->getParameter("start");
	    debugMsg("testViolations","act" << (i+1) << ":" << act[i]->toLongString());
	}

    debugMsg("testViolations",psengine.getViolation());
    debugMsg("testViolations",toString(psengine.getViolationExpl()));

	// Cause Violation
	setStart(psengine,act,5,8,16);
    setStart(psengine,act,6,11,20);
    setStart(psengine,act,5,10,3);

	// Remove Violation
    setStart(psengine,act,5,10,16);
}

