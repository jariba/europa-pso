#include "CBPlannerDefs.hh"
#include "CBPlanner.hh"
#include "DecisionManager.hh"
#include "SubgoalOnceRule.hh"
#include "CBPlannerModuleTests.hh"
#include "DMLogger.hh"
#include "Condition.hh"
#include "Horizon.hh"
#include "HorizonCondition.hh"
#include "TemporalVariableCondition.hh"
#include "DynamicInfiniteRealCondition.hh"
#include "TokenDecisionPoint.hh"
#include "TestSupport.hh"
#include "IntervalIntDomain.hh"
#include "IntervalDomain.hh"
#include "DefaultPropagator.hh"
#include "EqualityConstraintPropagator.hh"
#include "Constraint.hh"
#include "CeLogger.hh"
#include "Utils.hh"

#include "PlanDatabaseDefs.hh"
#include "PlanDatabase.hh"
#include "Schema.hh"
#include "Object.hh"
#include "EventToken.hh"
#include "TokenVariable.hh"
#include "ObjectTokenRelation.hh"
#include "Timeline.hh"
#include "DbLogger.hh"

#include "RulesEngine.hh"
#include "Rule.hh"

#include <iostream>
#include <string>

#define DEFAULT_SETUP(ce, db, schema, autoClose) \
    ConstraintEngine ce; \
    Schema schema; \
    PlanDatabase db(ce.getId(), schema.getId()); \
    new DefaultPropagator(LabelStr("Default"), ce.getId()); \
    RulesEngine re(db.getId()); \
    Horizon hor(0,200); \
    DecisionManager dm(db.getId()); \
    Id<DbLogger> dbLId; \
    if (loggingEnabled()) { \
      new CeLogger(std::cout, ce.getId()); \
      dbLId = (new DbLogger(std::cout, db.getId()))->getId(); \
      new DMLogger(std::cout, dm.getId()); \
    } \
    if (autoClose) \
      db.close();

#define DEFAULT_TEARDOWN() \
    delete (DbLogger*) dbLId;


#define DEFAULT_SETUP_PLAN(ce, db, schema, autoClose) \
    ConstraintEngine ce; \
    Schema schema; \
    PlanDatabase db(ce.getId(), schema.getId()); \
    new DefaultPropagator(LabelStr("Default"), ce.getId()); \
    RulesEngine re(db.getId()); \
    Horizon hor(0, 200); \
    CBPlanner planner(db.getId(), hor.getId()); \
    Id<DbLogger> dbLId; \
    if (loggingEnabled()) { \
      new CeLogger(std::cout, ce.getId()); \
      dbLId = (new DbLogger(std::cout, db.getId()))->getId(); \
      new DMLogger(std::cout, planner.getDecisionManager()); \
    } \
    if (autoClose) \
      db.close();

#define DEFAULT_TEARDOWN_PLAN() \
    delete (DbLogger*) dbLId;

class DefaultSetupTest {
public:
  static bool test() {
    runTest(testDefaultSetup);
    return(true);
  }

private:
  static bool testDefaultSetup() {
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testDefaultSetupImpl(ce, db, schema, dm, hor);
    DEFAULT_TEARDOWN();
    return retval;
  }
};

class ConditionTest {
public:
  static bool test() {
    runTest(testCondition);
    runTest(testHorizon);
    runTest(testHorizonCondition);
    runTest(testTemporalVariableCondition);
    runTest(testDynamicInfiniteRealCondition);
    return(true);
  }
private:
  static bool testCondition(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testConditionImpl(ce, db, schema, dm, hor);
    DEFAULT_TEARDOWN();
    return retval;
  }

  static bool testHorizon() {
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, true);
    retval = testHorizonImpl(ce, db, schema, dm, hor);
    DEFAULT_TEARDOWN();
    return retval;
  }

  static bool testHorizonCondition() {
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testHorizonConditionImpl(ce, db, schema, dm, hor);
    DEFAULT_TEARDOWN();
    return retval;
  }

  static bool testTemporalVariableCondition() {
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testTemporalVariableConditionImpl(ce, db, schema, dm, hor);
    DEFAULT_TEARDOWN();
    return retval;
  }

  static bool testDynamicInfiniteRealCondition() {
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testDynamicInfiniteRealConditionImpl(ce, db, schema, dm, hor);
    DEFAULT_TEARDOWN();
    return retval;
  }

};

class DecisionManagerTest {
public:
  static bool test() {
    runTest(testForwardDecisionHandling);
    return(true);
  }

private:
  static bool testForwardDecisionHandling() {
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testForwardDecisionHandlingImpl(ce, db, schema, dm, hor);
    DEFAULT_TEARDOWN();
    return retval;
  }
};

class CBPlannerTest {
public:
  static bool test() {
    runTest(testMakeMove);
    runTest(testCurrentState);
    runTest(testRetractMove);
    runTest(testNoBacktrackCase);
    runTest(testSubgoalOnceRule);
    runTest(testBacktrackCase);
    runTest(testTimeoutCase);
    return true;
  }
private:
  static bool testMakeMove() {
    bool retval = false;
    DEFAULT_SETUP_PLAN(ce, db, schema, false);
    retval = testMakeMoveImpl(ce, db, schema, planner);
    DEFAULT_TEARDOWN_PLAN();
    return retval;
  }

  static bool testCurrentState() {
    bool retval = false;
    DEFAULT_SETUP_PLAN(ce, db, schema, false);
    retval = testCurrentStateImpl(ce, db, schema, planner);
    DEFAULT_TEARDOWN_PLAN();
    return retval;
  }

  static bool testRetractMove() {
    bool retval = false;
    DEFAULT_SETUP_PLAN(ce, db, schema, false);
    retval = testRetractMoveImpl(ce, db, schema, planner);
    DEFAULT_TEARDOWN_PLAN();
    return retval;
  }

  static bool testNoBacktrackCase() {
    bool retval = false;
    DEFAULT_SETUP_PLAN(ce, db, schema, false);
    retval = testNoBacktrackCaseImpl(ce, db, schema, planner);
    DEFAULT_TEARDOWN_PLAN();
    return retval;
  }

  static bool testSubgoalOnceRule() {
    bool retval = false;
    DEFAULT_SETUP_PLAN(ce, db, schema, false);
    retval = testSubgoalOnceRuleImpl(ce, db, schema, planner);
    DEFAULT_TEARDOWN_PLAN();
    return retval;
  }

  static bool testBacktrackCase() {
    bool retval = false;
    DEFAULT_SETUP_PLAN(ce, db, schema, false);
    retval = testBacktrackCaseImpl(ce, db, schema, planner);
    DEFAULT_TEARDOWN_PLAN();
    return retval;
  }

  static bool testTimeoutCase() {
    bool retval = false;
    DEFAULT_SETUP_PLAN(ce, db, schema, false);
    retval = testTimeoutCaseImpl(ce, db, schema, planner);
    DEFAULT_TEARDOWN_PLAN();
    return retval;
  }
};    

class MultipleDecisionManagerTest {
public:
  static bool test() {
    runTest(testMultipleDMs);
    return(true);
  }
private:
  static bool testMultipleDMs() {
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testMultipleDMsImpl(ce, db, schema, dm, hor);
    DEFAULT_TEARDOWN();
    return retval;
  }
};

int main() {
  REGISTER_NARY(EqualConstraint, "concurrent", "Default");
  REGISTER_NARY(LessThanEqualConstraint, "before", "Default");
  REGISTER_NARY(AddEqualConstraint, "StartEndDurationRelation", "Default");
  REGISTER_NARY(ObjectTokenRelation, "ObjectTokenRelation", "Default");
  REGISTER_NARY(EqualConstraint, "eq", "Default");
  REGISTER_NARY(EqualConstraint, "Equal", "Default");
  REGISTER_NARY(LessThanConstraint, "lt", "Default");
  REGISTER_UNARY(SubsetOfConstraint, "Singleton", "Default");
  REGISTER_UNARY(SubsetOfConstraint, "SubsetOf", "Default");

  runTestSuite(DefaultSetupTest::test);
  runTestSuite(ConditionTest::test);
  runTestSuite(DecisionManagerTest::test);
  runTestSuite(CBPlannerTest::test);
  runTestSuite(MultipleDecisionManagerTest::test);
  std::cout << "Finished" << std::endl;
  ConstraintLibrary::purgeAll();
}
