// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwModel.java,v 1.2 2003-05-15 18:38:44 taylor Exp $
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
public interface PwModel {

  /**
   * <code>listPredicates</code>
   *
   * @return - <code>List</code> -  List of PwPredicate
   */
  public List listPredicates();

  /**
   * <code>listClasses</code>
   *
   * @return - <code>List</code> -  List of PwClass
   */
  public List listClasses();

  /**
   * <code>listParameters</code>
   *
   * @return - <code>List</code> -  List of PwParamter
   */
  public List listParameters();



} // end interface PwModel
