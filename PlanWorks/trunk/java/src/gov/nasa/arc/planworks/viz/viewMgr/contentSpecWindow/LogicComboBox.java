//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: LogicComboBox.java,v 1.2 2003-06-16 16:28:07 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import javax.swing.JComboBox;


public class LogicComboBox extends JComboBox {
  public LogicComboBox() {
    addItem("");
    addItem("AND");
    addItem("OR");
    setSize(58, 44);
  }
}
