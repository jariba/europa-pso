// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPartialPlanImpl.java,v 1.26 2003-07-29 23:24:36 miatauro Exp $
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
  private Long key; // PartialPlan key
  private Map objectMap; // key = attribute key, value = PwObjectImpl instance
  private Map timelineMap; // key = attribute key, value = PwTimelineImpl instance
  private Map slotMap; // key = attribute key, value = PwSlotImpl instance
  private Map tokenMap; // key = attribute key, value = PwTokenImpl instance
  private Map constraintMap; // key = attribute key, value = PwConstraintImpl instance
  private Map parameterMap; // key = attribute key, value = PwParameterImpl instance
  private Map predicateMap; // key = attribute key, value = PwPredicateImpl instance
  private Map tokenRelationMap; // key = attribute key, value = PwTokenRelationImpl instance
  private Map variableMap; // key = attribute key, value = PwVariableImpl instance

  private int minKey, maxKey;

  public PwPartialPlanImpl(String url, String planName, Integer sequenceKey)  
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
    createPartialPlan(sequenceKey);
  }

  private void createPartialPlan(Integer sequenceKey) throws ResourceNotFoundException, SQLException  {
    System.err.println( "Creating PwPartialPlan  ..." + url);
    long startTimeMSecs = System.currentTimeMillis();
    long loadTime = 0L;
    ResultSet existingPartialPlan =
      MySQLDB.queryDatabase("SELECT PartialPlanId, MinKey, MaxKey FROM PartialPlan WHERE SequenceId=".concat(sequenceKey.toString()).concat(" && PlanName='").concat(name).concat("'"));
    existingPartialPlan.last();
    if(existingPartialPlan.getRow() < 1) {
      String [] fileNames = new File(url).list(new FilenameFilter () {
          public boolean accept(File dir, String name) {
            return (name.endsWith(".partialPlan") || name.endsWith(".objects") || 
                    name.endsWith(".timelines") || name.endsWith(".slots") ||
                    name.endsWith(".tokens") || name.endsWith(".variables") ||
                    name.endsWith(".predicates") || name.endsWith(".parameters") ||
                    name.endsWith(".enumeratedDomains") || name.endsWith(".intervalDomains") ||
                    name.endsWith(".constraints") || name.endsWith(".tokenRelations") || 
                    name.endsWith(".paramVarTokenMap") || name.endsWith(".constraintVarMap"));
          }
        });
      for(int i = 0; i < fileNames.length; i++) {
        String tableName = fileNames[i].substring(fileNames[i].lastIndexOf(".")+1);
        tableName = tableName.substring(0,1).toUpperCase().concat(tableName.substring(1));
        if(tableName.lastIndexOf("s") == tableName.length()-1) {
          tableName = tableName.substring(0, tableName.length()-1);
        }
        if(tableName.equals("Constraint")) {
          tableName = "VConstraint";
        }
        System.err.println("Loading " + fileNames[i] + " into " + tableName);
        long time1 = System.currentTimeMillis();
        MySQLDB.updateDatabase("LOAD DATA INFILE '".concat(url).concat(System.getProperty("file.separator")).concat(fileNames[i]).concat("' IGNORE INTO TABLE ").concat(tableName));
        loadTime += System.currentTimeMillis() - time1;
      }
      MySQLDB.updateDatabase("UPDATE PartialPlan SET SequenceId=".concat(sequenceKey.toString()).concat(" WHERE SequenceId=-1"));
      existingPartialPlan =
        MySQLDB.queryDatabase("SELECT PartialPlanId, MinKey, MaxKey FROM PartialPlan WHERE SequenceId=".concat(sequenceKey.toString()).concat(" && PlanName='").concat(name).concat("'"));
    }
    System.err.println("LOAD DATA INFILE time " + loadTime + "ms.");
    existingPartialPlan.first();
    key = new Long(existingPartialPlan.getLong("PartialPlanId"));
    minKey = existingPartialPlan.getInt("MinKey");
    maxKey = existingPartialPlan.getInt("MaxKey");
    ResultSet dbObject =
      MySQLDB.queryDatabase("SELECT ObjectName, ObjectId FROM Object WHERE PartialPlanId=".concat(key.toString()));
    while(dbObject.next()) {
      Integer objectKey = new Integer(dbObject.getInt("ObjectId"));
      objectMap.put(objectKey, new PwObjectImpl(objectKey, dbObject.getString("ObjectName"),this));
    }
    dbObject.close();
    model = MySQLDB.INSTANCE.queryPartialPlanModelByKey(key);
    System.err.println("Creating Timeline/Slot/Token structure");
    MySQLDB.INSTANCE.createTimelineSlotTokenNodesStructure(this);
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

    
    MySQLDB.INSTANCE.queryConstraints( this);
    
    // parameters are inside predicates
    MySQLDB.INSTANCE.queryPredicates( this);

    MySQLDB.INSTANCE.queryTokenRelations( this);

    MySQLDB.INSTANCE.queryVariables( this);

    System.err.println( "Partial Plan keys:");
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
   * @param key - <code>int</code> - 
   * @return - <code>PwObjectImpl</code> - 
   */
  public PwObjectImpl getObjectImpl( Integer key) {
    return (PwObjectImpl) objectMap.get( key);
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

  public Long getKey() {
    return key;
  }

  public int getMinKey() {
    return minKey;
  }

  public int getMaxKey() {
    return maxKey;
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
   * @param key - <code>String</code> - 
   * @return - <code>PwObject</code> - 
   */
  public PwObject getObject( Integer key) {
    return (PwObject)objectMap.get(key);
  } // end getObject


  /**
   * <code>getTimeline</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwTimeline</code> - 
   */
  public PwTimeline getTimeline( Integer key) {
    return (PwTimeline)timelineMap.get(key);
  } // end getTimeline


  /**
   * <code>getSlot</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwSlot</code> - 
   */
  public PwSlot getSlot( Integer key) {
    return (PwSlot)slotMap.get(key);
  } // end getSlot


  /**
   * <code>getToken</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwToken</code> - 
   */
  public PwToken getToken( Integer key) {
    return (PwToken) tokenMap.get(key);
  } // end getToken


  /**
   * <code>getConstraint</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwConstraint</code> - 
   */
  public PwConstraint getConstraint( Integer key) {
    return (PwConstraint) constraintMap.get(key);
  } // end getConstraint


  /**
   * <code>getParameter</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwParameter</code> - 
   */
  public PwParameter getParameter( Integer key) {
    return (PwParameter) parameterMap.get(key);
  } // end getParameter


  /**
   * <code>getPredicate</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwPredicate</code> - 
   */
  public PwPredicate getPredicate( Integer key) {
    return (PwPredicate) predicateMap.get(key);
  } // end getPredicate


  /**
   * <code>getTokenRelation</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwTokenRelation</code> - 
   */
  public PwTokenRelation getTokenRelation( Integer key) {
    return (PwTokenRelation) tokenRelationMap.get(key);
  } // end getTokenRelation


  /**
   * <code>getVariable</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getVariable( Integer key) {
    return (PwVariable) variableMap.get(key);
  } // end getVariable


  // END IMPLEMENT PwPartialPlan INTERFACE 
    

  public boolean tokenExists(Integer key) {
    return tokenMap.containsKey(key);
  }

  /**
   * <code>addConstraint</code>
   *
   * @param key - <code>String</code> - 
   * @param constraint - <code>PwConstraintImpl</code> - 
   */
  public void addConstraint( Integer key, PwConstraintImpl constraint) {
    if(constraintMap.containsKey(key)) {
      return;
    }
    constraintMap.put( key, constraint);
  }

  /**
   * <code>addParameter</code>
   *
   * @param key - <code>String</code> - 
   * @param parameter - <code>PwParameterImpl</code> - 
   */
  public void addParameter( Integer key, PwParameterImpl parameter) {
    if(parameterMap.containsKey(key)) {
      return;
    }
    parameterMap.put( key, parameter);
  }

  /**
   * <code>addPredicate</code>
   *
   * @param key - <code>String</code> - 
   * @param predicate - <code>PwPredicateImpl</code> - 
   */
  public void addPredicate( Integer key, PwPredicateImpl predicate) {
    if(predicateMap.containsKey(key)) {
      return;
    }
    predicateMap.put( key, predicate);
  }

  /**
   * <code>addSlot</code>
   *
   * @param key - <code>String</code> - 
   * @param slot - <code>PwSlotImpl</code> - 
   */
  public void addSlot( Integer key, PwSlotImpl slot) {
    if(slotMap.containsKey(key)) {
      return;
    }
    slotMap.put( key, slot);
  }

  /**
   * <code>addTimeline</code>
   *
   * @param key - <code>String</code> - 
   * @param timeline - <code>PwTimelineImpl</code> - 
   */
  public void addTimeline( Integer key, PwTimelineImpl timeline) {
    if(timelineMap.containsKey(key)) {
      return;
    }
    timelineMap.put( key, timeline);
  }

  /**
   * <code>addToken</code>
   *
   * @param key - <code>String</code> - 
   * @param token - <code>PwTokenImpl</code> - 
   */
  public void addToken( Integer key, PwTokenImpl token) {
    if(tokenMap.containsKey(key)) {
      return;
    }
    tokenMap.put( key, token);
  }
  /**
   * <code>addTokenRelation</code>
   *
   * @param key - <code>String</code> - 
   * @param tokenRelation - <code>PwTokenRelationImpl</code> - 
   */
  public void addTokenRelation( Integer key, PwTokenRelationImpl tokenRelation) {
    if(tokenRelationMap.containsKey(key)) {
      return;
    }
    tokenRelationMap.put( key, tokenRelation);
  }

  /**
   * <code>addVariable</code>
   *
   * @param key - <code>String</code> - 
   * @param variable - <code>PwVariableImpl</code> - 
   */
  public void addVariable( Integer key, PwVariableImpl variable) {
    if(variableMap.containsKey(key)) {
      return;
    }
    variableMap.put( key, variable);
  }


} // end class PwPartialPlanImpl
