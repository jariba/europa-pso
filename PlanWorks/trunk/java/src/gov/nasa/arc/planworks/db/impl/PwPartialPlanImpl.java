// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPartialPlanImpl.java,v 1.4 2003-05-16 19:19:46 taylor Exp $
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
    objectMap = new HashMap();
    timelineMap = new HashMap();
    slotMap = new HashMap();
    tokenMap = new HashMap();
    constraintMap = new HashMap();
    parameterMap = new HashMap(); 
    predicateMap = new HashMap();
    tokenRelationMap = new HashMap(); 
    variableMap = new HashMap(); 
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
         objectMap.put( key, 
                        new PwObjectImpl( (String) objectNameAndKeyList.get( i),
                                          (String) objectNameAndKeyList.get( i)));
      }
      XmlDBeXist.INSTANCE.createTimelineSlotTokenNodesStructure( this, collectionName);
 
      // constraintKeyList = XmlDBeXist.INSTANCE.fillElementMap( "constraint",
    }
  } // end createPartialPlan


//   private final void fillElementMaps() {
//     constraintKeyList =
//       queryAttributeValueOfElements( constraintQuery + CONSTRAINT_KEY_ATTRIBUTE,
//                                      CONSTRAINT_KEY_ATTRIBUTE, collectionName);
//     Iterator keysIterator = constraintKeyList.iterator();
//     while (keysIterator.hasNext()) {
//       getConstraint( (String) keysIterator.next(), collectionName);
//     }

//     parameterKeyList =
//       queryAttributeValueOfElements( parameterQuery + PARAMETER_KEY_ATTRIBUTE,
//                                      PARAMETER_KEY_ATTRIBUTE, collectionName);
//     keysIterator = parameterKeyList.iterator();
//     while (keysIterator.hasNext()) {
//       getParameter( (String) keysIterator.next(), collectionName);
//     }

//     predicateKeyList =
//       queryAttributeValueOfElements( predicateQuery + PREDICATE_KEY_ATTRIBUTE,
//                                      PREDICATE_KEY_ATTRIBUTE, collectionName);
//     keysIterator = predicateKeyList.iterator();
//     while (keysIterator.hasNext()) {
//       getPredicate( (String) keysIterator.next(), collectionName);
//     }

//     tokenRelationKeyList =
//       queryAttributeValueOfElements( tokenRelationQuery + TOKEN_RELATION_KEY_ATTRIBUTE,
//                                      TOKEN_RELATION_KEY_ATTRIBUTE, collectionName);
//     keysIterator = tokenRelationKeyList.iterator();
//     while (keysIterator.hasNext()) {
//       getTokenRelation( (String) keysIterator.next(), collectionName);
//     }

//     variableKeyList =
//       queryAttributeValueOfElements( variableQuery + VARIABLE_KEY_ATTRIBUTE,
//                                      VARIABLE_KEY_ATTRIBUTE, collectionName);
//     keysIterator = variableKeyList.iterator();
//     while (keysIterator.hasNext()) {
//       getVariable( (String) keysIterator.next(), collectionName);
//     }
//   } // end fillElementMaps


  /**
   * <code>getObject</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwObjectImpl</code> - 
   */
  public PwObjectImpl getObject( String key, String collectionName) {
    PwObjectImpl object = (PwObjectImpl) objectMap.get( key);
    if (object == null) {
//       object = XmlDBeXist.queryObject( key, collectionName);
      objectMap.put( key, object);
    }
    return object;
  } // end getObject


  /**
   * <code>getTimeline</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwTimelineImpl</code> - 
   */
  public PwTimelineImpl getTimeline( String key, String collectionName) {
    PwTimelineImpl timeline = (PwTimelineImpl) timelineMap.get( key);
    if (timeline == null) {
//       timeline = XmlDBeXist.queryTimeline( key, collectionName);
      timelineMap.put( key, timeline);
    }
    return timeline;
  } // end getTimeline


  /**
   * <code>getSlot</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwSlotImpl</code> - 
   */
  public PwSlotImpl getSlot( String key, String collectionName) {
    PwSlotImpl slot = (PwSlotImpl) slotMap.get( key);
    if (slot == null) {
//       slot = XmlDBeXist.querySlot( key, collectionName);
      slotMap.put( key, slot);
    }
    return slot;
  } // end getSlot


  /**
   * <code>getToken</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwTokenImpl</code> - 
   */
  public PwTokenImpl getToken( String key, String collectionName) {
    PwTokenImpl token = (PwTokenImpl) tokenMap.get( key);
    if (token == null) {
//       token = XmlDBeXist.queryToken( key, collectionName);
      tokenMap.put( key, token);
    }
    return token;
  } // end getToken


  /**
   * <code>getConstraint</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwConstraintImpl</code> - 
   */
  public PwConstraintImpl getConstraint( String key, String collectionName) {
    PwConstraintImpl constraint = (PwConstraintImpl) constraintMap.get( key);
    if (constraint == null) {
//       constraint = XmlDBeXist.queryConstraint( key, collectionName);
      constraintMap.put( key, constraint);
    }
    return constraint;
  } // end getConstraint


  /**
   * <code>getParameter</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwParameterImpl</code> - 
   */
  public PwParameterImpl getParameter( String key, String collectionName) {
    PwParameterImpl parameter = (PwParameterImpl) parameterMap.get( key);
    if (parameter == null) {
//       parameter = XmlDBeXist.queryParameter( key, collectionName);
      parameterMap.put( key, parameter);
    }
    return parameter;
  } // end getParameter


  /**
   * <code>getPredicate</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwPredicateImpl</code> - 
   */
  public PwPredicateImpl getPredicate( String key, String collectionName) {
    PwPredicateImpl predicate = (PwPredicateImpl) predicateMap.get( key);
    if (predicate == null) {
      predicate = XmlDBeXist.queryPredicate( key, collectionName);
      predicateMap.put( key, predicate);
    }
    return predicate;
  } // end getPredicate


  /**
   * <code>getTokenRelation</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwTokenRelationImpl</code> - 
   */
  public PwTokenRelationImpl getTokenRelation( String key, String collectionName) {
    PwTokenRelationImpl tokenRelation = (PwTokenRelationImpl) tokenRelationMap.get( key);
    if (tokenRelation == null) {
//       tokenRelation = XmlDBeXist.queryTokenRelation( key, collectionName);
      tokenRelationMap.put( key, tokenRelation);
    }
    return tokenRelation;
  } // end getTokenRelation


  /**
   * <code>getVariable</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwVariableImpl</code> - 
   */
  public PwVariableImpl getVariable( String key, String collectionName) {
    PwVariableImpl variable = (PwVariableImpl) variableMap.get( key);
    // System.err.println( "getVariable: key  " + key + " variable " + variable);
    if (variable == null) {
      variable = XmlDBeXist.queryVariable( key, collectionName);
      variableMap.put( key, variable);
    }
    return variable;
  } // end getVariable


} // end class PwPartialPlanImpl
