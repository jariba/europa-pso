//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: TimelineGroupBox.java,v 1.3 2004-07-13 23:54:09 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.Map;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;

/**
 * <code>TimelineGroupBox</code> -
 *                      GroupBox->TimelineGroupBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A logical grouping of ContentSpecElements for specifying timelines.
 */

public class TimelineGroupBox extends GroupBox {
  private Map timelineNames;
  public TimelineGroupBox(MDIInternalFrame window, Map timelineNames) {
    super(window);
    build(timelineNames);
  }
  private void build(Map timelineNames) {
    this.timelineNames = timelineNames;
    GridBagLayout gridBag = (GridBagLayout) getLayout();
    GridBagConstraints c = new GridBagConstraints();
    
    c.weightx = 0.5;
    //c.weighty = 0.5;
    c.gridx = 0;
    c.gridy = 0;
    TimelineBox box1 = new TimelineBox(true, timelineNames);
    gridBag.setConstraints(box1, c);
    this.add((ContentSpecElement)box1);
    
    TimelineBox box2 = new TimelineBox(false, timelineNames);
    c.gridy++;
    gridBag.setConstraints(box2, c);
    this.add((ContentSpecElement)box2);
  }
  public void reset() {
    super.reset();
   build(timelineNames);
  }
}
