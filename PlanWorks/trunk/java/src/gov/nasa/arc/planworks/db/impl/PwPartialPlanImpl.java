// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPartialPlanImpl.java,v 1.31 2003-08-12 22:54:00 miatauro Exp $
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
import java.util.Map;

import gov.nasa.arc.planworks.db.DbConstants;
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
//import gov.nasa.arc.planworks.db.util.XmlDBeXist;
//import gov.nasa.arc.planworks.db.util.XmlFilenameFilter;
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

  private String url; // pathaname of xml file (no extension)
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

  private int minId, maxId;

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
      //MySQLDB.analyzeDatabase();
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
  } // end createPartialPlan


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
   * <code>getObjectIdList</code>
   *
   * @return - <code>List</code> - of String
   */
  public List getObjectIdList() {
    Object [] temp = objectMap.keySet().toArray();
    Integer [] objectIds = new Integer[temp.length];
    System.arraycopy(temp, 0, objectIds, 0, temp.length);
    ArrayList retval = new ArrayList();
    for(int i = 0; i < objectIds.length; i++) {
      retval.add(objectIds[i]);
    }
    return retval;
  }

  /**
   * <code>getObjectImpl</code>
   *
   * @param id - <code>int</code> - 
   * @return - <code>PwObjectImpl</code> - 
   */
  public PwObjectImpl getObjectImpl( Integer id) {
    return (PwObjectImpl) objectMap.get( id);
  }



  // IMPLEMENT PwPartialPlan INTERFACE 
    

  /**
   * <code>getUrl</code>
   *
   * @return - <code>String</code> - 
   */
  public String getUrl() {
    return url;
  }

  public Long getId() {
    return id;
  }

  public int getMinId() {
    return minId;
  }

  public int getMaxId() {
    return maxId;
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
   * <code>getObject</code> - if not in Map, query
   *
   * @param id - <code>String</code> - 
   * @return - <code>PwObject</code> - 
   */
  public PwObject getObject( Integer id) {
    return (PwObject)objectMap.get(id);
  } // end getObject


  /**
   * <code>getTimeline</code> - if not in Map, query
   *
   * @param id - <code>String</code> - 
   * @return - <code>PwTimeline</code> - 
   */
  public PwTimeline getTimeline( Integer id) {
    return (PwTimeline)timelineMap.get(id);
  } // end getTimeline


  /**
   * <code>getSlot</code> - if not in Map, query
   *
   * @param id - <code>String</code> - 
   * @return - <code>PwSlot</code> - 
   */
  public PwSlot getSlot( Integer id) {
    return (PwSlot)slotMap.get(id);
  } // end getSlot


  /**
   * <code>getToken</code> - if not in Map, query
   *
   * @param id - <code>String</code> - 
   * @return - <code>PwToken</code> - 
   */
  public PwToken getToken( Integer id) {
    return (PwToken) tokenMap.get(id);
  } // end getToken


  /**
   * <code>getConstraint</code> - if not in Map, query
   *
   * @param id - <code>String</code> - 
   * @return - <code>PwConstraint</code> - 
   */
  public PwConstraint getConstraint( Integer id) {
    return (PwConstraint) constraintMap.get(id);
  } // end getConstraint


  /**
   * <code>getParameter</code> - if not in Map, query
   *
   * @param id - <code>String</code> - 
   * @return - <code>PwParameter</code> - 
   */
  public PwParameter getParameter( Integer id) {
    return (PwParameter) parameterMap.get(id);
  } // end getParameter


  /**
   * <code>getPredicate</code> - if not in Map, query
   *
   * @param id - <code>String</code> - 
   * @return - <code>PwPredicate</code> - 
   */
  public PwPredicate getPredicate( Integer id) {
    return (PwPredicate) predicateMap.get(id);
  } // end getPredicate


  /**
   * <code>getTokenRelation</code> - if not in Map, query
   *
   * @param id - <code>String</code> - 
   * @return - <code>PwTokenRelation</code> - 
   */
  public PwTokenRelation getTokenRelation( Integer id) {
    return (PwTokenRelation) tokenRelationMap.get(id);
  } // end getTokenRelation


  /**
   * <code>getVariable</code> - if not in Map, query
   *
   * @param id - <code>String</code> - 
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getVariable( Integer id) {
    return (PwVariable) variableMap.get(id);
  } // end getVariable


  // END IMPLEMENT PwPartialPlan INTERFACE 
    

  public boolean tokenExists(Integer id) {
    return tokenMap.containsKey(id);
  }

  public void addObject(Integer id, PwObjectImpl object) {
    if(objectMap.containsKey(id)) {
      return;
    }
    objectMap.put(id, object);
  }

  /**
   * <code>addConstraint</code>
   *
   * @param id - <code>String</code> - 
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
   * @param id - <code>String</code> - 
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
   * @param id - <code>String</code> - 
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
   * @param id - <code>String</code> - 
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
   * @param id - <code>String</code> - 
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
   * @param id - <code>String</code> - 
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
   * @param id - <code>String</code> - 
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
   * @param id - <code>String</code> - 
   * @param variable - <code>PwVariableImpl</code> - 
   */
  public void addVariable( Integer id, PwVariableImpl variable) {
    if(variableMap.containsKey(id)) {
      return;
    }
    variableMap.put( id, variable);
  }


} // end class PwPartialPlanImpl
