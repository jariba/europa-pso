#include "TestSupport.hh"
#include "IntervalIntDomain.hh"
#include "BoolDomain.hh"
#include "EnumeratedDomain.hh"
#include "LabelStr.hh"
#include "DomainListener.hh"
#include "Domain.hh"
#include "domain-tests.hh"

#ifdef __sun
#include <strstream>
typedef std::strstream sstream;
#else
#include <sstream>
typedef std::stringstream sstream;
#endif

namespace Prototype {

  class ChangeListener: public DomainListener {
  public:
    ChangeListener(): m_changed(false), m_change(RESET){}

    void notifyChange(const ChangeType& change){
      m_changed = true;
      m_change = change;
    }

    bool checkAndClearChange(ChangeType& change) {
      bool result = m_changed;
      change = m_change;
      m_changed = false;
      return (result);
    }

  private:
    bool m_changed;
    ChangeType m_change;
  };

  class IntervalDomainTest
  {
  public:
    static bool test() {
      runTest(testAllocation); 
      runTest(testRelaxation); 
      runTest(testIntersection);  
      runTest(testSubset);
      runTest(testPrinting);
      runTest(testBoolDomain);
      runTest(testDifference);
      runTest(testOperatorEquals);
      runTest(testInfinitesAndInts);
      return true;
    }

  private:
    static bool testAllocation(){
      IntervalIntDomain intDomain(10, 20);
      check_error(intDomain.isFinite());
      check_error(!intDomain.isDynamic());
      IntervalIntDomain d1(intDomain);
      d1.empty();
      check_error(d1.isEmpty());

      AbstractDomain& d2 = static_cast<AbstractDomain&>(intDomain);
      check_error(!d2.isEmpty());

      IntervalIntDomain d3(static_cast<IntervalIntDomain&>(intDomain));
      IntervalIntDomain d4;

      check_error( ! (d3 == d4));
      d3.relax(d4);
      check_error(d3 == d4);

      check_error(d2 != d4);
      d2.relax(d4);
      check_error(d2 == d4);
      return true;
    }

    static bool testRelaxation(){
      ChangeListener l_listener;
      IntervalIntDomain dom0; // Will have very large default range
      IntervalIntDomain dom1(-100, 100, l_listener.getId());
      dom1.relax(dom0);
      DomainListener::ChangeType change;
      check_error(l_listener.checkAndClearChange(change)  && change == DomainListener::RELAXED);
      check_error(dom1.isSubsetOf(dom0));
      check_error(dom0.isSubsetOf(dom1));
      check_error(dom1 == dom0);

      IntervalIntDomain dom2(-300, 100);
      dom1.intersect(dom2);
      check_error(l_listener.checkAndClearChange(change));
      check_error(dom1 == dom2);
      dom1.relax(dom2);
      check_error(!l_listener.checkAndClearChange(change));
      return true;
    }

    static bool testIntersection() {
      ChangeListener l_listener;
      IntervalIntDomain dom0(l_listener.getId()); // Will have very large default range

      // Execute intersection and verify results
      IntervalIntDomain dom1(-100, 100);
      dom0.intersect(dom1);
      DomainListener::ChangeType change;
      check_error(l_listener.checkAndClearChange(change));
      check_error(dom0 == dom1);
    
      // verify no change triggered if none should take place.
      dom0.intersect(dom1);
      check_error(!l_listener.checkAndClearChange(change));

      // Verify only the upper bound changes
      IntervalIntDomain dom2(-200, 50);
      dom0.intersect(dom2);
      check_error(l_listener.checkAndClearChange(change));
      check_error(dom0.getLowerBound() == dom1.getLowerBound());
      check_error(dom0.getUpperBound() == dom2.getUpperBound());
    
      // Make an intersection that leads to an empty domain
      IntervalIntDomain dom3(500, 1000);
      dom0.intersect(dom3);
      check_error(l_listener.checkAndClearChange(change));
      check_error(dom0.isEmpty());

      IntervalDomain dom4(0.98, 101.23);
      IntervalDomain dom5(80, 120.44);
      IntervalDomain dom6(80, 101.23);
      dom4.equate(dom5);
      check_error(dom4 == dom6);
      check_error(dom5 == dom6);
      return true;
    }

    static bool testSubset(){
      IntervalIntDomain dom0(10, 35);
      IntervalDomain dom1(0, 101);
      check_error(dom0.isSubsetOf(dom1));
      check_error(! dom1.isSubsetOf(dom0));

      // Handle cases where domains are equal
      IntervalIntDomain dom2(dom0);
      check_error(dom2 == dom0);
      check_error(dom0.isSubsetOf(dom2));
      check_error(dom2.isSubsetOf(dom0));

      // Handle case with no intersection
      IntervalIntDomain dom3(0, 9);
      check_error(! dom3.isSubsetOf(dom0));
      check_error(! dom0.isSubsetOf(dom3));

      // Handle case with partial intersection
      IntervalIntDomain dom4(0, 20);
      check_error(! dom4.isSubsetOf(dom0));
      check_error(! dom0.isSubsetOf(dom4));

      return true;
    }

    static bool testListener()
    {
      return true;
    }

    static bool testPrinting(){
      IntervalIntDomain d1(1, 100);
      //       std::stringstream ss1;
      //       d1 >> ss1;
      sstream ss1;
      d1 >> ss1;
      std::string actualString = ss1.str();
      std::string expectedString("INT_INTERVAL:CLOSED[1, 100]");
      check_error(actualString == expectedString);
      return true;
    }

    static bool testBoolDomain(){
      BoolDomain dom0;
      check_error(dom0.getSize() == 2);
      check_error(dom0.getUpperBound() == true);
      check_error(dom0.getLowerBound() == false);

      IntervalIntDomain dom1(0, 100);
      dom1.intersect(dom0);
      check_error(dom1 == dom0);
      return true;
    }

    static bool testDifference(){
      IntervalDomain dom0(1, 10);
      IntervalDomain dom1(11, 20);
      check_error(!dom0.difference(dom1));
      check_error(!dom1.difference(dom0));

      IntervalDomain dom2(dom0);
      check_error(dom2.difference(dom0));
      check_error(dom2.isEmpty());

      IntervalIntDomain dom3(5, 100);
      check_error(dom3.difference(dom0));
      check_error(dom3.getLowerBound() == 11);
      check_error(dom3.difference(dom1));
      check_error(dom3.getLowerBound() == 21);

      IntervalDomain dom4(0, 20);
      check_error(dom4.difference(dom1));
      double newValue = (dom1.getLowerBound() - dom4.minDelta());
      check_error(dom4.getUpperBound() == newValue);
      return true;
    }

    static bool testOperatorEquals(){
      IntervalDomain dom0(1, 28);
      IntervalDomain dom1(50, 100);
      dom0 = dom1;
      check_error(dom0 == dom1);
      return true;
    }

    static bool testInfinitesAndInts(){
      IntervalDomain dom0;
      check_error(dom0.translateNumber(MINUS_INFINITY) == MINUS_INFINITY);
      check_error(dom0.translateNumber(MINUS_INFINITY - 1) == MINUS_INFINITY);
      check_error(dom0.translateNumber(MINUS_INFINITY + 1) == MINUS_INFINITY + 1);
      check_error(dom0.translateNumber(PLUS_INFINITY + 1) == PLUS_INFINITY);
      check_error(dom0.translateNumber(PLUS_INFINITY - 1) == PLUS_INFINITY - 1);
      check_error(dom0.translateNumber(2.8) == 2.8);

      IntervalIntDomain dom1;
      check_error(dom1.translateNumber(2.8, false) == 2);
      check_error(dom1.translateNumber(2.8, true) == 3);
      check_error(dom1.translateNumber(PLUS_INFINITY - 0.2, false) == PLUS_INFINITY - 1);
      return true;
    }
  };

  class EnumeratedDomainTest{
  public:
    static bool test(){
      runTest(testEnumerationOnly);
      runTest(testBasicLabelOperations);
      runTest(testLabelSetAllocations);
      runTest(testEquate);
      runTest(testValueRetrieval);
      runTest(testIntersection);
      runTest(testDifference);
      runTest(testOperatorEquals);
      return true;
    }
  private:

    static bool testEnumerationOnly(){
      std::list<double> values;
      values.push_back(-98.67);
      values.push_back(-0.01);
      values.push_back(1);
      values.push_back(2);
      values.push_back(10);
      values.push_back(11);

      EnumeratedDomain d0(values);
      EnumeratedDomain d1(values);
      check_error(d0 == d1);
      check_error(d0.isSubsetOf(d1));
      check_error(d0.isMember(-98.67));
      d0.remove(-0.01);
      check_error(!d0.isMember(-0.01));
      check_error(d0.isSubsetOf(d1));
      check_error(!d1.isSubsetOf(d0));

      return true;
    }

    static bool testBasicLabelOperations() {
      int initialCount = Prototype::LabelStr::getSize();
      Prototype::LabelStr l1("L1");
      Prototype::LabelStr l2("L2");
      Prototype::LabelStr l3("L3");
      check_error(l1 < l2 && l2 < l3);

      Prototype::LabelStr la("L");
      Prototype::LabelStr l4("L30");
      Prototype::LabelStr lb("L");

      Prototype::LabelStr copy1(l1);
      check_error(l1 == copy1);
      check_error (l2 != copy1);

      check_error((Prototype::LabelStr::getSize() - initialCount) == 5);
      check_error(l1.toString() == "L1");

      check_error(LabelStr::isString(l1.getKey()));
      check_error(!LabelStr::isString(PLUS_INFINITY+1));
      return true;
    }

    static bool testLabelSetAllocations(){
      std::list<Prototype::LabelStr> values;
      values.push_back(Prototype::LabelStr("L1"));
      values.push_back(Prototype::LabelStr("L4"));
      values.push_back(Prototype::LabelStr("L2"));
      values.push_back(Prototype::LabelStr("L5"));
      values.push_back(Prototype::LabelStr("L3"));

      ChangeListener l_listener;
      LabelSet ls0(values, true, l_listener.getId());
      check_error(!ls0.isDynamic());

      Prototype::LabelStr l2("L2");
      check_error(ls0.isMember(l2));
      DomainListener::ChangeType change;
      ls0.remove(l2);
      check_error(l_listener.checkAndClearChange(change) && change == DomainListener::VALUE_REMOVED);
      check_error(!ls0.isMember(l2));

      Prototype::LabelStr l3("L3");
      ls0.set(l3);
      check_error(ls0.isMember(l3));
      check_error(ls0.getSize() == 1);

      LabelSet ls1(values, true);
      ls0.relax(ls1);
      check_error(l_listener.checkAndClearChange(change) && change == DomainListener::RELAXED);
      check_error(ls0 == ls1);
      return true;
    }
    static bool testEquate(){
      std::list<Prototype::LabelStr> baseValues;
      baseValues.push_back(Prototype::LabelStr("A"));
      baseValues.push_back(Prototype::LabelStr("B"));
      baseValues.push_back(Prototype::LabelStr("C"));
      baseValues.push_back(Prototype::LabelStr("D"));
      baseValues.push_back(Prototype::LabelStr("E"));
      baseValues.push_back(Prototype::LabelStr("F"));
      baseValues.push_back(Prototype::LabelStr("G"));
      baseValues.push_back(Prototype::LabelStr("H"));

      ChangeListener l_listener;
      LabelSet ls0(baseValues, true, l_listener.getId());
      LabelSet ls1(baseValues, true, l_listener.getId());

      check_error(ls0 == ls1);
      check_error(ls0.getSize() == 8);
      check_error(ls0.equate(ls1) == false); // Implying no change occured

      Prototype::LabelStr lC("C");
      ls0.remove(lC);
      check_error(!ls0.isMember(lC));
      check_error(ls1.isMember(lC));
      check_error(ls0.equate(ls1)); // It should have changed
      check_error(!ls1.isMember(lC));

      LabelSet ls2(baseValues, true, l_listener.getId());
      ls2.remove(Prototype::LabelStr("A"));
      ls2.remove(Prototype::LabelStr("B"));
      ls2.remove(Prototype::LabelStr("C"));
      ls2.remove(Prototype::LabelStr("D"));
      ls2.remove(Prototype::LabelStr("E"));

      LabelSet ls3(baseValues, true, l_listener.getId());
      Prototype::LabelStr lA("A");
      Prototype::LabelStr lB("B");
      ls3.remove(lA);
      ls3.remove(lB);
      ls3.remove(lC);
      check_error(ls2.equate(ls3));
      check_error(ls2 == ls3);

      LabelSet ls4(baseValues, true, l_listener.getId());
      ls4.remove(Prototype::LabelStr("A"));
      ls4.remove(Prototype::LabelStr("B"));
      ls4.remove(Prototype::LabelStr("C"));
      ls4.remove(Prototype::LabelStr("D"));
      ls4.remove(Prototype::LabelStr("E"));

      LabelSet ls5(baseValues, true, l_listener.getId());
      ls5.remove(Prototype::LabelStr("F"));
      ls5.remove(Prototype::LabelStr("G"));
      ls5.remove(Prototype::LabelStr("H"));

      DomainListener::ChangeType change;
      ls4.equate(ls5);
      check_error(l_listener.checkAndClearChange(change) && change == DomainListener::EMPTIED);
      check_error(ls4.isEmpty() || ls5.isEmpty());
      check_error(!(ls4.isEmpty() && ls5.isEmpty()));

      return true;
    }

    static bool testValueRetrieval(){
      std::list<Prototype::LabelStr> values;
      values.push_back(Prototype::LabelStr("A"));
      values.push_back(Prototype::LabelStr("B"));
      values.push_back(Prototype::LabelStr("C"));
      values.push_back(Prototype::LabelStr("D"));
      values.push_back(Prototype::LabelStr("E"));

      LabelSet l1(values, true);
      std::list<Prototype::LabelStr> results;
      l1.getValues(results);

      LabelSet l2(results, true);

      check_error(l1 == l2);
      LabelStr lbl("C");
      l1.set(lbl);
      check_error(lbl == l1.getSingletonValue());
      return true;
    }

    static bool testIntersection(){
      std::list<Prototype::LabelStr> values;
      values.push_back(Prototype::LabelStr("A"));
      values.push_back(Prototype::LabelStr("B"));
      values.push_back(Prototype::LabelStr("C"));
      values.push_back(Prototype::LabelStr("D"));
      values.push_back(Prototype::LabelStr("E"));
      values.push_back(Prototype::LabelStr("F"));
      values.push_back(Prototype::LabelStr("G"));
      values.push_back(Prototype::LabelStr("H"));
      values.push_back(Prototype::LabelStr("I"));
      LabelSet ls1(values);

      LabelSet ls2(values);
      ls2.remove(Prototype::LabelStr("A"));
      ls2.remove(Prototype::LabelStr("C"));
      ls2.remove(Prototype::LabelStr("E"));
      check_error(ls2.isSubsetOf(ls1));
      check_error(!ls1.isSubsetOf(ls2));

      LabelSet ls3(ls1);

      ls1.intersect(ls2);
      check_error(ls1 == ls2);
      check_error(ls2.isSubsetOf(ls1));

      ls1.relax(ls3);
      check_error(ls2.isSubsetOf(ls1));
      check_error(ls1 == ls3);

      LabelSet ls4(values);
      ls4.remove(Prototype::LabelStr("A"));
      ls4.remove(Prototype::LabelStr("B"));
      ls4.remove(Prototype::LabelStr("C"));
      ls4.remove(Prototype::LabelStr("D"));
      ls4.remove(Prototype::LabelStr("E"));
      ls4.remove(Prototype::LabelStr("F"));
      ls4.remove(Prototype::LabelStr("G"));

      ls3.remove(Prototype::LabelStr("H"));
      ls3.remove(Prototype::LabelStr("I"));
      ls4.intersect(ls3);
      check_error(ls4.isEmpty());
      return true;
    }

    static bool testDifference(){

      EnumeratedDomain dom0;
      dom0.insert(1);
      dom0.insert(3);
      dom0.insert(2);
      dom0.insert(8);
      dom0.insert(10);
      dom0.insert(6);
      dom0.close();

      IntervalIntDomain dom1(11, 100);
      check_error(!dom0.difference(dom1));

      IntervalIntDomain dom2(5, 100);
      check_error(dom0.difference(dom2));
      check_error(dom0.getUpperBound() == 3);

      IntervalIntDomain dom3(0, 100);
      check_error(dom0.difference(dom3));
      check_error(dom0.isEmpty());

      return true;
    }

    static bool testOperatorEquals(){
      EnumeratedDomain dom0;
      dom0.insert(1);
      dom0.insert(3);
      dom0.insert(2);
      dom0.insert(8);
      dom0.insert(10);
      dom0.insert(6);
      dom0.close();

      EnumeratedDomain dom1;
      dom1.insert(1);
      dom1.insert(3);
      dom1.insert(2);
      dom1.close();

      EnumeratedDomain dom2(dom0);

      check_error(dom0 != dom1);
      dom0 = dom1;
      check_error(dom0 == dom1);

      dom1 = dom2;
      check_error(dom1 == dom2);

      return true;
    }
  };

  class MixedTypeTest{
  public:
    static bool test() {
      runTest(testEquality);
      runTest(testIntersection);
      runTest(testSubset);
      runTest(testIntDomain);
      return true;
    }
  private:
    static bool testEquality(){
      EnumeratedDomain dom;
      dom.insert(1.0);
      dom.insert(2.0);
      dom.close();

      EnumeratedDomain dom0(dom);
      dom0.set(1.0);

      IntervalDomain dom1(1.0);
      check_error(dom1 == dom0);
      check_error(dom0 == dom1);

      IntervalIntDomain dom2(1);
      check_error(dom1 == dom2);

      dom0.reset(dom);
      IntervalIntDomain dom3(1, 2);
      check_error(dom0 == dom3);
      return true;
    }

    static bool testIntersection(){
      EnumeratedDomain dom0;
      dom0.insert(0);
      dom0.insert(0.98);
      dom0.insert(1.0);
      dom0.insert(1.89);
      dom0.insert(2.98);
      dom0.insert(10);
      dom0.close();
      check_error(dom0.getSize() == 6);
      IntervalIntDomain dom1(1, 8);
      EnumeratedDomain dom2(dom0);

      dom0.intersect(dom1);
      check_error(dom0.getSize() == 1);
      check_error(dom0.isMember(1.0));

      IntervalDomain dom3(1, 8);
      dom2.intersect(dom3);
      check_error(dom2.getSize() == 3);

      BoolDomain dom4;
      dom2.intersect(dom4);
      check_error(dom2.getSize() == 1);
      return true;
    }

    static bool testSubset(){
      EnumeratedDomain dom0;
      dom0.insert(0);
      dom0.insert(0.98);
      dom0.insert(1.0);
      dom0.insert(1.89);
      dom0.insert(2.98);
      dom0.insert(10);
      dom0.close();

      IntervalDomain dom1(0, 10);
      check_error(dom0.isSubsetOf(dom1));

      IntervalIntDomain dom2(0, 10);
      check_error(!dom0.isSubsetOf(dom2));

      dom0.remove(0.98);
      dom0.remove(1.89);
      dom0.remove(2.98);
      check_error(dom0.isSubsetOf(dom2));

      check_error(dom2.isSubsetOf(dom1));
      check_error(!dom1.isSubsetOf(dom2));
      return true;
    }

    static bool testIntDomain(){
      Domain<int> dom0;
      dom0.insert(10);
      dom0.insert(12);
      dom0.close();

      Domain<float> dom1;
      dom1.insert(9.98);
      dom1.insert(9.037);
      dom1.close();

      check_error(dom0 != dom1);

      Domain<int> dom2(10);
      check_error(!dom2.isDynamic());
      check_error(dom2.isSingleton());
      return true;
    }
  };
}

using namespace Prototype;

bool DomainTests::test(){
  runTestSuite(IntervalDomainTest::test);
  runTestSuite(EnumeratedDomainTest::test);
  runTestSuite(MixedTypeTest::test);
  return true;
}
