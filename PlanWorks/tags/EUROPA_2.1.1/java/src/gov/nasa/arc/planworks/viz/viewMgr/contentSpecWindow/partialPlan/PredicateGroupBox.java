//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PredicateGroupBox.java,v 1.3 2004-01-14 21:27:37 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.List;
import java.util.Map;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;

/**
 * <code>PredicateGroupBox</code> -
 *                      GroupBox->PredicateGroupBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A logical grouping of ContentSpecElements for specifying predicates.
 */

public class PredicateGroupBox extends GroupBox {
  private Map predicateNames;
  public PredicateGroupBox(MDIInternalFrame window, Map predicateNames) {
    super(window);
    build(predicateNames);
  }
  private void build(Map predicateNames) {
    this.predicateNames = predicateNames;
    GridBagLayout gridBag = (GridBagLayout) getLayout();
    GridBagConstraints c = new GridBagConstraints();
    
    c.weightx = 0.5;
    //c.weighty = 0.5;
    c.gridx = 0;
    c.gridy = 0;
    PredicateBox box1 = new PredicateBox(true, predicateNames);
    gridBag.setConstraints(box1, c);
    this.add((ContentSpecElement)box1);
    
    PredicateBox box2 = new PredicateBox(false, predicateNames);
    c.gridy++;
    gridBag.setConstraints(box2, c);
    this.add((ContentSpecElement)box2);
  }
  public void reset() {
    super.reset();
    build(predicateNames);
  }
}
