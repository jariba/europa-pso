// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTransactionImpl.java,v 1.3 2003-10-02 23:24:21 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.arc.planworks.db.PwTransaction;


/**
 * <code>PwTransactionImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwTransactionImpl implements PwTransaction {

  private String type;
  private Integer transactionId;
  private String source;
  private Integer objectId;
  private Integer stepNumber;
  private Long sequenceId;
  private Long partialPlanId;

  public PwTransactionImpl( String type, Integer transactionId, String source,
                            Integer objectId, Integer stepNumber, Long sequenceId,
                            Long partialPlanId) {
    this.type = type;
    this.transactionId = transactionId;
    this.source = source;
    this.objectId = objectId;
    this.stepNumber = stepNumber;
    this.sequenceId = sequenceId;
    this.partialPlanId = partialPlanId;
  } // end constructor


  /**
   * <code>getType</code>
   *
   * @return - <code>String</code> - 
   */
  public String getType() {
    return type;
  }

  /**
   * <code>getId</code>
   *
   * @return - <code>Integer</code> - transaction id
   */
  public Integer getId() {
    return transactionId;
  }

  /**
   * <code>getSource</code> - one of PwTransaction.SOURCE_USER/SOURCE_SYSTEM/SOURCE_UNKNOWN
   *
   * @return - <code>String</code> - 
   */
  public String getSource() {
    return source;
  }

  /**
   * <code>getObjectId</code> - id of object acted on by this transaction
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getObjectId() {
    return objectId;
  }

  /**
   * <code>getStepNumber</code> - step number of sequence in which transaction occurred
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getStepNumber() {
    return stepNumber;
  }

  /**
   * <code>getSequenceId</code> - id of sequence of object acted on by this transaction
   *
   * @return - <code>Long</code> - 
   */
  public Long getSequenceId() {
    return sequenceId;
  }

  /**
   * <code>getPartialPlanId</code>
   *
   * @return - <code>Long</code> - id of partial plan of object acted on by this transaction
   */
  public Long getPartialPlanId() {
    return partialPlanId;
  }


} // end class PwTransactionImpl
