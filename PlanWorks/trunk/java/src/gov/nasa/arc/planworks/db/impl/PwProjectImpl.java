// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwProjectImpl.java,v 1.11 2003-06-25 16:40:13 miatauro Exp $
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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.DuplicateNameException;
//import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.db.util.XmlFileFilter;
//import gov.nasa.arc.planworks.db.util.XmlDBeXist;
import gov.nasa.arc.planworks.db.util.MySQLDB;

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
  //  private static String projectCollectionName;
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
    userCollectionName = System.getProperty( "file.separator") + System.getProperty( "user");
    try {
      projectsXmlDataDir = System.getProperty( "projects.xml.data.dir");
      projectsXmlDataPathname = System.getProperty( "projects.xml.data.pathname");
      if ((new File( projectsXmlDataPathname)).exists()) {
        FileInputStream fileInputStream =
          new FileInputStream( projectsXmlDataPathname);
        XMLDecoder xmlDecoder = new XMLDecoder( fileInputStream);
        List projectNamesRestore = (List) xmlDecoder.readObject();
        List projectUrlsRestore = (List) xmlDecoder.readObject();
        System.err.println( "PwProjectImpl: restore projectNames " +
                            projectNamesRestore);
        System.err.println( "PwProjectImpl: restore projectUrls " +
                            projectUrlsRestore);
        xmlDecoder.close();
        Iterator namesIterator = projectNamesRestore.iterator();
        while (namesIterator.hasNext()) {
          projectNames.add( namesIterator.next());
        }
        boolean isInDb = true;
        Iterator urlsIterator = projectUrlsRestore.iterator();
        while (urlsIterator.hasNext()) {
          String url = (String) urlsIterator.next();
          projectUrls.add( url);
          new PwProjectImpl( url, isInDb);
        }
      }
    } catch (Exception e) {
      throw new ResourceNotFoundException( "initProjects: " + e);
    }

    connectToDataBase();

  } // end initProjects

  private static void connectToDataBase() {
    System.err.println("Starting MySQL...");
    long startTime = System.currentTimeMillis();
    MySQLDB.INSTANCE.startDatabase();
    startTime = System.currentTimeMillis() - startTime;
    System.err.println("   ... elapsed time: " + startTime + "ms.");
    System.err.println("Connecting to MySQL...");
    long connectTime = System.currentTimeMillis();
    MySQLDB.INSTANCE.registerDatabase();
    connectTime = System.currentTimeMillis() - connectTime;
    System.err.println("   ... elapsed time: " + connectTime + "ms.");
  } // end connectToExistDataBase

  /**
   * <code>getProject</code> -
   *
   * @param url - <code>String</code> - 
   * @return - <code>PwProject</code> - 
   */
  public static PwProject getProject( String url) throws ResourceNotFoundException {
    int index = -1;
    if((index = projectUrls.indexOf(url)) == -1) {
      throw new ResourceNotFoundException("Project not found for url '" + url + "'");
    }
    return (PwProject) projects.get(index);
  } // end getProject

  /**
   * <code>listProjects</code>
   *
   * @return - <code>List</code> - of String (url)
   */
  public static List listProjects() {
    return projectUrls;
  }

  /**
   * <code>saveProjects</code> - save project names & urls in /xml/proj/projects.xml
   *                             and save project specific info in separate files
   *
   */
  public static void saveProjects() throws Exception {
    FileOutputStream fileOutputStream =
      new FileOutputStream( projectsXmlDataPathname);
    XMLEncoder xmlEncoder = new XMLEncoder( fileOutputStream);
    xmlEncoder.writeObject( projectNames);
    xmlEncoder.writeObject( projectUrls);
    System.err.println( "PwProjectImpl: save projectNames " + projectNames);
    System.err.println( "PwProjectImpl: save projectUrls " + projectUrls);
    xmlEncoder.close();

    System.err.println( "saveProjects: " + projects.size());
    Iterator projectsItr = projects.iterator();
    while (projectsItr.hasNext()) {
      PwProjectImpl activeProject = (PwProjectImpl) projectsItr.next();
      System.err.println( "  project " + activeProject.getName() +
                          " requiresSaving " + activeProject.requiresSaving);
      if (activeProject.requiresSaving) {
        activeProject.save();
        activeProject.setRequiresSaving( false);
      }
    }
  } // end saveProjects

  private String url; // project pathname
  private String name;
  private List planningSequences; // element PwPlanningSequence
  private List seqDirNames; // element String
  private List partialPlanNames; // element List of String
  private String projectDataPathname;
  private boolean requiresSaving;

  /**
   * <code>PwProjectImpl</code> - constructor 
   *                  create a new project from an url
   *                  called from PwProject.createProject
   *
   * @param url - <code>String</code> - 
   * @exception DuplicateNameException if an error occurs
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwProjectImpl( String url)  throws DuplicateNameException, ResourceNotFoundException {
    this.url = url; // project pathname
    name = parseProjectName( url);  //gets the project directory from url
    //    projectCollectionName = getProjectCollectionName();
    projectDataPathname = projectsXmlDataDir + System.getProperty( "file.separator") + name +
      XmlFileFilter.XML_EXTENSION_W_DOT;
    planningSequences = new ArrayList();
    seqDirNames = new ArrayList();
    partialPlanNames = new ArrayList();
    requiresSaving = true;
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
          (new File( url + System.getProperty( "file.separator") + fileName)).isDirectory()) {
        System.err.println( "Project " + name + " => seqDirName: " + fileName);
        seqDirNames.add( fileName);
        planningSequences.add
          ( new PwPlanningSequenceImpl(  url + System.getProperty( "file.separator") +
                                         fileName, this, new PwModelImpl()));
      }
    }
    if (planningSequences.size() == 0) {
      throw new ResourceNotFoundException( "CreateProject for url '" + url +
                                           "' does not have any sequence directories");
    }
    projectNames.add(name );
    projectUrls.add( url);
    projects.add( this);
  } // end  constructor PwProjectImpl.createProject


  /**
   * <code>PwProjectImpl</code> - constructor
   *                  inflate a restored project from
   *                  System.getProperty("projects.xml.data.dir")
   *                  called from PwProject.initProjects
   *
   * @param url - <code>String</code> - 
   * @param isInDb - <code>boolean</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public PwProjectImpl( String url, boolean isInDb) throws ResourceNotFoundException {
    this.url = url; // project pathname
    name = parseProjectName( url);
    //projectCollectionName = getProjectCollectionName();
    projectDataPathname = projectsXmlDataDir + System.getProperty( "file.separator") + name +
      XmlFileFilter.XML_EXTENSION_W_DOT;
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
    partialPlanNames = restoredProject.getPartialPlanNames();
    Iterator seqDirNamesItr = restoredProject.getSeqDirNames().iterator();
    int seqIndx = 0;
    while (seqDirNamesItr.hasNext()) {
      String seqDir = (String) seqDirNamesItr.next();
      this.seqDirNames.add( seqDir);
      this.planningSequences.add
        ( new PwPlanningSequenceImpl(  url + System.getProperty( "file.separator") +
                                       seqDir, this, new PwModelImpl(),
                                       (List) partialPlanNames.get( seqIndx)));
      seqIndx++;
    }
    // this project is already in projectNames & projectUrls
    projects.add( this);
  } // end  constructor PwProjectImpl.openProject


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

  /**
   * <code>addPartialPlanNames</code>
   *
   * @param names - <code>List</code> - 
   */
  public void addPartialPlanNames( List names) {
    partialPlanNames.add( names);
  }


  // EXTEND PwProject 

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
   * <code>getName</code> - project name (directory containing
   *                               planning sequences
   *
   * @return - <code>String</code> - 
   */
  public String getName() {
    return name;
  } // end getName

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
   * <code>getPlanningSequenceNames</code>
   *
   * @return - <code>List</code> - of String
   */
  public List getPlanningSequenceNames() {
    return seqDirNames;
  }

  /**
   * <code>getPartialPlanNames</code>
   *
   * @param seqName - <code>String</code> - 
   * @return - <code>List</code> - of List of String
   */
  public List getPartialPlanNames( String seqName) {
    for (int i = 0, n = seqDirNames.size(); i < n; i++) {
      if (((String) seqDirNames.get( i)).equals( seqName)) {
        return (List) partialPlanNames.get( i);
      }
    }
    return null;
  } // end getPartialPlanNames

  /**
   * <code>delete</code> - remove project from /xml/proj/projects.xml, and
   *                      remove /xml/proj/<projectName>.xml
   *
   * @exception Exception if an error occurs
   */
  public void delete() throws Exception, ResourceNotFoundException {
    Iterator projectNamesItr = projectNames.iterator();
    while (projectNamesItr.hasNext()) {
      if (((String) projectNamesItr.next()).equals( name)) {
        projectNamesItr.remove();
      }
    }
    Iterator projectUrlsItr = projectUrls.iterator();
    while (projectUrlsItr.hasNext()) {
      if (((String) projectUrlsItr.next()).equals( url)) {
        projectUrlsItr.remove();
      }
    }
    Iterator projectsItr = projects.iterator();
    while (projectsItr.hasNext()) {
      PwProjectImpl activeProject = (PwProjectImpl) projectsItr.next();
      if (activeProject.getUrl().equals( url)) {
        projectsItr.remove();
      }
    }
    // save() will save the changes to projectNames & projectUrls
    File projectFile = new File( projectDataPathname);
    if (! projectFile.exists()) {
//       throw new Exception( "Close Project for url '" + url + "' failed -- " +
//                            "file not found: " + projectDataPathname);
    } else {
      projectFile.delete();
    }
    requiresSaving = false;

    // remove XML:DB collection
    /*StringBuffer projectCollectionNameBuf =
      new StringBuffer( System.getProperty( "file.separator"));
    projectCollectionNameBuf.append( System.getProperty( "user")).
      append( System.getProperty( "file.separator"));
    projectCollectionNameBuf.append( name);
    String projectCollectionName = projectCollectionNameBuf.toString();
    if (XmlDBeXist.INSTANCE.getCollection( projectCollectionName) != null) {
      XmlDBeXist.INSTANCE.removeCollection( projectCollectionName);
      }*/
    try {
      ResultSet projectId = 
        MySQLDB.queryDatabase("SELECT (ProjectId) FROM Project WHERE URL=".concat(url));
      projectId.first();
      int projectKey = projectId.getInt("ProjectId");
      ResultSet sequenceIds = 
        MySQLDB.queryDatabase("SELECT (SequenceId) FROM Sequence WHERE ProjectId=".concat(Integer.toString(projectKey)));
      while(sequenceIds.next()) {
        int sequenceId = sequenceIds.getInt("SequenceId");
        ResultSet partialPlanIds =
          MySQLDB.queryDatabase("SELECT (PartialPlanId) FROM PartialPlan WHERE SequenceId=".concat(Integer.toString(sequenceId)));
        while(partialPlanIds.next()) {
          long partialPlanId = partialPlanIds.getLong("PartialPlanId");
          MySQLDB.updateDatabase("DELETE FROM Object, Timeline, Slot, Token, Variable, EnumeratedDomain, IntervalDomain, VConstraint, TokenRelation, ParamVarTokenMap, ConstraintVarMap, Predicate, Parameter WHERE PartialPlanId=".concat(Long.toString(partialPlanId)));
        }
        MySQLDB.updateDatabase("DELETE FROM Sequence WHERE SequenceId=".concat(Integer.toString(sequenceId)));
      }
      MySQLDB.updateDatabase("DELETE FROM Project WHERE ProjectId=".concat(Integer.toString(projectKey)));
    }
    catch(SQLException sqle) { //are we transactional?  can we roll back?
      System.err.println(sqle);
      return;
    }
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
   * <code>setRequiresSaving</code>
   *
   * @param value - <code>boolean</code> - 
   */
  public void setRequiresSaving( boolean value) {
    requiresSaving = value;
  } // end setRequiresSaving

  /**
   * <code>save</code> - save project url, name, & seqDirNames in /xml/proj/<Projectame>.xml
   *
   * @exception Exception if an error occurs
   */
  public void save() throws Exception {
    FileOutputStream fileOutputStream =
      new FileOutputStream( projectDataPathname);
    XMLEncoder xmlEncoder = new XMLEncoder( fileOutputStream);
    PwProjectImpl project = new PwProjectImpl();
    project.setUrl( url);
    project.setName( name);
    project.setSeqDirNames( seqDirNames);
    project.setPartialPlanNames( partialPlanNames);
    xmlEncoder.writeObject( project);
    System.err.println( "save: project name " + name);

    xmlEncoder.close();
  } // end save


  // END EXTEND PwProject 

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
   * <code>setName</code> - with get<...>, makes this a bean property
   *                       and enabled for XMLEncode/Decode
   *
   * @param name - <code>String</code> - 
   */
  public void setName( String name) {
    this.name = name;
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
   * @return - <code>List</code> - of String
   */
  public List getSeqDirNames() {
    return seqDirNames;
  }

  /**
   * <code>setSeqDirNames</code> - with get<...>, makes this a bean property
   *                       and enabled for XMLEncode/Decode
   *
   * @param seqDirNames - <code>List</code> - of String
   */
  public void setSeqDirNames( List seqDirNames) {
    this.seqDirNames = seqDirNames;
  }

  /**
   * <code>getPartialPlanNames</code> - with set<...>, makes this a bean property
   *                       and enabled for XMLEncode/Decode
   *
   * @return - <code>List</code> - List of String
   */
  public List getPartialPlanNames() {
    return partialPlanNames;
  }

  /**
   * <code>setPartialPlanNames</code> - with get<...>, makes this a bean property
   *                       and enabled for XMLEncode/Decode
   *
   * @param partialPlanNames - <code>List</code> - List of String
   */
  public void setPartialPlanNames( List partialPlanNames) {
    this.partialPlanNames = partialPlanNames;
  }



  private String parseProjectName( String url) throws ResourceNotFoundException {
    int index = url.lastIndexOf( System.getProperty( "file.separator"));
    if (index == -1) {
      throw new ResourceNotFoundException( "project url '" + url +
                                           "' cannot be parsed for '" +
                                           System.getProperty( "file.separator") + "'");
    } 
    return url.substring( index + 1);
  } // end parseProjectName

  //made obsolete--no collections anymore
  /*  private String getProjectCollectionName() {
    StringBuffer projectCollectionNameBuf = new StringBuffer( userCollectionName);
    projectCollectionNameBuf.append( System.getProperty( "file.separator")).append( name);
    return projectCollectionNameBuf.toString();
    }*/ 


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
    System.err.println( "restore: projectName " + project.getName());
    System.err.println( "restore: seqDirNames " + project.getSeqDirNames());
    System.err.println( "restore: partialPlanNames " + project.getPartialPlanNames());

    xmlDecoder.close();
    return project;
  } // end restore




} // end class PwProjectImpl
