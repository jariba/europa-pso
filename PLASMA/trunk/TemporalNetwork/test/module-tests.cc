#include "TestSupport.hh"
#include "TemporalNetwork.hh"
#include "TemporalPropagator.hh"
#include "STNTemporalAdvisor.hh"
#include "../PlanDatabase/TemporalAdvisor.hh"
#include "../PlanDatabase/Schema.hh"
#include "../PlanDatabase/PlanDatabase.hh"
#include "../PlanDatabase/DbLogger.hh"
#include "../ConstraintEngine/CeLogger.hh"
#include "ObjectTokenRelation.hh"

#include "IntervalToken.hh"
#include "Timeline.hh"
#include "../ConstraintEngine/IntervalIntDomain.hh"
#include "TokenTemporalVariable.hh"

#include <iostream>
#include <string>
#include <list>

#define DEFAULT_SETUP(ce, db, schema, autoClose) \
    ConstraintEngine ce; \
    Schema schema; \
    PlanDatabase db(ce.getId(), schema.getId()); \
    new DefaultPropagator(LabelStr("Default"), ce.getId()); \
    new TemporalPropagator(LabelStr("Temporal"), ce.getId()); \
    db.setTemporalAdvisor((new STNTemporalAdvisor(ce.getPropagatorByName(LabelStr("Temporal"))))->getId()); \
    Id<DbLogger> dbLId; \
    if (loggingEnabled()) { \
      new CeLogger(std::cout, ce.getId()); \
      dbLId = (new DbLogger(std::cout, db.getId()))->getId(); \
    } \
    if (autoClose) \
      db.close();

#define DEFAULT_TEARDOWN() \
    delete (DbLogger*) dbLId;

class TemporalNetworkTest {
public:
  static bool test(){
    runTest(testBasicAllocation);
    runTest(testTemporalConstraints);
    runTest(testFixForReversingEndpoints);
    return true;
  }

private:
  static bool testBasicAllocation(){
    check_error(g_noTime() != g_infiniteTime() && g_noTime() != -g_infiniteTime() && g_infiniteTime() != -g_infiniteTime());
    TemporalNetwork tn;
    TimepointId origin = tn.getOrigin();
    Time delta = g_noTime();
    Time epsilon = g_noTime();
    tn.getTimepointBounds(origin, delta, epsilon);
    check_error(delta == 0 && epsilon == 0);

    tn.calcDistanceBounds(origin, origin, delta, epsilon);
    check_error(delta == 0 && epsilon == 0);
    return true;
  }

  static bool testTemporalConstraints(){
    TemporalNetwork tn;
    TimepointId a_end = tn.addTimepoint();
    TimepointId b_start = tn.addTimepoint();
    TimepointId b_end = tn.addTimepoint();
    TimepointId c_start = tn.addTimepoint();
    TemporalConstraintId a_before_b = tn.addTemporalConstraint(a_end, b_start, 0, g_infiniteTime());
    TemporalConstraintId start_before_end = tn.addTemporalConstraint(b_start, b_end, 1, g_infiniteTime());
    TemporalConstraintId a_meets_c = tn.addTemporalConstraint(a_end, c_start, 0, 0);
    bool res = tn.isConsistent();
    check_error(res);

    Time dist_lb, dist_ub;
    tn.calcDistanceBounds(c_start, b_end, dist_lb, dist_ub);
    check_error(dist_lb > 0);

    // Force failure where b meets c
    TemporalConstraintId b_meets_c = tn.addTemporalConstraint(b_end, c_start, 0, 0);
    res = tn.isConsistent();
    check_error(!res);

    // Cleanup
    tn.removeTemporalConstraint(b_meets_c);
    tn.removeTemporalConstraint(a_meets_c);
    tn.removeTemporalConstraint(start_before_end);
    tn.removeTemporalConstraint(a_before_b);
    tn.deleteTimepoint(c_start);
    tn.deleteTimepoint(b_end);
    tn.deleteTimepoint(b_start);
    tn.deleteTimepoint(a_end);
    return true;
  }

  static bool testFixForReversingEndpoints(){
    TemporalNetwork tn;

    // Allocate timepoints
    TimepointId x = tn.getOrigin();
    TimepointId y = tn.addTimepoint();
    TimepointId z = tn.addTimepoint();

    TemporalConstraintId fromage = tn.addTemporalConstraint(x, y, (Time)0, g_infiniteTime());
    TemporalConstraintId tango = tn.addTemporalConstraint(y, x, 200, 200);

    bool res = tn.isConsistent();
    check_error(!res);

    tn.removeTemporalConstraint(fromage);
    tn.removeTemporalConstraint(tango);

    res = tn.isConsistent();
    check_error(res); // Consistency restored

    TemporalConstraintId c0 = tn.addTemporalConstraint(y, x, -200, g_infiniteTime());
    TemporalConstraintId c1 = tn.addTemporalConstraint(x, z, 0, g_infiniteTime());
    TemporalConstraintId c2 = tn.addTemporalConstraint(z, y, (Time)0, g_infiniteTime());
    TemporalConstraintId c3 = tn.addTemporalConstraint(x, y, 200, g_infiniteTime());

    res = tn.isConsistent();
    check_error(res);

    // Clean up
    tn.removeTemporalConstraint(c0);
    tn.removeTemporalConstraint(c1);
    tn.removeTemporalConstraint(c2);
    tn.removeTemporalConstraint(c3);
    tn.deleteTimepoint(y);
    tn.deleteTimepoint(z);
    return true;
  }
};

class TemporalPropagatorTest {
public:
  static bool test(){
    runTest(testBasicAllocation);
    runTest(testTemporalPropagation);
    runTest(testCanPrecede);
    runTest(testCanFitBetween);
    return true;
  }

private:

  static bool testBasicAllocation() {
    DEFAULT_SETUP(ce,db,schema,true);
    ce.propagate();
    DEFAULT_TEARDOWN();
    return true;
  }
  
  static bool testTemporalPropagation() {
    DEFAULT_SETUP(ce,db,schema,false);

    ObjectId timeline = (new Timeline(db.getId(), LabelStr("AllObjects"), LabelStr("o2")))->getId();
    check_error(!timeline.isNoId());

    db.close();

    IntervalToken t1(db.getId(),
    		     LabelStr("P1"), 
    		     true,
    		     IntervalIntDomain(0, 10),
    		     IntervalIntDomain(0, 20),
    		     IntervalIntDomain(1, 1000));

    t1.getDuration()->specify(IntervalIntDomain(5, 7));
    check_error(t1.getEnd()->getDerivedDomain().getLowerBound() == 5);
    check_error(t1.getEnd()->getDerivedDomain().getUpperBound() == 17);

    IntervalToken t2(db.getId(), 
    		     LabelStr("P2"), 
    		     true,
    		     IntervalIntDomain(0, 10),
    		     IntervalIntDomain(0, 20),
    		     IntervalIntDomain(1, 1000));

    //t2.getEnd()->specify(IntervalIntDomain(8, 10));

    std::vector<ConstrainedVariableId> temp;
    temp.push_back(t1.getEnd());
    temp.push_back(t2.getStart());

    ConstraintId beforeConstraint = ConstraintLibrary::createConstraint(LabelStr("before"),
                                                                        db.getConstraintEngine(),
                                                                        temp);
    check_error(!beforeConstraint.isNoId());

    check_error(t1.getStart()->getDerivedDomain().getLowerBound() == 0);
    check_error(t1.getStart()->getDerivedDomain().getUpperBound() == 5);
    check_error(t1.getEnd()->getDerivedDomain().getLowerBound() == 5);
    check_error(t1.getEnd()->getDerivedDomain().getUpperBound() == 10);
    check_error(t2.getStart()->getDerivedDomain().getLowerBound() == 5);
    check_error(t2.getStart()->getDerivedDomain().getUpperBound() == 10);
    check_error(t2.getEnd()->getDerivedDomain().getLowerBound() == 6);
    check_error(t2.getEnd()->getDerivedDomain().getUpperBound() == 20);
    DEFAULT_TEARDOWN();
    return true;
  }

  static bool testCanPrecede() {
    DEFAULT_SETUP(ce,db,schema,false);

    ObjectId timeline = (new Timeline(db.getId(), LabelStr("AllObjects"), LabelStr("o2")))->getId();
    check_error(!timeline.isNoId());

    db.close();
    
    IntervalToken first(db.getId(),
			LabelStr("P1"), 
			true,
			IntervalIntDomain(0, 100),
			IntervalIntDomain(0, 100),
			IntervalIntDomain(1, 1000));
    
    IntervalToken second(db.getId(),
			 LabelStr("P1"), 
			 true,
			 IntervalIntDomain(0, 100),
			 IntervalIntDomain(0, 100),
			 IntervalIntDomain(1, 1000));
    
    // check_error from propagator direcly
    check_error (((TemporalPropagatorId)ce.getPropagatorByName(LabelStr("Temporal")))->canPrecede(first.getEnd(), second.getStart()));
    check_error (((TemporalPropagatorId)ce.getPropagatorByName(LabelStr("Temporal")))->canPrecede(second.getEnd(), first.getStart()));

    // compute from advisor
    check_error (db.getTemporalAdvisor()->canPrecede(first.getId(),second.getId()));

    // restrict via specifying the domain

    IntervalIntDomain dom(21, 31);
    first.getStart()->specify(dom);
    first.getEnd()->specify(dom);

    IntervalIntDomain dom2(1, 20);
    second.getStart()->specify(dom2);
    second.getEnd()->specify(dom2);

    bool res = ce.propagate();
    check_error(res);
    
    // compute from propagator directly
    check_error (!((TemporalPropagatorId)ce.getPropagatorByName(LabelStr("Temporal")))->canPrecede(first.getEnd(), second.getStart()));
    check_error (((TemporalPropagatorId)ce.getPropagatorByName(LabelStr("Temporal")))->canPrecede(second.getEnd(), first.getStart()));
    // compute from advisor
    check_error (!db.getTemporalAdvisor()->canPrecede(first.getId(),second.getId()));
    
    second.getStart()->reset();
    second.getEnd()->reset();

    first.getStart()->reset();
    first.getEnd()->reset();

    // restrict via a constraint

    std::vector<ConstrainedVariableId> temp;
    temp.push_back(first.getEnd());
    temp.push_back(second.getStart());

    ConstraintId beforeConstraint = ConstraintLibrary::createConstraint(LabelStr("before"),
									db.getConstraintEngine(),
									temp);
    check_error(!beforeConstraint.isNoId());

    res = ce.propagate();
    check_error(res);
    
    // compute from propagator directly
    check_error (((TemporalPropagatorId)ce.getPropagatorByName(LabelStr("Temporal")))->canPrecede(first.getEnd(), second.getStart()));
    check_error (!((TemporalPropagatorId)ce.getPropagatorByName(LabelStr("Temporal")))->canPrecede(second.getEnd(), first.getStart()));

    // compute from advisor
    check_error (db.getTemporalAdvisor()->canPrecede(first.getId(),second.getId()));
    
    DEFAULT_TEARDOWN();
    return true;
  }

  static bool testCanFitBetween() {
    DEFAULT_SETUP(ce,db,schema,false);

    ObjectId timeline = (new Timeline(db.getId(), LabelStr("AllObjects"), LabelStr("o2")))->getId();
    check_error(!timeline.isNoId());

    db.close();

    IntervalToken token(db.getId(),
			LabelStr("P1"), 
			true,
			IntervalIntDomain(0, 10),
			IntervalIntDomain(0, 20),
			IntervalIntDomain(1, 1000));
    IntervalToken predecessor(db.getId(),
			      LabelStr("P1"), 
			      true,
			      IntervalIntDomain(0, 10),
			      IntervalIntDomain(0, 20),
			      IntervalIntDomain(1, 1000));
    IntervalToken successor(db.getId(),
			    LabelStr("P1"), 
			    true,
			    IntervalIntDomain(0, 10),
			    IntervalIntDomain(0, 20),
			    IntervalIntDomain(1, 1000));

    // compute from propagator directly
    check_error (((TemporalPropagatorId)ce.getPropagatorByName(LabelStr("Temporal")))->canFitBetween(token.getStart(), token.getEnd(), predecessor.getEnd(), successor.getStart()));

    // compute from advisor
    check_error (db.getTemporalAdvisor()->canFitBetween(token.getId(), predecessor.getId(), successor.getId()));

    DEFAULT_TEARDOWN();
    return true;
  }

};

int main() {
  initConstraintLibrary();

  // Special designations for temporal relations
  // REGISTER_NARY(EqualConstraint, "concurrent", "Temporal");
  REGISTER_NARY(LessThanEqualConstraint, "before", "Temporal");
  REGISTER_NARY(AddEqualConstraint, "StartEndDurationRelation", "Temporal");
  REGISTER_NARY(ObjectTokenRelation, "ObjectTokenRelation", "Default");

  runTestSuite(TemporalNetworkTest::test);
  runTestSuite(TemporalPropagatorTest::test);
  std::cout << "Finished" << std::endl;
  ConstraintLibrary::purgeAll();
}
