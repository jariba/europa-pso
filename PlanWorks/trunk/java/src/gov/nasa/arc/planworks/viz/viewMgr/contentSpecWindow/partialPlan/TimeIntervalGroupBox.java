//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: TimeIntervalGroupBox.java,v 1.1 2003-10-01 23:54:02 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;

/**
 * <code>TimeIntervalGroupBox</code> -
 *            JPanel->TimeIntervalGroupBox
 *            ContentSpecElement->TimeIntervalBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * See the documentation for the GroupBox class.  This is one of those for TimeIntervalBoxen.
 */


public class TimeIntervalGroupBox extends GroupBox {
  public TimeIntervalGroupBox(MDIInternalFrame window) {
    super(window);
    GridBagLayout gridBag = (GridBagLayout) getLayout();
    GridBagConstraints c = new GridBagConstraints();
    
    c.weightx = 0.5;
    //c.weighty = 0.5;
    c.gridx = 0;
    c.gridy = 0;
    TimeIntervalBox box1 = new TimeIntervalBox(true);
    gridBag.setConstraints(box1, c);
    this.add((ContentSpecElement)box1);
    
    TimeIntervalBox box2 = new TimeIntervalBox(false);
    c.gridy++;
    gridBag.setConstraints(box2, c);
    this.add((ContentSpecElement)box2);
  }
}
