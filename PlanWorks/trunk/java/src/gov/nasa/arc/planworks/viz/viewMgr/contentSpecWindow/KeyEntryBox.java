//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: KeyEntryBox.java,v 1.3 2003-06-16 18:51:09 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * <code>KeyEntryBox</code> -
 *                      JTextField->KeyEntryBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * The <code>JTextField</code> into which a key value is entered.
 */


public class KeyEntryBox extends JTextField {
  public KeyEntryBox() {
    super(5);
    add(new JLabel("Key"));
  }
}
