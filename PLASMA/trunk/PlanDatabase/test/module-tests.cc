#include "PlanDatabase.hh"
#include "Schema.hh"
#include "Object.hh"
#include "ObjectSet.hh"
#include "StaticToken.hh"
#include "EventToken.hh"
#include "TokenVariable.hh"
#include "TokenVariableListener.hh"
#include "./ConstraintEngine/TestSupport.hh"
#include "./ConstraintEngine/IntervalIntDomain.hh"
#include "./ConstraintEngine/IntervalRealDomain.hh"
#include "./ConstraintEngine/LabelSet.hh"
#include "./ConstraintEngine/DefaultPropagator.hh"

#include <iostream>


#define DEFAULT_SETUP(ce, db, schema) \
    ConstraintEngine ce;\
    Schema schema;\
    PlanDatabase db(ce.getId(), schema.getId());\
    new DefaultPropagator(LabelStr("Default"), ce.getId());\
    Object object(db.getId(), LabelStr("AllObjects"), LabelStr("o1"));\
    db.close();

class ObjectTest {
public:
  static bool test(){
    runTest(testBasicAllocation, "BasicAllocation");
    runTest(testObjectSet, "ObjetSet");
    return true;
  }
private:
  static bool testBasicAllocation(){
    PlanDatabase db(ENGINE);
    Object o1(db.getId(), LabelStr("AllObjects"), LabelStr("o1"));
    Object o2(db.getId(), LabelStr("AllObjects"), LabelStr("o2"));
    ObjectId id0((new Object(o1.getId(), LabelStr("AllObjects"), LabelStr("id0")))->getId());
    Object o3(o2.getId(), LabelStr("AllObjects"), LabelStr("o3"));
    assert(db.getObjects().size() == 4);
    assert(o1.getComponents().size() == 1);
    assert(o3.getParent() == o2.getId());
    delete (Object*) id0;
    assert(db.getObjects().size() == 3);
    assert(o1.getComponents().empty());

    ObjectId id1((new Object(db.getId(), LabelStr("AllObjects"), LabelStr("id1")))->getId());
    ObjectId id2((new Object(id1, LabelStr("AllObjects"), LabelStr("id2")))->getId());
    ObjectId id3((new Object(id1, LabelStr("AllObjects"), LabelStr("id3")))->getId());
    assert(db.getObjects().size() == 6);
    assert(id3->getName().toString() == "id1:id3");
    delete (Object*) id1;
    assert(db.getObjects().size() == 3);
    return true;
  }

  static bool testObjectSet(){
    PlanDatabase db(ENGINE);
    std::list<ObjectId> values;
    Object o1(db.getId(), LabelStr("AllObjects"), LabelStr("o1"));
    Object o2(db.getId(), LabelStr("AllObjects"), LabelStr("o2"));
    assert(db.getObjects().size() == 2);
    values.push_back(o1.getId());
    values.push_back(o2.getId());
    ObjectSet os1(values, true);
    assert(os1.isMember(o1.getId()));
    os1.remove(o1.getId());
    assert(!os1.isMember(o1.getId()));
    assert(os1.isSingleton());
    return true;
  }
};

class TokenTest {
public:
  static bool test(){
    runTest(testBasicTokenAllocation, "BasicTokenAllocation");
    runTest(testMasterSlaveRelationship, "MasterSlaveRelationship");
    runTest(testBasicMerging, "BasicMerging");
    return true;
  }

private:
  static bool testBasicTokenAllocation(){
    DEFAULT_SETUP(ce, db, schema);

    // Static Token
    StaticToken staticToken(db.getId(), LabelStr("Predicate"), LabelStr("o1"));
    assert(staticToken.getStart()->getParent() == staticToken.getId());
    staticToken.addParameter(IntervalIntDomain(-1000, 2000));

    // Event Token
    EventToken eventToken(db.getId(), LabelStr("Predicate"), IntervalIntDomain(0, 1), IntervalIntDomain(0, 1000));
    assert(eventToken.getStart()->getDerivedDomain() == eventToken.getEnd()->getDerivedDomain());
    assert(eventToken.getDuration()->getDerivedDomain() == IntervalIntDomain(0, 0));
    eventToken.getStart()->specify(IntervalIntDomain(5, 10));
    assert(eventToken.getEnd()->getDerivedDomain() == IntervalIntDomain(5, 10));
    eventToken.addParameter(IntervalRealDomain(-1.08, 20.18));

    // IntervalToken
    IntervalToken intervalToken(db.getId(), 
				LabelStr("Predicate"), 
				IntervalIntDomain(0, 1), 
				IntervalIntDomain(0, 1000),
				IntervalIntDomain(0, 1000),
				IntervalIntDomain(2, 10));

    std::list<Prototype::LabelStr> values;
    values.push_back(Prototype::LabelStr("L1"));
    values.push_back(Prototype::LabelStr("L4"));
    values.push_back(Prototype::LabelStr("L2"));
    values.push_back(Prototype::LabelStr("L5"));
    values.push_back(Prototype::LabelStr("L3"));
    intervalToken.addParameter(LabelSet(values, true));

    assert(intervalToken.getEnd()->getDerivedDomain().getLowerBound() == 2);
    intervalToken.getStart()->specify(IntervalIntDomain(5, 10));
    assert(intervalToken.getEnd()->getDerivedDomain() == IntervalIntDomain(7, 20));
    intervalToken.getEnd()->specify(IntervalIntDomain(9, 10));
    assert(intervalToken.getStart()->getDerivedDomain() == IntervalIntDomain(5, 8));
    assert(intervalToken.getDuration()->getDerivedDomain() == IntervalIntDomain(2, 5));

    return true;
  }

  static bool testMasterSlaveRelationship(){
    DEFAULT_SETUP(ce, db, schema);

    IntervalToken t0(db.getId(), 
		     LabelStr("Predicate"), 
		     IntervalIntDomain(1, 1), 
		     IntervalIntDomain(0, 1),
		     IntervalIntDomain(0, 1),
		     IntervalIntDomain(1, 1));
    t0.activate();

    TokenId t1 = (new IntervalToken(db.getId(), 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(1, 1), 
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(1, 1)))->getId();
    t1->activate();

    TokenId t2 = (new IntervalToken(t0.getId(), 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(1, 1), 
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(1, 1)))->getId();

    TokenId t3 = (new IntervalToken(t0.getId(), 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(1, 1), 
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(1, 1)))->getId();

    TokenId t4 = (new IntervalToken(t0.getId(), 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(1, 1), 
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(1, 1)))->getId();

    TokenId t5 = (new IntervalToken(t1, 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(1, 1), 
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(0, 1),
				    IntervalIntDomain(1, 1)))->getId();

    TokenId t6 = (new EventToken(t0.getId(), 
				    LabelStr("Predicate"), 
				    IntervalIntDomain(1, 1),
				    IntervalIntDomain(0, 1)))->getId();

    // Delete slave only
    delete (Token*) t2;

    // Delete master & slaves
    delete (Token*) t1;

    // Remainder should be cleaned up automatically
    return true;
  }

  static bool testBasicMerging(){
    DEFAULT_SETUP(ce, db, scema);

    // Create 2 mergeable tokens - predicates, types and base domaiuns match
    IntervalToken t0(db.getId(), 
		     LabelStr("P1"), 
		     BooleanDomain(0, 1),
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    check_error(t0.getDuration()->getDerivedDomain().getUpperBound() == 20);

    IntervalToken t1(db.getId(),
		     LabelStr("P1"), 
		     BooleanDomain(0, 1),
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    t1.getDuration()->specify(IntervalIntDomain(5, 7));

    // Activate one and merge the other with it.
    t0.activate();
    t1.merge(t0.getId());

    // Make sure the necessary restrictions have been imposed due to merging i.e. restruction due to specified domain
    check_error(t0.getDuration()->getDerivedDomain().getUpperBound() == 7);
    check_error(t1.isMerged());

    // Do a split and make sure the old values are reinstated.
    t1.split();
    check_error(t0.getDuration()->getDerivedDomain().getUpperBound() == 20);
    check_error(t1.isInactive());

    // Now post equality constraint between t1 and extra token t2 and remerge
    IntervalToken t2(db.getId(), 
		     LabelStr("P2"), 
		     BooleanDomain(0, 1),
		     IntervalIntDomain(0, 10),
		     IntervalIntDomain(0, 20),
		     IntervalIntDomain(1, 1000));

    t2.getEnd()->specify(IntervalIntDomain(8, 10));

    std::vector<ConstrainedVariableId> temp;
    temp.push_back(t1.getEnd());
    temp.push_back(t2.getEnd());
    ConstraintId equalityConstraint = ConstraintLibrary::createConstraint(LabelStr("CoTemporal"),
									  db.getConstraintEngine(),
									  temp);
    t1.merge(t0.getId());

    // Verify that the equality constraint has migrated and original has been deactivated.
    check_error(!equalityConstraint->isActive());
    check_error(t0.getEnd()->getDerivedDomain().getLowerBound() == 8);
    check_error(t0.getEnd()->getDerivedDomain() == t2.getEnd()->getDerivedDomain());

    // Undo the merge and check for initial conditions being established
    t1.split();
    check_error(equalityConstraint->isActive());

    // Redo the merge
    t1.merge(t0.getId());

    // Confirm deletion of the constraint is handled correctly
    delete (Constraint*) equalityConstraint;
    check_error(t0.getEnd()->getDerivedDomain() != t2.getEnd()->getDerivedDomain());

    // Confirm previous restriction due to specified domain, then reset and note the change
    check_error(t0.getDuration()->getDerivedDomain().getUpperBound() == 7);
    t1.getDuration()->reset();
    check_error(t0.getDuration()->getDerivedDomain().getUpperBound() == 20);

    // Deletion will now occur and test proper cleanup.
    return true;
  }
};

void main(){
  initConstraintLibrary();
  
  REGISTER_NARY(EqualConstraint, "CoTemporal", "Default");
  REGISTER_NARY(AddEqualConstraint, "StartEndDurationRelation", "Default");
  REGISTER_NARY(TokenVariableListener, "ObjectRelation", "Default");
  REGISTER_NARY(TokenVariableListener, "ModelRulePropagation", "Default");

  runTestSuite(ObjectTest::test, "Object Tests");
  runTestSuite(TokenTest::test, "Token Tests");
  cout << "Finished" << endl;
}
