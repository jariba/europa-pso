//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: LogicComboBox.java,v 1.1 2003-10-01 23:54:01 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

import javax.swing.JComboBox;

/**
 * <code>LogicComboBox</code> -
 *                      JComboBox->LogicComboBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * The combo box by which the user specifies a logical connective among ContentSpecElements.
 * Changing the value from blank to a connective ("AND" or "OR") creates an additional 
 * ContentSpecElement of the appropriate type.  Changing the value from a connective to blank 
 * removes the ContentSpecElement from the window.
 */

public class LogicComboBox extends JComboBox {
  public LogicComboBox() {
    addItem("");
    addItem("AND");
    addItem("OR");
    setSize(58, 44);
  }
}
