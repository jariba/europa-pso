// $Id: PwModel.java,v 1.1 2003-05-10 01:00:31 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;


/**
 * <code>PwModel</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public abstract class PwModel {

  /**
   * <code>listPredicates</code>
   *
   * @return - <code>List</code> -  List of PwPredicate
   */
  public abstract List listPredicates();

  /**
   * <code>listClasses</code>
   *
   * @return - <code>List</code> -  List of PwClass
   */
  public abstract List listClasses();

  /**
   * <code>listParameters</code>
   *
   * @return - <code>List</code> -  List of PwParamter
   */
  public abstract List listParameters();



} // end class PwModel
