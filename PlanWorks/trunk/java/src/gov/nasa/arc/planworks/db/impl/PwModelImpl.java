// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwModelImpl.java,v 1.4 2003-12-12 01:23:04 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwModel;
import gov.nasa.arc.planworks.db.PwRule;


/**
 * <code>PwModelImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwModelImpl implements PwModel {

  private List predicateList;
  private List classList;
  private List parameterList;
  private List ruleList;


  /**
   * <code>PwModelImpl</code> - constructor 
   *
   */
  public PwModelImpl() {
    predicateList = new ArrayList();
    classList = new ArrayList();
    parameterList = new ArrayList();
    ruleList = new ArrayList();

    // loadModel();
    loadModel1();

  } // end constructor

  private void loadModel() {

    // dummy data
    predicateList.add( "CameraNotCalibrated");
    predicateList.add( "CloseShutters");
    predicateList.add( "MeasureDarkCurrent");
    predicateList.add( "OpenShutters");

    ruleList.add( (PwRule) (new PwRuleImpl( "CameraNotCalibrated", "CloseShutters", "meets")));
    ruleList.add( (PwRule) (new PwRuleImpl( "CloseShutters", "CameraNotCalibrated", "met-by")));

    ruleList.add( (PwRule) (new PwRuleImpl( "CameraNotCalibrated", "CloseShutters", "contains")));
    ruleList.add( (PwRule) (new PwRuleImpl( "CloseShutters", "CameraNotCalibrated", "contained-by")));

    ruleList.add( (PwRule) (new PwRuleImpl( "CloseShutters", "MeasureDarkCurrent", "meets")));
    ruleList.add( (PwRule) (new PwRuleImpl( "MeasureDarkCurrent", "CloseShutters", "met-by")));

    ruleList.add( (PwRule) (new PwRuleImpl( "MeasureDarkCurrent", "OpenShutters", "meets")));
    ruleList.add( (PwRule) (new PwRuleImpl( "OpenShutters", "MeasureDarkCurrent", "met-by")));


  } // end loadModel


  private void loadModel1() {

    // dummy data
//     public PwRuleImpl( String fromPredicate, List fromPredicateParams,
//                      String fromPredicateObject, String fromPredicateAttribute,
//                      String toPredicate, List toPredicateParams,
//                      String toPredicateObject, String toPredicateAttribute,
//                      String ruleType, String durationStart, String durationEnd)

    String atPredicate = "At";
    predicateList.add( atPredicate);
    String goingPredicate = "Going";
    predicateList.add( goingPredicate);
    List atParams = new ArrayList();
    atParams.add( "loc");
    List atParamValues = new ArrayList();
    List paramValues = new ArrayList();
    paramValues.add( "Rock");
    paramValues.add( "Tree");
    atParamValues.add( paramValues);
    List goingMetByParams = new ArrayList();
    goingMetByParams.add( "*");
    goingMetByParams.add( "loc");
    List goingMeetsParams = new ArrayList();
    goingMeetsParams.add( "loc");
    goingMeetsParams.add( "*");
    List goingParamValues = new ArrayList();
    goingParamValues.add( paramValues);
    goingParamValues.add( paramValues);
    ruleList.add( (PwRule)
                  (new PwRuleImpl( atPredicate, atParams, atParamValues,
                                   "Monkey_Class", "Location_SV",
                                   goingPredicate, goingMetByParams, goingParamValues,
                                   "Monkey_Class", "Location_SV",
                                   DbConstants.RULE_MET_BY, "1", DbConstants.PLUS_INFINITY)));
    ruleList.add( (PwRule)
                  (new PwRuleImpl( atPredicate, atParams, atParamValues,
                                   "Monkey_Class", "Location_SV",
                                   goingPredicate, goingMeetsParams, goingParamValues,
                                   "Monkey_Class", "Location_SV",
                                   DbConstants.RULE_MEETS, "1", DbConstants.PLUS_INFINITY)));
    List atParamsA = new ArrayList();
    atParamsA.add( "locA");
    List atParamsB = new ArrayList();
    atParamsB.add( "locB");
    List goingParams = new ArrayList();
    goingParams.add( "locA");
    goingParams.add( "locB");
    ruleList.add( (PwRule)
                  (new PwRuleImpl( goingPredicate, goingParams, goingParamValues,
                                   "Monkey_Class", "Location_SV",
                                   atPredicate, atParamsA, atParamValues,
                                   "Monkey_Class", "Location_SV",
                                   DbConstants.RULE_MET_BY, "5", DbConstants.PLUS_INFINITY)));
    ruleList.add( (PwRule)
                  (new PwRuleImpl( goingPredicate, goingParams, goingParamValues,
                                   "Monkey_Class", "Location_SV",
                                   atPredicate, atParamsB, atParamValues,
                                   "Monkey_Class", "Location_SV",
                                   DbConstants.RULE_MEETS, "5", DbConstants.PLUS_INFINITY)));
      
  } // end loadModel1


  /**
   * <code>listPredicates</code>
   *
   * @return - <code>List</code> -  List of PwPredicate
   */
  public List listPredicates() {
    return predicateList;
  }

  /**
   * <code>listClasses</code>
   *
   * @return - <code>List</code> -  List of PwClass
   */
  public List listClasses() {
    return classList;
  }

  /**
   * <code>listParameters</code>
   *
   * @return - <code>List</code> -  List of PwParamter
   */
  public List listParameters() {
    return parameterList;
  }

  /**
   * <code>listRules</code>
   *
   * @return - <code>List</code> - List of PwRule
   */
  public List listRules() {
    return ruleList;
  }



} // end class PwModelImpl


