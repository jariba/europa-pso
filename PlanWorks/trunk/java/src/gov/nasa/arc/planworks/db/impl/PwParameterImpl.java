// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwParameterImpl.java,v 1.4 2003-06-25 17:04:04 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.arc.planworks.db.PwParameter;


/**
 * <code>PwParameterImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwParameterImpl implements PwParameter {

  private String name;
  private int key;

  /**
   * <code>Parameter</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param key - <code>int</code> - 
   */
  public PwParameterImpl( String name, int key) {
    this.name = name;
    this.key = key;
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
  public int getKey() {
    return key;
  }
	


} // end class PwParameterImpl
