// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPartialPlanImpl.java,v 1.3 2003-05-15 22:16:23 taylor Exp $
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
  private String key;
  private List objectList; // element PwObjectImpl
  private Map variableHashMap;
  private Map predicateHashMap;

  /**
   * <code>PwPartialPlanImpl</code> - constructor 
   *     retrieves partial plan from XML:DB and
   *     builds Java data structure
   * @param projectCollectionName - <code>String</code> - 
   * @param sequenceCollectionName - <code>String</code> - 
   */
  public PwPartialPlanImpl( String url, String projectCollectionName,
                            String sequenceCollectionName)
    throws ResourceNotFoundException {
    objectList = new ArrayList();
    variableHashMap = new HashMap(); // key = key attribute, value = Variable instance
    predicateHashMap = new HashMap(); // key = key attribute, value = Predicate instance
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

    System.err.println( "Load " + url);
    long startLoadTimeMSecs = (new Date()).getTime();

    XmlDBeXist.INSTANCE.addXMLFileToCollection( collectionName, url);

    long stopLoadTimeMSecs = (new Date()).getTime();
    String loadTimeString = "   ... elapsed time: " +
      (stopLoadTimeMSecs - startLoadTimeMSecs) + " msecs.";
    System.err.println( loadTimeString);

    createPartialPlan( collectionName);

  } // end constructor


  /**
   * <code>getObjectList</code>
   *
   * @return - <code>List of PwObject</code> - 
   */
  public List getObjectList() {
    return objectList;
  }


  private void createPartialPlan( String collectionName) {
    List partialPlanKeys = XmlDBeXist.INSTANCE.getPartialPlanKeys( collectionName);
    // should only be one with collection structure of
    // /db/wtaylor/test/monkey/step000
    Iterator keysIterator = partialPlanKeys.iterator();
    while (keysIterator.hasNext()) {
      String partialPlanKey = (String) keysIterator.next();
      key = partialPlanKey;
      System.err.println( "partialPlan key " + partialPlanKey);
      model = XmlDBeXist.INSTANCE.getPartialPlanModelByKey( partialPlanKey,
                                                            collectionName);
      List objectNameAndKeyList = XmlDBeXist.INSTANCE.getPartialPlanObjectsByKey
        ( partialPlanKey, collectionName);
      for (int i = 0, n = objectNameAndKeyList.size(); i < n; i++) {
        objectList.add( new PwObjectImpl( (String) objectNameAndKeyList.get( i),
                                          (String) objectNameAndKeyList.get( i)));
      }
      XmlDBeXist.INSTANCE.createTimelineSlotTokenNodesStructure( this, collectionName);
 
  
    }
  } // end createPartialPlan


  /**
   * <code>getVariableHashMap</code>
   *
   * @return variableHashMap - <code>Map</code> - 
   */
  public Map getVariableHashMap() {
    return variableHashMap;
  }


  /**
   * <code>getVariable</code> - if not in HashMap, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwVariableImpl</code> - 
   */
  public PwVariableImpl getVariable( String key, String collectionName) {
    PwVariableImpl variable = (PwVariableImpl) variableHashMap.get( key);
    // System.err.println( "getVariable: key  " + key + " variable " + variable);
    if (variable == null) {
      variable = XmlDBeXist.queryVariable( key, collectionName);
      variableHashMap.put( key, variable);
    }
    return variable;
  } // end getVariable


  /**
   * <code>getPredicateHashMap</code>
   *
   * @return predicateHashMap - <code>Map</code> - 
   */
  public Map getPredicateHashMap() {
    return predicateHashMap;
  }


  /**
   * <code>getPredicate</code> - if not in HashMap, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwPredicateImpl</code> - 
   */
  public PwPredicateImpl getPredicate( String key, String collectionName) {
    PwPredicateImpl predicate = (PwPredicateImpl) predicateHashMap.get( key);
    if (predicate == null) {
      predicate = XmlDBeXist.queryPredicate( key, collectionName);
      predicateHashMap.put( key, predicate);
    }
    return predicate;
  } // end getPredicate


} // end class PwPartialPlanImpl
