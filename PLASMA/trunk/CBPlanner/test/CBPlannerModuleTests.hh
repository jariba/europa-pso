#ifndef _H_CBPlannerModuleTests
#define _H_CBPlannerModuleTests

#include "ConstraintEngine.hh"
#include "ConstraintEngineDefs.hh"
#include "PlanDatabase.hh"
#include "PlanDatabaseDefs.hh"
#include "Schema.hh"
#include "Horizon.hh"
#include "DecisionManager.hh"
#include "CBPlanner.hh"
#include "Timeline.hh"
#include "IntervalToken.hh"
#include "IntervalDomain.hh"
#include "IntervalIntDomain.hh"
#include "TemporalVariableCondition.hh"
#include "DynamicInfiniteRealCondition.hh"
#include "TokenDecisionPoint.hh"
#include "HSTSHeuristics.hh"

namespace Prototype {

bool testDefaultSetupImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema, 
                          DecisionManager &dm, Horizon &hor);
bool testConditionImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema,
                       DecisionManager &dm, Horizon &hor);
bool testHorizonImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema,
                                DecisionManager &dm, Horizon &hor);
bool testHorizonConditionImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema,
                                         DecisionManager &dm, Horizon &hor);
bool testTemporalVariableConditionImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema,
                                       DecisionManager &dm, Horizon &hor);
bool testDynamicInfiniteRealConditionImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema,
                                          DecisionManager &dm, Horizon &hor);
bool testForwardDecisionHandlingImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema,
                                     DecisionManager &dm, Horizon &hor);
bool testMakeMoveImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema,
                      CBPlanner &planner);
bool testCurrentStateImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema,
                          CBPlanner &planner);
bool testRetractMoveImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema,
                         CBPlanner &planner);
bool testNoBacktrackCaseImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema,
                             CBPlanner &planner);
bool testSubgoalOnceRuleImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema, 
                             CBPlanner &planner);
bool testBacktrackCaseImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema, 
                           CBPlanner &planner);
bool testTimeoutCaseImpl(ConstraintEngine& ce, PlanDatabase& db, Schema& schema,  
		  CBPlanner& planner) ;
bool testMultipleDMsImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema, 
                         DecisionManager &dm, Horizon &hor);

  bool testVariableDecisionCycleImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema, CBPlanner& planner);

  bool testTokenDecisionCycleImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema, Horizon& hor, CBPlanner& planner);

  bool testObjectDecisionCycleImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema, Horizon& hor, CBPlanner& planner);

  bool testObjectAndObjectVariableImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema, Horizon& hor, CBPlanner& planner);

  bool testObjectHorizonImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema, Horizon& hor, CBPlanner& planner);

  bool testFindAnotherPlanImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema, Horizon& hor, CBPlanner& planner);

  bool testAddSubgoalAfterPlanningImpl(ConstraintEngine &ce, PlanDatabase &db, Schema &schema, Horizon& hor, CBPlanner& planner);

  bool testDefaultInitializationImpl(HSTSHeuristics& heuristics);

  bool testTokenInitializationImpl(HSTSHeuristics& heuristics);

  bool testVariableInitializationImpl(HSTSHeuristics& heuristics);

  bool testReaderImpl(HSTSHeuristics& heuristics);
}
#endif
