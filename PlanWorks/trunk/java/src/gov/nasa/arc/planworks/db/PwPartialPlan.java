// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPartialPlan.java,v 1.46 2004-09-30 22:02:31 miatauro Exp $
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
   * <code>isDummyPartialPlan</code>
   *
   * @return - <code>boolean</code> - 
   */
  public abstract boolean isDummyPartialPlan();

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

  public abstract List getTimelineList();

  public abstract List getSlotList();

  public abstract List getRuleInstanceList();

  /**
   * <code>getFreeTokenList</code>
   *
   * @return - <code>List</code> - of PwToken
   */
  public abstract List getFreeTokenList();

  public abstract List getSlottedTokenList();

  public abstract List getTokenList();

  public abstract List getResTransactionList();

  public abstract List getVariableList();

  public abstract List getConstraintList();

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
   * <code>getRuleInstance</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwRuleInstance</code> - 
   */
  public abstract PwRuleInstance getRuleInstance( final Integer id);


  /**
   * <code>getVariable</code> - 
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwVariable</code> - 
   */
  public abstract PwVariable getVariable( final Integer id);

  /**
   * <code>getResource</code>
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwResource</code> - 
   */
  public abstract PwResource getResource(final Integer id);

  /**
   * <code>getResourceTransaction</code>
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>PwResourceTransaction</code> - 
   */
  public abstract PwResourceTransaction getResourceTransaction(final Integer id);

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
   * <code>getSequence</code>
   *
   * @return - <code>PwPlanningSequence</code> - 
   */
  public abstract PwPlanningSequence getSequence();
  
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

  public abstract PwRule getRule(Integer rId);

  public abstract PwEntity getEntity(final Integer id);

  public abstract String getVariableParentName( final Integer parentId);

  public abstract List getPath(final Integer sKey, final Integer eKey, final List classes);
  
  public abstract List getPath(final Integer sKey, final Integer eKey, final List classes,
                               final int maxLength);
  
  public abstract List getPath(final Integer sKey, final Integer eKey, final List classes,
                               final List linkTypes, final int maxLength);
  
  public abstract boolean pathExists(final PwEntity sKey, final Integer eKey, final List classes);

  public abstract boolean pathExists(final PwEntity sKey, final Integer eKey, final List classes,
                                     final List linkTypes);
  
} // end interface PwPartialPlan

