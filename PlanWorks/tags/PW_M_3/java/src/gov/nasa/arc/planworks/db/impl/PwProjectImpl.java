// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwProjectImpl.java,v 1.25 2003-07-11 22:33:34 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db.impl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
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

  //private static List projectNames;  // element String
  //private static List projects;  // element PwProjectImpl

  private static HashMap projects;

  /**
   * <code>initProjects</code> - 
   *
   */
  public static void initProjects() throws ResourceNotFoundException, SQLException, IOException {
    projects = new HashMap();
    connectToDataBase();

    ResultSet dbProjectNames = MySQLDB.queryDatabase("SELECT ProjectName FROM Project");
    while(dbProjectNames.next()) {
      System.err.println("Got project " + dbProjectNames.getString("ProjectName"));
      projects.put(dbProjectNames.getString("ProjectName"),
                   new PwProjectImpl(dbProjectNames.getString("ProjectName"), true));
    }
  } // end initProjects

  public static PwProject createProject(String name) throws DuplicateNameException, SQLException {
    ResultSet dbProjects = MySQLDB.queryDatabase("SELECT ProjectId FROM Project WHERE ProjectName='".concat(name).concat("'"));
    dbProjects.last();
    if(dbProjects.getRow() > 0) {
      throw new DuplicateNameException("A project named '" + name +
                                       "' already exists.");
    }
    PwProjectImpl retval = null;
    retval = new PwProjectImpl(name);
    projects.put(name, retval);
    MySQLDB.updateDatabase("INSERT INTO Project (ProjectName) VALUES ('".concat(name).concat("')"));
    ResultSet newKey = MySQLDB.queryDatabase("SELECT MAX(ProjectId) AS ProjectId from Project");
    newKey.first();
    retval.setKey(new Integer(newKey.getInt("ProjectId")));
    return retval;
  }
  private static void connectToDataBase() throws SQLException, IOException {
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
  public static PwProject getProject( String name) throws ResourceNotFoundException {
    if(!projects.containsKey(name)) {
      throw new ResourceNotFoundException("Project " + name + " not found.");
    }
    
    return (PwProject) projects.get(name);
  } // end getProject

  /**
   * <code>listProjects</code>
   *
   * @return - <code>List</code> - of String (url)
   */
  public static List listProjects() {
    return new ArrayList(projects.keySet());
  }

  private String name;
  private Integer key;
  private List planningSequences; // element PwPlanningSequence

  /**
   * <code>PwProjectImpl</code> - constructor 
   *                  create a new project from an url
   *                  called from PwProject.createProject
   *
   * @param name - <code>String</code> - 
   * @exception DuplicateNameException if an error occurs
   */
  public PwProjectImpl( String name)  throws DuplicateNameException, SQLException {
    this.name = name;
    key = new Integer(-1);
    planningSequences = new ArrayList();
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
  public PwProjectImpl( String name, boolean isInDb) 
    throws ResourceNotFoundException, SQLException {
    this.name = name;
    ResultSet dbProject = MySQLDB.queryDatabase("SELECT (ProjectId) FROM Project WHERE ProjectName='".concat(name).concat("'"));
    dbProject.last();
    if(dbProject.getRow() == 0) {
      throw new ResourceNotFoundException("Project " + name + " not found in database.");
    }
    dbProject.beforeFirst();
    
    dbProject.first();
    key = new Integer(dbProject.getInt("ProjectId"));
    planningSequences = new ArrayList();
    
    //ResultSet sequences = MySQLDB.queryDatabase("SELECT (Sequence.SequenceURL, Sequence.SequenceId) FROM Sequence WHERE ProjectId=".concat(this.key.toString()));
    ResultSet sequences = MySQLDB.queryDatabase("SELECT SequenceURL, SequenceId FROM Sequence WHERE ProjectId=".concat(this.key.toString()));
    while(sequences.next()) {
      planningSequences.add(new PwPlanningSequenceImpl(sequences.getString("SequenceURL"),
                                                       new Integer(sequences.getInt("SequenceId")),
                                                       this, new PwModelImpl()));
    }

    // this project is already in projectNames & projectUrls
    //projects.add(name, this);
  } // end  constructor PwProjectImpl.openProject

  public Integer getKey() {
    return key;
  }
  
  protected void setKey(Integer key) {
    this.key = key;
  }

  /**
   * <code>getName</code> - project name 
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
    ArrayList retval = new ArrayList();
    ListIterator sequenceIterator = planningSequences.listIterator();
    while(sequenceIterator.hasNext()) {
      retval.add(((PwPlanningSequenceImpl)sequenceIterator.next()).getUrl());
    }
    return retval;
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

  public PwPlanningSequence addPlanningSequence(String url) 
    throws DuplicateNameException, ResourceNotFoundException, SQLException {
    PwPlanningSequenceImpl retval = null;
    ResultSet dupCheck = MySQLDB.queryDatabase("SELECT * FROM Sequence WHERE SequenceURL='".concat(url).concat("'"));
    dupCheck.last();
    if(dupCheck.getRow() != 0) {
      throw new DuplicateNameException("Sequence at " + url + " already in database.");
    }
    planningSequences.add(retval = new PwPlanningSequenceImpl(url, this, new PwModelImpl()));
    return retval;
  }

  /**
   * <code>delete</code> - remove project from /xml/proj/projects.xml, and
   *                      remove /xml/proj/<projectName>.xml
   *
   * @exception Exception if an error occurs
   */
  public void delete() throws Exception, ResourceNotFoundException {
    projects.remove(name);

    try {
      ResultSet sequenceIds = 
        MySQLDB.queryDatabase("SELECT SequenceId FROM Sequence WHERE ProjectId=".concat(key.toString()));
      while(sequenceIds.next()) {
        Integer sequenceId = new Integer(sequenceIds.getInt("SequenceId"));
        ResultSet partialPlanIds =
          MySQLDB.queryDatabase("SELECT PartialPlanId FROM PartialPlan WHERE SequenceId=".concat(sequenceId.toString()));
        while(partialPlanIds.next()) {
          Long partialPlanId = new Long(partialPlanIds.getLong("PartialPlanId"));
          MySQLDB.updateDatabase("DELETE FROM Object WHERE PartialPlanId=".concat(partialPlanId.toString()));
          MySQLDB.updateDatabase("DELETE FROM Timeline WHERE PartialPlanId=".concat(partialPlanId.toString()));
          MySQLDB.updateDatabase("DELETE FROM Slot WHERE PartialPlanId=".concat(partialPlanId.toString()));
          MySQLDB.updateDatabase("DELETE FROM Token WHERE PartialPlanId=".concat(partialPlanId.toString()));
          MySQLDB.updateDatabase("DELETE FROM Variable WHERE PartialPlanId=".concat(partialPlanId.toString()));
          MySQLDB.updateDatabase("DELETE FROM EnumeratedDomain WHERE PartialPlanId=".concat(partialPlanId.toString()));
          MySQLDB.updateDatabase("DELETE FROM IntervalDomain WHERE PartialPlanId=".concat(partialPlanId.toString()));
          MySQLDB.updateDatabase("DELETE FROM VConstraint WHERE PartialPlanId=".concat(partialPlanId.toString()));
          MySQLDB.updateDatabase("DELETE FROM TokenRelation WHERE PartialPlanId=".concat(partialPlanId.toString()));
          MySQLDB.updateDatabase("DELETE FROM ParamVarTokenMap WHERE PartialPlanId=".concat(partialPlanId.toString()));
          MySQLDB.updateDatabase("DELETE FROM ConstraintVarMap WHERE PartialPlanId=".concat(partialPlanId.toString()));
          MySQLDB.updateDatabase("DELETE FROM Predicate WHERE PartialPlanId=".concat(partialPlanId.toString()));
          MySQLDB.updateDatabase("DELETE FROM Parameter WHERE PartialPlanId=".concat(partialPlanId.toString()));
        }
        MySQLDB.updateDatabase("DELETE FROM Sequence WHERE SequenceId=".concat(sequenceId.toString()));
      }
      MySQLDB.updateDatabase("DELETE FROM Project WHERE ProjectId=".concat(key.toString()));
    }
    catch(SQLException sqle) { //are we transactional?  can we roll back?
      System.err.println(sqle);
      sqle.printStackTrace();
      return;
    }
  } // end close
} // end class PwProjectImpl
