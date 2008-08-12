// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwProjectImpl.java,v 1.55 2006-10-03 16:14:16 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db.impl;

import java.awt.Frame;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.ConfigureAndPlugins;
import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.BooleanFunctor;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.db.util.SQLDB;

/**
 * <code>PwProjectImpl</code> - Data base API for PlanWorks
 *                manages Project instances
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwProjectImpl extends PwProject {

  private static HashMap projects;

  /**
   * <code>initProjects</code> - initialize projects in the database
   *
   */
  public synchronized static void initProjects() 
    throws ResourceNotFoundException, IOException {
    projects = new HashMap();
    connectToDataBase();

    boolean isProjectInDb = true;
    ListIterator dbProjectNameIterator = SQLDB.getProjectNames().listIterator();
    while(dbProjectNameIterator.hasNext()) {
      String workingDir = null, plannerPath = null, modelName = null, modelPath = null;
      String modelOutputDestDir = null, modelInitStatePath = null, ruleDelimiters = null;
      String name = (String) dbProjectNameIterator.next();
      if (ConfigureAndPlugins.isProjectInConfigMap( name)) {
//           workingDir = ConfigureAndPlugins.getProjectConfigValue
//             ( ConfigureAndPlugins.PROJECT_WORKING_DIR, name);
//           plannerPath = ConfigureAndPlugins.getProjectConfigValue
//             ( ConfigureAndPlugins.PROJECT_PLANNER_PATH, name);
//           modelName = ConfigureAndPlugins.getProjectConfigValue
//             ( ConfigureAndPlugins.PROJECT_MODEL_NAME, name);
//           modelPath = ConfigureAndPlugins.getProjectConfigValue
//             ( ConfigureAndPlugins.PROJECT_MODEL_PATH, name);
//           modelOutputDestDir = ConfigureAndPlugins.getProjectConfigValue
//             ( ConfigureAndPlugins.PROJECT_MODEL_OUTPUT_DEST_DIR, name);
//           modelInitStatePath = ConfigureAndPlugins.getProjectConfigValue
//             ( ConfigureAndPlugins.PROJECT_MODEL_INIT_STATE_PATH, name);
      } else {
        System.err.println( "initProjects: PROJECT_CONFIG_MAP entry created for project '" +
                             name + "'");
        // give new project default vaulue for all config vars
        List nameValueList = new ArrayList();
        nameValueList.add( ConfigureAndPlugins.PROJECT_WORKING_DIR);
        workingDir = ConfigureAndPlugins.getProjectConfigValue
          ( ConfigureAndPlugins.PROJECT_WORKING_DIR, ConfigureAndPlugins.DEFAULT_PROJECT_NAME);
        nameValueList.add( workingDir);
        ConfigureAndPlugins.updateProjectConfigMap
          ( name, ConfigureAndPlugins.completeProjectConfigMap( nameValueList));
      }
//       projects.put(name, new PwProjectImpl( name, isProjectInDb, workingDir, plannerPath,
//                                             modelName, modelPath, modelOutputDestDir,
//                                             modelInitStatePath));
      projects.put(name, new PwProjectImpl( name, isProjectInDb));
    }
  } // end initProjects

  /**
   * <code>createProject</code> - create named project not in database
   *
   * @param name - <code>String</code> - 
//    * @param workingDir - <code>String</code> - 
   * @return - <code>PwProject</code> - 
   * @exception DuplicateNameException if an error occurs
   */
//   public static PwProject createProject( final String name, final String workingDir)
  public static PwProject createProject( final String name) throws DuplicateNameException {
    if(SQLDB.projectExists(name)) {
      throw new DuplicateNameException("A project named '" + name + "' already exists.");
    }
    PwProjectImpl retval = null;
//     retval = new PwProjectImpl( name, workingDir);
    retval = new PwProjectImpl( name);
    projects.put(name, retval);

    SQLDB.addProject(name);
    retval.setId(SQLDB.latestProjectId());
    System.err.println("Created project " + retval.getName() + " with id " + retval.getId());
    return retval;
  }

  /**
   * <code>connectToDataBase</code> - establish database connection
   * @exception IOException if the database fails to start
   */

  private static void connectToDataBase() throws IOException {
    System.err.println("Starting database ...");
    long startTime = System.currentTimeMillis();
    SQLDB.startDatabase();
    startTime = System.currentTimeMillis() - startTime;
    System.err.println("   ... elapsed time: " + startTime + "msecs.");
    System.err.println("Connecting to database ...");
    long connectTime = System.currentTimeMillis();
    SQLDB.registerDatabase();
    connectTime = System.currentTimeMillis() - connectTime;
    System.err.println("   ... elapsed time: " + connectTime + "msecs.");
  } // end connectToExistDataBase

  /**
   * <code>getProject</code> - get a project object by its name.
   *
   * @param name - <code>String</code> - the name of the project
   * @return - <code>PwProject</code> - the project datastructure
   * @exception - <code>ResourceNotFoundException</code> if no project by that name exists
   */
  public static PwProject getProject( final String name) throws ResourceNotFoundException {
    if(!projects.containsKey(name)) {
      throw new ResourceNotFoundException("Project " + name + " not found.");
    }
    return (PwProject) projects.get(name);
  } // end getProject

  /**
   * <code>listProjects</code>
   *
   * @return - <code>List</code> - of String (names)
   */
  public static List listProjects() {
    return new ArrayList(projects.keySet());
  }

  private String name;
  private Integer id;
  private List planningSequences; // element PwPlanningSequence
  private Map sequenceIdUrlMap;
  // set from projects config file
//   private String workingDir;
//   private String plannerPath;
//   private String modelName;
//   private String modelPath;
//   private String modelOutputDestDir;
//   private String modelInitStatePath;
  private boolean jniAdapterLoaded;
  
  //for use in testing only!
  public PwProjectImpl(final Integer id, final String name) {
    this.name = name;
    this.id = id;
    planningSequences = new ArrayList();
    sequenceIdUrlMap = new HashMap();
  }

  /**
   * <code>PwProjectImpl</code> - constructor 
   *                  create a new project from an url
   *                  called from PwProject.createProject
   *
   * @param name - <code>String</code> - 
//    * @param workingDir - <code>String</code> - 
   */
//   public PwProjectImpl( final String name, final String workingDir) {
  public PwProjectImpl( final String name) {
    this.name = name;
    id = new Integer(-1);
//     this.workingDir = workingDir;
//     this.plannerPath = null;
//     this.modelName = null;
//     this.modelPath = null;
//     this.modelOutputDestDir = null;
//     this.modelInitStatePath = null;
    jniAdapterLoaded = false;
    planningSequences = new ArrayList();
    //sequenceIdUrlMap = new HashMap();
    sequenceIdUrlMap = new HashMap();
  } // end  constructor PwProjectImpl.createProject

  /**
   * <code>PwProjectImpl</code> - constructor
   *                  construct project from information in database.
   *
   * @param name - <code>String</code> project name
   * @param isInDb - <code>boolean</code> -  boolean used to differentiate between constructors
//    * @param workingDir - <code>String</code> - 
//    * @param plannerPath - <code>String</code> - 
//    * @param modelName - <code>String</code> - 
//    * @param modelPath - <code>String</code> - 
//    * @param modelOutputDestDir - <code>String</code> - 
//    * @param modelInitStatePath - <code>String</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
//   public PwProjectImpl( final String name, final boolean isInDb, final String workingDir,
//                         final String plannerPath, final String modelName, final String modelPath,
//                         final String modelOutputDestDir, final String modelInitStatePath) 
  public PwProjectImpl( final String name, final boolean isInDb) 
    throws ResourceNotFoundException {
    this.name = name;
//     this.workingDir = workingDir;
//     this.plannerPath = plannerPath;
//     this.modelName = modelName;
//     this.modelPath = modelPath;
//     this.modelOutputDestDir = modelOutputDestDir;
//     this.modelInitStatePath = modelInitStatePath;
    jniAdapterLoaded = false;
    
    id = SQLDB.getProjectIdByName(name);
    if(id == null) {
      throw new ResourceNotFoundException("Project " + name + " not found in database.");
    }
    planningSequences = new ArrayList();
    //Map sequences = SQLDB.getSequences(id);
    sequenceIdUrlMap = SQLDB.getSequences(id); //sequenceId->sequenceUrl
    Iterator seqIdIterator = sequenceIdUrlMap.keySet().iterator();
    while(seqIdIterator.hasNext()) {
      Long sequenceId = (Long) seqIdIterator.next();
      String seqUrl = (String) sequenceIdUrlMap.get(sequenceId);
      try {
        planningSequences.add(new PwPlanningSequenceImpl(seqUrl, sequenceId, name));
      }
      catch(ResourceNotFoundException rnfe) {
        if(rnfe.getMessage().indexOf("is not a valid sequence directory") != -1) {
          invalidSequenceDialog(sequenceId, seqUrl, seqIdIterator);
        }
        else {
          throw rnfe;
        }
      }
    }
  } // end  constructor PwProjectImpl.openProject

  private void invalidSequenceDialog(final Long seqId, final String seqUrl, final Iterator it) 
  throws ResourceNotFoundException {
    Frame f = new Frame();
    int choice = JOptionPane.showOptionDialog(f, "Sequence " + seqUrl + 
                                              " is invalid.\nDelete it from the database? (Data that is loaded is still viewable) ", 
                                              "Invalid Sequence Directory", 
                                              JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE,
                                              null, null, null);
    if(choice == 0) {
      SQLDB.deletePlanningSequence(seqId);
      it.remove();
    }
  }

  /**
   * <code>getId</code> - get the project's Id.
   *
   * @return - <code>Integer</code> - the Id.
   */

  public Integer getId() {
    return id;
  }
  
  protected void setId(final Integer id) {
    this.id = id;
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
   * <code>getWorkingDir</code>
   *
   * @return - <code>String</code> - 
   */
//   public String getWorkingDir() {
//     return workingDir;
//   }

  /**
   * <code>getPlannerPath</code>
   *
   * @return - <code>String</code> - 
   */
//   public String getPlannerPath() {
//     return plannerPath;
//   }

  /**
   * <code>getModelName</code>
   *
   * @return - <code>String</code> - 
   */
//   public String getModelName() {
//     return modelName;
//   }

  /**
   * <code>getModelPath</code>
   *
   * @return - <code>String</code> - 
   */
//   public String getModelPath() {
//     return modelPath;
//   }

  /**
   * <code>getModelOutputDestDir</code>
   *
   * @return - <code>String</code> - 
   */
//   public String getModelOutputDestDir() {
//     return modelOutputDestDir;
//   }

  /**
   * <code>getModelInitStatePath</code>
   *
   * @return - <code>String</code> - 
   */
//   public String getModelInitStatePath() {
//     return modelInitStatePath;
//   }

  /**
   * <code>listPlanningSequences</code>
   *
   * @return - <code>List</code> -  List of Strings (urls of sequences)
   *                                each sequence is set of partial plans
   */
  public List listPlanningSequences() {
    return new ArrayList(sequenceIdUrlMap.values());
  } // end listPlanningSequences

  class PlanningSequenceNameEquals implements BooleanFunctor {
    private String name;
    public PlanningSequenceNameEquals(String name){this.name = name;}
    public final boolean func(Object n){return name.equals(((PwPlanningSequence)n).getUrl());}
  }

  class PlanningSequenceIdEquals implements BooleanFunctor {
    private Long id;
    public PlanningSequenceIdEquals(Long id){this.id = id;}
    public final boolean func(Object n){return id.equals(((PwPlanningSequence)n).getId());}
  }

  /**
   * <code>getPlanningSequence</code>
   *
   * @param url - <code>String</code> - 
   * @return - <code>PwPlanningSequence</code> - 
   */
  public PwPlanningSequence getPlanningSequence( final String url)
    throws ResourceNotFoundException {
    PwPlanningSequence retval;
    if(!sequenceIdUrlMap.containsValue(url)) {
      throw new ResourceNotFoundException("getPlanningSequence could not find " + url);
    }
    //List temp = CollectionUtils.lGrep(new PlanningSequenceNameEquals(url), planningSequences);
    retval = (PwPlanningSequence) CollectionUtils.findFirst(new PlanningSequenceNameEquals(url),
                                                            planningSequences);
    if(retval == null) {
      planningSequences.add(retval = new PwPlanningSequenceImpl(url, this));
    }
    return retval;
  } // end getPlanningSequence

  public PwPlanningSequence getPlanningSequence(final Long seqId) throws ResourceNotFoundException {
    PwPlanningSequence retval;
    if(!sequenceIdUrlMap.containsKey(seqId)) {
      throw new ResourceNotFoundException("getPlanning sequence could not find " + seqId);
    }
    retval = (PwPlanningSequence) CollectionUtils.findFirst(new PlanningSequenceIdEquals(seqId),
                                                            planningSequences);
    if(retval == null) {
      planningSequences.add(retval = 
                            new PwPlanningSequenceImpl((String) sequenceIdUrlMap.get(seqId),this));
    }
    return retval;
  }

  public PwPlanningSequence addPlanningSequence(final String url) 
    throws DuplicateNameException, ResourceNotFoundException {
    PwPlanningSequenceImpl retval = null;
    if(SQLDB.sequenceExists(url)) {
      throw new DuplicateNameException("Sequence at " + url + " already in database.");
    }
    planningSequences.add(retval = new PwPlanningSequenceImpl( url, this));
    sequenceIdUrlMap.put(retval.getId(), retval.getUrl());
    return retval;
  }

  //for use in testing only!
  public PwPlanningSequence addPlanningSequence(final PwPlanningSequenceImpl seq) {
    sequenceIdUrlMap.put(seq.getId(), seq.getUrl());
    planningSequences.add(seq);
    return seq;
  }

  public void deletePlanningSequence(final String seqName) throws ResourceNotFoundException {
    PwPlanningSequence seq = null;
    if((seq = closePlanningSequence(seqName)) != null) {
      sequenceIdUrlMap.remove(seq.getId());
    }
  }

  public void deletePlanningSequence(final Long seqId) throws ResourceNotFoundException {
    PwPlanningSequence seq = null;
    if((seq = closePlanningSequence(seqId)) != null) {
      sequenceIdUrlMap.remove(seq.getId());
    }
  }

  public PwPlanningSequence closePlanningSequence(final String seqName) 
    throws ResourceNotFoundException {
    if(!sequenceIdUrlMap.containsValue(seqName)) {
      throw new ResourceNotFoundException("Failed to find a sequence with url " + seqName);
    }
    PwPlanningSequence seq = 
      (PwPlanningSequence) CollectionUtils.findFirst(new PlanningSequenceNameEquals(seqName),
                                                     planningSequences);
    if(seq != null) {
      planningSequences.remove(seq);
      System.gc();
      // planningSequences.add(new PwPlanningSequenceImpl(seqName, seq.getId()));
      planningSequences.add(new PwPlanningSequenceImpl(seq.getUrl(), this));
      return seq;
    }
    return null;
  }

  public PwPlanningSequence closePlanningSequence(final Long seqId) 
    throws ResourceNotFoundException{
    if(!sequenceIdUrlMap.containsKey(seqId)) {
      throw new ResourceNotFoundException("Failed to find sequence with id " + seqId);
    }
    PwPlanningSequence seq = 
      (PwPlanningSequence) CollectionUtils.findFirst(new PlanningSequenceIdEquals(seqId),
                                                     planningSequences);
    if(seq != null) {
      planningSequences.remove(seq);
      System.gc();
      // planningSequences.add(new PwPlanningSequenceImpl(seq.getUrl(), seqId));
      planningSequences.add(new PwPlanningSequenceImpl(seq.getUrl(), this));
      return seq;
    }
    return null;
  }

  /**
   * <code>delete</code> - remove this project from list of projects and database
   *
   * @exception ResourceNotFoundException if an error occurs
   */
  public void delete() throws ResourceNotFoundException {
    long t1 = System.currentTimeMillis();
    projects.remove(name);
    SQLDB.deleteProject(id);
    System.err.println("Deleting project took " + (System.currentTimeMillis() - t1) + "ms");
  } // end delete

  public String toOutputString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(name).append("\n");
    return retval.toString();
  }


//   public abstract void setWorkingDir( final String dir);
//   public abstract void setPlannerPath( final String path);
//   public abstract void setModelName( final String name);
//   public abstract void setModelPath( final String path);
//   public abstract void setModelOutputDestDir( final String dir);
//   public abstract void setModelInitStatePath( final String path);

  public boolean getJNIAdapterLoaded() {
    return jniAdapterLoaded;
  }

  public void setJNIAdapterLoaded( boolean value) {
    jniAdapterLoaded = value;
  }

} // end class PwProjectImpl
