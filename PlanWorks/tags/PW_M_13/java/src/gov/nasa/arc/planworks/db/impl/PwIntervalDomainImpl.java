// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwIntervalDomainImpl.java,v 1.8 2004-02-03 19:22:15 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwIntervalDomain;


/**
 * <code>PwIntervalDomainImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwIntervalDomainImpl extends PwDomainImpl implements PwIntervalDomain {

  private String type;
  private String lowerBound;
  private String upperBound;


  /**
   * <code>IntervalDomain</code> - constructor 
   *
   * @param type - <code>String</code> - 
   * @param lowerBound - <code>String</code> - 
   * @param upperBound - <code>String</code> - 
   */
  public PwIntervalDomainImpl( final String type, final String lowerBound, 
                               final String upperBound) {
    this.type = type;
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  } // end constructor

  /**
   * <code>getLowerBound</code>
   *
   * @return - <code>String</code> - 
   */
  public String getLowerBound() {
    return lowerBound;
  }

  /**
   * <code>getUpperBound</code>
   *
   * @return - <code>String</code> - 
   */
  public String getUpperBound() {
    return upperBound;
  }

  /**
   * <code>getLowerBoundInt</code>
   *
   * @return - <code>int</code> - 
   */
  public int getLowerBoundInt() {
    if (lowerBound.equals( DbConstants.MINUS_INFINITY)) {
      return DbConstants.MINUS_INFINITY_INT;
    } else if (lowerBound.equals( DbConstants.PLUS_INFINITY)) {
      return DbConstants.PLUS_INFINITY_INT;
    } else {
      return Integer.parseInt( lowerBound);
    }
  }

  /**
   * <code>getUpperBoundInt</code>
   *
   * @return - <code>int</code> - 
   */
  public int getUpperBoundInt() {
    if (upperBound.equals( DbConstants.PLUS_INFINITY)) {
      return DbConstants.PLUS_INFINITY_INT;
    } else if (upperBound.equals( DbConstants.MINUS_INFINITY)) {
      return DbConstants.MINUS_INFINITY_INT;
    } else {
      return Integer.parseInt( upperBound);
    }
  }

  public boolean isSingleton() {
    return upperBound.equals(lowerBound);
  }

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public String toString() {
    StringBuffer boundBuf = new StringBuffer("");
    boolean isSingleton = false;
    if (lowerBound.equals( upperBound)) {
      isSingleton = true;
    }
    if (isSingleton) {
      boundBuf.append( "{");
    } else {
      boundBuf.append( "[");
    }
    boundBuf.append( lowerBound);
    if (! isSingleton) {
      boundBuf.append( " ");
      boundBuf.append( upperBound);
    }
    if (isSingleton) {
      boundBuf.append( "}");
    } else {
      boundBuf.append( "]");
    }
    return boundBuf.toString();
  }
		


} // end class PwIntervalDomainImpl
