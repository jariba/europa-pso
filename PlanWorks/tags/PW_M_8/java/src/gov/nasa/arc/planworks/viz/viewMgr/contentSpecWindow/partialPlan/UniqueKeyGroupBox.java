//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: UniqueKeyGroupBox.java,v 1.2 2003-10-09 17:23:32 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.Map;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;

public class UniqueKeyGroupBox extends GroupBox {
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
    
    UniqueKeyBox box = new UniqueKeyBox();
    gridBag.setConstraints(box, c);
    this.add((ContentSpecElement)box);
  }
  public void reset() {
    super.reset();
    build();
  }
}
