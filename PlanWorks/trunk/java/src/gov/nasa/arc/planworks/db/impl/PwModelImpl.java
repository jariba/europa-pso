// $Id: PwModelImpl.java,v 1.1 2003-05-10 01:00:32 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.arc.planworks.db.PwModel;
/**
 * <code>PwModelImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwModelImpl extends PwModel {

  private List predicateList;
  private List classList;
  private List parameterList;


  public PwModelImpl() {
    predicateList = new ArrayList();
    classList = new ArrayList();
    parameterList = new ArrayList();
  } // end constructor

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



} // end class PwModelImpl
