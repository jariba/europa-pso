// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwProject.java,v 1.8 2003-06-19 00:31:20 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db;

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


  /**
   * <code>initProjects</code> - register loaded XML files data base (eXist),
   *                             restore loaded projects configuration info,
   *                             and inflate projects, by reading XML formatted
   *                             saved project information.
   *
   * @exception ResourceNotFoundException if an error occurs
   */
  public static void initProjects() throws ResourceNotFoundException {
    PwProjectImpl.initProjects();
  }

  /**
   * <code>createProject</code> - using user supplied url, load XML formatted
   *                              partial plans into XML:DB data base, show user
   *                              cascading pulldown menu for their selection
   *
   * @param url - <code>String</code> - 
   * @return - <code>PwProject</code> - 
   * @exception DuplicateNameException if an error occurs
   * @exception ResourceNotFoundException if an error occurs
   */
  public static PwProject createProject( String url)
    throws DuplicateNameException, ResourceNotFoundException {
    return (new PwProjectImpl( url));
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
   * <code>saveProjects</code> - save project names & urls in /xml/proj/projects.xml
   *                             and save project specific info in separate files
   *
   * @exception Exception if an error occurs
   */
  public static void saveProjects() throws Exception {
    PwProjectImpl.saveProjects();
  }

  /**
   * <code>getUrl</code> - return project URL 
   *
   * @return - <code>String</code> - 
   */
  public abstract String getUrl();

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

  /**
   * <code>getPlanningSequenceNames</code> - return project's sequence names
   *
   * @return - <code>List</code> - of String
   */
  public abstract List getPlanningSequenceNames();

  /**
   * <code>getPartialPlanNames</code> - return project's partial plan names for
   *                                    specified <code>seqName</code>
   *
   * @param seqName - <code>String</code> - 
   * @return - <code>List</code> - of List of String
   */
  public abstract List getPartialPlanNames( String seqName);

  /**
   * <code>close</code> - remove project from /xml/proj/projects.xml, and
   *                      remove /xml/proj/<projectName>.xml
   *
   * @exception Exception if an error occurs
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract void close() throws Exception, ResourceNotFoundException;

  /**
   * <code>requiresSaving</code> - indicates to <code>save</code> that this
   *                               project's info should be written to
   *                               /xml/proj/<projectName>.xml
   *
   * @return - <code>boolean</code> - 
   */
  public abstract boolean requiresSaving();

  /**
   * <code>save</code> - save project url, name, & seqNames in /xml/proj/<projectName>.xml
   *
   * @exception Exception if an error occurs
   */
  public abstract void save() throws Exception;




} // end class PwProject
