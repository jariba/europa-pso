// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwVariable.java,v 1.12 2004-02-25 02:30:13 taylor Exp $
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
public interface PwVariable extends PwEntity {

  /**
   * <code>getId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public abstract Integer getId();

  /**
   * <code>getType</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getType();

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
  //public abstract List getParameterList();

  public abstract List getParameterNameList();

  /**
   * <code>getConstraintList</code>
   *
   * @return - <code>List</code> - of PwConstraint
   */
  public abstract List getConstraintList();

  public abstract PwVariableContainer getParent();

} // end interface PwVariable
