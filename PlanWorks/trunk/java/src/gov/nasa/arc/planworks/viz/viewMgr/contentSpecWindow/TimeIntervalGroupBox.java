//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: TimeIntervalGroupBox.java,v 1.2 2003-06-16 16:28:08 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;

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
