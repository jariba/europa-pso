// $Id: PwProject.java,v 1.1 2003-05-10 01:00:31 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;

import gov.nasa.arc.planworks.util.ResourceNotFoundException;


/**
 * <code>PwProject</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public abstract class PwProject {

  /**
   * <code>getUrl</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getUrl();

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

  /**
   * <code>close</code>
   *
   * @exception Exception if an error occurs
   */
  public abstract void close() throws Exception;

  /**
   * <code>requiresSaving</code>
   *
   * @return - <code>boolean</code> - 
   */
  public abstract boolean requiresSaving();

  /**
   * <code>save</code>
   *
   * @exception Exception if an error occurs
   */
  public abstract void save() throws Exception;



} // end class PwProject
