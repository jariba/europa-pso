// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPartialPlanImpl.java,v 1.8 2003-06-02 17:49:58 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db.impl;

import java.io.File;
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
import gov.nasa.arc.planworks.db.util.XmlDBeXist;
import gov.nasa.arc.planworks.db.util.XmlFilenameFilter;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;


/**
 * <code>PwPartialPlanImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwPartialPlanImpl implements PwPartialPlan {

  private String url; // pathaname of xml file
  private String projectCollectionName; // e.g. test
  private String sequenceCollectionName; // e.g. monkey

  private String userCollectionName; // e.g. /wtaylor
  private String collectionName; // e.g. /wtaylor/test/monkey (xml files directory)
  private String xmlFileName; // with no extension

  private String model;
  private String key; // PartialPlan key
  private Map objectMap; // key = attribute key, value = PwObjectImpl instance
  private Map timelineMap; // key = attribute key, value = PwTimelineImpl instance
  private Map slotMap; // key = attribute key, value = PwSlotImpl instance
  private Map tokenMap; // key = attribute key, value = PwTokenImpl instance
  private Map constraintMap; // key = attribute key, value = PwConstraintImpl instance
  private Map parameterMap; // key = attribute key, value = PwParameterImpl instance
  private Map predicateMap; // key = attribute key, value = PwPredicateImpl instance
  private Map tokenRelationMap; // key = attribute key, value = PwTokenRelationImpl instance
  private Map variableMap; // key = attribute key, value = PwVariableImpl instance

  private List objectIdList; // PwObjectImpl keys


  /**
   * <code>PwPartialPlanImpl</code> - constructor 
   *     retrieves partial plan from XML:DB and
   *     builds Java data structure
   * @param url - <code>String</code> - 
   * @param projectCollectionName - <code>String</code> - 
   * @param sequenceCollectionName - <code>String</code> - 
   * @param isInDb - <code>boolean</code> - 
   */
  public PwPartialPlanImpl( String url, String projectCollectionName,
                            String sequenceCollectionName, boolean isInDb)
    throws ResourceNotFoundException {
    objectMap = new HashMap();
    timelineMap = new HashMap();
    slotMap = new HashMap();
    tokenMap = new HashMap();
    constraintMap = new HashMap();
    parameterMap = new HashMap(); 
    predicateMap = new HashMap();
    tokenRelationMap = new HashMap(); 
    variableMap = new HashMap();
    objectIdList = new ArrayList();
    userCollectionName = "/" + System.getProperty( "user");
    this.projectCollectionName = projectCollectionName;
    this.sequenceCollectionName = sequenceCollectionName;
    StringBuffer collectionBuffer = new StringBuffer(userCollectionName );
    collectionBuffer.append( "/");
    collectionBuffer.append( projectCollectionName).append( "/");
    collectionBuffer.append( sequenceCollectionName).append( "/");

    int index = url.lastIndexOf( "/");
    if (index == -1) {
      throw new ResourceNotFoundException( "partial plan url '" + url +
                                           "' cannot be parsed for '/'");
    } 
    xmlFileName = url.substring( index + 1);
    index = xmlFileName.lastIndexOf( ".");
    xmlFileName = xmlFileName.substring( 0, index - 1);
    collectionBuffer.append( xmlFileName);
    collectionName = collectionBuffer.toString();
    System.err.println( "PwPartialPlanImpl collectionName: " + collectionName);

    if (! isInDb) {
      System.err.println( "Load " + url);
      long startLoadTimeMSecs = (new Date()).getTime();

      XmlDBeXist.INSTANCE.addXMLFileToCollection( collectionName, url);

      long stopLoadTimeMSecs = (new Date()).getTime();
      String loadTimeString = "   ... elapsed time: " +
        (stopLoadTimeMSecs - startLoadTimeMSecs) + " msecs.";
      System.err.println( loadTimeString);
    }

    createPartialPlan();

  } // end constructor


  private void createPartialPlan() {
    long startTimeMSecs = (new Date()).getTime();

    List partialPlanKeys = XmlDBeXist.INSTANCE.queryPartialPlanKeys( collectionName);
    // should only be one with collection structure of
    // /db/wtaylor/test/monkey/step000
    Iterator keysIterator = partialPlanKeys.iterator();
    while (keysIterator.hasNext()) {
      String partialPlanKey = (String) keysIterator.next();
      key = partialPlanKey;
      System.err.println( "partialPlan key " + partialPlanKey);
      model = XmlDBeXist.INSTANCE.queryPartialPlanModelByKey( partialPlanKey,
                                                            collectionName);
      List objectNameAndKeyList = XmlDBeXist.INSTANCE.queryPartialPlanObjectsByKey
        ( partialPlanKey, collectionName);
      for (int i = 0, n = objectNameAndKeyList.size(); i < n; i = +2) {
        String objectName = (String) objectNameAndKeyList.get( i);
        String objectKey = (String) objectNameAndKeyList.get( i + 1);
        objectIdList.add( objectKey);
        objectMap.put( objectKey, 
                       new PwObjectImpl( objectKey, objectName, this));
      }

      XmlDBeXist.INSTANCE.createTimelineSlotTokenNodesStructure( this, collectionName);
 
      fillElementMaps();

    }
    long stopTimeMSecs = (new Date()).getTime();
    String timeString = "Create PwPartialPlan from Collection \n   ... elapsed time: " +
      //       writeTime( (stopTimeMSecs - startTimeMSecs)) + " seconds.";
      (stopTimeMSecs - startTimeMSecs) + " msecs.";
    System.err.println( timeString);
  } // end createPartialPlan


  private final void fillElementMaps() {
    Iterator keysIterator = null;
    List constraintKeyList =
      XmlDBeXist.INSTANCE.queryElementKeysByType( "constraint", collectionName);
    keysIterator = constraintKeyList.iterator();
    while (keysIterator.hasNext()) {
      getConstraint( (String) keysIterator.next());
    }

    // parameters are inside predicates
    List predicateKeyList =
      XmlDBeXist.INSTANCE.queryElementKeysByType( "predicate", collectionName);
    keysIterator = predicateKeyList.iterator();
    while (keysIterator.hasNext()) {
      getPredicate( (String) keysIterator.next());
    }

    List tokenRelationKeyList =
       XmlDBeXist.INSTANCE.queryElementKeysByType( "tokenRelation", collectionName);
    keysIterator = tokenRelationKeyList.iterator();
    while (keysIterator.hasNext()) {
      getTokenRelation( (String) keysIterator.next());
    }

    List variableKeyList =
      XmlDBeXist.INSTANCE.queryElementKeysByType( "variable", collectionName);
    keysIterator = variableKeyList.iterator();
    while (keysIterator.hasNext()) {
      getVariable( (String) keysIterator.next());
    }

//     System.err.println( "objectMap " + objectMap.keySet());
//     System.err.println( "timelineMap " + timelineMap.keySet());
//     System.err.println( "slotMap " + slotMap.keySet());
//     System.err.println( "tokenMap " + tokenMap.keySet());
//     System.err.println( "constraintMap " + constraintMap.keySet());
//     System.err.println( "predicateMap " + predicateMap.keySet());
//     System.err.println( "parameterMap " + parameterMap.keySet());
//     System.err.println( "tokenRelationMap " + tokenRelationMap.keySet());
//     System.err.println( "variableMap " + variableMap.keySet());
  } // end fillElementMaps


  /**
   * <code>getObjectIdList</code>
   *
   * @return - <code>List</code> - of String
   */
  public List getObjectIdList() {
    return objectIdList;
  }

  /**
   * <code>getObjectImpl</code>
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwObjectImpl</code> - 
   */
  public PwObjectImpl getObjectImpl( String key) {
    return (PwObjectImpl) objectMap.get( key);
  }

  /**
   * <code>addParameter</code>
   *
   * @param key - <code>String</code> - 
   * @param parameter - <code>PwParameterImpl</code> - 
   */
  public void addParameter( String key, PwParameterImpl parameter) {
    parameterMap.put( key, parameter);
  } // end addParameter

  /**
   * <code>getTimelineMap</code>
   *
   * @return - <code>Map</code> - 
   */
  public Map getTimelineMap() {
    return timelineMap;
  }

  /**
   * <code>getSlotMap</code>
   *
   * @return - <code>Map</code> - 
   */
  public Map getSlotMap() {
    return slotMap;
  }

  /**
   * <code>getTokenMap</code>
   *
   * @return - <code>Map</code> - 
   */
  public Map getTokenMap() {
    return tokenMap;
  }


  /// IMPLEMENT PwPartialPlan INTERFACE /////
    

  /**
   * <code>getCollectionName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getCollectionName() {
    return collectionName;
  }

  /**
   * <code>getObjectList</code> -
   *
   * @return - <code>List</code> - of PwObject
   */
  public List getObjectList() {
    List retval = new ArrayList( objectIdList.size());
    for (int i = 0; i < objectIdList.size(); i++) {
      retval.add( this.getObject( (String) objectIdList.get(i)));
    }
    return retval;
  }
  /**
   * <code>getObject</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwObject</code> - 
   */
  public PwObject getObject( String key) {
    PwObject object = (PwObject) objectMap.get( key);
    if (object == null) {
//       object = XmlDBeXist.queryObjectByKey( key, this, collectionName);
      objectMap.put( key, object);
    }
    return object;
  } // end getObject


  /**
   * <code>getTimeline</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwTimeline</code> - 
   */
  public PwTimeline getTimeline( String key) {
    PwTimeline timeline = (PwTimeline) timelineMap.get( key);
    if (timeline == null) {
//       timeline = XmlDBeXist.queryTimelineByKey( key, this, collectionName);
      timelineMap.put( key, timeline);
    }
    return timeline;
  } // end getTimeline


  /**
   * <code>getSlot</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwSlot</code> - 
   */
  public PwSlot getSlot( String key) {
    PwSlot slot = (PwSlot) slotMap.get( key);
    if (slot == null) {
//       slot = XmlDBeXist.querySlotByKey( key, this, collectionName);
      slotMap.put( key, slot);
    }
    return slot;
  } // end getSlot


  /**
   * <code>getToken</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwToken</code> - 
   */
  public PwToken getToken( String key) {
    PwToken token = (PwToken) tokenMap.get( key);
    if (token == null) {
//       token = XmlDBeXist.queryTokenByKey( key, this, collectionName);
      tokenMap.put( key, token);
    }
    return token;
  } // end getToken


  /**
   * <code>getConstraint</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwConstraint</code> - 
   */
  public PwConstraint getConstraint( String key) {
    PwConstraint constraint = (PwConstraint) constraintMap.get( key);
    if (constraint == null) {
      constraint = XmlDBeXist.queryConstraintByKey( key, this, collectionName);
      constraintMap.put( key, constraint);
    }
    return constraint;
  } // end getConstraint


  /**
   * <code>getParameter</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwParameter</code> - 
   */
  public PwParameter getParameter( String key) {
    PwParameter parameter = (PwParameter) parameterMap.get( key);
    if (parameter == null) {
//       parameter = XmlDBeXist.queryParameterByKey( key, this, collectionName);
      parameterMap.put( key, parameter);
    }
    return parameter;
  } // end getParameter


  /**
   * <code>getPredicate</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwPredicate</code> - 
   */
  public PwPredicate getPredicate( String key) {
    PwPredicate predicate = (PwPredicate) predicateMap.get( key);
    if (predicate == null) {
      predicate = XmlDBeXist.queryPredicateByKey( key, this, collectionName);
      predicateMap.put( key, predicate);
    }
    return predicate;
  } // end getPredicate


  /**
   * <code>getTokenRelation</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwTokenRelation</code> - 
   */
  public PwTokenRelation getTokenRelation( String key) {
    PwTokenRelation tokenRelation = (PwTokenRelation) tokenRelationMap.get( key);
    if (tokenRelation == null) {
      tokenRelation = XmlDBeXist.queryTokenRelationByKey( key, this, collectionName);
      tokenRelationMap.put( key, tokenRelation);
    }
    return tokenRelation;
  } // end getTokenRelation


  /**
   * <code>getVariable</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getVariable( String key) {
    PwVariable variable = (PwVariable) variableMap.get( key);
    // System.err.println( "getVariable: key  " + key + " variable " + variable);
    if (variable == null) {
      variable = XmlDBeXist.queryVariableByKey( key, this, collectionName);
      variableMap.put( key, variable);
    }
    return variable;
  } // end getVariable


} // end class PwPartialPlanImpl
