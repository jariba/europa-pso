// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwProjectImpl.java,v 1.4 2003-06-02 17:49:59 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db.impl;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.db.util.XmlDBeXist;


/**
 * <code>PwProjectImpl</code> - Data base API for PlanWorks
 *                manages Project instances
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwProjectImpl extends PwProject {


  private static String userCollectionName;
  private static String projectCollectionName;
  private static String projectsXmlDataDir;
  private static String projectsXmlDataPathname;

  private static List projectNames;  // element String
  private static List projectUrls;  // element String
  private static List projects;  // element PwProjectImpl

  /**
   * <code>initProjects</code> - register loaded XML files data base (eXist)
   *                             restore loaded projects configuration info
   *
   */
  public static void initProjects() throws ResourceNotFoundException {
    projectNames = new ArrayList();
    projectUrls = new ArrayList();
    projects = new ArrayList();
    userCollectionName = "/" + System.getProperty( "user");
    try {
      StringBuffer projectsXmlDataDirBuf =
        new StringBuffer( System.getProperty( "planworks.root"));
      projectsXmlDataDirBuf.append( "/xml/db/proj/");
      projectsXmlDataDir = projectsXmlDataDirBuf.toString();
      StringBuffer projectsXmlDataPathnameBuf =
        new StringBuffer( System.getProperty( "planworks.root"));
      projectsXmlDataPathnameBuf.append( "/xml/db/proj/projects.xml");
      projectsXmlDataPathname = projectsXmlDataPathnameBuf.toString();
      if ((new File( projectsXmlDataPathname)).exists()) {
        FileInputStream fileInputStream =
          new FileInputStream( projectsXmlDataPathname);
        XMLDecoder xmlDecoder = new XMLDecoder( fileInputStream);
        List projectNamesRestore = (List) xmlDecoder.readObject();
        List projectUrlsRestore = (List) xmlDecoder.readObject();
        System.err.println( "PwProjectImpl restore: projectNames " +
                            projectNamesRestore);
        System.err.println( "PwProjectImpl restore: projectUrls " +
                            projectUrlsRestore);
        xmlDecoder.close();
        Iterator namesIterator = projectNamesRestore.iterator();
        while (namesIterator.hasNext()) {
          projectNames.add( namesIterator.next());
        }
        Iterator urlsIterator = projectUrlsRestore.iterator();
        while (urlsIterator.hasNext()) {
          projectUrls.add( urlsIterator.next());
        }
      }
    } catch (Exception e) {
      throw new ResourceNotFoundException( "initProjects: " + e);
    }

    connectToExistDataBase();

  } // end initProjects


  private String url; // project pathname
  private String projectName;
  private List planningSequences; // element PwPlanningSequence
  private List seqDirNames; // element String
  private String projectDataPathname;
  private boolean requiresSaving;

  /**
   * <code>PwProjectImpl</code> - constructor 
   *                  called from PwProject.createProject
   *
   * @param url - <code>String</code> - 
   * @param projectName - <code>String</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwProjectImpl( String url)  throws DuplicateNameException, ResourceNotFoundException {
    this.url = url; // project pathname
    projectName = parseProjectName( url);
    projectCollectionName = getProjectCollectionName();
    projectDataPathname = projectsXmlDataDir.concat( projectName);
    projectDataPathname = projectDataPathname.concat( ".xml");
    planningSequences = new ArrayList();
    seqDirNames = new ArrayList();
    requiresSaving = true;
    boolean isInDb = false;
    Iterator urlsIterator = projectUrls.iterator();
    while (urlsIterator.hasNext()) {
      if (((String) urlsIterator.next()).equals( url)) {
        throw new DuplicateNameException( "project '" + url + "' already exists");
      }
    }
    // determine project's sequences
    String [] fileNames = new File( url).list();
    for (int i = 0; i < fileNames.length; i++) {
      String fileName = fileNames[i];
      if ((! fileName.equals( "CVS")) &&
          (new File( url + "/" + fileName)).isDirectory()) {
        System.err.println( "Project " + projectName + " => seqDirName: " + fileName);
        seqDirNames.add( fileName);
        planningSequences.add
          ( new PwPlanningSequenceImpl(  url + "/" + fileName, projectName, 
                                         new PwModelImpl(), isInDb));
      }
    }
    if (planningSequences.size() == 0) {
      throw new ResourceNotFoundException( "CreateProject for url '" + url +
                                           "' does not have any sequence directories");
    }
    projectNames.add(projectName );
    projectUrls.add( url);
    projects.add( this);
  } // end  constructor PwProject.createProject


  /**
   * <code>PwProjectImpl</code> - constructor 
   *                  called from PwProject.openProject
   *
   * @param url - <code>String</code> - 
   * @param isInDb - <code>boolean</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwProjectImpl( String url, boolean isInDb) throws ResourceNotFoundException {
    this.url = url; // project pathname
    projectName = parseProjectName( url);
    projectCollectionName = getProjectCollectionName();
    projectDataPathname = projectsXmlDataDir.concat( projectName);
    projectDataPathname = projectDataPathname.concat( ".xml");
    planningSequences = new ArrayList();
    seqDirNames = new ArrayList();
    requiresSaving = false;
    PwProjectImpl restoredProject;
    try {
      restoredProject = restore( projectDataPathname);
    } catch (Exception e) {
      throw new ResourceNotFoundException( "OpenProject for url '" + url + "': " + e);
    }
    if (! restoredProject.getUrl().equals( this.url)) {
      throw new ResourceNotFoundException( "OpenProject for url '" + this.url + "': " +
                                           "does not match that in " +
                                           projectDataPathname.toString());
    }
    Iterator seqDirNamesItr = restoredProject.getSeqDirNames().iterator();
    while (seqDirNamesItr.hasNext()) {
      String seqDir = (String) seqDirNamesItr.next();
      this.seqDirNames.add( seqDir);
      this.planningSequences.add
        ( new PwPlanningSequenceImpl(  url + "/" + seqDir, projectName,
                                       new PwModelImpl(), isInDb));
    }
    // this project is already in projectNames & projectUrls
    projects.add( this);
  } // end  constructor PwProject.openProject


  /**
   * <code>PwProjectImpl</code> - constructor
   *                       for XMLEncode/XMLDecode
   *
   */
  public PwProjectImpl() {
  }


  /**
   * <code>getProjectNames</code> - with set<...>, makes this a bean property
   *                       and enabled for XMLEncode/Decode
   *
   * @return - <code>List</code> - 
   */
  public List getProjectNames() {
    return projectNames;
  }

  /**
   * <code>setProjectNames</code> - with get<...>, makes this a bean property
   *                       and enabled for XMLEncode/Decode
   *
   * @param projectNames - <code>List</code> - 
   */
  public void setProjectNames( List projectNames) {
    this.projectNames = projectNames;
  }

  /**
   * <code>getProjectUrls</code> - with set<...>, makes this a bean property
   *                       and enabled for XMLEncode/Decode
   *
   * @return - <code>List</code> - 
   */
  public List getProjectUrls() {
    return projectUrls;
  }

  /**
   * <code>setProjectUrls</code> - with get<...>, makes this a bean property
   *                       and enabled for XMLEncode/Decode
   *
   * @param projectUrls - <code>List</code> - 
   */
  public void setProjectUrls( List projectUrls) {
    this.projectUrls = projectUrls;
  }


  ///////// EXTEND PwProject ////// 

  /**
   * <code>listProjects</code>
   *
   * @return - <code>List</code> - of String (url)
   */
  public static List listProjects() {
    return projectUrls;
  }

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
   * <code>close</code> - remove project from /xml/proj/projects.xml, and
   *                      remove /xml/proj/<projectName>.xml
   *
   * @exception Exception if an error occurs
   */
  public void close() throws Exception, ResourceNotFoundException {

    throw new ResourceNotFoundException
      ( "PwProjectImpl.close: XmlDBeXist.removeCollection " +
        "does not work => " + projectName);

//     Iterator projectNamesItr = projectNames.iterator();
//     while (projectNamesItr.hasNext()) {
//       if (((String) projectNamesItr.next()).equals( projectName)) {
//         projectNamesItr.remove();
//       }
//     }
//     Iterator projectUrlsItr = projectUrls.iterator();
//     while (projectUrlsItr.hasNext()) {
//       if (((String) projectUrlsItr.next()).equals( url)) {
//         projectUrlsItr.remove();
//       }
//     }
//     // save() will save the changes to projectNames & projectUrls
//     File projectFile = new File( projectDataPathname);
//     if (! projectFile.exists()) {
//       throw new Exception( "Close Project for url '" + url + "' failed -- " +
//                            "file not found: " + projectDataPathname);
//     }
//     projectFile.delete();
//     requiresSaving = false;

//     // remove XML:DB collection
//     StringBuffer projectCollectionName = new StringBuffer( "/");
//     projectCollectionName.append( System.getProperty( "user")).append( "/");
//     projectCollectionName.append( projectName);
//     XmlDBeXist.removeCollection( projectCollectionName.toString());
  } // end close

  /**
   * <code>requiresSaving</code>
   *
   * @return - <code>boolean</code> - 
   */
  public boolean requiresSaving() {
    return requiresSaving;
  } // end requiresSaving

  /**
   * <code>save</code> - save project names & urls in /xml/proj/projects.xml
   *            save project url, name, & seqDirNames in /xml/proj/<Projectame>.xml
   *
   * @exception Exception if an error occurs
   */
  public void save() throws Exception {
    FileOutputStream fileOutputStream =
      new FileOutputStream( projectsXmlDataPathname);
    XMLEncoder xmlEncoder = new XMLEncoder( fileOutputStream);
    xmlEncoder.writeObject( projectNames);
    xmlEncoder.writeObject( projectUrls);
    System.err.println( "PwProjectImpl save: projectNames " + projectNames);
    System.err.println( "PwProjectImpl save: projectUrls " + projectUrls);
    xmlEncoder.close();

    Iterator projectsItr = projects.iterator();
    while (projectsItr.hasNext()) {
      PwProjectImpl activeProject = (PwProjectImpl) projectsItr.next();
      if (activeProject.requiresSaving) {
        fileOutputStream = new FileOutputStream( activeProject.projectDataPathname);
        xmlEncoder = new XMLEncoder( fileOutputStream);
        PwProjectImpl project = new PwProjectImpl();
        project.setUrl( activeProject.url);
        project.setProjectName( activeProject.projectName);
        project.setSeqDirNames( activeProject.seqDirNames);
        xmlEncoder.writeObject( project);
        System.err.println( "save: projectName " + activeProject.projectName);

        xmlEncoder.close();
      }
    }
  } // end save


  //////// END EXTEND PwProject ////////////////////////////////

  /**
   * <code>setUrl</code> - with getUrl, makes this a bean property and enabled
   *                       for XMLEncode/Decode
   *
   * @param url - <code>String</code> - 
   */
  public void setUrl( String url) {
    this.url = url;
  }

  /**
   * <code>setProjectName</code> - with get<...>, makes this a bean property
   *                       and enabled for XMLEncode/Decode
   *
   * @param projectName - <code>String</code> - 
   */
  public void setProjectName( String projectName) {
    this.projectName = projectName;
  }

  /**
   * <code>getPlanningSequences</code> - with set<...>, makes this a bean property
   *                       and enabled for XMLEncode/Decode
   *
   * @return - <code>List</code> - 
   */
  public List getPlanningSequences() {
    return planningSequences;
  }

  /**
   * <code>setPlanningSequences</code> - with get<...>, makes this a bean property
   *                       and enabled for XMLEncode/Decode
   *
   * @param sequences - <code>List</code> - 
   */
  public void setPlanningSequences( List sequences) {
    this.planningSequences = planningSequences;
  }

  /**
   * <code>getSeqDirNames</code> - with set<...>, makes this a bean property
   *                       and enabled for XMLEncode/Decode
   *
   * @return - <code>List</code> - 
   */
  public List getSeqDirNames() {
    return seqDirNames;
  }

  /**
   * <code>setSeqDirNames</code> - with get<...>, makes this a bean property
   *                       and enabled for XMLEncode/Decode
   *
   * @param seqDirNames - <code>List</code> - 
   */
  public void setSeqDirNames( List seqDirNames) {
    this.seqDirNames = seqDirNames;
  }

  private static void connectToExistDataBase() {
    // connect to eXist XPath data base
    long startTimeMSecs = (new Date()).getTime();

    XmlDBeXist.INSTANCE.registerDataBase();

    long stopTimeMSecs = (new Date()).getTime();
    String timeString = "Register Data Base \n   ... elapsed time: " +
      //       writeTime( (stopTimeMSecs - startTimeMSecs)) + " seconds.";
      (stopTimeMSecs - startTimeMSecs) + " msecs.";
    System.err.println( timeString);
  } // end connectToExistDataBase


  private String parseProjectName( String url) throws ResourceNotFoundException {
    int index = url.lastIndexOf( "/");
    if (index == -1) {
      throw new ResourceNotFoundException( "project url '" + url +
                                           "' cannot be parsed for '/'");
    } 
    return url.substring( index + 1);
  } // end parseProjectName


  private String getProjectCollectionName() {
    StringBuffer projectCollectionNameBuf = new StringBuffer( userCollectionName);
    projectCollectionNameBuf.append( "/").append( projectName);
    return projectCollectionNameBuf.toString();
  }


  private PwProjectImpl restore( String projectDataPathname) throws Exception {
    boolean doesExist = (new File( projectDataPathname).exists());
    if (! doesExist) {
      throw new Exception( "OpenProject for url '" + url +
                           "' => db file does not exist: '" +
                           projectDataPathname + "'");
    }
    FileInputStream fileInputStream =
      new FileInputStream( projectDataPathname);
    XMLDecoder xmlDecoder = new XMLDecoder( fileInputStream);
    PwProjectImpl project = (PwProjectImpl) xmlDecoder.readObject();
    System.err.println( "restore: url " + project.getUrl());
    System.err.println( "restore: projectName " + project.getProjectName());
    System.err.println( "restore: seqDirNames " + project.getSeqDirNames());

    xmlDecoder.close();
    return project;
  } // end restore




} // end class PwProjectImpl
