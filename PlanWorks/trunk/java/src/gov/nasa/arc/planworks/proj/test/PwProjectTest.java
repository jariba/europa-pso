// $Id: PwProjectTest.java,v 1.1 2003-05-10 01:00:34 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 06May03
//         derived from skunkworks/planViz/java/src/.../PlanViz.java
//

package gov.nasa.arc.planworks.proj.test;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.proj.PwProjectMgmt;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;


/**
 * <code>PwProjectTest</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwProjectTest {

 
  private static PwProjectTest pwProjectTest;
  private static String planWorksRoot;
  private static String userName;
  private static boolean isJvmGtEq1_4;
  private static String osType;

  // XML XPath - eXist-0.9.1
  private static String xmlFilesDirectory;
  private static String userCollectionName; // e.g. /wtaylor


  /**
   * <code>PwProjectTest</code> - constructor 
   *
   */
  public PwProjectTest() {

    PwPartialPlan pwPartialPlan = getTestPartialPlan();
    System.out.println( "Test partialPlan " + pwPartialPlan);

  } // end constructor


  private PwPartialPlan getTestPartialPlan() {
    String projectName = "", sequenceName = "";
    PwProject pwProject = null; PwPlanningSequence pwPlanSeq = null;
    PwPartialPlan pwPartialPlan = null;
    // ONE PROJECT HARD-CODED FOR NOW, WITH ONE SEQUENCE, WITH ONE PARTIAL PLAN
    // PlanWorks/xml/test/monkey/monkey.xml
    List projectList = PwProjectMgmt.listProjects();
    Iterator projIterator = projectList.iterator();
    while (projIterator.hasNext()) {
      projectName = (String) projIterator.next();
      System.out.println( "Project: " + projectName);
      try {
        pwProject = PwProjectMgmt.openProject( projectName);
      } catch (ResourceNotFoundException rnfExcep1) {
        System.err.println( "Project " + projectName + " not found: " + rnfExcep1);
        rnfExcep1.printStackTrace();
        System.exit( 1);
      }
      List sequenceList = pwProject.listPlanningSequences();
      Iterator seqIterator = sequenceList.iterator();
      while (seqIterator.hasNext()) {
        sequenceName = (String) seqIterator.next();
        System.out.println( "Sequence: " + sequenceName);
        try {
          pwPlanSeq = pwProject.getPlanningSequence( sequenceName);
        } catch (ResourceNotFoundException rnfExcep2) {
          System.err.println( "Sequence " + sequenceName + " not found: " + rnfExcep2 );
          rnfExcep2.printStackTrace();
          System.exit( 1);
        }
        int stepCount = pwPlanSeq.getStepCount();
        for (int step = 0; step < stepCount; step++) {
          try {
            pwPartialPlan = pwPlanSeq.getPartialPlan( step);
            System.out.println( "step " + step + " partialPlan " + pwPartialPlan);
          } catch (IndexOutOfBoundsException indExcep) {
            System.err.println( "Step " + step + " not found: " + indExcep);
            indExcep.printStackTrace();
            System.exit( 1);
          }
        }
      }
    }
    return pwPartialPlan;
  } // end performTest

  private static void processArguments( String[] args) {
    // input args - defaults
    isJvmGtEq1_4 = true;
    xmlFilesDirectory = "";
    String pathname = "";
    for (int argc = 0; argc < args.length; argc++) {
      // System.err.println( "argc " + argc + " " + args[argc]);
      if (argc == 0) {
        // linux | solaris | darwin (MacOSX)
        osType = args[argc];
      } else if (argc == 1) {
         if (args[argc].equals( "true")) {
          isJvmGtEq1_4 = true;
        } else if (args[argc].equals( "false")) {
          isJvmGtEq1_4 = false;    
        } else {
          System.err.println( "isJvmGtEq1_4 '" + args[argc] +
                              "' was not either 'true' or 'false'\n");
          System.exit( 0);
        }
      } else if (argc == 2) {
        pathname = args[argc];
        if (! pathname.equals( "null")) {
           xmlFilesDirectory = FileUtils.getCanonicalPath( pathname);
          System.err.println( "xmlFilesDirectory: " + xmlFilesDirectory);
        }
      } else {
        System.err.println( "argument '" + args[argc] + "' not handled");
        System.exit( 0);
      }
    }
  } // end processArguments

  
  /**
   * <code>main</code>
   *
   * @param args - <code>String[]</code> - 
   */
  public static void main( String[] args) {

    processArguments( args);

    // planWorksRoot = getEnvVar( "PLANWORKS_ROOT");
    planWorksRoot = System.getProperty( "planworks.root");
    // userName = getEnvVar( "USER");
    userName = System.getProperty( "user");
    userCollectionName = "/" + userName;

    pwProjectTest = new PwProjectTest();

  } // end main

} // end class PwProjectTest

