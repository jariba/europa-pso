// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwEnumeratedDomainImpl.java,v 1.5 2003-07-24 20:57:11 taylor Exp $
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

  /**
   * <code>PwEnumeratedDomainImpl</code> - constructor 
   *
   * @param enumerationString - <code>String</code> - 
   */
  public PwEnumeratedDomainImpl( String enumerationString) {
    this.enumeration = new ArrayList();
    StringTokenizer tokenizer = new StringTokenizer( enumerationString);
    while (tokenizer.hasMoreTokens()) {
      this.enumeration.add( tokenizer.nextToken());
    }
  } // end constructor

  /**
   * <code>getEnumeration</code>
   *
   * @return - <code>List</code> - 
   */
  public List getEnumeration() {
    return this.enumeration;	
  }
		
  /**
   * <code>getLowerBound</code>
   *
   * @return - <code>String</code> - 
   */
  public String getLowerBound() {
    if (enumeration.size() > 0) {
      return (String) enumeration.get( 0);
    } else {
      return "";
    }
  }

  /**
   * <code>getUpperBound</code>
   *
   * @return - <code>String</code> - 
   */
  public String getUpperBound() {
    if (enumeration.size() > 0) {
      return (String) enumeration.get( enumeration.size() - 1);
    } else {
      return "";
    }
  }
		
  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public String toString() {
    StringBuffer enumBuf = new StringBuffer( "");
    boolean isSingleton = (enumeration.size() == 1);
    if (isSingleton) {
      enumBuf.append( "{");
    } else {
      enumBuf.append( "[");
    }
    for (int i = 0; i < enumeration.size() - 1; i++) {
      enumBuf.append( (String) enumeration.get(i));
      enumBuf.append( ", ");
    }
    if (enumeration.size() > 0) {
      enumBuf.append( (String) enumeration.get(enumeration.size() - 1));
    }
    if (isSingleton) {
      enumBuf.append( "}");
    } else {
      enumBuf.append( "]");
    }
    return enumBuf.toString();
  }

} // end class PwEnumeratedDomainImpl

