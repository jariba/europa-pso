// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwSlot.java,v 1.9 2004-02-25 02:30:13 taylor Exp $
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
public interface PwSlot extends PwEntity {

  /**
   * <code>getId</code>
   *
   * @return name - <code>Integer</code> -
   */
  public abstract Integer getId();
	
  /**
   * <code>getTimelineId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public abstract Integer getTimelineId();

  /**
   * <code>getTokenList</code>
   *
   * @return - <code>List</code> - of PwToken
   */
  public abstract List getTokenList();
	
  /**
   * <code>getBaseToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public abstract PwToken getBaseToken();

  public abstract PwDomain getStartTime();
  public abstract PwDomain getEndTime();

  public boolean isEmpty();
} // end interface PwSlot
