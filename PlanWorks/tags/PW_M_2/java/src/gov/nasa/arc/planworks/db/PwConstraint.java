// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwConstraint.java,v 1.3 2003-06-11 01:02:11 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;


/**
 * <code>PwConstraint</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwConstraint {



  /**
   * <code>getName</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getName();

  /**
   * <code>getKey</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getKey();
	
  /**
   * <code>getType</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getType();
	
  /**
   * <code>getVariablesList</code>
   *
   * @return - <code>List</code> - of PwVariable
   */
  public abstract List getVariablesList();

} // end interface PwConstraint
