// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwModelImpl.java,v 1.3 2003-12-03 02:29:50 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;

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

    loadModel();

  } // end constructor

  private void loadModel() {

    // dummy data
    predicateList.add( "CameraNotCalibrated");
    predicateList.add( "CloseShutters");
    predicateList.add( "MeasureDarkCurrent");
    predicateList.add( "OpenShutters");

    ruleList.add( (PwRule) (new PwRuleImpl( "CameraNotCalibrated", "CloseShutters", "met-by")));
    ruleList.add( (PwRule) (new PwRuleImpl( "CameraNotCalibrated", "CloseShutters", "meets")));
    ruleList.add( (PwRule) (new PwRuleImpl( "CloseShutters", "MeasureDarkCurrent", "met-by")));
    ruleList.add( (PwRule) (new PwRuleImpl( "CloseShutters", "MeasureDarkCurrent", "meets")));
    ruleList.add( (PwRule) (new PwRuleImpl( "MeasureDarkCurrent", "OpenShutters", "met-by")));
    ruleList.add( (PwRule) (new PwRuleImpl( "MeasureDarkCurrent", "OpenShutters", "meets")));


  } // end loadModel

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


