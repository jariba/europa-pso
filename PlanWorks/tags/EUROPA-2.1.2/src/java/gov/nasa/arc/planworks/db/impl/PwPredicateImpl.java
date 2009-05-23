// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPredicateImpl.java,v 1.11 2004-04-02 00:58:27 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
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
  private Map params;
  private PwPartialPlanImpl partialPlan;

  /**
   * <code>PwPredicateImpl</code> - constructor 
   *
   * @param id - <code>Integer</code> - 
   * @param name - <code>String</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwPredicateImpl( Integer id, String name, PwPartialPlanImpl partialPlan) {
    this.name = name;
    this.id = id;
    this.params = new HashMap();
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
   * @return name - <code>Integer</code> -
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
    List retval = new ArrayList( params.keySet().size());
    Set temp = params.keySet();
    List keys = new ArrayList(temp);
    Collections.sort(keys);
    Iterator keyIterator = keys.iterator();
    while(keyIterator.hasNext()) {
      retval.add(params.get(keyIterator.next()));
    }
    return retval;
  }

} // end class PwPredicateImpl
