// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwVariable.java,v 1.4 2003-06-26 18:17:47 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;


/**
 * <code>PwVariable</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwVariable {

  /**
   * <code>getKey</code>
   *
   * @return - <code>int</code> - 
   */
  public abstract Integer getKey();

  /**
   * <code>getDomain</code>
   *
   * @return - <code>PwDomain</code> - 
   */
  public abstract PwDomain getDomain();

  /**
   * <code>getParameter</code>
   *
   * @return - <code>PwParameter</code> - 
   */
  public abstract PwParameter getParameter();

  /**
   * <code>getConstraintList</code>
   *
   * @return - <code>List</code> - of PwConstraint
   */
  public abstract List getConstraintList();


} // end interface PwVariable
