//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PlanWorksTest.java,v 1.30 2004-05-14 17:41:38 miatauro Exp $
//
package gov.nasa.arc.planworks.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;

import junit.extensions.jfcunit.JFCTestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite; 
import junit.textui.TestRunner;

public class PlanWorksTest extends JFCTestCase {
  public static int TEST_RUNNING = 0;
  public static String [] args;
  public static void main(String [] args) {
    PlanWorksTest.args = args;

    //TestRunner.run(suite());

    TestResult result = new TestResult();
    //BackendTest.suite().run(result);
    //while(TEST_RUNNING == 1) {
    //  try{Thread.yield();}catch(Exception e){}
    // }
    //printFailures(result);
    //MySQLDBTest.suite().run(result);
    //printFailures(result);
		System.err.println("Backend test...");
		BackendTest.suite().run(result);
		printFailures(result);
		System.err.println("Backend done.");
// 		MySQLDBTest.suite().run(result);
// 		printFailures(result);
		System.err.println("Utils test...");
		PlanWorksUtilsTest.suite().run(result);
		printFailures(result);
		System.err.println("Utils done.");
		System.err.println("GUI test...");
		PlanWorksGUITest.suite().run(result);
		printFailures(result);
		System.err.println("GUI done.");
  }
  public static TestSuite suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(BackendTest.suite());
    suite.addTest(MySQLDBTest.suite());
    suite.addTest(PlanWorksUtilsTest.suite());
    suite.addTest(PlanWorksGUITest.suite());
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

