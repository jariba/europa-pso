// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwVariableQuery.java,v 1.1 2003-12-20 01:54:48 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18dec03
//

package gov.nasa.arc.planworks.db;


/**
 * <code>PwVariableQuery</code> - variable created from Sequence Query
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwVariableQuery {

  /**
   * <code>getId</code>
   *
   * @return - <code>Integer</code> - variable id
   */
  public abstract Integer getId();

  /**
   * <code>getName</code>
   *
   * @return - <code>String</code> - variable name
   */
  public abstract String getName();

  /**
   * <code>getStepNumber</code> - step number containing this variable
   *
   * @return - <code>Integer</code> - 
   */
  public abstract Integer getStepNumber();

  /**
   * <code>getSequenceId</code> - id of sequence containing this variable
   *
   * @return - <code>Long</code> - 
   */
  public abstract Long getSequenceId();

  /**
   * <code>getPartialPlanId</code>
   *
   * @return - <code>Long</code> - id of partial plan containing this variable
   */
  public abstract Long getPartialPlanId();

  
  /**
   * <code>isUnbound</code> - is this variable unbound
   *
   * @return - <code>boolean</code> - 
   */
  public abstract boolean isUnbound();
  

} // end interface PwVariableQuery
