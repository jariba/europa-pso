// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: MDIDesktopPane.java,v 1.1 2003-06-11 01:09:47 taylor Exp $
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
    // prevents  UIDefaults.getUI() failed: no ComponentUI class for: javax.swing.JInternalFrame$JDesktopIcon[,0,0,0x0,invalid,hidden,alignmentX=null,alignmentY=null,border=,flags=0,maximumSize=,minimumSize=,preferredSize=]

//     UIDefaults defaults = UIManager.getDefaults();
//     defaults.put("DesktopIconUI", "EmptyDesktopIconUI");
    //   setDesktopManager(new MDIDesktopManager());
  }

  //class MDIDesktopManager extends DesktopManager
  //{
  //}

} // end class MDIDesktopPane

