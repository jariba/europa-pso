// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwIntervalDomainImpl.java,v 1.2 2003-05-16 20:06:19 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
  public PwIntervalDomainImpl( String type, String lowerBound, String upperBound) {
    this.type = type;
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  } // end constructor

		public String getLowerBound() {
				return lowerBound;
		}
		public String getUpperBound() {
				return upperBound;
		}
		
		public String toString() {
				StringBuffer boundBuf = new StringBuffer("[");
				boundBuf.append(lowerBound);
				boundBuf.append("-");
				boundBuf.append(upperBound);
				boundBuf.append("]");
				return boundBuf.toString();
		}
		

} // end class PwIntervalDomainImpl
