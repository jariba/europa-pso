//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MDIDesktopPane.java,v 1.3 2003-06-16 22:32:14 miatauro Exp $
//
// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: MDIDesktopPane.java,v 1.3 2003-06-16 22:32:14 miatauro Exp $
//
package gov.nasa.arc.planworks.mdi;

import javax.swing.JDesktopPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * <code>MDIDesktopPane</code> - was internal class of MDIDesktopFrame
 *          make it public class so that background color, etc can be
 *          changed
 * Sets OUTLINE_DRAG_MODE and the EmptyDesktopIconUI class so that iconifying (minimizing)
 * an MDIInternalFrame doesn't create an icon at the bottom of the screen.
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

