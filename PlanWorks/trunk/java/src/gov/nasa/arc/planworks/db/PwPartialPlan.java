// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPartialPlan.java,v 1.3 2003-05-18 00:02:24 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;


/**
 * <code>PwPartialPlan</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwPartialPlan {

  // constructor retrieves partial plan from XML:DB and
  // builds Java data structure

  /**
   * <code>getObjectIdList</code>
   *
   * @return - <code>List</code> - of String
   */
  public List getObjectIdList();


  /**
   * <code>getObject</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwObject</code> - 
   */
  public PwObject getObject( String key, String collectionName);


  /**
   * <code>getTimeline</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwTimeline</code> - 
   */
  public PwTimeline getTimeline( String key, String collectionName);


  /**
   * <code>getSlot</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwSlot</code> - 
   */
  public PwSlot getSlot( String key, String collectionName);


  /**
   * <code>getToken</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwToken</code> - 
   */
  public PwToken getToken( String key, String collectionName);


  /**
   * <code>getConstraint</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwConstraint</code> - 
   */
  public PwConstraint getConstraint( String key, String collectionName);


  /**
   * <code>getParameter</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwParameter</code> - 
   */
  public PwParameter getParameter( String key, String collectionName);


  /**
   * <code>getPredicate</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwPredicate</code> - 
   */
  public PwPredicate getPredicate( String key, String collectionName);


  /**
   * <code>getTokenRelation</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwTokenRelation</code> - 
   */
  public PwTokenRelation getTokenRelation( String key, String collectionName);


  /**
   * <code>getVariable</code> - if not in Map, query
   *
   * @param key - <code>String</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getVariable( String key, String collectionName);

} // end interface PwPartialPlan
