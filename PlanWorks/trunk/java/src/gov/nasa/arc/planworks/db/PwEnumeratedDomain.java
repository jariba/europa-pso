// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwEnumeratedDomain.java,v 1.6 2004-03-23 18:20:00 miatauro Exp $
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
public interface PwEnumeratedDomain extends PwDomain {

  /**
   * <code>getEnumeration</code>
   *
   * @return - <code>List</code> - of String
   */
  public abstract List getEnumeration();

} // end interface PwEnumeratedDomain
