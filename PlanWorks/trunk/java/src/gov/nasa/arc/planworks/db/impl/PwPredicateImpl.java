// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPredicateImpl.java,v 1.3 2003-05-16 21:25:25 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwParameter;
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
  private List parameterIdList; // element PwParameter
	private PwPartialPlanImpl partialPlan;
		private String collectionName;

  /**
   * <code>Predicate</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param key - <code>String</code> - 
   */
  public PwPredicateImpl( String name, String key, PwPartialPlanImpl partialPlan, String collectionName) {
    this.name = name;
    this.key = key;
    this.parameterIdList = new ArrayList();
		this.partialPlan = partialPlan;
		this.collectionName = collectionName;
  } // end constructor


  /**
   * <code>addParameter</code>
   *
   * @param name - <code>String</code> - 
   * @param key - <code>String</code> - 
   * @return parameter - <code>PwParameter</code> - 
   */
	  public PwParameter addParameter( String name, String key) {
		PwParameter parameter = new PwParameterImpl( name, key);
		parameterIdList.add(key);
		//parameterList.add( parameter);
		return parameter;
  } // end addParameter



} // end class PwPredicateImpl
