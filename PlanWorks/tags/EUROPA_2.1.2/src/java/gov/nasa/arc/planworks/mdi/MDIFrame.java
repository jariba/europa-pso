//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MDIFrame.java,v 1.2 2003-06-16 22:32:14 miatauro Exp $
//
package gov.nasa.arc.planworks.mdi;

import javax.swing.JButton;
import javax.swing.JMenu;

/**
 * <code>MDIFrame</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * An interface to provide the getAssociatedMenus() method to the MDIDynamicMenuBar and
 * getButton() to the MDIWindowButtonBar.
 */

public interface MDIFrame {
  /**
   * Gets the menus associated with the MDIFrame.  Used by MDIDynamicMenuBar to build the menu bar.
   * @return JMenu[] The array of associated menus.
   */
  public JMenu [] getAssociatedMenus();
  /**
   * Gets the button associated with the MDIFrame.
   * @return JButton The assocaited button.
   */
  public JButton getButton();
}
