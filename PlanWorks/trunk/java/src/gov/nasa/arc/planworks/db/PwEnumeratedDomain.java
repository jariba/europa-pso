// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwEnumeratedDomain.java,v 1.5 2003-08-26 01:37:10 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;


/**
 * <code>PwEnumeratedDomain</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwEnumeratedDomain {

  /**
   * <code>getEnumeration</code>
   *
   * @return - <code>List</code> - of String
   */
  public abstract List getEnumeration();

  /**
   * <code>getLowerBound</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getLowerBound();

  /**
   * <code>getUpperBound</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getUpperBound();
		
  /**
   * <code>getLowerBoundInt</code> - required because PwIntervalDomainImpl needs it
   *
   * @return - <code>int</code> - 
   */
  public abstract int getLowerBoundInt();

  /**
   * <code>getUpperBoundInt</code> - required because PwIntervalDomainImpl needs it
   *
   * @return - <code>int</code> - 
   */
  public abstract int getUpperBoundInt();
		
  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String toString();


} // end interface PwEnumeratedDomain
