// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwDomainImpl.java,v 1.4 2003-10-18 00:01:07 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import gov.nasa.arc.planworks.db.PwDomain;


/**
 * <code>PwDomainImpl</code> - super class for PwEnumeratedDomainImpl &
 *                             PwIntervalDomainImpl
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwDomainImpl implements PwDomain {

  public PwDomainImpl() {
  } // end constructor

  /**
   * <code>getLowerBound</code>
   *
   * @return - <code>String</code> - 
   */
  public String getLowerBound() {
    return "";
  }

  /**
   * <code>getUpperBound</code>
   *
   * @return - <code>String</code> - 
   */
  public String getUpperBound() {
    return "";
  }
		
  /**
   * <code>getLowerBoundInt</code> - 
   *
   * @return - <code>int</code> - 
   */
  public int getLowerBoundInt() {
    return -1;
  }

  /**
   * <code>getUpperBoundInt</code> - 
   *
   * @return - <code>int</code> - 
   */
  public int getUpperBoundInt() {
    return -1;
  }

  public boolean isSingleton() {
    return false;
  }
		
  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public String toString() {
    return "";
  }


} // end class PwDomainImpl
