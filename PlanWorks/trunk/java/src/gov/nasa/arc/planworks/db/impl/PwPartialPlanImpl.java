// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPartialPlanImpl.java,v 1.38 2003-09-02 21:49:16 taylor Exp $
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwParameter;
import gov.nasa.arc.planworks.db.PwPredicate;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwTokenRelation;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.db.util.PwSQLFilenameFilter;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;


/**
 * <code>PwPartialPlanImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwPartialPlanImpl implements PwPartialPlan {

  private String url; 
  private Integer projectId;
  private Integer sequenceId;

  private String model;
  private String name;
  private Long id; // PartialPlan id
  private Map objectMap; // key = attribute id, value = PwObjectImpl instance
  private Map timelineMap; // key = attribute id, value = PwTimelineImpl instance
  private Map slotMap; // key = attribute id, value = PwSlotImpl instance
  private Map tokenMap; // key = attribute id, value = PwTokenImpl instance
  private Map constraintMap; // key = attribute id, value = PwConstraintImpl instance
  private Map parameterMap; // key = attribute id, value = PwParameterImpl instance
  private Map predicateMap; // key = attribute id, value = PwPredicateImpl instance
  private Map tokenRelationMap; // key = attribute id, value = PwTokenRelationImpl instance
  private Map variableMap; // key = attribute id, value = PwVariableImpl instance

  /**
   * <code>PwPartialPlanImpl</code> - initialize storage structures then call createPartialPlan()
   *
   * @param - <code>url</code> - path to a directory containing plan data
   * @param - <code>planName</code> - the name of the partial plan (usually stepN)
   * @param - <code>sequenceId</code> - the Id of the sequence to which this plan is attached.
   * @exception ResourceNotFoundException if the plan data is invalid
   */

  public PwPartialPlanImpl(String url, String planName, Integer sequenceId)  
    throws ResourceNotFoundException, SQLException {
    System.err.println("In PwPartialPlanImpl");
    objectMap = new HashMap();
    timelineMap = new HashMap();
    slotMap = new HashMap();
    tokenMap = new HashMap();
    constraintMap = new HashMap();
    parameterMap = new HashMap(); 
    predicateMap = new HashMap();
    tokenRelationMap = new HashMap(); 
    variableMap = new HashMap();
    this.url = (new StringBuffer(url)).append(System.getProperty("file.separator")).append(planName).toString();
    this.name = planName;
    createPartialPlan(sequenceId);
  }

  /**
   * <code>createPartialPlan</code> - load plan data into the database and/or construct substructures
   *
   * @param - <code>sequenceId</code> - Id of the sequence to which this plan is attached
   * @exception ResourceNotFoundException if the plan data is invalid
   */

  private void createPartialPlan(Integer sequenceId) throws ResourceNotFoundException, SQLException  {
    System.err.println( "Creating PwPartialPlan  ..." + url);
    long startTimeMSecs = System.currentTimeMillis();
    long loadTime = 0L;
    HashMap existingPartialPlan = null;
    if(!MySQLDB.partialPlanExists(sequenceId, name)) {
      String [] fileNames = new File(url).list(new PwSQLFilenameFilter());
      for(int i = 0; i < fileNames.length; i++) {
        String tableName = fileNames[i].substring(fileNames[i].lastIndexOf(".")+1);
        tableName = tableName.substring(0,1).toUpperCase().concat(tableName.substring(1));
        if(tableName.lastIndexOf("s") == tableName.length()-1) {
          tableName = tableName.substring(0, tableName.length()-1);
        }
        if(tableName.equals("Constraint")) {
          tableName = "VConstraint";
        }
        long time1 = System.currentTimeMillis();
        MySQLDB.loadFile(url.toString().concat(System.getProperty("file.separator")).concat(fileNames[i]), tableName);
        loadTime += System.currentTimeMillis() - time1;
      }
      MySQLDB.updatePartialPlanSequenceId(sequenceId);
      MySQLDB.analyzeDatabase();
    }
    id = MySQLDB.getNewPartialPlanId(sequenceId, name);
    System.err.println("LOAD DATA INFILE time " + loadTime + "ms.");
    MySQLDB.createObjects(this);
    model = MySQLDB.queryPartialPlanModelById(id);
    System.err.println("Creating Timeline/Slot/Token structure");
    MySQLDB.createTimelineSlotTokenNodesStructure(this);
    System.err.println( "Creating constraint, predicate, tokenRelation, & variable ...");
    long start2TimeMSecs = (new Date()).getTime();
    fillElementMaps();
    long stop2TimeMSecs = (new Date()).getTime();
    System.err.println( "   ... elapsed time: " +
                        (stop2TimeMSecs - start2TimeMSecs) + " msecs.");
    long stopTimeMSecs = (new Date()).getTime();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    cleanConstraints();
    //reorderSlots();
  } // end createPartialPlan

  /**
   * <code>fillElementMaps</code> - construct constraint, predicate, token relation, and variable
   *                                structures.
   */

  private final void fillElementMaps() {

    
    MySQLDB.queryConstraints( this);
    
    // parameters are inside predicates
    MySQLDB.queryPredicates( this);

    MySQLDB.queryTokenRelations( this);

    MySQLDB.queryVariables( this);

    System.err.println( "Partial Plan ids:");
    System.err.println( "  objectMap        " + objectMap.keySet().size());
    System.err.println( "  timelineMap      " + timelineMap.keySet().size());
    System.err.println( "  slotMap          " + slotMap.keySet().size());
    System.err.println( "  tokenMap         " + tokenMap.keySet().size());
    System.err.println( "  constraintMap    " + constraintMap.keySet().size());
    System.err.println( "  predicateMap     " + predicateMap.keySet().size());
    System.err.println( "  parameterMap     " + parameterMap.keySet().size());
    System.err.println( "  tokenRelationMap " + tokenRelationMap.keySet().size());
    System.err.println( "  variableMap      " + variableMap.keySet().size());

  } // end fillElementMaps


  /**
   * <code>getObjectIdList</code> - get complete list of Ids for PwObject objects
   *
   * @return - <code>List</code> - of Integer
   */
  public List getObjectIdList() {
    /*Object [] temp = objectMap.keySet().toArray();
    Integer [] objectIds = new Integer[temp.length];
    System.arraycopy(temp, 0, objectIds, 0, temp.length);
    ArrayList retval = new ArrayList();
    for(int i = 0; i < objectIds.length; i++) {
      retval.add(objectIds[i]);
      }
      return retval;*/
    return new ArrayList(objectMap.keySet());
  }

  /**
   * <code>getObjectImpl</code> - get a PwObjectImpl object.  Not accessable outside of this package
   *
   * @param id - <code>Integer</code> - the Id of the desired PwObjectImpl
   * @return - <code>PwObjectImpl</code> - 
   */
  public PwObjectImpl getObjectImpl( Integer id) {
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
   * @return - <code>List</code> - of PwObject
   */
  public List getObjectList() {
    List retval = new ArrayList();
    retval.addAll(objectMap.values());
    return retval;
  }

  /**
   * <code>getFreeTokenList</code> - get a list of free tokens in this plan
   *
   * @return - <code>List</code> - of PwToken 
   */

  public List getFreeTokenList() {
    List retval = new ArrayList();
    Iterator tokenIterator = tokenMap.values().iterator();
    while(tokenIterator.hasNext()) {
      PwToken token = (PwToken) tokenIterator.next();
      if(token.isFreeToken()) {
        retval.add(token);
      }
    }
    return retval;
  }

  /**
   * <code>getObject</code> - get object by Id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwObject</code> - 
   */
  public PwObject getObject( Integer id) {
    return (PwObject)objectMap.get(id);
  } // end getObject


  /**
   * <code>getTimeline</code> - get timeline by id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwTimeline</code> - 
   */
  public PwTimeline getTimeline( Integer id) {
    return (PwTimeline)timelineMap.get(id);
  } // end getTimeline


  /**
   * <code>getSlot</code> - get slot by id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwSlot</code> - 
   */
  public PwSlot getSlot( Integer id) {
    return (PwSlot)slotMap.get(id);
  } // end getSlot


  /**
   * <code>getToken</code> - get token by id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwToken</code> - 
   */
  public PwToken getToken( Integer id) {
    return (PwToken) tokenMap.get(id);
  } // end getToken


  /**
   * <code>getConstraint</code> - get constraint by id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwConstraint</code> - 
   */
  public PwConstraint getConstraint( Integer id) {
    return (PwConstraint) constraintMap.get(id);
  } // end getConstraint


  /**
   * <code>getParameter</code> - get parameter by id
   *
   * @param id - <code>Id</code> - 
   * @return - <code>PwParameter</code> - 
   */
  public PwParameter getParameter( Integer id) {
    return (PwParameter) parameterMap.get(id);
  } // end getParameter


  /**
   * <code>getPredicate</code> - get predicate by id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwPredicate</code> - 
   */
  public PwPredicate getPredicate( Integer id) {
    return (PwPredicate) predicateMap.get(id);
  } // end getPredicate


  /**
   * <code>getTokenRelation</code> - get token relation by id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwTokenRelation</code> - 
   */
  public PwTokenRelation getTokenRelation( Integer id) {
    return (PwTokenRelation) tokenRelationMap.get(id);
  } // end getTokenRelation


  /**
   * <code>getVariable</code> - get variable by id
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getVariable( Integer id) {
    return (PwVariable) variableMap.get(id);
  } // end getVariable


  // END IMPLEMENT PwPartialPlan INTERFACE 
    

  /**
   * <code>tokenExists</code> - check to see if token with specified id is present
   *
   * @return - <code>boolean</code> - return value of tokenMap.containsKey()
   */

  public boolean tokenExists(Integer id) {
    return tokenMap.containsKey(id);
  }

  /**
   * <code>addObject</code> - add a PwObjectImpl object to the partial plan
   *
   * @param id - <code>Integer</code> - the Id of the PwObjectImpl
   * @param object - <code>PwObjectImpl</code>
   */

  public void addObject(Integer id, PwObjectImpl object) {
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
  public void addConstraint( Integer id, PwConstraintImpl constraint) {
    if(constraintMap.containsKey(id)) {
      return;
    }
    constraintMap.put( id, constraint);
  }

  /**
   * <code>addParameter</code>
   *
   * @param id - <code>Integer</code> - 
   * @param parameter - <code>PwParameterImpl</code> - 
   */
  public void addParameter( Integer id, PwParameterImpl parameter) {
    if(parameterMap.containsKey(id)) {
      return;
    }
    parameterMap.put( id, parameter);
  }

  /**
   * <code>addPredicate</code>
   *
   * @param id - <code>Integer</code> - 
   * @param predicate - <code>PwPredicateImpl</code> - 
   */
  public void addPredicate( Integer id, PwPredicateImpl predicate) {
    if(predicateMap.containsKey(id)) {
      return;
    }
    predicateMap.put( id, predicate);
  }

  /**
   * <code>addSlot</code>
   *
   * @param id - <code>Integer</code> - 
   * @param slot - <code>PwSlotImpl</code> - 
   */
  public void addSlot( Integer id, PwSlotImpl slot) {
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
  public void addTimeline( Integer id, PwTimelineImpl timeline) {
    if(timelineMap.containsKey(id)) {
      return;
    }
    timelineMap.put( id, timeline);
  }

  /**
   * <code>addToken</code>
   *
   * @param id - <code>Integer</code> - 
   * @param token - <code>PwTokenImpl</code> - 
   */
  public void addToken( Integer id, PwTokenImpl token) {
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
  public void addTokenRelation( Integer id, PwTokenRelationImpl tokenRelation) {
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
  public void addVariable( Integer id, PwVariableImpl variable) {
    if(variableMap.containsKey(id)) {
      return;
    }
    variableMap.put( id, variable);
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
          //constraintMap.remove(constraint.getId());
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

  public void checkPlan() {
    checkRelations();
    checkConstraints();
    checkTokens();
  }

  /**
   * <code>checkRelations</code> - verify that all tokens related by token relations exist
   */
  private void checkRelations() {
    Iterator relationIterator = tokenRelationMap.values().iterator();
    while(relationIterator.hasNext()) {
      PwTokenRelationImpl relation = (PwTokenRelationImpl) relationIterator.next();
      if(!tokenMap.containsKey(relation.getTokenAId())) {
        System.err.println("Token relation " + relation.getId() + " has nonexistant token " +
                           relation.getTokenAId());
      }
      if(!tokenMap.containsKey(relation.getTokenBId())) {
        System.err.println("Token relation " + relation.getId() + " has nonexistant token " +
                           relation.getTokenBId());
      }
    }
  }

  /**
   * <code>checkConstraints</code> - verify that all constrained variables exist
   */
  private void checkConstraints() {
    Iterator constraintIterator = constraintMap.values().iterator();
    while(constraintIterator.hasNext()) {
      PwConstraintImpl constraint = (PwConstraintImpl) constraintIterator.next();
      ListIterator variableIdIterator = constraint.getVariableIdList().listIterator();
      int variableIndex = 0;
      while(variableIdIterator.hasNext()) {
        Integer variableId = (Integer) variableIdIterator.next();
        if(!variableMap.containsKey(variableId)) {
          System.err.println("Constraint " + constraint.getId() + " has nonexistant variable " +
                             variableId + " at position " + variableIndex);
        }
        variableIndex++;
      }
    }
  }
  
  /**
   * <code>checkTokens</code> - verify that all tokens have predicates, the correct number of parameter
   *                            variables, that all variables exist and are of the right type, that 
   *                            slotted tokens have valid slots, timelines, and objects, as well as
   *                            checking that the tokens have the relations they are in as well as being
   *                            in only the relations they have.
   */
  private void checkTokens() {
    Iterator tokenIterator = tokenMap.values().iterator();
    while(tokenIterator.hasNext()) {
      PwTokenImpl token = (PwTokenImpl) tokenIterator.next();
      if(token.getPredicate() == null) {
        System.err.println("Token " + token.getId() + " has null predicate.");
      }
      if(!token.isFreeToken()) {
        if(token.getObjectId() == null) {
          System.err.println("Slotted token " + token.getId() + " has null objectId.");
        }
        else if(!objectMap.containsKey(token.getObjectId())) {
          System.err.println("Slotted token " + token.getId() + " has nonexistant objectId " + 
                             token.getObjectId());
        }
        if(token.getTimelineId() == null) {
          System.err.println("Slotted token " + token.getId() + " has null timelineId.");
        }
        else if(!timelineMap.containsKey(token.getTimelineId())) {
          System.err.println("Slotted token " + token.getId() + " has nonexistant timelineId " +
                             token.getTimelineId());
        }
        if(token.getSlotId() == null) {
          System.err.println("Slotted token " + token.getId() + " has null slotId.");
        }
        else if(!slotMap.containsKey(token.getSlotId())) {
          System.err.println("Slotted token " + token.getId() + " has nonexistant slotId " + 
                             token.getSlotId());
        }
      }
      checkTokenVars(token);
      checkTokenParamVars(token);
      checkTokenRelations(token);
    }
  }
  /**
   * <code>checkTokenVars</code> - verify that all variables exist and are of the proper type
   */
  private void checkTokenVars(PwTokenImpl token) {
    PwVariableImpl startVar = (PwVariableImpl) token.getStartVariable();
    PwVariableImpl endVar = (PwVariableImpl) token.getEndVariable();
    PwVariableImpl durationVar = (PwVariableImpl) token.getDurationVariable();
    PwVariableImpl objectVar = (PwVariableImpl) token.getObjectVariable();
    PwVariableImpl rejectVar = (PwVariableImpl) token.getRejectVariable();
    if(startVar == null) {
      System.err.println("Token " + token.getId() + " has null start variable.");
    }
    else {
      if(!startVar.getType().equals("START_VAR")) {
        System.err.println("Token " + token.getId() + "'s start variable " + startVar.getId() + 
                           " isn't.");
      }
      else {
        checkVariable(startVar);
      }
    }
    if(endVar == null) {
      System.err.println("Token " + token.getId() + " has null end variable.");
    }
    else {
      if(!endVar.getType().equals("END_VAR")) {
        System.err.println("Token " + token.getId() + "'s end variable " + endVar.getId() + " isn't.");
      }
      else {
        checkVariable(endVar);
      }
    }
    if(durationVar == null) {
      System.err.println("Token " + token.getId() + " has null duration variable.");
    }
    else {
      if(!durationVar.getType().equals("DURATION_VAR")) {
        System.err.println("Token " + token.getId() + "'s duration variable " + durationVar.getId() +
                           " isn't.");
      }
      else {
        checkVariable(durationVar);
      }
    }
    if(objectVar == null) {
      System.err.println("Token " + token.getId() + " has null object variable.");
    }
    else {
      if(!objectVar.getType().equals("OBJECT_VAR")) {
        System.err.println("Token " + token.getId() + "'s object variable " + objectVar.getId() + 
                           " isn't.");
      }
      else {
        checkObjectVariable(objectVar, token.isFreeToken());
      }
    }
    if(rejectVar == null) {
      System.err.println("Token " + token.getId() + " has null reject variable.");
    }
    else {
      if(!rejectVar.getType().equals("REJECT_VAR")) {
        System.err.println("Token " + token.getId() + "'s reject variable " + rejectVar.getId()  + 
                           " isn't.");
      }
      else {
        checkVariable(rejectVar);
      }
    }
  }
  /**
   * <code>checkTokenParamVars</code> - verify that the token has the correct number of parameter 
   *                                    variables
   */
  private void checkTokenParamVars(PwTokenImpl token) {
    List paramVarList = token.getParamVarsList();
    int paramVarSize = paramVarList.size();
    int paramSize = token.getPredicate().getParameterList().size();
    if(paramVarSize != paramSize) {
      System.err.println("Token " + token.getId().toString() + " has " + paramVarSize + 
                         " parameter variables.  Predicate " + token.getPredicate().getId() + " has " + 
                         paramSize + " parameters.");
    }
    ListIterator paramVarIterator = paramVarList.listIterator();
    while(paramVarIterator.hasNext()) {
      PwVariableImpl variable = (PwVariableImpl) paramVarIterator.next();
      if(variable == null) {
        System.err.println("Token " + token.getId().toString() + " has null variable.");
        continue;
      }
      if(!variableMap.containsValue(variable)) {
        System.err.println("Token " + token.getId().toString() + " has parameter variable " + 
                           variable.getId() + " that doesn't exist.");
      }
      else {
        checkVariable(variable);
      }
    }
  }
  /**
   * <code>checkTokenRelations</code> - ensure that all tokens related exist, are in the relations they
   *                                    have, and are in only the relations they have.
   */
  private void checkTokenRelations(PwTokenImpl token) {
    List relations = token.getTokenRelationIdsList();
    if(relations.size() == 0) {
      return;
    }
    ListIterator relationIdIterator = relations.listIterator();
    while(relationIdIterator.hasNext()) {
      Integer tokenRelationId = (Integer) relationIdIterator.next();
      PwTokenRelationImpl tokenRelation = 
        (PwTokenRelationImpl) tokenRelationMap.get(tokenRelationId);
      
      if(tokenRelation == null) {
        System.err.println("Token " + token.getId() + " has nonexistant token relation " +
                           tokenRelationId);
        continue;
      }
      if(tokenRelation.getTokenAId() == null) {
        System.err.println("Master token id of relation " + tokenRelation.getId() + " is null.");
        if(tokenRelation.getTokenBId() == null) {
          System.err.println("Slave token id of relation " + tokenRelation.getId() + " is null.");
          continue;
        }
      }
      if(tokenRelation.getTokenBId() == null) {
        System.err.println("Slave token id of relation " + tokenRelation.getId() + " is null.");
        continue;
      }
      if(!tokenRelation.getTokenAId().equals(token.getId()) && 
         !tokenRelation.getTokenBId().equals(token.getId())) {
        System.err.println("Token " + token.getId() + " has relation " + tokenRelation.getId() + 
                           " but isn't in it.");
      }
      if(tokenMap.get(tokenRelation.getTokenAId()) == null) {
        System.err.println("Master token " + tokenRelation.getTokenAId() + " doesn't exist.");
      }
      if(tokenMap.get(tokenRelation.getTokenBId()) == null) {
        System.err.println("Slave token " + tokenRelation.getTokenBId() + " doesn't exist.");
      }
    }
    Iterator tokenRelationIterator = tokenRelationMap.values().iterator();
    while(tokenRelationIterator.hasNext()) {
      PwTokenRelationImpl tokenRelation = (PwTokenRelationImpl) tokenRelationIterator.next();
      if((tokenRelation.getTokenAId().equals(token.getId()) || 
          tokenRelation.getTokenBId().equals(token.getId())) && !relations.contains(tokenRelation.getId())) {
        System.err.println("Token " + token.getId() + " is in relation " + tokenRelation.getId() +
                           " but doesn't have it.");
      }
    }
  }
  /**
   * <code>checkVariable</code> - verify that a variable is of a valid type, has constraints that exist,
   *                              is in the constraints that is has, and is only in those constraints
   */
  private void checkVariable(PwVariableImpl var) {
    if(var.getDomain() == null) {
      System.err.println("Variable " + var.getId() + " has no domain.");
    }
    String type = var.getType();
    if(!type.equals("START_VAR") && !type.equals("END_VAR") && !type.equals("DURATION_VAR") && 
       !type.equals("OBJECT_VAR") && !type.equals("PARAMETER_VAR") && !type.equals("REJECT_VAR") &&
       !type.equals("GLOBAL_VAR")) {
      System.err.println("Variable " + var.getId() + " has invalid type " + type);
    }
    List varConstraints = var.getConstraintList();
    ListIterator varConstraintIterator = varConstraints.listIterator();
    while(varConstraintIterator.hasNext()) {
      PwConstraintImpl constraint = (PwConstraintImpl) varConstraintIterator.next();
      if(constraint == null) {
        System.err.println("Variable " + var.getId() + " has a null constraint.");
        continue;
      }
      if(!constraint.getVariablesList().contains(var)) {
        System.err.println("Variable " + var.getId() + " has constraint " + constraint.getId() + 
                           " but is not in the constraint.");
      }
      if(!constraintMap.containsKey(constraint.getId())) {
        System.err.println("Variable " + var.getId() + " has nonexistant constraint " + 
                           constraint.getId());
      }
    }
    Iterator constraintIterator = constraintMap.values().iterator();
    while(constraintIterator.hasNext()) {
      PwConstraintImpl constraint = (PwConstraintImpl) constraintIterator.next();
      if(constraint.getVariablesList().contains(var) && !varConstraints.contains(constraint)) {
        System.err.println("Variable " + var.getId() + " is in constraint " + constraint.getId() + 
                           " but does not have it.");
      }
    }
  }
  /**
   * <code>checkObjectVariable</code> - check validity of object variable, as well as that there is only
   *                                  - one object in the domain if the token is slotted
   */
  private void checkObjectVariable(PwVariableImpl objectVar, boolean isFreeToken) {
    checkVariable(objectVar);
    PwEnumeratedDomainImpl domain = (PwEnumeratedDomainImpl) objectVar.getDomain();
    if(!isFreeToken) {
      if(domain.getEnumeration().size() > 1) {
        System.err.println("Slotted token has object variable " + objectVar.getId() + 
                           " with multiple objects.");
      }
    }
  }

} // end class PwPartialPlanImpl
