// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: DbConstants.java,v 1.2 2003-06-12 19:57:20 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09June03
//

package gov.nasa.arc.planworks.db;

import gov.nasa.arc.planworks.db.impl.PwEnumeratedDomainImpl;


/**
 * interface <code>DbConstants</code> - constants for use by .viz.* packages
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface DbConstants {

  /**
   * constant <code>NULL_DOMAIN</code> - PwEnumeratedDomain
   *
   */
  public static final PwDomain NULL_DOMAIN =
    (PwDomain) new PwEnumeratedDomainImpl( "");

  /**
   * constant <code>ZERO_DOMAIN</code> - PwEnumeratedDomain
   *
   */
  public static final PwDomain ZERO_DOMAIN =
    (PwDomain) new PwEnumeratedDomainImpl( "0");

  /**
   * constant <code>PLUS_INFINITY_DOMAIN</code> - PwEnumeratedDomain
   *
   */
  public static final PwDomain PLUS_INFINITY_DOMAIN =
    (PwDomain) new PwEnumeratedDomainImpl( "_plus_infinity_");

  /**
   * constant <code>MINUS_INFINITY_DOMAIN</code> - PwEnumeratedDomain
   *
   */
  public static final PwDomain MINUS_INFINITY_DOMAIN =
    (PwDomain) new PwEnumeratedDomainImpl( "_minus_infinity_");

  /**
   * constant <code>XML_DTD_FILENAME</code>
   *
   */
  public static final String XML_DTD_FILENAME = "PlanDb.dtd";

} // end interface DbConstants
