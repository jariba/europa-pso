// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwRule.java,v 1.2 2003-12-12 01:23:04 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 26nov03
//

package gov.nasa.arc.planworks.db;

import java.util.List;

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

  /**
   * <code>getFromPredicateParams</code>
   *
   * @return - <code>List</code> - 
   */
  public abstract List getFromPredicateParams();

  /**
   * <code>getToPredicateParams</code>
   *
   * @return - <code>List</code> - 
   */
  public abstract List getToPredicateParams();

  /**
   * <code>getFromPredicateParamValues</code>
   *
   * @return - <code>List</code> - 
   */
  public abstract List getFromPredicateParamValues();

  /**
   * <code>getToPredicateParamValues</code>
   *
   * @return - <code>List</code> - 
   */
  public abstract List getToPredicateParamValues();

  /**
   * <code>getFromPredicateObject</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getFromPredicateObject();

  /**
   * <code>getToPredicateObject</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getToPredicateObject();

  /**
   * <code>getFromPredicateAttribute</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getFromPredicateAttribute();

  /**
   * <code>getToPredicateAttribute</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getToPredicateAttribute();

  /**
   * <code>getDurationStart</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getDurationStart();

  /**
   * <code>getDurationEnd</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getDurationEnd();

} // end interface PwRule
