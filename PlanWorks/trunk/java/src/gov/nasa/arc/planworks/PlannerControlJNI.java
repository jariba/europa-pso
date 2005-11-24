// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PlannerControlJNI.java,v 1.7 2005-11-24 00:50:20 miatauro Exp $
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

  /**
   * <code>initPlannerRun</code>
   *
   * @param plannerPath - <code>String</code> - 
   * @param modelPath - <code>String</code> - 
   * @param nodelInitStatePath - <code>String</code> - 
   * @param modelOutputDestDir - <code>String</code> - 
   * @return - <code>int</code> - 
   */
  public static native int initPlannerRun( final String plannerPath,
                                           final String modelPath,
                                           final String modelInitStatePath,
                                           final String modelOutputDestDir,
                                           final String plannerConfigPath,
					   final String debugPath,
					   final String[] sourcePaths);

  /**
   * <code>getPlannerStatus</code>
   *
   * @return - <code>int</code> - 
   */
  public static native int getPlannerStatus();

  /**
   * <code>getDestinationPath</code>
   *
   * @return - <code>String</code> - 
   */
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
  /**
   * <code>writeStep</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>int</code> - 
   */
  public static native int writeStep(  final int step);
  /**
   * <code>writeNext</code>
   *
   * @param numStep - <code>int</code> - 
   * @return - <code>int</code> - 
   */
  public static native int writeNext(  final int numStep);
  /**
   * <code>completePlannerRun</code>
   *
   * @return - <code>int</code> - 
   */
  public static native int completePlannerRun();
  /**
   * <code>terminatePlannerRun</code>
   *
   * @return - <code>int</code> - 
   */
  public static native int terminatePlannerRun();

    public static native void enableDebugMsg(final String file, final String pattern);
    public static native void disableDebugMsg(final String file, final String pattern);
} // end PlannerControlJNI
