// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwProjectMgmt.java,v 1.2 2003-05-15 18:38:46 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.proj;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.db.util.XmlDBeXist;

/**
 * <code>PwProjectMgmt</code> - singleton class to manage Project instances
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwProjectMgmt {

  private static List projects; // element PwProject
  private static String userCollectionName;

  static {
    projects = new ArrayList();
    userCollectionName = "/" + System.getProperty( "user");
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
    boolean isInDb = true;
    return PwProject.createInstance( url, isInDb);
  } // end openProject

  /**
   * <code>createProject</code>
   *
   * @param url - <code>String</code> - 
   * @return - <code>PwProject</code> - 
   * @exception DuplicateNameException if an error occurs
   */
  public static PwProject createProject( String url)
    throws DuplicateNameException, ResourceNotFoundException {
    // check projects, if found throw DuplicateNameException
    Iterator projectsIterator = projects.iterator();
    while (projectsIterator.hasNext()) {
      if (((PwProject) projectsIterator.next()).getUrl().equals( url)) {
        throw new DuplicateNameException( "project '" + url + "' already exists");
      }
    }

    connectToExistDataBase();

    PwProject pwProject = PwProject.createInstance( url);
    projects.add( pwProject);
    return pwProject;
  } // end createProject


  private static void connectToExistDataBase() {
    // connect to eXist XPath data base
    long startTimeMSecs = (new Date()).getTime();

    XmlDBeXist.INSTANCE.registerDataBase();
    // create userName collection in data base, if needed
    XmlDBeXist.INSTANCE.createCollection( userCollectionName);

    long stopTimeMSecs = (new Date()).getTime();
    String timeString = "Register Data Base \n   ... elapsed time: " +
      //       writeTime( (stopTimeMSecs - startTimeMSecs)) + " seconds.";
      (stopTimeMSecs - startTimeMSecs) + " msecs.";
    System.err.println( timeString);
  } // end connectToExistDataBase



} // end class PwProjectMgmt
