// $Id: PwProjectMgmt.java,v 1.1 2003-05-10 01:00:33 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.proj;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.impl.Factory;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.DuplicateNameException;


/**
 * <code>PwProjectMgmt</code> - singleton class to manage Project instances
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwProjectMgmt {

  private static List projects;

  static {
    projects = new ArrayList();
    // TESTING
    projects.add( System.getProperty( "planworks.root") + "/xml/test");
  }

  private PwProjectMgmt() {
  } 

  /**
   * <code>listProjects</code>
   *
   * @return - <code>List</code> - 
   */
  public static List listProjects() {
    return projects;
  }

  /**
   * <code>openProject</code>
   *
   * @param url - <code>String</code> - 
   * @return - <code>PwProject</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public static PwProject openProject( String url) throws ResourceNotFoundException {
    return Factory.INSTANCE.getOpenProject( url);
  } // end openProject

  /**
   * <code>createProject</code>
   *
   * @param url - <code>String</code> - 
   * @return - <code>PwProject</code> - 
   * @exception DuplicateNameException if an error occurs
   */
  public static PwProject createProject( String url) throws DuplicateNameException {
    return Factory.INSTANCE.getCreateProject( url);
  } // end createProject

} // end class PwProjectMgmt
