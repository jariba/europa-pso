// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwRuleImpl.java,v 1.1 2003-12-03 02:29:50 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 26nov03
//

package gov.nasa.arc.planworks.db.impl;

import gov.nasa.arc.planworks.db.PwRule;


/**
 * <code>PwRuleImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwRuleImpl implements PwRule {

  private String fromPredicate;
  private String toPredicate;
  private String ruleType;
  
  /**
   * <code>PwRuleImpl</code> - constructor 
   *
   * @param fromPredicate - <code>String</code> - 
   * @param toPredicate - <code>String</code> - 
   * @param ruleType - <code>String</code> - 
   */
  public PwRuleImpl( String fromPredicate, String toPredicate, String ruleType) {
    this.fromPredicate = fromPredicate;
    this.toPredicate = toPredicate;
    this.ruleType = ruleType;
  } // end constructor

  /**
   * <code>getFromPredicete</code>
   *
   * @return - <code>String</code> - 
   */
  public String getFromPredicate() {
    return fromPredicate;
  }

  /**
   * <code>getToPredicete</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToPredicate() {
    return toPredicate;
  }

  /**
   * <code>getType</code>
   *
   * @return - <code>String</code> - 
   */
  public String getType() {
    return ruleType;
  }

} // end PwRuleImpl
