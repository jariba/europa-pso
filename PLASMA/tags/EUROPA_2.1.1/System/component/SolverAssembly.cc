#include "SolverAssembly.hh"

// Support for required major plan database components
#include "PlanDatabase.hh"
#include "PlanDatabaseWriter.hh"
#include "ConstraintEngine.hh"
#include "RulesEngine.hh"

// Misc
#include "Utils.hh"

// Solver Support
#include "Solver.hh"
#include "SolverPartialPlanWriter.hh"
#include "Filters.hh"

// Test Support
#include "TestSupport.hh"

#include <string>

namespace EUROPA {

  const char* SolverAssembly::TX_LOG() {
    static const char* sl_txLog = "TransactionLog.xml";
    return sl_txLog;
  }

  SolverAssembly::SolverAssembly(const SchemaId& schema) : StandardAssembly(schema) 
  {
  }

  SolverAssembly::~SolverAssembly() 
  {	  
  }

  bool SolverAssembly::plan(const char* txSource, const char* config, bool interp){
    check_error(config != NULL, "Must have a planner config argument.");
    TiXmlDocument doc(config);
    doc.LoadFile();
    return plan(txSource, *(doc.RootElement()), interp);
  }

  bool SolverAssembly::plan(const char* txSource, const TiXmlElement& config, bool interp){
    SOLVERS::SolverId solver = (new SOLVERS::Solver(m_planDatabase, config))->getId();

    SOLVERS::PlanWriter::PartialPlanWriter* ppw = 
      new SOLVERS::PlanWriter::PartialPlanWriter(m_planDatabase, m_constraintEngine, m_rulesEngine, solver);

    // Now process the transactions
    if(!playTransactions(txSource, interp))
      return false;

    debugMsg("SolverAssembly:plan", "Initial state: " << std::endl << PlanDatabaseWriter::toString(m_planDatabase));

    // Configure the planner from data in the initial state
    std::list<ObjectId> configObjects;
    m_planDatabase->getObjectsByType("PlannerConfig", configObjects); // Standard configuration class

    check_error(configObjects.size() == 1,
		"Expect exactly one instance of the class 'PlannerConfig'");

    ObjectId configSource = configObjects.front();
    check_error(configSource.isValid());

    const std::vector<ConstrainedVariableId>& variables = configSource->getVariables();
    checkError(variables.size() == 4,
	       "Expecting exactly 4 configuration variables.  Got " << variables.size());

    // Set up the horizon  from the model now. Will cause a refresh of the query, but that is OK.
    ConstrainedVariableId horizonStart = variables[0];
    ConstrainedVariableId horizonEnd = variables[1];
    ConstrainedVariableId plannerSteps = variables[2];
    ConstrainedVariableId maxDepth = variables[3];

    int start = (int) horizonStart->baseDomain().getSingletonValue();
    int end = (int) horizonEnd->baseDomain().getSingletonValue();
    SOLVERS::HorizonFilter::getHorizon() = IntervalDomain(start, end);

    // Now get planner step max
    int steps = (int) plannerSteps->baseDomain().getSingletonValue();
    int depth = (int) maxDepth->baseDomain().getSingletonValue();

    bool retval = solver->solve(steps, depth);
    
    m_totalNodes = solver->getStepCount();
    m_finalDepth = solver->getDepth();
    
    delete ppw;
    delete (SOLVERS::Solver*) solver;

    return retval;
  }

  const unsigned int SolverAssembly::getTotalNodesSearched() const { return m_totalNodes; }

  const unsigned int SolverAssembly::getDepthReached() const { return m_finalDepth; }
}
