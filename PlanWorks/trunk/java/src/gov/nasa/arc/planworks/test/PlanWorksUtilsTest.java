package gov.nasa.arc.planworks.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
    suite.addTest(new PlanWorksUtilsTest("testCollectionUtils"));
    return suite;
  }
  public PlanWorksUtilsTest(String testType) {
    super(testType);
  }
  public void testCollectionUtils() {
//     Integer [] array = new Integer[4];
//     for(int i = 0; i < array.length; i++) {
//       array[i] = new Integer(i);
//     }
//     Integer [] mappedArray = (Integer []) CollectionUtils.aMap(new IncrementFunctor(), array);
//     assertTrue("Mapped array isn't the same length", array.length == mappedArray.length);
//     for(int i = 0; i < mappedArray.length; i++) {
//       assertTrue("Mapped array value isn't incremented.",
//                  mappedArray[i].intValue() == array[i].intValue() + 1);
//     }
    
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
    
//     mappedArray = (Integer []) CollectionUtils.aInPlaceMap(new IncrementFunctor(), array);
    
//     assertTrue("In-place mapped array isn't the same size.", mappedArray.length == array.length);
//     for(int i = 0; i < array.length; i++) {
//       assertTrue("In-place mapped array isn't equal.",
//                  mappedArray[i].equals(array[i]));
//       assertTrue("In-place mapped array wasn't incremented.", array[i].intValue() == i + 1);
//     }

    mappedList = CollectionUtils.lInPlaceMap(new IncrementFunctor(), list);
    assertTrue("In-place mapped list isn't the same length", list.size() == mappedList.size());
    assertTrue("List was emptied!", list.size() != 0);
    assertTrue("Mapped list as emptied!", mappedList.size() != 0);
    for(int i = 0; i < mappedList.size(); i++) {
      assertTrue("In-place mapped list isn't equal.",
                 ((Integer)mappedList.get(i)).equals(list.get(i)));
      assertTrue("In-place mapped list wasn't incremented.",
                 ((Integer)list.get(i)).intValue() == i + 1);
    }

    int compInt = 2;
    mappedList = CollectionUtils.lGrep(new GreaterThanFunctor(compInt), list);
    assertTrue("List was emptied!", list.size() != 0);
    assertTrue("grepped-list was emptied!", mappedList.size() != 0);
    assertTrue("Grep-list failed to eliminate elements.", list.size() > mappedList.size());
    for(int i = 0; i < mappedList.size(); i++) {
      assertTrue("Grep-list failed to eliminate correct elements.",
                 ((Integer)mappedList.get(i)).intValue() > compInt);
    }

    int startSize = list.size();
    mappedList = CollectionUtils.lInPlaceGrep(new GreaterThanFunctor(compInt), list);
    assertTrue("List was emptied!", list.size() != 0);
    assertTrue("grepped-list was emptied!", mappedList.size() != 0);
    assertTrue("In-place grep list failed to eliminate elements.", list.size() < startSize);
    for(int i = 0; i < list.size(); i++) {
      assertTrue("In-place grep list failed to eliminate correct elements.",
                 ((Integer)list.get(i)).intValue() > compInt);
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
