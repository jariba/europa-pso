// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwRule.java,v 1.1 2003-12-03 02:29:50 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 26nov03
//

package gov.nasa.arc.planworks.db;


/**
 * <code>PwRule</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwRule {

  /**
   * <code>getFromPredicete</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getFromPredicate();

  /**
   * <code>getToPredicete</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getToPredicate();

  /**
   * <code>getType</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getType();


} // end interface PwRule
