// For performance tests only
#include "PrototypePerformanceConstraint.hh"

// Include prototypes required to integrate to the NDDL generated model
#include "Nddl.hh"
#include "SamplePlanDatabase.hh"

// Support for planner
#include "CBPlanner.hh"
#include "DecisionPoint.hh"
#include "EUROPAHeuristicStrategy.hh"

#include "TemporalPropagator.hh"
#include "STNTemporalAdvisor.hh"
#include "PlanDatabaseWriter.hh"

#include <fstream>

SchemaId schema;

#define REPLAY_DECISIONS

const char* TX_LOG = "TransactionLog.xml";
const char* TX_REPLAY_LOG = "ReplayedTransactions.xml";
bool replay = true;

bool runPlanner(){
  std::stringstream os1;
  {
    SamplePlanDatabase db1(schema, true);

  // Initialize the plan database
    NDDL::initialize(db1.planDatabase);

    // Set up the horizon  from the model now. Will cause a refresh of the query, but that is OK.
    std::list<ObjectId> objects;
    db1.planDatabase->getObjectsByType(LabelStr("World"), objects);
    ObjectId world = objects.front();
    check_error(objects.size() == 1);
    ConstrainedVariableId horizonStart = world->getVariable(LabelStr("world.m_horizonStart"));
    check_error(horizonStart.isValid());
    ConstrainedVariableId horizonEnd = world->getVariable(LabelStr("world.m_horizonEnd"));
    check_error(horizonEnd.isValid());
    int start = (int) horizonStart->baseDomain().getSingletonValue();
    int end = (int) horizonEnd->baseDomain().getSingletonValue();
    db1.horizon->setHorizon(start, end);

    // Create and run the planner
    ConstrainedVariableId maxPlannerSteps = world->getVariable(LabelStr("world.m_maxPlannerSteps"));
    check_error(maxPlannerSteps.isValid());
    int steps = (int) maxPlannerSteps->baseDomain().getSingletonValue();
    CBPlanner planner(db1.planDatabase->getClient(), db1.flawSource, steps);
    EUROPAHeuristicStrategy strategy;
      
    int res = planner.run(strategy.getId(), loggingEnabled());

    std::cout << "\nNr. Of Decisions:" << planner.getClosedDecisions().size() << std::endl;

    db1.planDatabase->getClient()->toStream(os1);
    PlanDatabaseWriter::write(db1.planDatabase, std::cout);

    assert(res == 1);

    // Store transactions for recreation of database
    {
      std::ofstream out(TX_LOG);
      db1.txLog->flush(out);
      out.close();
    }
  }

  std::stringstream os2;
  {
    SamplePlanDatabase db(schema, true);
    DbClientTransactionPlayer player(db.planDatabase->getClient());
    std::ifstream in(TX_LOG);
    player.play(in);
    db.planDatabase->getClient()->toStream(os2);
  }

  std::string s1 = os1.str();
  std::string s2 = os2.str();
  assert(s1 == s2);

  return true;
}

bool copyFromFile(){
  // Populate plan database from transaction log
  std::stringstream os1;
  {
    SamplePlanDatabase db(schema, true);
    DbClientTransactionPlayer player(db.planDatabase->getClient());
    std::ifstream in(TX_LOG);
    player.play(in);
    db.planDatabase->getClient()->toStream(os1);
  }
  std::stringstream os2;
  {
    SamplePlanDatabase db(schema, true);
    DbClientTransactionPlayer player(db.planDatabase->getClient());
    std::ifstream in(TX_LOG);
    player.play(in);
    db.planDatabase->getClient()->toStream(os2);
  }

  std::string s1 = os1.str();
  std::string s2 = os2.str();
  assert(s1 == s2);

  return true;
}

int main(int argc, const char ** argv){
  // Initialize constraint factories
  SamplePlanDatabase::initialize();
  schema = NDDL::schema();

  runTest(runPlanner);
  runTest(copyFromFile);

  SamplePlanDatabase::terminate();

  std::cout << "Finished" << std::endl;
}
