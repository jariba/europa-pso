//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: NegationCheckBox.java,v 1.2 2003-06-16 16:28:07 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import javax.swing.JCheckBox;

public class NegationCheckBox extends JCheckBox {
  public NegationCheckBox() {
    super("NOT");
    setSize(55, 40);
  }
}
