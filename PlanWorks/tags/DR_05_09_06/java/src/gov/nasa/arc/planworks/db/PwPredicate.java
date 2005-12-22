// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPredicate.java,v 1.8 2004-04-02 00:57:52 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;


/**
 * <code>PwPredicate</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwPredicate {

  /**
   * <code>getName</code>
   *
   * @return name - <code>String</code> -
   */
  public abstract String getName();

  /**
   * <code>getId</code>
   *
   * @return name - <code>Integer</code> -
   */
  public abstract Integer getId();
	
  /**
   * <code>getParameterList</code>
   *
   * @return - <code>List</code> - of PwParameter
   */
  public abstract List getParameterList();

} // end interface PwPredicate
