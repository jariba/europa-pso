// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPredicateImpl.java,v 1.4 2003-05-18 00:02:26 taylor Exp $
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
  private String key;
  private List parameterIdList; // element String
  private PwPartialPlanImpl partialPlan;
  private String collectionName;

  /**
   * <code>PwPredicateImpl</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param key - <code>String</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   * @param collectionName - <code>String</code> - 
   */
  public PwPredicateImpl( String name, String key, PwPartialPlanImpl partialPlan,
                          String collectionName) {
    this.name = name;
    this.key = key;
    this.parameterIdList = new ArrayList();
    this.partialPlan = partialPlan;
    this.collectionName = collectionName;
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
   * <code>getParameterList</code>
   *
   * @return - <code>List</code> - of PwParameter
   */
  public List getParameterList() {
    ArrayList retval = new ArrayList( parameterIdList.size());
    for (int i = 0; i < parameterIdList.size(); i++) {
      retval.set( i, partialPlan.getParameter( (String) parameterIdList.get( i),
                                               collectionName));
    }
    return retval;
  }

  /**
   * <code>addParameter</code>
   *
   * @param name - <code>String</code> - 
   * @param key - <code>String</code> - 
   * @return - <code>PwParameterImpl</code> - 
   */
  public PwParameterImpl addParameter( String name, String key) {
    PwParameterImpl parameter = new PwParameterImpl( name, key);
    parameterIdList.add( key);
    return parameter;
  } // end addParameter


} // end class PwPredicateImpl
