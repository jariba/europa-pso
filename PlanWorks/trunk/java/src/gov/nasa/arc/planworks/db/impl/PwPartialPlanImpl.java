// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPartialPlanImpl.java,v 1.85 2004-04-30 21:49:38 miatauro Exp $
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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPredicate;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwResourceInstant;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwRule;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwTokenRelation;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.db.util.PwSQLFilenameFilter;
import gov.nasa.arc.planworks.util.BooleanFunctor;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.FunctorFactory;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.UnaryFunctor;
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
  private Integer projectId;
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
  private Map tokenRelationMap; // key = attribute id, value = PwTokenRelationImpl instance
  private Map variableMap; // key = attribute id, value = PwVariableImpl instance
  private List contentSpec;
  private Map tokenMasterSlaveMap; // key = tokenId, value TokenRelations instance
  private Map instantMap; //just add water.  key = tokenId, value = PwResourceInstantImpl instance
  private Map resTransactionMap; //key = transactionId, value = PwResourceTransactionImpl instance

  /**
   * <code>PwPartialPlanImpl</code> - initialize storage structures then call createPartialPlan()
   *
   * @param - <code>url</code> - path to a directory containing plan data
   * @param - <code>planName</code> - the name of the partial plan (usually stepN)
   * @param - <code>sequenceId</code> - the Id of the sequence to which this plan is attached.
   * @exception ResourceNotFoundException if the plan data is invalid
   */
  public PwPartialPlanImpl(final String url, final String planName, 
                           final PwPlanningSequenceImpl sequence) 
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
    tokenRelationMap = new HashMap(); 
    resourceMap = new HashMap();
    resTransactionMap = new HashMap();
    variableMap = new HashMap();
    tokenMasterSlaveMap = new HashMap();
    instantMap = new HashMap();
    this.url = (new StringBuffer(url)).append(System.getProperty("file.separator")).append(planName).toString();
    contentSpec = new ArrayList();
    this.name = planName;
    createPartialPlan();
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
    tokenRelationMap = new HashMap(); 
    resourceMap = new HashMap();
    resTransactionMap = new HashMap();
    variableMap = new HashMap();
    tokenMasterSlaveMap = new HashMap();
    instantMap = new HashMap();
    this.url = (new StringBuffer(url)).append(System.getProperty("file.separator")).
      append(planName).toString();
    contentSpec = new ArrayList();
    this.name = planName;

    id = partialPlanId;
    this.model = model;
  }

  /**
   * <code>createPartialPlan</code> - load plan data into the database and/or construct substructures
   *
   * @param - <code>sequenceId</code> - Id of the sequence to which this plan is attached
   * @exception ResourceNotFoundException if the plan data is invalid
   */

  private void createPartialPlan() throws ResourceNotFoundException {
    long startTimeMSecs = System.currentTimeMillis();
    long loadTime = 0L;
    HashMap existingPartialPlan = null;
    id = MySQLDB.getPartialPlanIdByName(sequence.getId(), name);
    if(id == null) {
      File planDir = new File(sequence.getUrl() + System.getProperty("file.separator") +
                              name);
      loadFiles(planDir);
      MySQLDB.analyzeDatabase();
      id = MySQLDB.getPartialPlanIdByName(sequence.getId(), name);
    }
    MySQLDB.createObjects(this);
    model = MySQLDB.queryPartialPlanModelById(id);
    MySQLDB.createSlotTokenNodesStructure(this, sequence.getId());

    long start2TimeMSecs = System.currentTimeMillis();
    fillElementMaps();
    long stop2TimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... elapsed time: " +
                        (stop2TimeMSecs - start2TimeMSecs) + " msecs.");
    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    cleanConstraints();
    // checkPlan();
  } // end createPartialPlan

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
      MySQLDB.loadFile(planDir.getAbsolutePath().concat(System.getProperty("file.separator")).concat(fileNames[i]), tableName);
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
    MySQLDB.queryConstraints( this);

    MySQLDB.queryTokenRelations( this);
    MySQLDB.queryVariables( this);
    MySQLDB.queryResourceInstants(this);
    CollectionUtils.cInPlaceMap(new TimelineSlotCreator(), objectMap.values());

    initTokenRelationships();
    buildTokenRelationships();
    // printTokenRelationships();

    System.err.println( "Partial Plan: " + url);
    System.err.println( "Ids:");
    System.err.println( "  objects        " + objectMap.keySet().size());
    System.err.println( "  resources      " + resourceMap.keySet().size());
    System.err.println( "  timelines      " + timelineMap.keySet().size());
    System.err.println( "  slots          " + slotMap.keySet().size());
    System.err.println( "  tokens         " + tokenMap.keySet().size());
    System.err.println( "  constraints    " + constraintMap.keySet().size());
    System.err.println( "  tokenRelations " + tokenRelationMap.keySet().size());
    System.err.println( "  variables      " + variableMap.keySet().size());

  } // end fillElementMaps


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
    return new ArrayList(CollectionUtils.cGrep(new FreeTokenFunctor(), tokenMap.values()));
  }

  class SlottedTokenFunctor implements BooleanFunctor {
    public SlottedTokenFunctor(){}
    public boolean func(Object o){return ((PwToken)o).isSlotted();}
  }

  public List getSlottedTokenList() {
    return new ArrayList(CollectionUtils.cGrep(new SlottedTokenFunctor(), tokenMap.values()));
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
   * <code>getTokenRelation</code> - get token relation by id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwTokenRelation</code> - 
   */
  public PwTokenRelation getTokenRelation( final Integer id) {
    return (PwTokenRelation) tokenRelationMap.get(id);
  } // end getTokenRelation


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
   * <code>addTokenRelation</code>
   *
   * @param id - <code>Integer</code> - 
   * @param tokenRelation - <code>PwTokenRelationImpl</code> - 
   */
  public void addTokenRelation( final Integer id, final PwTokenRelationImpl tokenRelation) {
    if(tokenRelationMap.containsKey(id)) {
      return;
    }
    tokenRelationMap.put( id, tokenRelation);
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
    boolean retval = checkRelations() && checkConstraints() && checkTokens() && checkVariables();
    System.err.println("Done checking plan.");
    return retval;
  }

  private boolean checkVariables() {
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
         variable.getType().equals(DbConstants.MEMBER_VAR)) {
        if(variable.getParameterNameList().size() != 0 &&
           variable.getParameterNameList().size() != 1) {
          System.err.println(variable.getType() + " " + variable.getId() +
                             " has parameter list of size " + 
                             variable.getParameterNameList().size());
          System.err.println(variable.getParameterNameList());
          retval = false;
        }
      }
      else if(variable.getType().equals(DbConstants.PARAMETER_VAR)) {
        if(variable.getParameterNameList().size() == 0) {
          System.err.println("Parameter variable " + variable.getId() + " has no parameters.");
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
   * <code>checkRelations</code> - verify that all tokens related by token relations exist
   */
  private boolean checkRelations() {
    Iterator relationIterator = tokenRelationMap.values().iterator();
    boolean retval = true;
    while(relationIterator.hasNext()) {
      PwTokenRelationImpl relation = (PwTokenRelationImpl) relationIterator.next();
      if(!tokenMap.containsKey(relation.getTokenAId())) {
        System.err.println("Token relation " + relation.getId() + " has nonexistant token " +
                           relation.getTokenAId());
        retval = false;
      }
      if(!tokenMap.containsKey(relation.getTokenBId())) {
        System.err.println("Token relation " + relation.getId() + " has nonexistant token " +
                           relation.getTokenBId());
        retval = false;
      }
    }
    return retval;
  }

  /**
   * <code>checkConstraints</code> - verify that all constrained variables exist
   */
  private boolean checkConstraints() {
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
   *                            checking that the tokens have the relations they are in as well as being
   *                            in only the relations they have.
   */
  private boolean checkTokens() {
    Iterator tokenIterator = tokenMap.values().iterator();
    boolean retval = true;
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
      retval = retval && checkTokenVars(token) && checkTokenParamVars(token) && 
        checkTokenRelations(token);
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
      if(!endVar.getType().equals(DbConstants.END_VAR)) {
        System.err.println("Token " + token.getId() + "'s end variable " + endVar.getId() + " isn't.");
        retval = false;
      }
      else {
        retval = retval && checkVariable(endVar);
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
   * <code>checkTokenRelations</code> - ensure that all tokens related exist, are in the relations they
   *                                    have, and are in only the relations they have.
   */
  private boolean checkTokenRelations(final PwTokenImpl token) {
    List relations = token.getTokenRelationIdsList();
    boolean retval = true;
    if(relations.size() == 0) {
      return true;
    }
    ListIterator relationIdIterator = relations.listIterator();
    while(relationIdIterator.hasNext()) {
      Integer tokenRelationId = (Integer) relationIdIterator.next();
      PwTokenRelationImpl tokenRelation = 
        (PwTokenRelationImpl) tokenRelationMap.get(tokenRelationId);
      
      if(tokenRelation == null) {
        System.err.println("Token " + token.getId() + " has nonexistant token relation " +
                           tokenRelationId);
        retval = false;
        continue;
      }
      if(tokenRelation.getTokenAId() == null) {
        System.err.println("Master token id of relation " + tokenRelation.getId() + " is null.");
        retval = false;
        if(tokenRelation.getTokenBId() == null) {
          System.err.println("Slave token id of relation " + tokenRelation.getId() + " is null.");
          continue;
        }
      }
      if(tokenRelation.getTokenBId() == null) {
        System.err.println("Slave token id of relation " + tokenRelation.getId() + " is null.");
        retval = false;
        continue;
      }
      if(// !tokenRelation.getTokenAId().equals(token.getId()) && 
         !tokenRelation.getTokenBId().equals(token.getId())) {
        System.err.println("Token " + token.getId() + " has relation " + tokenRelation.getId() + 
                           " but isn't in it.");
        retval = false;
      }
      if(tokenMap.get(tokenRelation.getTokenAId()) == null) {
        System.err.println("Master token " + tokenRelation.getTokenAId() + " doesn't exist.");
        retval = false;
      }
      if(tokenMap.get(tokenRelation.getTokenBId()) == null) {
        System.err.println("Slave token " + tokenRelation.getTokenBId() + " doesn't exist.");
        retval = false;
      }
    }
    Iterator tokenRelationIterator = tokenRelationMap.values().iterator();
    while(tokenRelationIterator.hasNext()) {
      PwTokenRelationImpl tokenRelation = (PwTokenRelationImpl) tokenRelationIterator.next();
      if(// (tokenRelation.getTokenAId().equals(token.getId()) || 
          tokenRelation.getTokenBId().equals(token.getId()) && !relations.contains(tokenRelation.getId())) {
        System.err.println("Token " + token.getId() + " is in relation " + tokenRelation.getId() +
                           " but doesn't have it.");
        retval = false;
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
    if(!isFreeToken) {
      if(domain.getEnumeration().size() > 1) {
        System.err.println("Slotted token has object variable " + objectVar.getId() + 
                           " with multiple objects.");
        retval = false;
      }
    }
    return retval;
  }

  // implement ViewableObject

  public void setContentSpec(final List spec) {
    contentSpec.clear();
    contentSpec.addAll(spec);
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
            constraintMap.keySet().size() + tokenRelationMap.keySet().size() + 
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


  class TokenRelations {

    private PwToken token;
    private Integer masterTokenId; 
    private List slaveTokenIds; // element Integer

    public TokenRelations( final PwToken token) {
      this.token = token;
      masterTokenId = null;
      slaveTokenIds = new ArrayList();
    } // end constructor

    public Integer getMasterTokenId () {
      return masterTokenId;
    }

    public void setMasterTokenId(final  Integer id) {
      if (masterTokenId != null) {
        System.err.println( "PwPartialPlanImpl.setMasterTokenId conflict: oldValue " +
                            masterTokenId.toString() + " newValue " + id.toString());
        System.exit( -1);
      }
      masterTokenId = id;
    }

    public List getSlaveTokenIds () {
      return slaveTokenIds;
    }

    public void addSlaveTokenId( final Integer id) {
      slaveTokenIds.add( id);
    }

    public String toString() {
      StringBuffer buffer = new StringBuffer( "tokenId: " +
                                              String.valueOf( token.getId()));
      buffer.append( "\n  masterTokenId: " + masterTokenId);
      buffer.append( "\n  slaveTokenIds: " + slaveTokenIds);
      return buffer.toString();
    }

  } // end class TokenRelations

  private void initTokenRelationships() {
    List tokenKeyList = new ArrayList( tokenMap.keySet());
    Iterator tokenKeyItr = tokenKeyList.iterator();
    while (tokenKeyItr.hasNext()) {
      PwToken token = (PwToken) tokenMap.get( tokenKeyItr.next());
      tokenMasterSlaveMap.put( token.getId(), new TokenRelations( token));
    }
  } // end initTokenRelationships

  private void buildTokenRelationships() {
    // process each token relation only once
    List tokenKeyList = new ArrayList( tokenMap.keySet());
    Iterator tokenKeyItr = tokenKeyList.iterator();
    while (tokenKeyItr.hasNext()) {
      PwToken token = (PwToken) tokenMap.get( tokenKeyItr.next());
      // System.err.println( "\nTOKENID " + token.getId());
      if (token.getTokenRelationIdsList().size() != 0) {
        Integer tokenId = token.getId();
        TokenRelations tokenRelations =
          (TokenRelations) tokenMasterSlaveMap.get( tokenId);
        Iterator tokenRelationIdIterator = token.getTokenRelationIdsList().iterator();
        while (tokenRelationIdIterator.hasNext()) {
          PwTokenRelation tokenRelation =
            getTokenRelation( (Integer) tokenRelationIdIterator.next());
          if (tokenRelation != null) {
            // buildTokenParentChildRelationships printout is complete with
            // this commented out -- same links are drawn
            //                 Integer id = tokenRelation.getId();
            //                 if (tokenRelationIds.indexOf( id) == -1) {
            //                   tokenRelationIds.add( id);
            Integer masterTokenId = tokenRelation.getTokenAId();
            Integer slaveTokenId = tokenRelation.getTokenBId();
            // System.err.println( "tokenId " + tokenId + " masterTokenId " + masterTokenId +
            //                     " slaveTokenId " + slaveTokenId);
            // masterTokenIds do not have tokenRelations - this does nothing
            if (masterTokenId.equals( tokenId)) {
              tokenRelations.addSlaveTokenId( slaveTokenId);
            }
            if (slaveTokenId.equals( tokenId)) {
              tokenRelations.setMasterTokenId( masterTokenId);
              TokenRelations masterTokenRelations =
                (TokenRelations) tokenMasterSlaveMap.get( masterTokenId);
              masterTokenRelations.addSlaveTokenId( slaveTokenId);
              tokenMasterSlaveMap.put( masterTokenId, masterTokenRelations);
            }
            //                 }
          }
        }
        tokenMasterSlaveMap.put( tokenId, tokenRelations);
      }
    }
  } // end buildTokenRelationships

  private void printTokenRelationships() {
    Iterator relationsItr = tokenMasterSlaveMap.values().iterator();
    while (relationsItr.hasNext()) {
      TokenRelations relation = (TokenRelations) relationsItr.next();
      System.err.println( relation.toString());
    }
  } // end printTokenRelationships

  /**
   * <code>getMasterTokenId</code>
   *
   * @param tokenId - <code>Integer</code> - 
   * @return - <code>Integer</code> - 
   */
  public Integer getMasterTokenId( final Integer tokenId) {
    return ((TokenRelations) tokenMasterSlaveMap.get( tokenId)).getMasterTokenId();
  }

  /**
   * <code>getSlaveTokenIds</code>
   *
   * @param tokenId - <code>Integer</code> - 
   * @return - <code>List</code> - of Integer
   */
  public List getSlaveTokenIds( final Integer tokenId) {
    return ((TokenRelations) tokenMasterSlaveMap.get( tokenId)).getSlaveTokenIds();
  }

  public String toOutputString() {
    StringBuffer retval = new StringBuffer(name);
    retval.append("\t").append(id).append("\t").append(model).append("\t");
    retval.append(sequence.getId()).append("\n");
    return retval.toString();
  }

  public PwRule getRule(Integer rId) {
    return sequence.getRule(rId);
  }
} // end class PwPartialPlanImpl
