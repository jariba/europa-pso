package gov.nasa.arc.planworks.test;

import java.util.Enumeration;

import junit.framework.*;

public class PlanWorksTest extends TestCase {
  public static int TEST_RUNNING = 0;
  public static void main(String [] args) {
    junit.textui.TestRunner.run(suite());
    //TestResult result = new TestResult();
    //BackendTest.suite().run(result);
    //while(TEST_RUNNING == 1) {
    //  try{Thread.yield();}catch(Exception e){}
    // }
    //printFailures(result);
    //MySQLDBTest.suite().run(result);
    //printFailures(result);
  }
  public static TestSuite suite() {
    final TestSuite suite = new TestSuite();
    suite.addTest(BackendTest.suite());
    suite.addTest(MySQLDBTest.suite());
    suite.addTest(PlanWorksUtilsTest.suite());
    return suite;
  }
  private static void printFailures(TestResult result) {
    if(result.failureCount() == 0) {
      return;
    }
    Enumeration failures = result.failures();
    while(failures.hasMoreElements()) {
      TestFailure failure = (TestFailure) failures.nextElement();
      System.err.println(failure.toString());
    }
  }
}

