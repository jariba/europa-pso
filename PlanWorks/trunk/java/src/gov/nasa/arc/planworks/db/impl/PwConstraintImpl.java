// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwConstraintImpl.java,v 1.13 2004-05-04 01:27:11 taylor Exp $
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
import gov.nasa.arc.planworks.util.UniqueSet;

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
  //private List variableIds; // element Integer
  private UniqueSet variableIds;
  private PwPartialPlanImpl partialPlan;


  /**
   * <code>PwConstraintImpl</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param id - <code>Integer</code> - 
   * @param type - <code>String</code> - 
   * @param variableIds - <code>List of Integer</code> -
   * @param partialPlan - <code>PwPartialPlan</code> - 
   */
  public PwConstraintImpl( final String name, final Integer id, final String type, 
                           final List variableIds, final PwPartialPlanImpl partialPlan) {
    this.name = name;
    this.id = id;
    this.type = type;
    this.variableIds = new UniqueSet(variableIds);
    this.partialPlan = partialPlan;
  } // end constructor

  public PwConstraintImpl(final String name, final Integer id, final String type, 
                          final PwPartialPlanImpl partialPlan) {
    this.name = name;
    this.id = id;
    this.type = type;
    this.variableIds = new UniqueSet();
    this.partialPlan = partialPlan;
  }

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
   * @return name - <code>Integer</code> -
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

  /**
   * <code>getVariableIdList</code>
   *
   * @return - <code>List</code> - of Integer
   */
  public List getVariableIdList() {
    return new ArrayList(variableIds);
  }

  public void addVariable(final Integer variableId) {
    variableIds.add(variableId);
  }

  /**
   * <code>toOutputString</code> step<n>.constraints 
   *
   * @return - <code>String</code> - 
   */
  public String toOutputString() {
    StringBuffer retval = new StringBuffer(id.toString());
    retval.append("\t").append(partialPlan.getId()).append("\t").append(name).append("\t");
    retval.append(type).append("\n");
    return retval.toString();
  }

  /**
   * <code>toOutputStringVarMap</code> - step<n>.constraintVarMap
   *
   * @return - <code>String</code> - 
   */
  public String toOutputStringVarMap() {
    Iterator variableItr = getVariableIdList().iterator();
    StringBuffer retval = new StringBuffer();
    while (variableItr.hasNext()) {
      retval.append(id.toString()).append("\t");
      retval.append( ((Integer) variableItr.next()).toString()).append("\t");
      retval.append(partialPlan.getId()).append("\t").append("\n");
    }
    return retval.toString();
  }

} // end class PwConstraintImpl
