// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPredicateImpl.java,v 1.1 2003-05-15 22:16:23 taylor Exp $
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
  private List parameterList; // element PwParameter


  /**
   * <code>Predicate</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param key - <code>String</code> - 
   */
  public PwPredicateImpl( String name, String key) {
    this.name = name;
    this.key = key;
    this.parameterList = new ArrayList();

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
    parameterList.add( parameter);
    return parameter;
  } // end addParameter



} // end class PwPredicateImpl
