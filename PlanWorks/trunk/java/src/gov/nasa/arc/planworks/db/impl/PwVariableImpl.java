// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwVariableImpl.java,v 1.1 2003-05-15 22:16:23 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwVariable;


/**
 * <code>PwVariableImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwVariableImpl implements PwVariable {

  private String key;
  private String type;
  private List constraintIds; // element String
  private String paramId;
  private PwDomainImpl domain; // PwEnumeratedDomainImpl || PwIntervalDomainImpl


  /**
   * <code>PwVariableImpl</code> - constructor 
   *
   * @param key - <code>String</code> - 
   * @param type - <code>String</code> - 
   * @param constraintIds - <code>String</code> - 
   * @param paramId - <code>String</code> - 
   * @param domain - <code>PwDomainImpl</code> - PwEnumeratedDomainImpl || PwIntervalDomainImpl
   */
  public PwVariableImpl( String key, String type, String constraintIds, String paramId,
                         PwDomainImpl domain) {
    this.key = key;
    this.type = type;
    this.constraintIds = new ArrayList();
    StringTokenizer tokenizer = new StringTokenizer( constraintIds);
    while (tokenizer.hasMoreTokens()) {
      this.constraintIds.add( tokenizer.nextToken());
    }
    this.paramId = paramId;
    this.domain = domain;
  } // end constructor


  /**
   * <code>getDomain</code>
   *
   * @return domain - <code>PwDomainImpl</code> - 
   */
  public PwDomainImpl getDomain()  {
    return this.domain;
  }

} // end class PwVariableImpl
