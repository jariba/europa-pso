// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwDBTransaction.java,v 1.4 2004-05-21 21:38:55 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09May03
//

package gov.nasa.arc.planworks.db;


/**
 * <code>PwDBTransaction</code> - plan data base transactions
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwDBTransaction extends PwEntity {

  /**
   * <code>getName</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getName();

  /**
   * <code>getId</code>
   *
   * @return - <code>Integer</code> - transaction id
   */
  public abstract Integer getId();

  /**
   * <code>getSource</code> - one of SOURCE_USER/SOURCE_SYSTEM/SOURCE_UNKNOWN
   *
   * @return - <code>String</code> - 
   */
  public abstract String getSource();

  /**
   * <code>getEntityId</code> - id of entity acted on by this transaction
   *
   * @return - <code>Integer</code> - 
   */
  public abstract Integer getEntityId();

  /**
   * <code>getStepNumber</code> - step number of sequence in which transaction occurred
   *
   * @return - <code>Integer</code> - 
   */
  public abstract Integer getStepNumber();

  /**
   * <code>getSequenceId</code> - id of sequence of entity acted on by this transaction
   *
   * @return - <code>Long</code> - 
   */
  public abstract Long getSequenceId();

  /**
   * <code>getPartialPlanId</code>
   *
   * @return - <code>Long</code> - id of partial plan of entity acted on by this transaction
   */
  public abstract Long getPartialPlanId();

  public abstract String [] getInfo();

} // end interface PwDBTransaction
