// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwRule.java,v 1.3 2004-03-27 00:30:45 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 26nov03
//

package gov.nasa.arc.planworks.db;

import java.util.List;

/**
 * <code>PwRule</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwRule {

  /**
   * <code>getId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public abstract Integer getId();

  /**
   * <code>getText</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getText();

} // end interface PwRule
