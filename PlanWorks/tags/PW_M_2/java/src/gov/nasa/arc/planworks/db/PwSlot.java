// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwSlot.java,v 1.3 2003-06-11 01:02:12 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;


/**
 * <code>PwSlot</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwSlot {

  /**
   * <code>getKey</code>
   *
   * @return name - <code>String</code> -
   */
  public abstract String getKey();
	
  /**
   * <code>getTokenList</code>
   *
   * @return - <code>List</code> - of PwToken
   */
  public abstract List getTokenList();
	


} // end interface PwSlot
