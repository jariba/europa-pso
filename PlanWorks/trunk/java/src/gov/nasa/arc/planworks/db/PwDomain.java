// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwDomain.java,v 1.4 2003-08-26 01:37:10 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db;

import gov.nasa.arc.planworks.db.impl.PwIntervalDomainImpl;


/**
 * <code>PwDomain</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwDomain {

  /**
   * constant <code>PLUS_INFINITY</code> - String
   *
   */
  public static final String PLUS_INFINITY = "Infinity";

  /**
   * constant <code>MINUS_INFINITY</code> - String
   *
   */
  public static final String MINUS_INFINITY = "-Infinity";

  /**
   * constant <code>PLUS_INFINITY_INT</code>
   *
   */
  public static final int PLUS_INFINITY_INT = Integer.MAX_VALUE;

  /**
   * constant <code>MINUS_INFINITY_INT</code>
   *
   */
  public static final int MINUS_INFINITY_INT = Integer.MIN_VALUE;

  /**
   * constant <code>ZERO_DOMAIN</code> - PwIntervalDomain
   *
   */
  public static final PwDomain ZERO_INTERVAL_DOMAIN =
    (PwDomain) new PwIntervalDomainImpl( "INTEGER_SORT", "0", "0");

  /**
   * constant <code>INFINITY_INTERVAL_DOMAIN</code> - PwIntervalDomain
   *
   */
  public static final PwDomain INFINITY_INTERVAL_DOMAIN =
    (PwDomain) new PwIntervalDomainImpl( "INTEGER_SORT", "-Infinity", "Infinity");

  /**
   * <code>getLowerBound</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getLowerBound();

  /**
   * <code>getUpperBound</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getUpperBound();
		
  /**
   * <code>getLowerBoundInt</code> - 
   *
   * @return - <code>int</code> - 
   */
  public abstract int getLowerBoundInt();

  /**
   * <code>getUpperBoundInt</code> - 
   *
   * @return - <code>int</code> - 
   */
  public abstract int getUpperBoundInt();
		
  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String toString();


} // end interface PwDomain
