// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwToken.java,v 1.20 2004-03-02 02:34:11 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db;

import java.awt.FontMetrics;
import java.util.List;


/**
 * <code>PwToken</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwToken extends PwVariableContainer {


  /**
   * <code>getId</code>
   *
   * @return name - <code>int</code> -
   */
  public abstract Integer getId();
	
  /**
   * <code>getPredicate</code>
   *
   * @return - <code>PwPredicate</code> - 
   */
  //public abstract PwPredicate getPredicate();

  public abstract String getPredicateName();

  /**
   * <code>getStartVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public abstract PwVariable getStartVariable();
		
  /**
   * <code>getEndVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public abstract PwVariable getEndVariable();

  /**
   * <code>getDurationVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public abstract PwVariable getDurationVariable();

  /**
   * <code>getObjectVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public abstract PwVariable getObjectVariable();

  /**
   * <code>getRejectVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public abstract PwVariable getStateVariable();

  /**
   * <code>getTokenRelationIdsList</code>
   *
   * @return - <code>List</code> - of Integer.  Id for token relation establishing this token
   * as a slave.
   */
  public abstract List getTokenRelationIdsList();

  /**
   * <code>getVariablesList</code> - return TokenVars & ParamVars
   *
   * @return - <code>List</code> - of PwVariable
   */
  public abstract List getVariablesList();

  /**
   * <code>getTokenVarsList</code>
   *
   * @return - <code>List</code> - of PwVariable
   */
  public abstract List getTokenVarsList();

  /**
   * <code>getParamVarsList</code>
   *
   * @return - <code>List</code> - of PwVariable
   */
  public abstract List getParamVarsList();

  /**
   * <code>getSlotId</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract Integer getSlotId();

  /**
   * <code>isFree</code> - not attached to an object
   *
   * @return - <code>boolean</code> - 
   */
  public abstract boolean isFree();

  /**
   * <code>isSlotted</code> - in a slot in a timeline
   *
   * @return - <code>boolean</code> - 
   */
  public abstract boolean isSlotted();

  /**
   * <code>isBaseToken</code> - is slotted and the base token
   *
   * @return - <code>boolean</code> - 
   */
  public abstract boolean isBaseToken();

  /**
   * <code>getTimelineId</code>
   *
   * @return - <code>Integer</code> -
   */

  public abstract Integer getParentId();

  /**
   * <code>getEarliestStart</code>
   *
   * @return - <code>Integer</code> -
   */

  public abstract Integer getEarliestStart();
  
  /**
   * <code>getLatestEnd</code>
   *
   * @return - <code>Integer</code> -
   */

  public abstract Integer getLatestEnd();

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String toString();

} // end interface PwToken
