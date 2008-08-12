//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: UniqueKeyGroupBox.java,v 1.3 2004-07-13 23:54:10 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.Map;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;

public class UniqueKeyGroupBox extends GroupBox {
  private UniqueKeyBox box;
  public UniqueKeyGroupBox(MDIInternalFrame window) {
    super(window);
    build();
  }
  private void build() {
    GridBagLayout gridBag = (GridBagLayout) getLayout();
    GridBagConstraints c = new GridBagConstraints();
    
    c.weightx = 0.5;
    c.gridx = 0;
    c.gridy = 0;
    
    box = new UniqueKeyBox();
    gridBag.setConstraints(box, c);
    this.add((ContentSpecElement)box);
  }
  public void reset() {
    super.reset();
    build();
  }
  public UniqueKeyBox getUniqueKeyBox() {
    return box;
  }
}
