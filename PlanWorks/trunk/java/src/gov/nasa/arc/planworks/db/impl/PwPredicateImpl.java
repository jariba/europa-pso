// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPredicateImpl.java,v 1.8 2003-08-12 22:54:01 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwPredicate;


/**
 * <code>PwPredicateImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwPredicateImpl implements PwPredicate {

  private String name;
  private Integer id;
  private List parameterIdList; // element String
  private PwPartialPlanImpl partialPlan;

  /**
   * <code>PwPredicateImpl</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param id - <code>String</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwPredicateImpl( Integer id, String name, PwPartialPlanImpl partialPlan) {
    this.name = name;
    this.id = id;
    this.parameterIdList = new ArrayList();
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
   * @return name - <code>String</code> -
   */
  public Integer getId() {
    return id;
  }
	
  /**
   * <code>getParameterList</code>
   *
   * @return - <code>List</code> - of PwParameter
   */
  public List getParameterList() {
    List retval = new ArrayList( parameterIdList.size());
    for (int i = 0; i < parameterIdList.size(); i++) {
      retval.add( partialPlan.getParameter( (Integer)parameterIdList.get( i)));
    }
    return retval;
  }

  /**
   * <code>addParameter</code>
   *
   * @param name - <code>String</code> - 
   * @param id - <code>String</code> - 
   * @return - <code>PwParameterImpl</code> - 
   */
  public PwParameterImpl addParameter( Integer id, String name) {
    PwParameterImpl parameter = new PwParameterImpl(id, name);
    parameterIdList.add( id);
    return parameter;
  } // end addParameter


} // end class PwPredicateImpl
