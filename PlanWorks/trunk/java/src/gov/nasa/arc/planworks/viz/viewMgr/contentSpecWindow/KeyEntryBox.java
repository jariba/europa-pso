//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: KeyEntryBox.java,v 1.2 2003-06-16 16:28:07 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class KeyEntryBox extends JTextField {
  public KeyEntryBox() {
    super(5);
    add(new JLabel("Key"));
  }
}
