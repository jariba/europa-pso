// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwConstraintImpl.java,v 1.6 2003-06-26 18:20:07 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 16May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gov.nasa.arc.planworks.db.PwConstraint;


/**
 * <code>PwConstraintImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwConstraintImpl implements PwConstraint {

  private String name;
  private Integer key;
  private String type;
  private List variableIds; // element Integer
  private PwPartialPlanImpl partialPlan;


  /**
   * <code>PwConstraintImpl</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param key - <code>int</code> - 
   * @param type - <code>String</code> - 
   * @param variableIds - <code>List of Integer</code> -
   * @param partialPlan - <code>PwPartialPlan</code> - 
   */
  public PwConstraintImpl( String name, Integer key, String type, List variableIds,
                           PwPartialPlanImpl partialPlan) {
    this.name = name;
    this.key = key;
    this.type = type;
    this.variableIds = variableIds;
    this.partialPlan = partialPlan;
  } // end constructor


  /**
   * <code>getName</code>
   *
   * @return name - <code>String</code> -
   */
  public String getName() {
    return name;
  }

  /**
   * <code>getKey</code>
   *
   * @return name - <code>int</code> -
   */
  public Integer getKey() {
    return key;
  }
	
  /**
   * <code>getType</code>
   *
   * @return type - <code>String</code> -
   */
  public String getType() {
    return type;
  }
	
  /**
   * <code>getVariablesList</code>
   *
   * @return - <code>List</code> - of PwVariable
   */
  public List getVariablesList() {
    List retval = new ArrayList( variableIds.size());
    for (int i = 0; i < variableIds.size(); i++) {
      retval.add( partialPlan.getVariable((Integer) variableIds.get( i)));
    }
    return retval;
  }

} // end class PwConstraintImpl
