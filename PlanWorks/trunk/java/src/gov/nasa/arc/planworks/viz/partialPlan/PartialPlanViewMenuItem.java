// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PartialPlanViewMenuItem.java,v 1.3 2004-04-22 19:26:23 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 09oct03
//

package gov.nasa.arc.planworks.viz.partialPlan;

import gov.nasa.arc.planworks.SequenceViewMenuItem;
import gov.nasa.arc.planworks.viz.ViewListener;


/**
 * <code>PartialPlanViewMenuItem</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *             NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PartialPlanViewMenuItem extends SequenceViewMenuItem {

  private String partialPlanName;

  /**
   * <code>PartialPlanViewMenuItem</code> - constructor 
   *
   * @param viewName - <code>String</code> - 
   * @param seqUrl - <code>String</code> - 
   * @param seqName - <code>String</code> - 
   * @param partialPlanName - <code>String</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public PartialPlanViewMenuItem( final String viewName, final String seqUrl,
                                  final String seqName, final String partialPlanName,
                                  final ViewListener viewListener) {
    super( viewName, seqUrl, seqName, viewListener);
    this.partialPlanName = partialPlanName;
  }

  /**
   * <code>getPartialPlanName</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getPartialPlanName() {
    return partialPlanName;
  }

  
} // end class PartialPlanViewMenuItem


