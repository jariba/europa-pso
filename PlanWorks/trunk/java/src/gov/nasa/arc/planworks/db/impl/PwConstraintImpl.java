// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwConstraintImpl.java,v 1.4 2003-06-02 17:49:58 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 16May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

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
  private String key;
  private String type;
  private List variableIds; // element String
  private PwPartialPlanImpl partialPlan;


  /**
   * <code>PwConstraintImpl</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param key - <code>String</code> - 
   * @param type - <code>String</code> - 
   * @param variableIds - <code>String</code> - 
   * @param partialPlan - <code>PwPartialPlan</code> - 
   */
  public PwConstraintImpl( String name, String key, String type, String variableIds,
                           PwPartialPlanImpl partialPlan) {
    this.name = name;
    this.key = key;
    this.type = type;
    this.variableIds = new ArrayList();
    StringTokenizer tokenizer = new StringTokenizer( variableIds);
    while (tokenizer.hasMoreTokens()) {
      this.variableIds.add( tokenizer.nextToken());
    }
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
   * @return name - <code>String</code> -
   */
  public String getKey() {
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
      retval.add( partialPlan.getVariable( (String) variableIds.get( i)));
    }
    return retval;
  }

} // end class PwConstraintImpl
