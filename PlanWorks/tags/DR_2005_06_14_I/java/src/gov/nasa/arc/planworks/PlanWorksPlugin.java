// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PlanWorksPlugin.java,v 1.3 2005-01-20 21:07:47 meboyce Exp $
//
// PlanWorks
//
// Will Taylor -- started 13july04
//

package gov.nasa.arc.planworks;

import gov.nasa.arc.planworks.plugin.PwPluginListener;
import javax.swing.JMenu;

/**
 * <code>PlanWorksPlugin</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *             NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PlanWorksPlugin extends PwPluginListener {

  /**
   * <code>loadPlugin</code>
   *
   * @param plugInMenu - <code>JMenu</code> - 
   */
  public void loadPlugin( JMenu plugInMenu);

} // end interface PlanWorksPlugin

