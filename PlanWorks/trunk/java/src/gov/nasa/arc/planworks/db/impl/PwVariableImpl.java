// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwVariableImpl.java,v 1.12 2003-08-20 23:34:17 miatauro Exp $
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

  private Integer id;
  private String type;
  private List constraintIdList; // element String
  private List parameterIdList;
  private List tokenIdList;
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
  public PwVariableImpl( Integer id, String type, List constraintIds, List parameterIds,
                         List tokenIds, PwDomainImpl domain, PwPartialPlanImpl partialPlan) {
    this.id = id;
    this.type = type;
    this.constraintIdList = new ArrayList(constraintIds);
    this.parameterIdList = new ArrayList(parameterIds);
    this.tokenIdList = new ArrayList(tokenIds);
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

  /**
   * <code>getTokenList</code>
   *
   * @return - <code>List</code> - of PwToken
   */
  public List getTokenList() {
    List retval = new ArrayList(tokenIdList.size());
    for(int i = 0; i < tokenIdList.size(); i++) {
      retval.add(partialPlan.getToken((Integer) tokenIdList.get(i)));
    }
    return retval;
  }

  public void removeConstraint(Integer constraintId) {
    constraintIdList.remove(constraintId);
  }

} // end class PwVariableImpl
