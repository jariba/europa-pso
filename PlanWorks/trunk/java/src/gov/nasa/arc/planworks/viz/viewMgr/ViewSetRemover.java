//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ViewSetRemover.java,v 1.2 2003-06-10 20:34:07 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr;

import gov.nasa.arc.planworks.db.PwPartialPlan;

public interface ViewSetRemover {
  public void removeViewSet(PwPartialPlan key);
}
