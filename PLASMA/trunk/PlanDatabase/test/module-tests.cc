#include "PlanDatabase.hh"
#include "Schema.hh"
#include "Object.hh"
#include "EventToken.hh"
#include "TokenVariable.hh"
#include "ObjectTokenRelation.hh"
#include "Timeline.hh"
#include "DbLogger.hh"
#include "CommonAncestorConstraint.hh"
#include "HasAncestorConstraint.hh"
#include "DbClientTransactionLog.hh"

#include "DbClient.hh"
#include "ObjectFactory.hh"
#include "TokenFactory.hh"

#include "TestSupport.hh"
#include "Utils.hh"
#include "IntervalIntDomain.hh"
#include "Domain.hh"
#include "DefaultPropagator.hh"
#include "EqualityConstraintPropagator.hh"
#include "UnaryConstraint.hh"

#include "PlanDbModuleTests.hh"


#include <iostream>
#include <string>
  
/**
 * Test class for testing client and factory
 */

#define DEFAULT_SETUP(ce, db, schema, autoClose) \
    ConstraintEngineId ce = (new ConstraintEngine())->getId(); \
    SchemaId schema = (new Schema())->getId(); \
    PlanDatabaseId db = (new PlanDatabase(ce, schema))->getId(); \
    { DefaultPropagator* dp = new DefaultPropagator(LabelStr("Default"), ce); \
      assert(dp != 0); } \
    Id<DbLogger> dbLId; \
    if (loggingEnabled()) { \
      new CeLogger(std::cout, ce); \
      dbLId = (new DbLogger(std::cout, db))->getId(); \
    } \
    { EqualityConstraintPropagator* ecp = new EqualityConstraintPropagator(LabelStr("EquivalenceClass"), ce); \
      assert(ecp != 0); } \
    Object* objectPtr = new Object(db, LabelStr("AllObjects"), LabelStr("o1")); \
    assert(objectPtr != 0); \
    Object& object = *objectPtr; \
    assert(objectPtr->getId() == object.getId()); \
    if (autoClose) \
      db->close();\
    {

#define DEFAULT_TEARDOWN() \
    }\
    Entity::purgeStarted();\
    delete (PlanDatabase*) db;\
    delete (Schema*) schema;\
    delete (ConstraintEngine*) ce;\
    Entity::purgeEnded();



class ObjectTest {
public:
  
  static bool test() {
    runTest(testBasicAllocation);
    runTest(testObjectDomain);
    runTest(testObjectVariables);
    runTest(testObjectTokenRelation);
    runTest(testCommonAncestorConstraint);
    runTest(testHasAncestorConstraint);
    runTest(testMakeObjectVariable);
    runTest(testTokenObjectVariable);
    return(true);
  }
  
private:
  static bool testBasicAllocation() {
    return testBasicObjectAllocationImpl();
  }
  
  static bool testObjectDomain(){
    return testObjectDomainImpl();
  }
  
  static bool testObjectVariables(){
    return testObjectVariablesImpl();
  }
  
  
  static bool testObjectTokenRelation(){
    return testObjectObjectTokenRelationImpl();
  }
  
  static bool testCommonAncestorConstraint(){
    return testCommonAncestorConstraintImpl();
  }
  
  static bool testHasAncestorConstraint(){
    return testHasAncestorConstraintImpl();
  }
  
  /**
   * The most basic case for dynamic objects is that we can populate the variable correclty
   * and synchronize its values.
   */
  static bool testMakeObjectVariable(){
    return testMakeObjectVariableImpl();
  }
  
  /**
   * Have ate least one object in the system prior to creating a token. Then show how
   * removal triggers an inconsistency, and insertion of another object fixes it. Also
   * show that specifiying the object prevents propagation if we add another object, but
   * relaxing it will populate the object variable to include the new object.
   */
  static bool testTokenObjectVariable(){
    return testTokenObjectVariableImpl();
  }
};

class TokenTest {
public:
  
  static bool test() {
    runTest(testBasicTokenAllocation);
    runTest(testBasicTokenCreation);
    runTest(testStateModel);
    runTest(testMasterSlaveRelationship);
    runTest(testBasicMerging);
    runTest(testConstraintMigrationDuringMerge);
    runTest(testMergingPerformance);
    runTest(testTokenCompatibility);
    runTest(testTokenFactory);
    runTest(testCorrectSplit_Gnats2450);
    return(true);
  }
  
private:
  
  static bool testBasicTokenAllocation() {
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, true);
    retval = testBasicTokenAllocationImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }

  static bool testBasicTokenCreation() {                                                            
    bool retval = false;
    DEFAULT_SETUP(ce,db,schema,false);
    retval = testBasicTokenCreationImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;                                                                         
  }                            

  static bool testStateModel(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, true);
    retval = testStateModelImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }

  static bool testMasterSlaveRelationship(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, true);
    retval = testMasterSlaveRelationshipImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    // Remainder should be cleaned up automatically.
    return retval;
  }

  static bool testBasicMerging(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, true);
    retval = testBasicMergingImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    // Deletion will now occur and test proper cleanup.
    return retval;
  }

  // This test has been fixed by line 56 in MergeMemento.cc.
  // If we invert the order of the splits at the end of this test, the code
  // will error out.

  static bool testConstraintMigrationDuringMerge() {
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testConstraintMigrationDuringMergeImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }

  // add backtracking and longer chain, also add a before constraint
  static bool testMergingPerformance(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testMergingPerformanceImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }    

  static bool testTokenCompatibility(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, true);
    retval = testTokenCompatibilityImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }

  static bool testTokenFactory(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, true);
    retval = testTokenFactoryImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }

  /**
   * @brief Tests that a split will not cause the specified domain of the merged token
   * to be relaxed to the base domain.
   */
  static bool testCorrectSplit_Gnats2450(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, true);
    retval = testCorrectSplit_Gnats2450impl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }
};

class TimelineTest {
public:
  static bool test(){
    runTest(testFullInsertion);
    runTest(testBasicInsertion);
    runTest(testObjectTokenRelation);
    runTest(testTokenOrderQuery);
    runTest(testEventTokenInsertion);
    runTest(testNoChoicesThatFit);
    return true;
  }

private:
  static bool testBasicInsertion(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testBasicInsertionImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }

  static bool testObjectTokenRelation(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testObjectTokenRelationImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }

  static bool testTokenOrderQuery(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testTokenOrderQueryImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }

  static bool testEventTokenInsertion(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testEventTokenInsertionImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }

  static bool testFullInsertion(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testFullInsertionImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }

  static bool testNoChoicesThatFit(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testNoChoicesThatFitImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }
};

class DbClientTest {
public:
  static bool test(){
    runTest(testFactoryMethods);
    runTest(testBasicAllocation);
    runTest(testPathBasedRetrieval);
    return true;
  }
private:
  static bool testFactoryMethods(){
    std::vector<ConstructorArgument> arguments;
    IntervalIntDomain arg0(10);
    LabelSet arg1(LabelStr("Label"));
    arguments.push_back(ConstructorArgument(LabelStr("int"), &arg0)); 
    arguments.push_back(ConstructorArgument(LabelStr("string"), &arg1));
    LabelStr factoryName = ObjectFactory::makeFactoryName(LabelStr("Foo"), arguments);
    assert(factoryName == LabelStr("Foo:int:string"));
    return true;
  }

  static bool testBasicAllocation(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, false);
    retval = testBasicAllocationImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }

  static bool testPathBasedRetrieval(){
    bool retval = false;
    DEFAULT_SETUP(ce, db, schema, true);
    retval = testPathBasedRetrievalImpl(ce, db, schema);
    DEFAULT_TEARDOWN();
    return retval;
  }
};



int main() {
  initConstraintLibrary();
  
  initDbModuleTests();
  
  runTestSuite(ObjectTest::test);
  runTestSuite(TokenTest::test);
  runTestSuite(TimelineTest::test);
  runTestSuite(DbClientTest::test);
  std::cout << "Finished" << std::endl;
  ConstraintLibrary::purgeAll();
}
