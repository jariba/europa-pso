//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: VariableTypeGroupBox.java,v 1.3 2003-06-16 18:51:11 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;

/**
 * <code>VariableTypeGroupBox</code> -
 *            JPanel->VariableTypeGroupBox
 *            ContentSpecElement->VariableTypeBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * See the documentation for the GroupBox class.  This is one of those for VariableTypeBoxen.
 */

public class VariableTypeGroupBox extends GroupBox {
  public VariableTypeGroupBox(MDIInternalFrame window) {
    super(window);
    GridBagLayout gridBag = (GridBagLayout) getLayout();
    GridBagConstraints c = new GridBagConstraints();
    
    c.weightx = 0.5;
    //c.weighty = 0.5;
    c.gridx = 0;
    c.gridy = 0;
    VariableTypeBox box1 = new VariableTypeBox(true);
    gridBag.setConstraints(box1, c);
    this.add((ContentSpecElement)box1);
    
    VariableTypeBox box2 = new VariableTypeBox(false);
    c.gridy++;
    gridBag.setConstraints(box2, c);
    this.add((ContentSpecElement)box2);
  }
}
