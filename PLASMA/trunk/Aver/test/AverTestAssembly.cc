#include "AverTestAssembly.hh"

// Support for required major plan database components
#include "PlanDatabase.hh"
#include "PlanDatabaseWriter.hh"
#include "ConstraintEngine.hh"
#include "RulesEngine.hh"
#include "DefaultPropagator.hh"

#include "Resource.hh"
#include "ResourceConstraint.hh"
#include "ResourceDefs.hh"
#include "ResourcePropagator.hh"
#include "Transaction.hh"

// Transactions
#include "DbClientTransactionPlayer.hh"
#include "BoolTypeFactory.hh"
#include "StringTypeFactory.hh"
#include "floatType.hh"
#include "intType.hh"

// Support for Temporal Network
#include "TemporalPropagator.hh"
#include "STNTemporalAdvisor.hh"
#include "TemporalConstraints.hh"

// Support for registered constraints
#include "ConstraintLibrary.hh"
#include "Constraints.hh"
#include "EqualityConstraintPropagator.hh"
#include "ObjectTokenRelation.hh"
#include "CommonAncestorConstraint.hh"
#include "HasAncestorConstraint.hh"

#include "NddlDefs.hh"

// For cleanup purging
#include "TokenFactory.hh"
#include "ObjectFactory.hh"
#include "Rule.hh"

// Misc
#include "Utils.hh"

// Planner Support
#include "CBPlannerDefs.hh"
#include "CBPlanner.hh"
#include "PartialPlanWriter.hh"
#include "Horizon.hh"
#include "DecisionManager.hh"
#include "ResourceOpenDecisionManager.hh"

// Test Support
//#include "PLASMAPerformanceConstraint.hh"
//#include "LoraxConstraints.hh"
#include "TestSupport.hh"

#include "AverInterp.hh"

#include <string>
#include <fstream>

#define PPW_WITH_PLANNER

const char* TX_LOG = "TransactionLog.xml";

namespace EUROPA {

  AverTestAssembly::AverTestAssembly(const SchemaId& schema, const char* averFile) : StandardAssembly(schema) { 
    m_planner = (new CBPlanner(m_planDatabase, m_horizon.getId()))->getId();
    AverInterp::init(averFile, m_planner->getDecisionManager(), m_constraintEngine, m_planDatabase, m_rulesEngine);
  }

  AverTestAssembly::~AverTestAssembly() {
    AverInterp::terminate();
    delete (CBPlanner*) m_planner;
  }

  /**
   * @brief Sets up the necessary constraint factories
   */
  void AverTestAssembly::initialize() {
    StandardAssembly::initialize();
    isInitialized() = true;
  }

  CBPlanner::Status AverTestAssembly::plan(const char* txSource, const char* averFile){
    return CBPlanner::PLAN_FOUND;
  }

  void AverTestAssembly::replay(const DbClientTransactionLogId& txLog) {
  }

  const PlanDatabaseId& AverTestAssembly::getPlanDatabase() const {
    return m_planDatabase;
  }
  
  const RulesEngineId& AverTestAssembly::getRulesEngine() const {
    return m_rulesEngine;
  }
}
