#ifndef H_SolverDefs
#define H_SolverDefs

#include "PlanDatabaseDefs.hh"
#include "Entity.hh"

#include <vector>

using namespace EUROPA;

namespace EUROPA {
  namespace SOLVERS {

    class Component;
    typedef Id<Component> ComponentId;

    class MatchingEngine;
    typedef Id<MatchingEngine> MatchingEngineId;

    class MatchingRule;
    typedef Id<MatchingRule> MatchingRuleId;

    class FlawFilter;
    typedef Id<FlawFilter> FlawFilterId;

    class FlawHandler;
    typedef Id<FlawHandler> FlawHandlerId;

    class FlawManager;
    typedef Id<FlawManager> FlawManagerId;

    typedef std::list<FlawManagerId> FlawManagers;

    class DecisionPoint;
    typedef Id<DecisionPoint> DecisionPointId;

    class Solver;
    typedef Id<Solver> SolverId;

    typedef std::vector<DecisionPointId> DecisionStack;

    typedef double Priority; /*!< Used to reference to the priority used in calculating heuristics. */

    /**
     * @brief Used to store guard entry data - var name and expected value to match on.
     */
    typedef std::pair<LabelStr, double> GuardEntry;

    /**
     * @brief Defines a configuration class
     */
    class SolverConfig{
    public:
      SolverConfig();
    };

    namespace PlanWriter {
      class PartialPlanWriter;
    }
  }
}
#endif
