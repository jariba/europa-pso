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

#include <iostream>
#include <string>

/**
 * @brief Declaration and definition for test constraint to force a failure when the domain becomes a singleton
 */
class ForceFailureConstraint : public UnaryConstraint {
public:
  ForceFailureConstraint(const LabelStr& name,
			 const LabelStr& propagatorName,
			 const ConstraintEngineId& constraintEngine,
			 const ConstrainedVariableId& variable,
			 const AbstractDomain& domain = IntervalIntDomain())
  : UnaryConstraint(name, propagatorName, constraintEngine, variable){}

  const AbstractDomain& getDomain() const {
    static IntervalIntDomain sl_noDomain;
    return sl_noDomain;
  }

  void handleExecute(){
    if(getCurrentDomain(m_variables[0]).isSingleton())
      getCurrentDomain(m_variables[0]).empty();
  }
};

class DefaultSchemaAccessor {
public:

  static const SchemaId& instance() {
    if (s_instance.isNoId())
      s_instance = (new Schema())->getId();
    return(s_instance);
  }

  static void reset() {
    if (!s_instance.isNoId()) {
      delete (Schema*) s_instance;
      s_instance = SchemaId::noId();
    }
  }

private:
  static SchemaId s_instance;
};

const LabelStr ALL_OBJECTS("AllObjects");

SchemaId DefaultSchemaAccessor::s_instance;

#define SCHEMA DefaultSchemaAccessor::instance()

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
    Object* objectPtr = new Object(db, ALL_OBJECTS, LabelStr("o1")); \
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
    PlanDatabase db(ENGINE, SCHEMA);
    Object o1(db.getId(), ALL_OBJECTS, LabelStr("o1"));
    Object o2(db.getId(), ALL_OBJECTS, LabelStr("o2"));
    ObjectId id0((new Object(o1.getId(), ALL_OBJECTS, LabelStr("id0")))->getId());
    Object o3(o2.getId(), ALL_OBJECTS, LabelStr("o3"));
    assert(db.getObjects().size() == 4);
    assert(o1.getComponents().size() == 1);
    assert(o3.getParent() == o2.getId());
    delete (Object*) id0;
    assert(db.getObjects().size() == 3);
    assert(o1.getComponents().empty());

    ObjectId id1((new Object(db.getId(), ALL_OBJECTS, LabelStr("id1")))->getId());
    new Object(id1, ALL_OBJECTS, LabelStr("id2"));
    ObjectId id3((new Object(id1, ALL_OBJECTS, LabelStr("id3")))->getId());
    assert(db.getObjects().size() == 6);
    assert(id3->getName().toString() == "id1.id3");

    // Test ancestor call
    ObjectId id4((new Object(id3, ALL_OBJECTS, LabelStr("id4")))->getId());
    std::list<ObjectId> ancestors;
    id4->getAncestors(ancestors);
    assert(ancestors.front() == id3);
    assert(ancestors.back() == id1);

    // Force cascaded delete
    delete (Object*) id1;
    assert(db.getObjects().size() == 3);

    // Now allocate dynamically and allow the plan database to clean it up when it deallocates
    ObjectId id5 = ((new Object(db.getId(), ALL_OBJECTS, LabelStr("id5")))->getId());
    new Object(id5, ALL_OBJECTS, LabelStr("id6"));
    return(true);
  }

  static bool testObjectDomain(){
    PlanDatabase db(ENGINE, SCHEMA);
    std::list<ObjectId> values;
    Object o1(db.getId(), ALL_OBJECTS, LabelStr("o1"));
    Object o2(db.getId(), ALL_OBJECTS, LabelStr("o2"));
    assert(db.getObjects().size() == 2);
    values.push_back(o1.getId());
    values.push_back(o2.getId());
    ObjectDomain os1(values, true);
    assert(os1.isMember(o1.getId()));
    os1.remove(o1.getId());
    assert(!os1.isMember(o1.getId()));
    assert(os1.isSingleton());
    return true;
  }

  static bool testObjectVariables(){
    PlanDatabase db(ENGINE, SCHEMA);
    Object o1(db.getId(), ALL_OBJECTS, LabelStr("o1"), true);
    assert(!o1.isComplete());
    o1.addVariable(IntervalIntDomain(), LabelStr("VAR1"));
    o1.addVariable(BoolDomain(), LabelStr("VAR2"));
    o1.close();
    assert(o1.isComplete());
    assert(o1.getVariable(LabelStr("VAR21")) != o1.getVariable(LabelStr("VAR2")));

    Object o2(db.getId(), ALL_OBJECTS, LabelStr("o2"), true);
    assert(!o2.isComplete());
    o2.addVariable(IntervalIntDomain(15, 200), LabelStr("VAR1"));
    o2.close();

    // Add a unary constraint
    ConstraintLibrary::createConstraint(LabelStr("SubsetOf"), 
					db.getConstraintEngine(), 
					o1.getVariables()[0],
					IntervalIntDomain(10, 20));

    // Now add a constraint equating the variables and test propagation
    std::vector<ConstrainedVariableId> constrainedVars;
    constrainedVars.push_back(o1.getVariables()[0]);
    constrainedVars.push_back(o2.getVariables()[0]);
    ConstraintId constraint = ConstraintLibrary::createConstraint(LabelStr("Equal"),
								  db.getConstraintEngine(),
								  constrainedVars);

    assert(db.getConstraintEngine()->propagate());
    assert(o1.getVariables()[0]->lastDomain() == o1.getVariables()[0]->lastDomain());

    // Delete one of the constraints to force automatic clean-up path and explciit clean-up
    delete (Constraint*) constraint;

    return(true);
  }


  static bool testObjectTokenRelation(){
    PlanDatabase db(ENGINE, SCHEMA);
    // 1. Create 2 objects
    ObjectId object1 = (new Object(db.getId(), ALL_OBJECTS, LabelStr("O1")))->getId();
    ObjectId object2 = (new Object(db.getId(), ALL_OBJECTS, LabelStr("O2")))->getId();    
    db.close();

    assert(object1 != object2);
    assert(db.getObjects().size() == 2);
    // 2. Create 1 token.
    EventToken eventToken(db.getId(), LabelStr("Predicate"), false, IntervalIntDomain(0, 10));

    // Confirm not added to the object
    assert(!eventToken.getObject()->getDerivedDomain().isSingleton());

    // 3. Activate token. (NO subgoals)
    eventToken.activate();

    // Confirm not added to the object
    assert(!eventToken.getObject()->getDerivedDomain().isSingleton());

    // 4. Specify tokens object variable to a ingletone

    eventToken.getObject()->specify(object1);

    // Confirm added to the object
    assert(eventToken.getObject()->getDerivedDomain().isSingleton());

    // 5. propagate
    db.getConstraintEngine()->propagate();

    // 6. reset object variables domain.
    eventToken.getObject()->reset();

    // Confirm it is no longer part of the object
    // Confirm not added to the object
    assert(!eventToken.getObject()->getDerivedDomain().isSingleton());

    return true;
  }

  static bool testCommonAncestorConstraint(){
    PlanDatabase db(ENGINE, SCHEMA);
    Object o1(db.getId(), ALL_OBJECTS, LabelStr("o1"));
    Object o2(o1.getId(), ALL_OBJECTS, LabelStr("o2"));
    Object o3(o1.getId(), ALL_OBJECTS, LabelStr("o3"));
    Object o4(o2.getId(), ALL_OBJECTS, LabelStr("o4"));
    Object o5(o2.getId(), ALL_OBJECTS, LabelStr("o5"));
    Object o6(o3.getId(), ALL_OBJECTS, LabelStr("o6"));
    Object o7(o3.getId(), ALL_OBJECTS, LabelStr("o7"));

    ObjectDomain allObjects;
    allObjects.insert(o1.getId());
    allObjects.insert(o2.getId());
    allObjects.insert(o3.getId());
    allObjects.insert(o4.getId());
    allObjects.insert(o5.getId());
    allObjects.insert(o6.getId());
    allObjects.insert(o7.getId());
    allObjects.close();

    // Ensure there they agree on a common root.
    {
      Variable<ObjectDomain> first(ENGINE, ObjectDomain(o4.getId()));
      Variable<ObjectDomain> second(ENGINE, ObjectDomain(o7.getId()));
      Variable<ObjectDomain> restrictions(ENGINE, ObjectDomain(o1.getId()));
      CommonAncestorConstraint constraint(LabelStr("commonAncestor"), 
					  LabelStr("Default"), 
					  ENGINE, 
					  makeScope(first.getId(), second.getId(), restrictions.getId()));

      assert(ENGINE->propagate());
    }

    // Now impose a different set of restrictions which will eliminate all options
    {
      Variable<ObjectDomain> first(ENGINE, ObjectDomain(o4.getId()));
      Variable<ObjectDomain> second(ENGINE, ObjectDomain(o7.getId()));
      Variable<ObjectDomain> restrictions(ENGINE, ObjectDomain(o2.getId()));
      CommonAncestorConstraint constraint(LabelStr("commonAncestor"), 
					  LabelStr("Default"), 
					  ENGINE, 
					  makeScope(first.getId(), second.getId(), restrictions.getId()));

      assert(!ENGINE->propagate());
    }

    // Now try a set of restrictions, which will allow it to pass
    {
      Variable<ObjectDomain> first(ENGINE, ObjectDomain(o4.getId()));
      Variable<ObjectDomain> second(ENGINE, ObjectDomain(o7.getId()));
      Variable<ObjectDomain> restrictions(ENGINE, allObjects);
      CommonAncestorConstraint constraint(LabelStr("commonAncestor"), 
					  LabelStr("Default"), 
					  ENGINE, 
					  makeScope(first.getId(), second.getId(), restrictions.getId()));

      assert(ENGINE->propagate());
    }

    // Now try when no variable is a singleton, and then one becomes a singleton
    {
      Variable<ObjectDomain> first(ENGINE, allObjects);
      Variable<ObjectDomain> second(ENGINE, allObjects);
      Variable<ObjectDomain> restrictions(ENGINE, allObjects);
      CommonAncestorConstraint constraint(LabelStr("commonAncestor"), 
					  LabelStr("Default"), 
					  ENGINE, 
					  makeScope(first.getId(), second.getId(), restrictions.getId()));

      assert(ENGINE->propagate()); // All ok so far

      restrictions.specify(o2.getId());
      assert(ENGINE->propagate()); // Nothing happens yet.

      first.specify(o6.getId()); // Now we should propagate to failure
      assert(!ENGINE->propagate());
      first.reset();

      first.specify(o4.getId());
      assert(ENGINE->propagate());
    }

    return true;
  }

 static bool testHasAncestorConstraint(){

    PlanDatabase db(ENGINE, SCHEMA);
    Object o1(db.getId(), ALL_OBJECTS, LabelStr("o1"));
    Object o2(o1.getId(), ALL_OBJECTS, LabelStr("o2"));
    Object o3(o1.getId(), ALL_OBJECTS, LabelStr("o3"));
    Object o4(o2.getId(), ALL_OBJECTS, LabelStr("o4"));
    Object o5(o2.getId(), ALL_OBJECTS, LabelStr("o5"));
    Object o6(o3.getId(), ALL_OBJECTS, LabelStr("o6"));
    Object o7(o3.getId(), ALL_OBJECTS, LabelStr("o7"));
    Object o8(db.getId(), ALL_OBJECTS, LabelStr("o8"));


    // Positive test immediate ancestor
    {
      Variable<ObjectDomain> first(ENGINE, ObjectDomain(o7.getId()));
      Variable<ObjectDomain> restrictions(ENGINE, ObjectDomain(o3.getId()));
      HasAncestorConstraint constraint(LabelStr("hasAncestor"), 
					  LabelStr("Default"), 
					  ENGINE, 
					  makeScope(first.getId(), restrictions.getId()));

      assert(ENGINE->propagate());
    }

    // negative test immediate ancestor
    {
      Variable<ObjectDomain> first(ENGINE, ObjectDomain(o7.getId()));
      Variable<ObjectDomain> restrictions(ENGINE, ObjectDomain(o2.getId()));
      HasAncestorConstraint constraint(LabelStr("hasAncestor"), 
					  LabelStr("Default"), 
					  ENGINE, 
					  makeScope(first.getId(), restrictions.getId()));

      assert(!ENGINE->propagate());
    }
    // Positive test higher up  ancestor
    {
      Variable<ObjectDomain> first(ENGINE, ObjectDomain(o7.getId()));
      Variable<ObjectDomain> restrictions(ENGINE, ObjectDomain(o1.getId()));
      HasAncestorConstraint constraint(LabelStr("hasAncestor"), 
					  LabelStr("Default"), 
					  ENGINE, 
					  makeScope(first.getId(), restrictions.getId()));

      assert(ENGINE->propagate());
    }
    // negative test higherup ancestor
    {
      Variable<ObjectDomain> first(ENGINE, ObjectDomain(o7.getId()));
      Variable<ObjectDomain> restrictions(ENGINE, ObjectDomain(o8.getId()));
      HasAncestorConstraint constraint(LabelStr("hasAncestor"), 
					  LabelStr("Default"), 
					  ENGINE, 
					  makeScope(first.getId(), restrictions.getId()));

      assert(!ENGINE->propagate());
    }

    //positive restriction of the set.
    {
      ObjectDomain obs;
      obs.insert(o7.getId());
      obs.insert(o4.getId());
      obs.close();

      Variable<ObjectDomain> first(ENGINE, obs);
      Variable<ObjectDomain> restrictions(ENGINE, ObjectDomain(o2.getId()));
      HasAncestorConstraint constraint(LabelStr("hasAncestor"), 
					  LabelStr("Default"), 
					  ENGINE, 
					  makeScope(first.getId(), restrictions.getId()));

      assert(ENGINE->propagate());
      assert(first.getDerivedDomain().isSingleton());
    }

    //no restriction of the set.
    {
      ObjectDomain obs1;
      obs1.insert(o7.getId());
      obs1.insert(o4.getId());
      obs1.close();

      Variable<ObjectDomain> first(ENGINE, obs1);
      Variable<ObjectDomain> restrictions(ENGINE, ObjectDomain(o1.getId()));
      HasAncestorConstraint constraint(LabelStr("hasAncestor"), 
					  LabelStr("Default"), 
					  ENGINE, 
					  makeScope(first.getId(), restrictions.getId()));

      assert(ENGINE->propagate());
      assert(first.getDerivedDomain().getSize() == 2);
    }

    return true;
 }

  /**
   * The most basic case for dynamic objects is that we can populate the variable correclty
   * and synchronize its values.
   */
  static bool testMakeObjectVariable(){
    PlanDatabase db(ENGINE, SCHEMA);
    ConstrainedVariableId v0 = (new Variable<ObjectDomain>(ENGINE, ObjectDomain()))->getId();
    assert(!v0->isClosed());
    db.makeObjectVariableFromType(ALL_OBJECTS, v0);
    assert(v0->isClosed());
    assert(ENGINE->provenInconsistent());

    // Now add an object and we should expect the constraint network to be consistent
    Object o1(db.getId(), ALL_OBJECTS, LabelStr("o1"));
    assert(ENGINE->propagate());
    assert(!db.isClosed(ALL_OBJECTS));

    // Now delete the variable. This should remove the listener
    delete (ConstrainedVariable*) v0;

    return true;
  }

  /**
   * Have ate least one object in the system prior to creating a token. Then show how
   * removal triggers an inconsistency, and insertion of another object fixes it. Also
   * show that specifiying the object prevents propagation if we add another object, but
   * relaxing it will populate the object variable to include the new object.
   */
  static bool testTokenObjectVariable(){
    PlanDatabase db(ENGINE, SCHEMA);

    // Now add an object and we should expect the constraint network to be consistent
    ObjectId o1 = (new Object(db.getId(), ALL_OBJECTS, LabelStr("o1")))->getId();
    EventToken eventToken(db.getId(), LabelStr("Predicate"), false, IntervalIntDomain(0, 10));
    assert(ENGINE->propagate());

    // Deletion of the object should mean an immediate inconsistency
    delete (Object*) o1;
    assert(ENGINE->provenInconsistent());
    assert(eventToken.getObject()->baseDomain().isEmpty());

    // Insertion of a new object should reecover the situation
    ObjectId o2 = (new Object(db.getId(), ALL_OBJECTS, LabelStr("o2")))->getId();
    assert(ENGINE->pending());
    assert(ENGINE->propagate());
    assert(eventToken.getObject()->baseDomain().isSingleton());

    // Now specify it
    eventToken.getObject()->specify(o2);

    // Addition of a new object will update the base domain, but not the spec or derived.
    // Consequently, no further propagation is required
    ObjectId o3 = (new Object(db.getId(), ALL_OBJECTS, LabelStr("o3")))->getId();
    assert(ENGINE->constraintConsistent());
    assert(!eventToken.getObject()->baseDomain().isSingleton());
    assert(eventToken.getObject()->lastDomain().isSingleton());

    // Now resetting the specified domain will reverty the derived domain back completely
    eventToken.getObject()->reset();
    assert(ENGINE->pending());
    assert(ENGINE->propagate());
    assert(eventToken.getObject()->lastDomain().isMember(o2));
    assert(eventToken.getObject()->lastDomain().isMember(o3));

    return true;
  }
};




class IntervalTokenFactory: public ConcreteTokenFactory {
public:
  IntervalTokenFactory(): ConcreteTokenFactory(LabelStr("Foo")){}
private:
  TokenId createInstance(const PlanDatabaseId& planDb) const{
    TokenId token = (new IntervalToken(planDb, LabelStr("Foo"), true))->getId();
    return token;
  }
  TokenId createInstance(const TokenId& master) const{
    TokenId token = (new IntervalToken(master, LabelStr("Foo")))->getId();
    return token;
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
    DEFAULT_SETUP(ce, db, schema, true);

    // Event Token
    EventToken eventToken(db, LabelStr("Predicate"), true, IntervalIntDomain(0, 1000), Token::noObject(), false);
    assert(eventToken.getStart()->getDerivedDomain() == eventToken.getEnd()->getDerivedDomain());
    assert(eventToken.getDuration()->getDerivedDomain() == IntervalIntDomain(0, 0));
    eventToken.getStart()->specify(IntervalIntDomain(5, 10));
    assert(eventToken.getEnd()->getDerivedDomain() == IntervalIntDomain(5, 10));
    eventToken.addParameter(IntervalDomain(-1.08, 20.18), LabelStr("TestParam"));
    eventToken.close();

    // IntervalToken
    IntervalToken intervalToken(db, 
				LabelStr("Predicate"), 
				true, 
				IntervalIntDomain(0, 1000),
				IntervalIntDomain(0, 1000),
				IntervalIntDomain(2, 10),
				Token::noObject(), false);

    std::list<Prototype::LabelStr> values;
    values.push_back(Prototype::LabelStr("L1"));
    values.push_back(Prototype::LabelStr("L4"));
    values.push_back(Prototype::LabelStr("L2"));
    values.push_back(Prototype::LabelStr("L5"));
    values.push_back(Prototype::LabelStr("L3"));
    intervalToken.addParameter(LabelSet(values, true));
    intervalToken.close();
    assert(intervalToken.getEnd()->getDerivedDomain().getLowerBound() == 2);
    intervalToken.getStart()->specify(IntervalIntDomain(5, 10));
    assert(intervalToken.getEnd()->getDerivedDomain() == IntervalIntDomain(7, 20));
    intervalToken.getEnd()->specify(IntervalIntDomain(9, 10));
    assert(intervalToken.getStart()->getDerivedDomain() == IntervalIntDomain(5, 8));
    assert(intervalToken.getDuration()->getDerivedDomain() == IntervalIntDomain(2, 5));

    // Create and delete a Token
    TokenId token = (new IntervalToken(db, 
				       LabelStr("Predicate"), 
				       true, 
				       IntervalIntDomain(0, 1000),
				       IntervalIntDomain(0, 1000),
				       IntervalIntDomain(2, 10),
				       Token::noObject(), true))->getId();

    delete (Token*) token; // It is inComplete

    DEFAULT_TEARDOWN();
    return true;
  }

  static bool testBasicTokenCreation() {                                                            
    DEFAULT_SETUP(ce,db,schema,false);                                                   
    ObjectId timeline = (new Timeline(db, ALL_OBJECTS, LabelStr("o2")))->getId();
    assert(!timeline.isNoId());
    db->close();                                                                          
    
    IntervalToken t1(db,                                                         
                     LabelStr("P1"),                                                     
                     true,                                                               
                     IntervalIntDomain(0, 10),                                           
                     IntervalIntDomain(0, 20),                                           
                     IntervalIntDomain(1, 1000));                                        
                                                                                         
    DEFAULT_TEARDOWN();
    return true;                                                                         
  }                            

  static bool testStateModel(){
    DEFAULT_SETUP(ce, db, schema, true);

    IntervalToken t0(db, 
		     LabelStr("Predicate"), 
		     true, 
		     IntervalIntDomain(0, 1000),
		     IntervalIntDomain(0, 1000),
		     IntervalIntDomain(2, 10),
		     Token::noObject(), false);

    assert(t0.isIncomplete());
    t0.close();
    assert(t0.isInactive());
    t0.reject();
    assert(t0.isRejected());
    t0.cancel();
    assert(t0.isInactive());
    t0.activate();
    assert(t0.isActive());
    t0.cancel();
    assert(t0.isInactive());

    IntervalToken t1(db, 
		     LabelStr("Predicate"), 
		     true, 
		     IntervalIntDomain(0, 1000),
		     IntervalIntDomain(0, 1000),
		     IntervalIntDomain(2, 10),
		     Token::noObject(), true);

    // Constraint the start variable of both tokens
    EqualConstraint c0(LabelStr("eq"), LabelStr("Default"), ENGINE, makeScope(t0.getStart(), t1.getStart()));

    assert(t1.isInactive());
    t0.activate();
    t1.merge(t0.getId());
    assert(t1.isMerged());
    t1.cancel();
    assert(t1.isInactive());
    t1.merge(t0.getId());
    DEFAULT_TEARDOWN();
    return true;
  }

  static bool testMasterSlaveRelationship(){
    DEFAULT_SETUP(ce, db, schema, true);

    IntervalToken t0(db, 
		     LabelStr("Predicate"), 
		     false, 
		     IntervalIntDomain(0, 1),
		     IntervalIntDomain(0, 1),
		     IntervalIntDomain(1, 1));
    t0.activate();

    TokenId t1 = (new IntervalToken(db, 
				    LabelStr("Predicate"), 
				    false,
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(1, 1)))->getId();
    t1->activate();

    TokenId t2 = (new IntervalToken(t0.getId(), 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(1, 1)))->getId();

    TokenId t3 = (new IntervalToken(t0.getId(), 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(1, 1)))->getId();

    TokenId t4 = (new IntervalToken(t0.getId(), 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(1, 1)))->getId();

    TokenId t5 = (new IntervalToken(t1, 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(1, 1)))->getId();

    TokenId t6 = (new EventToken(t0.getId(), 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(0, 1)))->getId();

    // These are mostly to avoid compiler warnings about unused variables.
    assert(t3 != t4);
    assert(t5 != t6);

    // Delete slave only
    delete (Token*) t2;
    // Should verify correct count of tokens remain. --wedgingt 2004 Feb 27

    // Delete master & slaves
    delete (Token*) t1;
    // Should verify correct count of tokens remain. --wedgingt 2004 Feb 27

    DEFAULT_TEARDOWN();

    // Remainder should be cleaned up automatically.
    return(true);
  }

  static bool testBasicMerging(){
    DEFAULT_SETUP(ce, db, schema, true);

    // Create 2 mergeable tokens - predicates, types and base domaiuns match
    IntervalToken t0(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    assert(t0.getDuration()->getDerivedDomain().getUpperBound() == 20);

    IntervalToken t1(db,
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    t1.getDuration()->specify(IntervalIntDomain(5, 7));

    // Activate & deactivate - ensure proper handling of rejectability variable
    assert(!t0.getState()->getDerivedDomain().isSingleton());
    t0.activate();
    assert(t0.getState()->getDerivedDomain().isSingleton());
    assert(t0.getState()->getDerivedDomain().getSingletonValue() == Token::ACTIVE);
    t0.cancel();
    assert(!t0.getState()->getDerivedDomain().isSingleton());

    // Now activate and merge
    t0.activate();
    t1.merge(t0.getId());

    // Make sure the necessary restrictions have been imposed due to merging i.e. restruction due to specified domain
    assert(t0.getDuration()->getDerivedDomain().getUpperBound() == 7);
    assert(t1.isMerged());

    // Do a split and make sure the old values are reinstated.
    t1.cancel();
    assert(t0.getDuration()->getDerivedDomain().getUpperBound() == 20);
    assert(t1.isInactive());

    // Now post equality constraint between t1 and extra token t2 and remerge
    IntervalToken t2(db, 
		     LabelStr("P2"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    t2.getEnd()->specify(IntervalIntDomain(8, 10));

    std::vector<ConstrainedVariableId> temp;
    temp.push_back(t1.getEnd());
    temp.push_back(t2.getEnd());
    ConstraintId equalityConstraint = ConstraintLibrary::createConstraint(LabelStr("concurrent"),
									  db->getConstraintEngine(),
									  temp);
    t1.merge(t0.getId());

    assert(!t0.getMergedTokens().empty());

    // Verify that the equality constraint has migrated and original has been deactivated.
    //TBW: when stacking instead of merging tokens, the next check is not true
    // assert(!equalityConstraint->isActive());
    assert(t0.getEnd()->getDerivedDomain().getLowerBound() == 8);
    assert(t0.getEnd()->getDerivedDomain() == t2.getEnd()->getDerivedDomain());

    // Undo the merge and check for initial conditions being established
    t1.cancel();
    assert(equalityConstraint->isActive());

    // Redo the merge
    t1.merge(t0.getId());

    // Confirm deletion of the constraint is handled correctly
    delete (Constraint*) equalityConstraint;
    assert(t0.getEnd()->getDerivedDomain() != t2.getEnd()->getDerivedDomain());

    // Confirm previous restriction due to specified domain, then reset and note the change
    assert(t0.getDuration()->getDerivedDomain().getUpperBound() == 7);
    t1.getDuration()->reset();
    assert(t0.getDuration()->getDerivedDomain().getUpperBound() == 20);


    // Test unary
    t1.cancel();
    ConstraintId subsetOfConstraint = ConstraintLibrary::createConstraint(LabelStr("SubsetOf"),
									  db->getConstraintEngine(),
									  t1.getDuration(),
									  IntervalIntDomain(5, 6));
    t1.merge(t0.getId());
    assert(t0.getDuration()->getDerivedDomain().getUpperBound() == 6);
    delete (Constraint*) subsetOfConstraint;

    DEFAULT_TEARDOWN();

    // Deletion will now occur and test proper cleanup.
    return true;
  }

  // This test has been fixed by line 56 in MergeMemento.cc.
  // If we invert the order of the splits at the end of this test, the code
  // will error out.

  static bool testConstraintMigrationDuringMerge() {
    DEFAULT_SETUP(ce, db, schema, false);
    ObjectId timeline1 = (new Timeline(db, ALL_OBJECTS, LabelStr("timeline1")))->getId();
    ObjectId timeline2 = (new Timeline(db, ALL_OBJECTS, LabelStr("timeline2")))->getId();
    db->close();

    // Create two base tokens
    IntervalToken t0(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    IntervalToken t1(db,
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));


    // Create 2 mergeable tokens - predicates, types and base domains match
    IntervalToken t2(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    IntervalToken t3(db,
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));


    LessThanEqualConstraint c0(LabelStr("leq"), LabelStr("Default"), db->getConstraintEngine(), makeScope(t1.getStart(), t3.getStart()));

    t0.activate();
    t2.activate();
    timeline1->constrain(t0.getId());
    timeline2->constrain(t2.getId());

    db->getConstraintEngine()->propagate();

    t1.merge(t0.getId());
    t3.merge(t2.getId());

    t3.cancel();
    t1.cancel();

    DEFAULT_TEARDOWN();

    return true;
  }

  // add backtracking and longer chain, also add a before constraint
  static bool testMergingPerformance(){
    DEFAULT_SETUP(ce, db, schema, false);
    ObjectId timeline = (new Timeline(db, ALL_OBJECTS, LabelStr("o2")))->getId();
    db->close();

    typedef Id<IntervalToken> IntervalTokenId;
    
    static const int NUMTOKS=3;
    static const int UNIFIED=1;
    static const int NUMPARAMS=1;

    //Create tokens with the same domains.  We will impose a constraint on
    //each token variable.  Tokens will have 5 parameter variables.
    std::vector< std::vector<IntervalTokenId> > tokens;

    for (int i=0; i < NUMTOKS; i++) {
      std::vector<IntervalTokenId> tmp;
      for (int j=0; j < UNIFIED; j++) {
	IntervalTokenId t = (new IntervalToken(db, 
					       LabelStr("P1"), 
					       true,
					       IntervalIntDomain(0, 210),
					       IntervalIntDomain(0, 220),
					       IntervalIntDomain(1, 110),
					       Token::noObject(), false))->getId();
	for (int k=0; k < NUMPARAMS; k++)
	  t->addParameter(IntervalIntDomain(500+j,1000));
	t->close();
	tmp.push_back(t);
      }
      tokens.push_back(tmp);
    }

    IntervalIntDomain sdom1(tokens[0][0]->getStart()->getDerivedDomain());
    assert(sdom1.getLowerBound() == 0);
    assert(sdom1.getUpperBound() == 210);

    IntervalIntDomain edom1(tokens[0][0]->getEnd()->getDerivedDomain());
    assert(edom1.getLowerBound() == 1);
    assert(edom1.getUpperBound() == 220);

    Id<TokenVariable<IntervalIntDomain> > pvar1(tokens[0][0]->getParameters()[0]);
    IntervalIntDomain pdom1(pvar1->getDerivedDomain());
    assert(pdom1.getLowerBound() == 500);
    assert(pdom1.getUpperBound() == 1000);

    for (int i=0; i < NUMTOKS; i++) {
      tokens[i][0]->activate();
      timeline->constrain(tokens[i][0]);
    }

    IntervalIntDomain sdom2(tokens[0][0]->getStart()->getDerivedDomain());
    assert(sdom2.getLowerBound() == 0);
    assert(sdom2.getUpperBound() == 208);

    IntervalIntDomain edom2(tokens[0][0]->getEnd()->getDerivedDomain());
    assert(edom2.getLowerBound() == 1);
    assert(edom2.getUpperBound() == 209);

    Id<TokenVariable<IntervalIntDomain> > pvar2(tokens[0][0]->getParameters()[0]);
    IntervalIntDomain pdom2(pvar2->getDerivedDomain());
    assert(pdom2.getLowerBound() == 500);
    assert(pdom2.getUpperBound() == 1000);

    for (int i=0; i < NUMTOKS; i++)
      for (int j=1; j < UNIFIED; j++) { 
	tokens[i][j]->merge(tokens[i][0]);
	ce->propagate();
      }

    IntervalIntDomain sdom3(tokens[0][0]->getStart()->getDerivedDomain());
    assert(sdom3.getLowerBound() == 0);
    assert(sdom3.getUpperBound() == 208);

    IntervalIntDomain edom3(tokens[0][0]->getEnd()->getDerivedDomain());
    assert(edom3.getLowerBound() == 1);
    assert(edom3.getUpperBound() == 209);

    Id<TokenVariable<IntervalIntDomain> > pvar3(tokens[0][0]->getParameters()[0]);
    IntervalIntDomain pdom3(pvar3->getDerivedDomain());
    assert(pdom3.getLowerBound() == 500+UNIFIED-1);
    assert(pdom3.getUpperBound() == 1000);

    for (int i=0; i < NUMTOKS; i++)
      for (int j=1; j < UNIFIED; j++) {
	tokens[i][j]->cancel();
	ce->propagate();
      }

    IntervalIntDomain sdom4(tokens[0][0]->getStart()->getDerivedDomain());
    assert(sdom4.getLowerBound() == sdom2.getLowerBound());
    assert(sdom4.getUpperBound() == sdom2.getUpperBound());

    IntervalIntDomain edom4(tokens[0][0]->getEnd()->getDerivedDomain());
    assert(edom4.getLowerBound() == edom2.getLowerBound());
    assert(edom4.getUpperBound() == edom2.getUpperBound());

    Id<TokenVariable<IntervalIntDomain> > pvar4(tokens[0][0]->getParameters()[0]);
    IntervalIntDomain pdom4(pvar4->getDerivedDomain());
    assert(pdom4.getLowerBound() == pdom2.getLowerBound());
    assert(pdom4.getUpperBound() == pdom2.getUpperBound());

    DEFAULT_TEARDOWN();
    return true;
  }    

  static bool testTokenCompatibility(){
    DEFAULT_SETUP(ce, db, schema, true);

    // Create 2 mergeable tokens - predicates, types and base domaiuns match
    IntervalToken t0(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000),
		     Token::noObject(), false);
    t0.addParameter(IntervalDomain(1, 20));
    t0.close();

    // Same predicate and has an intersection
    IntervalToken t1(db,
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000),
		     Token::noObject(), false);
    t1.addParameter(IntervalDomain(10, 40)); // There is an intersection - but it is not a subset. Still should match
    t1.close();

    t0.activate();
    std::vector<TokenId> compatibleTokens;
    bool res = ce->propagate();
    assert(res);
    db->getCompatibleTokens(t1.getId(), compatibleTokens);
    assert(compatibleTokens.size() == 1);
    assert(compatibleTokens[0] == t0.getId());

    compatibleTokens.clear();
    t0.cancel();
    res = ce->propagate();
    assert(res);
    db->getCompatibleTokens(t1.getId(), compatibleTokens);
    assert(compatibleTokens.empty()); // No match since no tokens are active

    IntervalToken t2(db,
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000),
		     Token::noObject(), false);
    t2.addParameter(IntervalDomain(0, 0)); // Force no intersection
    t2.close();

    t0.activate();
    res = ce->propagate();
    assert(res);
    compatibleTokens.clear();
    db->getCompatibleTokens(t2.getId(), compatibleTokens);
    assert(compatibleTokens.empty()); // No match since parameter variable has no intersection


    IntervalToken t3(db,
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000),
		     Token::noObject(), false);
    t3.addParameter(IntervalDomain()); // Force no intersection
    t3.close();

    // Post equality constraint between t3 and t0. Should permit a match since it is a binary constraint
    EqualConstraint c0(LabelStr("eq"), LabelStr("Default"), db->getConstraintEngine(), makeScope(t0.getStart(), t3.getStart()));
    db->getConstraintEngine()->propagate();
    compatibleTokens.clear();
    db->getCompatibleTokens(t3.getId(), compatibleTokens);
    assert(compatibleTokens.size() == 1); // Expect a single match

    DEFAULT_TEARDOWN();
    return true;
  }

  static bool testTokenFactory(){
    DEFAULT_SETUP(ce, db, schema, true);
    TokenId master = TokenFactory::createInstance(db, LabelStr("Foo"));
    master->activate();
    TokenId slave = TokenFactory::createInstance(master, LabelStr("Foo"));
    assert(slave->getMaster() == master);
    DEFAULT_TEARDOWN();
    return true;
  }

  /**
   * @brief Tests that a split will not cause the specified domain of the merged token
   * to be relaxed to the base domain.
   */
  static bool testCorrectSplit_Gnats2450(){
    DEFAULT_SETUP(ce, db, schema, true);

    IntervalToken tokenA(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    // Change to base class to excercise problem with wrong signature on TokenVariable
    ConstrainedVariableId start = tokenA.getStart();
    start->specify(5);

    tokenA.activate();
    assert(ce->propagate());

    IntervalToken tokenB(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    IntervalToken tokenC(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    // Post a constraint on tokenB so that it will always fail when it gets merged
    ForceFailureConstraint c0(LabelStr("ForceFailure"), LabelStr("Default"), ce, tokenC.getState());

    // Propagate and test our specified value
    assert(ce->propagate());
    assert(tokenA.getStart()->lastDomain().getSingletonValue() == 5);

    // Now do the merges and test
    tokenB.merge(tokenA.getId());
    assert(ce->propagate());
    assert(tokenA.getStart()->lastDomain().getSingletonValue() == 5);

    tokenC.merge(tokenA.getId());
    assert(!ce->propagate()); // Should always fail

    // Now split it and test that the specified domain is unchanged
    tokenC.cancel();
    assert(ce->propagate()); // Should be OK now
    assert(tokenA.getStart()->lastDomain().getSingletonValue() == 5);

    DEFAULT_TEARDOWN();
    return true;
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
    DEFAULT_SETUP(ce, db, schema, false);
    Timeline timeline(db, ALL_OBJECTS, LabelStr("o2"));
    db->close();

    IntervalToken tokenA(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    IntervalToken tokenB(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    IntervalToken tokenC(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    assert(!timeline.hasTokensToOrder());
    tokenA.activate();
    tokenB.activate();
    tokenC.activate();
    std::vector<TokenId> tokens;
    timeline.getTokensToOrder(tokens);
    assert(tokens.size() == 3);
    assert(timeline.getTokenSequence().size() == 0);
    assert(timeline.hasTokensToOrder());

    unsigned int num_constraints = ce->getConstraints().size();

    timeline.constrain(tokenA.getId());
    num_constraints += 1; // Only object is constrained since sequence should be empty
    assert(ce->getConstraints().size() == num_constraints);

    timeline.constrain(tokenB.getId());
    num_constraints += 2; // Object variable and a single temporal constraint since placing at the end
    assert(ce->getConstraints().size() == num_constraints);

    timeline.constrain(tokenC.getId(), tokenA.getId());
    num_constraints += 2; // Object variable and a single temporal constraint since placing at the beginning
    assert(ce->getConstraints().size() == num_constraints);

    assert(tokenA.getEnd()->getDerivedDomain().getUpperBound() <= tokenB.getStart()->getDerivedDomain().getUpperBound());
    assert(timeline.getTokenSequence().size() == 3);
    assert(!timeline.hasTokensToOrder());

    timeline.free(tokenA.getId());
    num_constraints -= 3; // Object variable and temporal constraints for placement w.r.t B and C.
    num_constraints += 1; // Should have added a new constraint to preserve temporal relationship between B and C which had been indirect
    assert(ce->getConstraints().size() == num_constraints);

    assert(timeline.getTokenSequence().size() == 2);
    tokens.clear();
    timeline.getTokensToOrder(tokens);
    assert(tokens.size() == 1);
    assert(timeline.hasTokensToOrder());

    // Now force it to be part of this timeline, even though it is not otherwise constrained
    tokenA.getObject()->specify(timeline.getId());
    tokens.clear();
    timeline.getTokensToOrder(tokens);
    assert(tokens.size() == 1); // Won't affect this quantity
    assert(tokens.front() == tokenA.getId());

    timeline.constrain(tokenA.getId());
    assert(!timeline.hasTokensToOrder());
    assert(timeline.getTokenSequence().size() == 3);
    timeline.free(tokenC.getId());

    assert(timeline.getTokenSequence().size() == 2);
    assert(timeline.hasTokensToOrder());

    DEFAULT_TEARDOWN();
    return true;
  }

  static bool testObjectTokenRelation(){
    DEFAULT_SETUP(ce, db, schema, false);
    Timeline timeline(db, ALL_OBJECTS, LabelStr("o2"));
    db->close();

    IntervalToken tokenA(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    IntervalToken tokenB(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    IntervalToken tokenC(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    // Object variables are not singletons - so query for tokens to order should return nothing
    std::vector<TokenId> tokensToOrder;
    timeline.getTokensToOrder(tokensToOrder);
    assert(tokensToOrder.empty());

    // Specify the object variable of one - but still should return no tokens since they are all inactive
    tokenA.getObject()->specify(timeline.getId());
    timeline.getTokensToOrder(tokensToOrder);
    assert(tokensToOrder.empty());

    // Now activate all of them
    tokenA.activate();
    tokenB.activate();
    tokenC.activate();
    timeline.getTokensToOrder(tokensToOrder);
    assert(tokensToOrder.size() == 3);

    // Set remainders so they are singeltons and get all back
    tokenB.getObject()->specify(timeline.getId());
    tokenC.getObject()->specify(timeline.getId());
    tokensToOrder.clear();
    timeline.getTokensToOrder(tokensToOrder);
    assert(tokensToOrder.size() == 3);

    // Now incrementally constrain and show reduction in tokens to order
    timeline.constrain(tokenA.getId());
    tokensToOrder.clear();
    timeline.getTokensToOrder(tokensToOrder);
    assert(tokensToOrder.size() == 2);

    timeline.constrain(tokenB.getId());
    tokensToOrder.clear();
    timeline.getTokensToOrder(tokensToOrder);
    assert(tokensToOrder.size() == 1);

    timeline.constrain(tokenC.getId());
    tokensToOrder.clear();
    timeline.getTokensToOrder(tokensToOrder);
    assert(tokensToOrder.empty());


    // Test destruction call path
    Token* tokenD = new IntervalToken(db, 
				      LabelStr("P1"), 
				      true,
				      IntervalIntDomain(0, 10),
				      IntervalIntDomain(0, 20),
				      IntervalIntDomain(1, 1000));
    tokenD->activate();
    timeline.getTokensToOrder(tokensToOrder);
    assert(tokensToOrder.size() == 1);
    delete tokenD;
    tokensToOrder.clear();
    timeline.getTokensToOrder(tokensToOrder);
    assert(tokensToOrder.empty());

    DEFAULT_TEARDOWN();
    return true;
  }

  static bool testTokenOrderQuery(){
    DEFAULT_SETUP(ce, db, schema, false);
    Id<Timeline> timeline = (new Timeline(db, ALL_OBJECTS, LabelStr("o2")))->getId();
    db->close();

    const int COUNT = 5;
    const int DURATION = 10;
    
    for (int i=0;i<COUNT;i++){
      int start = i*DURATION;
      TokenId token = (new IntervalToken(db, 
					 LabelStr("P1"),
					 true,
					 IntervalIntDomain(start, start),
					 IntervalIntDomain(start+DURATION, start+DURATION),
					 IntervalIntDomain(DURATION, DURATION)))->getId();
      assert(!token->getObject()->getBaseDomain().isSingleton());
      token->getObject()->specify(timeline->getId());
      token->activate();
    }

    assert(timeline->getTokens().size() == 0);
    ce->propagate();
    assert(timeline->getTokens().size() == (unsigned int) COUNT);

    int i = 0;
    std::vector<TokenId> tokensToOrder;
    timeline->getTokensToOrder(tokensToOrder);

    while(!tokensToOrder.empty()){
      assert(timeline->getTokenSequence().size() == (unsigned int) i);
      assert(tokensToOrder.size() == (unsigned int) (COUNT - i));
      std::vector<TokenId> choices;
      TokenId toConstrain = tokensToOrder.front();
      timeline->getOrderingChoices(toConstrain, choices);
      assert(!choices.empty());
      TokenId successor = choices.front();
      timeline->constrain(toConstrain, successor);
      bool res = ce->propagate();
      assert(res);
      tokensToOrder.clear();
      timeline->getTokensToOrder(tokensToOrder);
      i++;
      res = ce->propagate();
      assert(res);
    }

    const std::list<TokenId>& tokenSequence = timeline->getTokenSequence();
    assert(tokenSequence.front()->getStart()->getDerivedDomain().getSingletonValue() == 0);
    assert(tokenSequence.back()->getEnd()->getDerivedDomain().getSingletonValue() == COUNT*DURATION);

    // Now ensure the query can correctly indicate no options available
    TokenId token = (new IntervalToken(db, 
				       LabelStr("P1"),
				       true,
				       IntervalIntDomain(0, 0),
				       IntervalIntDomain(),
				       IntervalIntDomain(DURATION, DURATION)))->getId();
    token->getObject()->specify(timeline->getId());
    token->activate();
    std::vector<TokenId> choices;
    timeline->getOrderingChoices(token, choices);
    assert(choices.empty());

    DEFAULT_TEARDOWN();
    return true;
  }

  static bool testEventTokenInsertion(){
    DEFAULT_SETUP(ce, db, schema, false);
    Timeline timeline(db, ALL_OBJECTS, LabelStr("o2"));
    db->close();

    IntervalToken it1(db, 
		      LabelStr("P1"), 
		      true,
		      IntervalIntDomain(0, 10),
		      IntervalIntDomain(0, 1000),
		      IntervalIntDomain(1, 1000));

    it1.getObject()->specify(timeline.getId());
    it1.activate();
    timeline.constrain(it1.getId(), TokenId::noId());

    // Insert at the end after a token
    EventToken et1(db, 
		   LabelStr("P2"), 
		   true, 
		   IntervalIntDomain(0, 100), 
		   Token::noObject());

    et1.getObject()->specify(timeline.getId());
    et1.activate();
    timeline.constrain(et1.getId(), TokenId::noId());
    assert(it1.getEnd()->getDerivedDomain().getUpperBound() == 100);

    // Insert between a token and an event
    EventToken et2(db, 
		   LabelStr("P2"), 
		   true, 
		   IntervalIntDomain(0, 100), 
		   Token::noObject());

    et2.getObject()->specify(timeline.getId());
    et2.activate();
    timeline.constrain(et2.getId(), et1.getId());
    assert(it1.getEnd()->getDerivedDomain().getUpperBound() == 100);

    // Insert before a token
    EventToken et3(db, 
		   LabelStr("P2"), 
		   true, 
		   IntervalIntDomain(10, 100), 
		   Token::noObject());

    et3.getObject()->specify(timeline.getId());
    et3.activate();
    timeline.constrain(et3.getId(), it1.getId());
    assert(it1.getStart()->getDerivedDomain().getLowerBound() == 10);

    // Insert between events
    EventToken et4(db, 
		   LabelStr("P2"), 
		   true, 
		   IntervalIntDomain(0, 100), 
		   Token::noObject());

    et4.getObject()->specify(timeline.getId());
    et4.activate();
    timeline.constrain(et4.getId(), et1.getId());
    bool res = ce->propagate();
    assert(res);

    DEFAULT_TEARDOWN();
    return true;
  }

  static bool testFullInsertion(){
    DEFAULT_SETUP(ce, db, schema, false);
    Timeline timeline(db, ALL_OBJECTS, LabelStr("o2"));
    db->close();

    IntervalToken tokenA(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    IntervalToken tokenB(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    IntervalToken tokenC(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    assert(!timeline.hasTokensToOrder());
    tokenA.activate();
    tokenB.activate();
    tokenC.activate();

    timeline.constrain(tokenA.getId()); // Put A on the end.
    timeline.constrain(tokenB.getId()); // Put B on the end.
    assert(tokenA.getEnd()->getDerivedDomain().getUpperBound() <= tokenB.getStart()->getDerivedDomain().getUpperBound());

    // Now insert token C in the middle.
    timeline.constrain(tokenC.getId(), tokenB.getId());
    assert(tokenA.getEnd()->getDerivedDomain().getUpperBound() <= tokenC.getStart()->getDerivedDomain().getUpperBound());
    assert(tokenC.getEnd()->getDerivedDomain().getUpperBound() <= tokenB.getStart()->getDerivedDomain().getUpperBound());

    DEFAULT_TEARDOWN();
    return true;
  }

  static bool testNoChoicesThatFit(){
    DEFAULT_SETUP(ce, db, schema, false);
    Timeline timeline(db, ALL_OBJECTS , LabelStr("o2"));
    db->close();

    IntervalToken tokenA(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(10, 10),
		     IntervalIntDomain(20, 20),
		     IntervalIntDomain(1, 1000));

    IntervalToken tokenB(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(100, 100),
		     IntervalIntDomain(120, 120),
		     IntervalIntDomain(1, 1000));

    IntervalToken tokenC(db, 
		     LabelStr("P1"), 
		     true,
		     IntervalIntDomain(9, 9),
		     IntervalIntDomain(11, 11),
		     IntervalIntDomain(1, 1000));

    tokenA.activate();
    tokenB.activate();
    tokenC.activate();

    timeline.constrain(tokenA.getId());
    timeline.constrain(tokenB.getId());
    bool res = ce->propagate();
    assert(res);

    std::vector<TokenId> choices;
    timeline.getOrderingChoices(tokenC.getId(), choices);
    assert(choices.empty());
    timeline.constrain(tokenC.getId(), tokenB.getId());
    res = ce->propagate();
    assert(!res);

    DEFAULT_TEARDOWN();
    return(true);
  }
};

/**
 * Test class for testing client and factory
 */

class Foo;
typedef Id<Foo> FooId;

class Foo : public Timeline {
public:
  Foo(const PlanDatabaseId& planDatabase, const LabelStr& type, const LabelStr& name);
  Foo(const ObjectId& parent, const LabelStr& type, const LabelStr& name);
  void handleDefaults(bool autoClose = true); // default variable initialization
    
  // test/simple-predicate.nddl:4 Foo
  void constructor();
  void constructor(int arg0, LabelStr& arg1);
  Id< Variable< IntervalIntDomain > > m_0;
  Id< Variable< LabelSet > > m_1;
};

Foo::Foo(const PlanDatabaseId& planDatabase, const LabelStr& type, const LabelStr& name)
  : Timeline(planDatabase, type, name, true) {
}

Foo::Foo(const ObjectId& parent, const LabelStr& type, const LabelStr& name)
  : Timeline(parent, type, name, true) {}

// default initialization of member variables
void Foo::handleDefaults(bool autoClose) {
  if(m_0.isNoId()){
    check_error(!ObjectId::convertable(m_0)); // Object Variables must be explicitly initialized to a singleton
    m_0 = addVariable(IntervalIntDomain(), LabelStr("m_0"));
  }
  check_error(!m_1.isNoId()); // string variables must be initialized explicitly
  if(autoClose) close();
}

void Foo::constructor() {
  m_1 = addVariable(LabelSet(LabelStr("Hello World")), LabelStr(getName().toString() + "." + "m_1"));
}

void Foo::constructor(int arg0, LabelStr& arg1) {
  m_0 = addVariable(IntervalIntDomain(arg0), LabelStr(getName().toString() + "." + "m_0"));
  m_1 = addVariable(LabelSet(LabelStr("Hello World")), LabelStr(getName().toString() + "." + "m_1"));
}

class StandardFooFactory: public ConcreteObjectFactory{
public:
  StandardFooFactory(): ConcreteObjectFactory(LabelStr("Foo")){}

private:
  ObjectId createInstance(const PlanDatabaseId& planDb, 
			  const LabelStr& objectType, 
			  const LabelStr& objectName,
			  const std::vector<ConstructorArgument>& arguments) const {
    check_error(arguments.empty());
    FooId foo = (new Foo(planDb, objectType, objectName))->getId();
    foo->constructor();
    foo->handleDefaults();
    return foo;
  }
};

class SpecialFooFactory: public ConcreteObjectFactory{
public:
  SpecialFooFactory(): ConcreteObjectFactory(LabelStr("Foo:int:string")){}

private:
  ObjectId createInstance(const PlanDatabaseId& planDb, 
			  const LabelStr& objectType, 
			  const LabelStr& objectName,
			  const std::vector<ConstructorArgument>& arguments) const {
    FooId foo = (new Foo(planDb, objectType, objectName))->getId();
    // Type check the arguments
    assert(arguments.size() == 2);
    assert(arguments[0].first == LabelStr("int"));
    assert(arguments[1].first == LabelStr("string"));

    int arg0((int) arguments[0].second->getSingletonValue());
    LabelStr arg1(arguments[1].second->getSingletonValue());
    foo->constructor(arg0, arg1);
    foo->handleDefaults();
    return foo;
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
    DEFAULT_SETUP(ce, db, schema, false);

    DbClientId client = db->getClient();
    DbClientTransactionLog* txLog = new DbClientTransactionLog(client);

    FooId foo1 = client->createObject(LabelStr("Foo"), LabelStr("foo1"));
    assert(foo1.isValid());

    std::vector<ConstructorArgument> arguments;
    IntervalIntDomain arg0(10);
    LabelSet arg1(LabelStr("Label"));
    arguments.push_back(ConstructorArgument(LabelStr("int"), &arg0)); 
    arguments.push_back(ConstructorArgument(LabelStr("string"), &arg1));
    FooId foo2 = client->createObject(LabelStr("Foo"), LabelStr("foo2"), arguments);
    assert(foo2.isValid());

    TokenId token = client->createToken(LabelStr("Foo"));
    assert(token.isValid());

    // Constrain the token duration
    client->createConstraint(LabelStr("SubsetOf"), token->getDuration(), IntervalIntDomain(100));
    std::vector<ConstrainedVariableId> scope;
    scope.push_back(token->getStart());
    scope.push_back(token->getDuration());
    client->createConstraint(LabelStr("eq"), scope);

    delete txLog;

    DEFAULT_TEARDOWN();
    return true;
  }

  static bool testPathBasedRetrieval(){
    DEFAULT_SETUP(ce, db, schema, true);
    TokenId t0 = db->getClient()->createToken(LabelStr("Foo"));
    t0->activate();

    TokenId t1 = db->getClient()->createToken(LabelStr("Foo"));
    t1->activate();

    TokenId t0_0 = (new IntervalToken(t0, 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(1, 1)))->getId();
    t0_0->activate();

    TokenId t0_1 = (new IntervalToken(t0, 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(1, 1)))->getId();
    t0_1->activate();

    TokenId t0_2 = (new IntervalToken(t0, 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(1, 1)))->getId();
    t0_2->activate();

    TokenId t1_0 = (new IntervalToken(t1, 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(1, 1)))->getId();
    t1_0->activate();

    TokenId t0_1_0 = (new EventToken(t0_1, 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(0, 1)))->getId();
    t0_1_0->activate();

    TokenId t0_1_1 = (new EventToken(t0_1, 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(0, 1)))->getId();
    t0_1_1->activate();

    // Test paths
    std::vector<int> path;
    path.push_back(0); // Start with the index of the token key in the path


    // Base case with just the root
    assert(db->getClient()->getTokenByPath(path) == t0);
    assert(db->getClient()->getPathByToken(t0).size() == 1);

    // Now test a more convoluted path
    path.push_back(1);
    path.push_back(1);
    assert(db->getClient()->getTokenByPath(path) == t0_1_1);

    path.clear();
    path = db->getClient()->getPathByToken(t0_1_1);
    assert(path.size() == 3);
    assert(path[0] == 0);
    assert(path[1] == 1);
    assert(path[2] == 1);


    // Negative tests
    path.push_back(100);
    assert(db->getClient()->getTokenByPath(path) == TokenId::noId());
    path[0] = 99999;
    assert(db->getClient()->getTokenByPath(path) == TokenId::noId());

    DEFAULT_TEARDOWN();
    return true;
  }
};

int main() {
  initConstraintLibrary();
  
  // Special designations for temporal relations
  REGISTER_NARY(EqualConstraint, "concurrent", "Default");
  REGISTER_NARY(LessThanEqualConstraint, "before", "Default");

  // Support for Token implementations
  REGISTER_NARY(AddEqualConstraint, "StartEndDurationRelation", "Default");
  REGISTER_NARY(ObjectTokenRelation, "ObjectTokenRelation", "Default");
  REGISTER_UNARY(SubsetOfConstraint, "Singleton", "Default");
  REGISTER_UNARY(ForceFailureConstraint, "ForceFailure", "Default");

  // This is now done in ConstraintEngine/test-support.cc::initConstraintLibrary()
  //   for ConstraintEngine/module-tests.cc::testArbitraryConstraints().
  // --wedgingt 2004 Mar 11
  //REGISTER_NARY(EqualConstraint, "eq", "Default");

  REGISTER_NARY(EqualConstraint, "EqualConstraint", "EquivalenceClass");

  // Allocate default schema initially so tests don't fail because of ID's
  SCHEMA;

  // Have to register factories for testing.
  new StandardFooFactory();
  new SpecialFooFactory();
  new IntervalTokenFactory();

  runTestSuite(ObjectTest::test);
  runTestSuite(TokenTest::test);
  runTestSuite(TimelineTest::test);
  runTestSuite(DbClientTest::test);
  std::cout << "Finished" << std::endl;
  ConstraintLibrary::purgeAll();
}
