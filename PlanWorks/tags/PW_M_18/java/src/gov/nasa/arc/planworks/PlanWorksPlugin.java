// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PlanWorksPlugin.java,v 1.1 2004-07-15 21:24:45 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 13july04
//

package gov.nasa.arc.planworks;

import javax.swing.JMenuItem;

/**
 * <code>PlanWorksPlugin</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *             NASA Ames Research Center - Code IC
 * @version 0.0
 */
public abstract class PlanWorksPlugin {

  /**
   * <code>loadPlugin</code>
   *
   * @param plugInMenu - <code>JMenuItem</code> - 
   */
  public static void loadPlugin( JMenuItem plugInMenu) {}

} // end interface PlanWorksPlugin

