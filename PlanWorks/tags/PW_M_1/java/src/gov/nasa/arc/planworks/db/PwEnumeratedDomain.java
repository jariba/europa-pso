// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwEnumeratedDomain.java,v 1.2 2003-05-18 00:02:24 taylor Exp $
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
  public List getEnumeration();

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public String toString();


} // end interface PwEnumeratedDomain
