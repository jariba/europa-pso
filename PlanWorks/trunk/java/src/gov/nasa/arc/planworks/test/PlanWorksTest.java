package gov.nasa.arc.planworks.test;

import java.util.Enumeration;

import junit.framework.*;

public class PlanWorksTest extends TestCase {
  public static void main(String [] args) {
    junit.textui.TestRunner.run(suite());
  }
  public static TestSuite suite() {
    final TestSuite suite = new TestSuite();
    suite.addTest(BackendTest.suite());
    suite.addTest(MySQLDBTest.suite());
    return suite;
  }
}

