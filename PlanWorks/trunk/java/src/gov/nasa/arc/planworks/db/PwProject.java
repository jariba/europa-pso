// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwProject.java,v 1.15 2003-08-12 21:32:57 miatauro Exp $
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
    } catch (SQLException sqlExcp) {
      System.err.println( sqlExcp);
      System.exit( -1);
    }
  }
  /**
   * <code>initProjects</code> - register loaded XML files data base (eXist),
   *                             restore loaded projects configuration info,
   *                             and inflate projects, by reading XML formatted
   *                             saved project information.
   *
   * @exception ResourceNotFoundException if an error occurs
   */
  /*  public static void initProjects() throws ResourceNotFoundException, SQLException, IOException {
    PwProjectImpl.initProjects();
    }*/

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
    throws DuplicateNameException, ResourceNotFoundException, SQLException {
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

  /**
   * <code>close</code> - remove project from /xml/proj/projects.xml, and
   *                      remove /xml/proj/<projectName>.xml
   *
   * @exception Exception if an error occurs
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract void delete() throws Exception, ResourceNotFoundException;

  public abstract PwPlanningSequence addPlanningSequence(String url) 
    throws DuplicateNameException, ResourceNotFoundException, SQLException;
} // end class PwProject
