//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ClassGroupBox.java,v 1.1 2004-08-21 00:31:59 taylor Exp $
//
package gov.nasa.arc.planworks.viz.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.ContentSpecElement;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.GroupBox;

/**
 * <code>ClassGroupBox</code> -
 *                      GroupBox->ClassGroupBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A logical grouping of ContentSpecElements for specifying classes.
 */

public class ClassGroupBox extends GroupBox {
  private Map classNames;
  public ClassGroupBox(MDIInternalFrame window, Map classNames) {
    super(window);
    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    build(classNames);
  }
  private void build(Map classNames) {
    this.classNames = classNames;
    GridBagLayout gridBag = (GridBagLayout) getLayout();
    GridBagConstraints c = new GridBagConstraints();
    
    c.weightx = 0.5;
    //c.weighty = 0.5;
    c.gridx = 0;
    c.gridy = 0;
    ClassBox box1 = new ClassBox(true, " class ", classNames, new HashMap( classNames));
    gridBag.setConstraints(box1, c);
    this.add((ContentSpecElement)box1);
    
//     ClassBox box2 = new ClassBox(false, " class ", classNames);
//     c.gridy++;
//     gridBag.setConstraints(box2, c);
//     this.add((ContentSpecElement)box2);
  }
  public void reset() {
    super.reset();
    build(classNames);
  }
}
