// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwResourceTransactionImpl.java,v 1.3 2004-03-02 21:45:16 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 27Jan04
//

package gov.nasa.arc.planworks.db.impl;

import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwIntervalDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.impl.PwIntervalDomainImpl;


/**
 * <code>PwResourceTransactionImpl</code> - Transaction on a resource -
 *                                          a member of a unique set
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwResourceTransactionImpl extends PwTokenImpl implements PwResourceTransaction {

  private double quantityMin;
  private double quantityMax;

  public PwResourceTransactionImpl(final Integer id, final boolean isValueToken, 
                                   final String predName, final Integer startVarId, 
                                   final Integer endVarId, final Integer durationVarId,
                                   final Integer stateVarId, final Integer objectVarId,
                                   final Integer parentId, final String tokenRelationIds,
                                   final String paramVarIds, final String transInfo, 
                                   final PwPartialPlanImpl partialPlan) {
    super(id, isValueToken, DbConstants.noId, predName, startVarId, endVarId, durationVarId, 
          stateVarId, objectVarId, parentId, tokenRelationIds, paramVarIds, null, partialPlan);
    StringTokenizer strTok = new StringTokenizer(transInfo, ",");
    quantityMin = Double.parseDouble(strTok.nextToken());
    quantityMax = Double.parseDouble(strTok.nextToken());
    partialPlan.addResourceTransaction(id, this);
  }

  /**
   * <code>getQuantityMin</code>
   *
   * @return - <code>double</code> - 
   */
  public double getQuantityMin() {
    return quantityMin;
  }

  /**
   * <code>getQuantityMax</code>
   *
   * @return - <code>double</code> - 
   */
  public double getQuantityMax() {
    return quantityMax;
  }

  public PwIntervalDomain getInterval() {
    String type = "INTEGER_SORT";
    PwIntervalDomain startDomain = (PwIntervalDomain) getStartVariable().getDomain();
    PwIntervalDomain endDomain = (PwIntervalDomain) getEndVariable().getDomain();
    return new PwIntervalDomainImpl(type, startDomain.getLowerBound(), endDomain.getUpperBound());
  }
} // end class PwResourceTransactionImpl
