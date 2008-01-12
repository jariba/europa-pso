// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwEnumeratedDomainImpl.java,v 1.12 2004-09-30 22:03:01 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
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
   * <code>getLowerBound</code> - 
   *
   * @return - <code>int</code> - 
   */
  public int getLowerBoundInt() {
    String lowerBound = getLowerBound();
    if (lowerBound.equals( DbConstants.MINUS_INFINITY)) {
      return DbConstants.MINUS_INFINITY_INT;
    } else if (lowerBound.equals( DbConstants.PLUS_INFINITY)) {
      return DbConstants.PLUS_INFINITY_INT;
    } else if (lowerBound.equals( "")) {
      return -1;
    } else {
      return Integer.parseInt( lowerBound);
    }
  }

  /**
   * <code>getUpperBound</code> - 
   *
   * @return - <code>int</code> - 
   */
  public int getUpperBoundInt() {
    String upperBound = getUpperBound();
    if (upperBound.equals( DbConstants.PLUS_INFINITY)) {
      return DbConstants.PLUS_INFINITY_INT;
    } else if (upperBound.equals( DbConstants.MINUS_INFINITY)) {
      return DbConstants.MINUS_INFINITY_INT;
    } else if (upperBound.equals( "")) {
      return -1;
    } else {
      return Integer.parseInt( upperBound);
    }
  }

  public boolean isSingleton() {
    return enumeration.size() == 1;
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
   
    for(Iterator it = enumeration.iterator(); it.hasNext();) {
      String elem = (String) it.next();
      if(elem.equals(DbConstants.PLUS_INFINITY))
        elem = DbConstants.PLUS_INFINITY_UNIC;
      else if(elem.equals(DbConstants.MINUS_INFINITY))
        elem = DbConstants.MINUS_INFINITY_UNIC;
      enumBuf.append(elem);
      if(it.hasNext())
        enumBuf.append(", ");
    }
    if (isSingleton) {
      enumBuf.append( "}");
    } else {
      enumBuf.append( "]");
    }
    return enumBuf.toString();
  }

  public List getNeighbors(){return null;}
  public List getNeighbors(List classes){return null;}
  public List getNeighbors(List classes, Set ids){return null;}
  public List getNeighbors(List classes, List linkTypes){return null;}

} // end class PwEnumeratedDomainImpl

