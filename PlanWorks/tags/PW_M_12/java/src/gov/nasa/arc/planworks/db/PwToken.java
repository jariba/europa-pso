// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwToken.java,v 1.15 2004-01-14 21:22:11 miatauro Exp $
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
public interface PwToken {


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
  public abstract PwVariable getRejectVariable();

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
   * <code>isFreeToken</code>
   *
   * @return - <code>boolean</code> -
   */

  public abstract boolean isFreeToken();

  /**
   * <code>getTimelineId</code>
   *
   * @return - <code>Integer</code> -
   */

  public abstract Integer getTimelineId();

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

  /**
   * <code>addParamVar</code>
   *
   * @param paramVarId - <code>Integer</code> - 
   */
  public abstract void addParamVar(Integer paramVarId);
  
  /**
   * <code>addTokenRelation</code>
   *
   * @param tokenRelationId - <code>Integer</code> - 
   */
  public abstract void addTokenRelation(Integer tokenRelationId);

} // end interface PwToken
