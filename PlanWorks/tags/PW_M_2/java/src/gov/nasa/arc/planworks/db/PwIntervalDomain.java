// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwIntervalDomain.java,v 1.3 2003-06-11 01:02:11 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;


/**
 * <code>PwIntervalDomain</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwIntervalDomain {

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
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String toString();


} // end interface PwIntervalDomain
