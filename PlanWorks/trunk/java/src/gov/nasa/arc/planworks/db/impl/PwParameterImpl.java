// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwParameterImpl.java,v 1.6 2003-08-12 22:54:00 miatauro Exp $
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
  private Integer id;

  /**
   * <code>Parameter</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param id - <code>int</code> - 
   */
  public PwParameterImpl( Integer id, String name) {
    this.name = name;
    this.id = id;
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
	


} // end class PwParameterImpl
