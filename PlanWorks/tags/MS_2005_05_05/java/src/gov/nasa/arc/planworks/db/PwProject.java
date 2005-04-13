// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwProject.java,v 1.27 2004-09-10 20:02:31 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db;

import java.io.IOException;
import java.util.List;

import gov.nasa.arc.planworks.db.impl.PwProjectImpl;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;

/**
 * <code>PwProject</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public abstract class PwProject {

  static {
    try {
      Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
      PwProjectImpl.initProjects();
    } catch (IOException excp) {
      System.err.println( excp);
      System.exit( -1);
    } catch (ResourceNotFoundException rnfExcp) {
      System.err.println( rnfExcp);
      System.exit( -1);
    }
  }

  /**
   * <code>createProject</code> - using user supplied name, load formatted
   *                              partial plans into data base
   *
   * @param name - <code>String</code> - 
   * @return - <code>PwProject</code> - 
   * @exception DuplicateNameException if an error occurs
   */
//   public static PwProject createProject( final String name, final String workingDir)
  public static PwProject createProject( final String name)
    throws DuplicateNameException {
//     return PwProjectImpl.createProject( name, workingDir);
    return PwProjectImpl.createProject( name);
  }

  /**
   * <code>getProject</code> - get project instance after it is Created, or
   *                           Opened (restored)
   *
   * @param name - <code>String</code> - 
   * @return - <code>PwProject</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public static PwProject getProject( final String name)  throws ResourceNotFoundException {
    return PwProjectImpl.getProject( name);
  }

  /**
   * <code>listProjects</code> - list of the active project names
   *
   *
   * @return - <code>List</code> - 
   */
  public static List listProjects() {
    return PwProjectImpl.listProjects();
  }

  /**
   * <code>getName</code> - return project name 
   *
   * @return - <code>String</code> - 
   */
  public abstract String getName();

  /**
   * <code>getWorkingDir</code>
   *
   * @return - <code>String</code> - 
   */
  // public abstract String getWorkingDir();

  /**
   * <code>getPlannerPath</code>
   *
   * @return - <code>String</code> - 
   */
  // public abstract String getPlannerPath();

  /**
   * <code>getModelName</code>
   *
   * @return - <code>String</code> - 
   */
  // public abstract String getModelName();

  /**
   * <code>getModelPath</code>
   *
   * @return - <code>String</code> - 
   */
  // public abstract String getModelPath();

  /**
   * <code>getModelOutputDestDir</code>
   *
   * @return - <code>String</code> - 
   */
  // public abstract String getModelOutputDestDir();

  /**
   * <code>getModelInitStatePath</code>
   *
   * @return - <code>String</code> - 
   */
  // public abstract String getModelInitStatePath();

  /**
   * <code>listPlanningSequences</code>
   *
   * @return - <code>List</code> -  List of String (name of sequence)
   *                                each sequence is set of partial plans
   */
  public abstract List listPlanningSequences();

  /**
   * <code>getPlanningSequence</code>
   *
   * @param seqName - <code>String</code> - 
   * @return - <code>PwPlanningSequence</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract PwPlanningSequence getPlanningSequence( final String seqName)
    throws ResourceNotFoundException;

  /**
   * <code>getPlanningSequence</code>
   *
   * @param seqId - <code>Long</code> - 
   * @return - <code>PwPlanningSequence</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract PwPlanningSequence getPlanningSequence(final Long seqId)
    throws ResourceNotFoundException;

  /**
   * <code>addPlanningSequence</code>
   *
   * @param url - <code>String</code> - 
   * @return - <code>PwPlanningSequence</code> - 
   * @exception DuplicateNameException if an error occurs
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract PwPlanningSequence addPlanningSequence(final String url) 
    throws DuplicateNameException, ResourceNotFoundException;

  /**
   * <code>deletePlanningSequence</code>
   *
   * @param seqName - <code>String</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract void deletePlanningSequence(final String seqName)
    throws ResourceNotFoundException;

  /**
   * <code>deletePlanningSequence</code>
   *
   * @param seqId - <code>Long</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract void deletePlanningSequence(final Long seqId)
    throws ResourceNotFoundException;

  public abstract PwPlanningSequence closePlanningSequence(final String seqName)
    throws ResourceNotFoundException;
  
  public abstract PwPlanningSequence closePlanningSequence(final Long seqId) 
    throws ResourceNotFoundException;

  /**
   * <code>close</code> - 
   *
   * @exception Exception if an error occurs
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract void delete() throws ResourceNotFoundException;

  public abstract boolean getJNIAdapterLoaded();

  public abstract void setJNIAdapterLoaded( boolean value);

} // end class PwProject
