// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwRuleImpl.java,v 1.2 2003-12-12 01:23:04 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 26nov03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.List;

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
  private List fromPredicateParams;
  private List fromPredicateParamValues;
  private String fromPredicateObject;
  private String fromPredicateAttribute;
  private String toPredicate;
  private List toPredicateParams;
  private List toPredicateParamValues;
  private String toPredicateObject;
  private String toPredicateAttribute;
  private String ruleType;
  private String durationStart;
  private String durationEnd;
  
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

  public PwRuleImpl( String fromPredicate, List fromPredicateParams, List fromPredicateParamValues,
                     String fromPredicateObject, String fromPredicateAttribute,
                     String toPredicate, List toPredicateParams, List toPredicateParamValues,
                     String toPredicateObject, String toPredicateAttribute,
                     String ruleType, String durationStart, String durationEnd) {
    this.fromPredicate = fromPredicate;
    this.fromPredicateParams = fromPredicateParams;
    this.fromPredicateParamValues = fromPredicateParamValues;
    this.fromPredicateObject = fromPredicateObject;
    this.fromPredicateAttribute = fromPredicateAttribute;
    this.toPredicate = toPredicate;
    this.toPredicateParams = toPredicateParams;
    this.toPredicateParamValues = toPredicateParamValues;
    this.toPredicateObject = toPredicateObject;
    this.toPredicateAttribute = toPredicateAttribute;
    this.ruleType = ruleType;
    this.durationStart = durationStart;
    this.durationEnd = durationEnd;
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

  /**
   * <code>getFromPredicateParams</code>
   *
   * @return - <code>List</code> - 
   */
  public List getFromPredicateParams() {
    return fromPredicateParams;
  }

  /**
   * <code>getToPredicateParams</code>
   *
   * @return - <code>List</code> - 
   */
  public List getToPredicateParams() {
    return toPredicateParams;
  }

  /**
   * <code>getFromPredicateParamValues</code>
   *
   * @return - <code>List</code> - 
   */
  public List getFromPredicateParamValues() {
    return fromPredicateParamValues;
  }

  /**
   * <code>getToPredicateParamValues</code>
   *
   * @return - <code>List</code> - 
   */
  public List getToPredicateParamValues() {
    return toPredicateParamValues;
  }

  /**
   * <code>getFromPredicateObject</code>
   *
   * @return - <code>String</code> - 
   */
  public String getFromPredicateObject() {
    return fromPredicateObject;
  }

  /**
   * <code>getToPredicateObject</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToPredicateObject() {
    return toPredicateObject;
  }

  /**
   * <code>getFromPredicateAttribute</code>
   *
   * @return - <code>String</code> - 
   */
  public String getFromPredicateAttribute() {
    return fromPredicateAttribute;
  }

  /**
   * <code>getToPredicateAttribute</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToPredicateAttribute() {
    return toPredicateAttribute;
  }

  /**
   * <code>getDurationStart</code>
   *
   * @return - <code>String</code> - 
   */
  public String getDurationStart() {
    return durationStart;
  }

  /**
   * <code>getDurationEnd</code>
   *
   * @return - <codeString></code> - 
   */
  public String getDurationEnd() {
    return durationEnd;
  }

} // end PwRuleImpl
