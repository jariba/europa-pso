// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwVariableQueryImpl.java,v 1.1 2003-12-20 01:54:48 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18dec03
//

package gov.nasa.arc.planworks.db.impl;

import gov.nasa.arc.planworks.db.PwVariableQuery;


/**
 * <code>PwVariableQueryImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwVariableQueryImpl implements PwVariableQuery {

  private Integer id;
  private String variableName;
  private Integer stepNumber;
  private Long sequenceId;
  private Long partialPlanId;
  private boolean isUnbound;

  /**
   * <code>PwVariableQueryImpl</code> - constructor 
   *
   * @param id - <code>Integer</code> - 
   * @param stepNumber - <code>Integer</code> - 
   * @param sequenceId - <code>Long</code> - 
   * @param partialPlanId - <code>Long</code> - 
   */
  public PwVariableQueryImpl( Integer id, String variableName, Integer stepNumber,
                                Long sequenceId, Long partialPlanId, boolean isUnbound) {
    this.id = id;
    this.variableName = variableName;
    this.stepNumber = stepNumber;
    this.sequenceId = sequenceId;
    this.partialPlanId = partialPlanId;
    this.isUnbound = isUnbound;
  } // end constructor

  /**
   * <code>getId</code>
   *
   * @return - <code>Integer</code> - variable id
   */
  public Integer getId() {
    return id;
  }

  /**
   * <code>getName</code>
   *
   * @return - <code>String</code> - variable name
   */
  public String getName() {
    return variableName;
  }

  /**
   * <code>getStepNumber</code> - step number containing this variable
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getStepNumber() {
    return stepNumber;
  }

  /**
   * <code>getSequenceId</code> - id of sequence containing this variable
   *
   * @return - <code>Long</code> - 
   */
  public Long getSequenceId() {
    return sequenceId;
  }

  /**
   * <code>getPartialPlanId</code>
   *
   * @return - <code>Long</code> - id of partial plan containing this variable
   */
  public Long getPartialPlanId() {
    return partialPlanId;
  }

  /**
   * <code>isUnbound</code> - is this variable unbound
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isUnbound() {
    return isUnbound;
  }

} // end interface PwVariableQueryImpl
