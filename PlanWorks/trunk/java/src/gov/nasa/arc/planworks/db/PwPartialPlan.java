// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPartialPlan.java,v 1.12 2003-07-14 22:18:50 miatauro Exp $
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

  /**
   * <code>getUrl</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getUrl();

  public abstract int getMinKey();

  public abstract int getMaxKey();

  public abstract Long getKey();

  /**
   * <code>getObjectList</code>
   *
   * @return - <code>List</code> - of PwObject
   */
  public abstract List getObjectList();

  public abstract List getFreeTokenList();

  /**
   * <code>getObject</code> - if not in Map, query
   *
   * @param key - <code>Integer</code> - 
   * @return - <code>PwObject</code> - 
   */
  public abstract PwObject getObject( Integer key);


  /**
   * <code>getTimeline</code> - if not in Map, query
   *
   * @param key - <code>Integer</code> - 
   * @return - <code>PwTimeline</code> - 
   */
  public abstract PwTimeline getTimeline( Integer key);


  /**
   * <code>getSlot</code> - if not in Map, query
   *
   * @param key - <code>Integer</code> - 
   * @return - <code>PwSlot</code> - 
   */
  public abstract PwSlot getSlot( Integer key);


  /**
   * <code>getToken</code> - if not in Map, query
   *
   * @param key - <code>Integer</code> - 
   * @return - <code>PwToken</code> - 
   */
  public abstract PwToken getToken( Integer key);


  /**
   * <code>getConstraint</code> - if not in Map, query
   *
   * @param key - <code>Integer</code> - 
   * @return - <code>PwConstraint</code> - 
   */
  public abstract PwConstraint getConstraint( Integer key);


  /**
   * <code>getParameter</code> - if not in Map, query
   *
   * @param key - <code>Integer</code> - 
   * @return - <code>PwParameter</code> - 
   */
  public abstract PwParameter getParameter( Integer key);


  /**
   * <code>getPredicate</code> - if not in Map, query
   *
   * @param key - <code>Integer</code> - 
   * @return - <code>PwPredicate</code> - 
   */
  public abstract PwPredicate getPredicate( Integer key);


  /**
   * <code>getTokenRelation</code> - if not in Map, query
   *
   * @param key - <code>Integer</code> - 
   * @return - <code>PwTokenRelation</code> - 
   */
  public abstract PwTokenRelation getTokenRelation( Integer key);


  /**
   * <code>getVariable</code> - if not in Map, query
   *
   * @param key - <code>Integer</code> - 
   * @return - <code>PwVariable</code> - 
   */
  public abstract PwVariable getVariable( Integer key);

} // end interface PwPartialPlan
