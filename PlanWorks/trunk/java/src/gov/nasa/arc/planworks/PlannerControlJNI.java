// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PlannerControlJNI.java,v 1.1 2004-09-03 00:35:33 taylor Exp $
//
// PlanWorks -- started 31aug04
//

package gov.nasa.arc.planworks;

/**
 * <code>PlannerControlJNI</code> - Java Native Interface for PLASMA Planner Control
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                             NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PlannerControlJNI {

  public static native int initPlannerRun( String modelPath,
                                           String nodelInitStatePath,
                                           String modelOutputDestDir);

  public static native int getPlannerStatus();

  public static native String getDestinationPath();

  /*
   * return values for initPlannerRun() and getPlannerStatus()
   *
   * initPlannerRun() returns only PLANNER_IN_PROGRESS if okay 
   * or PLANNER_INITIALLY_INCONSISTANT if it fails
   *
   */
  public static final int PLANNER_IN_PROGRESS             = 0;
  public static final int PLANNER_TIMEOUT_REACHED         = 1;
  public static final int PLANNER_FOUND_PLAN              = 2;
  public static final int PLANNER_SEARCH_EXHAUSTED        = 3;
  public static final int PLANNER_INITIALLY_INCONSISTANT  = 4;

  // return value for following methods is int lastStepCompleted  
  public static native int writeStep( int step);
  public static native int writeNext( int numStep);
  public static native int completePlannerRun();
  public static native int terminatePlannerRun();


} // end PlannerControlJNI
