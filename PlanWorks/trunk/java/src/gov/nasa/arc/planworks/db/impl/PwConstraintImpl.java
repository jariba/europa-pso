// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwConstraintImpl.java,v 1.8 2003-08-12 22:53:59 miatauro Exp $
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
  private Integer id;
  private String type;
  private List variableIds; // element Integer
  private PwPartialPlanImpl partialPlan;


  /**
   * <code>PwConstraintImpl</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param id - <code>int</code> - 
   * @param type - <code>String</code> - 
   * @param variableIds - <code>List of Integer</code> -
   * @param partialPlan - <code>PwPartialPlan</code> - 
   */
  public PwConstraintImpl( String name, Integer id, String type, List variableIds,
                           PwPartialPlanImpl partialPlan) {
    this.name = name;
    this.id = id;
    this.type = type;
    this.variableIds = new ArrayList(variableIds);
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
   * <code>getId</code>
   *
   * @return name - <code>int</code> -
   */
  public Integer getId() {
    return id;
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
