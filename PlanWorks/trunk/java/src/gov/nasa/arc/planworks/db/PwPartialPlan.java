// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPartialPlan.java,v 1.17 2003-09-23 21:53:36 miatauro Exp $
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

  /**
   * <code>getId</code>
   *
   * @return - <code>Long</code> -
   */

  public abstract Long getId();

  /**
   * <code>getObjectList</code>
   *
   * @return - <code>List</code> - of PwObject
   */
  public abstract List getObjectList();

  /**
   * <code>getFreeTokenList</code>
   *
   * @return - <code>List</code> - of PwToken
   */

  public abstract List getFreeTokenList();

  /**
   * <code>getObject</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwObject</code> - 
   */
  public abstract PwObject getObject( Integer id);


  /**
   * <code>getTimeline</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwTimeline</code> - 
   */
  public abstract PwTimeline getTimeline( Integer id);


  /**
   * <code>getSlot</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwSlot</code> - 
   */
  public abstract PwSlot getSlot( Integer id);


  /**
   * <code>getToken</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwToken</code> - 
   */
  public abstract PwToken getToken( Integer id);


  /**
   * <code>getConstraint</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwConstraint</code> - 
   */
  public abstract PwConstraint getConstraint( Integer id);


  /**
   * <code>getParameter</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwParameter</code> - 
   */
  public abstract PwParameter getParameter( Integer id);


  /**
   * <code>getPredicate</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwPredicate</code> - 
   */
  public abstract PwPredicate getPredicate( Integer id);


  /**
   * <code>getTokenRelation</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwTokenRelation</code> - 
   */
  public abstract PwTokenRelation getTokenRelation( Integer id);


  /**
   * <code>getVariable</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwVariable</code> - 
   */
  public abstract PwVariable getVariable( Integer id);

  public abstract void setContentSpec(List spec);

  public abstract List getContentSpec();
  
} // end interface PwPartialPlan
