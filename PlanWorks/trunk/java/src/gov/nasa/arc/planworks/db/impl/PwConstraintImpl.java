// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwConstraintImpl.java,v 1.1 2003-05-16 18:33:41 taylor Exp $
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
  private List variableIds;


  /**
   * <code>PwConstraintImpl</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param key - <code>String</code> - 
   * @param type - <code>String</code> - 
   * @param variableIds - <code>String</code> - 
   */
  public PwConstraintImpl( String name, String key, String type, String variableIds) {
    this.name = name;
    this.key = key;
    this.type = type;
    this.variableIds = new ArrayList();
    StringTokenizer tokenizer = new StringTokenizer( variableIds);
    while (tokenizer.hasMoreTokens()) {
      this.variableIds.add( tokenizer.nextToken());
    }
  } // end constructor



} // end class PwConstraintImpl
