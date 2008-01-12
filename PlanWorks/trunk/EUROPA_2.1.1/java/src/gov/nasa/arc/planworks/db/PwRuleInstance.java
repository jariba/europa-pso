// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwRuleInstance.java,v 1.2 2004-06-23 21:36:34 pdaley Exp $
//
// PlanWorks -- 
//
// Patrick Daley -- started 17May04
//

package gov.nasa.arc.planworks.db;

import java.util.List;


/**
 * <code>PwRuleInstance</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwRuleInstance extends PwVariableContainer {

  /**
   * <code>getId</code>
   *
   * @return name - <code>int</code> - RuleInstanceId
   */
  public abstract Integer getId();

  /**
   * <code>getRuleId</code>
   *
   * @return name - <code>int</code> -
   */
  public abstract Integer getRuleId();
	
  /**
   * <code>getMasterId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public abstract Integer getMasterId();

  /**
   * <code>getSlaveIdsList</code>
   *
   * @return - <code>List</code> - of Integer.  
   */
  public abstract List getSlaveIdsList();

  /**
   * <code>getRuleVarIdList</code>
   *
   * @return - <code>List</code> - of Integer.  
   */
  public abstract List getRuleVarIdList();

  /**
   * <code>getVariables</code> - return rule variables
   *
   * @return - <code>List</code> of PwVariable
   */
  public abstract List getVariables();

  /**
   * <code>getName</code>
   *
   * @return - <code>String</code> name of rule instance
   */
  public abstract String getName();


} // end interface PwRuleInstance
