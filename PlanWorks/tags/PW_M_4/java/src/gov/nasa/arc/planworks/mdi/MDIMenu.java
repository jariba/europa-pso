//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MDIMenu.java,v 1.2 2003-06-16 22:32:15 miatauro Exp $
//
package gov.nasa.arc.planworks.mdi;

/*
 * <code>MDIMenu</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * An interface to expose only the notifyActivated() method to MDIInternalFrame.
 */


public interface MDIMenu {
  /**
   * Notifies the MDIDynamicMenuBar that a frame has been activated and so its associated menus
   * should be drawn.
   */
  public void notifyActivated(MDIFrame frame);
}
