// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PartialPlanViewMenuItem.java,v 1.1 2003-10-09 22:07:45 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 09oct03
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.JPopupMenu;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.SequenceViewMenuItem;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.Utilities;


class PartialPlanViewMenuItem extends SequenceViewMenuItem {

  private String partialPlanName;

  public PartialPlanViewMenuItem( String viewName, String seqUrl, String seqName,
                                  String partialPlanName) {
    super( viewName, seqUrl, seqName);
    this.partialPlanName = partialPlanName;
  }

  public String getPartialPlanName() {
    return partialPlanName;
  }

} // end class PartialPlanViewMenuItem


