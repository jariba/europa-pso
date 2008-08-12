// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTokenQuery.java,v 1.1 2003-12-20 01:54:47 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 17dec03
//

package gov.nasa.arc.planworks.db;


/**
 * <code>PwTokenQuery</code> - token created from Sequence Queries
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwTokenQuery {

  /**
   * <code>getId</code>
   *
   * @return - <code>Integer</code> - token id
   */
  public abstract Integer getId();

  /**
   * <code>getPredicateName</code>
   *
   * @return - <code>String</code> - token predicate name
   */
  public abstract String getPredicateName();

  /**
   * <code>getStepNumber</code> - step number containing this token
   *
   * @return - <code>Integer</code> - 
   */
  public abstract Integer getStepNumber();

  /**
   * <code>getSequenceId</code> - id of sequence containing this token
   *
   * @return - <code>Long</code> - 
   */
  public abstract Long getSequenceId();

  /**
   * <code>getPartialPlanId</code>
   *
   * @return - <code>Long</code> - id of partial plan containing this token
   */
  public abstract Long getPartialPlanId();

  /**
   * <code>isFreeToken</code> - is this a free token
   *
   * @return - <code>boolean</code> - 
   */
  public abstract boolean isFreeToken();

} // end interface PwTokenQuery
