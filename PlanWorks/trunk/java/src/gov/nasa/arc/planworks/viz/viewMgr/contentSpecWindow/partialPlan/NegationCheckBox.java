//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: NegationCheckBox.java,v 1.1 2003-10-01 23:54:01 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

import javax.swing.JCheckBox;

/**
 * <code>NegationCheckBox</code> -
 *                      JCheckBox->NegationCheckBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A check box used for negating a particular connective.
 */


public class NegationCheckBox extends JCheckBox {
  public NegationCheckBox() {
    super("NOT");
    setSize(55, 40);
  }
}
