// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: MDIDesktopPane.java,v 1.2 2003-06-13 20:22:27 miatauro Exp $
//
package gov.nasa.arc.planworks.mdi;

import javax.swing.JDesktopPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * <code>MDIDesktopPane</code> - was internal class of MDIDesktopFrame
 *          make it public class so that background color, etc can be
 *          changed
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 1.4
 */
public class MDIDesktopPane extends JDesktopPane {

  public MDIDesktopPane() {
    super();
    setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

     UIDefaults defaults = UIManager.getDefaults();
     defaults.put("DesktopIconUI", getClass().getPackage().getName() + ".EmptyDesktopIconUI");
  }
} // end class MDIDesktopPane

