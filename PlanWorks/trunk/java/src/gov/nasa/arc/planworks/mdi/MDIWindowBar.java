//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MDIWindowBar.java,v 1.2 2003-06-16 22:32:15 miatauro Exp $
//
package gov.nasa.arc.planworks.mdi;

import javax.swing.JButton;

/*
 * <code>MDIWindowBar</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * An interface to expose only the notifyDeleted() and add() methods to MDIInternalFrame.
 */


public interface MDIWindowBar {
  /**
   * Notifies the MDIWindowButtonBar that the frame has been closed, and so its associated button
   * should be deleted.
   * @param frame The closed frame.
   */
  void notifyDeleted(MDIFrame frame);
  /**
   * Adds a button to the MDIWindowButtonBar.
   * @param button The button to be added.
   */
  void add(JButton button);
}
