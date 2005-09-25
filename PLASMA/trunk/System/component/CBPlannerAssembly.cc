#include "CBPlannerAssembly.hh"

// Support for required major plan database components
#include "PlanDatabase.hh"
#include "PlanDatabaseWriter.hh"
#include "ConstraintEngine.hh"
#include "RulesEngine.hh"

// Transactions
#include "DbClientTransactionPlayer.hh"

#include "NddlDefs.hh"

// Misc
#include "Utils.hh"
#include "Debug.hh"

// Planner Support
#include "CBPlanner.hh"
#include "PartialPlanWriter.hh"
#include "Horizon.hh"
#include "DecisionManager.hh"
#include "OpenDecisionManager.hh"
#include "HeuristicsEngine.hh"
#include "HeuristicsReader.hh"
#include "MasterMustBeInserted.hh"

// Test Support
#include "PLASMAPerformanceConstraint.hh"
#include "LoraxConstraints.hh"
#include "TestSupport.hh"

#include <string>

#include "AverInterp.hh"

#define PPW_WITH_PLANNER

namespace EUROPA {

  const char* CBPlannerAssembly::TX_LOG() {
    static const char* sl_txLog = "TransactionLog.xml";
    return sl_txLog;
  }

  CBPlannerAssembly::CBPlannerAssembly(const SchemaId& schema) : StandardAssembly(schema) { }

  CBPlannerAssembly::~CBPlannerAssembly() {}

  /**
   * @brief Sets up the necessary constraint factories
   */
  void CBPlannerAssembly::initialize() {
    StandardAssembly::initialize();

    REGISTER_CONSTRAINT(PLASMAPerformanceConstraint, "perf", "Default");

    // LoraxConstraints for some of the resources tests.
    REGISTER_CONSTRAINT(SquareOfDifferenceConstraint, "diffSquare", "Default");
    REGISTER_CONSTRAINT(DistanceFromSquaresConstraint, "distanceSquares", "Default");
    REGISTER_CONSTRAINT(DriveBatteryConstraint, "driveBattery", "Default");
    REGISTER_CONSTRAINT(DriveDurationConstraint, "driveDuration", "Default");
    REGISTER_CONSTRAINT(WindPowerConstraint, "windPower", "Default");
    REGISTER_CONSTRAINT(SampleBatteryConstraint, "sampleBattery", "Default");
    REGISTER_CONSTRAINT(SampleDurationConstraint, "sampleDuration", "Default");

    isInitialized() = true;
  }

  bool CBPlannerAssembly::plan(const char* txSource, const TiXmlElement&,  
			       const char* heuristics, const char* averFile){
    Horizon horizon;
    HeuristicsEngine he(m_planDatabase);
    HeuristicsReader reader(he.getId());
    reader.read(heuristics);

    OpenDecisionManager odm(m_planDatabase, he.getId());
    CBPlanner planner(m_planDatabase, horizon.getId(), odm.getId());

#ifdef PPW_WITH_PLANNER
    PlanWriter::PartialPlanWriter ppw(m_planDatabase, m_constraintEngine, m_rulesEngine, planner.getId());
#else
    PlanWriter::PartialPlanWriter ppw(m_planDatabase, m_constraintEngine, m_rulesEngine);
#endif

    if(averFile != NULL) {
      AverInterp::init(std::string(averFile), planner.getDecisionManager(), 
                       m_planDatabase->getConstraintEngine(), m_planDatabase, m_rulesEngine);
    }

    // Now process the transactions
    if(!playTransactions(txSource))
      return false;

    // Configure the planner from data in the initial state
    std::list<ObjectId> configObjects;
    m_planDatabase->getObjectsByType("PlannerConfig", configObjects); // Standard configuration class

    check_error(configObjects.size() == 1,
		"Expect exactly one instance of the class 'PlannerConfig'");

    ObjectId configSource = configObjects.front();
    check_error(configSource.isValid());

    const std::vector<ConstrainedVariableId>& variables = configSource->getVariables();
    check_error(variables.size() == 4, "Expecting exactly 4 configuration variables");

    // Set up the horizon  from the model now. Will cause a refresh of the query, but that is OK.
    ConstrainedVariableId horizonStart = variables[0];
    ConstrainedVariableId horizonEnd = variables[1];
    ConstrainedVariableId plannerSteps = variables[2];
    ConstrainedVariableId plannerDepth = variables[3];
    bool expectLimits = false;

    if(configSource->getType() == LabelStr("PlannerTestConfig"))
      expectLimits = true;

    int start = (int) horizonStart->baseDomain().getSingletonValue();
    int end = (int) horizonEnd->baseDomain().getSingletonValue();
    horizon.setHorizon(start, end);

    // Now get planner step max
    unsigned int steps = (unsigned int) plannerSteps->baseDomain().getSingletonValue();
    unsigned int depth = (unsigned int) plannerDepth->baseDomain().getSingletonValue();

    // Add the MasterMustBeInserted condition
    MasterMustBeInserted condition1(planner.getDecisionManager());

    CBPlanner::Status retval = planner.run(steps+1);
    
    m_totalNodes = planner.getTime();
    m_finalDepth = planner.getDepth();

    if(averFile != NULL)
      AverInterp::terminate();

    debugMsg("Main:plan", "Result:" << retval << " Depth:" << m_finalDepth << " Steps:" << m_totalNodes);

    assertTrue(!expectLimits || (steps == m_totalNodes && depth == m_finalDepth),
	       "Result:" + EUROPA::toString(retval) + 
	       " Expected Depth:" + EUROPA::toString(depth) + 
	       " Expected Steps:" + EUROPA::toString(steps) + 
	       " Actual Depth:" + EUROPA::toString(m_finalDepth) + 
	       " Actual Steps:" + EUROPA::toString(m_totalNodes));

    if(retval == CBPlanner::PLAN_FOUND)
      return true;
    else
      return false;
  }

  const PlanDatabaseId& CBPlannerAssembly::getPlanDatabase() const {
    return m_planDatabase;
  }

  const unsigned int CBPlannerAssembly::getTotalNodesSearched() const { return m_totalNodes; }

  const unsigned int CBPlannerAssembly::getDepthReached() const { return m_finalDepth; }

}
