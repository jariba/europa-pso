package gov.nasa.arc.planworks.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.*;

import gov.nasa.arc.planworks.util.BooleanFunctor;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.UnaryFunctor;

public class PlanWorksUtilsTest extends TestCase {
  public static void main(String [] args) {
    junit.textui.TestRunner.run(suite());
  }
  public static TestSuite suite() {
    final TestSuite suite = new TestSuite();
    suite.addTest(new PlanWorksUtilsTest("testCollectionMaps"));
    suite.addTest(new PlanWorksUtilsTest("testCollectionGreps"));
    suite.addTest(new PlanWorksUtilsTest("testMapUtils"));
    suite.addTest(new PlanWorksUtilsTest("testListUtils"));
    return suite;
  }
  public PlanWorksUtilsTest(String testType) {
    super(testType);
  }
  public void testCollectionMaps() {
		System.err.println("In testCollectionMaps");
    Integer [] array = new Integer[4];
    for(int i = 0; i < array.length; i++) {
      array[i] = new Integer(i);
    }

    Object [] mappedArray = CollectionUtils.aMap(new IncrementFunctor(), array);
    assertTrue("Mapped array isn't the same length", array.length == mappedArray.length);
    for(int i = 0; i < mappedArray.length; i++) {
      assertTrue("Mapped array value isn't of correct type: " + 
                 mappedArray[i].getClass().getName(),
                 mappedArray[i] instanceof Integer);
      assertTrue("Mapped array value isn't incremented.",
                 ((Integer)mappedArray[i]).intValue() == array[i].intValue() + 1);
    }
    
    List list = new ArrayList();
    for(int i = 0; i < 4; i++) {
      list.add(new Integer(i));
    }

    assertTrue("Mapped list started empty!", list.size() != 0);
    List mappedList = CollectionUtils.lMap(new IncrementFunctor(), list);
    assertTrue("List was emptied!", list.size() != 0);
    assertTrue("Mapped list was emptied!", mappedList.size() != 0);
    assertTrue("Mapped list isn't the same length", list.size() == mappedList.size());
    for(int i = 0; i < mappedList.size(); i++) {
      assertTrue("Mapped list value isn't incremented.",
                 ((Integer)mappedList.get(i)).intValue() ==
                 ((Integer)list.get(i)).intValue() + 1);
    }
    
    int beforeLength = array.length;
    CollectionUtils.aInPlaceMap(new IncrementFunctor(), array);
    
    assertTrue("In-place mapped array isn't the same size.", beforeLength == array.length);
    for(int i = 0; i < array.length; i++) {
      assertTrue("In-place mapped array wasn't incremented.", array[i].intValue() == i + 1);
    }

    List inPlaceList = new ArrayList(list.size());
    for(int i = 0; i < list.size(); i++) {
      inPlaceList.add(new InPlaceInteger(i));
    }
    CollectionUtils.lInPlaceMap(new InPlaceIncrementFunctor(), inPlaceList);
    assertTrue("List was emptied!", inPlaceList.size() != 0);
    assertTrue("List size was changed!", inPlaceList.size() == list.size());
    for(int i = 0; i < inPlaceList.size(); i++) {
      assertTrue("In-place mapped list wasn't incremented.",
                 ((InPlaceInteger)inPlaceList.get(i)).intValue() == i + 1);
    }
		System.err.println("testCollectionMaps done");
  }

  public void testCollectionGreps() {
		System.err.println("In testCollectionGreps");
    Integer [] array = new Integer[4];
    for(int i = 0; i < array.length; i++) {
      array[i] = new Integer(i);
    }
    
    List list = new ArrayList();
    for(int i = 0; i < 4; i++) {
      list.add(new Integer(i));
    }

    int compInt = 1;

    Object [] greppedArray = CollectionUtils.aGrep(new GreaterThanFunctor(compInt), array);
    assertTrue("Array was emptied!", array.length != 0);
    assertTrue("grepped array was emptied!", greppedArray.length != 0);
    assertTrue("grep-array failed to eliminate elements.", greppedArray.length < array.length);
    for(int i = 0; i < greppedArray.length; i++) {
      assertTrue("grep-array has wrong type.", greppedArray[i] instanceof Integer);
      assertTrue("grep-array failed to eliminate correct elements.",
                 ((Integer) greppedArray[i]).intValue() > compInt);
    }

    List greppedList = CollectionUtils.lGrep(new GreaterThanFunctor(compInt), list);
    assertTrue("List was emptied!", list.size() != 0);
    assertTrue("grepped-list was emptied!", greppedList.size() != 0);
    assertTrue("Grep-list failed to eliminate elements.", list.size() > greppedList.size());
    for(int i = 0; i < greppedList.size(); i++) {
      assertTrue("grep-list has wrong type.", greppedList.get(i) instanceof Integer);
      assertTrue("Grep-list failed to eliminate correct elements.",
                 ((Integer)greppedList.get(i)).intValue() > compInt);
    }

    int startSize = list.size();
    CollectionUtils.lInPlaceGrep(new GreaterThanFunctor(compInt), list);
    assertTrue("List was emptied!", list.size() != 0);
    assertTrue("In-place grep list failed to eliminate elements.", list.size() < startSize);
    for(int i = 0; i < list.size(); i++) {
      assertTrue("In-place grep list failed to eliminate correct elements.",
                 ((Integer)list.get(i)).intValue() > compInt);
    }
		System.err.println("testCollectionGreps done");
  }

  //testMapUtils
  public void testMapUtils() {
		System.err.println("In testMapUtils");
    Map testMap = new HashMap();
    testMap.put("foo", null);
    testMap.put("bar", "mumble");
    testMap.put("baz", new Integer(0));
    testMap.put("quux", "wango");
    testMap.put("argle", null);
    List testList = Arrays.asList(new Object [] {"foo", "bar", "quux", "argle", "bargle"});

    List vals = CollectionUtils.validValues(testMap);
    assertTrue("ValidValues returned incorrect number of values: " + vals.size(),
               vals.size() == 3);
    assertTrue("ValidValues returned incorrect values: " + vals,
               vals.contains("mumble") && vals.contains(new Integer(0)) && vals.contains("wango"));

    for(Iterator i = vals.iterator(); i.hasNext();) {
      assertTrue("ValidValues returned a null value: " + vals, i.next() != null);
    }

    List keys = CollectionUtils.validKeys(testMap, testList);
    assertTrue("ValidKeys returned incorrect number of keys: " + keys.size(),
               keys.size() == 2);
    assertTrue("ValidKeys returned incorrect keys: " + keys,
               keys.contains("bar") && keys.contains("quux"));

    for(Iterator i = keys.iterator(); i.hasNext();) {
      Object o = i.next();
      assertTrue("ValidKeys returned an invalid key: " + o, testMap.get(o) != null);
    }
    
    vals = CollectionUtils.validValues(testMap, testList);
    assertTrue("ValidValues returned incorrect number of values: " + vals.size(),
               vals.size() == 2);
    assertTrue("ValidValues returned incorrect values: " + vals,
               vals.contains("mumble") && vals.contains("wango"));
    for(Iterator i = vals.iterator(); i.hasNext();) {
      assertTrue("ValidValues returned a null value: " + vals, i.next() != null);
    }
		System.err.println("testMapUtils done");
  }
  //testListUtils
  public void testListUtils() {
		System.err.println("In testListUtils");
    Object [] testArray = {"foo", "bar", "baz", "quux", "mumble", "wango"};
    List testList = Arrays.asList(testArray);
    
    Object test = CollectionUtils.findFirst(new FindSuccessFunctor(), testArray);

    assertTrue("Array find first failed unexpectedly: " + test, 
               test != null && test.equals("quux"));

    boolean in = false;
    for(int i = 0; i < testArray.length; i++) {
      if(testArray[i].equals(test)) {
        in = true;
      }
    }

    assertTrue("Array find found element not in array: " + test, in);

    test = CollectionUtils.findFirst(new FindFailureFunctor(), testArray);

    assertTrue("Array find first failed to fail expectedly: " + test, test == null);


    test = CollectionUtils.findFirst(new FindSuccessFunctor(), testList);

    assertTrue("List find first failed unexpectedly: " + test,
               test != null && test.equals("quux"));

    in = false;

		int check = testList.size();
    for(Iterator i = testList.iterator(); i.hasNext() && check != 0; check--) {
      in = in || i.next().equals(test);
    }

    assertTrue("List find first found element not in list: " + test, in);

    test = CollectionUtils.findFirst(new FindFailureFunctor(), testList);

    assertTrue("List find first failed to fail expectedly: " + test, test == null);
		System.err.println("testListUtils done");
  }

  class InPlaceInteger {
    private Integer intVal;
    public InPlaceInteger() {intVal = new Integer(0);}
    public InPlaceInteger(int n) {intVal = new Integer(n);}
    public int intValue() {return intVal.intValue();}
    public void set(int n) {
      intVal = new Integer(n);
    }
  }

  class FindSuccessFunctor implements BooleanFunctor {
    public FindSuccessFunctor(){}
    public final boolean func(final Object o) {
      return o.equals("quux");
    }
  }

  class FindFailureFunctor implements BooleanFunctor {
    public FindFailureFunctor(){}
    public final boolean func(final Object o) {
      return o.equals(new Integer(2));
    }
  }

  class InPlaceIncrementFunctor implements UnaryFunctor {
    public InPlaceIncrementFunctor(){}
    public final Object func(final Object o) {
      ((InPlaceInteger)o).set(((InPlaceInteger)o).intValue() + 1);
      return o;
    }
  }
  
  class IncrementFunctor implements UnaryFunctor {
    public IncrementFunctor() {}
    public final Object func(final Object o) {
      return new Integer(((Integer)o).intValue() + 1);
    }
  }

  class GreaterThanFunctor implements BooleanFunctor {
    private int x;
    public GreaterThanFunctor(final int x) {
      this.x = x;
    }
    public final boolean func(final Object o) {
      return ((Integer) o).intValue() > x;
    }
  }
}
