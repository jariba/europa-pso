// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwProject.java,v 1.20 2003-10-10 23:59:52 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db;

import java.io.IOException;
import java.sql.SQLException;
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
   * <code>createProject</code> - using user supplied url, load formatted
   *                              partial plans into data base
   *
   * @param url - <code>String</code> - 
   * @return - <code>PwProject</code> - 
   * @exception DuplicateNameException if an error occurs
   * @exception ResourceNotFoundException if an error occurs
   */
  public static PwProject createProject( String url)
    throws DuplicateNameException, ResourceNotFoundException {
    //return (new PwProjectImpl( url));
    return PwProjectImpl.createProject(url);
  }

  /**
   * <code>getProject</code> - get project instance after it is Created, or
   *                           Opened (restored)
   *
   * @param url - <code>String</code> - 
   * @return - <code>PwProject</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public static PwProject getProject( String url)  throws ResourceNotFoundException {
    return PwProjectImpl.getProject( url);
  }

  /**
   * <code>listProjects</code> - list of the active project URLs
   *
   * @return - <code>List</code> - of String (url)
   */
  public static List listProjects() {
    return PwProjectImpl.listProjects();
  }

  /**
   * <code>getName</code> - return project name (directory containing
   *                               planning sequences)
   *
   * @return - <code>String</code> - 
   */
  public abstract String getName();

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
  public abstract PwPlanningSequence getPlanningSequence( String seqName)
    throws ResourceNotFoundException;

  public abstract PwPlanningSequence getPlanningSequence(Long seqId)
    throws ResourceNotFoundException;

  /**
   * <code>close</code> - 
   *
   * @exception Exception if an error occurs
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract void delete() throws Exception, ResourceNotFoundException;

  public abstract void deletePlanningSequence(String seqName) throws ResourceNotFoundException;

  public abstract void deletePlanningSequence(Long seqId) throws ResourceNotFoundException;

  public abstract PwPlanningSequence addPlanningSequence(String url) 
    throws DuplicateNameException, ResourceNotFoundException;
} // end class PwProject
