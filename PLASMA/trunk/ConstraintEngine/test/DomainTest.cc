#include "LabelStr.hh"
#include "Domain.hh"
#include "IntervalIntDomain.hh"
#include "BoolDomain.hh"
#include "ConstraintEngine.hh"
#include "Constraints.hh"
#include "DefaultPropagator.hh"
#include "Variable.hh"
#include "EqualityConstraintPropagator.hh"

#include <iostream>
#include <list>
#include <vector>

using namespace Prototype;
using namespace std;

static void testEquate(const LabelSet& a, const LabelSet& b) {
  LabelSet l_a(a);
  LabelSet l_b(b);
  l_a.equate(l_b);
}

static void testIntersection() {
  IntervalIntDomain dom1(100, 1000);
  IntervalIntDomain dom2(250, 2000);
  dom1.intersect(dom2);
}

static void outerLoopForTestIntersection() {
  for (int i = 0; i < 1000000; i++)
    testIntersection();
}

static void outerLoopForTestEquate() {
  std::list<Prototype::LabelStr> values;
  values.push_back(Prototype::LabelStr("A"));
  values.push_back(Prototype::LabelStr("B"));
  values.push_back(Prototype::LabelStr("C"));
  values.push_back(Prototype::LabelStr("D"));
  values.push_back(Prototype::LabelStr("E"));
  values.push_back(Prototype::LabelStr("F"));
  values.push_back(Prototype::LabelStr("G"));
  values.push_back(Prototype::LabelStr("H"));
  LabelSet ls_a(values);

  values.clear();
  values.push_back(Prototype::LabelStr("1"));
  values.push_back(Prototype::LabelStr("2"));
  values.push_back(Prototype::LabelStr("3"));
  values.push_back(Prototype::LabelStr("E"));
  values.push_back(Prototype::LabelStr("4"));
  values.push_back(Prototype::LabelStr("5"));
  values.push_back(Prototype::LabelStr("6"));
  values.push_back(Prototype::LabelStr("7"));
  values.push_back(Prototype::LabelStr("8"));
  LabelSet ls_b(values);

  for (int i = 0; i < 1000000; i++)
    testEquate(ls_a, ls_b);
}

static void testLabelSetEqualityPerformance(const ConstraintEngineId& ce) {
  std::list<Prototype::LabelStr> values;
  values.push_back(Prototype::LabelStr("V0"));
  values.push_back(Prototype::LabelStr("V1"));
  values.push_back(Prototype::LabelStr("V2"));
  values.push_back(Prototype::LabelStr("V3"));
  values.push_back(Prototype::LabelStr("V4"));
  values.push_back(Prototype::LabelStr("V5"));
  values.push_back(Prototype::LabelStr("V6"));
  values.push_back(Prototype::LabelStr("V7"));
  values.push_back(Prototype::LabelStr("V8"));
  values.push_back(Prototype::LabelStr("V9"));
  LabelSet labelSet(values);


  Variable<LabelSet> v0(ce, labelSet);
  Variable<LabelSet> v1(ce, labelSet);
  Variable<LabelSet> v2(ce, labelSet);
  Variable<LabelSet> v3(ce, labelSet);
  Variable<LabelSet> v4(ce, labelSet);
  Variable<LabelSet> v5(ce, labelSet);
  Variable<LabelSet> v6(ce, labelSet);
  Variable<LabelSet> v7(ce, labelSet);
  Variable<LabelSet> v8(ce, labelSet);
  Variable<LabelSet> v9(ce, labelSet);

  std::vector<ConstrainedVariableId> variables;

  variables.push_back(v0.getId());
  variables.push_back(v1.getId());
  EqualConstraint c0(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v1.getId());
  variables.push_back(v2.getId());
  EqualConstraint c1(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v2.getId());
  variables.push_back(v3.getId());
  EqualConstraint c2(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v3.getId());
  variables.push_back(v4.getId());
  EqualConstraint c3(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v4.getId());
  variables.push_back(v5.getId());
  EqualConstraint c4(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v5.getId());
  variables.push_back(v6.getId());
  EqualConstraint c5(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v6.getId());
  variables.push_back(v7.getId());
  EqualConstraint c6(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v7.getId());
  variables.push_back(v8.getId());
  EqualConstraint c7(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v8.getId());
  variables.push_back(v9.getId());
  EqualConstraint c8(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v0.getId());
  variables.push_back(v1.getId());
  variables.push_back(v2.getId());
  variables.push_back(v3.getId());
  variables.push_back(v4.getId());
  variables.push_back(v5.getId());
  variables.push_back(v6.getId());
  variables.push_back(v7.getId());
  variables.push_back(v8.getId());
  variables.push_back(v9.getId());

  Variable<LabelSet>*  p_v0 = (Variable<LabelSet>*) v0.getId();


  for(int i = 10; i > 2; i--){
    values.pop_back();
    LabelSet newDomain(values);
    Variable<LabelSet>*  p_v = (Variable<LabelSet>*) variables[i-1];
    p_v->specify(newDomain);
    ce->propagate();
    assert(ce->constraintConsistent());
    assert(p_v0->getDerivedDomain().getSize() == i-1);
  }
}

static void outerLoopLabelSetEqualConstraint(bool useEquivalenceClasses) {
  ConstraintEngine ce;

  if (useEquivalenceClasses)
    new EqualityConstraintPropagator(LabelStr("Equal"), ce.getId());
  else
    new DefaultPropagator(LabelStr("Equal"), ce.getId());

  for (int i = 0; i < 1000; i++)
    testLabelSetEqualityPerformance(ce.getId());
}

static void testIntervalEqualityPerformance(const ConstraintEngineId& ce) {
  IntervalIntDomain intSort(-1000, 1000);
  Variable<IntervalIntDomain> v0(ce, intSort);
  Variable<IntervalIntDomain> v1(ce, intSort);
  Variable<IntervalIntDomain> v2(ce, intSort);
  Variable<IntervalIntDomain> v3(ce, intSort);
  Variable<IntervalIntDomain> v4(ce, intSort);
  Variable<IntervalIntDomain> v5(ce, intSort);
  Variable<IntervalIntDomain> v6(ce, intSort);
  Variable<IntervalIntDomain> v7(ce, intSort);
  Variable<IntervalIntDomain> v8(ce, intSort);
  Variable<IntervalIntDomain> v9(ce, intSort);

  std::vector<ConstrainedVariableId> variables;

  variables.push_back(v0.getId());
  variables.push_back(v1.getId());
  EqualConstraint c0(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v1.getId());
  variables.push_back(v2.getId());
  EqualConstraint c1(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v2.getId());
  variables.push_back(v3.getId());
  EqualConstraint c2(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v3.getId());
  variables.push_back(v4.getId());
  EqualConstraint c3(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v4.getId());
  variables.push_back(v5.getId());
  EqualConstraint c4(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v5.getId());
  variables.push_back(v6.getId());
  EqualConstraint c5(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v6.getId());
  variables.push_back(v7.getId());
  EqualConstraint c6(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v7.getId());
  variables.push_back(v8.getId());
  EqualConstraint c7(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v8.getId());
  variables.push_back(v9.getId());
  EqualConstraint c8(LabelStr("Equal"), LabelStr("Equal"), ce, variables);

  variables.clear();
  variables.push_back(v0.getId());
  variables.push_back(v1.getId());
  variables.push_back(v2.getId());
  variables.push_back(v3.getId());
  variables.push_back(v4.getId());
  variables.push_back(v5.getId());
  variables.push_back(v6.getId());
  variables.push_back(v7.getId());
  variables.push_back(v8.getId());
  variables.push_back(v9.getId());

  Variable<IntervalIntDomain>*  p_v0 = (Variable<IntervalIntDomain>*) v0.getId();

  int lb = -1000;
  int ub = 1000;

  for (int i = 10; i > 2; i--) {
    lb += 100;
    ub -= 100;
    IntervalIntDomain newDomain(lb, ub);
    Variable<IntervalIntDomain>* p_v = (Variable<IntervalIntDomain>*) variables[i-1];
    p_v->specify(newDomain);
    ce->propagate();
    assert(ce->constraintConsistent());
    assert(p_v0->getDerivedDomain().getUpperBound() == ub);
    assert(p_v0->getDerivedDomain().getLowerBound() == lb);
  }
}

static void outerLoopIntervalEqualConstraint(bool useEquivalenceClasses) {
  ConstraintEngine ce;

  if (useEquivalenceClasses)
    new EqualityConstraintPropagator(LabelStr("Equal"), ce.getId());
  else
    new DefaultPropagator(LabelStr("Equal"), ce.getId());

  for (int i = 0; i < 1000; i++)
    testIntervalEqualityPerformance(ce.getId());
}

static void testCopyingBoolDomains() {
  AbstractDomain *copyPtr;
  BoolDomain falseDom(false);
  BoolDomain trueDom(true);
  BoolDomain both;

  copyPtr = falseDom.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::BOOL);
  assertTrue((dynamic_cast<BoolDomain*>(copyPtr))->isFalse());
  assertFalse((dynamic_cast<BoolDomain*>(copyPtr))->isTrue());
  delete copyPtr;

  copyPtr = trueDom.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::BOOL);
  assertTrue((dynamic_cast<BoolDomain*>(copyPtr))->isTrue());
  assertFalse((dynamic_cast<BoolDomain*>(copyPtr))->isFalse());
  delete copyPtr;

  copyPtr = both.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::BOOL);
  assertFalse((dynamic_cast<BoolDomain*>(copyPtr))->isFalse());
  assertFalse((dynamic_cast<BoolDomain*>(copyPtr))->isTrue());
  delete copyPtr;

  // Cannot check that expected errors are detected until
  //   new error handling support is in use.
}

static void testCopyingEnumeratedDomains() {
  AbstractDomain *copyPtr;
  EnumeratedDomain emptyOpen;
  std::list<double> values;
  values.push_back(0.0);
  values.push_back(1.1);
  values.push_back(2.7);
  values.push_back(3.1);
  EnumeratedDomain fourDom(values, false); // Open
  values.push_back(4.2);
  EnumeratedDomain fiveDom(values); // Closed
  EnumeratedDomain oneDom(2.7); // Singleton

  copyPtr = emptyOpen.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::REAL_ENUMERATION);
  assertTrue(copyPtr->isDynamic());
  assertTrue(copyPtr->isNumeric());
  assertTrue(copyPtr->isEnumerated());
  copyPtr->insert(3.1);
  //assertFalse(copyPtr->isSingleton()); Or should that provoke an error? wedgingt 2004 Mar 3
  copyPtr->close();
  assertTrue(copyPtr->isSingleton());
  assertFalse(copyPtr->isDynamic());
  delete copyPtr;

  copyPtr = fourDom.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::REAL_ENUMERATION);
  assertTrue(copyPtr->isDynamic());
  assertTrue(copyPtr->isEnumerated());
  copyPtr->close();
  assertTrue(copyPtr->getSize() == 4);
  assertTrue(copyPtr->isSubsetOf(fiveDom));
  delete copyPtr;

  copyPtr = fiveDom.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::REAL_ENUMERATION);
  assertFalse(copyPtr->isDynamic());
  assertTrue(copyPtr->isEnumerated());
  assertTrue(copyPtr->getSize() == 5);
  assertTrue(fourDom.isSubsetOf(*copyPtr));
  delete copyPtr;

  copyPtr = oneDom.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::REAL_ENUMERATION);
  assertFalse(copyPtr->isDynamic());
  assertTrue(copyPtr->isEnumerated());
  assertTrue(copyPtr->isSingleton());
  assertTrue(copyPtr->isSubsetOf(fourDom));
  delete copyPtr;

  // Cannot check that expected errors are detected until
  //   new error handling support is in use.
}

static void testCopyingIntervalDomains() {
  AbstractDomain *copyPtr;
  IntervalDomain empty;
  IntervalDomain one2ten(1.0, 10.9);
  IntervalDomain four(4.0);
  // domains containing infinities should also be tested

  copyPtr = empty.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::REAL_INTERVAL);
  assertFalse(copyPtr->isDynamic());
  assertTrue(copyPtr->isNumeric());
  assertFalse(copyPtr->isEnumerated());
  assertTrue(copyPtr->isFinite());
  assertFalse(copyPtr->isMember(0.0));
  assertFalse(copyPtr->isSingleton());
  assertTrue(copyPtr->isEmpty());
  assertTrue(copyPtr->getSize() == 0);
  assertTrue(*copyPtr == empty);
  assertFalse(*copyPtr == one2ten);
  copyPtr->relax(IntervalDomain(-3.1, 11.0));
  assertTrue(copyPtr->isMember(0.0));
  assertFalse(copyPtr->isSingleton());
  assertFalse(copyPtr->isEmpty());
  assertTrue(empty.isEmpty());
  assertFalse(*copyPtr == empty);
  assertTrue(empty.isSubsetOf(*copyPtr));
  delete copyPtr;

  copyPtr = one2ten.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::REAL_INTERVAL);
  assertFalse(copyPtr->isDynamic());
  assertTrue(copyPtr->isNumeric());
  assertFalse(copyPtr->isEnumerated());
  assertFalse(copyPtr->isFinite());
  assertFalse(copyPtr->isMember(0.0));
  assertFalse(copyPtr->isSingleton());
  assertFalse(copyPtr->isEmpty());
  assertFalse(*copyPtr == empty);
  assertTrue(*copyPtr == one2ten);
  copyPtr->relax(IntervalDomain(-3.1, 11.0));
  assertTrue(copyPtr->isMember(0.0));
  assertFalse(copyPtr->isSingleton());
  assertFalse(copyPtr->isEmpty());
  assertFalse(*copyPtr == one2ten);
  assertTrue(one2ten.isSubsetOf(*copyPtr));
  delete copyPtr;

  copyPtr = four.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::REAL_INTERVAL);
  assertFalse(copyPtr->isDynamic());
  assertTrue(copyPtr->isNumeric());
  assertFalse(copyPtr->isEnumerated());
  assertTrue(copyPtr->isFinite());
  assertFalse(copyPtr->isMember(0.0));
  assertTrue(copyPtr->isSingleton());
  assertFalse(copyPtr->isEmpty());
  assertTrue(copyPtr->getSize() == 1);
  assertFalse(*copyPtr == empty);
  assertTrue(*copyPtr == four);
  assertFalse(*copyPtr == one2ten);
  copyPtr->relax(IntervalDomain(-3.1, 11.0));
  assertTrue(copyPtr->isMember(0.0));
  assertFalse(copyPtr->isSingleton());
  assertFalse(copyPtr->isEmpty());
  assertFalse(*copyPtr == empty);
  assertFalse(*copyPtr == four);
  assertTrue(four.isSubsetOf(*copyPtr));
  delete copyPtr;

  // Cannot check that expected errors are detected until
  //   new error handling support is in use.
}

static void testCopyingIntervalIntDomains() {
  AbstractDomain *copyPtr;
  IntervalIntDomain empty;
  IntervalIntDomain one2ten(1, 10);
  IntervalIntDomain four(4);
  // domains containing infinities should also be tested

  copyPtr = empty.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::INT_INTERVAL);
  assertFalse(copyPtr->isDynamic());
  assertTrue(copyPtr->isNumeric());
  assertFalse(copyPtr->isEnumerated());
  assertTrue(copyPtr->isFinite());
  assertFalse(copyPtr->isMember(0));
  assertFalse(copyPtr->isSingleton());
  assertTrue(copyPtr->isEmpty());
  assertTrue(copyPtr->getSize() == 0);
  assertTrue(*copyPtr == empty);
  assertFalse(*copyPtr == one2ten);
  copyPtr->relax(IntervalDomain(-3, 11));
  assertTrue(copyPtr->isMember(0));
  assertFalse(copyPtr->isSingleton());
  assertFalse(copyPtr->isEmpty());
  assertTrue(empty.isEmpty());
  assertFalse(*copyPtr == empty);
  assertTrue(empty.isSubsetOf(*copyPtr));
  delete copyPtr;

  copyPtr = one2ten.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::INT_INTERVAL);
  assertFalse(copyPtr->isDynamic());
  assertTrue(copyPtr->isNumeric());
  assertFalse(copyPtr->isEnumerated());
  assertTrue(copyPtr->isFinite());
  assertFalse(copyPtr->isMember(0));
  assertFalse(copyPtr->isSingleton());
  assertFalse(copyPtr->isEmpty());
  assertTrue(copyPtr->getSize() == 10);
  assertFalse(*copyPtr == empty);
  assertTrue(*copyPtr == one2ten);
  copyPtr->relax(IntervalIntDomain(-3, 11));
  assertTrue(copyPtr->getSize() == 15);
  assertTrue(copyPtr->isMember(0));
  assertFalse(copyPtr->isSingleton());
  assertFalse(copyPtr->isEmpty());
  assertFalse(*copyPtr == one2ten);
  assertTrue(one2ten.isSubsetOf(*copyPtr));
  delete copyPtr;

  copyPtr = four.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::INT_INTERVAL);
  assertFalse(copyPtr->isDynamic());
  assertTrue(copyPtr->isNumeric());
  assertFalse(copyPtr->isEnumerated());
  assertTrue(copyPtr->isFinite());
  assertFalse(copyPtr->isMember(0));
  assertTrue(copyPtr->isSingleton());
  assertFalse(copyPtr->isEmpty());
  assertTrue(copyPtr->getSize() == 1);
  assertFalse(*copyPtr == empty);
  assertTrue(*copyPtr == four);
  assertFalse(*copyPtr == one2ten);
  copyPtr->relax(IntervalIntDomain(-3, 11));
  assertTrue(copyPtr->getSize() == 15);
  assertTrue(copyPtr->isMember(0));
  assertFalse(copyPtr->isSingleton());
  assertFalse(copyPtr->isEmpty());
  assertFalse(*copyPtr == empty);
  assertFalse(*copyPtr == four);
  assertTrue(four.isSubsetOf(*copyPtr));
  delete copyPtr;

  // Cannot check that expected errors are detected until
  //   new error handling support is in use.
}

// Has to be outside next function or compiler complains.
typedef enum Fruits { orange, lemon, blueberry, raspberry } Fruits;

static void testCopyingTemplateDomains() {
  AbstractDomain *copyPtr;
  Domain<Fruits> emptyFruit;
  Domain<Fruits> orangeOnly(orange);
  std::list<Fruits> fruitList;
  fruitList.push_back(lemon);
  fruitList.push_back(raspberry);
  fruitList.push_back(orange);
  Domain<Fruits> fruitDom(fruitList);

  // Should test dynamic domains

  copyPtr = emptyFruit.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::USER_DEFINED);
  assertFalse(copyPtr->isDynamic());
  assertFalse(copyPtr->isNumeric());
  assertTrue(copyPtr->isEnumerated());
  assertFalse(copyPtr->isInterval());
  assertTrue(copyPtr->isFinite());
  assertFalse(copyPtr->isMember(orange));
  assertFalse(copyPtr->isSingleton());
  assertTrue(copyPtr->isEmpty());
  assertTrue(copyPtr->getSize() == 0);
  assertTrue(*copyPtr == emptyFruit);
  assertFalse(*copyPtr == orangeOnly);
  assertFalse(*copyPtr == fruitDom);
  copyPtr->reset(fruitDom);
  assertFalse(*copyPtr == emptyFruit);
  assertFalse(*copyPtr == orangeOnly);
  assertTrue(*copyPtr == fruitDom);
  delete copyPtr;

  copyPtr = orangeOnly.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::USER_DEFINED);
  assertFalse(copyPtr->isDynamic());
  assertFalse(copyPtr->isNumeric());
  assertTrue(copyPtr->isEnumerated());
  assertTrue(copyPtr->isFinite());
  assertFalse(copyPtr->isMember(lemon));
  assertTrue(copyPtr->isMember(orange));
  assertTrue(copyPtr->isSingleton());
  assertFalse(copyPtr->isEmpty());
  assertTrue(copyPtr->getSize() == 1);
  assertFalse(*copyPtr == emptyFruit);
  assertTrue(*copyPtr == orangeOnly);
  assertFalse(*copyPtr == fruitDom);
  assertFalse(copyPtr->intersect(fruitDom));
  assertTrue(copyPtr->isSingleton());
  assertFalse(copyPtr->isEmpty());
  assertTrue(emptyFruit.isSubsetOf(*copyPtr));
  assertTrue(orangeOnly.isSubsetOf(*copyPtr));
  assertFalse(fruitDom.isSubsetOf(*copyPtr));
  assertTrue(copyPtr->isSubsetOf(Domain<Fruits>(orange)));
  assertTrue(copyPtr->isSubsetOf(fruitDom));
  assertFalse(copyPtr->isSubsetOf(emptyFruit));
  delete copyPtr;

  copyPtr = fruitDom.copy();
  assertTrue(copyPtr->getType() == AbstractDomain::USER_DEFINED);
  assertFalse(copyPtr->isDynamic());
  assertFalse(copyPtr->isNumeric());
  assertTrue(copyPtr->isEnumerated());
  assertTrue(copyPtr->isFinite());
  assertTrue(copyPtr->isMember(lemon));
  assertTrue(copyPtr->isMember(orange));
  assertFalse(copyPtr->isMember(blueberry));
  assertFalse(copyPtr->isSingleton());
  assertFalse(copyPtr->isEmpty());
  assertTrue(copyPtr->getSize() == 3);
  assertFalse(*copyPtr == emptyFruit);
  assertFalse(*copyPtr == orangeOnly);
  assertTrue(*copyPtr == fruitDom);
  assertFalse(copyPtr->intersect(orangeOnly));
  assertTrue(copyPtr->isSingleton());
  assertFalse(copyPtr->isEmpty());
  assertTrue(emptyFruit.isSubsetOf(*copyPtr));
  assertTrue(orangeOnly.isSubsetOf(*copyPtr));
  assertFalse(fruitDom.isSubsetOf(*copyPtr));
  assertTrue(copyPtr->isSubsetOf(Domain<Fruits>(orange)));
  assertTrue(copyPtr->isSubsetOf(fruitDom));
  assertFalse(copyPtr->isSubsetOf(emptyFruit));
  delete copyPtr;

  // Cannot check that expected errors are detected until
  //   new error handling support is in use.
}

static void testCopy() {
  testCopyingBoolDomains();
  testCopyingEnumeratedDomains();
  testCopyingIntervalDomains();
  testCopyingIntervalIntDomains();
  testCopyingTemplateDomains();
}

int main() {
  //outerLoopForTestEquate();
  outerLoopForTestIntersection();
  //outerLoopLabelSetEqualConstraint(true);
  //outerLoopIntervalEqualConstraint(true);
  testCopy();
  cout << "Finished" << endl;
}
