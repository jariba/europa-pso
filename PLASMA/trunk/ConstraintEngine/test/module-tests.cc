/**
 * @file module-tests.cc
 * @author Conor McGann
 * @date August, 2003
 * @brief Read the source for details
 */
#include "TestSupport.hh"
#include "Utils.hh"
#include "Variable.hh"
#include "Constraints.hh"
#include "ConstraintLibrary.hh"
#include "IdTable.hh"
#include "EquivalenceClassCollection.hh"
#include "EqualityConstraintPropagator.hh"

/* Include for domain management */
#include "AbstractDomain.hh"
#include "EnumeratedDomain.hh"
#include "LabelStr.hh"
#include "IntervalIntDomain.hh"
#include "BoolDomain.hh"
#include "StringDomain.hh"
#include "NumericDomain.hh"

#include "TypeFactory.hh"

#include "module-tests.hh"

#include <iostream>
#include <vector>
#include <string>

#include <fstream>

using namespace Prototype;
using namespace std;

class DelegationTestConstraint : public Constraint {
public:
  DelegationTestConstraint(const LabelStr& name,
			   const LabelStr& propagatorName,
			   const ConstraintEngineId& constraintEngine,
			   const std::vector<ConstrainedVariableId>& variables)
    : Constraint(name, propagatorName, constraintEngine, variables){s_instanceCount++;}
  ~DelegationTestConstraint(){s_instanceCount--;}
  void handleExecute(){s_executionCount++;}
  void handleExecute(const ConstrainedVariableId&,int, const DomainListener::ChangeType&){}
  bool canIgnore(const ConstrainedVariableId& variable, 
		 int argIndex, 
		 const DomainListener::ChangeType& changeType){
    if(changeType == DomainListener::SET)
      return true;
    return false;
  }

  static int s_executionCount;
  static int s_instanceCount;
};

int DelegationTestConstraint::s_executionCount = 0;
int DelegationTestConstraint::s_instanceCount = 0;

class TestListener: public ConstraintEngineListener{
public:
  TestListener(const ConstraintEngineId& ce):ConstraintEngineListener(ce){
    for (int i=0;i<ConstraintEngine::EVENT_COUNT;i++) m_events[i] = 0;
  }
  void notifyPropagationCommenced(){increment(ConstraintEngine::PROPAGATION_COMMENCED);}
  void notifyPropagationCompleted(){increment(ConstraintEngine::PROPAGATION_COMPLETED);}
  void notifyPropagationPreempted(){increment(ConstraintEngine::PROPAGATION_PREEMPTED);}
  void notifyAdded(const ConstraintId& constraint){increment(ConstraintEngine::CONSTRAINT_ADDED);}
  void notifyRemoved(const ConstraintId& constraint){increment(ConstraintEngine::CONSTRAINT_REMOVED);}
  void notifyExecuted(const ConstraintId& constraint){increment(ConstraintEngine::CONSTRAINT_EXECUTED);}
  void notifyAdded(const ConstrainedVariableId& variable){increment(ConstraintEngine::VARIABLE_ADDED);}
  void notifyRemoved(const ConstrainedVariableId& variable){increment(ConstraintEngine::VARIABLE_REMOVED);}
  void notifyChanged(const ConstrainedVariableId& variable, const DomainListener::ChangeType& changeType){increment(changeType);}

  int getCount(ConstraintEngine::Event event){return m_events[event];}
  void reset() {for(int i=0; i<ConstraintEngine::EVENT_COUNT;i++) m_events[i] = 0;}
private:
  void increment(int event){m_events[event] = m_events[event] + 1;}
  int m_events[ConstraintEngine::EVENT_COUNT];
};

class TypeFactoryTests {
public:
  static bool test(){
    runTest(testValueCreation);
    runTest(testDomainCreation);
    runTest(testVariableCreation);
    runTest(testVariableWithDomainCreation);
    return true; 
  }

  static bool testValueCreation(){
    IntervalIntDomain d0(5);
    int v0 = (int) TypeFactory::createValue(d0.getTypeName().c_str(), std::string("5"));
    assert(d0.compareEqual(d0.getSingletonValue(), v0));

    IntervalDomain d1(2.3);
    double v1 = (double) TypeFactory::createValue(d1.getTypeName().c_str(), std::string("2.3"));
    assert(d1.compareEqual(d1.getSingletonValue(), v1));

    BoolDomain d2(true);
    bool v2 = (bool) TypeFactory::createValue(d2.getTypeName().c_str(), std::string("true"));
    assert(d2.compareEqual(d2.getSingletonValue(), v2));

    return true;
  }

  static bool testDomainCreation(){
    const IntervalIntDomain & bd0 = dynamic_cast<const IntervalIntDomain &>(TypeFactory::baseDomain(IntervalIntDomain().getTypeName().c_str()));
    const IntervalDomain & bd1 = dynamic_cast<const IntervalDomain &>(TypeFactory::baseDomain(IntervalDomain().getTypeName().c_str()));
    const BoolDomain & bd2 = dynamic_cast<const BoolDomain &>(TypeFactory::baseDomain(BoolDomain().getTypeName().c_str()));
    return true;
  }

  static bool testVariableCreation(){
    ConstraintEngineId ce = (new ConstraintEngine())->getId();
    ConstrainedVariableId cv0 = TypeFactory::createVariable(IntervalIntDomain().getTypeName().c_str(), ce);
    assert(cv0->baseDomain().getType() == IntervalIntDomain().getType());
    ConstrainedVariableId cv1 = TypeFactory::createVariable(IntervalDomain().getTypeName().c_str(), ce);
    assert(cv1->baseDomain().getType() == IntervalDomain().getType());
    ConstrainedVariableId cv2 = TypeFactory::createVariable(BoolDomain().getTypeName().c_str(), ce);
    assert(cv2->baseDomain().getType() == BoolDomain().getType());
    Entity::purgeStarted();
    delete (ConstraintEngine*) ce;
    Entity::purgeEnded();
    return true;
  }

  static bool testVariableWithDomainCreation(){
    IntervalIntDomain d0(5);
    IntervalDomain d1(2.3);
    BoolDomain d2(true);

    ConstraintEngineId ce = (new ConstraintEngine())->getId();
    ConstrainedVariableId cv0 = TypeFactory::createVariable(d0.getTypeName().c_str(), ce, d0);
    assert(cv0->baseDomain() == d0);
    ConstrainedVariableId cv1 = TypeFactory::createVariable(d1.getTypeName().c_str(), ce, d1);
    assert(cv1->baseDomain() == d1);
    ConstrainedVariableId cv2 = TypeFactory::createVariable(d2.getTypeName().c_str(), ce, d2);
    assert(cv2->baseDomain() == d2);

    Entity::purgeStarted();
    delete (ConstraintEngine*) ce;
    Entity::purgeEnded();
    return true;
  }
};

class EntityTests {
public:
  static bool test(){
    return true;
  }
};

class ConstraintEngineTest
{
public:
  static bool test(){
    runTest(testDeallocationWithPurging);
    runTest(testInconsistentInitialVariableDomain);
    runTest(testVariableLookupByIndex);
    return true;
  }

  static bool testDeallocationWithPurging(){
    ConstraintEngineId ce = (new ConstraintEngine())->getId();
    new DefaultPropagator(LabelStr("Default"), ce);

    // Set up a base domain
    NumericDomain intBaseDomain;
    intBaseDomain.insert(1);
    intBaseDomain.insert(2);
    intBaseDomain.insert(3);
    intBaseDomain.insert(4);
    intBaseDomain.insert(5);
    intBaseDomain.close();

    for(int i=0;i<100;i++){
      Id<Variable<NumericDomain > > v0 = (new Variable<NumericDomain> (ce, intBaseDomain))->getId();
      Id<Variable<NumericDomain > > v1 = (new Variable<NumericDomain>(ce, intBaseDomain))->getId();
      new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), ce, makeScope(v0, v1));
    }

    assert(ce->propagate());
    Entity::purgeStarted();
    delete (ConstraintEngine*) ce;
    Entity::purgeEnded();

    return true;
  }

  static bool testInconsistentInitialVariableDomain(){
    NumericDomain emptyDomain;
    emptyDomain.close();
    {
      Variable<NumericDomain> v0(ENGINE, emptyDomain);
      assert(ENGINE->provenInconsistent()); // Should be immediately inconsistent!
    }
    assert(ENGINE->propagate()); // Should be fixed by deletion of the variable

    return true;
  }

  static bool testVariableLookupByIndex(){
    std::vector<ConstrainedVariableId> vars;

    for(unsigned int i=0;i<10;i++){
      ConstrainedVariableId var = (new Variable<IntervalIntDomain> (ENGINE, IntervalIntDomain()))->getId();
      assert(var == ENGINE->getVariable(i));
      assert(ENGINE->getIndex(var) == i);
      vars.push_back(var);
    }

    cleanup(vars);
    return true;
  }
};

class TestVariableListener: public ConstrainedVariableListener{
public:
  TestVariableListener(const ConstrainedVariableId& observedVar, const ConstrainedVariableId& managedVar)
    : ConstrainedVariableListener(observedVar), m_managedVar(managedVar){}
  ~TestVariableListener(){
    delete (ConstrainedVariable*) m_managedVar;
  }
private:
  ConstrainedVariableId m_managedVar;
};

class VariableTest
{
public:
  static bool test() {
    runTest(testAllocation);
    runTest(testMessaging);
    runTest(testDynamicVariable);
    runTest(testListener);
    return true;
  }

private:
  static bool testAllocation(){
    IntervalIntDomain dom0(0, 1000);
    Variable<IntervalIntDomain> v0(ENGINE, dom0);
    const IntervalIntDomain& dom1 = v0.getBaseDomain();
    assert(dom0 == dom1);
    assert(v0.isValid());
    assert(v0.canBeSpecified());

    // Now restrict the base domain
    IntervalIntDomain dom2(3, 10);
    v0.restrictBaseDomain(dom2);
    assert(v0.getDerivedDomain() == dom2);

    Variable<IntervalIntDomain> v1(ENGINE, dom1, false, LabelStr("TEST VARIABLE"));
    assert(!v1.canBeSpecified());
    assert(v1.getName() == LabelStr("TEST VARIABLE"));
    assert(v1.isValid());
    return true;
  }

  static bool testMessaging(){
    TestListener listener(ENGINE);

    // Add, Specify, Remove
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(0, 100));
      assert(listener.getCount(ConstraintEngine::SET) == 1);
      assert(listener.getCount(ConstraintEngine::VARIABLE_ADDED) == 1);
      v0.specify(IntervalIntDomain(3, 8));
      assert(listener.getCount(ConstraintEngine::SET) == 2);
      v0.specify(5);
      assert(listener.getCount(ConstraintEngine::SET) == 2);
      assert(listener.getCount(ConstraintEngine::SET_TO_SINGLETON) == 1);
    }
    assert(listener.getCount(ConstraintEngine::VARIABLE_REMOVED) == 1);

    // Bounds restriction messages for derived domain
    listener.reset();
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(0, 100));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(0, 10));
      EqualConstraint c0(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId()));
      ENGINE->propagate();
      assert(listener.getCount(ConstraintEngine::UPPER_BOUND_DECREASED) == 1);

      v0.specify(IntervalIntDomain(5, 10)); // Expect lower-bound-increased message
      assert(listener.getCount(ConstraintEngine::LOWER_BOUND_INCREASED) == 1);

      ENGINE->propagate(); // Expect another through propagation
      assert(listener.getCount(ConstraintEngine::LOWER_BOUND_INCREASED) == 2);

      v1.specify(IntervalIntDomain(6, 8)); // 
      assert(listener.getCount(ConstraintEngine::BOUNDS_RESTRICTED) == 1);

      ENGINE->propagate(); // Expect another through propagation
      assert(listener.getCount(ConstraintEngine::BOUNDS_RESTRICTED) == 2);

      v0.specify(7);
      ENGINE->propagate(); // Expect a RESTRICT_TO_SINGLETON event through propagation
      assert(listener.getCount(ConstraintEngine::RESTRICT_TO_SINGLETON) == 1);

      v0.reset(); // Expect a RESET message for v0 and a RELAXATION message for both variables
      assert(listener.getCount(ConstraintEngine::RESET) == 1);
      assert(listener.getCount(ConstraintEngine::RELAXED) == 2);
      assert(ENGINE->pending());

      v0.specify(0); // Expect EMPTIED
      ENGINE->propagate();
      assert(listener.getCount(ConstraintEngine::EMPTIED) == 1);
    }

    // Now tests message handling on Enumerated Domain
    listener.reset();
    {
      Variable<NumericDomain> v0(ENGINE, NumericDomain());
      v0.insert(1);
      v0.insert(3);
      v0.insert(5);
      v0.insert(10);
      assert(listener.getCount(ConstraintEngine::RELAXED) == 0); // Should not generate any of these messages while not closed
      v0.close();
      assert(listener.getCount(ConstraintEngine::CLOSED) == 1);
      assert(listener.getCount(ConstraintEngine::SET) == 1); // Expect to see specified domain cause 'set' on derived domain once closed.

      NumericDomain d0;
      d0.insert(2);
      d0.insert(3);
      d0.insert(5);
      d0.insert(11);
      d0.close();
      Variable<NumericDomain> v1(ENGINE, d0);
      assert(listener.getCount(ConstraintEngine::SET) == 2); // Expect to see specified domain cause 'set' immediately.

      EqualConstraint c0(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId()));
      ENGINE->propagate(); // Should see values removed from both variables domains. 
      assert(listener.getCount(ConstraintEngine::VALUE_REMOVED) == 2);
      v0.specify(3);
      assert(listener.getCount(ConstraintEngine::SET_TO_SINGLETON) == 1);
      v1.specify(5);
      assert(listener.getCount(ConstraintEngine::SET_TO_SINGLETON) == 2);
      ENGINE->propagate(); // Expect to see exactly one domain emptied
      assert(listener.getCount(ConstraintEngine::EMPTIED) == 1);
      v1.reset(); // Should now see 2 domains relaxed.
      assert(listener.getCount(ConstraintEngine::RELAXED) == 2);
    }

    return true;
  }

  static bool testDynamicVariable(){
    // Test empty on closure forces inconsistency
    {
      Variable<NumericDomain> v0(ENGINE,NumericDomain());
      assert(ENGINE->propagate()); // No reason to be inconsistent
      v0.close(); // Should push empty event.
      assert(ENGINE->provenInconsistent()); // Should be inconsistent
    }

    // Test that insertion is possible for variables and that it is handled correctly through propagation
    {
      Variable<NumericDomain> v0(ENGINE, NumericDomain()); // The empty one.
      Variable<NumericDomain> v1(ENGINE, NumericDomain()); // The full one
      v0.insert(1); // The only value, leave it open.

      // Fill up v1 and close it.
      v1.insert(1);
      v1.insert(2);
      v1.insert(3);
      v1.close();

      // Post equality constraint between v0 and v1. It should not cause any restriction yet
      // since v0 has not been closed      
      EqualConstraint c0(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId()));
      assert(ENGINE->propagate());

      // Now close v0, and we should see a restriction on v1
      v0.close();
      assert(v1.getDerivedDomain().isSingleton());

      // insert further, and propagate again, the domain should grow by one
      v0.insert(2);
      assert(v1.getDerivedDomain().getSize() == 2);

      // specify v0 to a singleton
      v0.specify(2);
      assert(v1.getDerivedDomain().isSingleton());

      // Now insert and confirm it does not affect v1.
      v0.insert(3);
      assert(v1.getDerivedDomain().isSingleton());

      // Now if we reset, the base domain will have 3 elements in it, so derived domain of v1 will also have 3 elements
      // in it since v0 should be relaxed to the base domain.
      v0.reset();
      assert(v1.getDerivedDomain().getSize() == 3);
    }

    return true;
  }

  static bool testListener(){
    ConstrainedVariableId v0 = (new Variable<IntervalIntDomain>(ENGINE, IntervalIntDomain()))->getId();
    ConstrainedVariableId v1 = (new Variable<IntervalIntDomain>(ENGINE, IntervalIntDomain()))->getId();
    new TestVariableListener(v0, v1);
    delete (ConstrainedVariable*) v0; // Should force deletion of all
    return true;
  }
};

class ConstraintTest
{
public:
  static bool test() {
    runTest(testSubsetConstraint);
    runTest(testAddEqualConstraint);
    runTest(testEqualConstraint);
    runTest(testLessThanEqualConstraint);
    runTest(testLessOrEqThanSumConstraint);
    runTest(testBasicPropagation);
    runTest(testForceInconsistency);
    runTest(testRepropagation);
    runTest(testConstraintRemoval);
    runTest(testDelegation);
    runTest(testNotEqual);
    runTest(testMultEqualConstraint);
    runTest(testAddMultEqualConstraint);
    runTest(testEqualSumConstraint);
    runTest(testCondAllSameConstraint);
    runTest(testCondAllDiffConstraint);
    runTest(testConstraintDeletion);
    runTest(testArbitraryConstraints);
    runTest(testLockConstraint);
    runTest(testNegateConstraint);
    runTest(testUnaryQuery);
    runTest(testTestEqConstraint);
    return(true);
  }

private:
  static bool testSubsetConstraint() {
    std::list<double> values;
    values.push_back(Prototype::LabelStr("A"));
    values.push_back(Prototype::LabelStr("B"));
    values.push_back(Prototype::LabelStr("C"));
    values.push_back(Prototype::LabelStr("D"));
    values.push_back(Prototype::LabelStr("E"));
    LabelSet ls0(values);
    values.pop_back();
    values.pop_back();
    LabelSet ls1(values);
    assert(ls1.isSubsetOf(ls0));
    assert(!(ls1 == ls0));

    Variable<LabelSet> v0(ENGINE, ls0);
    LabelSet dom = v0.getDerivedDomain();
    assert(dom == ls0 && !(dom == ls1));
    Variable<LabelSet> v1(ENGINE, ls1);
    SubsetOfConstraint c0(LabelStr("SubsetOf"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId()));
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    assert(v0.getDerivedDomain() == ls1);
    assert(c0.executionCount() == 1);

    values.pop_back();
    LabelSet ls2(values);
    v0.specify(ls2);
    assert(!ENGINE->pending()); // No change expected since it is a restriction.
    assert(!(v0.getDerivedDomain() == ls1));
    assert(c0.executionCount() == 1);
    assert(ENGINE->constraintConsistent());
    v0.reset();
    assert(ENGINE->pending());
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    assert(v0.getDerivedDomain() == ls1);
    assert(c0.executionCount() == 2);
    
    return true;
  }

  static bool testAddEqualConstraint() {
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, 1));
      Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(0, 2));
      AddEqualConstraint c0(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      ENGINE->propagate();
      assert(ENGINE->constraintConsistent());
      assert(v0.getDerivedDomain().getSingletonValue() == 1);
      assert(v1.getDerivedDomain().getSingletonValue() == 1);
      assert(v2.getDerivedDomain().getSingletonValue() == 2);
    }
    /**
    // Test to force negative values where A + B == C and C < A. Focus on computation of B
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(9, 29));
      Variable<IntervalDomain> v1(ENGINE, IntervalDomain());
      Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(8, 28));
      AddEqualConstraint c0(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      ENGINE->propagate();
      assert(ENGINE->constraintConsistent());
      assert(v1.getDerivedDomain().getLowerBound() == -21);
      assert(v1.getDerivedDomain().getUpperBound() == -1);
    }

    // Another test to force negative values where A + B == C and C < A. Focus on computation of A and C.
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 30));
      Variable<IntervalDomain> v1(ENGINE, IntervalDomain(-30, -1));
      Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(0, 27));
      AddEqualConstraint c0(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      ENGINE->propagate();
      assert(ENGINE->constraintConsistent());
      assert(v0.getDerivedDomain() == IntervalIntDomain(3, 30));
      assert(v1.getDerivedDomain() == IntervalDomain(-30, -3));
      assert(v2.getDerivedDomain() == IntervalIntDomain(0, 27));
    }
    **/

    // Now test mixed types
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
      Variable<IntervalDomain> v1(ENGINE, IntervalDomain(1.2, 2.8));
      Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(0, 3));
      AddEqualConstraint c0(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      ENGINE->propagate();
      assert(ENGINE->constraintConsistent());
      assert(v0.getDerivedDomain().getSingletonValue() == 1);
      assert(v1.getDerivedDomain().getSingletonValue() == 2);
      assert(v2.getDerivedDomain().getSingletonValue() == 3);
    }

    // Now test special case of rounding with a singleton
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(0, 10));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(0, 10));
      Variable<IntervalDomain> v2(ENGINE, IntervalDomain(0.5, 0.5));
      AddEqualConstraint c0(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      ENGINE->propagate();
      assert(ENGINE->provenInconsistent());
    }

    // Now test special case of rounding with negative domain bounds.
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(-10, 10));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(-10, 10));
      Variable<IntervalDomain> v2(ENGINE, IntervalDomain(0.01, 0.99));
      AddEqualConstraint c0(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      bool res = ENGINE->propagate();
      assert(!res);
    }

    // Another, similar, case of rounding with negative domain bounds.
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(-10, 10));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(-10, 10));
      Variable<IntervalDomain> v2(ENGINE, IntervalDomain(0.01, 1.99));
      AddEqualConstraint c0(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      bool res = ENGINE->propagate();
      assertTrue(res);
      // Require correct result to be in v2's domain.
      assertTrue(v2.getDerivedDomain().isMember(1.0));
      // Following is false because implementation of AddEqualConstraint is not smart enough to deduce it.
      //assertTrue(v2.getDerivedDomain().getSingletonValue() == 1.0);
    }

    // Confirm correct result with all singletons.
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(-1, -1));
      Variable<IntervalDomain> v1(ENGINE, IntervalDomain(10.4, 10.4));
      Variable<IntervalDomain> v2(ENGINE, IntervalDomain(9.4, 9.4));
      AddEqualConstraint c0(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      bool res = ENGINE->propagate();
      assert(res);
    }

    // Confirm inconsistency detected with all singletons.
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(-1, -1));
      Variable<IntervalDomain> v1(ENGINE, IntervalDomain(10.4, 10.4));
      Variable<IntervalDomain> v2(ENGINE, IntervalDomain(9.39, 9.39));
      AddEqualConstraint c0(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      bool res = ENGINE->propagate();
      assert(!res);
    }

    // Obtain factors correct values for fixed result.
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(0, PLUS_INFINITY));
      Variable<IntervalDomain> v1(ENGINE, IntervalDomain(0, PLUS_INFINITY));
      Variable<IntervalDomain> v2(ENGINE, IntervalDomain(9.390, 9.390));
      AddEqualConstraint c0(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      bool res = ENGINE->propagate();
      assert(res);
      assert(v0.getDerivedDomain() == IntervalIntDomain(0, 9));
      assert(v1.getDerivedDomain() == IntervalDomain(0.39, 9.39));
    }

    // Test handling with all infinites
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(MINUS_INFINITY, MINUS_INFINITY));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, PLUS_INFINITY));
      Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(PLUS_INFINITY, PLUS_INFINITY));
      AddEqualConstraint c0(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      bool res = ENGINE->propagate();
      assert(res);
      assert(v0.getDerivedDomain() == IntervalIntDomain(MINUS_INFINITY, MINUS_INFINITY));
      assert(v1.getDerivedDomain() == IntervalIntDomain(1, PLUS_INFINITY));
      assert(v2.getDerivedDomain() == IntervalIntDomain(PLUS_INFINITY, PLUS_INFINITY));
    }

    // Test handling with infinites and non-infinites
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(10, PLUS_INFINITY));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, PLUS_INFINITY));
      Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(MINUS_INFINITY, 100));
      AddEqualConstraint c0(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      bool res = ENGINE->propagate();
      assert(res);
      assert(v0.getDerivedDomain() == IntervalIntDomain(10, 99));
      assert(v1.getDerivedDomain() == IntervalIntDomain(1, 90));
      assert(v2.getDerivedDomain() == IntervalIntDomain(11, 100));
    }

    // Test propagating infinites: start + duration == end.
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(MINUS_INFINITY, PLUS_INFINITY));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1));
      Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(MINUS_INFINITY, PLUS_INFINITY));
      AddEqualConstraint c0(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      bool res = ENGINE->propagate();
      assert(res);
      assert(v0.getDerivedDomain() == IntervalIntDomain(MINUS_INFINITY, PLUS_INFINITY));
      assert(v1.getDerivedDomain() == IntervalIntDomain(1));
      assert(v2.getDerivedDomain() == IntervalIntDomain(MINUS_INFINITY, PLUS_INFINITY));
    }

    return true;
  }

  static bool testEqualConstraint()
  {
    // Set up a base domain
    std::list<double> baseValues;
    baseValues.push_back(Prototype::LabelStr("A"));
    baseValues.push_back(Prototype::LabelStr("B"));
    baseValues.push_back(Prototype::LabelStr("C"));
    baseValues.push_back(Prototype::LabelStr("D"));
    baseValues.push_back(Prototype::LabelStr("E"));
    LabelSet baseDomain(baseValues);

    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(-100, 1));
    EqualConstraint c0(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId()));
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    assert(v0.getDerivedDomain().getSingletonValue() == 1);
    assert(v1.getDerivedDomain().getSingletonValue() == 1);

    LabelSet ls0(baseDomain);
    ls0.empty();
    ls0.insert(Prototype::LabelStr("A"));

    LabelSet ls1(baseDomain);
    ls1.empty();
    ls1.insert(Prototype::LabelStr("A"));
    ls1.insert(Prototype::LabelStr("B"));
    ls1.insert(Prototype::LabelStr("C"));
    ls1.insert(Prototype::LabelStr("D"));
    ls1.insert(Prototype::LabelStr("E"));

    Variable<LabelSet> v2(ENGINE, ls1);
    Variable<LabelSet> v3(ENGINE, ls1);
    EqualConstraint c1(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v2.getId(), v3.getId()));
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    assert(v2.getDerivedDomain() == v3.getDerivedDomain());
    assert(!v2.getDerivedDomain().isSingleton());

    LabelSet ls2(ls1);
    ls2.remove(Prototype::LabelStr("E"));

    v2.specify(ls2);
    ENGINE->propagate();
    assert(!v3.getDerivedDomain().isMember(Prototype::LabelStr("E")));

    Variable<LabelSet> v4(ENGINE, ls0);
    EqualConstraint c2(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v2.getId(), v4.getId()));
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    assert(v2.getDerivedDomain() == v3.getDerivedDomain());
    assert(v2.getDerivedDomain() == v4.getDerivedDomain());
    assert(v3.getDerivedDomain() == v4.getDerivedDomain());
    assert(v3.getDerivedDomain().getSingletonValue() == Prototype::LabelStr("A"));

    // Now test that equality is working correctly for dynamic domains
    {
      NumericDomain e0;
      e0.insert(1);
      e0.insert(2);
      e0.insert(3);

      NumericDomain e1;
      e1.insert(1);
      e1.insert(2);
      e1.insert(3);
      e1.insert(4);

      NumericDomain e2;
      e2.insert(5);
      // Leave domains dynamic

      Variable<NumericDomain> a(ENGINE, e0);
      Variable<NumericDomain> b(ENGINE, e1);
      Variable<NumericDomain> c(ENGINE, e2);
      EqualConstraint eq(LabelStr("EqualConstraint"), 
			 LabelStr("Default"), 
			 ENGINE, 
			 makeScope(a.getId(), b.getId(), c.getId()));
      assert(ENGINE->propagate());

      // Now close one only. Should not change anything else.
      b.close();
      assert(b.lastDomain().getSize() == 4);
      assert(ENGINE->propagate());

      // Close another, should see partial restriction
      a.close();
      assert(a.lastDomain().getSize() == 3);
      assert(ENGINE->propagate());
      assert(a.lastDomain().getSize() == 3);
      assert(b.lastDomain().getSize() == 3);

      // By closing the final variables domain
      c.close();
      assert(!ENGINE->propagate());
    }

    // Create a fairly large multi-variable test that will ensure we handle the need for 2 passes.
    {
      Variable<IntervalIntDomain> a(ENGINE, IntervalIntDomain(0, 100));
      Variable<IntervalIntDomain> b(ENGINE, IntervalIntDomain(10, 90));
      Variable<IntervalIntDomain> c(ENGINE, IntervalIntDomain(20, 80));
      Variable<IntervalIntDomain> d(ENGINE, IntervalIntDomain(30, 70));
      std::vector<ConstrainedVariableId> scope;
      scope.push_back(a.getId());
      scope.push_back(b.getId());
      scope.push_back(c.getId());
      scope.push_back(d.getId());
      EqualConstraint eq(LabelStr("EqualConstraint"), 
			 LabelStr("Default"), 
			 ENGINE, 
			 scope);
      assert(ENGINE->propagate());
      assert(a.lastDomain() == IntervalIntDomain(30, 70));
    }
    return true;
  }

  static bool testLessThanEqualConstraint() {
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 100));
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, 100));
    LessThanEqualConstraint c0(LabelStr("LessThanEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId()));
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    assert(v0.getDerivedDomain() == v1.getDerivedDomain());

    v0.specify(IntervalIntDomain(50, 100));
    assert(v1.getDerivedDomain().getLowerBound() == 50);
    IntervalIntDomain copy(v1.getDerivedDomain());
    v0.specify(IntervalIntDomain(50, 80));
    assert(v1.getDerivedDomain() == copy);
    v1.specify(IntervalIntDomain(60, 70));
    assert(v0.getDerivedDomain() == IntervalIntDomain(50, 70));
    v1.reset();
    assert(v0.getDerivedDomain() == IntervalIntDomain(50, 80));
    assert(v1.getDerivedDomain() == IntervalIntDomain(50, 100));

    // Handle propagation of infinities
    Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(2, PLUS_INFINITY));
    Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain(MINUS_INFINITY, 100));
    LessThanEqualConstraint c2(LabelStr("LessThanEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v2.getId(), v3.getId()));
    bool res = ENGINE->propagate();
    assert(res);
    assert(v2.getDerivedDomain().getUpperBound() == 100);
    assert(v3.getDerivedDomain().getLowerBound() == 2);

    // Handle restriction to singleton
    Variable<IntervalIntDomain> v4(ENGINE, IntervalIntDomain(0, 10));
    Variable<IntervalIntDomain> v5(ENGINE, IntervalIntDomain(5, 15));
    Variable<IntervalIntDomain> v6(ENGINE, IntervalIntDomain(0, 100));
    LessThanEqualConstraint c3(LabelStr("LessThanEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v4.getId(), v5.getId()));
    EqualConstraint c4(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v5.getId(), v6.getId()));
    res = ENGINE->propagate();
    assert(res);
    v6.specify(9);
    res = ENGINE->propagate();
    assert(res);
    assert(v4.getDerivedDomain().getUpperBound() == 9);

    return(true);
  }

  static bool testLessOrEqThanSumConstraint() {
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(0, 100));
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(0, 100));
    Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(0, 100));
    LessOrEqThanSumConstraint c0(LabelStr("LessOrEqThanSumConstraint"), LabelStr("Default"), ENGINE,
                                 makeScope(v0.getId(), v1.getId(), v2.getId()));
    bool res = ENGINE->propagate();
    assert(res);
    assert(ENGINE->constraintConsistent());
    assert(v0.getDerivedDomain() == v1.getDerivedDomain());
    assert(v1.getDerivedDomain() == v2.getDerivedDomain());

    v1.specify(IntervalIntDomain(0, 50));
    v2.specify(IntervalIntDomain(0, 40));
    assert(v0.getDerivedDomain() == IntervalIntDomain(0, 90));
    v0.specify(IntervalIntDomain(60, 70));
    assert(v1.getDerivedDomain() == IntervalIntDomain(20, 50));
    assert(v2.getDerivedDomain() == IntervalIntDomain(10, 40));
    v1.reset();
    assert(v0.getDerivedDomain() == IntervalIntDomain(60, 70));
    assert(v1.getDerivedDomain() == IntervalIntDomain(20, 100));
    assert(v2.getDerivedDomain() == IntervalIntDomain(0, 40));

    // @todo Lots more ...

    return(true);
  }

  static bool testBasicPropagation(){
    // v0 == v1
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, 10));
    EqualConstraint c0(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId()));

    // v2 + v3 == v0
    Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(1, 4));
    Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain(1, 1));
    AddEqualConstraint c1(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v2.getId(), v3.getId(), v0.getId()));
    assert(!v0.getDerivedDomain().isEmpty());

    // v4 + v5 == v1
    Variable<IntervalIntDomain> v4(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v5(ENGINE, IntervalIntDomain(1, 1000));
    AddEqualConstraint c2(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v4.getId(), v5.getId(), v1.getId()));

    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    assert(!v4.getDerivedDomain().isEmpty());
    return true;
  }

  static bool testForceInconsistency(){
    // v0 == v1
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, 10));
    EqualConstraint c0(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId()));
    
    // v2 + v3 == v0
    Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(1, 1));
    Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain(1, 1));
    AddEqualConstraint c1(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v2.getId(), v3.getId(), v0.getId()));

    // v4 + v5 == v1
    Variable<IntervalIntDomain> v4(ENGINE, IntervalIntDomain(2, 2));
    Variable<IntervalIntDomain> v5(ENGINE, IntervalIntDomain(2, 2));
    AddEqualConstraint c2(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v4.getId(), v5.getId(), v1.getId()));
    
    ENGINE->propagate();
    assert(ENGINE->provenInconsistent());
    assertTrue(v1.getDerivedDomain().isEmpty() || v2.getDerivedDomain().isEmpty());

    std::vector<ConstrainedVariableId> variables;
    variables.push_back(v0.getId());
    variables.push_back(v1.getId());
    variables.push_back(v2.getId());
    variables.push_back(v3.getId());
    variables.push_back(v4.getId());
    variables.push_back(v5.getId());

    int emptyCount(0);
    for(std::vector<ConstrainedVariableId>::iterator it = variables.begin(); it != variables.end(); ++it){
      Variable<IntervalIntDomain>* id = (Variable<IntervalIntDomain>*) (*it);
      if(id->lastDomain().isEmpty())
	emptyCount++;
    }
    assert(emptyCount == 1);
    return true;
  }

  static bool testRepropagation()
  {
    // v0 == v1
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, 10));
    EqualConstraint c0(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId()));


    // v2 + v3 == v0
    Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain(1, 10));
    AddEqualConstraint c1(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v2.getId(), v3.getId(), v0.getId()));

    // v4 + v5 == v1
    Variable<IntervalIntDomain> v4(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v5(ENGINE, IntervalIntDomain(1, 10));
    AddEqualConstraint c2(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v4.getId(), v5.getId(), v1.getId()));

    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    v0.specify(IntervalIntDomain(8, 10));
    v1.specify(IntervalIntDomain(2, 7));
    assert(ENGINE->pending());

    ENGINE->propagate();
    assert(ENGINE->provenInconsistent());

    v0.reset();
    assert(ENGINE->pending());
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());

    /* Call reset on a constraint consistent network - not sure one would want to do this. */
    v1.reset();
    assert(ENGINE->pending()); /* Strictly speaking we know it is not inconsistent here since all we have done is relax a previously
				  consistent network. However, we have to propagate to find the new derived domains based on relaxed
				  domains. */
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    return true;
  }

  static bool testConstraintRemoval()
  {
    std::vector<ConstrainedVariableId> variables;
    // v0 == v1
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
    variables.push_back(v0.getId());
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, 10));
    variables.push_back(v1.getId());
    ConstraintId c0((new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, variables))->getId());

    // v2 + v3 == v0
    variables.clear();
    Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(1, 10));
    variables.push_back(v2.getId());
    Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain(1, 10));
    variables.push_back(v3.getId());
    variables.push_back(v0.getId());
    ConstraintId c1((new AddEqualConstraint(LabelStr("AddEqualConstraint"), LabelStr("Default"), ENGINE, variables))->getId());

    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());

    /* Show that we can simply delete a constraint and confirm that the system is still consistent. */
    delete (Constraint*) c1;
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());

    variables.clear();
    Variable<IntervalIntDomain> v4(ENGINE, IntervalIntDomain(1, 1));
    variables.push_back(v0.getId());
    variables.push_back(v4.getId());
    ConstraintId c2((new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, variables))->getId());
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    assert(v1.getDerivedDomain().getSingletonValue() == 1);

    delete (Constraint*) c2;
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    assert(v1.getDerivedDomain().getUpperBound() == 10);

    /* Add a constraint to force an inconsistency and show that consistency can be restored by removing the
     * constraint. */
    variables.clear();
    Variable<IntervalIntDomain> v5(ENGINE, IntervalIntDomain(0, 0));
    variables.push_back(v0.getId());
    variables.push_back(v5.getId());
    ConstraintId c3((new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("Default"), ENGINE, variables))->getId());
    ENGINE->propagate();
    assert(ENGINE->provenInconsistent());
    delete (Constraint*) c3;
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());

    // Clean up remaining constraint
    delete (Constraint*) c0;
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    return true;
  }

  static bool testDelegation(){
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(0, 1000));
    ConstraintId c0 = ConstraintLibrary::createConstraint(LabelStr("TestOnly"), ENGINE, makeScope(v0.getId()));
    ConstraintId c1 = ConstraintLibrary::createConstraint(LabelStr("TestOnly"), ENGINE, makeScope(v0.getId()));
    ConstraintId c2 = ConstraintLibrary::createConstraint(LabelStr("TestOnly"), ENGINE, makeScope(v0.getId()));
    ConstraintId c3 = ConstraintLibrary::createConstraint(LabelStr("TestOnly"), ENGINE, makeScope(v0.getId()));
    ConstraintId c4 = ConstraintLibrary::createConstraint(LabelStr("TestOnly"), ENGINE, makeScope(v0.getId()));
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    assert(DelegationTestConstraint::s_instanceCount == 5);
    assert(DelegationTestConstraint::s_executionCount == 5);

    // Cause a change in the domain which will impact agenda, then deactivate a constraint and verify the correct execution count
    v0.specify(IntervalIntDomain(0, 900));
    c0->deactivate();
    assert(!c0->isActive());
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    assert(DelegationTestConstraint::s_instanceCount == 5);
    assert(DelegationTestConstraint::s_executionCount == 9);

    // Delete the delegate and verify instance counts and that the prior delegate has been reinstated and executed.
    delete (Constraint*) c1;
    c0->activate();
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    assert(DelegationTestConstraint::s_instanceCount == 4);
    assert(DelegationTestConstraint::s_executionCount == 10);

    // Now create a new instance and mark it for delegation only. Add remaining constraints as delegates
    ConstraintId c5 = ConstraintLibrary::createConstraint(LabelStr("TestOnly"), ENGINE, makeScope(v0.getId()));
    c0->deactivate();
    c2->deactivate();
    c3->deactivate();
    c4->deactivate();
    assert(DelegationTestConstraint::s_instanceCount == 5);
    ENGINE->propagate();
    assert(DelegationTestConstraint::s_executionCount == 11);

    // Force propagation and confirm only one instance executes
    v0.specify(IntervalIntDomain(100, 900));
    ENGINE->propagate();
    assert(DelegationTestConstraint::s_executionCount == 12);

    // Now confirm correct handling of constraint deletions
    delete (Constraint*) c5;
    delete (Constraint*) c4;
    delete (Constraint*) c3;
    delete (Constraint*) c2;
    delete (Constraint*) c0;
    assert(DelegationTestConstraint::s_instanceCount == 0);
    return true;
  }

  static bool testNotEqual(){
    NumericDomain dom0;
    dom0.insert(1);
    dom0.insert(2);
    dom0.insert(3);
    dom0.close();

    Variable<NumericDomain> v0(ENGINE, dom0);
    Variable<NumericDomain> v1(ENGINE, dom0);
    Variable<NumericDomain> v2(ENGINE, dom0);

    // Test not equals among variables and singletons
    IntervalIntDomain dom1(1);
    IntervalIntDomain dom2(2);
    NumericDomain dom3;
    dom3.insert(1);
    dom3.insert(2);
    dom3.insert(3);
    dom3.close();
    Variable<IntervalIntDomain> v3(ENGINE, dom1); 
    Variable<NumericDomain> v4(ENGINE, dom3);
    Variable<IntervalIntDomain> v5(ENGINE, dom2); 

    NotEqualConstraint c4(LabelStr("neq"), LabelStr("Default"), ENGINE, makeScope(v4.getId(), v5.getId()));
    NotEqualConstraint c3(LabelStr("neq"), LabelStr("Default"), ENGINE, makeScope(v4.getId(), v3.getId()));
    assert(ENGINE->pending());
    bool res = ENGINE->propagate();
    assert(res);
    assert(v4.getDerivedDomain().isSingleton());
    assert(v4.getDerivedDomain().getSingletonValue() == 3);

    // Test not equals among variables which are not singletons

    NotEqualConstraint c0(LabelStr("neq"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId()));
    NotEqualConstraint c1(LabelStr("neq"), LabelStr("Default"), ENGINE, makeScope(v1.getId(), v2.getId()));
    NotEqualConstraint c2(LabelStr("neq"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v2.getId()));
    assert(ENGINE->pending());
    res = ENGINE->propagate();
    assert(res);

    dom0.remove(2);
    v0.specify(dom0);
    assert(!ENGINE->pending()); // No propagation required

    v1.specify(3);
    assert(ENGINE->pending());
    res = ENGINE->propagate();
    assert(res);
    assert(v0.getDerivedDomain().getSingletonValue() == 1);
    assert(v2.getDerivedDomain().getSingletonValue() == 2);

    v0.reset();
    assert(ENGINE->pending());
    res = ENGINE->propagate();
    assert(res);
    assert(v0.getDerivedDomain() == v2.getDerivedDomain());
    return true;
  }

  static bool testMultEqualConstraint(){
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, 1));
      Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(0, 2));
      MultEqualConstraint c0(LabelStr("MultEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      ENGINE->propagate();
      assert(ENGINE->constraintConsistent());
      assert(v0.getDerivedDomain() == v2.getDerivedDomain());
    }

    // Now test with 0 valued denominators and infinites
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(0, PLUS_INFINITY));
      Variable<IntervalDomain> v1(ENGINE, IntervalDomain(1, PLUS_INFINITY));
      Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(MINUS_INFINITY, 6));
      MultEqualConstraint c0(LabelStr("MultEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      ENGINE->propagate();
      assert(ENGINE->constraintConsistent());
      assert(v0.getDerivedDomain().getUpperBound() == 6);
      assert(v2.getDerivedDomain().getLowerBound() == 0);
      assert(v1.getDerivedDomain().getUpperBound() == PLUS_INFINITY);
    }


    // Special case of negative values on LHS
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(-4, 10));
      Variable<IntervalDomain> v1(ENGINE, IntervalDomain(1, 10));
      Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain());
      MultEqualConstraint c0(LabelStr("MultEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      ENGINE->propagate();
      assert(ENGINE->constraintConsistent());
      assert(v2.getDerivedDomain().getLowerBound() == -40);
    }

    // Part of test 157 of CLibTestCases, which incorrectly claimed to be
    // inconsistent during testing of a trial version of MultEqualConstraint.
    {
      Variable<IntervalDomain> v0(ENGINE, IntervalDomain(1.0));
      Variable<IntervalDomain> v1(ENGINE, IntervalDomain(MINUS_INFINITY, PLUS_INFINITY));
      Variable<IntervalDomain> v2(ENGINE, IntervalDomain(1.0, 2.0));
      MultEqualConstraint c0(LabelStr("MultEqualConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      ENGINE->propagate();
      assert(ENGINE->constraintConsistent());
      assert(v1.getDerivedDomain() == IntervalDomain(1.0, 2.0));
    }
    return true;
  }

  static bool testAddMultEqualConstraint() {
    // 1 + 2 * 3 == 7
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 1));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(2, 2));
      Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(3, 3));
      Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain(7, 7));
      AddMultEqualConstraint c0(LabelStr("AddMultEqualConstraint"), 
				LabelStr("Default"), 
				ENGINE, 
				makeScope(v0.getId(), v1.getId(), v2.getId(), v3.getId()));
      bool res = ENGINE->propagate();
      assert(res);
    }

    // 1 + 2 * 3 == 8 => empty
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 1));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(2, 2));
      Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(3, 3));
      Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain(8, 8));
      AddMultEqualConstraint c0(LabelStr("AddMultEqualConstraint"), 
				LabelStr("Default"), 
				ENGINE, 
				makeScope(v0.getId(), v1.getId(), v2.getId(), v3.getId()));
      bool res = ENGINE->propagate();
      assert(!res);
    }

    // 1 + 1 * [-infty 0] = 1 -> 1 + 1 * 0 = 1
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 1));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, 1));
      Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(MINUS_INFINITY, 0));
      Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain(1, 1));
      AddMultEqualConstraint c0(LabelStr("AddMultEqualConstraint"), 
				LabelStr("Default"), 
				ENGINE, 
				makeScope(v0.getId(), v1.getId(), v2.getId(), v3.getId()));
      bool res = ENGINE->propagate();
      assert(res);
      assert(v2.getDerivedDomain().getSingletonValue() == 0);
    }

    // [1.0 10.0] + 1.0 * [1.0 10.0] = 10.0 ->  [1.0 9.0] + 1.0 * [1.0 9.0] = 10.0
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
      Variable<IntervalDomain> v1(ENGINE, IntervalDomain(1.0, 1.0));
      Variable<IntervalDomain> v2(ENGINE, IntervalDomain(1.0, 10.0));
      Variable<IntervalDomain> v3(ENGINE, IntervalDomain(10.0, 10.0));
      AddMultEqualConstraint c0(LabelStr("AddMultEqualConstraint"), 
				LabelStr("Default"), 
				ENGINE, 
				makeScope(v0.getId(), v1.getId(), v2.getId(), v3.getId()));
      bool res = ENGINE->propagate();
      assert(res);
      assert(v0.getDerivedDomain() == IntervalIntDomain(1, 9));
      assert(v2.getDerivedDomain() == IntervalDomain(1.0, 9.0));
    }


    return true;
  }

  static bool testEqualSumConstraint() {
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, 1));
    Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(0, 2));
    Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> v4(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> v5(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> v6(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> v7(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> v8(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> v9(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> vA(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> vB(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> vC(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> vD(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> vE(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> vF(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> vG(ENGINE, IntervalIntDomain(0, 27));
    { // Duplicate first test case in testAddEqualConstraint(),
      //   but note args are in different order in scope to get same result
      EqualSumConstraint c0(LabelStr("EqualSumConstraint"), LabelStr("Default"), ENGINE, makeScope(v2.getId(), v0.getId(), v1.getId()));
      ENGINE->propagate();
      assert(ENGINE->constraintConsistent());
      assert(v0.getDerivedDomain().getSingletonValue() == 1);
      assert(v1.getDerivedDomain().getSingletonValue() == 1);
      assert(v2.getDerivedDomain().getSingletonValue() == 2);
    }
    { // Same, but add another variable that will be constrained to 0
      std::vector<ConstrainedVariableId> scope;
      scope.push_back(v2.getId());
      scope.push_back(v0.getId());
      scope.push_back(v1.getId());
      scope.push_back(v3.getId());
      EqualSumConstraint c0(LabelStr("EqualSumConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assert(ENGINE->constraintConsistent());
      assert(v0.getDerivedDomain().getSingletonValue() == 1);
      assert(v1.getDerivedDomain().getSingletonValue() == 1);
      assert(v2.getDerivedDomain().getSingletonValue() == 2);
      assert(v3.getDerivedDomain().getSingletonValue() == 0);
    }
    { // Same, but add more variables that will be constrained to 0
      std::vector<ConstrainedVariableId> scope;
      scope.push_back(v2.getId());
      scope.push_back(v0.getId());
      scope.push_back(v1.getId());
      scope.push_back(v3.getId());
      scope.push_back(v4.getId());
      scope.push_back(v5.getId());
      scope.push_back(v6.getId());
      scope.push_back(v7.getId());
      scope.push_back(v8.getId());
      scope.push_back(v9.getId());
      scope.push_back(vA.getId());
      scope.push_back(vB.getId());
      scope.push_back(vC.getId());
      scope.push_back(vD.getId());
      scope.push_back(vE.getId());
      scope.push_back(vF.getId());
      scope.push_back(vG.getId());
      EqualSumConstraint c0(LabelStr("EqualSumConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assert(ENGINE->constraintConsistent());
      assert(v0.getDerivedDomain().getSingletonValue() == 1);
      assert(v1.getDerivedDomain().getSingletonValue() == 1);
      assert(v2.getDerivedDomain().getSingletonValue() == 2);
      assert(v3.getDerivedDomain().getSingletonValue() == 0);
      assert(v4.getDerivedDomain().getSingletonValue() == 0);
      assert(v5.getDerivedDomain().getSingletonValue() == 0);
      assert(v6.getDerivedDomain().getSingletonValue() == 0);
      assert(v7.getDerivedDomain().getSingletonValue() == 0);
      assert(v8.getDerivedDomain().getSingletonValue() == 0);
      assert(v9.getDerivedDomain().getSingletonValue() == 0);
      assert(vA.getDerivedDomain().getSingletonValue() == 0);
      assert(vB.getDerivedDomain().getSingletonValue() == 0);
      assert(vC.getDerivedDomain().getSingletonValue() == 0);
      assert(vD.getDerivedDomain().getSingletonValue() == 0);
      assert(vE.getDerivedDomain().getSingletonValue() == 0);
      assert(vF.getDerivedDomain().getSingletonValue() == 0);
      assert(vG.getDerivedDomain().getSingletonValue() == 0);
    }
    return(true);
  }

  static bool testCondAllSameConstraint() {
    BoolDomain bothDom;
    Variable<BoolDomain> bothVar(ENGINE, bothDom);
    Variable<BoolDomain> falseVar(ENGINE, bothDom);
    falseVar.specify(false);
    Variable<BoolDomain> trueVar(ENGINE, bothDom);
    trueVar.specify(true);
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, 1));
    Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(0, 2));
    Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> v4(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> v5(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> v6(ENGINE, IntervalIntDomain(0));
    Variable<IntervalIntDomain> v7(ENGINE, IntervalIntDomain(0));
    Variable<IntervalIntDomain> v8(ENGINE, IntervalIntDomain(0));
    Variable<IntervalIntDomain> v9(ENGINE, IntervalIntDomain(1));
    Variable<IntervalIntDomain> vA(ENGINE, IntervalIntDomain(11, 27));
    std::vector<ConstrainedVariableId> scope;
    {
      scope.push_back(bothVar.getId());
      scope.push_back(v0.getId());
      scope.push_back(v1.getId());
      CondAllSameConstraint c0(LabelStr("CondAllSameConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(!bothVar.getDerivedDomain().isSingleton());
      assertTrue(v0.getDerivedDomain() == IntervalIntDomain(1, 10));
      assertTrue(v1.getDerivedDomain().getSingletonValue() == 1);
    }
    assertTrue(!bothVar.getDerivedDomain().isSingleton());
    scope.clear();
    {
      scope.push_back(bothVar.getId());
      scope.push_back(v0.getId());
      scope.push_back(vA.getId());
      CondAllSameConstraint c0(LabelStr("CondAllSameConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(bothVar.getDerivedDomain() == BoolDomain(false));
    }
    assertTrue(!bothVar.getDerivedDomain().isSingleton());
    scope.clear();
    {
      scope.push_back(bothVar.getId());
      scope.push_back(v6.getId());
      scope.push_back(v7.getId());
      scope.push_back(v8.getId());
      CondAllSameConstraint c0(LabelStr("CondAllSameConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(bothVar.getDerivedDomain() == BoolDomain(true));
    }
    assertTrue(!bothVar.getDerivedDomain().isSingleton());
    scope.clear();
    {
      scope.push_back(bothVar.getId());
      scope.push_back(v2.getId());
      scope.push_back(v6.getId());
      scope.push_back(v7.getId());
      scope.push_back(v8.getId());
      CondAllSameConstraint c0(LabelStr("CondAllSameConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(!bothVar.getDerivedDomain().isSingleton());
    }
    assertTrue(!bothVar.getDerivedDomain().isSingleton());
    scope.clear();
    {
      scope.push_back(bothVar.getId());
      scope.push_back(v6.getId());
      scope.push_back(v7.getId());
      scope.push_back(v8.getId());
      scope.push_back(v2.getId());
      CondAllSameConstraint c0(LabelStr("CondAllSameConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(!bothVar.getDerivedDomain().isSingleton());
    }
    assertTrue(!bothVar.getDerivedDomain().isSingleton());
    scope.clear();
    {
      scope.push_back(bothVar.getId());
      scope.push_back(v6.getId());
      scope.push_back(v7.getId());
      scope.push_back(v2.getId());
      scope.push_back(v8.getId());
      CondAllSameConstraint c0(LabelStr("CondAllSameConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(!bothVar.getDerivedDomain().isSingleton());
    }
    assertTrue(!bothVar.getDerivedDomain().isSingleton());
    scope.clear();
    {
      scope.push_back(trueVar.getId());
      scope.push_back(v2.getId());
      scope.push_back(v6.getId());
      scope.push_back(v7.getId());
      CondAllSameConstraint c0(LabelStr("CondAllSameConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(v2.getDerivedDomain() == IntervalIntDomain(0));
    }
    scope.clear();
    {
      scope.push_back(trueVar.getId());
      scope.push_back(v2.getId());
      scope.push_back(v3.getId());
      scope.push_back(v6.getId());
      scope.push_back(v7.getId());
      CondAllSameConstraint c0(LabelStr("CondAllSameConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(v2.getDerivedDomain() == IntervalIntDomain(0));
      assertTrue(v3.getDerivedDomain() == IntervalIntDomain(0));
    }
    scope.clear();
    {
      scope.push_back(trueVar.getId());
      scope.push_back(v2.getId());
      scope.push_back(v3.getId());
      CondAllSameConstraint c0(LabelStr("CondAllSameConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(v2.getDerivedDomain() == IntervalIntDomain(0, 2));
      assertTrue(v3.getDerivedDomain() == IntervalIntDomain(0, 2));
    }
    scope.clear();
    {
      scope.push_back(trueVar.getId());
      scope.push_back(v1.getId());
      scope.push_back(v2.getId());
      scope.push_back(v3.getId());
      CondAllSameConstraint c0(LabelStr("CondAllSameConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(v2.getDerivedDomain() == IntervalIntDomain(1));
      assertTrue(v3.getDerivedDomain() == IntervalIntDomain(1));
    }
    assertTrue(v2.getDerivedDomain() == IntervalIntDomain(0, 2));
    assertTrue(v3.getDerivedDomain() == IntervalIntDomain(0, 27));
    scope.clear();
    {
      scope.push_back(falseVar.getId());
      scope.push_back(v3.getId());
      scope.push_back(v6.getId());
      scope.push_back(v7.getId());
      CondAllSameConstraint c0(LabelStr("CondAllSameConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(v3.getDerivedDomain().isSubsetOf(IntervalIntDomain(1, 27)));
    }
    scope.clear();
    {
      scope.push_back(falseVar.getId());
      scope.push_back(v6.getId());
      scope.push_back(v7.getId());
      CondAllSameConstraint c0(LabelStr("CondAllSameConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(!ENGINE->constraintConsistent());
    }
    ENGINE->propagate();
    assertTrue(ENGINE->constraintConsistent());
    scope.clear();
    {
      scope.push_back(falseVar.getId());
      scope.push_back(v2.getId());
      scope.push_back(v3.getId());
      CondAllSameConstraint c0(LabelStr("CondAllSameConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(v2.getDerivedDomain() == IntervalIntDomain(0, 2));
      assertTrue(v3.getDerivedDomain() == IntervalIntDomain(0, 27));
    }
    ENGINE->propagate();
    assertTrue(ENGINE->constraintConsistent());
    scope.clear();
    {
      scope.push_back(trueVar.getId());
      scope.push_back(v0.getId());
      scope.push_back(vA.getId());
      CondAllSameConstraint c0(LabelStr("CondAllSameConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(!ENGINE->constraintConsistent());
    }
    ENGINE->propagate();
    assertTrue(ENGINE->constraintConsistent());
    assertTrue(!bothVar.getDerivedDomain().isSingleton());
    scope.clear();
    return(true);
  }

  static bool testCondAllDiffConstraint() {
    BoolDomain bothDom;
    Variable<BoolDomain> bothVar(ENGINE, bothDom);
    Variable<BoolDomain> falseVar(ENGINE, bothDom);
    falseVar.specify(false);
    Variable<BoolDomain> trueVar(ENGINE, bothDom);
    trueVar.specify(true);
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, 1));
    Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(0, 2));
    Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> v4(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> v5(ENGINE, IntervalIntDomain(0, 27));
    Variable<IntervalIntDomain> v6(ENGINE, IntervalIntDomain(0));
    Variable<IntervalIntDomain> v7(ENGINE, IntervalIntDomain(0));
    Variable<IntervalIntDomain> v8(ENGINE, IntervalIntDomain(0));
    Variable<IntervalIntDomain> v9(ENGINE, IntervalIntDomain(1));
    Variable<IntervalIntDomain> vA(ENGINE, IntervalIntDomain(11, 27));
    Variable<IntervalIntDomain> vB(ENGINE, IntervalIntDomain(-1, 2));
    Variable<IntervalIntDomain> vC(ENGINE, IntervalIntDomain(10, 11));
    Variable<IntervalIntDomain> vD(ENGINE, IntervalIntDomain(10, 11));
    Variable<IntervalIntDomain> vE(ENGINE, IntervalIntDomain(10, 11));
    Variable<IntervalIntDomain> vF(ENGINE, IntervalIntDomain(0, 3));
    Variable<IntervalIntDomain> vG(ENGINE, IntervalIntDomain(9, 12));
    Variable<IntervalIntDomain> vH(ENGINE, IntervalIntDomain(3, 5));
    Variable<IntervalIntDomain> vI(ENGINE, IntervalIntDomain(0, 7));
    Variable<IntervalIntDomain> vJ(ENGINE, IntervalIntDomain(5, 9));
    Variable<IntervalIntDomain> vK(ENGINE, IntervalIntDomain(9));
    Variable<IntervalIntDomain> vL(ENGINE, IntervalIntDomain(5, 13));
    Variable<IntervalIntDomain> vM(ENGINE, IntervalIntDomain(5));
    Variable<IntervalIntDomain> vN(ENGINE, IntervalIntDomain(-1, 15));
    Variable<IntervalIntDomain> vO(ENGINE, IntervalIntDomain(-2, 6));
    Variable<IntervalIntDomain> vP(ENGINE, IntervalIntDomain(-2));
    Variable<IntervalIntDomain> vQ(ENGINE, IntervalIntDomain(15));
    std::vector<ConstrainedVariableId> scope;
    {
      scope.push_back(bothVar.getId());
      scope.push_back(v0.getId());
      scope.push_back(v1.getId());
      CondAllDiffConstraint c0(LabelStr("CondAllDiffConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(!bothVar.getDerivedDomain().isSingleton());
      assertTrue(v0.getDerivedDomain() == IntervalIntDomain(1, 10));
      assertTrue(v1.getDerivedDomain().getSingletonValue() == 1);
    }
    scope.clear();
    {
      scope.push_back(bothVar.getId());
      scope.push_back(v0.getId());
      scope.push_back(v6.getId());
      scope.push_back(vA.getId());
      CondAllDiffConstraint c0(LabelStr("CondAllDiffConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(bothVar.getDerivedDomain().getSingletonValue());
      assertTrue(v0.getDerivedDomain() == IntervalIntDomain(1, 10));
      assertTrue(v6.getDerivedDomain().getSingletonValue() == 0);
      assertTrue(vA.getDerivedDomain() == IntervalIntDomain(11, 27));
    }
    assertTrue(!bothVar.getDerivedDomain().isSingleton());
    scope.clear();
    {
      scope.push_back(bothVar.getId());
      scope.push_back(v0.getId());
      scope.push_back(v6.getId());
      scope.push_back(v9.getId());
      scope.push_back(vA.getId());
      CondAllDiffConstraint c0(LabelStr("CondAllDiffConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(!bothVar.getDerivedDomain().isSingleton());
      assertTrue(v0.getDerivedDomain() == IntervalIntDomain(1, 10));
      assertTrue(v6.getDerivedDomain().getSingletonValue() == 0);
      assertTrue(v9.getDerivedDomain().getSingletonValue() == 1);
      assertTrue(vA.getDerivedDomain() == IntervalIntDomain(11, 27));
    }
    scope.clear();
    {
      scope.push_back(trueVar.getId());
      scope.push_back(v0.getId()); // 1 10 so far
      scope.push_back(v2.getId()); // 1 10, 0 2
      scope.push_back(v6.getId()); // 1 10, 1 2, 0
      scope.push_back(v9.getId()); // 3 10, 2, 0, 1
      scope.push_back(vA.getId()); // 3 10, 2, 0, 1, 11 27
      scope.push_back(vB.getId()); // 3 10, 2, 0, 1, 11 27, -1
      CondAllDiffConstraint c0(LabelStr("CondAllDiffConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(trueVar.getDerivedDomain().isSingleton());
      assertTrue(v0.getDerivedDomain() == IntervalIntDomain(3, 10));
      assertTrue(v2.getDerivedDomain().getSingletonValue() == 2);
      assertTrue(v6.getDerivedDomain().getSingletonValue() == 0);
      assertTrue(v9.getDerivedDomain().getSingletonValue() == 1);
      assertTrue(vA.getDerivedDomain() == IntervalIntDomain(11, 27));
      assertTrue(vB.getDerivedDomain().getSingletonValue() == -1);
    }
    assertTrue(v0.getDerivedDomain() == IntervalIntDomain(1, 10));
    assertTrue(v2.getDerivedDomain() == IntervalIntDomain(0, 2));
    assertTrue(vB.getDerivedDomain() == IntervalIntDomain(-1, 2));
    scope.clear();
    {
      scope.push_back(falseVar.getId());
      scope.push_back(v0.getId()); // 1 10
      scope.push_back(v6.getId()); // 1 10, 0 (inconsistent)
      scope.push_back(v9.getId()); // 1, 0, 1
      scope.push_back(vA.getId()); // 1, 0, 1, 11 27
      CondAllDiffConstraint c0(LabelStr("CondAllDiffConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      // This next restriction is correct but the current implementation
      //   does not enforce it, as it requires checking all pairs to see
      //   that two individual vars are the only ones that overlap and
      //   therefore must be equal.
      // assertTrue(v0.getDerivedDomain().getSingletonValue() == 1);
      assertTrue(v6.getDerivedDomain().getSingletonValue() == 0);
      assertTrue(v9.getDerivedDomain().getSingletonValue() == 1);
      assertTrue(vA.getDerivedDomain() == IntervalIntDomain(11, 27));
    }
    scope.clear();
    {
      scope.push_back(bothVar.getId());
      scope.push_back(v6.getId()); // 0
      scope.push_back(v7.getId()); // 0, 0 -> false; rest irrelevant
      scope.push_back(v8.getId());
      scope.push_back(v9.getId());
      scope.push_back(vA.getId());
      CondAllDiffConstraint c0(LabelStr("CondAllDiffConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(!bothVar.getDerivedDomain().getSingletonValue());
      assertTrue(v6.getDerivedDomain() == IntervalIntDomain(0));
      assertTrue(v7.getDerivedDomain() == IntervalIntDomain(0));
      assertTrue(v8.getDerivedDomain() == IntervalIntDomain(0));
      assertTrue(v9.getDerivedDomain() == IntervalIntDomain(1));
      assertTrue(vA.getDerivedDomain() == IntervalIntDomain(11, 27));
    }
    assertTrue(!bothVar.getDerivedDomain().isSingleton());
    scope.clear();
    {
      scope.push_back(bothVar.getId());
      scope.push_back(vC.getId()); // 10 11
      scope.push_back(vD.getId()); // 10 11, 10 11
      CondAllDiffConstraint c0(LabelStr("CondAllDiffConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(!bothVar.getDerivedDomain().isSingleton());
    }
    scope.clear();
    {
      scope.push_back(bothVar.getId());
      scope.push_back(vC.getId()); // 10 11
      scope.push_back(vD.getId()); // 10 11, 10 11
      scope.push_back(vE.getId()); // 10 11, 10 11, 10 11 -> false: three vars but only two values
      CondAllDiffConstraint c0(LabelStr("CondAllDiffConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(!bothVar.getDerivedDomain().getSingletonValue());
    }
    assertTrue(!bothVar.getDerivedDomain().isSingleton());
    scope.clear();
    {
      scope.push_back(trueVar.getId());
      scope.push_back(v0.getId()); // 1 10
      scope.push_back(vA.getId()); // 1 10, 11 27
      scope.push_back(vB.getId()); // 1 10, 11 27, -1 2
      scope.push_back(vC.getId()); // 1 10, 11 27, -1 2, 10 11
      scope.push_back(vD.getId()); // 1 10, 11 27, -1 2, 10 11, 10 11
      CondAllDiffConstraint c0(LabelStr("CondAllDiffConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      // 10 could be removed from v0 and 11 from vA, but current
      //   implementation doesn't check for such things.
    }
    scope.clear();
    {
      scope.push_back(trueVar.getId());
      scope.push_back(v0.getId()); // 1 10 so far
      scope.push_back(v1.getId()); // 2 10, 1 (since condition is true, remove singletons from all others)
      scope.push_back(v2.getId()); // 2 10, 1, 0 2
      scope.push_back(v3.getId()); // 2 10, 1, 0 2, 0 27
      scope.push_back(v4.getId()); // 2 10, 1, 0 2, 0 27, 0 27
      scope.push_back(v5.getId()); // 2 10, 1, 0 2, 0 27, 0 27, 0 27
      scope.push_back(v6.getId()); // 3 10, 1, 2, 3 27, 3 27, 3 27, 0
      scope.push_back(vA.getId()); // 3 10, 1, 2, 3 27, 3 27, 3 27, 0, 11 27
      scope.push_back(vB.getId()); // 3 10, 1, 2, 3 27, 3 27, 3 27, 0, 11 27, -1
      scope.push_back(vC.getId()); // 3 10, 1, 2, 3 27, 3 27, 3 27, 0, 11 27, -1, 10 11
      scope.push_back(vF.getId()); // 4 10, 1, 2, 4 27, 4 27, 4 27, 0, 11 27, -1, 10 11, 3
      scope.push_back(vG.getId()); // 4 10, 1, 2, 4 27, 4 27, 4 27, 0, 11 27, -1, 10 11, 3, 9 12
      scope.push_back(vH.getId()); // 4 10, 1, 2, 4 27, 4 27, 4 27, 0, 11 27, -1, 10 11, 3, 9 12, 4 5
      scope.push_back(vI.getId()); // 4 10, 1, 2, 4 27, 4 27, 4 27, 0, 11 27, -1, 10 11, 3, 9 12, 4 5, 4 7
      scope.push_back(vJ.getId()); // 4 10, 1, 2, 4 27, 4 27, 4 27, 0, 11 27, -1, 10 11, 3, 9 12, 4 5, 4 7, 5 9
      scope.push_back(vK.getId()); // 4 10, 1, 2, 4 27, 4 27, 4 27, 0, 11 27, -1, 10 11, 3, 10 12, 4 5, 4 7, 5 8, 9
      scope.push_back(vL.getId()); // 4 10, 1, 2, 4 27, 4 27, 4 27, 0, 11 27, -1, 10 11, 3, 10 12, 4 5, 4 7, 5 8, 9, 5 13
      scope.push_back(vM.getId()); // 6 10, 1, 2, 6 27, 6 27, 6 27, 0, 11 27, -1, 10 11, 3, 10 12, 4, 6 7, 6 8, 9, 6 13, 5
      scope.push_back(vN.getId()); // 6 10, 1, 2, 6 27, 6 27, 6 27, 0, 11 27, -1, 10 11, 3, 10 12, 4, 6 7, 6 8, 9, 6 13, 5, 6 15
      scope.push_back(vO.getId()); // 6 10, 1, 2, 6 27, 6 27, 6 27, 0, 11 27, -1, 10 11, 3, 10 12, 4, 6 7, 6 8, 9, 6 13, 5, 6 15, -2 6
      scope.push_back(vP.getId()); // 10, 1, 2, 14 27, 14 27, 14 27, 0, 14 27, -1, 11, 3, 12, 4, 7, 8, 9, 13, 5, 14 15, 6, -2
      scope.push_back(vQ.getId()); // 10, 1, 2, 16 27, 16 27, 16 27, 0, 16 27, -1, 11, 3, 12, 4, 7, 8, 9, 13, 5, 14, 6, -2, 15
      CondAllDiffConstraint c0(LabelStr("CondAllDiffConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(v0.getDerivedDomain() == IntervalIntDomain(10));
      assertTrue(v1.getDerivedDomain() == IntervalIntDomain(1));
      assertTrue(v2.getDerivedDomain() == IntervalIntDomain(2));
      assertTrue(v3.getDerivedDomain() == IntervalIntDomain(16, 27));
      assertTrue(v4.getDerivedDomain() == IntervalIntDomain(16, 27));
      assertTrue(v5.getDerivedDomain() == IntervalIntDomain(16, 27));
      assertTrue(v6.getDerivedDomain() == IntervalIntDomain(0));
      assertTrue(vA.getDerivedDomain() == IntervalIntDomain(16, 27));
      assertTrue(vB.getDerivedDomain() == IntervalIntDomain(-1));
      assertTrue(vC.getDerivedDomain() == IntervalIntDomain(11));
      assertTrue(vF.getDerivedDomain() == IntervalIntDomain(3));
      assertTrue(vG.getDerivedDomain() == IntervalIntDomain(12));
      assertTrue(vH.getDerivedDomain() == IntervalIntDomain(4));
      assertTrue(vI.getDerivedDomain() == IntervalIntDomain(7));
      assertTrue(vJ.getDerivedDomain() == IntervalIntDomain(8));
      assertTrue(vK.getDerivedDomain() == IntervalIntDomain(9));
      assertTrue(vL.getDerivedDomain() == IntervalIntDomain(13));
      assertTrue(vM.getDerivedDomain() == IntervalIntDomain(5));
      assertTrue(vN.getDerivedDomain() == IntervalIntDomain(14));
      assertTrue(vO.getDerivedDomain() == IntervalIntDomain(6));
      assertTrue(vP.getDerivedDomain() == IntervalIntDomain(-2));
      assertTrue(vQ.getDerivedDomain() == IntervalIntDomain(15));
    }
    scope.clear();
    {
      scope.push_back(falseVar.getId());
      scope.push_back(v6.getId()); // 0
      scope.push_back(v7.getId()); // 0
      CondAllDiffConstraint c0(LabelStr("CondAllDiffConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(v6.getDerivedDomain().getSingletonValue() == 0);
      assertTrue(v7.getDerivedDomain().getSingletonValue() == 0);
    }
    scope.clear();
    {
      scope.push_back(falseVar.getId());
      scope.push_back(v3.getId()); // 0 27
      scope.push_back(v4.getId()); // 0 27
      CondAllDiffConstraint c0(LabelStr("CondAllDiffConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      assertTrue(v3.getDerivedDomain() == IntervalIntDomain(0, 27));
      assertTrue(v4.getDerivedDomain() == IntervalIntDomain(0, 27));
    }
    scope.clear();
    {
      scope.push_back(falseVar.getId());
      scope.push_back(vP.getId()); // -2
      scope.push_back(vF.getId()); // 0 3
      scope.push_back(vI.getId()); // 0 7
      scope.push_back(vC.getId()); // 10 11
      CondAllDiffConstraint c0(LabelStr("CondAllDiffConstraint"), LabelStr("Default"), ENGINE, scope);
      ENGINE->propagate();
      assertTrue(ENGINE->constraintConsistent());
      // Only vF and vI overlap, so they have to be equal.
      assertTrue(vF.getDerivedDomain() == IntervalIntDomain(0, 3));
      // This next restriction is correct but the current implementation
      //   does not enforce it, as it requires checking all pairs to see
      //   that two individual vars are the only ones that overlap and
      //   therefore must be equal.
      // assertTrue(vI.getDerivedDomain() == IntervalIntDomain(0, 3));
    }
    return(true);
  }

  static bool testConstraintDeletion() {
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, 100));
    Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(10, 100));
    Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain());

    ConstraintId c0 = (new EqualConstraint(LabelStr("eq"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId())))->getId();
    ConstraintId c1 = (new EqualConstraint(LabelStr("eq"), LabelStr("Default"), ENGINE, makeScope(v1.getId(), v2.getId())))->getId();
    ConstraintId c2 = (new EqualConstraint(LabelStr("eq"), LabelStr("Default"), ENGINE, makeScope(v2.getId(), v3.getId())))->getId();

    // Force an inconsistency
    v0.specify(1);
    v1.specify(1);
    bool res = ENGINE->propagate();
    assert(!res);

    // Reset, and delete constraint, but it should not matter
    v1.reset();
    delete (Constraint*) c2;

    // Confirm still inconsistent
    res = ENGINE->propagate();
    assert(!res);

    delete (Constraint*) c0;
    delete (Constraint*) c1;
    return true;
  }

  /**
   * @brief Create a new EnumeratedDomain or BoolDomain from data read from the stream.
   * @note Incomplete, but should allow tests to pass.
   */
  static AbstractDomain* readSet(std::istream& in) {
    char ch;
    AbstractDomain *dom;
    AbstractDomain::DomainType type = AbstractDomain::REAL_ENUMERATION;
    bool negative = false;
    double value;
    std::list<double> values;
    std::string member;
    std::list<std::string> members;
    for (in.get(ch); ch != '}' && in.good(); ) {
      switch (ch) {
      case ' ':
        negative = false;
        in.get(ch);
        continue;
      case '-':
        assertTrue(members.empty() && type == AbstractDomain::REAL_ENUMERATION);
        negative = true;
        in.get(ch);
        continue;
      case '0': case '1': case '2': case '3': case '4':
      case '5': case '6': case '7': case '8': case '9':
        assertTrue(members.empty() && type == AbstractDomain::REAL_ENUMERATION);
        if (negative)
          member = "-";
        member += ch;
        for (in.get(ch); ch != '}' && ch != ' ' && in.good(); in.get(ch))
          member += ch;
        assertTrue(in.good());
        value = atof(member.c_str());
        values.push_back(value);
        member = "";
        continue;
      default:
        if (negative)
          member = "-";
        member += ch;
        for (in.get(ch); ch != '}' && ch != ' ' && in.good(); in.get(ch))
          member += ch;
        assertTrue(in.good());
        if (member == "-Infinity" || member == "-Inf" || member == "-INF") {
          assertTrue(type == AbstractDomain::REAL_ENUMERATION);
          values.push_back(MINUS_INFINITY);
        } else
          if (member == "Infinity" || member == "Inf" || member == "INF") {
            assertTrue(type == AbstractDomain::REAL_ENUMERATION);
            values.push_back(PLUS_INFINITY);
          } else
            if (member == "false" || member == "False" || member == "FALSE") {
              members.push_back("false");
              // Allow "false" - but not "False"! - to be a member of a user defined type.
              // Need to know expected type of this arg of constraint to do better.
              if (type == AbstractDomain::REAL_ENUMERATION)
                type = AbstractDomain::BOOL;
            } else
              if (member == "true" || member == "True" || member == "TRUE") {
                members.push_back("true");
                // Allow "true" - but not "True"! - to be a member of a user defined type.
                // Need to know expected type of this arg of constraint to do better.
                if (type == AbstractDomain::REAL_ENUMERATION)
                  type = AbstractDomain::BOOL;
              } else {
                members.push_back(member);
                type = AbstractDomain::USER_DEFINED;
              }
        member = "";
        break;
      }
    }
    assertTrue(in.good() && ch == '}');
    assertTrue(values.empty() || members.empty());
    switch (type) {
    case AbstractDomain::REAL_ENUMERATION:
      dom = new NumericDomain(values);
      break;
    case AbstractDomain::BOOL:
      dom = new BoolDomain;
      dom->empty();
      for (std::list<std::string>::iterator it = members.begin();
           it != members.end(); it++)
        if (*it == "false")
          dom->insert(false);
        else
          if (*it == "true")
            dom->insert(true);
          else
            assertTrue(false);
      break;
    case AbstractDomain::USER_DEFINED:
      // Cannot support members without knowing how to map them to doubles.
      // For now, caller will have to skip this test.
      return(0);
      break;
    default: // Unsupported or unimplemented type.
      assertTrue(false);
      break;
    }
    assertTrue(dom != 0);
    return(dom);
  }

  /**
   * @brief Create a new IntervalDomain from data read from the stream.
   * @note Incomplete, but should allow tests to pass.
   */
  static AbstractDomain* readInterval(std::istream& in) {
    char ch;
    AbstractDomain *dom;
    bool negative = false;
    double endPoints[2];
    unsigned int which = 0; // 0 for no bounds; 1 for lower bound; 2 for upper bound.
    for (in.get(ch); ch != ']' && in.good(); ) {
      switch (ch) {
      case ' ': case '+':
        negative = false;
        in.get(ch);
        continue;
      case '-':
        negative = true;
        in.get(ch);
        continue;
      case 'I': case 'i': // Infinity or a variant thereof.
        which++;
        assertTrue(which < 3);
        if (negative)
          endPoints[which - 1] = MINUS_INFINITY;
        else
          endPoints[which - 1] = PLUS_INFINITY;
        for (in.get(ch); ch != ' ' && ch != ']' && in.good(); in.get(ch))
          ;
        assertTrue(in.good());
        continue;
      case '0': case '1': case '2': case '3': case '4':
      case '5': case '6': case '7': case '8': case '9': {
        which++;
        assertTrue(which < 3);
        std::string number;
        if (negative)
          number = "-";
        number += ch;
        for (in.get(ch); ch != ']' && ch != ' ' && in.good(); in.get(ch))
          number += ch;
        assertTrue(in.good());
        endPoints[which - 1] = atof(number.c_str());
      }
        continue;
      default:
        // Unrecognized input.
        assertTrue(false);
        break;
      }
    }
    assertTrue(in.good() && ch == ']' && which < 3);
    // Presume always IntervalDomain (rather than IntervalIntDomain) for now.
    // To know which will probably require a DomainType argument to this function.
    if (which == 0) {
      dom = new IntervalDomain();
      dom->empty();
    } else
      if (which == 1)
        dom = new IntervalDomain(endPoints[0]);
      else
        dom = new IntervalDomain(endPoints[0], endPoints[1]);
    assertTrue(dom != 0);
    return(dom);
  }

  /**
   * @brief Describes one constraint function test case for testArbitraryConstraints().
   * @see testArbitraryConstraints
   */
  class ConstraintTestCase {
  public:

    /**
     * @brief Primary constructor, requiring all of the info.
     */
    ConstraintTestCase(std::string cN, std::string fN, unsigned int l,
                       std::list<AbstractDomain*> doms)
      : m_constraintName(cN), m_fileName(fN), m_line(l),
        m_domains(doms) {
    }

    // Default copy constructor should be fine.

    const std::string m_constraintName; /**< Equal, AddEqual, etc. */
    const std::string m_fileName; /**< File containing the "source" of the test case.  Printed when test fails. */
    const unsigned int m_line; /**< Line within file of test case.  Printed when test fails. */
    const std::list<AbstractDomain*> m_domains; /**< Input and (expected) output domains, interleaved.
                                                 * That is, first is first input domain, second is first output domain,
                                                 * third is second input domain, fourth is second output domain, etc.
                                                 */
  };

  /**
   * @brief Read constraint test cases from the given file, adding them to the
   * list passed in.
   * @return True if tests pass; false only if file cannot be opened; throw an error
   * if the file can be opened but some test fails.
   */
  static bool readTestCases(std::string file, std::list<ConstraintTestCase>& testCases) {
    ifstream tCS(file.c_str()); /**< testCaseStream. */
    if (!tCS.is_open() || !tCS.good())
      return(false);
    unsigned line = 1; /**< Line within file. */
    std::string constraintName; /**< Name of a constraint, from each line of file. */
    char buf[20]; /**< For single "words" of input. */
    unsigned int cnt; /**< For test number. */
    char ch; /**< For braces, brackets, and other miscellany. */
    AbstractDomain *domain = 0;
    while (tCS.good() && !tCS.eof()) {
      tCS.width(5);
      tCS >> buf;
      if (tCS.eof())
        break;
      assertTrue(strcmp(buf, "test") == 0 && !tCS.eof() && tCS.good());
      tCS >> cnt;

      // This requirement that cnt == line is probably overkill and should be dropped.
      // If nothing else, it makes it harder to insert tests in the middle of files.
      assert(cnt == line && !tCS.eof() && tCS.good());

      constraintName = "";
      tCS.get(ch);
      assertTrue(ch == ' ' && !tCS.eof() && tCS.good());
      for (tCS.get(ch); ch != ' ' && tCS.good(); tCS.get(ch))
        constraintName += ch;
      assertTrue(constraintName.size() > 0 && !tCS.eof() && tCS.good());
      tCS.width(7);
      tCS >> buf;
      assertTrue(strcmp(buf, "inputs") == 0 && !tCS.eof() && tCS.good());
      tCS.get(ch);
      assertTrue(ch == ' ' && !tCS.eof() && tCS.good());
      // Build input domains until 'o'utputs is seen, then output domains until end of line.
      // Details depend on NewPlan/Libraries/Domain.cc::Domain::print() or similar, but
      // this is meant to be fairly flexible so that new tests can be written by hand.
      std::list<AbstractDomain*> domains, inputDoms, outputDoms;

      // Until Europa label sets are supported by readSet(),
      // we may have to skip some tests:
      bool skipThisTest = false;

      bool readingInputDoms = true;
      for (tCS.get(ch); ch != '\n' && tCS.good(); tCS.get(ch)) {
        if (skipThisTest)
          continue;
        switch (ch) {
        case ' ': // Blank between fields; ignore it.
          break;
        case '{': // Singleton, enumeration, or boolean but could be IntervalDomain, IntervalIntDomain, or BoolDomain.
          domain = readSet(tCS);
          // This if is temporary, until readSet is fully implemented.
          // Presently, it cannot support Europa label sets.
          if (domain == 0) {
            skipThisTest = true;
            continue;
          }
          assertTrue(domain != 0 && !tCS.eof() && tCS.good());
          if (readingInputDoms)
            inputDoms.push_back(domain);
          else
            outputDoms.push_back(domain);
          break;
        case '[': // Interval, real or integer, but could use 'Infinity' and variations.
          domain = readInterval(tCS);
          assertTrue(domain != 0 && !tCS.eof() && tCS.good());
          if (readingInputDoms)
            inputDoms.push_back(domain);
          else
            outputDoms.push_back(domain);
          break;
        case 'o':
          tCS.width(7);
          tCS >> buf;
          assertTrue(strcmp(buf, "utputs") == 0 && !tCS.eof() && tCS.good());
          readingInputDoms = false;
          break;
        default:
          assertTrue(false);
          break;
        } // switch (ch): '{', '[', or 'o'
      } // for tCS.get(ch); ch != '\n' && tCS.good(); tCS.get(ch)

      if (skipThisTest) {
        line++; // To preserve comparison to cnt in assertion.
        continue;
      }

      // Simple checks that this test case is OK.
      assertTrue(inputDoms.size() == outputDoms.size() && !tCS.eof() && tCS.good());

      // Some NewPlan constraints are the same as constraints in the
      //   prototype except for the order of the arguments.
      // These two move the last argument to the front:
      /* This is wrong:
         addleq(x, y, z) is x + y <= z
         ... but:
         LessOrEqThanSum(z, x, y) is z <= x + y
         ... which has the comparison in the opposite direction.
         --wedgingt 2004 Mar 15
      if (constraintName == "addleq" ||
          constraintName == "addlt") {
        if (constraintName == "addleq")
          constraintName = "LessOrEqThanSum";
        if (constraintName == "addlt")
          constraintName = "LessThanSum";
        AbstractDomain *moving = inputDoms.back();
        inputDoms.pop_back();
        inputDoms.push_front(moving);
        moving = outputDoms.back();
        outputDoms.pop_back();
        outputDoms.push_front(moving);
      }
      */

      // OK, done with a line, each line being a test, so
      // interleave the input and output domains to make
      // things easier in caller.
      domains.clear();
      while (!inputDoms.empty() && !outputDoms.empty()) {
        domains.push_back(inputDoms.front());
        inputDoms.pop_front();
        domains.push_back(outputDoms.front());
        outputDoms.pop_front();
      }
      // ... and add this test to the list:
      testCases.push_back(ConstraintTestCase(constraintName, file, line++, domains));
    } // while tCS.good() && !tCS.eof()
    return(tCS.eof());
  }

  /**
   * @brief Run arbtrary constraints with arbitrary input domains,
   * comparing the propagated domains with the expected output domains.
   */
  static bool testArbitraryConstraints() {
    // Input to this test: a list of constraint calls and expected output domains.
    std::list<ConstraintTestCase> tests;
    std::set<std::string> warned; /**< List of unregistered constraints seen so far. */

    // This kind of information can also be read from a file, as below.
    std::string constraintName("Equal");
    std::list<AbstractDomain*> domains;
    domains.push_back(new IntervalIntDomain(1, 10)); // first input domain
    domains.push_back(new IntervalIntDomain(2, 10)); // expected value of first output domain
    domains.push_back(new IntervalIntDomain(2, 11)); // second input domain
    domains.push_back(new IntervalIntDomain(2, 10)); // expected value of second output domain
    tests.push_back(ConstraintTestCase(constraintName, __FILE__, __LINE__, std::list<AbstractDomain*>(domains)));

    // Try reading "test cases" file of NewPlan/ModuleTests/ConstraintLibrary/testCLib,
    // committed here as CLibTestCases after some minor editing to use '[]' for all
    // numeric domains since Europa prints some of those using '{}' syntax and the
    // prototype treats as intervals all numeric domains that aren't explicitly
    // identified as enumerations.
    // For each file, try twice with different relative paths since we don't know what
    // the current working directory is.
    assertTrue(readTestCases(std::string("ConstraintEngine/NewTestCases"), tests) ||
               readTestCases(std::string("NewTestCases"), tests));
    assertTrue(readTestCases(std::string("ConstraintEngine/CLibTestCases"), tests) ||
               readTestCases(std::string("CLibTestCases"), tests));

    // Run each test, in the same order they were read/init'd,
    //   keeping a count of failed test cases.
    unsigned int problemCount = 0;
    for ( ; !tests.empty(); tests.pop_front()) {
      // Warn about unregistered constraint names and otherwise ignore tests using them.
      if (!ConstraintLibrary::isRegistered(LabelStr(tests.front().m_constraintName), false)) {
        if (loggingEnabled() &&
            warned.find(tests.front().m_constraintName) == warned.end()) {
          std::cout << tests.front().m_fileName << ':' << tests.front().m_line
                    << ": constraint " << tests.front().m_constraintName
                    << " is unregistered; skipping tests of it.\n";
          warned.insert(tests.front().m_constraintName);
        }
        continue;
      }

      std::list<AbstractDomain*> testDomains(tests.front().m_domains);
      // Each input domain must have a matching output domain.
      assertTrue(testDomains.size() % 2 == 0);

      // Build the scope and the list of expected output domains.
      std::vector<ConstrainedVariableId> scope;
      std::list<AbstractDomain*> outputDoms;
      ConstrainedVariableId cVarId;
      while (!testDomains.empty()) {
        AbstractDomain *domPtr = testDomains.front();
        assertTrue(domPtr != 0 && (domPtr->isOpen() || !domPtr->isEmpty()));
        testDomains.pop_front();
        AbstractDomain::DomainType domType = domPtr->getType();

        // This is ugly and precludes support for USER_DEFINED domains
        // since the corresponding C++ class is unknown. --wedgingt 2004 Mar 10
        switch (domType) {
        case AbstractDomain::INT_INTERVAL: {
          Variable<IntervalIntDomain> *var = new Variable<IntervalIntDomain>(ENGINE, IntervalIntDomain());
          assertTrue(var != 0);
          var->specify(*domPtr);
          cVarId = var->getId();
        }
          break;
        case AbstractDomain::REAL_INTERVAL: {
          Variable<IntervalDomain> *var = new Variable<IntervalDomain>(ENGINE, IntervalDomain());
          assertTrue(var != 0);
          var->specify(*domPtr);
          cVarId = var->getId();
        }
          break;
        case AbstractDomain::REAL_ENUMERATION: {
          std::list<double> values;
          domPtr->getValues(values);
          Variable<NumericDomain> *var = new Variable<NumericDomain>(ENGINE, NumericDomain(values));
          assertTrue(var != 0);
          var->specify(*domPtr);
          cVarId = var->getId();
        }
          break;
        case AbstractDomain::BOOL: {
          Variable<BoolDomain> *var = new Variable<BoolDomain>(ENGINE, BoolDomain());
          assertTrue(var != 0);
          var->specify(*domPtr);
          cVarId = var->getId();
        }
          break;
        default:
          assertTrue(false);
          break;
        }

        delete domPtr;
        scope.push_back(cVarId);
        domPtr = testDomains.front();
        assertTrue(domPtr != 0);
        outputDoms.push_back(domPtr);
        testDomains.pop_front();
      }
      assertTrue(scope.size() == outputDoms.size());

      // Create and execute the constraint.
      ConstraintId constraint = ConstraintLibrary::createConstraint(LabelStr(tests.front().m_constraintName), ENGINE, scope);
      assertTrue(ENGINE->pending());
      ENGINE->propagate();
      assertFalse(ENGINE->pending());

      // Compare derived domains with outputDoms.
      std::vector<ConstrainedVariableId>::iterator scopeIter = scope.begin();
      unsigned int i = 1;
      bool problem = false;
      for ( ; scopeIter != scope.end() && !outputDoms.empty(); scopeIter++, i++) {
        AbstractDomain *domPtr = outputDoms.front();
        outputDoms.pop_front();
        if (domPtr->isEmpty()) {
          if (!(*scopeIter)->derivedDomain().isEmpty()) {
            if (!problem)
              std::cerr << tests.front().m_fileName << ':' << tests.front().m_line
                        << ": unexpected result propagating " << tests.front().m_constraintName;
            std::cerr << ";\n  argument " << i << " is " << (*scopeIter)->derivedDomain()
                      << "\n     rather than empty";
            problem = true;
          }
        } else
          if ((*scopeIter)->derivedDomain().isEmpty()) {
            if (!domPtr->isEmpty()) {
              if (!problem)
                std::cerr << tests.front().m_fileName << ':' << tests.front().m_line
                          << ": unexpected result propagating " << tests.front().m_constraintName;
              std::cerr << ";\n  argument " << i << " is empty"
                        << "\n    rather than " << *domPtr;
              problem = true;
            }
          } else
            if ((*scopeIter)->derivedDomain() != *domPtr) {
              if (!problem)
                std::cerr << tests.front().m_fileName << ':' << tests.front().m_line
                          << ": unexpected result propagating " << tests.front().m_constraintName;
              std::cerr << ";\n  argument " << i << " is " << (*scopeIter)->derivedDomain()
                        << "\n    rather than " << *domPtr;
              problem = true;
            }
        delete domPtr;
      } // for ( ; scopeIter != scope.end() && ...

      // Finish complaining if a compare failed.
      if (problem) {
        std::cerr << '\n';
        ++problemCount;
      }

      // Print that the test succeeded, count the successes for this constraint function, or whatever.
      delete (Constraint*) constraint;
      while (!scope.empty()) {
        cVarId = scope.back();
        scope.pop_back();
        delete (ConstrainedVariable*) cVarId;
      }
    }
    if (problemCount > 0) {
      std::cerr << __FILE__ << ':' << __LINE__ << ": testArbitraryConstraints() failed "
                << problemCount << " test cases" << std::endl;
      throw Error::GeneralUnknownError();
    }
    return(true);
  }

  static bool testLockConstraint() {
    LabelSet lockDomain;
    lockDomain.insert(Prototype::LabelStr("A"));
    lockDomain.insert(Prototype::LabelStr("B"));
    lockDomain.insert(Prototype::LabelStr("C"));
    lockDomain.insert(Prototype::LabelStr("D"));
    lockDomain.close();

    LabelSet baseDomain;
    baseDomain.insert(Prototype::LabelStr("A"));
    baseDomain.insert(Prototype::LabelStr("B"));
    baseDomain.insert(Prototype::LabelStr("C"));
    baseDomain.insert(Prototype::LabelStr("D"));
    baseDomain.insert(Prototype::LabelStr("E"));
    baseDomain.close();

    // Set up variable with base domain - will exceed lock domain
    Variable<LabelSet> v0(ENGINE, baseDomain);
    assert(v0.getDerivedDomain() != lockDomain);
    Variable<LabelSet> v1(ENGINE, lockDomain);

    // Post constraint, and ensure it is propagated to equality with lock domain
    LockConstraint c0(LabelStr("Lock"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId()));
    assert(ENGINE->propagate());
    assert(v0.getDerivedDomain() == lockDomain);

    // Now specify to a restricted value, and ensure an inconsistency
    v0.specify(Prototype::LabelStr("C"));
    assert(!ENGINE->propagate());

    // Now ensure that we can rty again, without changing anything, and get the same result.
    assert(!ENGINE->propagate());

    // Reset the variable and ensure we can repropagate it correctly
    v0.reset();
    assert(ENGINE->propagate());
    assert(v0.getDerivedDomain() == lockDomain);

    return true;
  }

  static bool testNegateConstraint() {
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain());
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain());
    NegateConstraint c0(LabelStr("NegateConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId()));
    ENGINE->propagate();
    assert(ENGINE->constraintConsistent());
    assert(v0.getDerivedDomain() == IntervalIntDomain(0, PLUS_INFINITY));
    assert(v1.getDerivedDomain() == IntervalIntDomain(MINUS_INFINITY, 0));

    v0.specify(IntervalIntDomain(20, 30));
    assert(v1.getDerivedDomain() == IntervalIntDomain(-30, -20));

    v1.specify(IntervalIntDomain(-22, -21));
    assert(v0.getDerivedDomain() == IntervalIntDomain(21, 22));

    v0.specify(21);
    assert(v1.getDerivedDomain().getSingletonValue() == -21);

    return true;
  }

  static bool testUnaryQuery() {
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain());
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain());
    NegateConstraint c0(LabelStr("NegateConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v1.getId()));
    assert(!c0.isUnary());

    Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(1));
    NegateConstraint c1(LabelStr("NegateConstraint"), LabelStr("Default"), ENGINE, makeScope(v0.getId(), v2.getId()));
    assert(c1.isUnary());

    return true;
  }

  static bool testTestEqConstraint() {
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(5));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(5));
      Variable<BoolDomain> v2(ENGINE, BoolDomain());
      TestEqConstraint c0(LabelStr("TestEqConstraint"), LabelStr("Default"), 
			  ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      ENGINE->propagate();
      assert(v2.getDerivedDomain().isTrue());
    }
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(5));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(6));
      Variable<BoolDomain> v2(ENGINE, BoolDomain());
      TestEqConstraint c0(LabelStr("TestEqConstraint"), LabelStr("Default"), 
			  ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      ENGINE->propagate();
      assert(v2.getDerivedDomain().isFalse());
    }
    {
      Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(5,10));
      Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(5, 20));
      Variable<BoolDomain> v2(ENGINE, BoolDomain());
      TestEqConstraint c0(LabelStr("TestEqConstraint"), LabelStr("Default"), 
			  ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      ENGINE->propagate();
      assert(!v2.getDerivedDomain().isSingleton());
      v0.specify(7);
      v1.specify(7);
      ENGINE->propagate();
      assert(v2.getDerivedDomain().isTrue());
      v0.reset();
      ENGINE->propagate();
      assert(!v2.getDerivedDomain().isSingleton());      
    }
    {
      Variable<LabelSet> v0(ENGINE, LabelSet(LabelStr("C")));
      Variable<LabelSet> v1(ENGINE, LabelSet(LabelStr("C")));
      Variable<BoolDomain> v2(ENGINE, BoolDomain());
      TestEqConstraint c0("TestEqConstraint", "Default", 
			  ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      ENGINE->propagate();
      assert(v2.getDerivedDomain().isTrue());
    }
    {
      Variable<LabelSet> v0(ENGINE, LabelSet(LabelStr("C")));
      Variable<LabelSet> v1(ENGINE, LabelSet(LabelStr("E")));
      Variable<BoolDomain> v2(ENGINE, BoolDomain());
      TestEqConstraint c0(LabelStr("TestEqConstraint"), LabelStr("Default"), 
			  ENGINE, makeScope(v0.getId(), v1.getId(), v2.getId()));
      ENGINE->propagate();
      assert(v2.getDerivedDomain().isFalse());
    }
    return true;
  }



}; // class ConstraintTest

class ConstraintFactoryTest
{
public:
  static bool test() {
    runTest(testAllocation);
    return true;
  }

private:
  static bool testAllocation(){
    std::vector<ConstrainedVariableId> variables;
    // v0 == v1
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
    variables.push_back(v0.getId());
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(1, 1));
    variables.push_back(v1.getId());
    ConstraintId c0 = ConstraintLibrary::createConstraint(LabelStr("Equal"), ENGINE, variables);    
    ENGINE->propagate();
    assert(v0.getDerivedDomain().getSingletonValue() == 1);
    delete (Constraint*) c0;
    return true;
  }
};

class EquivalenceClassTest{
public:
  static bool test() {
    runTest(testBasicAllocation);
    runTest(testConstructionOfSingleGraph);
    runTest(testSplittingOfSingleGraph);
    runTest(testMultiGraphMerging);
    runTest(testEqualityConstraintPropagator);
    return true;
  }

private:
  static bool testBasicAllocation(){
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(2, 8));
    EquivalenceClassCollection g0;
    g0.addConnection(v0.getId(), v1.getId());
    assert(g0.getGraphCount() == 1);
    v0.specify(10);
    return true;
  }

  static bool testConstructionOfSingleGraph(){
    EquivalenceClassCollection g0;
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(2, 8));
    Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(8, 20));
    g0.addConnection(v0.getId(), v1.getId());
    g0.addConnection(v1.getId(), v2.getId());
    assert(g0.getGraphCount() == 1);
    int graphKey = g0.getGraphKey(v0.getId());
    assert(g0.getGraphKey(v1.getId()) == graphKey);
    assert(g0.getGraphKey(v2.getId()) == graphKey);

    Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain(1, 100));
    Variable<IntervalIntDomain> v4(ENGINE, IntervalIntDomain(-100, 100));
    g0.addConnection(v2.getId(), v3.getId());
    g0.addConnection(v3.getId(), v4.getId());
    assert(g0.getGraphCount() == 1);
    assert(graphKey != g0.getGraphKey(v0.getId())); // Should have updated for all
    return true;
  }

  static bool testSplittingOfSingleGraph(){
    EquivalenceClassCollection g0;
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(2, 8));
    Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(8, 20));
    Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain(1, 100));
    Variable<IntervalIntDomain> v4(ENGINE, IntervalIntDomain(-100, 100));
    Variable<IntervalIntDomain> v5(ENGINE, IntervalIntDomain(-100, 1000));
    Variable<IntervalIntDomain> v6(ENGINE, IntervalIntDomain(-100, 100));
    Variable<IntervalIntDomain> v7(ENGINE, IntervalIntDomain(-100, 100));
    Variable<IntervalIntDomain> v8(ENGINE, IntervalIntDomain(-100, 100));
    g0.addConnection(v0.getId(), v1.getId());
    g0.addConnection(v1.getId(), v2.getId());
    g0.addConnection(v2.getId(), v3.getId());
    g0.addConnection(v3.getId(), v4.getId());
    g0.addConnection(v4.getId(), v5.getId());
    g0.addConnection(v5.getId(), v6.getId());
    g0.addConnection(v6.getId(), v7.getId());
    g0.addConnection(v7.getId(), v8.getId());
    assert(g0.getGraphCount() == 1);

    // Cause a split by removing a connection in the middle
    g0.removeConnection(v3.getId(), v4.getId());
    assert(g0.getGraphCount() == 2);

    // Cause another split
    g0.removeConnection(v5.getId(), v6.getId());
    assert(g0.getGraphCount() == 3);

    // Test membership of resulting classes
    assert((g0.getGraphKey(v1.getId()) + g0.getGraphKey(v2.getId()) + g0.getGraphKey(v3.getId()))/3 == g0.getGraphKey(v0.getId()));
    assert(g0.getGraphKey(v4.getId()) == g0.getGraphKey(v5.getId()));
    assert((g0.getGraphKey(v6.getId()) + g0.getGraphKey(v7.getId()))/2   == g0.getGraphKey(v8.getId()));

    return true;
  }

  static bool testMultiGraphMerging(){
    EquivalenceClassCollection g0;
    Variable<IntervalIntDomain> v0(ENGINE, IntervalIntDomain(1, 10));
    Variable<IntervalIntDomain> v1(ENGINE, IntervalIntDomain(2, 8));
    Variable<IntervalIntDomain> v2(ENGINE, IntervalIntDomain(8, 20));
    Variable<IntervalIntDomain> v3(ENGINE, IntervalIntDomain(1, 100));
    Variable<IntervalIntDomain> v4(ENGINE, IntervalIntDomain(-100, 100));
    Variable<IntervalIntDomain> v5(ENGINE, IntervalIntDomain(-100, 1000));
    Variable<IntervalIntDomain> v6(ENGINE, IntervalIntDomain(-100, 100));
    Variable<IntervalIntDomain> v7(ENGINE, IntervalIntDomain(-100, 100));
    Variable<IntervalIntDomain> v8(ENGINE, IntervalIntDomain(-100, 100));
    // First group
    g0.addConnection(v0.getId(), v1.getId());
    g0.addConnection(v1.getId(), v2.getId());
    g0.addConnection(v2.getId(), v3.getId());

    // Second group
    g0.addConnection(v4.getId(), v5.getId());

    // Third group
    g0.addConnection(v6.getId(), v7.getId());
    g0.addConnection(v7.getId(), v8.getId());

    // Test resulting classes
    assert(g0.getGraphCount() == 3);
    assert((g0.getGraphKey(v1.getId()) + g0.getGraphKey(v2.getId()) + g0.getGraphKey(v3.getId()))/3 == g0.getGraphKey(v0.getId()));
    assert(g0.getGraphKey(v4.getId()) == g0.getGraphKey(v5.getId()));
    assert((g0.getGraphKey(v6.getId()) + g0.getGraphKey(v7.getId()))/2   == g0.getGraphKey(v8.getId()));

    // Add connectionto cause a merge
    g0.addConnection(v3.getId(), v4.getId());
    assert(g0.getGraphCount() == 2);
    assert((g0.getGraphKey(v1.getId()) + g0.getGraphKey(v2.getId()) + g0.getGraphKey(v3.getId()))/3 == g0.getGraphKey(v0.getId()));
    assert((g0.getGraphKey(v4.getId()) + g0.getGraphKey(v5.getId()))/2 == g0.getGraphKey(v0.getId()));
    assert((g0.getGraphKey(v6.getId()) + g0.getGraphKey(v7.getId()))/2   == g0.getGraphKey(v8.getId()));


    // Add connectionto cause a merge
    g0.addConnection(v5.getId(), v6.getId());
    assert(g0.getGraphCount() == 1);

    return true;
  }

  static bool testEqualityConstraintPropagator(){
    ConstraintEngineId ce((new ConstraintEngine())->getId());
    new EqualityConstraintPropagator(LabelStr("EquivalenceClass"), ce);
    {
      std::vector<ConstrainedVariableId> variables;
      // v0 == v1
      Variable<IntervalIntDomain> v0(ce, IntervalIntDomain(1, 10));
      variables.push_back(v0.getId());
      Variable<IntervalIntDomain> v1(ce, IntervalIntDomain(-100, 100));
      variables.push_back(v1.getId());
      EqualConstraint c0(LabelStr("EqualConstraint"), LabelStr("EquivalenceClass"), ce, variables);
      ce->propagate();

      variables.clear();
      Variable<IntervalIntDomain> v2(ce, IntervalIntDomain(8, 10));
      variables.push_back(v2.getId());
      Variable<IntervalIntDomain> v3(ce, IntervalIntDomain(10, 200));
      variables.push_back(v3.getId());
      EqualConstraint c1(LabelStr("EqualConstraint"), LabelStr("EquivalenceClass"), ce, variables);
      ce->propagate();

      assert(v0.getDerivedDomain().getUpperBound() == 10);
      assert(v2.getDerivedDomain().getSingletonValue() == 10);

      variables.clear();
      variables.push_back(v3.getId());
      variables.push_back(v1.getId());
      EqualConstraint c2(LabelStr("EqualConstraint"), LabelStr("EquivalenceClass"), ce, variables);

      ce->propagate();
      assert(ce->constraintConsistent());
      assert(v0.getDerivedDomain().getSingletonValue() == 10);

      variables.clear();
      Variable<IntervalIntDomain> v4(ce, IntervalIntDomain(1, 9));
      variables.push_back(v3.getId());
      variables.push_back(v4.getId());
      ConstraintId c3((new EqualConstraint(LabelStr("EqualConstraint"), LabelStr("EquivalenceClass"), ce, variables))->getId());
      ce->propagate();
      assert(ce->provenInconsistent());

      delete (Constraint*) c3;
      assert(ce->pending());
      ce->propagate();
      assert(ce->constraintConsistent());
      assert(v0.getDerivedDomain().getSingletonValue() == 10);
    }
    delete (ConstraintEngine*) ce;
    return(true);
  }
};

int main() {
  initConstraintEngine();
  initConstraintLibrary();
  REGISTER_CONSTRAINT(DelegationTestConstraint, "TestOnly", "Default");
  TypeFactory::createValue("INT_INTERVAL", std::string("5"));
  runTestSuite(DomainTests::test);
  runTestSuite(TypeFactoryTests::test);
  runTestSuite(EntityTests::test);
  runTestSuite(ConstraintEngineTest::test);
  runTestSuite(VariableTest::test); 
  runTestSuite(ConstraintTest::test); 
  runTestSuite(ConstraintFactoryTest::test);
  runTestSuite(EquivalenceClassTest::test);
  cout << "Finished" << endl;
  ConstraintLibrary::purgeAll();
}
