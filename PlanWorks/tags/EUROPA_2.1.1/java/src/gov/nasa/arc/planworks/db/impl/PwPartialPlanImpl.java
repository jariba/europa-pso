// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPartialPlanImpl.java,v 1.111 2006-10-03 16:14:16 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.ProgressMonitor;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwEntity;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwPredicate;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwResourceInstant;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwRule;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwRuleInstance;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.util.SQLDB;
import gov.nasa.arc.planworks.db.util.PwSQLFilenameFilter;
import gov.nasa.arc.planworks.util.OneToManyMap;
import gov.nasa.arc.planworks.util.BooleanFunctor;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.FunctorFactory;
import gov.nasa.arc.planworks.util.CreatePartialPlanException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.util.UnaryFunctor;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;


/**
 * <code>PwPartialPlanImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwPartialPlanImpl implements PwPartialPlan, ViewableObject {

  private String url; 
  private boolean isDummyPartialPlan;
  //private Long sequenceId;
  private PwPlanningSequenceImpl sequence;
  private String model;
  private String name;
  private String seqName;
  private Long id; // PartialPlan id
  private Map objectMap; // key = attribute id, value = PwObjectImpl instance
  private Map timelineMap; // key = attribute id, value = PwTimelineImpl instance
  private Map resourceMap; // key = attribute id, value = PwResourceImpl instance
  private Map slotMap; // key = attribute id, value = PwSlotImpl instance
  private Map tokenMap; // key = attribute id, value = PwTokenImpl instance
  private Map constraintMap; // key = attribute id, value = PwConstraintImpl instance
  //private Map predicateMap; // key = attribute id, value = PwPredicateImpl instance
  private Map ruleInstanceMap; // key = attribute id, value = PwRuleInstanceImpl instance
  private Map variableMap; // key = attribute id, value = PwVariableImpl instance
  private List contentSpec;
  private Map instantMap; //just add water.  key = tokenId, value = PwResourceInstantImpl instance
  private Map resTransactionMap; //key = transactionId, value = PwResourceTransactionImpl instance
  private Map tokenChildRuleInstIdMap; // key = masterTokenId, value = List RuleInstanceId
  private ProgressMonitor progressMonitor;
  private boolean isProgressMonitorCancel;

  /**
   * <code>PwPartialPlanImpl</code> - initialize storage structures then call createPartialPlan()
   *
   * @param - <code>url</code> - path to a directory containing plan data
   * @param - <code>planName</code> - the name of the partial plan (usually stepN)
   * @param - <code>sequenceId</code> - the Id of the sequence to which this plan is attached.
   * @exception ResourceNotFoundException if the plan data is invalid
   * @exception CreatePartialPlanException if the user interrupts the plan creation
   */
  public PwPartialPlanImpl(final String url, final String planName, 
                           final PwPlanningSequenceImpl sequence) 
    throws ResourceNotFoundException, CreatePartialPlanException {
    this.sequence = sequence;
    //System.err.println("In PwPartialPlanImpl");
    //objectMap = new HashMap();
    objectMap = new TreeMap();
    timelineMap = new HashMap();
    slotMap = new HashMap();
    tokenMap = new HashMap();
    constraintMap = new HashMap();
    //predicateMap = new HashMap();
    ruleInstanceMap = new HashMap(); 
    tokenChildRuleInstIdMap = new OneToManyMap();
    resourceMap = new HashMap();
    resTransactionMap = new HashMap();
    variableMap = new HashMap();
    instantMap = new HashMap();
    this.url = (new StringBuffer(url)).append(System.getProperty("file.separator")).
      append(planName).toString();
    contentSpec = new ArrayList();
    this.name = planName;
    isDummyPartialPlan = false;
    createPartialPlan();
  }

  /**
   * <code>PwPartialPlanImpl</code> - constructor - dummy pp called by createPartialPlanView
   *
   * @param planName - <code>String</code> - 
   * @param sequence - <code>PwPlanningSequenceImpl</code> - 
   */
  public PwPartialPlanImpl(final String planName, final PwPlanningSequence sequence) {
    this.sequence = (PwPlanningSequenceImpl) sequence;
    this.name = planName;
    this.url = sequence.getUrl() + System.getProperty("file.separator") + planName;
    this.isDummyPartialPlan = true;
    this.id = SQLDB.getPartialPlanIdByStepNum( sequence.getId(),
						 Integer.parseInt( planName.substring( 4)));
//     System.err.println( "seqId " + sequence.getId() + " planName " + planName +
// 			" partialPlan.getId() " + id);
  }

  //for testing only
  public PwPartialPlanImpl(final String url, final String planName, 
                           final PwPlanningSequenceImpl sequence, final Long partialPlanId,
                           final String model) 
    throws ResourceNotFoundException {
    this.sequence = sequence;
    //System.err.println("In PwPartialPlanImpl");
    //objectMap = new HashMap();
    objectMap = new TreeMap();
    timelineMap = new HashMap();
    slotMap = new HashMap();
    tokenMap = new HashMap();
    constraintMap = new HashMap();
    //predicateMap = new HashMap();
    ruleInstanceMap = new HashMap(); 
    tokenChildRuleInstIdMap = new OneToManyMap();
    resourceMap = new HashMap();
    resTransactionMap = new HashMap();
    variableMap = new HashMap();
    instantMap = new HashMap();
    this.url = (new StringBuffer(url)).append(System.getProperty("file.separator")).
      append(planName).toString();
    contentSpec = new ArrayList();
    this.name = planName;

    id = partialPlanId;
    this.model = model;
    isDummyPartialPlan = false;
  }

  /**
   * <code>createPartialPlan</code> - load plan data into the database and/or construct substructures
   *
   * @param - <code>sequenceId</code> - Id of the sequence to which this plan is attached
   * @exception ResourceNotFoundException if the plan data is invalid
   * @exception CreatePartialPlanException if the user interrupts the plan creation
   */

  private void createPartialPlan() throws ResourceNotFoundException, CreatePartialPlanException {
    boolean printTime = true;
    // boolean printTime = false;
    boolean doProgMonitor = true;
    if (System.getProperty("ant.target.test").equals( "true")) {
      doProgMonitor = false;
    }
    int numOperations = 6;
    if (doProgMonitor) {
      progressMonitorThread( "Partial Plan:", 0, numOperations, "");
      if (! progressMonitorWait()) {
        return;
      }
    }
    numOperations = 1;
    System.err.println( "Create PartialPlan ...");
    long startTimeMSecs = System.currentTimeMillis();
    long loadTime = 0L;
    HashMap existingPartialPlan = null;
    id = SQLDB.getPartialPlanIdByName(sequence.getId(), name);
    long t1 = 0L, t2 = 0L, t3 = 0L, t4 = 0L, t5 = 0L;
    if (printTime) {
      t2 = System.currentTimeMillis();
    }
    if(id == null) {
      File planDir = new File(sequence.getUrl() + System.getProperty("file.separator") +
                              name);
      if (doProgMonitor) {
        progressMonitor.setNote( "Loading files ...");
        progressMonitor.setProgress( numOperations * ViewConstants.MONITOR_MIN_MAX_SCALING);
      }
      loadFiles(planDir);
      if (printTime) {
        t1 = System.currentTimeMillis();
        System.err.println( "   ... loadFiles elapsed time: " + (t1 - startTimeMSecs) + " msecs.");
      }
      if (doProgMonitor) {
        checkProgressMonitor(); numOperations++;
        progressMonitor.setProgress( numOperations * ViewConstants.MONITOR_MIN_MAX_SCALING);
        progressMonitor.setNote( "Analyzing Database ...");
      }
      SQLDB.analyzeDatabase();
      if (printTime) {
        t2 = System.currentTimeMillis();
        System.err.println( "   ... analyzeDatabase elapsed time: " + (t2 - t1) + " msecs.");
      }
      if (doProgMonitor) {
        checkProgressMonitor(); numOperations++;
        progressMonitor.setProgress( numOperations * ViewConstants.MONITOR_MIN_MAX_SCALING);
      }
      id = SQLDB.getPartialPlanIdByName(sequence.getId(), name);
    }
    numOperations = 2;
    if (doProgMonitor) {
      progressMonitor.setNote( "Creating Objects ...");
      progressMonitor.setProgress( numOperations * ViewConstants.MONITOR_MIN_MAX_SCALING);
    }
    SQLDB.createObjects(this);
    if (printTime) {
      t3 = System.currentTimeMillis();
      System.err.println( "   ... createObjects elapsed time: " + (t3 - t2) + " msecs.");
    }
    model = SQLDB.queryPartialPlanModelById(id);

    if (doProgMonitor) {
      checkProgressMonitor(); numOperations++;
      progressMonitor.setProgress( numOperations * ViewConstants.MONITOR_MIN_MAX_SCALING);
      progressMonitor.setNote( "Query DB & Create Slot/Token Nodes ...");
    }
    SQLDB.createSlotTokenNodesStructure(this, sequence.getId());
    if (printTime) {
      t4 = System.currentTimeMillis();
      System.err.println( "   ... createSlotTokenNodesStructure elapsed time: " +
                          (t4 - t3) + " msecs.");
    }
    if (doProgMonitor) {
      checkProgressMonitor(); numOperations++;
      progressMonitor.setProgress( numOperations * ViewConstants.MONITOR_MIN_MAX_SCALING);
      progressMonitor.setNote( "Filling Element Maps ...");
    }
    long start2TimeMSecs = System.currentTimeMillis();

    fillElementMaps();
    long stop2TimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... Fill PartialPlan element maps elapsed time: " +
                        (stop2TimeMSecs - start2TimeMSecs) + " msecs.");
    if (doProgMonitor) {
      checkProgressMonitor(); numOperations++;
      progressMonitor.setProgress( numOperations * ViewConstants.MONITOR_MIN_MAX_SCALING);
      progressMonitor.setNote( "Cleaning Constraints ...");
    }
    cleanConstraints();
    if (printTime) {
      t5 = System.currentTimeMillis();
      System.err.println( "   ... cleanConstraints elapsed time: " +
                          (t5 - stop2TimeMSecs) + " msecs.");
    }
    //checkPlan();  //for checkPlan debug only -- normally runs from Backend Test
    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... Create PartialPlan elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    if (doProgMonitor) {
      isProgressMonitorCancel = true;
    }
  } // end createPartialPlan

  private void checkProgressMonitor() throws CreatePartialPlanException {
    if (progressMonitor.isCanceled()) {
      isProgressMonitorCancel = true;
      String msg = "User Canceled Create Partial Plan";
      System.err.println( msg);
      throw new CreatePartialPlanException( msg);
    }
  } // end checkProgressMonitor

  //  private void loadFiles(File planDir) throws ResourceNotFoundException {
  public static void loadFiles(final File planDir) throws ResourceNotFoundException {
    if(planDir == null) {
      throw new ResourceNotFoundException("Failed to find sequence directory.");
    }
    String [] fileNames = planDir.list(new PwSQLFilenameFilter());
    if(fileNames == null) {
      throw new ResourceNotFoundException("Failed to get file listing for " +
                                          planDir.getName());
    }
    for(int i = 0; i < fileNames.length; i++) {
      String tableName = fileNames[i].substring(fileNames[i].lastIndexOf(".") + 1);
      tableName = tableName.substring(0,1).toUpperCase().concat(tableName.substring(1));
      if(tableName.lastIndexOf("s") == tableName.length() - 1) {
        tableName = tableName.substring(0, tableName.length() - 1);
      }
      if(tableName.equals("Constraint")) {
        tableName = "VConstraint";
      }
      if(tableName.equals("Instant")) {
        tableName = "ResourceInstants";
      }
      SQLDB.loadFile(planDir.getAbsolutePath().concat(System.getProperty("file.separator")).
                       concat(fileNames[i]), tableName);
    }
  }

  class TimelineSlotCreator implements UnaryFunctor {
    public TimelineSlotCreator(){}
    public Object func(Object o) {
      if(o instanceof PwTimelineImpl){((PwTimelineImpl)o).finishSlots();} return o;}
  }

  /**
   * <code>fillElementMaps</code> - construct constraint, predicate, token relation, and variable
   *                                structures.
   */

  private final void fillElementMaps() {
    SQLDB.queryConstraints( this);

    SQLDB.queryRuleInstances( this);
    SQLDB.queryVariables( this);
    SQLDB.queryResourceInstants(this);
    tokenChildRuleInstIdMap = SQLDB.queryAllChildRuleInstanceIds(this);
    CollectionUtils.cInPlaceMap(new TimelineSlotCreator(), objectMap.values());

    System.err.println( "Partial Plan: " + url);
    System.err.println( "Ids:");
    // System.err.println( "  objects              " + objectMap.keySet().size());
    int numObjects = 0;
    Iterator objItr = objectMap.keySet().iterator();
    while (objItr.hasNext()) {
      Integer key = (Integer) objItr.next();
      if ((resourceMap.get( key) == null) && (timelineMap.get( key) == null)) {
        numObjects++;
      }
    }
    System.err.println( "  objects              " + numObjects);
    System.err.println( "  resources            " + resourceMap.keySet().size());
    System.err.println( "  timelines            " + timelineMap.keySet().size());
    System.err.println( "  slots                " + slotMap.keySet().size());
    System.err.println( "  tokens               " + tokenMap.keySet().size());
    System.err.println( "  constraints          " + constraintMap.keySet().size());
    System.err.println( "  ruleInstances        " + ruleInstanceMap.keySet().size());
    System.err.println( "  variables            " + variableMap.keySet().size());
    System.err.println( "  resourceTransactions " + resTransactionMap.keySet().size());
    System.err.println( "  resourceInstants     " + instantMap.keySet().size());

  } // end fillElementMaps

  public PwEntity getEntity(final Integer id) {
    PwEntity retval = (PwEntity) objectMap.get(id);
    if(retval == null)
      retval = (PwEntity) timelineMap.get(id);
    if(retval == null)
      retval = (PwEntity) resourceMap.get(id);
    if(retval == null)
      retval = (PwEntity) slotMap.get(id);
    if(retval == null)
      retval = (PwEntity) tokenMap.get(id);
    if(retval == null)
      retval = (PwEntity) constraintMap.get(id);
    if(retval == null)
      retval = (PwEntity) ruleInstanceMap.get(id);
    if(retval == null)
      retval = (PwEntity) variableMap.get(id);
    if(retval == null)
      retval = (PwEntity) resTransactionMap.get(id);
    return retval;
  }

  /**
   * <code>getObjectIdList</code> - get complete list of Ids for PwObject objects
   *
   * @return - <code>List</code> - of Integer
   */
  public List getObjectIdList() {
    return new ArrayList(objectMap.keySet());
  }

  /**
   * <code>getObjectImpl</code> - get a PwObjectImpl object.  Not accessable outside of this package
   *
   * @param id - <code>Integer</code> - the Id of the desired PwObjectImpl
   * @return - <code>PwObjectImpl</code> - 
   */
  public PwObjectImpl getObjectImpl( final Integer id) {
    return (PwObjectImpl) objectMap.get( id);
  }



  // IMPLEMENT PwPartialPlan INTERFACE 
    

  /**
   * <code>isDummyPartialPlan</code> -  dummy partial plan "flag" for DBTransactionView
   *                              when step files are not in database or on disk
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isDummyPartialPlan() {
    return isDummyPartialPlan;
  }

  /**
   * <code>getUrl</code> - get URL of partial plan files
   *
   * @return - <code>String</code> - 
   */
  public String getUrl() {
    return url;
  }

  /**
   * <code>getId</code> - get the Id of this partial plan
   *
   *@return - <code>Long</code>
   */
  public Long getId() {
    return id;
  }

  /**
   * <code>getObjectList</code> -
   *
   * @return - <code>List</code> - of PwObjectImpl
   */
  public List getObjectList() {
    List retval = new ArrayList();
    retval.addAll(objectMap.values());
    return retval;
  }

  /**
   * <code>getResourceList</code>
   *
   * @return - <code>List</code> - of PwResourceImpl
   */
  public List getResourceList() {
    List retval = new ArrayList();
    retval.addAll(resourceMap.values());
    return retval;
  }

  /**
   * <code>getTimelineList</code>
   *
   * @return - <code>List</code> - of PwTimelineImpl
   */
  public List getTimelineList() {
    List retval = new ArrayList();
    retval.addAll(timelineMap.values());
    return retval;
  }

  /**
   * <code>getSlotList</code>
   *
   * @return - <code>List</code> - of PwSlotImpl
   */
  public List getSlotList() {
    List retval = new ArrayList();
    retval.addAll(slotMap.values());
    return retval;
  }

  /**
   * <code>getRuleInstanceList</code>
   *
   * @return - <code>List</code> - of PwRuleInstanceImpl
   */
  public List getRuleInstanceList() {
    List retval = new ArrayList();
    retval.addAll(ruleInstanceMap.values());
    return retval;
  }

  /**
   * <code>getResTransactionList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getResTransactionList() {
    List retval = new ArrayList();
    retval.addAll(resTransactionMap.values());
    return retval;
  }

  class FreeTokenFunctor implements BooleanFunctor {
    public FreeTokenFunctor(){}
    public boolean func(Object o){return ((PwToken)o).isFree();}
  }

  /**
   * <code>getFreeTokenList</code> - get a list of free tokens in this plan
   *
   * @return - <code>List</code> - of PwToken 
   */
  public List getFreeTokenList() {
    return new ArrayList(CollectionUtils.cGrep(new FreeTokenFunctor(),
                                               new ArrayList( tokenMap.values())));
  }

  class SlottedTokenFunctor implements BooleanFunctor {
    public SlottedTokenFunctor(){}
    public boolean func(Object o){return ((PwToken)o).isSlotted();}
  }

  public List getSlottedTokenList() {
    return new ArrayList(CollectionUtils.cGrep(new SlottedTokenFunctor(),
                                               new ArrayList( tokenMap.values())));
  }

  public List getTokenList() {
    return new ArrayList(tokenMap.values());
  }

  public List getVariableList() {
    return new ArrayList(variableMap.values());
  }

  public List getConstraintList() {
    return new ArrayList(constraintMap.values());
  }

  public List getInstantList() {
    return new ArrayList(instantMap.values());
  }

  public List getObjectList(List objectIds) {
    return CollectionUtils.validValues(objectMap, objectIds);
  }

  public List getVariableList(List varIds) {
    return CollectionUtils.validValues(variableMap, varIds);
  }

  public List getTokenList(List tokenIds) {
    return CollectionUtils.validValues(tokenMap, tokenIds);
  }

  public List getSlotList(List slotIds) {
    return CollectionUtils.validValues(slotMap, slotIds);
  }

  public List getInstantList(List instIds) {
    return CollectionUtils.validValues(instantMap, instIds);
  }

  public List getConstraintList(List constrIds) {
    return CollectionUtils.validValues(constraintMap, constrIds);
  }

  public List getResourceTransactionList(List resTransIds) {
    return CollectionUtils.validValues(resTransactionMap, resTransIds);
  }

  /**
   * <code>getObject</code> - get object by Id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwObject</code> - 
   */
  public PwObject getObject( final Integer id) {
    return (PwObject)objectMap.get(id);
  } // end getObject


  /**
   * <code>getTimeline</code> - get timeline by id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwTimeline</code> - 
   */
  public PwTimeline getTimeline( final Integer id) {
    return (PwTimeline)timelineMap.get(id);
  } // end getTimeline


  /**
   * <code>getSlot</code> - get slot by id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwSlot</code> - 
   */
  public PwSlot getSlot( final Integer id) {
    return (PwSlot)slotMap.get(id);
  } // end getSlot


  /**
   * <code>getToken</code> - get token by id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwToken</code> - 
   */
  public PwToken getToken( final Integer id) {
    return (PwToken) tokenMap.get(id);
  } // end getToken


  /**
   * <code>getConstraint</code> - get constraint by id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwConstraint</code> - 
   */
  public PwConstraint getConstraint( final Integer id) {
    return (PwConstraint) constraintMap.get(id);
  } // end getConstraint

  /**
   * <code>getRuleInstance</code> - get rule instance by id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwRuleInstance</code> - 
   */
  public PwRuleInstance getRuleInstance( final Integer id) {
    return (PwRuleInstance) ruleInstanceMap.get(id);
  } // end getRuleInstance


  public PwResourceInstant getInstant(final Integer id) {
    return (PwResourceInstant) instantMap.get(id);
  }

  /**
   * <code>getVariable</code> - get variable by id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getVariable( final Integer id) {
    return (PwVariable) variableMap.get(id);
  } // end getVariable


  /**
   * <code>getResource</code>
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwResource</code> - 
   */
  public PwResource getResource(final Integer id) {
    return (PwResource) resourceMap.get(id);
  }

  /**
   * <code>getResourceTransaction</code>
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwResourceTransaction</code> - 
   */
  public PwResourceTransaction getResourceTransaction(final Integer id) {
    return (PwResourceTransaction) resTransactionMap.get(id);
  }

  // END IMPLEMENT PwPartialPlan INTERFACE 
    

  /**
   * <code>tokenExists</code> - check to see if token with specified id is present
   *
   * @return - <code>boolean</code> - return value of tokenMap.containsKey()
   */

  public boolean tokenExists(final Integer id) {
    return tokenMap.containsKey(id);
  }

  /**
   * <code>addObject</code> - add a PwObjectImpl object to the partial plan
   *
   * @param id - <code>Integer</code> - the Id of the PwObjectImpl
   * @param object - <code>PwObjectImpl</code>
   */

  public void addObject(final Integer id, final PwObjectImpl object) {
    if(objectMap.containsKey(id)) {
      return;
    }
    objectMap.put(id, object);
  }

  /**
   * <code>addConstraint</code> - add a PwConstraintImpl to the partial plan
   *
   * @param id - <code>Integer</code> - 
   * @param constraint - <code>PwConstraintImpl</code> - 
   */
  public void addConstraint( final Integer id, final PwConstraintImpl constraint) {
    if(constraintMap.containsKey(id)) {
      return;
    }
    constraintMap.put( id, constraint);
  }

  /**
   * <code>addSlot</code>
   *
   * @param id - <code>Integer</code> - 
   * @param slot - <code>PwSlotImpl</code> - 
   */
  public void addSlot( final Integer id, final PwSlotImpl slot) {
    if(slotMap.containsKey(id)) {
      return;
    }
    slotMap.put( id, slot);
  }

  /**
   * <code>addTimeline</code>
   *
   * @param id - <code>Integer</code> - 
   * @param timeline - <code>PwTimelineImpl</code> - 
   */
  public void addTimeline( final Integer id, final PwTimelineImpl timeline) {
    if(!objectMap.containsKey(id)) {
      objectMap.put(id, timeline);
    }
    if(!timelineMap.containsKey(id)) {
      timelineMap.put( id, timeline);
    }
  }

  /**
   * <code>addToken</code>
   *
   * @param id - <code>Integer</code> - 
   * @param token - <code>PwTokenImpl</code> - 
   */
  public void addToken( final Integer id, final PwTokenImpl token) {
    if(tokenMap.containsKey(id)) {
      return;
    }
    tokenMap.put( id, token);
  }

  /**
   * <code>addRuleInstance</code>
   *
   * @param id - <code>Integer</code> - 
   * @param ruleInstance - <code>PwRuleInstanceImpl</code> - 
   */
  public void addRuleInstance( final Integer id, final PwRuleInstanceImpl ruleInstance) {
    if(ruleInstanceMap.containsKey(id)) {
      return;
    }
    ruleInstanceMap.put( id, ruleInstance);
  }

  /**
   * <code>addVariable</code>
   *
   * @param id - <code>Integer</code> - 
   * @param variable - <code>PwVariableImpl</code> - 
   */
  public void addVariable( final Integer id, final PwVariableImpl variable) {
    if(variableMap.containsKey(id)) {
      return;
    }
    variableMap.put( id, variable);
  }

  public void addResource(final Integer id, final PwResourceImpl resource) {
    if(!objectMap.containsKey(id)) {
      objectMap.put(id, resource);
    }
    if(!resourceMap.containsKey(id)) {
      resourceMap.put(id, resource);
    }
  }

  public void addResourceInstant(final Integer id, final PwResourceInstantImpl instant) {
    if(!instantMap.containsKey(id)) {
      instantMap.put(id, instant);
    }
  }

  public void addResourceTransaction(final Integer id, final PwResourceTransactionImpl trans) {
    if(!tokenMap.containsKey(id)) {
      tokenMap.put(id, trans);
    }
    if(!resTransactionMap.containsKey(id)) {
      resTransactionMap.put(id, trans);
    }
  }

  /**
   * <code>cleanConstraints</code> - remove constraints with nonexistant variables
   *                    done because not every variable is on a token, and is not easily
   *                    accessable from Europa
   */
  private void cleanConstraints() {
    Iterator constraintIterator = constraintMap.values().iterator();
    while(constraintIterator.hasNext()) {
      boolean removedConstraint = false;
      PwConstraintImpl constraint = (PwConstraintImpl) constraintIterator.next();
      ListIterator variableIdIterator = constraint.getVariableIdList().listIterator();
      while(variableIdIterator.hasNext()) {
        Integer variableId = (Integer) variableIdIterator.next();
        if(!variableMap.containsKey(variableId)) {
          constraintIterator.remove();
          removedConstraint = true;
          break;
        }
      }
      if(removedConstraint) {
        variableIdIterator = constraint.getVariableIdList().listIterator();
        while(variableIdIterator.hasNext()) {
          PwVariableImpl variable = (PwVariableImpl) variableMap.get(variableIdIterator.next());
          if(variable != null) {
            variable.removeConstraint(constraint.getId());
          }
        }
      }
    }
  }
  
  /**
   * <code>checkPlan</code> - verify that the PwPartialPlan structure is internally consistent.
   */

  public boolean checkPlan() {
    System.err.println("Checking plan internal consistency.");
    //for debug make sure they all run even if a prior one fails
    boolean retval = checkRuleInstances();
    retval = checkConstraints() && retval;
    retval = checkTokens() && retval;
    retval = checkVariables() && retval;
    //retval = checkDecisions();
    //boolean retval = checkRuleInstances() && checkConstraints() && checkTokens() && checkVariables();
    System.err.println("Done checking plan.");
    return retval;
  }

  private boolean checkVariables() {
    //System.err.println("In checkVariables");
    Iterator variableIterator = variableMap.values().iterator();
    boolean retval = true;
    while(variableIterator.hasNext()) {
      PwVariableImpl variable = (PwVariableImpl) variableIterator.next();
      if(variable.getParent() == null) {
        System.err.println("Variable " + variable.getId() + " has no parent.");
        retval = false;
      }
      if(variable.getDomain() == null) {
        System.err.println("Variable " + variable.getId() + " has null domain.");
        retval = false;
      }
      if(variable.getType().equals(DbConstants.START_VAR) ||
         variable.getType().equals(DbConstants.END_VAR) ||
         variable.getType().equals(DbConstants.DURATION_VAR) ||
         variable.getType().equals(DbConstants.OBJECT_VAR) ||
         variable.getType().equals(DbConstants.STATE_VAR) ||
         variable.getType().equals(DbConstants.MEMBER_VAR) ||
         variable.getType().equals(DbConstants.PARAMETER_VAR) ||
         variable.getType().equals(DbConstants.RULE_VAR)) {
        if(variable.getParameterNameList().size() != 0 &&
           variable.getParameterNameList().size() != 1) {
          System.err.println(variable.getType() + " " + variable.getId() +
                             " has parameter list of size " + 
                             variable.getParameterNameList().size());
          System.err.println(variable.getParameterNameList());
          retval = false;
        }
      }
      else {
        System.err.println("Variable " + variable.getId() + " has unsupported variable type " +
                           variable.getType());
        retval = false;
      }
    }
    return retval;
  }

  /**
   * <code>checkRuleInstances</code> - verify that all tokens related by rule instances exist
   *                                   that the rule for this rule instance exists
   *                                   and that any rule variables exist and are the correct type
   */
  private boolean checkRuleInstances() {
    //System.err.println("In checkRuleInstances");
    Iterator ruleInstanceIterator = ruleInstanceMap.values().iterator();
    boolean retval = true;
    while(ruleInstanceIterator.hasNext()) {
      PwRuleInstanceImpl ruleInstance = (PwRuleInstanceImpl) ruleInstanceIterator.next();
      if(!tokenMap.containsKey(ruleInstance.getMasterId())) {
        System.err.println("Rule instance " + ruleInstance.getId() + " has nonexistant master token " +
                           ruleInstance.getMasterId());
        retval = false;
      }
      Integer ruleId = ruleInstance.getRuleId();
      if (getRule(ruleId) == null) {
        System.err.println("Rule instance " + ruleInstance.getId() + " has nonexistant rule " + ruleId);
        retval = false;
      }
      List slaves = ruleInstance.getSlaveIdsList();
      Iterator sidIterator = slaves.iterator();
      while (sidIterator.hasNext()) {
        Integer slaveId = (Integer)sidIterator.next();
        if(!tokenMap.containsKey(slaveId)) {
          System.err.println("Rule instance " + ruleInstance.getId() + " has nonexistant slave token " +
                             slaveId);
          retval = false;
        }
      }
      List ruleVarList = ruleInstance.getRuleVarIdList();
      Iterator ruleVarIterator = ruleVarList.iterator();
      int ruleVarIndex = 0;
      while(ruleVarIterator.hasNext()) {
        Integer ruleVarId = (Integer)ruleVarIterator.next();
        if(!variableMap.containsKey(ruleVarId)) {
          System.err.println("Rule instance " + ruleInstance.getId() + " has nonexistant rule variable " +
                             ruleVarId + " at position " + ruleVarIndex);
          retval = false;
        }
        else {
          PwVariableImpl ruleVar = (PwVariableImpl) getVariable(ruleVarId);
          if(!(ruleVar.getType().equals(DbConstants.RULE_VAR))) {
            System.err.println("Rule instance " + ruleInstance.getId() + " has rule variable " +
                               ruleVarId + " that is not type RULE_VAR");
            retval = false;
          }
          if(ruleVar.getParameterNameList().size() != 0 &&
             ruleVar.getParameterNameList().size() != 1) {
            System.err.println(ruleVar.getType() + " " + ruleVar.getId() +
                               " has parameter list of size " + 
                               ruleVar.getParameterNameList().size());
            System.err.println(ruleVar.getParameterNameList());
            retval = false;
          }
        }
        ruleVarIndex++;
      }
    }
    return retval;
  }

  /**
   * <code>checkDecisions</code> - verify that current decision exists among open decisions
   *                               for this step and that entityId maps to an existing entity
   *                               of type decisionType.
   */
//   private boolean checkDecisions() {

//     //System.err.println("In checkDecisions");
//     Integer currentDecisionId = SQLDB.queryCurrentDecisionIdForStep(id);
//     boolean retval = true;
//     boolean foundCurrentDecision = false;

//     List openDecisionsList = SQLDB.queryOpenDecisionsForStep(id, this);
//     Iterator openDecIterator = openDecisionsList.iterator();
//     while(openDecIterator.hasNext()) {
//       PwDecisionImpl decision = (PwDecisionImpl) openDecIterator.next();
//       if (decision.getId().equals(currentDecisionId)) {
//          foundCurrentDecision = true;
//       }
//       // check EntityId and DecisionType
//       int type = decision.getType();
//       Integer entityId = decision.getEntityId();
//       if (type == DbConstants.D_OBJECT) {
//         if (getToken(entityId) == null) {
//           System.err.println("Decision " + decision.getId() + " has nonexistant object " +
//                              "for EntityId " + entityId);
//           retval = false;
//         }
//       } else if (type == DbConstants.D_TOKEN) {
//         if (getToken(entityId) == null) {
//           System.err.println("Decision " + decision.getId() + " has nonexistant token " +
//                              "for EntityId " + entityId);
//           retval = false;
//         }
//       } else if (type == DbConstants.D_VARIABLE) {
//         if (getVariable(entityId) == null) {
//           System.err.println("Decision " + decision.getId() + " has nonexistant variable " +
//                              "for EntityId " + entityId);
//           retval = false;
//         }
//       }
//     }//end while

//     if (!foundCurrentDecision) {
//       System.err.println("Current Decision " + currentDecisionId + 
//                          " not found in open decision list for step " + id);
//       retval = false;
//     }
//     return retval;
//   }

  /**
   * <code>checkConstraints</code> - verify that all constrained variables exist
   */
  private boolean checkConstraints() {
    //System.err.println("In checkConstraints");
    Iterator constraintIterator = constraintMap.values().iterator();
    boolean retval = true;
    while(constraintIterator.hasNext()) {
      PwConstraintImpl constraint = (PwConstraintImpl) constraintIterator.next();
      ListIterator variableIdIterator = constraint.getVariableIdList().listIterator();
      int variableIndex = 0;
      while(variableIdIterator.hasNext()) {
        Integer variableId = (Integer) variableIdIterator.next();
        if(!variableMap.containsKey(variableId)) {
          System.err.println("Constraint " + constraint.getId() + " has nonexistant variable " +
                             variableId + " at position " + variableIndex);
          retval = false;
        }
        variableIndex++;
      }
    }
    return retval;
  }
  /**
   * <code>checkTokens</code> - verify that all tokens have predicates, the correct number of parameter
   *                            variables, that all variables exist and are of the right type, that 
   *                            slotted tokens have valid slots, timelines, and objects, as well as
   *                            checking that the tokens have a valid rule instance
   *                            in only the relations they have.
   */
  private boolean checkTokens() {
    //System.err.println("In checkTokens");
    Iterator tokenIterator = tokenMap.values().iterator();
    boolean retval = true;
    int zeroRuleInstanceIdCount = 0;
    while(tokenIterator.hasNext()) {
      PwTokenImpl token = (PwTokenImpl) tokenIterator.next();
      if(token.getPredicateName() == null) {
        System.err.println("Token " + token.getId() + " has null predicate name.");
        retval = false;
      }
      if(token.isSlotted()) {
        //if(token.getObjectId() == null) {
        //  System.err.println("Slotted token " + token.getId() + " has null objectId.");
        //  retval = false;
        //}
        //else if(!objectMap.containsKey(token.getObjectId())) {
        //  System.err.println("Slotted token " + token.getId() + " has nonexistant objectId " + 
        //                     token.getObjectId());
        //  retval = false;
        //}
        if(token.getParentId() == null) {
          System.err.println("Slotted token " + token.getId() + " has null timelineId.");
          retval = false;
        }
        else if(!timelineMap.containsKey(token.getParentId())) {
          System.err.println("Slotted token " + token.getId() + " has nonexistant timelineId " +
                             token.getParentId());
          retval = false;
        }
        if(token.getSlotId() == null) {
          System.err.println("Slotted token " + token.getId() + " has null slotId.");
          retval = false;
        }
        else if(!slotMap.containsKey(token.getSlotId())) {
          System.err.println("Slotted token " + token.getId() + " has nonexistant slotId " + 
                             token.getSlotId());
          retval = false;
        }
      }
      Integer ruleInstanceId = token.getRuleInstanceId();
      if (ruleInstanceId.intValue() == 0)  {
         //only the initial state token is allowed to not have a rule instance, in this case the
         //id will be zero. keep a count of these. Only one will be allowed.
         zeroRuleInstanceIdCount++;
      }
      else {
        if(getRuleInstance(ruleInstanceId) == null) {
          System.err.println("Token " + token.getId() + " has null rule instance for RuleInstanceId " +
                             ruleInstanceId);
          retval = false;
        }
      }
      retval = retval && checkTokenVars(token) && checkTokenParamVars(token);
    }
    if (zeroRuleInstanceIdCount > 1) {
      // check commented out until inconsistancy is resolved -- pdaley
      //System.err.println(zeroRuleInstanceIdCount + " tokens found with a null RuleInstanceId");
      //retval = false;
    }
    return retval;
  }
  /**
   * <code>checkTokenVars</code> - verify that all variables exist and are of the proper type
   */
  private boolean checkTokenVars(final PwTokenImpl token) {
    PwVariableImpl startVar = (PwVariableImpl) token.getStartVariable();
    PwVariableImpl endVar = (PwVariableImpl) token.getEndVariable();
    PwVariableImpl durationVar = (PwVariableImpl) token.getDurationVariable();
    PwVariableImpl objectVar = (PwVariableImpl) token.getObjectVariable();
    PwVariableImpl stateVar = (PwVariableImpl) token.getStateVariable();
    boolean retval = true;
    if(startVar == null) {
      System.err.println("Token " + token.getId() + " has null start variable.");
      retval = false;
    }
    else {
      if(!startVar.getType().equals(DbConstants.START_VAR)) {
        System.err.println("Token " + token.getId() + "'s start variable " + startVar.getId() + 
                           " isn't.");
        retval = false;
      }
      else {
        retval = retval && checkVariable(startVar);
      }
    }
    if(endVar == null) {
      System.err.println("Token " + token.getId() + " has null end variable.");
      retval = false;
    }
    else {
      //PwResourceTransaction tokens have no end variables
      if (token instanceof PwResourceTransaction == false) {
        if(!endVar.getType().equals(DbConstants.END_VAR)) {
          System.err.println("Token " + token.getId() + "'s end variable " + endVar.getId() + " isn't.");
          retval = false;
        }
        else {
          retval = retval && checkVariable(endVar);
        }
      }
    }
    if(durationVar == null) {
      System.err.println("Token " + token.getId() + " has null duration variable.");
      retval = false;
    }
    else {
      if(!durationVar.getType().equals(DbConstants.DURATION_VAR)) {
        System.err.println("Token " + token.getId() + "'s duration variable " + durationVar.getId() +
                           " isn't.");
        retval = false;
      }
      else {
        retval = retval && checkVariable(durationVar);
      }
    }
    if(objectVar == null) {
      System.err.println("Token " + token.getId() + " has null object variable.");
      retval = false;
    }
    else {
      if(!objectVar.getType().equals(DbConstants.OBJECT_VAR)) {
        System.err.println("Token " + token.getId() + "'s object variable " + objectVar.getId() + 
                           " isn't.");
        retval = false;
      }
      else {
        retval = retval && checkObjectVariable(objectVar, token.isFree());
      }
    }
    if(stateVar == null) {
      System.err.println("Token " + token.getId() + " has null state variable.");
    }
    else {
      if(!stateVar.getType().equals(DbConstants.STATE_VAR)) {
        System.err.println("Token " + token.getId() + "'s reject variable " + stateVar.getId()  + 
                           " isn't.");
        retval = false;
      }
      else {
        retval = retval && checkVariable(stateVar);
      }
    }
    return retval;
  }
  /**
   * <code>checkTokenParamVars</code> - verify that the token has the correct number of parameter 
   *                                    variables
   */
  private boolean checkTokenParamVars(final PwTokenImpl token) {
    List paramVarList = token.getParamVarsList();
    int paramVarSize = paramVarList.size();
    boolean retval = true;
    ListIterator paramVarIterator = paramVarList.listIterator();
    while(paramVarIterator.hasNext()) {
      PwVariableImpl variable = (PwVariableImpl) paramVarIterator.next();
      if(variable == null) {
        System.err.println("Token " + token.getId().toString() + " has null variable.");
        retval = false;
        continue;
      }
      if(!variableMap.containsValue(variable)) {
        System.err.println("Token " + token.getId().toString() + " has parameter variable " + 
                           variable.getId() + " that doesn't exist.");
        retval = false;
      }
      else {
        retval = retval && checkVariable(variable);
      }
    }
    return retval;
  }

  /**
   * <code>checkVariable</code> - verify that a variable is of a valid type, has constraints that exist,
   *                              is in the constraints that is has, and is only in those constraints
   */
  private boolean checkVariable(final PwVariableImpl var) {
    boolean retval = true;
    if(var.getDomain() == null) {
      System.err.println("Variable " + var.getId() + " has no domain.");
      retval = false;
    }
    String type = var.getType();
    boolean isValidType = false;
    for (int i = 0, n = DbConstants.DB_VARIABLE_TYPES.length; i < n; i++) {
      if (DbConstants.DB_VARIABLE_TYPES[i].equals( type)) {
        isValidType = true;
        break;
      }
    }
    if (! isValidType) {
      System.err.println("Variable " + var.getId() + " has invalid type " + type);
      retval = false;
    }
    List varConstraints = var.getConstraintList();
    ListIterator varConstraintIterator = varConstraints.listIterator();
    while(varConstraintIterator.hasNext()) {
      PwConstraintImpl constraint = (PwConstraintImpl) varConstraintIterator.next();
      if(constraint == null) {
        System.err.println("Variable " + var.getId() + " has a null constraint.");
        retval = false;
        continue;
      }
      if(!constraint.getVariablesList().contains(var)) {
        System.err.println("Variable " + var.getId() + " has constraint " + constraint.getId() + 
                           " but is not in the constraint.");
        retval = false;
      }
      if(!constraintMap.containsKey(constraint.getId())) {
        System.err.println("Variable " + var.getId() + " has nonexistant constraint " + 
                           constraint.getId());
        retval = false;
      }
    }
    Iterator constraintIterator = constraintMap.values().iterator();
    while(constraintIterator.hasNext()) {
      PwConstraintImpl constraint = (PwConstraintImpl) constraintIterator.next();
      if(constraint.getVariablesList().contains(var) && !varConstraints.contains(constraint)) {
        System.err.println("Variable " + var.getId() + " is in constraint " + constraint.getId() + 
                           " but does not have it.");
        retval = false;
      }
    }
    return retval;
  }
  /**
   * <code>checkObjectVariable</code> - check validity of object variable, as well as that there is only
   *                                  - one object in the domain if the token is slotted
   */
  private boolean checkObjectVariable(final PwVariableImpl objectVar, final boolean isFreeToken) {
    boolean retval = true && checkVariable(objectVar);
    //checkVariable(objectVar);
    PwEnumeratedDomainImpl domain = (PwEnumeratedDomainImpl) objectVar.getDomain();

// check commented out until inconsistancy is resolveda -- pdaley
//    if(!isFreeToken) {
//      if(domain.getEnumeration().size() > 1) {
//        System.err.println("Slotted token has object variable " + objectVar.getId() + 
//                           " with multiple objects.");
//        retval = false;
//      }
//    }
    return retval;
  }

  // implement ViewableObject

  public void setContentSpec(final List spec) {
    if (contentSpec != null) {
      contentSpec.clear();
      contentSpec.addAll(spec);
    }
  }
  
  public List getContentSpec() {
    return new ArrayList(contentSpec);
  }

  /**
   * <code>setName</code> - sequenceDir/stepDir
   *      PlanWorks.renderPartialPlanView invokes this method 
   *
   * @param name - <code>String</code> - 
   */
  public void setName( final String name) {
    this.seqName = name;
  }


  /**
   * <code>getName</code> - sequenceDir/stepDir
   *
   * @return - <code>String</code> - 
   */
  public String getName() {
    return seqName;
  }

  // end implement ViewableObject

  /**
   * <code>getPartialPlanName</code> - stepDir
   *
   * @return - <code>String</code> - 
   */
  public String getPartialPlanName() {
    return this.seqName.substring( this.seqName.lastIndexOf
                                   ( System.getProperty( "file.separator")) + 1);
  }

  /**
   * <code>getPlanDBSize</code> - sum of hash map sizes of all plan objects
   *
   * @return - <code>int</code> - 
   */
  public int getPlanDBSize() {
    return (objectMap.keySet().size() + timelineMap.keySet().size() +
            slotMap.keySet().size() + tokenMap.keySet().size() +
            constraintMap.keySet().size() + ruleInstanceMap.keySet().size() + 
            variableMap.keySet().size());
  } // end getDataBaseSize

  /**
   * <code>getStepNumber</code> - strip "step" prefix off stepDir and create int
   *
   * @return - <code>int</code> - 
   */
  public int getStepNumber() {
    String stepDir =
      this.seqName.substring( this.seqName.lastIndexOf( System.getProperty("file.separator")) +
                              1);
    return Integer.parseInt( stepDir.substring( 4)); // strip off step
  } // end getStepNumber

  /**
   * <code>getSequence</code>
   *
   * @return - <code>PwPlanningSequence</code> - 
   */
  public PwPlanningSequence getSequence() {
    return sequence;
  }
  
  /**
   * <code>getSequenceUrl</code>
   *
   * @return - <code>String</code> - 
   */
  public String getSequenceUrl() {
    return url.substring( 0, url.lastIndexOf( System.getProperty( "file.separator")));
  }
  
  public Integer getObjectIdByName(final String name) {
    Iterator objectIterator = objectMap.values().iterator();
    while(objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      if(object.getName().equals(name)) {
        return object.getId();
      }
    }
    return null;
  }

  /**
   * <code>getMasterTokenId</code>
   *
   * @param tokenId - <code>Integer</code> - 
   * @return - <code>Integer</code> - 
   */
  public Integer getMasterTokenId(final Integer tokenId) {

    // 1) get token for this tokenId
    // 2) get the rule instance id for this token
    // 3) get the rule instance for this ruleInstanceId
    // 4) get the master token of the rule instance

//     System.err.println( "\ngetMasterTokenId tokenId " + tokenId);
//     List ruleInstList = new ArrayList( ruleInstanceMap.keySet());
//     Iterator ruleInstItr = ruleInstList.iterator();
//     while (ruleInstItr.hasNext()) {
//       System.err.println( "  ruleInstKey " + (Integer) ruleInstItr.next());
//     }

    PwToken token =  getToken(tokenId);
    if (token == null) {
      System.err.println("tokenId " + tokenId + " has nonexistant Token");
    } else {

//       System.err.println( "RuleInstanceId " + getToken(tokenId).getRuleInstanceId());

      Integer ruleInstanceId = token.getRuleInstanceId();
      PwRuleInstance ruleInstance = getRuleInstance(ruleInstanceId); 
      if (ruleInstance != null) {

//         System.err.println( "RuleInstanceMasterId " +
//                             getRuleInstance( getToken(tokenId).getRuleInstanceId()).getMasterId());

        return ruleInstance.getMasterId();
      }
    }
    return null;
  }

  /**
   * <code>getSlaveTokenIds</code>
   *
   * @param tokenId - <code>Integer</code> - 
   * @return - <code>List</code> - 
   */
  public List getSlaveTokenIds(final Integer masterTokenId) {
   
    List retval = new ArrayList();
    List childRuleInstanceIdList = (List) tokenChildRuleInstIdMap.get(masterTokenId);
    if(childRuleInstanceIdList == null) {
      //System.err.println("Token " + masterTokenId + " has nonexistant rule instance list");
      return retval;
    }

    //System.err.println("Token " + masterTokenId + " has a rule instance list");
    Iterator cridIterator = childRuleInstanceIdList.iterator();
    while (cridIterator.hasNext()) {
      Integer ruleInstanceId = (Integer)cridIterator.next();
      PwRuleInstance ruleInstance = getRuleInstance(ruleInstanceId); 

      //Next 7 lines are for command window trace and can be removed
      //System.err.println("MasterTokenId " + masterTokenId + " RuleInstanceId " + ruleInstanceId);
      //List slaves = ruleInstance.getSlaveIdsList();
      //Iterator sidIterator = slaves.iterator();
      //while (sidIterator.hasNext()) {
      //  Integer slaveId = (Integer)sidIterator.next();
      //  System.err.println("SlaveId " + slaveId);
      //}

      retval.addAll(ruleInstance.getSlaveIdsList());
    }    
    return retval;
  }


  public String toOutputString() {
    StringBuffer retval = new StringBuffer(name);
    retval.append("\t").append(id).append("\t").append(model).append("\t");
    retval.append(sequence.getId()).append("\n");
    return retval.toString();
  }

  /**
   * <code>getRule</code>
   *
   * @param rId - <code>Integer</code> - 
   * @return - <code>PwRule</code> - 
   */
  public PwRule getRule(Integer rId) {
    return sequence.getRule(rId);
  }

  /**
   * <code>getVariableParentName</code>
   *
   * @param parentId - <code>Integer</code> - 
   * @return - <code>String</code> - 
   */
  public String getVariableParentName( final Integer parentId) {
    Object parent = null;
    parent = getToken( parentId);
    if (parent != null) {
      return ((PwToken) parent).getPredicateName();
    }
    parent = getObject( parentId);
    if (parent != null) {
      return ((PwObject) parent).getName();
    }
    parent = getResource( parentId);
    if (parent != null) {
      return ((PwResource) parent).getName();
    }
    parent = getTimeline( parentId);
    if (parent != null) {
      return ((PwTimeline) parent).getName();
    }
    return "<not found>";
  } // end getVariableParentName

  private void progressMonitorThread( String title, int minValue, int maxValue,
                                            String note) {
    Thread thread = new ProgressMonitorThread( title, minValue, maxValue, note);
    thread.setPriority(Thread.MAX_PRIORITY);
    thread.start();
  }


  public class ProgressMonitorThread extends Thread {

    private String title;
    private int minValue;
    private int maxValue;
    private String note;

    public ProgressMonitorThread( String title, int minValue, int maxValue, String note) {
      this.title = title;
      this.minValue = minValue * ViewConstants.MONITOR_MIN_MAX_SCALING;
      this.maxValue = maxValue * ViewConstants.MONITOR_MIN_MAX_SCALING;
      this.note = note;
    }  // end constructor

    public void run() {
      isProgressMonitorCancel = false;
      progressMonitor = new ProgressMonitor( PlanWorks.getPlanWorks(), title, note,
                                             minValue, maxValue);
      progressMonitor.setMillisToDecideToPopup( 0);
      progressMonitor.setMillisToPopup( 0);
      // these two must be set to 0 before calling setProgress, which puts up the dialog
      progressMonitor.setProgress( 0);
    
      while (! isProgressMonitorCancel) {
        try {
          Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL * 2);
        }
        catch (InterruptedException ie) {}
      }
      progressMonitor.close();
      progressMonitor = null;
    } // end run
      
  } // end class ProgressMonitorThread


  private boolean progressMonitorWait() {
    int numCycles = ViewConstants.WAIT_NUM_CYCLES;
    while ((progressMonitor == null) && numCycles != 0) {
      try {
        Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL);
      }
      catch (InterruptedException ie) {}
      numCycles--;
      // System.err.println( "progressMonitorWait numCycles " + numCycles);
    }
    if (numCycles == 0) {
      System.err.println( "progressMonitorWait failed after " +
                          (ViewConstants.WAIT_INTERVAL * ViewConstants.WAIT_NUM_CYCLES) +
                          " for PwPartialPlanImpl");
    }
    return numCycles != 0;
  } // end progressMonitorWait

  //boolean isPathDebug = true;
  boolean isPathDebug = false;

  public List getPath(final Integer sKey, final Integer eKey, final List classes) {
    return getPath(sKey, eKey, classes, Integer.MAX_VALUE);
  }

  public List getPath(final Integer sKey, final Integer eKey, final List classes, 
                      final int maxLength) {
    return getPath(sKey, eKey, classes, ViewConstants.ALL_LINK_TYPES, maxLength);
  }

  public List getPath(final Integer sKey, final Integer eKey, final List classes,
                      final List linkTypes, final int maxLength) {
    if (isPathDebug) {
      System.err.println( "getPath: sKey " + sKey + " eKey " + eKey);
    }
    long t1 = System.currentTimeMillis();
    PwEntity start, end;
    if(sKey == null)
      throw new IllegalArgumentException("Start key can't be null");
    if(eKey == null)
      throw new IllegalArgumentException("End key can't be null");
    if(classes == null || classes.isEmpty())
      throw new IllegalArgumentException("Invalid class list.");
    if(maxLength < 1)
      throw new IllegalArgumentException("Max length must be >= 1");
    if((start = getEntity(sKey)) == null)
      throw new IllegalArgumentException("Start key " + sKey + " isn't a valid entity id.");
    if((end = getEntity(eKey)) == null)
      throw new IllegalArgumentException("End key " + eKey + " isn't a valid entity id.");
    boolean foundClass = false;
    for(Iterator it = classes.iterator(); it.hasNext();) {
      Class temp = (Class) it.next();
      if((foundClass = temp.isInstance(end)))
        break;
    }
    if(!foundClass)
      throw new IllegalArgumentException("Valid class list must contain end type '" + 
                                         end.getClass() + "'");
    LinkedList path = new LinkedList();
    if(pathExists(start, eKey, classes, linkTypes))
      for(int i = 1; i < maxLength; i++)
        if(getPathRecurse(start, eKey, classes, linkTypes, path, 0, i))
          break;
    System.err.println("Finding path took " + (System.currentTimeMillis() - t1) + " msecs.");
    return path;
  }

  public boolean pathExists(final PwEntity start, final Integer end, final List classes) {
    return pathExists(start, end, classes, ViewConstants.ALL_LINK_TYPES);
  }

  public boolean pathExists(final PwEntity start, final Integer end, final List classes,
                            final List linkTypes) {
    long t1 = System.currentTimeMillis();
    if (isPathDebug) {
      System.err.println("pathExists start " + start.getClass().getName());
      System.err.println( "   id " + start.getId());
   }
    LinkedList component = new LinkedList();
    buildConnectedComponent(start, classes, linkTypes, component);
    System.err.println("Determining path existence took " +
                       (System.currentTimeMillis() - t1) + " msecs.");
    return component.contains(end);
  }

  private void buildConnectedComponent(final PwEntity ent, final List classes,
                                       final List linkTypes, LinkedList component) {
    if (isPathDebug) {
      System.err.println( "buildConnectedComponent " + ent.getClass().getName());
      System.err.println( "   id " + ent.getId());
    }
    if(component.contains(ent.getId()))
      return;
    component.addLast(ent.getId());
    for(Iterator it = ent.getNeighbors(classes, linkTypes).iterator(); it.hasNext();) {
      PwEntity entity = (PwEntity)it.next();
      if (isPathDebug) {
        System.err.println( "buildConnectedComponent expand " + entity.getClass().getName());
      }
      buildConnectedComponent( entity, classes, linkTypes, component);
    }
  }

  private boolean getPathRecurse(final PwEntity current, final Integer eKey,
                                 final List classes, final List linkTypes, LinkedList path, 
                                 int currentDepth, final int finalDepth) {
    if(path.contains(current.getId()))
      return false;
    if(currentDepth == finalDepth)
      return false;
    path.addLast(current.getId());
    currentDepth++;
    if(current.getId().equals(eKey))
      return true;
    for(Iterator it = current.getNeighbors(classes, linkTypes).iterator(); it.hasNext();) {
      if(getPathRecurse((PwEntity)it.next(), eKey, classes, linkTypes, path, currentDepth,
                        finalDepth))
        return true;
    }
    path.removeLast();
    currentDepth--;
    return false;
  }

} // end class PwPartialPlanImpl
