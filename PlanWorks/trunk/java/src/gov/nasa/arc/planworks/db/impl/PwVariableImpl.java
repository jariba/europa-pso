// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwVariableImpl.java,v 1.24 2004-05-04 01:27:13 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.util.UniqueSet;

/**
 * <code>PwVariableImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 * NOTE: For the moment, the assumption is made (due to a EUROPA design decision) that a
 * variable is on at most one token (globals obviously have no tokens).  Should you be using this
 * in a non-EUROPA context, then certain changes will have to be made to account for that, but the
 * general form of this data structure doesn't disallow the possibility.
 */
public class PwVariableImpl implements PwVariable {

  private Integer id;
  private Integer parentId;
  private String type;
  private UniqueSet constraintIdList; // element String
  private UniqueSet parameterNameList;
  private PwDomainImpl domain; // PwEnumeratedDomainImpl || PwIntervalDomainImpl
  private PwPartialPlanImpl partialPlan;


  /**
   * <code>PwVariableImpl</code> - constructor 
   *
   * @param id - <code>Integer</code> - 
   * @param type - <code>String</code> - 
   * @param constraintIdList - <code>List</code> - 
   * @param paramId - <code>Integer</code> - 
   * @param domain - <code>PwDomainImpl</code> - PwEnumeratedDomainImpl || PwIntervalDomainImpl
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwVariableImpl( final Integer id, final String type, final List constraintIds, 
                         final List parameterNames, final Integer parentId, 
                         final PwDomainImpl domain, final PwPartialPlanImpl partialPlan) {
    this.id = id;
    this.type = type;
    this.constraintIdList = new UniqueSet(constraintIds);
    this.parameterNameList = new UniqueSet(parameterNames);
    this.parentId = parentId;
    this.domain = domain;
    this.partialPlan = partialPlan;
  } // end constructor

  public PwVariableImpl( final Integer id, final String type, final Integer parentId,
                         final PwDomainImpl domain, final PwPartialPlanImpl partialPlan) {
    this.id = id;
    this.type = type;
    this.constraintIdList = new UniqueSet();
    this.parameterNameList = new UniqueSet();
    this.parentId = parentId;
    this.domain = domain;
    this.partialPlan = partialPlan;
  } // end constructor

  /**
   * <code>getId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getId()  {
    return this.id;
  }

  /**
   * <code>getType</code>
   *
   * @return - <code>String</code> - 
   */
  public String getType() {
    return this.type;
  }

  /**
   * <code>getDomain</code>
   *
   * @return - <code>PwDomain</code> - 
   */
  public PwDomain getDomain()  {
    return this.domain;
  }


  public List getParameterNameList() {
    return new ArrayList(parameterNameList);
  }

  /**
   * <code>getConstraintList</code>
   *
   * @return - <code>List</code> - of PwConstraint
   */
  public List getConstraintList() {
    return partialPlan.getConstraintList(constraintIdList);
  }

  public PwVariableContainer getParent() {
    PwVariableContainer retval = (PwVariableContainer) partialPlan.getToken(parentId);
    if(retval == null) {
      retval = (PwVariableContainer) partialPlan.getObject(parentId);
    }
    return retval;
  }

  public void removeConstraint(final Integer constraintId) {
    constraintIdList.remove(constraintId);
  }

  public void addConstraint(final Integer constraintId) {
    constraintIdList.add(constraintId);
  }

  public void addParameter(final String paramName) {
    parameterNameList.add(paramName);
  }

  public void setParent(final Integer parentId) {
    this.parentId = parentId;
  }

  public String toOutputString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(partialPlan.getId()).append("\t").append(parentId).append("\t");
    retval.append(parameterNameList.get(0)).append("\t");
    if(domain instanceof PwEnumeratedDomainImpl) {
      retval.append(DbConstants.ENUMERATED_DOMAIN).append("\t");
      PwEnumeratedDomainImpl eDom = (PwEnumeratedDomainImpl) domain;
      for(ListIterator it = eDom.getEnumeration().listIterator(); it.hasNext();) {
        retval.append(it.next()).append(" ");
      }
      retval.append("\t\\N\t\\N\t\\N\t");
    }
    else if(domain instanceof PwIntervalDomainImpl) {
      PwIntervalDomainImpl iDom = (PwIntervalDomainImpl) domain;
      retval.append(DbConstants.INTERVAL_DOMAIN).append("\t\\N\t").append(iDom.getType()).append("\t");
      retval.append(iDom.getLowerBound()).append("\t").append(iDom.getUpperBound()).append("\t");
    }
    retval.append(type).append("\n");
    return retval.toString();
  }

} // end class PwVariableImpl
