// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwVariableImpl.java,v 1.7 2003-07-02 18:30:05 miatauro Exp $
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

import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwParameter;
import gov.nasa.arc.planworks.db.PwVariable;


/**
 * <code>PwVariableImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwVariableImpl implements PwVariable {

  private Integer key;
  private String type;
  private List constraintIdList; // element String
  private List parameterIdList;
  private PwDomainImpl domain; // PwEnumeratedDomainImpl || PwIntervalDomainImpl
  private PwPartialPlanImpl partialPlan;


  /**
   * <code>PwVariableImpl</code> - constructor 
   *
   * @param key - <code>Integer</code> - 
   * @param type - <code>String</code> - 
   * @param constraintIdList - <code>List</code> - 
   * @param paramId - <code>Integer</code> - 
   * @param domain - <code>PwDomainImpl</code> - PwEnumeratedDomainImpl || PwIntervalDomainImpl
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwVariableImpl( Integer key, String type, List constraintIds, List parameterIds,
                         PwDomainImpl domain, PwPartialPlanImpl partialPlan) {
    this.key = key;
    this.type = type;
    this.constraintIdList = constraintIds;
    this.parameterIdList = parameterIds;
    this.domain = domain;
    this.partialPlan = partialPlan;
  } // end constructor


  /**
   * <code>getKey</code>
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getKey()  {
    return this.key;
  }

  /**
   * <code>getDomain</code>
   *
   * @return - <code>PwDomain</code> - 
   */
  public PwDomain getDomain()  {
    return this.domain;
  }

  /**
   * <code>getParameterList</code>
   *
   * @return - <code>List of PwParameter</code> - 
   */
  public List getParameterList() {
    List retval = new ArrayList(parameterIdList.size());
    ListIterator parameterIdIterator = parameterIdList.listIterator();
    while(parameterIdIterator.hasNext()) {
      retval.add(partialPlan.getParameter((Integer)parameterIdIterator.next()));
    }
    return retval;
  }

  /**
   * <code>getConstraintList</code>
   *
   * @return - <code>List</code> - of PwConstraint
   */
  public List getConstraintList() {
    List retval = new ArrayList( constraintIdList.size());
    for (int i = 0; i < constraintIdList.size(); i++) {
      retval.add( partialPlan.getConstraint( (Integer) constraintIdList.get( i)));
    }
    return retval;
  }


} // end class PwVariableImpl
