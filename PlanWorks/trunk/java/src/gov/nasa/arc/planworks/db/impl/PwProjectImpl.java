// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwProjectImpl.java,v 1.3 2003-05-27 19:00:08 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;


/**
 * <code>PwProjectImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwProjectImpl extends PwProject {


  private String url; // project pathname
  private String projectName;
  private List planningSequences; // element PwPlanningSequence
  private String [] seqDirNames;


  /**
   * <code>PwProjectImpl</code> - constructor 
   *                  called from ProjectMgmt.createProject
   *
   * @param url - <code>String</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwProjectImpl( String url)  throws ResourceNotFoundException {
    this.url = url; // project pathname
    planningSequences = new ArrayList();
    int index = url.lastIndexOf( "/");
    if (index == -1) {
      throw new ResourceNotFoundException( "project url '" + url +
                                           "' cannot be parsed for '/'");
    } 
    projectName = url.substring( index + 1);

    // determine project's sequences
    seqDirNames = new File( url).list();
    for (int i = 0; i < seqDirNames.length; i++) {
      String seqDirName = seqDirNames[i];
      if (! seqDirName.equals( "CVS")) {
        System.err.println( "PwProjectImpl seqDirName: " + seqDirName);
        planningSequences.add
          ( new PwPlanningSequenceImpl(  url + "/" + seqDirName, projectName, 
                                         new PwModelImpl()));
      }
    }
    if (planningSequences.size() == 0) {
      throw new ResourceNotFoundException( "project url '" + url +
                                           "' does not have any sequence directories");
    }
  } // end  constructor String


  /**
   * <code>PwProjectImpl</code> - constructor 
   *                  called from ProjectMgmt.openProject
   *
   * @param url - <code>String</code> - 
   * @param isInDb - <code>boolean</code> - 
   * @exception DuplicateNameExceptionif an error occurs
   */
  public PwProjectImpl( String url, boolean isInDb)  throws ResourceNotFoundException {
    this.url = url; // project pathname
    planningSequences = new ArrayList();


  } // end  constructor String String


  /**
   * <code>getUrl</code> - project pathname for planning sequences. e.g.
   *                       PlanWorks/xml/test
   *
   * @return - <code>String</code> - 
   */
  public String getUrl() {
    return url;
  } // end getUrl

  /**
   * <code>getProjectName</code> - project name (directory containing
   *                               planning sequences
   *
   * @return - <code>String</code> - 
   */
  public String getProjectName() {
    return projectName;
  } // end getProjectName

  /**
   * <code>listPlanningSequences</code>
   *
   * @return - <code>List</code> -  List of Strings (urls of sequences)
   *                                each sequence is set of partial plans
   *                                e.g. monkey (PlanWorks/xml/test/monkey)
   */
  public List listPlanningSequences() {
    List urlList = new ArrayList();
    Iterator planningSeqIterator = planningSequences.iterator();
    while (planningSeqIterator.hasNext()) {
      urlList.add( ((PwPlanningSequence) planningSeqIterator.next()).getUrl());
    }
    return urlList;
  } // end listPlanningSequences

  /**
   * <code>getPlanningSequence</code>
   *
   * @param sequenceName - <code>String</code> - 
   * @return - <code>PwPlanningSequence</code> - 
   */
  public PwPlanningSequence getPlanningSequence( String url)
    throws ResourceNotFoundException {
    Iterator planningSeqIterator = planningSequences.iterator();
    while (planningSeqIterator.hasNext()) {
      PwPlanningSequence pwPlanningSequence =
        (PwPlanningSequence) planningSeqIterator.next();
      if (pwPlanningSequence.getUrl().equals( url)) {
        return pwPlanningSequence;
      }
    }
    throw new ResourceNotFoundException( "getPlanningSequence could not find " + url);
  } // end getPlanningSequence

  /**
   * <code>close</code>
   *
   * @exception Exception if an error occurs
   */
  public void close() throws Exception {
  } // end close

  /**
   * <code>requiresSaving</code>
   *
   * @return - <code>boolean</code> - 
   */
  public boolean requiresSaving() {
    return true;
  } // end requiresSaving

  /**
   * <code>save</code>
   *
   * @exception Exception if an error occurs
   */
  public void save() throws Exception {
    StringBuffer projectDataPathname =
      new StringBuffer( System.getProperty( "planworks.root"));
    projectDataPathname.append( "/data/project/");
    projectDataPathname.append( "project.serialized");
    FileOutputStream fileOutStream =
      new FileOutputStream( projectDataPathname.toString());
    ObjectOutputStream objectOutStream = new ObjectOutputStream( fileOutStream);
    objectOutStream.writeObject( projectName);

    /**

           PwProjectImpl clonedProject = this.clone();
           // null out PwPlanningSequenceImpl.partialPlans & transactions


	oos.writeInt(12345);
	oos.writeObject("Today");
	oos.writeObject(new Date());

    **/
    objectOutStream.close();
  } // end save

  /**
   * <code>restore</code>
   *
   * @exception Exception if an error occurs
   */
  public void restore() throws Exception {
    StringBuffer projectDataPathname =
      new StringBuffer( System.getProperty( "planworks.root"));
    projectDataPathname.append( "/data/project/");
    projectDataPathname.append( "project.serialized");
    FileInputStream fileInStream =
      new FileInputStream( projectDataPathname.toString());
    ObjectInputStream objectInStream = new ObjectInputStream( fileInStream);
    String projectName = (String) objectInStream.readObject();
    System.err.println( "restore: projectName " + projectName);
    /**
           planWorksRoot = System.getProperty( "planworks.root");


	int i = ois.readInt();
	String today = (String) ois.readObject();
	Date date = (Date) ois.readObject();

 
    **/
    objectInStream.close();
  } // end restore



} // end class PwProjectImpl
