// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTokenQueryImpl.java,v 1.1 2003-12-20 01:54:48 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 17dec03
//

package gov.nasa.arc.planworks.db.impl;

import gov.nasa.arc.planworks.db.PwTokenQuery;


/**
 * <code>PwTokenQueryImpl</code> - token created from Sequence Query
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwTokenQueryImpl implements PwTokenQuery {

  private Integer id;
  private String predicateName;
  private Integer stepNumber;
  private Long sequenceId;
  private Long partialPlanId;
  private boolean isFreeToken;

  /**
   * <code>PwTokenQueryImpl</code> - constructor 
   *
   * @param id - <code>Integer</code> - 
   * @param stepNumber - <code>Integer</code> - 
   * @param sequenceId - <code>Long</code> - 
   * @param partialPlanId - <code>Long</code> - 
   */
  public PwTokenQueryImpl( Integer id, String predicateName, Integer stepNumber,
                          Long sequenceId, Long partialPlanId, boolean isFreeToken) {
    this.id = id;
    this.predicateName = predicateName;
    this.stepNumber = stepNumber;
    this.sequenceId = sequenceId;
    this.partialPlanId = partialPlanId;
    this.isFreeToken = isFreeToken;
  } // end constructor

  /**
   * <code>getId</code>
   *
   * @return - <code>Integer</code> - token id
   */
  public Integer getId() {
    return id;
  }

  /**
   * <code>getPredicateName</code>
   *
   * @return - <code>String</code> - token predicate name
   */
  public String getPredicateName() {
    return predicateName;
  }

  /**
   * <code>getStepNumber</code> - step number containing this token
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getStepNumber() {
    return stepNumber;
  }

  /**
   * <code>getSequenceId</code> - id of sequence containing this token
   *
   * @return - <code>Long</code> - 
   */
  public Long getSequenceId() {
    return sequenceId;
  }

  /**
   * <code>getPartialPlanId</code>
   *
   * @return - <code>Long</code> - id of partial plan containing this token
   */
  public Long getPartialPlanId() {
    return partialPlanId;
  }

  /**
   * <code>isFreeToken</code> - is this a free token
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isFreeToken() {
    return isFreeToken;
  }
  
} // end interface PwTokenQueryImpl
