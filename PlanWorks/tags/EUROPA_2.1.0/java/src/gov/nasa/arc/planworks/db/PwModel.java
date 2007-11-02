// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwModel.java,v 1.4 2003-12-03 02:29:50 taylor Exp $
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
   * @return - <code>List</code> -  List of PwParameter
   */
  public abstract List listParameters();

  /**
   * <code>listRules</code>
   *
   * @return - <code>List</code> - List of PwRule
   */
  public abstract List listRules();


} // end interface PwModel
