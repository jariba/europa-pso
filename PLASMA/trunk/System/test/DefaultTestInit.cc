#include "PlanDatabaseDefs.hh"
#include "ConstraintEngineDefs.hh"
#include "RulesEngineDefs.hh"
#include "CBPlannerDefs.hh"

using namespace EUROPA;

void averInit(const PlanDatabaseId& db, 
              const DecisionManagerId& dm = DecisionManagerId::noId(),
              const ConstraintEngineId& ce = ConstraintEngineId::noId(),
              const RulesEngineId& re = RulesEngineId::noId()) {
}

void averDeinit(){
}
