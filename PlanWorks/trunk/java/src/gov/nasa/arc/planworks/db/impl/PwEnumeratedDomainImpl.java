// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwEnumeratedDomainImpl.java,v 1.1 2003-05-15 22:16:22 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwEnumeratedDomain;


/**
 * <code>PwEnumeratedDomainImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwEnumeratedDomainImpl extends PwDomainImpl implements PwEnumeratedDomain {

  private List enumeration; // element String

  public PwEnumeratedDomainImpl( String enumerationString) {
    this.enumeration = new ArrayList();
    StringTokenizer tokenizer = new StringTokenizer( enumerationString);
    while (tokenizer.hasMoreTokens()) {
      this.enumeration.add( tokenizer.nextToken());
    }
  } // end constructor


} // end class PwEnumeratedDomainImpl
