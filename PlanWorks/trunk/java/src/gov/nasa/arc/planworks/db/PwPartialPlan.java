// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPartialPlan.java,v 1.31 2004-02-27 18:04:13 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;

import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;

/**
 * <code>PwPartialPlan</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwPartialPlan extends ViewableObject {

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

  public abstract List getResourceList();

  /**
   * <code>getFreeTokenList</code>
   *
   * @return - <code>List</code> - of PwToken
   */

  public abstract List getFreeTokenList();

  public abstract List getSlottedTokenList();

  public abstract List getTokenList();
  /**
   * <code>getObject</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwObject</code> - 
   */
  public abstract PwObject getObject( final Integer id);


  /**
   * <code>getTimeline</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwTimeline</code> - 
   */
  public abstract PwTimeline getTimeline( final Integer id);


  /**
   * <code>getSlot</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwSlot</code> - 
   */
  public abstract PwSlot getSlot( final Integer id);


  /**
   * <code>getToken</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwToken</code> - 
   */
  public abstract PwToken getToken( final Integer id);


  /**
   * <code>getConstraint</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwConstraint</code> - 
   */
  public abstract PwConstraint getConstraint( final Integer id);


  /**
   * <code>getParameter</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwParameter</code> - 
   */
  //public abstract PwParameter getParameter( Integer predId, Integer paramId);


  /**
   * <code>getPredicate</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwPredicate</code> - 
   */
  //public abstract PwPredicate getPredicate( Integer id);


  /**
   * <code>getTokenRelation</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwTokenRelation</code> - 
   */
  public abstract PwTokenRelation getTokenRelation( final Integer id);


  /**
   * <code>getVariable</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwVariable</code> - 
   */
  public abstract PwVariable getVariable( final Integer id);

  public abstract PwResourceTransaction getTransaction(final Integer id);

  // extend ViewableObject

  /**
   * <code>setContentSpec</code>
   *
   * @param spec - <code>List</code> - 
   */
  public abstract void setContentSpec( final List spec);

  /**
   * <code>getContentSpec</code>
   *
   * @return - <code>List</code> - 
   */
  public abstract List getContentSpec();

  /**
   * <code>setName</code> - sequenceDir/stepDir
   *
   * @param name - <code>String</code> - 
   */
  public abstract void setName( final String name);

  /**
   * <code>getName</code> - sequenceDir/stepDir
   *
   * @return - <code>String</code> 
   */
  public abstract String getName();
  
  // end extend ViewableObject

  /**
   * <code>getPartialPlanName</code> - stepDir
   *
   * @return - <code>String</code> - 
   */
  public abstract String getPartialPlanName();

  /**
   * <code>getPlanDBSize</code> - sum of hash map sizes of all plan objects
   *
   * @return - <code>int</code> - 
   */
  public abstract int getPlanDBSize();

  /**
   * <code>getStepNumber</code> - strip "step" prefix off stepDir and create int
   *
   * @return - <code>int</code> - 
   */
  public abstract int getStepNumber();

  /**
   * <code>getSequenceUrl</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getSequenceUrl();
  
  public abstract Integer getObjectIdByName(final String name);

  /**
   * <code>getMasterTokenId</code>
   *
   * @param tokenId - <code>Integer</code> - 
   * @return - <code>Integer</code> - 
   */
  public abstract Integer getMasterTokenId( final Integer tokenId);

  /**
   * <code>getSlaveTokenIds</code>
   *
   * @param tokenId - <code>Integer</code> - 
   * @return - <code>List</code> - of Integer
   */
  public abstract List getSlaveTokenIds( final Integer tokenId);


} // end interface PwPartialPlan

