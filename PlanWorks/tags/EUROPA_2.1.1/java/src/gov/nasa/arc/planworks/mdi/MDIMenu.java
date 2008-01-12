//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MDIMenu.java,v 1.4 2004-02-03 19:23:22 miatauro Exp $
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
  public void notifyActivated(final MDIFrame frame);
  public void notifyDeleted(final MDIFrame frame);
  public void addWindow(final MDIInternalFrame frame);
}
