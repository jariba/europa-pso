// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwResourceTransactionImpl.java,v 1.9 2004-08-21 00:31:52 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 27Jan04
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ListIterator;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwIntervalDomain;
import gov.nasa.arc.planworks.db.PwResourceTransaction;


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
                                   final Integer parentId, final Integer ruleInstanceId,
                                   final String paramVarIds, final String transInfo, 
                                   final PwPartialPlanImpl partialPlan) {
    super(id, isValueToken, DbConstants.NO_ID, predName, startVarId, endVarId, durationVarId, 
          stateVarId, objectVarId, parentId, ruleInstanceId, paramVarIds, null, partialPlan);
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
    String type = DbConstants.INTEGER_INTERVAL_DOMAIN_TYPE;
    PwIntervalDomain startDomain = (PwIntervalDomain) getStartVariable().getDomain();
    PwIntervalDomain endDomain = (PwIntervalDomain) getEndVariable().getDomain();
    return new PwIntervalDomainImpl(type, startDomain.getLowerBound(), endDomain.getUpperBound());
  }

  public String toOutputString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(DbConstants.T_TRANSACTION).append("\t");
    retval.append("\\N").append("\t").append("\\N").append("\t");
    retval.append( partialPlan.getId()).append("\t");
    
    if(isFree()) {
      retval.append("1").append("\t");
    }
    else {
      retval.append("0").append("\t");
    }
    if(isValueToken) {
      retval.append("1").append("\t");
    }
    else {
      retval.append("0").append("\t");
    }
    retval.append(startVarId).append("\t");
    // no endVarId
    retval.append(startVarId).append("\t");
    retval.append(durationVarId).append("\t");
    retval.append(stateVarId).append("\t");
    retval.append(predicateName).append("\t");
    retval.append(parentId).append("\t");
    retval.append(partialPlan.getObject(parentId).getName()).append("\t");
    retval.append(objectVarId).append("\t");
    if(!paramVarIds.isEmpty()) {
      for(ListIterator it = paramVarIds.listIterator(); it.hasNext();) {
        retval.append(it.next()).append(":");
      }
    }
    else {
      retval.append("\\N");
    }
    retval.append("\t");
    retval.append( (int) quantityMin).append(",").append( (int) quantityMax);
    retval.append("\n");
    return retval.toString();
  }


} // end class PwResourceTransactionImpl
