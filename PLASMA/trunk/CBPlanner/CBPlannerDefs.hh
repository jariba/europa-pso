#ifndef _H_CBPlannerDefs
#define _H_CBPlannerDefs

#include "CommonDefs.hh"
#include "PlanDatabaseDefs.hh"
#include <cmath>

namespace PLASMA {

  class Condition;
  typedef Id<Condition> ConditionId;

  class Horizon;
  typedef Id<Horizon> HorizonId;

  class HorizonCondition;
  typedef Id<HorizonCondition> HorizonConditionId;

  class TemporalVariableCondition;
  typedef Id<TemporalVariableCondition> TemporalVariableConditionId;

  class DynamicInfiniteRealCondition;
  typedef Id<DynamicInfiniteRealCondition> DynamicInfiniteRealConditionId;

  class DecisionManager;
  typedef Id<DecisionManager> DecisionManagerId;

  class Choice;
  typedef Id<Choice> ChoiceId;

  class DecisionPoint;
  typedef Id<DecisionPoint> DecisionPointId;

  class TokenDecisionPoint;
  typedef Id<TokenDecisionPoint> TokenDecisionPointId;

  class ObjectDecisionPoint;
  typedef Id<ObjectDecisionPoint> ObjectDecisionPointId;

  class ConstrainedVariableDecisionPoint;
  typedef Id<ConstrainedVariableDecisionPoint> ConstrainedVariableDecisionPointId;

  class CBPlanner;
  typedef Id<CBPlanner> CBPlannerId;

  class DecisionManagerListener;
  typedef Id<DecisionManagerListener> DecisionManagerListenerId;

  class OpenDecisionManager;
  typedef Id<OpenDecisionManager> OpenDecisionManagerId;


  class ResourceOpenDecisionManager;
  typedef Id<ResourceOpenDecisionManager> ResourceOpenDecisionManagerId;

  class ResourceFlawDecisionPoint;
  typedef Id<ResourceFlawDecisionPoint> ResourceFlawDecisionPointId;

  class ResourceFlawChoice;
  typedef Id<ResourceFlawChoice> ResourceFlawChoiceId;

  class DMResourceListener;
  typedef Id<DMResourceListener> DMResourceListenerId;

  class HSTSHeuristics;
  typedef Id<HSTSHeuristics> HSTSHeuristicsId;

  class HSTSNoBranch;
  typedef Id<HSTSNoBranch> HSTSNoBranchId;
}

#endif
